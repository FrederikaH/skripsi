package com.polygonbikes.ebike.v3.feature_trip.presentation.save_trip

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.navOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.FeatureList
import com.polygonbikes.ebike.core.dao.LogTrackDao
import com.polygonbikes.ebike.core.database.LogDatabase
import com.polygonbikes.ebike.core.route.HistoryRoute
import com.polygonbikes.ebike.core.route.TripRoute
import com.polygonbikes.ebike.core.service.TripService
import com.polygonbikes.ebike.core.util.AlertDialogData
import com.polygonbikes.ebike.core.util.Coordinate
import com.polygonbikes.ebike.core.util.GooglePolylineUtils
import com.polygonbikes.ebike.core.util.TimeUtil
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.core.util.UiEvent.AlertDialog
import com.polygonbikes.ebike.core.util.UiEvent.Navigate
import com.polygonbikes.ebike.core.util.UiEvent.PickImage
import com.polygonbikes.ebike.core.util.UiEvent.PickImages
import com.polygonbikes.ebike.core.util.calculateElevationGain
import com.polygonbikes.ebike.core.util.compressImage
import com.polygonbikes.ebike.core.util.createFITFile
import com.polygonbikes.ebike.core.util.createMultipartRequestBody
import com.polygonbikes.ebike.feature_trip.domain.entities.TripBody
import com.polygonbikes.ebike.feature_trip.presentation.TripViewModel.Companion.KEY_CONFIRMATION_DISCARD
import com.polygonbikes.ebike.v3.feature_trip.data.entities.response.SaveTripResponse
import com.polygonbikes.ebike.v3.feature_trip.data.remote.TripServiceMiddleware
import com.polygonbikes.ebike.v3.feature_trip.domain.state.BikeType
import com.polygonbikes.ebike.v3.feature_trip.domain.state.SaveTripState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.jenetics.jpx.GPX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SaveTripViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val tripServiceMiddleware: TripServiceMiddleware
) : ViewModel() {
    private val TAG = "SaveTripVM"
    private var _state: MutableState<SaveTripState> = mutableStateOf(SaveTripState())
    val state: State<SaveTripState> get() = _state

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val logDatabase: LogDatabase = LogDatabase.getDatabase(context = context)
    private val logTrackDao: LogTrackDao = logDatabase.logTrackDao()

    private var tripService: TripService? = null

    private var _mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: TripService.TripBinder = service as TripService.TripBinder
            tripService = binder.service

            viewModelScope.launch {
                tripService?.tripState?.value?.let { tripState ->
                    if (state.value.eBikeName == null) {
                        _state.value = state.value.copy(
                            eBikeName = tripState.bikeName,
                            bikeName = tripState.bikeName
                        )
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "onServiceDisconnected $name")
            tripService = null
        }
    }

    fun onEvent(event: SaveTripEvent) {
        when (event) {
            is SaveTripEvent.SetBikeType -> {
                _state.value = state.value.copy(bikeType = event.type)
            }

            is SaveTripEvent.Init -> {
                viewModelScope.launch {
                    val rawLogTrack = logTrackDao.fetchByRideId(event.tripId)
                    val logTrack = rawLogTrack.filter {
                        it.latitude != null && it.longitude != null
                    }

                    val firstLog = logTrack.first()
                    val lastLog = logTrack.last()
                    val listCoordinate: List<Coordinate> = logTrack
                        .filter { it.latitude != null && it.longitude != null}
                        .map {
                        Coordinate(
                            longitude = it.longitude!!,
                            latitude = it.latitude!!
                        )
                    }

                    val gpxFile = createGPXFile(event.tripId, listCoordinate)

                    val speeds = logTrack.filter { it.speed != null && it.speed != 0.0 }.map { it.speed!! }

                    val movingTime: Long = event.movingTime
                    val elapsedTime: Long = event.elapsedTime
                    val maxSpeed = speeds.fold(0.0) { acc, speed -> maxOf(acc, speed) }
                    val distance: Double = lastLog.distance ?: 0.0
                    val avgSpeed: Double = if (movingTime > 0) distance / (movingTime / 3600.0) else 0.0
                    val polyline = GooglePolylineUtils.encode(listCoordinate)
                    val elevationGain = calculateElevationGain(rawLogTrack)

                    val tripBody = TripBody(
                        tripId = event.tripId,
                        time = TimeUtil.getFormattedDate(
                            epoch = firstLog.timestamp ?: System.currentTimeMillis(),
                            format = TimeUtil.FormatSimpleDate
                        ),
                        startTime = firstLog.timestamp,
                        endTime = lastLog.timestamp ?: System.currentTimeMillis(),
                        track = rawLogTrack,
                        polyline = polyline,
                        maxSpeed = maxSpeed,
                        avgSpeed = avgSpeed,
                        movingTime = movingTime,
                        elapsedTime = elapsedTime,
                        distance = distance,
                        bikeName = state.value.bikeName,
                        uploadId = null,
                        activityId = null,
                        fit = null,
                        events = null
                    )

                    _state.value = state.value.copy(
                        timestamp = firstLog.timestamp ?: System.currentTimeMillis(),
                        movingTime = movingTime,
                        elapsedTime = elapsedTime,
                        distance = distance,
                        maxSpeed = maxSpeed,
                        avgSpeed = avgSpeed,
                        polyline = polyline,
                        elevation = elevationGain,
                        tripBody = tripBody,
                        tracks = rawLogTrack,
                        gpxFile = gpxFile,
                        eBikeName = event.bikeName,
                        bikeName = event.bikeName ?: ""
                    )
                    gpxFile?.let { parseGpxAndSetMapData(it) }
                }

                viewModelScope.launch {
                    Intent(context, TripService::class.java).also {
                        if (FeatureList.ExperimentalTripService) {
                            context.bindService(it, _mConnection, 0)
                        } else {
                            context.bindService(it, _mConnection, Context.BIND_AUTO_CREATE)
                        }
                    }
                }
            }

            is SaveTripEvent.Create -> {
                if (state.value.tripBody == null)
                {
                    return
                }
                _state.value = state.value.copy(
                    isEmptyTripName = state.value.name.isBlank(),
                    isEmptyBikeName = state.value.bikeName.isBlank(),
                    isEmptyRoadType = state.value.roadType.isEmpty(),
                    isEmptyPurpose = state.value.purpose.isBlank(),
                    isEmptyImage = state.value.headerImageFiles.isEmpty()
                )

                if (!state.value.name.isBlank() && !state.value.bikeName.isBlank() && !state.value.roadType.isEmpty()
                    && !state.value.purpose.isBlank() && !state.value.headerImageFiles.isEmpty()){
                    _state.value = state.value.copy(isLoading = true)

//                    val imageRequestBody = MultipartBody.Part.createFormData(
//                        name = "images[]",
//                        filename = "image.jpg",
//                        body = state.value.headerImageFile!!.asRequestBody("image/*".toMediaTypeOrNull())
//                    )

                    val imageRequestBodies = state.value.headerImageFiles.mapIndexed { index, file ->
                        MultipartBody.Part.createFormData(
                            name = "images[$index]",
                            filename = "image_$index.jpg",
                            RequestBody.create("image/*".toMediaTypeOrNull(), file.readBytes())
                        )
                    }

                    val fileFit = createFITFile(
                        context = context,
                        tripBody = state.value.tripBody!!,
                        listTrack = state.value.tracks,
                        usingEbike = state.value.bikeType == BikeType.EBIKE
                    )

                    val fitPart = MultipartBody.Part.createFormData(
                        name = "fit",
                        filename = fileFit.name,
                        body = fileFit.asRequestBody("application/octet-stream".toMediaType())
                    )
                    
                    val fileGpx = state.value.gpxFile ?: throw Exception("Failed to create GPX file")

                    val gpxPart = MultipartBody.Part.createFormData(
                        name = "gpx",
                        filename = fileGpx.name,
                        body = fileGpx.asRequestBody("application/gpx+xml".toMediaTypeOrNull())
                    )

                    val bikeName = if (state.value.bikeType == BikeType.EBIKE)
                        state.value.eBikeName ?: ""
                    else
                        state.value.bikeName

                    val call: Call<SaveTripResponse> = tripServiceMiddleware.saveTrip(
                        name = createMultipartRequestBody(state.value.name),
                        timestamp = createMultipartRequestBody(state.value.timestamp),
                        movingTime = createMultipartRequestBody(state.value.movingTime),
                        elapsedTime = createMultipartRequestBody(state.value.elapsedTime),
                        avgSpeed = createMultipartRequestBody(state.value.avgSpeed),
                        maxSpeed = createMultipartRequestBody(state.value.maxSpeed),
                        bikeName = createMultipartRequestBody(bikeName),
                        startLocation = createMultipartRequestBody(state.value.startLocation),
                        startLatitude = createMultipartRequestBody(state.value.startLatitude),
                        startLongitude = createMultipartRequestBody(state.value.startLongitude),
                        roadType = createMultipartList(state.value.roadType),
                        distance = createMultipartRequestBody(state.value.distance),
                        elevation = createMultipartRequestBody(state.value.elevation.toLong()),
                        purpose = createMultipartRequestBody(state.value.purpose),
                        polyline = createMultipartRequestBody(state.value.polyline),
                        fitFile = fitPart,
                        gpxFile = gpxPart,
//                        imageFile = imageRequestBody,
                        imageFiles = imageRequestBodies,
                        isPrivate = createMultipartRequestBody(state.value.isPrivate.toString())
                    )

                    call.enqueue(object : Callback<SaveTripResponse> {
                        override fun onResponse(call: Call<SaveTripResponse?>, response: Response<SaveTripResponse?>) {
                            Intent(context, TripService::class.java).apply {
                                action = "discard"
                                context.startService(this)
                            }

                            _state.value = state.value.copy(isLoading = false)
                            sendUiEvent(Navigate(
                                route = HistoryRoute,
                                navOptions = navOptions {
                                    popUpTo<TripRoute> {
                                        inclusive = true
                                    }
                                }
                            ))
                        }

                        override fun onFailure(
                            call: Call<SaveTripResponse?>,
                            t: Throwable
                        ) {
                            _state.value = state.value.copy(
                                isLoading = false
                            )

                            val errorMessage = t.message ?: "Unknown error occurred"
                            t.printStackTrace()
                            Toast.makeText(context, "Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    if (state.value.name.isBlank()) {
                        Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                    } else if (state.value.bikeName.isBlank()) {
                        Toast.makeText(context, "Please enter your bike name", Toast.LENGTH_SHORT).show()
                    } else if (state.value.roadType.isEmpty()) {
                        Toast.makeText(context, "Please select a road type", Toast.LENGTH_SHORT).show()
                    } else if (state.value.purpose.isBlank()) {
                        Toast.makeText(context, "Please enter the purpose", Toast.LENGTH_SHORT).show()
                    } else if (state.value.headerImageFiles.isEmpty()) {
                        Toast.makeText(context, "Please add a header image", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please fill all required data", Toast.LENGTH_SHORT).show()
                    }

                }

            }

            is SaveTripEvent.SetValueName -> {
                _state.value = state.value.copy(
                    name = event.value,
                    isEmptyTripName = event.value.isBlank()
                )
            }

            is SaveTripEvent.PickImage -> {
                sendUiEvent(PickImage)
                _state.value = state.value.copy(
                    isEmptyImage = state.value.headerImageBitmap == null
                )
            }

            is SaveTripEvent.PickImages -> {
                sendUiEvent(PickImages)
                _state.value = state.value.copy(
                    isEmptyImage = state.value.headerImageFiles.isEmpty()
                )
            }


            is SaveTripEvent.SetImageUri -> {
                _state.value = state.value.copy(
                    compressingImage = true,
                )

                viewModelScope.launch {
                    val inputStream = context.contentResolver.openInputStream(event.data) ?: return@launch
                    val temporaryFile = File.createTempFile("temp_header_", ".jpeg")
                    val compressedBitmap = compressImage(inputStream = inputStream, outputFile = temporaryFile)

                    _state.value = state.value.copy(
                        compressingImage = false,
                        headerImageBitmap = compressedBitmap,
                        headerImageFile = temporaryFile
                    )
                }
            }

            is SaveTripEvent.SetImageUris -> {
                _state.value = state.value.copy(compressingImage = true)

                viewModelScope.launch {
                    val updatedFiles = state.value.headerImageFiles.toMutableList()
                    val updatedUris = state.value.headerImageUris.toMutableList()

                    event.data.forEach { uri ->
                        val inputStream = context.contentResolver.openInputStream(uri)
                        if (inputStream == null) {
                            Log.e(TAG, "Failed to open InputStream for URI: $uri")
                            return@forEach
                        }

                        val tempFile = File.createTempFile("temp_image_", ".jpeg")
                        compressImage(inputStream, tempFile)

                        updatedFiles.add(tempFile)
                        updatedUris.add(uri)

//                        Log.d(TAG, "Added image file: ${tempFile.absolutePath}")
//                        Log.d(TAG, "Updated image URIs: ${updatedUris.size}")
                    }

                    _state.value = state.value.copy(
                        compressingImage = false,
                        headerImageFiles = updatedFiles,
                        headerImageUris = updatedUris
                    )

//                    Log.d(TAG, "Final headerImageFiles size: ${_state.value.headerImageFiles?.size}")
//                    Log.d(TAG, "Final headerImageUris size: ${_state.value.headerImageUris.size}")
                }
            }


            is SaveTripEvent.SetValueBikeName -> {
                _state.value = state.value.copy(
                    bikeName = event.value,
                    isEmptyBikeName = event.value.isBlank()
                )
            }

            is SaveTripEvent.SetValueRoadType -> {
                Log.d("RoadType", "Received road types: ${event.value}")

                _state.value = state.value.copy(
                    roadType = event.value,
                    isEmptyRoadType = event.value.isEmpty()
                )
            }

            is SaveTripEvent.SetValuePurpose -> {
                _state.value = state.value.copy(
                    purpose = event.value,
                    isEmptyPurpose = event.value.isBlank()
                )

            }

            is SaveTripEvent.OpenHistoryScreen -> {
                sendUiEvent(Navigate(route = HistoryRoute))
            }

            is SaveTripEvent.ConfirmationDiscardTrip -> {
                sendUiEvent(
                    AlertDialog(
                        data = AlertDialogData(
                            id = KEY_CONFIRMATION_DISCARD,
                            title = "Discard Trip",
                            description = "Are you sure you want to discard this trip?",
                            dismissCaption = "Cancel",
                            confirmCaption = "Discard",
                            dismissButtonColor = R.color.sub_text,
                            confirmButtonColor = R.color.red
                        )
                    )
                )
            }

            is SaveTripEvent.DiscardTrip -> {
                discardTrip()
            }

            is SaveTripEvent.SetIsPrivate -> {
                _state.value = state.value.copy(isPrivate = event.value)
            }

            is SaveTripEvent.AddImageUri -> {
                val updatedUris = state.value.headerImageUris.toMutableList()
                if (updatedUris.size < 3) {
                    updatedUris.add(event.uri)
                    _state.value = state.value.copy(headerImageUris = updatedUris)
                }
            }

            is SaveTripEvent.RemoveImageUri -> {
                val updatedUris = state.value.headerImageUris.toMutableList()
                updatedUris.remove(event.uri)
                _state.value = state.value.copy(headerImageUris = updatedUris)
            }
        }
    }

    private fun discardTrip() {
        Intent(context, TripService::class.java).apply {
            action = "discard"
            context.startService(this)
        }
        sendUiEvent(Navigate(route = TripRoute(code = null)))
    }

    private fun createGPXFile(tripId: String, coordinates: List<Coordinate>): File? {
        if (coordinates.isEmpty()) return null

        coordinates

        return try {
            val gpx = GPX.builder()
                .addTrack { track ->
                    track.addSegment { segment ->
                        coordinates.forEach { coord ->
                            segment.addPoint { p ->
                                p.lat(coord.latitude)
                                    .lon(coord.longitude)
                                    .ele(coord.elevation)
                            }
                        }
                    }
                }
                .build()

            val gpxFolder = File(context.filesDir, "gpx")
            if (!gpxFolder.exists()) {
                gpxFolder.mkdirs()
            }

            val gpxFile = File(gpxFolder, "trip_${tripId.replace(Regex("[^a-zA-Z0-9_]"), "_")}.gpx")
            if (gpxFile.exists()) {
                gpxFile.delete()
            }

            GPX.write(gpx, gpxFile.toPath())
            gpxFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create GPX file: ${e.message}")
            null
        }
    }

    private fun parseGpxAndSetMapData(gpxFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val gpxOutput = GPX.read(gpxFile.toPath())

                if (gpxOutput.tracks.isEmpty()) {
                    sendUiEvent(UiEvent.ShowSnackBar(message = "GPX file has no tracks"))
                    return@launch
                }

                val routeWaypoints = gpxOutput.tracks.flatMap { it.segments }
                    .flatMap { it.points }
                    .map { LatLng(it.latitude.toDegrees(), it.longitude.toDegrees()) }

                _state.value = state.value.copy(
                    routeWaypoint = routeWaypoints
                )
                _state.value.bounds.value = routeWaypoints.getBounds()



            } catch (e: Exception) {
                sendUiEvent(UiEvent.ShowSnackBar(message = "Failed to load GPX"))
            }
        }
    }

    private fun List<LatLng>.getBounds(): LatLngBounds =
        LatLngBounds.Builder().apply { forEach { include(it) } }.build()

    private fun createMultipartList(list: List<String>): List<RequestBody> {
        return list.map {
            val cleaned = it.trim('"')
            cleaned.toRequestBody("text/plain".toMediaTypeOrNull())
        }
    }
    override fun onCleared() {
        try {
            state.value.headerImageFile?.delete()
            state.value.gpxFile?.delete()

        } catch (e: Exception) {
            Log.d(TAG, e.message.toString())
            e.printStackTrace()
        }
        super.onCleared()
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}