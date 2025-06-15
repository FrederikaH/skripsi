package com.polygonbikes.ebike.v3.feature_route.presentation.route_management.create_route

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.navOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds.Builder
import com.polygonbikes.ebike.core.dao.LogTrackDao
import com.polygonbikes.ebike.core.database.LogDatabase
import com.polygonbikes.ebike.core.entities.response.LocationResponse
import com.polygonbikes.ebike.core.enums.ProcessGPXStatus
import com.polygonbikes.ebike.core.network.LocationServiceMiddleware
import com.polygonbikes.ebike.core.route.CreateRouteRoute
import com.polygonbikes.ebike.core.route.DetailRouteManagementRoute
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.core.util.UiEvent.Navigate
import com.polygonbikes.ebike.core.util.UiEvent.PickGPX
import com.polygonbikes.ebike.core.util.UiEvent.PickImages
import com.polygonbikes.ebike.core.util.UiEvent.ShowSnackBar
import com.polygonbikes.ebike.core.util.compressImage
import com.polygonbikes.ebike.core.util.createMultipartRequestBody
import com.polygonbikes.ebike.core.util.sizeValidAsGPXFile
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.DetailRouteResponseMiddleware
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.v3.feature_route.data.remote.RouteServiceMiddleware
import com.polygonbikes.ebike.v3.feature_route.domain.state.CreateRouteState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.jenetics.jpx.GPX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CreateRouteViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val routeServiceMiddleware: RouteServiceMiddleware,
    private val locationServiceMiddleware: LocationServiceMiddleware
) : ViewModel() {
    private val TAG = "CreateRouteVM"
    private var _state: MutableState<CreateRouteState> = mutableStateOf(CreateRouteState())
    val state: State<CreateRouteState> get() = _state

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val logDatabase: LogDatabase = LogDatabase.getDatabase(context = context)
    private val logTrackDao: LogTrackDao = logDatabase.logTrackDao()

    init {
        fetchLocations()
    }

    fun onEvent(event: CreateRouteEvent) {
        when (event) {
            is CreateRouteEvent.AddImageUri -> {
                val updatedUris = state.value.headerImageUris.toMutableList()
                if (updatedUris.size < 3) {
                    updatedUris.add(event.uri)
                    _state.value = state.value.copy(headerImageUris = updatedUris)
                }
            }

            CreateRouteEvent.Create -> {
                _state.value = state.value.copy(
                    isEmptyRouteName = state.value.name.isBlank(),
                    isEmptyGpxFile = state.value.gpxFile == null,
                    isEmptyCity = state.value.selectedCityId == null,
                    isEmptyRoadType = state.value.roadType.isEmpty(),
                    isEmptyPurpose = state.value.purpose.isBlank(),
                    isEmptyImage = state.value.headerImageFiles.isEmpty()
                )

                if (!(state.value.isEmptyRouteName || state.value.isEmptyRoadType ||
                            state.value.isEmptyPurpose || state.value.isEmptyImage ||
                            state.value.isEmptyGpxFile || state.value.isEmptyCity)) {

                    _state.value = state.value.copy(isLoading = true)

                    val imageRequestBodies = state.value.headerImageFiles.mapIndexed { index, file ->
                        MultipartBody.Part.createFormData(
                            name = "images[$index]",
                            filename = "image_$index.jpg",
                            RequestBody.create("image/*".toMediaTypeOrNull(), file.readBytes())
                        )
                    }

                    var gpxRequestBody: MultipartBody.Part? = null
                    state.value.gpxFile?.let { file ->
                        gpxRequestBody = MultipartBody.Part.createFormData(
                            name = "file",
                            filename = "route.gpx",
                            RequestBody.create(
                                "application/gpx+xml".toMediaTypeOrNull(),
                                file.readBytes()
                            )
                        )
                    }

                    val call = routeServiceMiddleware.createRoute(
                        name = createMultipartRequestBody(state.value.name),
                        locationId = createMultipartRequestBody(state.value.selectedCityId.toString()),
                        startLatitude = createMultipartRequestBody(state.value.startLatitude),
                        startLongitude = createMultipartRequestBody(state.value.startLongitude),
                        roadType = createMultipartList(state.value.roadType),
                        distance = createMultipartRequestBody(state.value.distance),
                        elevation = createMultipartRequestBody(state.value.elevation.toLong()),
                        purpose = createMultipartRequestBody(state.value.purpose),
                        polyline = createMultipartRequestBody(state.value.polyline),
                        file = gpxRequestBody,
                        imageFiles = imageRequestBodies
                    )

                    call.enqueue(object : Callback<DetailRouteResponseMiddleware> {
                        override fun onResponse(
                            call: Call<DetailRouteResponseMiddleware>,
                            response: Response<DetailRouteResponseMiddleware>
                        ) {
                            Log.d(TAG, "onResponse: $response")
                            _state.value = state.value.copy(isLoading = false)
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Success create route", Toast.LENGTH_SHORT)
                                    .show()
                                response.body()?.data?.let { route ->
                                    sendUiEvent(
                                        Navigate(
                                            route = DetailRouteManagementRoute(route = route),
                                            navOptions = navOptions {
                                                popUpTo<CreateRouteRoute> {
                                                    inclusive = true
                                                }
                                            }
                                        )
                                    )
                                }
                            } else {
                                Toast.makeText(context, "Failed create route", Toast.LENGTH_SHORT)
                                    .show()
                                _state.value = state.value.copy(isCreateRouteEnabled = true)
                            }
                        }

                        override fun onFailure(call: Call<DetailRouteResponseMiddleware?>, t: Throwable) {
                            Log.e(TAG, "onFailure: ${t.message}")
                            _state.value = state.value.copy(isLoading = false)
                            _state.value = state.value.copy(isCreateRouteEnabled = true)
                            Toast.makeText(context, "Failed create event", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    when {
                        state.value.isEmptyRouteName ->
                            Toast.makeText(context, "Please enter the route's name", Toast.LENGTH_SHORT).show()
                        state.value.isEmptyGpxFile ->
                            Toast.makeText(context, "GPX file is missing", Toast.LENGTH_SHORT).show()
                        state.value.isEmptyCity ->
                            Toast.makeText(context, "Please select a city", Toast.LENGTH_SHORT).show()
                        state.value.isEmptyImage ->
                            Toast.makeText(context, "Please add route previews", Toast.LENGTH_SHORT).show()
                        state.value.isEmptyRoadType ->
                            Toast.makeText(context, "Please select the road type", Toast.LENGTH_SHORT).show()
                        state.value.isEmptyPurpose ->
                            Toast.makeText(context, "Please select the purpose", Toast.LENGTH_SHORT).show()
                        else ->
                            Toast.makeText(context, "Please fill all required data", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            CreateRouteEvent.PickImages -> {
                sendUiEvent(PickImages)
                _state.value = state.value.copy(
                    isEmptyImage = state.value.headerImageFiles.isEmpty()
                )
            }

            is CreateRouteEvent.RemoveImageUri -> {
                val updatedUris = state.value.headerImageUris.toMutableList()
                updatedUris.remove(event.uri)
                _state.value = state.value.copy(headerImageUris = updatedUris)
            }

            is CreateRouteEvent.SetImageUris -> {
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
                    }

                    _state.value = state.value.copy(
                        compressingImage = false,
                        headerImageFiles = updatedFiles,
                        headerImageUris = updatedUris
                    )
                }
            }

            is CreateRouteEvent.SetValueName -> {
                _state.value = state.value.copy(
                    name = event.value,
                    isEmptyRouteName = event.value.isBlank()
                )
            }

            is CreateRouteEvent.SetValuePurpose -> {
                _state.value = state.value.copy(
                    purpose = event.value,
                    isEmptyPurpose = event.value.isBlank()
                )
            }

            is CreateRouteEvent.SetValueRoadType -> {
                Log.d("RoadType", "Received road types: ${event.value}")

                _state.value = state.value.copy(
                    roadType = event.value,
                    isEmptyRoadType = event.value.isEmpty()
                )
            }

            is CreateRouteEvent.OnInputLocationCoordinate -> {
                _state.value = state.value.copy(
                    locationCoordinate = event.value,
                    showDialogPickMaps = false
                )
            }

            is CreateRouteEvent.OnInputLocationId -> {
                _state.value = state.value.copy(locationId = event.value)
            }

            CreateRouteEvent.PickGPX -> {
                sendUiEvent(PickGPX)
            }

            is CreateRouteEvent.SelectCity -> {
                val selectedCity = state.value.listCity.find { it.id == event.locationData?.id }
                _state.value = state.value.copy(
                    selectedCityId = event.locationData?.id,
                    selectedCityName = selectedCity?.city ?: "",
                )
            }

            is CreateRouteEvent.SetGpxUri -> {
                Log.d(TAG, "Received GPX file URI: ${event.data}")
                _state.value = state.value.copy(processGPXStatus = ProcessGPXStatus.PARSING)

                CoroutineScope(Dispatchers.IO).launch {
                    val temporaryFile = File.createTempFile("temp_gpx_", ".gpx")
                    try {
                        val inputStream = context.contentResolver.openInputStream(event.data)
                        if (inputStream == null) {
                            viewModelScope.launch {
                                _state.value = state.value.copy(
                                    processGPXStatus = ProcessGPXStatus.GENERAL_FAILURE
                                )
                                sendUiEvent(ShowSnackBar(message = "Failed to open GPX file"))
                            }
                            return@launch
                        }

                        temporaryFile.outputStream().use { output ->
                            inputStream.use { input ->
                                input.copyTo(output)
                            }
                        }

                        if (!temporaryFile.sizeValidAsGPXFile()) {
                            viewModelScope.launch {
                                _state.value = state.value.copy(
                                    processGPXStatus = ProcessGPXStatus.FILE_TOO_LARGE
                                )
                                sendUiEvent(ShowSnackBar(message = "Select a file smaller than 5MB"))
                            }
                            return@launch
                        }

                        val gpxOutput = GPX.read(temporaryFile.toPath())
                        if (gpxOutput.tracks.isEmpty()) {
                            viewModelScope.launch {
                                _state.value = state.value.copy(
                                    processGPXStatus = ProcessGPXStatus.ROUTE_EMPTY
                                )
                                sendUiEvent(ShowSnackBar(message = "GPX file must have tracks"))
                            }
                            return@launch
                        }

                        val routeWaypoints = gpxOutput.tracks
                            .flatMap { it.segments }
                            .flatMap { it.points }
                            .map { LatLng(it.latitude.toDegrees(), it.longitude.toDegrees()) }

                        Log.d(TAG, "Extracted ${routeWaypoints.size} waypoints from GPX file")

                        val polylineString = encodePolyline(routeWaypoints)
                        Log.d(TAG, "Generated Polyline: $polylineString")

                        val builderLatLngBounds = Builder()
                        routeWaypoints.forEach { builderLatLngBounds.include(it) }
                        val bounds = builderLatLngBounds.build()

                        _state.value = state.value.copy(
                            processGPXStatus = ProcessGPXStatus.SUCCESS,
                            gpxFile = temporaryFile,
//                            name = routeName,
                            routeWaypoint = routeWaypoints,
                            polyline = polylineString
                        )
                        _state.value.bounds.update { bounds }

                    } catch (e: IOException) {
                        Log.e(TAG, "IOException: ${e.message}")
                        _state.value =
                            state.value.copy(processGPXStatus = ProcessGPXStatus.NOT_VALID_GPX)
                    } catch (e: FileNotFoundException) {
                        Log.e(TAG, "FileNotFoundException: ${e.message}")
                        _state.value =
                            state.value.copy(processGPXStatus = ProcessGPXStatus.GENERAL_FAILURE)
                    }
                }
            }

            is CreateRouteEvent.SetShowModalLocation -> {
                _state.value = state.value.copy(isLocationModalShowing = event.isShowing)
            }

            is CreateRouteEvent.SetCreateEventFalse -> {
                if (state.value.isCreateRouteEnabled == false) {
                    return
                } else {
                    _state.value = state.value.copy(isCreateRouteEnabled = false)
                }
            }
        }
    }

    private fun createMultipartList(list: List<String>): List<RequestBody> {
        return list.map {
            val cleaned = it.trim('"')
            cleaned.toRequestBody("text/plain".toMediaTypeOrNull())
        }
    }

    private fun encodePolyline(points: List<LatLng>): String {
        val result = StringBuilder()
        var prevLat = 0
        var prevLng = 0

        for (point in points) {
            val lat = (point.latitude * 1e5).toInt()
            val lng = (point.longitude * 1e5).toInt()

            val deltaLat = lat - prevLat
            val deltaLng = lng - prevLng

            prevLat = lat
            prevLng = lng

            result.append(encodeSignedNumber(deltaLat))
            result.append(encodeSignedNumber(deltaLng))
        }
        return result.toString()
    }

    private fun encodeSignedNumber(value: Int): String {
        var sValue = value shl 1
        if (value < 0) sValue = sValue.inv()

        val encodedString = StringBuilder()
        while (sValue >= 0x20) {
            encodedString.append(((0x20 or (sValue and 0x1F)) + 63).toChar())
            sValue = sValue shr 5
        }
        encodedString.append((sValue + 63).toChar())

        return encodedString.toString()
    }

    private fun fetchLocations() {
        locationServiceMiddleware.GetLocation().enqueue(object : Callback<LocationResponse> {
            override fun onResponse(
                call: Call<LocationResponse>,
                response: Response<LocationResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { locationList ->
                        _state.value.listCity.clear()
                        _state.value.listCity.addAll(locationList)
                    }
                } else {
                    Log.e("CreateEventVM", "Failed to fetch locations: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LocationResponse>, t: Throwable) {
                Log.e("CreateEventVM", "Error fetching locations: ${t.message}")
            }
        })
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}