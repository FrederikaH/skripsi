package com.polygonbikes.ebike.v3.feature_route.presentation.route_management.edit_route

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
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
import androidx.navigation.NavController
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.component.CityInputField
import com.polygonbikes.ebike.core.component.GPXPreview
import com.polygonbikes.ebike.core.component.InputImage
import com.polygonbikes.ebike.core.component.LoadingIndicator
import com.polygonbikes.ebike.core.component.SelectLocationModal
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_event.domain.state.CityFilterItem
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.v3.feature_route.domain.state.EditRouteState
import kotlin.collections.get

@Composable
fun EditRouteScreen(
    navController: NavController,
    onPopBackStack: () -> Unit,
    onNavigate: (UiEvent.Navigate) -> Unit,
    route: RouteData,
    viewModel: EditRouteViewModel = hiltViewModel()
) {
    val state by viewModel.state

    val multipleImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            viewModel.onEvent(EditRouteEvent.SetImageUris(uris))
        }

    val pickGpxLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri ->
            viewModel.onEvent(EditRouteEvent.SetGpxUri(uri))
        }
    }

    LaunchedEffect(key1 = viewModel) {
        viewModel.onEvent(EditRouteEvent.GetRouteData(route))

        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onNavigate(event)

                is UiEvent.PopBackWithResult -> {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(event.key, event.value)
                    navController.popBackStack()
                }

                is UiEvent.PickImages -> {
                    multipleImageLauncher.launch("image/*")
                }

                is UiEvent.PickGPX -> {
                    pickGpxLauncher.launch("application/gpx+xml")
                }

                else -> Unit
            }
        }
    }

    EditRouteLayout(
        onPopBackStack = onPopBackStack,
        state = state,
        onInputName = {
            viewModel.onEvent(EditRouteEvent.SetValueName(it))
        },

        onInputRoadType = {
            viewModel.onEvent(EditRouteEvent.SetValueRoadType(it))
        },

        onInputPurpose = {
            viewModel.onEvent(EditRouteEvent.SetValuePurpose(it))
        },

        onEditRoute = {
            viewModel.onEvent(EditRouteEvent.Edit)
        },

        onPickImages = {
            viewModel.onEvent(EditRouteEvent.PickImages)
        },

        onAddImage = { uri ->
            viewModel.onEvent(EditRouteEvent.AddImageUri(uri))
        },

        onRemoveImage = { uri ->
            viewModel.onEvent(EditRouteEvent.RemoveImageUri(uri))
        },

        onPickGPX = {
            viewModel.onEvent(EditRouteEvent.PickGPX)
        },

        onSetShowModalLocation = {
            viewModel.onEvent(EditRouteEvent.SetShowModalLocation(it))
        },

        onSetEditRouteFalse = {
            viewModel.onEvent(EditRouteEvent.SetEditRouteFalse)
        },

        onInputLocationId = {
            viewModel.onEvent(EditRouteEvent.OnInputLocationId(it))
        },

        onSelectCity = { location ->
            viewModel.onEvent(EditRouteEvent.SelectCity(location))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EditRouteLayout(
    onPopBackStack: () -> Unit,
    state: EditRouteState,
    onInputName: (String) -> Unit,
    onPickImages: () -> Unit,
    onAddImage: (Uri) -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onInputRoadType: (List<String>) -> Unit,
    onInputPurpose: (String) -> Unit,
    onEditRoute: () -> Unit,
    onPickGPX: () -> Unit,
    onSetShowModalLocation: (Boolean) -> Unit,
    onSetEditRouteFalse: () -> Unit,
    onInputLocationId: (Int) -> Unit,
    onSelectCity: (LocationData) -> Unit
){
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
                            text = "Edit route",
                            fontFamily = fontRns,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colorResource(id = R.color.white)
                        )
                    }
                },
                modifier = Modifier.height(60.dp),
                navigationIcon = {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentSize(align = Alignment.Center)
                    ) {
                        IconButton(onClick = onPopBackStack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back Icon",
                                tint = colorResource(id = R.color.white)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = colorResource(id = R.color.black)
                )
            )
        },
    ) {
        if (state.isLoading)
            LoadingIndicator(onDismiss = {})

        if (state.isLocationModalShowing) {
            ModalBottomSheet(
                onDismissRequest = { onSetShowModalLocation(false) },
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                SelectLocationModal(
                    listCityFilter = state.listCity.map {
                        CityFilterItem(it.id.toString(), it.city.orEmpty())
                    },
                    onSelectCity = { selectedId ->
                        val selected = state.listCity.find { it.id.toString() == selectedId }
                        selected?.let {
                            onSelectCity(it)
                            it.id?.let(onInputLocationId)
                        }
                        onSetShowModalLocation(false)
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.background_apps))
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(
                    top = it.calculateTopPadding() + 16.dp,
                    start = it.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                    end = it.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    bottom = it.calculateBottomPadding() + 8.dp
                ),
        ) {
            if (state.isLoading == true)
                LoadingIndicator(onDismiss = {})

            Text(
                text = "Route name *",
                fontSize = 18.sp,
                fontFamily = fontRns,
                color = colorResource(id = R.color.text),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            var textFieldBorder =
                if (state.isEmptyRouteName) Color.Red else colorResource(id = R.color.text_field_border)
            BasicTextField(
                value = state.name,
                onValueChange = { newValue ->
                    onInputName(newValue)
                    state.isEmptyRouteName = newValue.isBlank()
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
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(id = R.string.event_route_label),
                fontSize = 18.sp,
                fontFamily = fontRns,
                color = colorResource(id = R.color.text),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column {
                GPXPreview(state.bounds, state.routeWaypoint, onPickGPX)

                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "City *",
                fontSize = 18.sp,
                fontFamily = fontRns,
                color = colorResource(id = R.color.text),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            CityInputField(
                cityName = state.selectedCityName,
                onSetShowModal = onSetShowModalLocation
            )
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Route image preview (Max 3)",
                fontSize = 18.sp,
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
            val selectedRoadType = remember { mutableStateListOf<String>() }

            LaunchedEffect(state.oldRouteData?.roadType) {
                selectedRoadType.clear()
                state.oldRouteData?.roadType?.let { selectedRoadType.addAll(it) }
            }

            MultiChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { (label, apiValue) ->
                    SegmentedButton(
                        checked = selectedRoadType.contains(apiValue),
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                if (selectedRoadType.contains(apiValue) == false) {
                                    selectedRoadType.add(apiValue)
                                }
                            } else {
                                selectedRoadType.remove(apiValue)
                            }

                            onInputRoadType(selectedRoadType)
                        },

                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, colorResource(id = R.color.text)),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = if (selectedRoadType.contains(apiValue)) colorResource(
                                id = R.color.text
                            ) else Color.Transparent,
                            activeContentColor = if (selectedRoadType.contains(apiValue)) colorResource(
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

            LaunchedEffect(state.oldRouteData?.purpose) {
                val oldPurpose = state.oldRouteData?.purpose
                val purposeMapping = mapOf(
                    "exercise" to 0,
                    "recreation" to 1,
                    "touring" to 2
                )

                val purposeIndex = purposeMapping[oldPurpose] ?: -1

                Log.d("TAG", "purpose adalah $purposeIndex")
                selectedPurpose = purposeIndex
            }

            val purposes = listOf("Exercise", "Recreation", "Touring")

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                purposes.forEachIndexed { index, purpose ->
                    SegmentedButton(
                        onClick = {
                            selectedPurpose = index
                            onInputPurpose(purpose.lowercase())
                        },
                        selected = index == selectedPurpose,
                        shape = when (index) {
                            0 -> RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                            purposes.size - 1 -> RoundedCornerShape(
                                topEnd = 4.dp,
                                bottomEnd = 4.dp
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
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    onSetEditRouteFalse
                    onEditRoute()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = colorResource(id = R.color.red_accent)
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.label_button_save_changes),
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.text)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

        }   // close main column
    }
}

@Composable
@Preview
fun EditRouteCustomPreview() {
    EbikeTheme {
        EditRouteLayout(
            onPopBackStack = {},
            state = EditRouteState(),
            onInputName = {},
            onPickImages = {},
            onPickGPX = {},
            onSetShowModalLocation = {},
            onAddImage = {},
            onRemoveImage = {},
            onInputRoadType = {},
            onInputPurpose = {},
            onEditRoute = {},
            onSetEditRouteFalse = {},
            onInputLocationId = {},
            onSelectCity = {}
        )
    }
}