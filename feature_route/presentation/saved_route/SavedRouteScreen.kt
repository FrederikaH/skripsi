package com.polygonbikes.ebike.v3.feature_route.presentation.saved_route

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.entities.FileEntity
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.v3.feature_route.domain.state.SavedRouteState
import com.polygonbikes.ebike.v3.feature_route.presentation.RouteEvent
import com.polygonbikes.ebike.v3.feature_route.presentation.RouteViewModel
import com.polygonbikes.ebike.v3.feature_route.presentation.component.RouteCard

@Composable
fun SavedRouteScreen(
    onPopBackStack: () -> Unit,
    onNavigate: (UiEvent.Navigate) -> Unit,
    viewModel: SavedRouteViewModel = hiltViewModel(),
    routeViewModel: RouteViewModel = hiltViewModel()
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

    LaunchedEffect(key1 = routeViewModel) {
        routeViewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onNavigate(event)
                else -> Unit
            }
        }
    }

    val lazyListState = rememberLazyListState()

    SavedRouteLayout(
        state = state,
        lazyListState = lazyListState,
        onPopBackStack = onPopBackStack,
        onOpenRouteDetail = { route ->
            routeViewModel.onEvent(RouteEvent.OpenRouteDetail(route))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedRouteLayout(
    state: SavedRouteState,
    lazyListState: LazyListState,
    onOpenRouteDetail: (RouteData) -> Unit,
    onPopBackStack: () -> Unit
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
                            text = stringResource(id = R.string.saved_route_caption),
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
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                //  routes list
                items(count = state.listRoute.size) { index ->
                    val route = state.listRoute[index]

                    RouteCard(
                        detail = route,
                        onClick = { onOpenRouteDetail(route) },
                        isRouteLoading = state.isRouteLoading
                    )
                }
            }   // close scrollable column

            if (state.listRoute.isEmpty() && state.isRouteLoading == false) {
                Text(
                    text = "No saved routes yet.",
                    fontSize = 14.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(id = R.color.sub_text)
                )
            }
        }
    }
}

@Composable
@Preview
fun SavedRoutePreview() {
    EbikeTheme {
        SavedRouteLayout(
            onPopBackStack = {},
            state = SavedRouteState(
                listRoute = remember {
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
                }
            ),
            lazyListState = rememberLazyListState(),
            onOpenRouteDetail = {}
        )
    }
}