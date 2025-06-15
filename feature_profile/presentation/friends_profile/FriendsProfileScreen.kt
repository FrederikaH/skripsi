package com.polygonbikes.ebike.v3.feature_profile.presentation.friends_profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import com.polygonbikes.ebike.core.component.LoadingIndicator
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.core.util.TimeUtil
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_home.presentation.component.ActivityCard
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ProfileData
import com.polygonbikes.ebike.v3.feature_profile.domain.state.FriendsProfileState
import com.polygonbikes.ebike.v3.feature_trip.data.entities.TripBodyMiddleware
import kotlin.collections.get

@Composable
fun FriendsProfileScreen(
    onPopBackStack: () -> Unit,
    onNavigate: (UiEvent.Navigate) -> Unit,
    userId: Int,
    viewModel: FriendsProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state

    LaunchedEffect(key1 = viewModel) {
        viewModel.onEvent(FriendsProfileEvent.GetUserProfile(userId))
        viewModel.onEvent(FriendsProfileEvent.GetUserActivity(userId))
        viewModel.onEvent(FriendsProfileEvent.GetTripSummary("month", userId))
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onNavigate(event)
                else -> Unit
            }
        }
    }

    val lazyListState = rememberLazyListState()

    FriendsProfileLayout(
        state = state,
        lazyListState = lazyListState,
        userId = userId,
        onPopBackStack = onPopBackStack,
        onPeriodSelected = { period ->
            viewModel.onEvent(FriendsProfileEvent.GetTripSummary(period, userId))
        },
        onFollow = {
            viewModel.onEvent(FriendsProfileEvent.Follow)
        },
        onUnfollow = {
            viewModel.onEvent(FriendsProfileEvent.Unfollow)
        },
        onOpenDetailActivity = {
            viewModel.onEvent(FriendsProfileEvent.OpenDetailActivity(it))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun FriendsProfileLayout(
    state: FriendsProfileState,
    lazyListState: LazyListState,
    userId: Int,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onPopBackStack: () -> Unit,
    onPeriodSelected: (String) -> Unit,
    onOpenDetailActivity: (TripBodyMiddleware) -> Unit
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
                            text = "Profile",
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
        //  main column
        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.background_apps))
                .fillMaxSize()
                .padding(
                    top = it.calculateTopPadding() + 16.dp,
                    start = it.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                    end = it.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    bottom = it.calculateBottomPadding() + 16.dp
                )
        ) {
            val activities by remember { derivedStateOf { state.listActivity } }

            if (state.isLoading)
                LoadingIndicator(onDismiss = {})

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.wrapContentHeight()
            ) {

                item {


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
                        text = state.profile?.username ?: "Unknown User".toString(),
                        fontSize = 26.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.text)
                    )

                    if (state.profile?.city != null || state.profile?.cyclingStyle?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(24.dp)
                        ) {
                            // City
                            state.profile.city?.city?.let { city ->
                                Text(
                                    text = city.takeIf { it.isNotEmpty() }
                                        ?.replaceFirstChar { it.uppercaseChar() } ?: "",
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
                    Spacer(Modifier.height(16.dp))

                    Row {
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
                    Spacer(Modifier.height(16.dp))

                    if (state.isOthersProfile) {
                        if (state.profile?.isFollowed == true) {
                            OutlinedButton(
                                onClick = { onUnfollow() },
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 12.dp)
                                    .fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, colorResource(id = R.color.text))
                            ) {
                                Text(
                                    text = "Followed",
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = colorResource(id = R.color.text)
                                )
                            }

                        } else {
                            Button(
                                onClick = { onFollow() },
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 12.dp)
                                    .fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.red_accent)),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Follow",
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = colorResource(id = R.color.text)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }


                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.5.dp,
                                color = colorResource(id = R.color.text_field_border),
                                shape = RoundedCornerShape(4.dp)
                            )
                    ) {
                        var expanded by remember { mutableStateOf(false) }
                        val filterDuration = state.period
                        val durationList = listOf("week", "month", "all")

                        val durationMap = mapOf(
                            "week" to stringResource(id = R.string.trip_summary_filter_period_week),
                            "month" to stringResource(id = R.string.trip_summary_filter_period_month),
                            "all" to stringResource(id = R.string.trip_summary_filter_period_all)
                        )

                        val durationDropdownMap = mapOf(
                            "week" to stringResource(id = R.string.trip_summary_filter_dropdown_week),
                            "month" to stringResource(id = R.string.trip_summary_filter_dropdown_month),
                            "all" to stringResource(id = R.string.trip_summary_filter_period_all)
                        )

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
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
                                            text = durationMap[filterDuration]
                                                ?: stringResource(id = R.string.trip_summary_filter_period_month),
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
                                                it == (it * 10).toInt() / 10.0 -> "%.1f km".format(
                                                    it
                                                )

                                                else -> "%.2f km".format(it)
                                            }
                                        } ?: "0 km",
                                        fontSize = 24.sp,
                                        fontFamily = fontRns,
                                        fontWeight = FontWeight.Bold,
                                        color = colorResource(id = R.color.text)
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
                                        color = colorResource(id = R.color.text)
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
                                        text = (state.tripSummary?.elevation ?: 0).toString()
                                            .plus(" m"),
                                        fontSize = 24.sp,
                                        fontFamily = fontRns,
                                        fontWeight = FontWeight.Bold,
                                        color = colorResource(id = R.color.text)
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
                                        color = colorResource(id = R.color.text)
                                    )
                                }

                            }   // close activity

                        }   // close column inside box
                    }   // close activity summary box

                }   // close item

                if (activities.size > 0) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Activities",
                            fontSize = 22.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.text)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    activities.forEach { activity ->
                        item {
                            Column {
                                ActivityCard(
                                    detail = activity,
                                    onOpenDetailActivity = { selectedActivity -> onOpenDetailActivity(activity) },
                                    isActivityLoading = state.isActivityLoading
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }

            }   // close lazy column

        }   // close main column
    }   // close scaffold
}

@Preview
@Composable
fun FriendsProfilePreview() {
    EbikeTheme {
        FriendsProfileLayout(
            state = FriendsProfileState(
                ProfileData(
                    username = "Mark Beaumont",
                    city = LocationData(
                        city = "Sidoarjo"
                    ),
                    cyclingStyle = listOf("road", "gravel", "mountain")
                )
            ),
            onPopBackStack = {},
            onPeriodSelected = {},
            userId = 1,
            lazyListState = rememberLazyListState(),
            onFollow = {},
            onUnfollow = {},
            onOpenDetailActivity = {}
        )
    }
}