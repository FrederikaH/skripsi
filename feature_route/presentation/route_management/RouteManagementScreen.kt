package com.polygonbikes.ebike.v3.feature_route.presentation.route_management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.component.LoadingIndicator
import com.polygonbikes.ebike.core.entities.FileEntity
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_event.presentation.EventEvent
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.v3.feature_route.domain.model.RouteFilterData
import com.polygonbikes.ebike.v3.feature_route.domain.state.RouteManagementState
import com.polygonbikes.ebike.v3.feature_route.presentation.component.RouteFilter
import com.polygonbikes.ebike.v3.feature_route.presentation.route_management.active_route.ActiveRouteScreen
import com.polygonbikes.ebike.v3.feature_route.presentation.route_management.deleted_route.DeletedRouteScreen

@Composable
fun RouteManagementScreen(
    onPopBackStack: () -> Unit,
    onNavigate: (UiEvent.Navigate) -> Unit,
    viewModel: RouteManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state

    LaunchedEffect(key1 = viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onNavigate(event)
                else -> Unit
            }
        }
    }

    LaunchedEffect(true) {
        viewModel.onEvent(RouteManagementEvent.InitDataChanges)
    }

    RouteManagementLayout(
        state = state,
        onPopBackStack = onPopBackStack,
        onOpenDetailRoute = { route ->
            viewModel.onEvent(RouteManagementEvent.OpenDetailRoute(route))
        },
        onOpenCreateRoute = {
            viewModel.onEvent(RouteManagementEvent.OpenCreateRoute)
        },
        onOpenRouteStatistic = {
            viewModel.onEvent(RouteManagementEvent.OpenRouteStatistic)
        },
        onDeleteRoute = { routeId ->
            viewModel.onEvent(RouteManagementEvent.DeleteRoute(routeId))
        },
        onRestoreRoute = { routeId ->
            viewModel.onEvent(RouteManagementEvent.RestoreRoute(routeId))
        },
        onShowRouteFilter = {
            viewModel.onEvent(RouteManagementEvent.ShowRouteFilter)
        },
        onUpdateRouteFilter = { routeFilterData ->
            viewModel.onEvent(
                RouteManagementEvent.UpdateRouteFilter(
                    routeFilterData
                )
            )
        }

    )
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RouteManagementLayout(
    state: RouteManagementState,
    onPopBackStack: () -> Unit,
    onOpenDetailRoute: (RouteData) -> Unit,
    onOpenCreateRoute: () -> Unit,
    onOpenRouteStatistic: () -> Unit,
    onShowRouteFilter: () -> Unit,
    onUpdateRouteFilter: (RouteFilterData) -> Unit,
    onDeleteRoute: (Int) -> Unit,
    onRestoreRoute: (Int) -> Unit
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
                            text = stringResource(id = R.string.route_management_caption),
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
                ),
                actions = {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentSize(align = Alignment.Center)
                    ) {
                        IconButton(onClick = { onOpenRouteStatistic() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.statistic),
                                contentDescription = "route statistic",
                                tint = colorResource(id = R.color.white)
                            )
                        }

                        IconButton(onClick = { onOpenCreateRoute() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.create_route),
                                contentDescription = "create route",
                                tint = colorResource(id = R.color.white)
                            )
                        }

                    }
                }

            )
        }
    ) {
        //  main column
        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.background_apps))
                .fillMaxSize()
                .padding(
                    top = it.calculateTopPadding() + 16.dp,
                    start = it.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                    end = it.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    bottom = it.calculateBottomPadding() + 8.dp
                )
        ) {
            if (state.isLoading == true)
                LoadingIndicator(onDismiss = {})

            var selectedTabIndex by remember { mutableStateOf(0) }
            val tabs = listOf("Active", "Deleted")

            Column {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = colorResource(id = R.color.background_apps),
                    contentColor = colorResource(id = R.color.text),
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                when (selectedTabIndex) {
                    0 -> ActiveRouteScreen(
                        state = state,
                        onOpenDetailRoute = onOpenDetailRoute,
                        onDeleteRoute = onDeleteRoute,
                        onShowRouteFilter = onShowRouteFilter,
                        onUpdateRouteFilter = onUpdateRouteFilter
                    )
                    1 -> DeletedRouteScreen(
                        state = state,
                        onOpenDetailRoute = onOpenDetailRoute,
                        onRestoreRoute = onRestoreRoute,
                        onShowRouteFilter = onShowRouteFilter,
                        onUpdateRouteFilter = onUpdateRouteFilter
                    )
                }
            }

        }   // close main column
        if (state.showRouteFilter) {
            // open full modal bottom sheet immediately
            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            )
            ModalBottomSheet(
                onDismissRequest = { onShowRouteFilter() },
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                sheetState = sheetState
            ) {
                RouteFilter(
                    routeFilterData = state.routeFilterData,
                    onUpdateRouteFilter = onUpdateRouteFilter,
                    listCityFilter = state.listCityFilter,
                )
            }
        }
    }
}

@Composable
@Preview
fun RouteManagementScreenPreview() {
    EbikeTheme {
        RouteManagementLayout(
            state = RouteManagementState(
                listActiveRoute = remember {
                    mutableStateListOf(
                        RouteData(
                            id = 35,
                            name = "Bersepeda di sore hari",
                            thumbnail = FileEntity(
                                url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/images/8-9c5a4953-2403-4387-bc55-3292d274ca02.jpg",
                                type = "image"
                            ),
                            images = emptyList(),
                            startLocationData = LocationData(
                                1,
                                "Indonesia",
                                "Jawa Timur",
                                "Sidoarjo"
                            ),
                            startLatitude = -7.4194821f,
                            startLongitude = 112.7240684f,
                            roadType = listOf("gravel", "road"),
                            distance = 2.4,
                            elevation = 24,
                            purpose = "exercise",
                            polyline = "vdhl@}r`oTCFAHAFAFCHAFAFCHAFAFAHCFAHAFAFCHAFAFCHAHADAHCFAFAHCFAHAFAFCHAF?@AHCF",
                            gpx = FileEntity(
                                url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/gpx/8-bae85d62-4398-4a13-ab00-a798b70645d6.gpx",
                                type = "gpx"
                            )
                        )
                    )
                },
                listDeletedRoute = remember {
                    mutableStateListOf(
                        RouteData(
                            id = 35,
                            name = "Bersepeda di sore hari",
                            thumbnail = FileEntity(
                                url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/images/8-9c5a4953-2403-4387-bc55-3292d274ca02.jpg",
                                type = "image"
                            ),
                            images = emptyList(),
                            startLocationData = LocationData(
                                1,
                                "Indonesia",
                                "Jawa Timur",
                                "Sidoarjo"
                            ),
                            startLatitude = -7.4194821f,
                            startLongitude = 112.7240684f,
                            roadType = listOf("gravel", "road"),
                            distance = 2.4,
                            elevation = 24,
                            purpose = "exercise",
                            polyline = "vdhl@}r`oTCFAHAFAFCHAFAFCHAFAFAHCFAHAFAFCHAFAFCHAHADAHCFAFAHCFAHAFAFCHAF?@AHCF",
                            gpx = FileEntity(
                                url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/gpx/8-bae85d62-4398-4a13-ab00-a798b70645d6.gpx",
                                type = "gpx"
                            )
                        )
                    )
                },
            ),
            onShowRouteFilter = {},
            onUpdateRouteFilter = {},
            onPopBackStack = {},
            onOpenDetailRoute = {},
            onDeleteRoute = {},
            onRestoreRoute = {},
            onOpenCreateRoute = {},
            onOpenRouteStatistic = {}
        )
    }
}