package com.polygonbikes.ebike.v3.feature_route.presentation.route_management.edit_route

import android.content.Context
import android.net.Uri
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
import com.google.android.gms.maps.model.LatLngBounds.Builder
import com.polygonbikes.ebike.core.dao.LogTrackDao
import com.polygonbikes.ebike.core.database.LogDatabase
import com.polygonbikes.ebike.core.entities.response.LocationResponse
import com.polygonbikes.ebike.core.enums.ProcessGPXStatus
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.core.network.LocationServiceMiddleware
import com.polygonbikes.ebike.core.route.EditRouteRoute
import com.polygonbikes.ebike.core.route.DetailRouteManagementRoute
import com.polygonbikes.ebike.core.util.GooglePolylineUtils
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.core.util.UiEvent.Navigate
import com.polygonbikes.ebike.core.util.UiEvent.PickGPX
import com.polygonbikes.ebike.core.util.UiEvent.PickImages
import com.polygonbikes.ebike.core.util.UiEvent.PopBackWithResult
import com.polygonbikes.ebike.core.util.UiEvent.ShowSnackBar
import com.polygonbikes.ebike.core.util.compressImage
import com.polygonbikes.ebike.core.util.createMultipartRequestBody
import com.polygonbikes.ebike.core.util.sizeValidAsGPXFile
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.DetailRouteResponseMiddleware
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.v3.feature_route.data.remote.RouteServiceMiddleware
import com.polygonbikes.ebike.v3.feature_route.domain.state.EditRouteState
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
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class EditRouteViewModel  @Inject constructor(
    @ApplicationContext val context: Context,
    private val routeServiceMiddleware: RouteServiceMiddleware,
    private val locationServiceMiddleware: LocationServiceMiddleware
) : ViewModel() {
    private val TAG = "EditRouteVM"
    private var _state: MutableState<EditRouteState> = mutableStateOf(EditRouteState())
    val state: State<EditRouteState> get() = _state

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val logDatabase: LogDatabase = LogDatabase.getDatabase(context = context)
    private val logTrackDao: LogTrackDao = logDatabase.logTrackDao()

    init {
        fetchLocations()
    }

    fun onEvent(event: EditRouteEvent) {
        when (event) {
            is EditRouteEvent.GetRouteData -> {
                val route = event.route

                Log.d("EditRoute","route: $route")
                _state.value = state.value.copy(
                    routeId = route.id,
                    name = route.name ?: "",
                    startLocationData = route.startLocationData ?: LocationData(),
                    locationId = route.startLocationData?.id ?: 0,
                    roadType = route.roadType ?: emptyList(),
                    purpose = route.purpose ?: "",
                    selectedCityId = route.startLocationData?.id ?: 0,
                    selectedCityName = route.startLocationData?.city ?: "",
                    polyline = route.polyline ?: "",
                    headerImageUris = route.images
                        ?.mapNotNull { it.url }
                        ?.map { Uri.parse(it) }
                        ?: emptyList(),
                    oldRouteData = route
                )

                val images = route.images ?: emptyList()

                val imageUriPairs = images.mapNotNull { image ->
                    val uri = image.url?.let(Uri::parse)
                    val id = image.id
                    if (uri != null && id != null) uri to id else null
                }

                _state.value = state.value.copy(
                    headerImageUris = images.mapNotNull { it.url?.let(Uri::parse) },
                    oldImages = imageUriPairs,
                    oldImageIds = images.mapNotNull { it.id }
                )

                handlePolylineData(route)
                event.route.gpx?.url?.let { gpxUrl ->
                    fetchAndParseGpx(gpxUrl)
                }
            }

            is EditRouteEvent.AddImageUri -> {
                val updatedUris = state.value.headerImageUris.toMutableList()
                updatedUris.add(event.uri)
                _state.value = state.value.copy(headerImageUris = updatedUris)
            }

            EditRouteEvent.Edit -> {
                _state.value = state.value.copy(
                    isEmptyRouteName = state.value.name.isBlank(),
                    isEmptyGpxFile = state.value.gpxFile == null && state.value.polyline.isBlank(),
                    isEmptyCity = state.value.selectedCityId == null,
                    isEmptyRoadType = state.value.roadType.isEmpty(),
                    isEmptyPurpose = state.value.purpose.isBlank(),
                    isEmptyImage = state.value.headerImageFiles.isEmpty() && state.value.oldImages.isNullOrEmpty()
                )

                if (!(state.value.isEmptyRouteName || state.value.isEmptyRoadType ||
                            state.value.isEmptyPurpose || state.value.isEmptyCity)) {

                    _state.value = state.value.copy(isLoading = true)

                    val newImageParts = state.value.headerImageFiles.mapIndexed { index, file ->
                        MultipartBody.Part.createFormData(
                            name = "images[$index]",
                            filename = "image_$index.jpg",
                            RequestBody.create("image/*".toMediaTypeOrNull(), file.readBytes())
                        )
                    }

                    val gpxPart = state.value.gpxFile?.let { file ->
                        MultipartBody.Part.createFormData(
                            name = "file",
                            filename = "route.gpx",
                            RequestBody.create("application/gpx+xml".toMediaTypeOrNull(), file.readBytes())
                        )
                    }

                    val oldImageIdBodies = state.value.oldImageIds.map {
                        it.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    }

                    val call = routeServiceMiddleware.editRoute(
                        routeId = state.value.routeId,
                        name = createMultipartRequestBody(state.value.name),
                        locationId = createMultipartRequestBody(state.value.selectedCityId.toString()),
                        startLatitude = createMultipartRequestBody(state.value.startLatitude.toLong()),
                        startLongitude = createMultipartRequestBody(state.value.startLongitude.toLong()),
                        roadType = createMultipartList(state.value.roadType),
                        distance = createMultipartRequestBody(state.value.distance),
                        elevation = createMultipartRequestBody(state.value.elevation.toLong()),
                        purpose = createMultipartRequestBody(state.value.purpose),
                        polyline = createMultipartRequestBody(state.value.polyline),
                        file = gpxPart,
                        images = newImageParts,
                        oldImageIds = oldImageIdBodies
                    )

                    call.enqueue(object : Callback<DetailRouteResponseMiddleware> {
                        override fun onResponse(
                            call: Call<DetailRouteResponseMiddleware>,
                            response: Response<DetailRouteResponseMiddleware>
                        ) {
                            _state.value = state.value.copy(isLoading = false)
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Success edit route", Toast.LENGTH_SHORT).show()
                                response.body()?.data?.let { route ->
//                                    sendUiEvent(
//                                        Navigate(
//                                            route = DetailRouteManagementRoute(route = route),
//                                            navOptions = navOptions {
//                                                popUpTo<EditRouteRoute> { inclusive = true }
//                                            }
//                                        )
//                                    )
//                                    sendUiEvent(UiEvent.PopBackStack)
                                    sendUiEvent(
                                        PopBackWithResult(
                                            key = "selected_route",
                                            value = route
                                        )
                                    )

                                }
                            } else {
                                Log.e("EditRouteVM", "Edit failed: code=${response.code()}, errorBody=${response.errorBody()?.string()}")
                                Toast.makeText(context, "Failed edit route", Toast.LENGTH_SHORT).show()
                                _state.value = state.value.copy(isEditRouteEnabled = true)
                            }
                        }

                        override fun onFailure(call: Call<DetailRouteResponseMiddleware?>, t: Throwable) {
                            Log.e(TAG, "onFailure: ${t.message}")
                            _state.value = state.value.copy(
                                isLoading = false,
                                isEditRouteEnabled = true
                            )
                            Toast.makeText(context, "Failed to edit route", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    when {
                        state.value.isEmptyRouteName ->
                            Toast.makeText(context, "Please enter the route's name", Toast.LENGTH_SHORT).show()
                        state.value.isEmptyCity ->
                            Toast.makeText(context, "Please select a city", Toast.LENGTH_SHORT).show()
                        state.value.isEmptyRoadType ->
                            Toast.makeText(context, "Please select the road type", Toast.LENGTH_SHORT).show()
                        state.value.isEmptyPurpose ->
                            Toast.makeText(context, "Please select the purpose", Toast.LENGTH_SHORT).show()
                        else ->
                            Toast.makeText(context, "Please fill all required data", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            EditRouteEvent.PickImages -> {
                sendUiEvent(PickImages)
                _state.value = state.value.copy(
                    isEmptyImage = state.value.headerImageFiles.isEmpty()
                )
            }

            is EditRouteEvent.RemoveImageUri -> {
                val updatedUris = state.value.headerImageUris.toMutableList()
                updatedUris.remove(event.uri)

                val matchingOldImage = state.value.oldImages.find { it.first == event.uri }

                val updatedOldImageIds = state.value.oldImageIds.toMutableList()
                if (matchingOldImage != null) {
                    updatedOldImageIds.remove(matchingOldImage.second)
                }

                _state.value = state.value.copy(
                    headerImageUris = updatedUris,
                    oldImageIds = updatedOldImageIds
                )
            }

            is EditRouteEvent.SetImageUris -> {
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
                }
            }

            is EditRouteEvent.SetValueName -> {
                _state.value = state.value.copy(
                    name = event.value,
                    isEmptyRouteName = event.value.isBlank()
                )
            }

            is EditRouteEvent.SetValuePurpose -> {
                _state.value = state.value.copy(
                    purpose = event.value,
                    isEmptyPurpose = event.value.isBlank()
                )
            }

            is EditRouteEvent.SetValueRoadType -> {
                Log.d("RoadType", "Received road types: ${event.value}")

                _state.value = state.value.copy(
                    roadType = event.value,
                    isEmptyRoadType = event.value.isEmpty()
                )
            }

            is EditRouteEvent.OnInputLocationCoordinate -> {
                _state.value = state.value.copy(
                    locationCoordinate = event.value,
                    showDialogPickMaps = false
                )
            }

            is EditRouteEvent.OnInputLocationId -> {
                _state.value = state.value.copy(locationId = event.value)
            }

            EditRouteEvent.PickGPX -> {
                sendUiEvent(PickGPX)
            }

            is EditRouteEvent.SelectCity -> {
                val selectedCity = state.value.listCity.find { it.id == event.locationData?.id }
                _state.value = state.value.copy(
                    selectedCityId = event.locationData?.id,
                    selectedCityName = selectedCity?.city ?: "",
                )
            }

            is EditRouteEvent.SetGpxUri -> {
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

            is EditRouteEvent.SetShowModalLocation -> {
                _state.value = state.value.copy(isLocationModalShowing = event.isShowing)
            }

            is EditRouteEvent.SetEditRouteFalse -> {
                if (state.value.isEditRouteEnabled == false) {
                    return
                } else {
                    _state.value = state.value.copy(isEditRouteEnabled = false)
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

    private fun handlePolylineData(route: RouteData) {
        val polyline = route.polyline.orEmpty()

        if (polyline.length < 16) {
            Log.e("PolylineError", "Invalid polyline: $polyline")
            return
        }

        val decodedPolyline = GooglePolylineUtils.decode(polyline)
        _state.value = state.value.copy(
            routeWaypoint = decodedPolyline,
        )
        Log.d("PolylineDecode", "Decoded: $decodedPolyline")

        val bounds = decodedPolyline.getBounds()
        Log.d("PolylineBounds", "Bounds: $bounds")
        _state.value.bounds.update { bounds }
    }

    private fun List<LatLng>.getBounds(): LatLngBounds =
        LatLngBounds.Builder().apply { forEach { include(it) } }.build()

    private fun fetchAndParseGpx(gpxUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val gpxFile = downloadFileFromUrl(gpxUrl)
                val gpxOutput = GPX.read(gpxFile.toPath())

                if (gpxOutput.tracks.isEmpty()) {
                    sendUiEvent(ShowSnackBar(message = "GPX file has no tracks"))
                    return@launch
                }

                val routeWaypoints = gpxOutput.tracks.flatMap { it.segments }
                    .flatMap { it.points }
                    .map { LatLng(it.latitude.toDegrees(), it.longitude.toDegrees()) }

                _state.value = state.value.copy(routeWaypoint = routeWaypoints)
                _state.value.bounds.update { routeWaypoints.getBounds() }

            } catch (e: Exception) {
                sendUiEvent(UiEvent.ShowSnackBar(message = "Failed to load GPX"))
            }
        }
    }

    private fun downloadFileFromUrl(url: String): File {
        return File.createTempFile("temp_gpx_", ".gpx").apply {
            URL(url).openStream().use { input -> outputStream().use { input.copyTo(it) } }
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
                    Log.e("EditEventVM", "Failed to fetch locations: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LocationResponse>, t: Throwable) {
                Log.e("EditEventVM", "Error fetching locations: ${t.message}")
            }
        })
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}