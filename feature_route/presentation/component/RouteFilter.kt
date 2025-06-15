package com.polygonbikes.ebike.v3.feature_route.presentation.component

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.component.LocationFilter
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_route.domain.model.Distance
import com.polygonbikes.ebike.v3.feature_route.domain.model.Elevation
import com.polygonbikes.ebike.v3.feature_route.domain.model.Location
import com.polygonbikes.ebike.v3.feature_route.domain.model.RouteFilterData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteFilter(
    routeFilterData: RouteFilterData,
    onUpdateRouteFilter: (RouteFilterData) -> Unit,
    listCityFilter: List<LocationData>,
) {
    val tempRouteFilterData = remember { mutableStateOf(routeFilterData) }

    RouteFilterLayout(
        tempRouteFilterData = tempRouteFilterData,
        onUpdateRouteFilter = onUpdateRouteFilter,
        listCityFilter = listCityFilter,
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RouteFilterLayout(
    tempRouteFilterData: MutableState<RouteFilterData>,
    onUpdateRouteFilter: (RouteFilterData) -> Unit,
    listCityFilter: List<LocationData>,
) {
    Box {
        //  Column filter
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(colorResource(id = R.color.background_apps)),
            verticalArrangement = Arrangement.spacedBy(16.dp)

        ) {
            val options = mapOf(
                stringResource(id = R.string.road_type_paved) to "paved_road",
                stringResource(id = R.string.road_type_gravel) to "gravel_road",
                stringResource(id = R.string.road_type_offroad) to "off_road"
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row {
                    Text(
                        text = stringResource(id = R.string.route_filter_caption),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontRns,
                        color = colorResource(id = R.color.text)
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(id = R.string.reset_route_filter_label),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = fontRns,
                        color = colorResource(id = R.color.sub_text),
                        modifier = Modifier.clickable {
                            tempRouteFilterData.value = RouteFilterData.emptyState
                        }

                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(id = R.string.route_filter_road_type_label),
                    fontSize = 20.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text)
                )
                Spacer(modifier = Modifier.height(16.dp))

                MultiChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    options.forEach { (label, apiValue) ->
                        SegmentedButton(
                            checked = tempRouteFilterData.value.roads.contains(apiValue),
                            onCheckedChange = { isChecked ->
                                tempRouteFilterData.value = tempRouteFilterData.value.copy(
                                    roads = if (isChecked) {
                                        tempRouteFilterData.value.roads.toMutableSet()
                                            .apply { add(apiValue) }.toList()
                                    } else {
                                        tempRouteFilterData.value.roads.toMutableSet()
                                            .apply { remove(apiValue) }.toList()
                                    }
                                )
                                Log.d(
                                    "RouteFilter",
                                    "Updated roads: ${tempRouteFilterData.value.roads}"
                                )
                            },
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, colorResource(id = R.color.text)),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = if (tempRouteFilterData.value.roads.contains(
                                        apiValue
                                    )
                                )
                                    colorResource(id = R.color.text)
                                else
                                    Color.Transparent,

                                activeContentColor = if (tempRouteFilterData.value.roads.contains(
                                        apiValue
                                    )
                                )
                                    colorResource(id = R.color.background_apps)
                                else
                                    colorResource(id = R.color.text),

                                inactiveContainerColor = Color.Transparent,
                                inactiveContentColor = colorResource(id = R.color.text)
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(label)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.text_field_border))
            )

//            // column start from
//            Column(
//                modifier = Modifier.padding(horizontal = 16.dp)
//            ) {
//                Text(
//                    text = "Start from",
//                    fontSize = 20.sp,
//                    fontFamily = fontRns,
//                    fontWeight = FontWeight.Bold,
//                    color = colorResource(id = R.color.text)
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // row untuk memisah 2 bagian
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    // column within
//                    Column(
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Text(
//                            text = "Within",
//                            fontSize = 18.sp,
//                            fontFamily = fontRns,
//                            fontWeight = FontWeight.SemiBold,
//                            color = colorResource(id = R.color.text)
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        // start from text field
//                        var startFrom by remember { mutableStateOf("") }
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(44.dp)
//                                .border(
//                                    1.dp,
//                                    colorResource(id = R.color.text_field_border),
//                                    RoundedCornerShape(6.dp)
//                                )
//                                .padding(horizontal = 12.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            BasicTextField(
//                                value = startFrom,
//                                onValueChange = { startFrom = it },
//                                textStyle = TextStyle(
//                                    fontSize = 18.sp,
//                                    fontFamily = fontRns,
//                                    color = colorResource(id = R.color.text),
//                                    fontWeight = FontWeight.Medium
//                                ),
//                                cursorBrush = SolidColor(colorResource(id = R.color.text)),
//                                modifier = Modifier.weight(1f)
//                            )
//                            Text(
//                                text = "km",
//                                fontSize = 16.sp,
//                                fontFamily = fontRns,
//                                fontWeight = FontWeight.SemiBold,
//                                color = colorResource(id = R.color.text_field_border)
//                            )
//                        }   // close start from text field
//                    }   // close start from
//
//                    Column(modifier = Modifier.weight(1f)) {} // spy column within jadi separuh
//                }   // close row start from
//            }   // close column start
//            Spacer(modifier = Modifier.height(8.dp))
//
//            HorizontalDivider(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(colorResource(id = R.color.text_field_border))
//            )

            val numberOnly = remember {
                Regex("^[0-9]*$")
            }

            val decimalNumberOnly = remember {
                Regex("^[0-9]*\\.?[0-9]*\$")
            }

            // column distance
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.label_distance),
                    fontSize = 20.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // row untuk memisah from dan to
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // column from
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = stringResource(id = R.string.route_filter_from_label),
                            fontSize = 18.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // distance from text field
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(
                                    1.dp,
                                    colorResource(id = R.color.text_field_border),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var distanceFromText by remember {
                                mutableStateOf(
                                    tempRouteFilterData.value.distance?.from?.toString() ?: ""
                                )
                            }

                            BasicTextField(
                                value = distanceFromText,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.matches(decimalNumberOnly)) {
                                        distanceFromText = newValue.replace("\n", "")

                                        val newDistance = if (newValue.isNotEmpty()) newValue.toDoubleOrNull() else null
                                        tempRouteFilterData.value = tempRouteFilterData.value.copy(
                                            distance = tempRouteFilterData.value.distance?.copy(
                                                from = newDistance ?: 0.0
                                            ) ?: Distance(
                                                from = newDistance ?: 0.0,
                                                to = tempRouteFilterData.value.distance?.to ?: 0.0
                                            )
                                        )
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 18.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(colorResource(id = R.color.text)),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Text(
                                text = "km",
                                fontSize = 16.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(id = R.color.text_field_border)
                            )
                        }   // close distance from text field
                    }   // close distance from

                    // column distance to
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = stringResource(id = R.string.route_filter_to_label),
                            fontSize = 18.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // distance to text field
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(
                                    1.dp,
                                    colorResource(id = R.color.text_field_border),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var distanceToText by remember {
                                mutableStateOf(
                                    tempRouteFilterData.value.distance?.to?.toString() ?: ""
                                )
                            }

                            BasicTextField(
                                value = distanceToText,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.matches(decimalNumberOnly)) {
                                        distanceToText = newValue.replace("\n", "")

                                        val newDistance = if (newValue.isNotEmpty()) newValue.toDoubleOrNull() else null
                                        tempRouteFilterData.value = tempRouteFilterData.value.copy(
                                            distance = tempRouteFilterData.value.distance?.copy(
                                                to = newDistance ?: 0.0
                                            ) ?: Distance(
                                                to = newDistance ?: 0.0,
                                                from = tempRouteFilterData.value.distance?.from ?: 0.0
                                            )
                                        )
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 18.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(colorResource(id = R.color.text)),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "km",
                                fontSize = 16.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(id = R.color.text_field_border)
                            )
                        }   // close distance to text field
                    }   // close distance to
                }   // close row from & to
            }   // close column distance
            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.text_field_border))
            )

            // column elevation
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.label_elevation),
                    fontSize = 20.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // row untuk memisah from dan to
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // column from
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = stringResource(id = R.string.route_filter_from_label),
                            fontSize = 18.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // elevation from text field
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(
                                    1.dp,
                                    colorResource(id = R.color.text_field_border),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var elevationFromText by remember {
                                mutableStateOf(
                                    tempRouteFilterData.value.elevation?.from?.toString() ?: ""
                                )
                            }

                            BasicTextField(
                                value = elevationFromText,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.matches(numberOnly)) {
                                        elevationFromText = newValue.replace("\n", "")

                                        val newElevation = if (newValue.isNotEmpty()) newValue.toIntOrNull() else null
                                        tempRouteFilterData.value = tempRouteFilterData.value.copy(
                                            elevation = tempRouteFilterData.value.elevation?.copy(
                                                from = newElevation ?: 0
                                            )
                                                ?: Elevation(
                                                    from = newElevation ?: 0,
                                                    to = tempRouteFilterData.value.elevation?.to ?: 0
                                                )
                                        )

                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 18.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(colorResource(id = R.color.text)),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "m",
                                fontSize = 16.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(id = R.color.text_field_border)
                            )
                        }   // close elevation from text field
                    }   // close elevation from

                    // column elevation to
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = stringResource(id = R.string.route_filter_to_label),
                            fontSize = 18.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // elevation to text field
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(
                                    1.dp,
                                    colorResource(id = R.color.text_field_border),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var elevationToText by remember {
                                mutableStateOf(
                                    tempRouteFilterData.value.elevation?.to?.toString() ?: ""
                                )
                            }

                            BasicTextField(
                                value = elevationToText,
                                onValueChange = { newValue ->

                                    if (newValue.isEmpty() || newValue.matches(numberOnly)) {
                                        elevationToText = newValue.replace("\n", "")

                                        val newElevation =
                                            if (newValue.isNotEmpty()) newValue.toIntOrNull() else null
                                        tempRouteFilterData.value = tempRouteFilterData.value.copy(
                                            elevation = tempRouteFilterData.value.elevation?.copy(
                                                to = newElevation ?: 0
                                            )
                                                ?: Elevation(
                                                    from = tempRouteFilterData.value.elevation?.from
                                                        ?: 0, to = newElevation ?: 0
                                                )
                                        )
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 18.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(colorResource(id = R.color.text)),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "m",
                                fontSize = 16.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(id = R.color.text_field_border)
                            )
                        }   // close elevation to text field
                    }   // close elevation to

                }   // close row from & to
            }   // close column elevation
            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.text_field_border))
            )

            var showLocationBottomSheet by remember { mutableStateOf(false) }
            // column location
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.route_filter_location_label),
                    fontSize = 20.sp,
                    lineHeight = 1.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // location card
                Card(
                    onClick = { showLocationBottomSheet = true },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, colorResource(id = R.color.text_field_border)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Row(
                        modifier = Modifier
                            .background(colorResource(id = R.color.background_apps))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.route_filter_search_location_label),
                            fontSize = 18.sp,
                            lineHeight = 1.sp,
                            fontFamily = fontRns,
                            color = colorResource(id = R.color.sub_text),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "search icon",
                            Modifier
                                .padding(end = 4.dp)
                                .size(24.dp),
                            tint = colorResource(id = R.color.sub_text)
                        )
                    }
                }   // close location card
                Spacer(modifier = Modifier.height(8.dp))

                // Show selected locations as chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    tempRouteFilterData.value.cities?.forEach { location ->
                        LocationChip(
                            locationData = location,
                            onRemove = { removedLocation ->
                                tempRouteFilterData.value = tempRouteFilterData.value.copy(
                                    cities = tempRouteFilterData.value.cities?.filter { it != removedLocation }
                                )
                            }
                        )
                    }
                }

                // Bottom sheet for location filter
                if (showLocationBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showLocationBottomSheet = false },
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ) {
                        LocationFilter(
                            listCityFilter = listCityFilter,
                            selectedLocationData = tempRouteFilterData.value.cities,
                            onSaveSelectedLocations = { selectedLocations ->
                                tempRouteFilterData.value =
                                    tempRouteFilterData.value.copy(cities = selectedLocations)
                                showLocationBottomSheet = false
                            }
                        )
                    }
                }

            }   // close column location
            Spacer(modifier = Modifier.height(100.dp))

        }   // close column filter

        // box floating button
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(color = colorResource(id = R.color.background_apps))
            ) {
                FloatingActionButton(
                    onClick = {
                        Log.d("RouteFilter", "Temp Route Filter Data: $tempRouteFilterData")
                        onUpdateRouteFilter(tempRouteFilterData.value)
                    },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(vertical = 20.dp, horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(40.dp),
                    containerColor = colorResource(id = R.color.red_accent)
                ) {
                    Text(
                        text = stringResource(id = R.string.route_filter_label_save_filter),
                        color = colorResource(id = R.color.text),
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
        }   // close box floating button
    }   // close main box
}

@Composable
@Preview
fun RouteFilterPreview() {
    EbikeTheme {
        RouteFilterLayout(
            tempRouteFilterData =
                remember {
                    mutableStateOf(
                        RouteFilterData(
                            location = Location(
                                latitude = -7.250445,
                                longitude = 112.768845,
                                radius = 10.0
                            ),
                            purpose = "Exercise",
                            roads = listOf("Gravel", "Paved"),
                            distance = Distance(
                                from = 5.0,
                                to = 25.0
                            ),
                            elevation = Elevation(
                                from = 100,
                                to = 800
                            ),
                            cities = listOf(
                                LocationData(
                                    id = 1,
                                    country = "Indonesia",
                                    region = "Jawa Timur",
                                    city = "Surabaya"
                                ),
                                LocationData(
                                    id = 2,
                                    country = "Indonesia",
                                    region = "Jawa Timur",
                                    city = "Malang"
                                )
                            )
                        )
                    )
                },
            onUpdateRouteFilter = {},
            listCityFilter = listOf(
                LocationData(
                    id = 1,
                    country = "Indonesia",
                    region = "Jawa Timur",
                    city = "Surabaya"
                )
            )
        )
    }
}