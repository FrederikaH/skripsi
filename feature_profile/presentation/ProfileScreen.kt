package com.polygonbikes.ebike.v3.feature_profile.presentation

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.FeatureList
import com.polygonbikes.ebike.core.component.loadingSkeleton
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.core.util.TimeUtil
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_event.data.entities.EventData
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ProfileData
import com.polygonbikes.ebike.v3.feature_profile.domain.state.ProfileState
import com.polygonbikes.ebike.v3.feature_profile.presentation.component.UpcomingEventCard

@Composable
fun ProfileScreen(
    onNavigate: (UiEvent.Navigate) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
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

    LaunchedEffect(key1 = true) {
        viewModel.onEvent(ProfileEvent.FetchData)
    }

    ProfileLayout(
        state = state,
        onOpenHistoryScreen = {
            viewModel.onEvent(ProfileEvent.OpenHistoryScreen)
        },

        onOpenRouteManagementScreen = {
            viewModel.onEvent(ProfileEvent.OpenRouteManagementScreen)
        },

        onPeriodSelected = { period ->
            viewModel.onEvent(ProfileEvent.SetValuePeriod(period))
        },

        onOpenDetailEvent = {
            viewModel.onEvent(ProfileEvent.OpenEventDetail(it))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun ProfileLayout(
    state: ProfileState,
    onOpenHistoryScreen: () -> Unit,
    onOpenRouteManagementScreen: () -> Unit,
    onPeriodSelected: (String) -> Unit,
    onOpenDetailEvent: (EventData) -> Unit
) {

    val upcomingEvents = state.listUpcomingEvent

    Scaffold {
        //  main column
        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.background_apps))
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = it.calculateTopPadding() + 16.dp,
                    start = it.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                    end = it.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    bottom = it.calculateBottomPadding() + 16.dp
                )
        ) {
//            Box{
//                Image(
//                    painter = painterResource(id = R.drawable.route_image),
//                    contentDescription = "profile picture",
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier
//                        .size(66.dp)
//                        .clip(CircleShape)
//                )
//
//                IconButton(
//                    onClick = {},
//                    modifier = Modifier
//                        .clip(CircleShape)
//                        .size(22.dp)
//                        .background(color = colorResource(id = R.color.lighter_gray))
//                        .wrapContentSize(align = Alignment.Center)
//                        .align(Alignment.BottomEnd)
//                ) {
//                    Icon(
//                        imageVector = Icons.Outlined.PhotoCamera,
//                        contentDescription = "Camera icon",
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(start = 3.8.dp, end = 3.dp, top = 3.dp, bottom = 3.dp),
//                        tint = colorResource(id = R.color.white)
//                    )
//                }
//            }
//            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = state.profile?.username ?: "Username".toString(),
                fontSize = 26.sp,
                fontFamily = fontRns,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.text),
                modifier = Modifier.loadingSkeleton(state.isProfileLoading)
            )

            // Only show city and profile if it's not null
            if (state.profile?.city != null || state.profile?.cyclingStyle?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(24.dp)
                        .loadingSkeleton(state.isProfileLoading)
                ) {
                    // City
                    state.profile.city?.city?.let { city ->
                        Text(
                            text = city,
                            fontSize = 18.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.Medium,
                            color = colorResource(id = R.color.text)
                        )
                    }

                    val cyclingStyleMap = mapOf(
                        "road" to "Road",
                        "gravel" to "Gravel",
                        "mountain" to "Off-road"
                    )

                    // Cyling style
                    state.profile.cyclingStyle?.takeIf { it.isNotEmpty() }?.let { styles ->
                        if (state.profile.city != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            VerticalDivider(
                                modifier = Modifier
                                    .width(1.5.dp)
                                    .fillMaxHeight()
                                    .background(colorResource(id = R.color.sub_text))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Icon(
                            painter = painterResource(id = R.drawable.road),
                            contentDescription = "Cycling type icon",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = styles.joinToString(", ") { cyclingStyleMap[it] ?: it },
                            fontSize = 18.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.Medium,
                            color = colorResource(id = R.color.text)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (FeatureList.FeatureFriendship) {
                Row(
                    modifier = Modifier.loadingSkeleton(state.isProfileLoading)
                ) {
                    Column {
                        Text(
                            text = (state.profile?.totalFollower ?: 0).toString(),
                            fontSize = 22.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.text)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Followers",
                            fontSize = 18.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.Medium,
                            color = colorResource(id = R.color.sub_text)
                        )
                    }
                    Spacer(Modifier.width(24.dp))

                    Column {
                        Text(
                            text = (state.profile?.totalFollowing ?: 0).toString(),
                            fontSize = 22.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.text)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Following",
                            fontSize = 18.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.Medium,
                            color = colorResource(id = R.color.sub_text)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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

                    val durationMap = mapOf(
                        "week" to stringResource(id = R.string.trip_summary_filter_period_week),
                        "month" to stringResource(id = R.string.trip_summary_filter_period_month),
                        "year" to stringResource(id = R.string.trip_summary_filter_period_all)
                    )

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

                    // Activity statistic's summary
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(id = R.string.total_distance_label),
                                fontSize = 14.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(id = R.color.sub_text)
                            )

                            Text(
                                text = state.tripSummary?.distance?.let {
                                    when {
                                        it % 1.0 == 0.0 -> it.toInt().toString()
                                        it == (it * 10).toInt() / 10.0 -> "%.1f km".format(it)
                                        else -> "%.2f km".format(it)
                                    }
                                } ?: "0 km",
                                fontSize = 24.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(id = R.color.text),
                                modifier = Modifier.loadingSkeleton(state.isTripSummaryLoading)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(id = R.string.trip_summary_total_trip),
                                fontSize = 14.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(id = R.color.sub_text)
                            )
                            Text(
                                text = state.tripSummary?.trips?.toString() ?: "0",
                                fontSize = 24.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(id = R.color.text),
                                modifier = Modifier.loadingSkeleton(state.isTripSummaryLoading)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(id = R.string.total_elevation_label),
                                fontSize = 14.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(id = R.color.sub_text)
                            )
                            Text(
                                text = (state.tripSummary?.elevation ?: 0).toString().plus(" m"),
                                fontSize = 24.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(id = R.color.text),
                                modifier = Modifier.loadingSkeleton(state.isTripSummaryLoading)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(id = R.string.trip_summary_total_time),
                                fontSize = 14.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(id = R.color.sub_text)
                            )
                            Text(
                                text = (TimeUtil.formatSimpleTime(
                                    state.tripSummary?.time ?: 0
                                )).toString(),
                                fontSize = 24.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(id = R.color.text),
                                modifier = Modifier.loadingSkeleton(state.isTripSummaryLoading)
                            )
                        }

                    }   // close activity

                }   // close column inside box
            }   // close activity summary box
            Spacer(modifier = Modifier.height(16.dp))

            Row {
//                Button(
//                    onClick = {},
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(48.dp),
//                    colors = ButtonDefaults.buttonColors().copy(
//                        containerColor = colorResource(id = R.color.secondary)
//                    ),
//                    shape = RoundedCornerShape(4.dp)
//                ) {
//                    // Saved route
//                    Row (
//                        verticalAlignment = Alignment.CenterVertically,
//                    ){
//                        Icon(
//                            painter = painterResource(id = R.drawable.save),
//                            contentDescription = "Save icon",
//                            tint = colorResource(id = R.color.text),
//                            modifier = Modifier.size(14.dp)
//                        )
//                        Spacer(modifier = Modifier.width(10.dp))
//
//                        Text(
//                            text = "Saved route",
//                            fontFamily = fontRns,
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 18.sp,
//                            color = colorResource(id = R.color.text)
//                        )
//                    }
//                }   // Close saved route button
//                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        onOpenHistoryScreen()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = colorResource(id = R.color.secondary)
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    // Trip history
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Save icon",
                            tint = colorResource(id = R.color.text),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = stringResource(id = R.string.label_button_history),
                            fontFamily = fontRns,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colorResource(id = R.color.text)
                        )
                    }
                }   // Close button trip history
            }   // Close button row

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Your upcoming joined events",
                fontFamily = fontRns,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colorResource(id = R.color.text)
            )

            Spacer(modifier = Modifier
                .height(12.dp)
                .fillMaxWidth())

            if (!state.isUpcomingEventLoading && state.listUpcomingEvent.isEmpty()) {
                Text(
                    text = "There is no upcoming event",
                    fontSize = 14.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(id = R.color.sub_text)
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.isUpcomingEventLoading) {
                    items(1) {
                        UpcomingEventCard(
                            detail = EventData(
                                id = 0,
                                startAt = "2025-03-20 08:00:00",
                                endAt = "2025-03-20 08:00:00"
                            ),
                            onClick = { selectedEvent ->
                                onOpenDetailEvent(EventData(id = 0))
                            },
                            isUpcomingEventLoading = true
                        )
                    }
                }

                items(upcomingEvents) { upcomingEvent ->
                    UpcomingEventCard(
                        detail = upcomingEvent,
                        onClick = { selectedEvent ->
                            onOpenDetailEvent(upcomingEvent)
                        },
                        isUpcomingEventLoading = state.isUpcomingEventLoading
                    )
                }
            }

        }   // close main column
    }
}

@Preview
@Composable
fun ProfilePreview() {
    EbikeTheme {
        ProfileLayout(
            state = ProfileState(
                ProfileData(
                    username = "Catalina",
                    city = LocationData(
                        id = 1,
                        city = "Sidoarjo"
                    ),
                    cyclingStyle = listOf("road", "gravel", "mountain")
                )
            ),
            onOpenHistoryScreen = {},
            onOpenRouteManagementScreen = {},
            onPeriodSelected = {},
            onOpenDetailEvent = {}
        )
    }
}