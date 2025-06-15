package com.polygonbikes.ebike.v3.feature_history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.component.loadingSkeleton
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_history.domain.state.HistoryState
import com.polygonbikes.ebike.v3.feature_history.presentation.component.HistoryCard
import com.polygonbikes.ebike.v3.feature_trip.data.entities.TripBodyMiddleware

@Composable
fun HistoryScreen(
    onNavigate: (UiEvent.Navigate) -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
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

    val lazyListState = rememberLazyListState()

    HistoryScreenLayout(
        state = state,
        lazyListState = lazyListState,
        onPopBackStack = onPopBackStack,
        onOpenDetailHistory = {
            viewModel.onEvent(HistoryEvent.OpenHistoryDetail(it))
        },
        onPeriodSelected = { period ->
            viewModel.onEvent(HistoryEvent.SetValuePeriod(period))
        },
        onOpenStartTrip = {
            viewModel.onEvent(HistoryEvent.OpenStartTrip)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun HistoryScreenLayout(
    state: HistoryState,
    lazyListState: LazyListState,
    onPopBackStack: () -> Unit,
    onOpenDetailHistory: (TripBodyMiddleware) -> Unit,
    onPeriodSelected: (String) -> Unit,
    onOpenStartTrip: () -> Unit
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
                            text = stringResource(id = R.string.navbar_title_history_short),
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
        // trip history list
        val histories = state.listHistory
        
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

            if (!state.isHistoryLoading && state.isHistoryEmpty == true) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.DirectionsBike,
                        contentDescription = "activity icon",
                        modifier = Modifier.size(34.dp),
                        tint = colorResource(id = R.color.text)
                    )
                    Text(
                        text = stringResource(id = R.string.empty_activity_title),
                        fontFamily = fontRns,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorResource(id = R.color.text)
                    )
                    Text(
                        text = stringResource(id = R.string.empty_history_description),
                        fontFamily = fontRns,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = colorResource(id = R.color.sub_text)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onOpenStartTrip,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .wrapContentSize()
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.red_accent),
                            contentColor = colorResource(id = R.color.white)
                        )

                    ) {
                        Text(
                            stringResource(id = R.string.label_button_start_trip),
                            fontFamily = fontRns,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colorResource(id = R.color.text)
                        )
                    }

                }

            } else {

                // Row duration
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // card filter
                    var expanded by remember { mutableStateOf(false) }
                    val filterDuration = state.period
                    val durationList = listOf("week", "month", "all")

                    val durationMap = mapOf(
                        "week" to stringResource(id = R.string.trip_summary_filter_dropdown_week),
                        "month" to stringResource(id = R.string.trip_summary_filter_dropdown_month),
                        "all" to stringResource(id = R.string.trip_summary_filter_period_all)
                    )

                    // Duration mappings
                    val durationTextMap = mapOf(
                        "week" to stringResource(id = R.string.filter_period_week_text),
                        "month" to stringResource(id = R.string.filter_period_month_text),
                        "all" to stringResource(id = R.string.filter_period_all_text)
                    )

                    val displayText = durationTextMap[filterDuration] ?: stringResource(id = R.string.filter_period_month_text)

                    Text(
                        text = displayText,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = colorResource(id = R.color.text)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    var cardWidth by remember { mutableIntStateOf(0) }
                    // column card filter
                    Column {
                        Card(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .border(
                                    1.dp,
                                    colorResource(id = R.color.text),
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { expanded = !expanded }
                                .padding(8.dp)
                                .onGloballyPositioned { layoutCoordinates ->
                                    cardWidth = layoutCoordinates.size.width
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(colorResource(id = R.color.background_apps))
                                    .padding(start = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = durationMap[filterDuration] ?: "Monthly",
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
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .border(
                                    width = 1.dp,
                                    color = colorResource(id = R.color.sub_text),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .width(with(LocalDensity.current) { cardWidth.toDp() + 16.dp }),
                            containerColor = colorResource(id = R.color.background_apps),
                            onDismissRequest = { expanded = false }
                        ) {
                            durationList.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = durationMap[option] ?: "Monthly",
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
                        }   // close dropdown menu
                    }   // close column card filter
                }   // close row duration
                Spacer(modifier = Modifier.height(26.dp))

                LazyColumn(
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(0.94f)
                ) {
                    if (state.isHistoryLoading) {
                        item {
                            HistoryCard(
                                detail = TripBodyMiddleware(
                                    id = 1,
                                    name = "Morning Ride",
                                    date = "2024-03-12",
                                    avgSpeed = 22.5,
                                    bikeName = "E-Mountain Pro"
                                ),
                                onClick = { selectedTrip ->
                                    onOpenDetailHistory(TripBodyMiddleware(
                                        id = 1,
                                        name = "Morning Ride",
                                        date = "2024-03-12",
                                        avgSpeed = 22.5,
                                        bikeName = "E-Mountain Pro"
                                    ))
                                },
                                isHistoryLoading = state.isHistoryLoading
                            )
                        }
                    }
                    histories.forEach { history ->
                        item {
                            HistoryCard(
                                detail = history,
                                onClick = { selectedTrip ->
                                    onOpenDetailHistory(history)
                                },
                                isHistoryLoading = state.isHistoryLoading
                            )
                        }
                    }
                }   // close trip history list

            }

        }   // close main column with padding
    }   // close scaffold

}

@Composable
@Preview
fun HistoryScreenPreview() {
    EbikeTheme {
        HistoryScreenLayout(
            state = HistoryState(
                listHistory = listOf(
                    TripBodyMiddleware(
                        id = 1,
                        name = "Morning Ride",
                        date = "2024-03-12",
                        avgSpeed = 22.5,
                        bikeName = "E-Mountain Pro"
                    ),
                    TripBodyMiddleware(
                        id = 2,
                        name = "Evening Commute",
                        date = "2024-03-11",
                        avgSpeed = 18.2,
                        bikeName = "Urban Cruiser"
                    ),
                    TripBodyMiddleware(
                        id = 3,
                        name = "Weekend Adventure",
                        date = "2024-03-10",
                        avgSpeed = 25.0,
                        bikeName = "Gravel Explorer"
                    )
                ).toMutableStateList()
            ),
            onPopBackStack = {},
            lazyListState = rememberLazyListState(),
            onOpenDetailHistory = {},
            onPeriodSelected = {},
            onOpenStartTrip = {}
        )
    }
}
