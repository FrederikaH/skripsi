package com.polygonbikes.ebike.v3.feature_route.presentation.route_management.route_statistic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.component.PieChart
import com.polygonbikes.ebike.core.entities.FileEntity
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.core.component.RouteCard
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_profile.presentation.ProfileEvent
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.v3.feature_route.domain.state.RouteStatisticState
import kotlin.collections.get

@Composable
fun RouteStatisticScreen(
    onPopBackStack: () -> Unit,
    onNavigate: (UiEvent.Navigate) -> Unit,
    viewModel: RouteStatisticViewModel = hiltViewModel()
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

    RouteStatisticLayout(
        state = state,
        onPopBackStack = onPopBackStack,
        onOpenDetailRoute = { route ->
            viewModel.onEvent(RouteStatisticEvent.OpenDetailRoute(route))
        },
        onPeriodSelected = { period ->
            viewModel.onEvent(RouteStatisticEvent.SetValuePeriod(period))
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteStatisticLayout(
    state: RouteStatisticState,
    onPopBackStack: () -> Unit,
    onOpenDetailRoute: (RouteData) -> Unit,
    onPeriodSelected: (String) -> Unit
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
                            text = "Route statistic",
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
        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.background_apps))
                .fillMaxSize()
                .padding(
                    top = it.calculateTopPadding() + 16.dp,
                    start = it.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                    end = it.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    bottom = it.calculateBottomPadding()
                )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Popular road types",
                        fontSize = 18.sp,
                        fontFamily = fontRns,
                        color = colorResource(id = R.color.text),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.5.dp,
                                color = colorResource(id = R.color.text_field_border),
                                shape = RoundedCornerShape(4.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            var expanded by remember { mutableStateOf(false) }
                            val filterDuration = state.period
                            val durationList = listOf("week", "month", "year")

                            val durationDropdownMap = mapOf(
                                "week" to stringResource(id = R.string.trip_summary_filter_dropdown_week),
                                "month" to stringResource(id = R.string.trip_summary_filter_dropdown_month),
                                "year" to stringResource(id = R.string.trip_summary_filter_dropdown_year)
                            )

                            Column {
                                Card(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { expanded = !expanded }
                                ) {
                                    Row(
                                        modifier = Modifier.background(colorResource(id = R.color.background_apps)),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = durationDropdownMap[filterDuration] ?: stringResource(id = R.string.trip_summary_filter_period_month),
                                            color = colorResource(id = R.color.text),
                                            fontFamily = fontRns,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 18.sp
                                        )
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = "dropdown arrow",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    durationList.forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = durationDropdownMap[option] ?: "Month",
                                                    color = colorResource(id = R.color.text),
                                                    fontFamily = fontRns,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 16.sp
                                                )
                                            },
                                            onClick = {
                                                expanded = false
                                                onPeriodSelected(option)
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            PieChart(
                                data = mapOf(
                                    "Paved road" to (state.roadTypesCount?.paved ?: 0),
                                    "Gravel road" to (state.roadTypesCount?.gravel ?: 0),
                                    "Off-road" to (state.roadTypesCount?.offroad ?: 0),
                                )
                            )

                        }
                    }
                }

                item {
                    Text(
                        text = "Most saved route",
                        fontSize = 18.sp,
                        fontFamily = fontRns,
                        color = colorResource(id = R.color.text),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(state.listMostSavedRoute) { route ->
                    RouteCard(
                        route = route,
                        onClick = { onOpenDetailRoute(RouteData()) },
                        isShowSaveCount = true
                    )
                }
                item{ Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun RouteStatisticPreview() {
    EbikeTheme {
        RouteStatisticLayout(
            state = RouteStatisticState(
                listMostSavedRoute = remember {
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
            onPopBackStack = {},
            onOpenDetailRoute = {},
            onPeriodSelected = {}
        )
    }
}
