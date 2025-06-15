package com.polygonbikes.ebike.v3.feature_trip.presentation.save_trip

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.FeatureList
import com.polygonbikes.ebike.core.component.ConfirmDialog
import com.polygonbikes.ebike.core.component.InputImage
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.core.component.LoadingIndicator
import com.polygonbikes.ebike.core.component.MapPreview
import com.polygonbikes.ebike.core.util.AlertDialogData
import com.polygonbikes.ebike.core.util.TimeUtil
import com.polygonbikes.ebike.feature_trip.presentation.TripViewModel
import com.polygonbikes.ebike.feature_trip.presentation.TripViewModel.Companion.KEY_CONFIRMATION_DISCARD
import com.polygonbikes.ebike.v3.feature_trip.domain.state.BikeType
import com.polygonbikes.ebike.v3.feature_trip.domain.state.SaveTripState
import java.util.Locale

@Composable
fun SaveTripScreen(
    onNavigate: (UiEvent.Navigate) -> Unit,
    tripId: String,
    movingTime: Long,
    elapsedTime: Long,
    bikeName: String?,
    viewModel: SaveTripViewModel = hiltViewModel()
) {
    val state by viewModel.state
    var selectImage by remember { mutableStateOf<Uri?>(value = null) }

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            selectImage = it
            it?.let { uri ->
                viewModel.onEvent(SaveTripEvent.SetImageUri(uri))
            }
        }

    val multipleImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            viewModel.onEvent(SaveTripEvent.SetImageUris(uris))
        }

    var showDialog by remember { mutableStateOf(value = false) }
    var dialogData by remember { mutableStateOf(value = AlertDialogData()) }

    LaunchedEffect(key1 = viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onNavigate(event)

                is UiEvent.PickImage -> {
                    galleryLauncher.launch("image/*")
                }

                is UiEvent.PickImages -> {
                    multipleImageLauncher.launch("image/*")
                }

                is UiEvent.AlertDialog -> {
                    showDialog = true
                    dialogData = event.data
                }

                else -> Unit
            }
        }
    }

    LaunchedEffect(key1 = viewModel) {
        viewModel.onEvent(
            SaveTripEvent.Init(
                tripId = tripId,
                movingTime = movingTime,
                elapsedTime = elapsedTime,
                bikeName = bikeName
            )
        )
    }

    SaveTripLayout(
        state = state,
        dialogData = dialogData,
        showDialog = showDialog,
        onInputName = {
            viewModel.onEvent(SaveTripEvent.SetValueName(it))
        },

        onInputBikeName = {
            viewModel.onEvent(SaveTripEvent.SetValueBikeName(it))
        },

        onInputRoadType = {
            viewModel.onEvent(SaveTripEvent.SetValueRoadType(it))
        },

        onInputPurpose = {
            viewModel.onEvent(SaveTripEvent.SetValuePurpose(it))
        },

        onSetIsPrivate = {
            viewModel.onEvent(SaveTripEvent.SetIsPrivate(it))
        },

        onCreateSaveTrip = {
            viewModel.onEvent(SaveTripEvent.Create)
        },

        onOpenHistoryScreen = {
            viewModel.onEvent(SaveTripEvent.OpenHistoryScreen)
        },

        onPickImage = {
            viewModel.onEvent(SaveTripEvent.PickImage)
        },

        onPickImages = {
            viewModel.onEvent(SaveTripEvent.PickImages)
        },

        onAddImage = { uri ->
            viewModel.onEvent(SaveTripEvent.AddImageUri(uri))
        },

        onRemoveImage = { uri ->
            viewModel.onEvent(SaveTripEvent.RemoveImageUri(uri))
        },

        onConfirmationDiscardTrip = {
            viewModel.onEvent(SaveTripEvent.ConfirmationDiscardTrip)
        },

        onDiscardTrip = {
            viewModel.onEvent(SaveTripEvent.DiscardTrip)
        },
        onDismissDialog = {
            showDialog = false
        },
        onSetBikeType = {
            viewModel.onEvent(SaveTripEvent.SetBikeType(it))
        }
//        onInputPrivacy = {
//            viewModel.onEvent(SaveTripEvent.SetPrivacy(it))
//        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SaveTripLayout(
    state: SaveTripState,
    onInputName: (String) -> Unit,
    onPickImage: () -> Unit,
    onPickImages: () -> Unit,
    onAddImage: (Uri) -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onInputBikeName: (String) -> Unit,
    onInputRoadType: (List<String>) -> Unit,
    onInputPurpose: (String) -> Unit,
    onSetIsPrivate: (Int) -> Unit,
    onCreateSaveTrip: () -> Unit,
    onOpenHistoryScreen: () -> Unit,
    showDialog: Boolean,
    dialogData: AlertDialogData,
    onConfirmationDiscardTrip: () -> Unit,
    onDiscardTrip: () -> Unit,
    onDismissDialog: () -> Unit,
    onSetBikeType: (BikeType) -> Unit
//    onInputPrivacy: (String) -> Unit    // String / boolean?
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentSize(align = Alignment.Center)
                    ) {
                        Text(
                            text = "Save Trip",
                            fontFamily = fontRns,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colorResource(id = R.color.white)
                        )
                    }
                },
                modifier = Modifier.height(60.dp),
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = colorResource(id = R.color.black)
                )
            )
        },
    ) {
        if (state.isLoading) {
            LoadingIndicator(onDismiss = {})
        }

        if (showDialog)
            ConfirmDialog(
                onDismiss = onDismissDialog,
                onConfirm = {
                    when (dialogData.id) {
                        TripViewModel.KEY_CONFIRMATION_DISCARD -> onDiscardTrip()
                    }
                },
                data = dialogData
            )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .padding(top = it.calculateTopPadding())
            ) {
                // zoomable map holder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color.DarkGray)
                ) {
                    // Map preview
                    MapPreview(
                        bounds = state.bounds,
                        routeWaypoint = state.routeWaypoint
                    )
                }
            }

            Column(
                modifier = Modifier
                    .background(colorResource(id = R.color.background_apps))
                    .fillMaxWidth()
                    .padding(
                        top = 24.dp,
                        start = it.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                        end = it.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                        bottom = it.calculateBottomPadding() + 8.dp
                    ),
            ) {

                Row {
                    // Column distance
                    Column(modifier = Modifier.weight(0.5f)) {
                        Text(
                            text = "Distance",
                            fontSize = 16.sp,
                            fontFamily = fontRns,
                            color = colorResource(id = R.color.text),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format(
                                    Locale.getDefault(),
                                    "%.2f km",
                                    state.distance
                                ),
                                fontSize = 32.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.text),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Column elevation
                    Column(modifier = Modifier.weight(0.5f)) {
                        Text(
                            text = "Elevation",
                            fontSize = 16.sp,
                            fontFamily = fontRns,
                            color = colorResource(id = R.color.text),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format(Locale.getDefault(), "%d m", state.elevation),
                                fontSize = 32.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.text),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }   // close column elevation

                }   // close row distance & elevation
                Spacer(modifier = Modifier.height(24.dp))

                Row {
                    // Column average speed
                    Column(modifier = Modifier.weight(0.5f)) {
                        Text(
                            text = "Avg speed",
                            fontSize = 16.sp,
                            fontFamily = fontRns,
                            color = colorResource(id = R.color.sub_text),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f", state.avgSpeed),
                                fontSize = 26.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.text),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " km/h",
                                fontSize = 24.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.text),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Column max speed
                    Column(modifier = Modifier.weight(0.5f)) {
                        Text(
                            text = "Max speed",
                            fontSize = 16.sp,
                            fontFamily = fontRns,
                            color = colorResource(id = R.color.sub_text),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f", state.maxSpeed),
                                fontSize = 26.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.text),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " km/h",
                                fontSize = 24.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.text),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                }   // close row avg & max speed
                Spacer(modifier = Modifier.height(24.dp))


                Row {
                    // Column moving time
                    Column(modifier = Modifier.weight(0.5f)) {
                        Text(
                            text = "Moving time",
                            fontSize = 16.sp,
                            fontFamily = fontRns,
                            color = colorResource(id = R.color.sub_text),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = TimeUtil.formatSimpleTime(state.movingTime.toInt()),
                                fontSize = 26.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.text),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Column elapsed time
                    Column(modifier = Modifier.weight(0.5f)) {
                        Text(
                            text = "Elapsed time",
                            fontSize = 16.sp,
                            fontFamily = fontRns,
                            color = colorResource(id = R.color.sub_text),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = TimeUtil.formatSimpleTime(state.elapsedTime.toInt()),
                                fontSize = 26.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.text),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }   // close column elapsed time

                }   // close row moving time & elapsed time
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Trip name *",
                    fontSize = 20.sp,
                    fontFamily = fontRns,
                    color = colorResource(id = R.color.text),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                var textFieldBorder =
                    if (state.isEmptyTripName) Color.Red else colorResource(id = R.color.text_field_border)
                BasicTextField(
                    value = state.name,
                    onValueChange = { newValue ->
                        onInputName(newValue)
                        state.isEmptyTripName = newValue.isBlank()
                    },
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = fontRns,
                        color = colorResource(id = R.color.text),
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(colorResource(id = R.color.text)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .drawBehind {
                            val borderSize = 1.dp.toPx()
                            drawLine(
                                color = textFieldBorder,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = borderSize
                            )
                        }
                        .padding(8.dp)
                )
                if (state.isEmptyTripName) {
                    Text(
                        text = "This field can't be empty",
                        color = Color.Red,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Add image *",
                    fontSize = 20.sp,
                    fontFamily = fontRns,
                    color = colorResource(id = R.color.text),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                InputImage(
                    headerImageUris = state.headerImageUris,
                    onPickImages = { onPickImages() },
                    onAddImage = { uri -> onAddImage(uri) },
                    onRemoveImage = { uri -> onRemoveImage(uri) }
                )
//                InputImage()
//                val stroke = Stroke(
//                    width = 2f,
//                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
//                )
//                var color =
//                    if (state.isEmptyImage) Color.Red else colorResource(id = R.color.text_field_border)
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(180.dp)
//                        .clickable(onClick = onPickImage)
//                        .then(
//                            if (state.headerImageBitmap == null)
//                                Modifier.drawBehind {
//                                    drawRoundRect(
//                                        color = color,
//                                        style = stroke,
//                                        cornerRadius = CornerRadius(8.dp.toPx())
//                                    )
//                                }
//                            else
//                                Modifier
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    if (state.headerImageBitmap != null) {
//                        Image(
//                            bitmap = state.headerImageBitmap.asImageBitmap(),
//                            contentDescription = "image header",
//                        )
//                    } else if (state.compressingImage) {
//                        CircularProgressIndicator()
//                    } else {
//                        Image(
//                            painter = painterResource(id = R.drawable.baseline_add_circle_outline_24),
//                            contentDescription = "icon add",
//                            modifier = Modifier.size(28.dp),
//                            colorFilter = ColorFilter.tint(color)
//                        )
//                    }
//                }
//                if (state.isEmptyImage) {
//                    Text(
//                        text = "This field can't be empty",
//                        color = Color.Red,
//                        fontFamily = fontRns,
//                        fontWeight = FontWeight.SemiBold,
//                        fontSize = 14.sp,
//                        modifier = Modifier.padding(top = 4.dp)
//                    )
//                }
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(id = R.string.upload_file_note),
                    fontSize = 16.sp,
                    fontFamily = fontRns,
                    color = colorResource(id = R.color.sub_text),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Bike *",
                    fontSize = 20.sp,
                    fontFamily = fontRns,
                    color = colorResource(id = R.color.text),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Bike type
                val bikeTypes = listOf("Non e-bike", "E-bike")
                val bikeTypesEnum = listOf(BikeType.NONEBIKE, BikeType.EBIKE)

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    bikeTypes.forEachIndexed { index, purpose ->
                        SegmentedButton(
                            onClick = { onSetBikeType(bikeTypesEnum[index]) },
                            selected = bikeTypesEnum[index] == state.bikeType,
                            shape = when (index) {
                                0 -> RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
                                bikeTypes.size - 1 -> RoundedCornerShape(
                                    topEnd = 6.dp,
                                    bottomEnd = 6.dp
                                )

                                else -> RectangleShape
                            },
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = if (bikeTypesEnum[index] == state.bikeType) colorResource(
                                    id = R.color.text
                                ) else Color.Transparent,
                                activeContentColor = if (bikeTypesEnum[index] == state.bikeType) colorResource(id = R.color.background_apps) else colorResource(
                                    id = R.color.text
                                ),
                                inactiveContainerColor = if (bikeTypesEnum[index] != state.bikeType) Color.Transparent else colorResource(
                                    id = R.color.text
                                ),
                                inactiveContentColor = if (bikeTypesEnum[index] != state.bikeType) colorResource(
                                    id = R.color.text
                                ) else colorResource(id = R.color.background_apps)
                            ),
                            border = BorderStroke(1.dp, Color.White),
                            icon = {}
                        ) {
                            Text(
                                text = purpose,
                                fontSize = 16.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                var bikeNameBorder =
                    if (state.isEmptyBikeName) Color.Red else colorResource(id = R.color.text_field_border)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .border(
                            width = 1.dp,
                            color = bikeNameBorder,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Non e-bike
                    if (state.bikeType == BikeType.NONEBIKE) {
                        var expanded by remember { mutableStateOf(false) }

                        BasicTextField(
                            value = state.bikeName,
                            onValueChange = { newText ->
                                onInputBikeName(newText)
                                expanded = newText.isNotEmpty()
                                state.isEmptyBikeName = newText.isBlank()
                            },
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.text),
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(colorResource(id = R.color.text)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (!focusState.isFocused) expanded = false
                                }
                        )

                        if (state.bikeName.isEmpty()) {
                            Text(
                                text = "Enter your bike name...",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    color = bikeNameBorder,
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

//                        // Dropdown menu for suggestions
//                        if (expanded) {
//                            DropdownMenu(
//                                expanded = expanded,
//                                onDismissRequest = { expanded = false },
//                                modifier = Modifier.fillMaxWidth(),
//                                properties = PopupProperties(focusable = false)
//                            ) {
//                                bikeList.filter { it.contains(state.bikeName, ignoreCase = true) }
//                                    .forEach { bike ->
//                                        DropdownMenuItem(onClick = {
//                                            onInputBikeName(bike); expanded = false
//                                        }, text = { Text(bike) })
//                                    }
//                            }
//                        }

                    } else {
                        Row {
                            Icon(
                                Icons.Default.ElectricBike,
                                contentDescription = "E-bike icon",
                                tint = colorResource(id = R.color.sub_text)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.eBikeName ?: "-",
                                fontSize = 16.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.sub_text),
                                fontWeight = FontWeight.SemiBold
                            )

                        }
                    }
                }
                if (state.isEmptyBikeName)
                    Text(
                        text = "This field can't be empty",
                        color = Color.Red,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Road type *",
                    fontSize = 20.sp,
                    fontFamily = fontRns,
                    color = colorResource(id = R.color.text),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Road type
                val options = mapOf(
                    "Paved" to "paved_road",
                    "Gravel" to "gravel_road",
                    "Off-road" to "off_road"
                )
                val selectedOptions = remember { mutableStateListOf<String>() }

                MultiChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    options.forEach { (label, apiValue) ->
                        SegmentedButton(
                            checked = selectedOptions.contains(apiValue),
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    if (!selectedOptions.contains(apiValue)) {
                                        selectedOptions.add(apiValue)
                                    }
                                } else {
                                    selectedOptions.remove(apiValue)
                                }
                                onInputRoadType(selectedOptions)
                                state.isEmptyRoadType = selectedOptions.isEmpty()
                            },
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, colorResource(id = R.color.text)),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = if (selectedOptions.contains(apiValue)) colorResource(
                                    id = R.color.text
                                ) else Color.Transparent,
                                activeContentColor = if (selectedOptions.contains(apiValue)) colorResource(
                                    id = R.color.background_apps
                                ) else colorResource(id = R.color.text),
                                inactiveContainerColor = Color.Transparent,
                                inactiveContentColor = colorResource(id = R.color.text)
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(label)
                        }
                    }
                }

                // Warning for empty option
                if (state.isEmptyRoadType) {
                    Text(
                        text = "Please choose at least one road type",
                        color = Color.Red,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Trip's purpose *",
                    fontSize = 20.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Trip's Purpose
                var selectedPurpose by remember { mutableIntStateOf(-1) }
                val purposes = listOf("Exercise", "Recreation", "Touring")

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    purposes.forEachIndexed { index, purpose ->
                        SegmentedButton(
                            onClick = {
                                selectedPurpose = index
                                onInputPurpose(purpose)
                                state.isEmptyPurpose = selectedPurpose == -1
                            },
                            selected = index == selectedPurpose,
                            shape = when (index) {
                                0 -> RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
                                purposes.size - 1 -> RoundedCornerShape(
                                    topEnd = 6.dp,
                                    bottomEnd = 6.dp
                                )

                                else -> RectangleShape
                            },
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = if (index == selectedPurpose) colorResource(
                                    id = R.color.text
                                ) else Color.Transparent,
                                activeContentColor = if (index == selectedPurpose) colorResource(id = R.color.background_apps) else colorResource(
                                    id = R.color.text
                                ),
                                inactiveContainerColor = if (index != selectedPurpose) Color.Transparent else colorResource(
                                    id = R.color.text
                                ),
                                inactiveContentColor = if (index != selectedPurpose) colorResource(
                                    id = R.color.text
                                ) else colorResource(id = R.color.background_apps)
                            ),
                            border = BorderStroke(1.dp, Color.White)
                        ) {
                            Text(
                                text = purpose,
                                fontSize = 16.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                // Warning for empty option
                if (state.isEmptyPurpose) {
                    Text(
                        text = "Please choose a purpose",
                        color = Color.Red,
                        fontFamily = fontRns,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))

                if (FeatureList.FeatureFriendship) {
                    // trip privacy
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            Text(
                                text = "Keep trip private",
                                fontSize = 20.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(id = R.color.text)
                            )
                            Spacer(modifier = Modifier.width(4.dp))

                            val tooltipPosition =
                                TooltipDefaults.rememberPlainTooltipPositionProvider()
                            val tooltipState = rememberBasicTooltipState(isPersistent = false)

                            BasicTooltipBox(
                                positionProvider = tooltipPosition,
                                tooltip = {
                                    Text(
                                        text = "Your activity will be shared to public if you turn the privacy off",
                                        modifier = Modifier
                                            .background(colorResource(id = R.color.secondary))
                                            .padding(
                                                start = 16.dp,
                                                end = 16.dp,
                                                top = 12.dp,
                                                bottom = 18.dp
                                            )
                                            .clip(RoundedCornerShape(8.dp))
                                            .width(200.dp)
                                    )
                                },
                                state = tooltipState
                            ) {

                                Icon(
                                    Icons.Outlined.Info,
                                    contentDescription = "info icon",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))

                        var checked by remember { mutableStateOf(state.isPrivate == 1) }

                        Box(
                            modifier = Modifier
                                .size(41.6.dp, 25.6.dp)
                        ) {
                            Switch(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    checked = isChecked
                                    onSetIsPrivate(if (isChecked) 1 else 0)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colorResource(id = R.color.text),
                                    checkedTrackColor = colorResource(id = R.color.switch_track),
                                    uncheckedThumbColor = colorResource(id = R.color.text),
                                    uncheckedBorderColor = colorResource(id = R.color.sub_text),
                                    uncheckedTrackColor = colorResource(id = R.color.background_apps),
                                ),
                                modifier = Modifier.scale(0.8f)
                            )
                        }   // close switch
                    }   // close trip privacy
                }
                Spacer(modifier = Modifier.height(100.dp))

            }   // main column
        }   // close scrollable column

        // box floating button
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(color = colorResource(id = R.color.background_apps))
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { onConfirmationDiscardTrip() },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = colorResource(id = R.color.text),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .height(40.dp)
                        .weight(1f),
                    containerColor = colorResource(id = R.color.background_apps)
                ) {
                    Text(
                        text = "Discard",
                        color = colorResource(id = R.color.text),
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(2.dp)
                    )
                }   // close floating button

                FloatingActionButton(
                    onClick = {
                        onCreateSaveTrip()
                    },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .height(40.dp)
                        .weight(1f),
                    containerColor = colorResource(id = R.color.red_accent)
                ) {
                    Text(
                        text = "Save activity",
                        color = colorResource(id = R.color.text),
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(2.dp)
                    )
                }   // close floating button

            }
        }   // close box floating button

    }   // close scaffold

}

@Composable
@Preview
fun SaveTripPreview() {
    EbikeTheme {
        SaveTripLayout(
            state = SaveTripState(),
            onInputName = {},
            onInputBikeName = {},
            onInputRoadType = {},
            onInputPurpose = {},
            onSetIsPrivate = {},
            onCreateSaveTrip = {},
            onOpenHistoryScreen = {},
            onPickImage = {},
            onPickImages = {},
            onAddImage = {},
            onRemoveImage = {},
            showDialog = false,
            dialogData = AlertDialogData(
                id = KEY_CONFIRMATION_DISCARD,
                title = "Discard Trip",
                description = "Are you sure you want to discard this trip?",
                dismissCaption = "Cancel",
                confirmCaption = "Discard",
                dismissButtonColor = R.color.sub_text,
                confirmButtonColor = R.color.red
            ),
            onConfirmationDiscardTrip = {},
            onDiscardTrip = {},
            onDismissDialog = {},
            onSetBikeType = {}
        )
    }
}