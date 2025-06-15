package com.polygonbikes.ebike.v3.feature_route.presentation.route_management.active_route

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.v3.feature_route.domain.model.RouteFilterData
import com.polygonbikes.ebike.v3.feature_route.domain.state.RouteManagementState
import com.polygonbikes.ebike.v3.feature_route.presentation.route_management.RouteManagementCard

@Composable
fun ActiveRouteScreen(
    state: RouteManagementState,
    onOpenDetailRoute: (RouteData) -> Unit,
    onDeleteRoute: (Int) -> Unit,
    onShowRouteFilter: () -> Unit,
    onUpdateRouteFilter: (RouteFilterData) -> Unit
) {
    val lazyListState = rememberLazyListState()

    ActiveRouteLayout(
        state = state,
        lazyListState = lazyListState,
        onOpenDetailRoute = { route ->
            onOpenDetailRoute(route)
        },

        onDeleteRoute = { routeId ->
            onDeleteRoute(routeId)
        },
        onShowRouteFilter = onShowRouteFilter,
        onUpdateRouteFilter = onUpdateRouteFilter
    )
}

@Composable
fun ActiveRouteLayout(
    state: RouteManagementState,
    lazyListState: LazyListState,
    onOpenDetailRoute: (RouteData) -> Unit,
    onDeleteRoute: (Int) -> Unit,
    onShowRouteFilter: () -> Unit,
    onUpdateRouteFilter: (RouteFilterData) -> Unit
) {
    Column(
        modifier = Modifier
            .background(colorResource(id = R.color.background_apps))
            .fillMaxSize()
    ) {
        // row filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.background_apps))
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // List of available purposes
            val purposeOptions = listOf(
                stringResource(id = R.string.purpose_exercise) to Icons.Default.FitnessCenter,
                stringResource(id = R.string.purpose_recreation) to R.drawable.recreation,
                stringResource(id = R.string.purpose_touring) to R.drawable.touring
            )

            // Track selected purposes
            val selectedPurposes = remember {
                mutableStateOf(
                    state.routeFilterData.purpose?.split(",")
                        ?.map { it.lowercase() }?.toMutableSet()
                        ?: mutableSetOf()
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                purposeOptions.forEach { (label, icon) ->
                    val lowercaseLabel = label.lowercase()
                    val isSelected =
                        lowercaseLabel in selectedPurposes.value

                    FilterChip(
                        onClick = {
                            selectedPurposes.value =
                                selectedPurposes.value.apply {
                                    if (isSelected) remove(lowercaseLabel) else add(
                                        lowercaseLabel
                                    )
                                }
                                onUpdateRouteFilter(
                                    state.routeFilterData.copy(
                                        purpose = selectedPurposes.value.joinToString(
                                            ","
                                        )
                                    )
                                )
                        },
                        selected = isSelected,
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colorResource(id = R.color.background_apps),
                            labelColor = colorResource(id = R.color.text),
                            selectedLabelColor = colorResource(id = R.color.background_apps),
                            selectedContainerColor = colorResource(id = R.color.white)
                        ),
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                when (icon) {
                                    is Int -> Icon(
                                        painter = painterResource(id = icon),
                                        contentDescription = "$label icon"
                                    )

                                    is ImageVector -> Icon(
                                        imageVector = icon,
                                        contentDescription = "$label icon",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    fontSize = 14.sp,
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) colorResource(id = R.color.background_apps) else colorResource(
                                        id = R.color.text
                                    )
                                )
                            }
                        }
                    )
                }
            }

            //  filter icon card
            Card(
                onClick = { onShowRouteFilter() },
                modifier = Modifier
                    .height(32.dp)
                    .wrapContentWidth(),
                colors = CardDefaults.cardColors(colorResource(id = R.color.background_apps)),
                border = BorderStroke(
                    1.dp, color = colorResource(id = R.color.sub_text)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = "Filter icon",
                        modifier = Modifier.size(14.dp)
                    )
                    //  jika user menggunakan filter, maka muncul jumlah filter yg digunakan
                    var filterCount by remember { mutableIntStateOf(0) }
                    if (filterCount > 0) {
                        Text(
                            text = "3", //filterCount
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }   // close filter icon card
        }   // close row filter

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.listActiveRoute) { route ->
                RouteManagementCard(
                    detail = route,
                    onOpenDetailRoute = onOpenDetailRoute,
                    isDeleted = false,
                    isRouteLoading = state.isActiveRouteLoading,
                    onActionRoute = onDeleteRoute,
                )
            }
            item{ Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ActiveRoutePreview() {
    EbikeTheme {
        ActiveRouteLayout(
            state = RouteManagementState(
                listActiveRoute = remember { mutableStateListOf() },
                listDeletedRoute = remember { mutableStateListOf() }
            ),
            lazyListState = rememberLazyListState(),
            onOpenDetailRoute = {},
            onDeleteRoute = {},
            onShowRouteFilter = {},
            onUpdateRouteFilter = {}
        )
    }
}