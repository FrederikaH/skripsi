package com.polygonbikes.ebike.v3.feature_route.presentation.friends

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.polygonbikes.ebike.core.entities.FileEntity
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.v3.feature_route.domain.model.RouteFilterData
import com.polygonbikes.ebike.v3.feature_route.domain.state.FriendsRouteState
import com.polygonbikes.ebike.v3.feature_route.presentation.component.FriendsRoute
import com.polygonbikes.ebike.v3.feature_route.presentation.component.RouteFilter

@Composable
fun FriendsRouteScreen(
    onPopBackStack: () -> Unit,
    onNavigate: (UiEvent.Navigate) -> Unit,
    viewModel: FriendsRouteViewModel = hiltViewModel()
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

    FriendsRouteLayout(
        onPopBackStack = onPopBackStack,
        state = state,
        onOpenRouteDetail = { route ->
            viewModel.onEvent(FriendsRouteEvent.OpenRouteDetail(route))
        },
        onShowRouteFilter = {
            viewModel.onEvent(FriendsRouteEvent.ShowRouteFilter)
        },
        onUpdateRouteFilter = { routeFilterData ->
            viewModel.onEvent(
                FriendsRouteEvent.UpdateRouteFilter(
                    routeFilterData
                )
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsRouteLayout(
    onPopBackStack: () -> Unit,
    state: FriendsRouteState,
    onOpenRouteDetail: (RouteData) -> Unit,
    onShowRouteFilter: () -> Unit,
    onUpdateRouteFilter: (RouteFilterData) -> Unit
) {
    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentSize(align = Alignment.Center)
                    ) {
                        Text(
                            text = stringResource(id = R.string.friends_route_caption),
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
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorResource(id = R.color.background_apps)),
                        verticalArrangement = Arrangement.Center
                    ) {

                        // row filter
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
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
                    }   // Box background filter
                    Spacer(modifier = Modifier.height(16.dp))

                    // Scrollable column
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(
                                modifier = Modifier
                                    .height(8.dp)
                                    .fillMaxWidth()
                            )
                        }

                        items(count = state.listFriendsRoute.size) { index ->
                            val route = state.listFriendsRoute[index]

                            FriendsRoute(
                                detail = route,
                                onClick = { onOpenRouteDetail(route) }
                            )
                        }
                    }   // close scrollable column
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
    }   // close scaffold
}

@Composable
@Preview
fun FriendsRoutePreview() {
    EbikeTheme {
        FriendsRouteLayout(
            onPopBackStack = {},
            state = FriendsRouteState(
                listFriendsRoute = remember {
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
            onOpenRouteDetail = {},
            onShowRouteFilter = {},
            onUpdateRouteFilter = {}
        )
    }
}