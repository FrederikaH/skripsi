package com.polygonbikes.ebike.v3.feature_home.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.util.TimeUtil
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_home.domain.state.HomeState
import com.polygonbikes.ebike.v3.feature_home.presentation.component.ActivityCard
import com.polygonbikes.ebike.v3.feature_trip.data.entities.TripBodyMiddleware
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.polygonbikes.ebike.core.FeatureList
import com.polygonbikes.ebike.core.component.loadingSkeleton
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.core.model.Thumbnail
import com.polygonbikes.ebike.v3.feature_event.data.entities.EventData
import com.polygonbikes.ebike.core.model.Images
import com.polygonbikes.ebike.v3.feature_home.presentation.component.FriendsEventCard
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ProfileData
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigate: (UiEvent.Navigate) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
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

    LaunchedEffect(key1 = viewModel) {
        viewModel.getHomeData()
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                viewModel.syncFollowDataWithDB()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val lazyListState = rememberLazyListState()

    HomeLayout(
        state = state,
        lazyListState = lazyListState,
        onOpenProfileScreen = {
            viewModel.onEvent(HomeEvent.OpenProfileScreen)
        },

        onOpenEventScreen = {
            viewModel.onEvent(HomeEvent.OpenEventScreen)
        },

        onOpenGroupScreen = {
            viewModel.onEvent(HomeEvent.OpenGroupScreen)
        },

        onOpenFriendsEventScreen = {
            viewModel.onEvent(HomeEvent.OpenFriendsEvent)
        },

        onOpenTripScreen = {
            viewModel.onEvent(HomeEvent.OpenTripScreen)
        },

        onOpenDetailActivity = {
            viewModel.onEvent(HomeEvent.OpenDetailActivity(it))
        },

        onOpenUserProfile = {
            viewModel.onEvent(HomeEvent.OpenUserProfile(it))
        },

        onFollow = { userId ->
            viewModel.onEvent(HomeEvent.Follow(userId))
        },

        onUnfollow = { userId ->
            viewModel.onEvent(HomeEvent.Unfollow(userId))
        },

        onOpenDetailEvent = {
            viewModel.onEvent(HomeEvent.OpenEventDetail(it))
        },

        onJoinEvent = { eventId ->
            viewModel.onEvent(HomeEvent.JoinEvent(eventId))
        },

        onLeaveEvent = { eventId ->
            viewModel.onEvent(HomeEvent.LeaveEvent(eventId))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun HomeLayout(
    state: HomeState,
    lazyListState: LazyListState,
    onOpenProfileScreen: () -> Unit,
    onOpenEventScreen: () -> Unit,
    onOpenGroupScreen: () -> Unit,
    onOpenFriendsEventScreen: () -> Unit,
    onOpenTripScreen: () -> Unit,
    onOpenDetailActivity: (TripBodyMiddleware) -> Unit,
    onOpenUserProfile: (Int) -> Unit,
    onFollow: (Int) -> Unit,
    onUnfollow: (Int) -> Unit,
    onOpenDetailEvent: (EventData) -> Unit,
    onJoinEvent: (Int) -> Unit,
    onLeaveEvent: (Int) -> Unit
) {
    val activities by remember { derivedStateOf { state.listActivity } }

    Scaffold {
        //  main column
        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.background_apps))
                .fillMaxSize()
                .padding(
                    top = it.calculateTopPadding(),
                    start = it.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                    end = it.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    bottom = it.calculateBottomPadding()
                )
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.wrapContentHeight()
            ) {
                item {
                    Spacer(Modifier.height(16.dp))
                    // Card summary trip
                    Card {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .height(176.dp)
                                .loadingSkeleton(state.isTripSummaryLoading)
                        ) {
                            Image(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(4.dp)),
                                painter = painterResource(id = if (state.tripSummary != null) R.drawable.home_filled_activity else R.drawable.home_empty_activity),
                                contentDescription = "card background",
                                contentScale = ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = if (state.tripSummary != null) 0.3f else 0.5f),
                                                Color.Black.copy(alpha = 0.4f)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                            ) {

                                if (state.tripSummary != null) {
                                    Text(
                                        text = stringResource(id = R.string.home_filled_trip_summary_title),
                                        color = colorResource(id = R.color.text),
                                        fontSize = 20.sp,
                                        fontFamily = fontRns,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = state.tripSummary.trips?.toString() ?: "0",
                                            color = colorResource(id = R.color.text),
                                            fontSize = 60.sp,
                                            fontFamily = fontRns,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(id = R.string.home_filled_trip_summary_trips),
                                            color = colorResource(id = R.color.text),
                                            fontSize = 18.sp,
                                            fontFamily = fontRns,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier
                                                .padding(bottom = 6.dp)
                                        )
                                        Spacer(modifier = Modifier.width(20.dp))

                                        Text(
                                            text = state.tripSummary.distance?.let {
                                                "%.1f".format(Locale.US, it).trimEnd('0').trimEnd('.')
                                            } ?: "0",
                                            color = colorResource(id = R.color.text),
                                            fontSize = 60.sp,
                                            fontFamily = fontRns,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(id = R.string.home_filled_trip_summary_distance),
                                            color = colorResource(id = R.color.text),
                                            fontSize = 18.sp,
                                            fontFamily = fontRns,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier
                                                .padding(bottom = 6.dp)
                                        )

                                    }
                                    Spacer(modifier = Modifier.weight(1f))

                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.End)
                                            .clickable(onClick = { onOpenProfileScreen() })
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.home_filled_trip_summary_seemore),
                                            color = colorResource(id = R.color.text),
                                            fontSize = 18.sp,
                                            fontFamily = fontRns,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = "icon arrow"
                                        )
                                    }
                                } else {
                                    Text(
                                        text = stringResource(id = R.string.home_empty_trip_summary_title),
                                        color = colorResource(id = R.color.text),
                                        fontSize = 22.sp,
                                        fontFamily = fontRns,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = stringResource(id = R.string.home_empty_trip_summary_description),
                                        color = colorResource(id = R.color.text),
                                        fontSize = 16.sp,
                                        fontFamily = fontRns,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.weight(1f))

                                    OutlinedButton(
                                        onClick = { onOpenTripScreen() },
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = 4.dp)
                                            .height(36.dp)
                                            .align(Alignment.CenterHorizontally),
                                        border = BorderStroke(
                                            1.dp,
                                            colorResource(id = R.color.text)
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = colorResource(id = R.color.text),
                                        ),
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.home_empty_trip_summary_start_trip),
                                            fontSize = 16.sp,
                                            fontFamily = fontRns,
                                            color = colorResource(id = R.color.text),
                                            fontWeight = FontWeight.SemiBold

                                        )
                                    }
                                }

                            }

                        }
                    }   // close card activity


                    Spacer(modifier = Modifier.height(16.dp))

                    // City location row
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = "location icon",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = (if (!state.city.isNullOrEmpty()) state.city else "Cant find location").toString(),
                            fontSize = 20.sp,
                            lineHeight = 1.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text),
                            modifier = Modifier.loadingSkeleton(state.isLocationLoading)
                        )
                    }


                    Spacer(modifier = Modifier.height(16.dp))

                    // Weather row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .loadingSkeleton(state.isWeatherLoading)
                    ) {
                        (0 until 5).forEach { index ->
                            val weather = state.listWeather.getOrNull(index)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .then(
                                        if (index == 0) Modifier.border(
                                            width = 1.5.dp,
                                            color = colorResource(id = R.color.text),
                                            shape = RoundedCornerShape(6.dp)
                                        ) else Modifier
                                    )
                                    .padding(vertical = 8.dp)
                            ) {
                                val formattedDate = if (index == 0) {
                                    "NOW"
                                } else {
                                    if (state.isWeatherLoading) {
                                        ""
                                    } else {
                                        weather?.time?.let {
                                            TimeUtil.getFormattedDate(it, "yyyy-MM-dd HH:mm", "h a")
                                        } ?: "No data"
                                    }
                                }

                                Text(
                                    text = formattedDate,
                                    fontSize = 16.sp,
                                    lineHeight = 1.sp,
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorResource(id = R.color.text)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Image(
                                    painter = rememberAsyncImagePainter(weather?.icon),
                                    contentDescription = "icon weather",
                                    modifier = Modifier
                                        .size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (state.isWeatherLoading) {
                                        ""
                                    } else {
                                        "${weather?.temp?.toInt() ?: "N/A"}°"
                                    },
                                    fontSize = 16.sp,
                                    lineHeight = 1.sp,
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorResource(id = R.color.text),
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                        }
                    }
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    //  menu event & group
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(16.dp),
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Card(
//                            modifier = Modifier
//                                .weight(1f)
//                                .clip(RoundedCornerShape(6.dp))
//                                .clickable { onOpenEventScreen() }
//                                .background(colorResource(id = R.color.secondary)),
//                        ) {
//                            Box(modifier = Modifier.padding(8.dp)) {
////                        Box(
////                            modifier = Modifier
////                                .size(8.dp)
////                                .background(
////                                    color = Color.Red,
////                                    shape = CircleShape
////                                )
////                                .align(Alignment.TopEnd)
////                        )
//                                Column(
//                                    Modifier
//                                        .fillMaxWidth()
//                                        .padding(vertical = 6.dp),
//                                    horizontalAlignment = Alignment.CenterHorizontally
//                                ) {
//                                    Icon(
//                                        Icons.Outlined.Event,
//                                        contentDescription = "icon event",
//                                        modifier = Modifier.size(24.dp),
//                                        tint = colorResource(id = R.color.text)
//                                    )
//                                    Spacer(modifier = Modifier.height(4.dp))
//
//                                    Text(
//                                        text = stringResource(id = R.string.event_caption),
//                                        fontSize = 18.sp,
//                                        fontFamily = fontRns,
//                                        fontWeight = FontWeight.SemiBold,
//                                        color = colorResource(id = R.color.text),
//                                        modifier = Modifier
//                                            .padding(start = 4.dp)
//                                    )
//                                }
//                            }   // close box event
//                        }   // close card event
//
//                        Card(
//                            modifier = Modifier
//                                .weight(1f)
//                                .clip(RoundedCornerShape(6.dp))
//                                .clickable { onOpenGroupScreen() }
//                                .background(colorResource(id = R.color.secondary)),
//                        ) {
//                            Box(modifier = Modifier.padding(8.dp)) {
////                        Box(
////                            modifier = Modifier
////                                .size(8.dp)
////                                .background(
////                                    color = Color.Red,
////                                    shape = CircleShape
////                                )
////                                .align(Alignment.TopEnd)
////                        )
//                                Column(
//                                    Modifier
//                                        .fillMaxWidth()
//                                        .padding(vertical = 8.dp),
//                                    horizontalAlignment = Alignment.CenterHorizontally
//                                ) {
//                                    Icon(
//                                        Icons.Outlined.Groups,
//                                        contentDescription = "icon group",
//                                        modifier = Modifier.size(28.dp),
//                                        tint = colorResource(id = R.color.text)
//                                    )
//
//                                    Text(
//                                        text = stringResource(id = R.string.group_caption),
//                                        fontSize = 16.sp,
//                                        fontFamily = fontRns,
//                                        fontWeight = FontWeight.SemiBold,
//                                        color = colorResource(id = R.color.text),
//                                        modifier = Modifier
//                                            .padding(start = 4.dp)
//                                    )
//                                }
//                            }   // close box group
//                        }   // close card group
//                    }   // close row card event & grup

                    Spacer(modifier = Modifier.height(32.dp))

//                    if (FeatureList.FeatureFriendship) {
//                        // column friend's event
//                        Column {
//                            Row (
//                                modifier = Modifier.clickable{ onOpenFriendsEventScreen() }
//                            ){
//                                Text(
//                                    text = "Your friends joined these events",
//                                    fontSize = 18.sp,
//                                    fontFamily = fontRns,
//                                    fontWeight = FontWeight.Bold,
//                                    color = colorResource(id = R.color.text),
//                                    modifier = Modifier.padding(start = 4.dp)
//                                )
//                                Spacer(modifier = Modifier.weight(1f))
//                                Icon(
//                                    Icons.Default.ChevronRight,
//                                    contentDescription = "icon arrow"
//                                )
//                            }
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            if (!state.isFriendsEventLoading && state.friendsListEvent.isEmpty()) {
//                                Text(
//                                    text = "You don’t have any friend's upcoming events yet.",
//                                    fontSize = 14.sp,
//                                    fontFamily = fontRns,
//                                    fontWeight = FontWeight.SemiBold,
//                                    color = colorResource(id = R.color.sub_text)
//                                )
//                                Spacer(modifier = Modifier.height(32.dp))
//                            }
//
//                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                                if (state.isFriendsEventLoading) {
//                                    items(1) {
//                                        FriendsEventCard(
//                                            detail = EventData(
//                                                id = 0,
//                                                date = "2025-02-20",
//                                                startAt = "2025-03-20 08:00:00",
//                                                endAt = "2025-03-20 08:00:00"
//                                            ),
//                                            onClick = { selectedEvent -> onOpenDetailEvent(EventData(
//                                                id = 0,
//                                                date = "2025-02-20",
//                                                startAt = "2025-03-20 08:00:00",
//                                                endAt = "2025-03-20 08:00:00"
//                                            )) },
//                                            onJoinEvent = { eventId -> onJoinEvent(eventId) },
//                                            onLeaveEvent = { eventId -> onLeaveEvent(eventId) },
////                                            isRecentlyJoined = EventData(
////                                                id = 0,
////                                                date = "2025-02-20",
////                                                startAt = "2025-03-20 08:00:00",
////                                                endAt = "2025-03-20 08:00:00"
////                                            ) in recentlyJoinedEvents,
//                                            isFriendsEventLoading = true
//                                        )
//                                    }
//                                }
//
//                                items(state.friendsListEvent) { event ->
//                                    FriendsEventCard(
//                                        detail = event,
//                                        onClick = { selectedEvent -> onOpenDetailEvent(event) },
//                                        onJoinEvent = { eventId -> onJoinEvent(eventId) },
//                                        onLeaveEvent = { eventId -> onLeaveEvent(eventId) },
//                                        isFriendsEventLoading = state.isFriendsEventLoading
//                                    )
//                                }
//                            }
//                        }   // close column friend's event
//
//                        if (state.isFriendsEventLoading || state.friendsListEvent.isNotEmpty()) {
//                            Spacer(modifier = Modifier.height(32.dp))
//                        }d
//                    }

                    Text(
                        text = stringResource(id = R.string.activity_header_section),
                        fontSize = 18.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.text)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                }   // close item 1

                if (activities.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.road),
                                contentDescription = "Activity icon",
                                modifier = Modifier.size(32.dp),
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = stringResource(id = R.string.empty_activity_title),
                                fontFamily = fontRns,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = colorResource(id = R.color.text)
                            )
                            Text(
                                text = stringResource(id = R.string.home_empty_activity_description),
                                fontFamily = fontRns,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                color = colorResource(id = R.color.sub_text)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onOpenTripScreen,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .wrapContentSize(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(id = R.color.red_accent),
                                    contentColor = colorResource(id = R.color.white)
                                )
                            ) {
                                Text(
                                    text = stringResource(id = R.string.label_button_start_trip),
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = colorResource(id = R.color.text)
                                )
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }

                } else {
                    activities.forEach { activity ->
                        item {
                            Column {
                                ActivityCard(
                                    detail = activity,
                                    onOpenDetailActivity = { selectedActivity ->
                                        onOpenDetailActivity(
                                            activity
                                        )
                                    },
                                    onOpenUserProfile = { userId -> onOpenUserProfile(userId) },
                                    onFollow = { userId -> onFollow(userId) },
                                    onUnfollow = { userId -> onUnfollow(userId) },
                                    isActivityLoading = state.isActivityLoading
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }

            }
            Spacer(modifier = Modifier.height(50.dp))

        }   // main column
    }   // close scaffold

}

@Composable
@Preview
fun LoadingHomePreview() {
    EbikeTheme {
        HomeLayout(
            state = HomeState(),
            lazyListState = rememberLazyListState(),
            onOpenProfileScreen = {},
            onOpenTripScreen = {},
            onOpenEventScreen = {},
            onOpenGroupScreen = {},
            onOpenDetailActivity = {},
            onOpenUserProfile = {},
            onFollow = {},
            onUnfollow = {},
            onOpenDetailEvent = {},
            onJoinEvent = {},
            onLeaveEvent = {},
            onOpenFriendsEventScreen = {}
        )
    }
}

@Composable
@Preview
fun HomePreview() {
    EbikeTheme {
        HomeLayout(
            state = HomeState(
                listActivity = remember {
                    mutableStateListOf(
                        TripBodyMiddleware(
                            name = "Trip Saturday",
                            route = RouteData(
                                name = "route234",
                                roadType = listOf("paved", "gravel"),
                                startLocationData = LocationData(
                                    id = 1,
                                    country = "indonesia",
                                    region = "jawa timur",
                                    city = "sidoarjo"
                                )
                            ),
                            user = ProfileData(
                                userId = 1,
                                username = "User 45"
                            ),
                            date = "2025-03-12 00:04:10"
                        )
                    )
                },
                friendsListEvent = remember {
                    mutableStateListOf(
                        EventData(
                            id = 17,
                            name = "Sepedaan seharian",
                            locationData = LocationData(
                                id = 1,
                                region = "Jawa Timur",
                                city = "Sidoarjo"
                            ),
                            date = "2025-02-20",
                            startAt = "2025-02-20 08:00:00",
                            endAt = "2025-02-20 15:59:59",
                            description = "Ini deskripsi data nya",
                            thumbnail = Thumbnail(url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/images/gLnJ1M9ZJSGJxwfRzUGp1G1gFFx8y2LSEzQUJ9jH.jpg"),
                            creator = ProfileData(
                                userId = 1,
                                username = "Ahmadfaris"
                            ),
                            members = listOf(
                                ProfileData(
                                    userId = 1,
                                    username = "Ahmadfaris"
                                )
                            ),
                            isMember = false,
                            images = listOf(
                                Images(url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/images/gLnJ1M9ZJSGJxwfRzUGp1G1gFFx8y2LSEzQUJ9jH.jpg")
                            )
                        )
                    )
                },
                isTripSummaryLoading = false,
                isWeatherLoading = false,
                isLocationLoading = false,
                isFriendsEventLoading = false,
                isActivityLoading = true
            ),
            lazyListState = rememberLazyListState(),
            onOpenProfileScreen = {},
            onOpenTripScreen = {},
            onOpenEventScreen = {},
            onOpenGroupScreen = {},
            onOpenDetailActivity = {},
            onOpenUserProfile = {},
            onFollow = {},
            onUnfollow = {},
            onOpenDetailEvent = {},
            onJoinEvent = {},
            onLeaveEvent = {},
            onOpenFriendsEventScreen = {}
        )
    }
}