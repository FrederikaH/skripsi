package com.polygonbikes.ebike.v3.feature_home.presentation.detail_activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.FeatureList
import com.polygonbikes.ebike.core.component.BikeRoadType
import com.polygonbikes.ebike.core.component.CommentInputBox
import com.polygonbikes.ebike.core.component.MapPreview
import com.polygonbikes.ebike.core.util.TimeUtil
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_home.domain.state.DetailActivityState
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.v3.feature_route.presentation.component.Comment
import com.polygonbikes.ebike.v3.feature_trip.data.entities.TripBodyMiddleware
import java.util.Locale

@Composable
fun DetailActivityScreen(
    onPopBackStack: () -> Unit,
    onNavigate: (UiEvent.Navigate) -> Unit,
    activity: TripBodyMiddleware,
    viewModel: DetailActivityViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    LaunchedEffect(key1 = viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onNavigate(event)
                else -> Unit
            }
        }
    }

    LaunchedEffect(key1 = viewModel) {
        viewModel.onEvent(DetailActivityEvent.GetData(activity))
    }

    DetailActivityLayout(
        state = state,
        onPopBackStack = onPopBackStack,
        onInputComment = {
            viewModel.onEvent(DetailActivityEvent.OnInputComment(it))
        },

        onSendComment = {
            viewModel.onEvent(DetailActivityEvent.SendComment)
        },

        onSetCommentSending = {
            viewModel.onEvent(DetailActivityEvent.OnSetCommentSending(it))
        }

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailActivityLayout(
    state: DetailActivityState,
    onPopBackStack: () -> Unit,
    onInputComment: (String) -> Unit,
    onSendComment: () -> Unit,
    onSetCommentSending: (Boolean) -> Unit
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
                            text = stringResource(id = R.string.activity_detail_caption),
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

        LazyColumn (
            modifier = Modifier.background(color = colorResource(id = R.color.background_apps))
        ){
            item {
                // carousel box
                Box(
                    modifier = Modifier
                        .padding(top = it.calculateTopPadding())
                        .height(218.dp)
                ) {
//                val routeImages = listOf(
//                    CarouselItem(0, R.drawable.route_image, "image 1"),
//                    CarouselItem(1, R.drawable.route_image, "image 2"),
//                    CarouselItem(2, R.drawable.route_image, "image 3")
//                )
//                CarouselLayout(items = routeImages)
                    Image(
                        painter = rememberAsyncImagePainter(state.trip?.thumbnail?.url ?: R.drawable.route_image),
                        contentDescription = "Activity image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

//                // Shape overlay
//                val trapezoidShape = GenericShape { size, _ ->
//                    moveTo(0f, 0f)
//                    lineTo(size.width * 0.8f, 0f)
//                    lineTo(size.width, size.height)
//                    lineTo(0f, size.height)
//                    close()
//                }
//
//                val context = LocalContext.current
//                val formattedPurpose = when (state.detail?.purpose?.lowercase()) {
//                    "exercise" -> context.getString(R.string.purpose_exercise)
//                    "recreation" -> context.getString(R.string.purpose_recreation)
//                    "touring" -> context.getString(R.string.purpose_touring)
//                    else -> state.detail?.purpose.orEmpty()
//                }
//
//                if (formattedPurpose.isNotEmpty()) {
//                    Box(
//                        contentAlignment = Alignment.Center,
//                        modifier = Modifier
//                            .offset(x = (-2).dp)
//                            .padding(bottom = 30.dp)
//                            .wrapContentSize()
//                            .background(
//                                colorResource(id = R.color.background_apps),
//                                shape = trapezoidShape
//                            )
//                            .border(width = 1.dp, color = Color.White, shape = trapezoidShape)
//                            .padding(horizontal = 8.dp, vertical = 6.dp)
//                            .padding(bottom = 2.dp)
//                            .padding(end = 14.dp)
//                            .align(Alignment.BottomStart)
//                    ) {
//                        Text(
//                            text = formattedPurpose,
//                            color = colorResource(id = R.color.text),
//                            fontSize = 18.sp,
//                            fontFamily = fontRns,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//                }
                }   // Close carousel box
            }

            item {

                Column(
                    modifier = Modifier
                        .background(colorResource(id = R.color.background_apps))
                        .fillMaxWidth()
                        .padding(
                            top = 16.dp,
                            start = it.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                            end = it.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                            bottom = it.calculateBottomPadding() + 8.dp
                        ),
                ) {

                    Text(
                        text = state.trip?.name ?: "Unknown route",
                        fontSize = 26.sp,
                        fontFamily = fontRns,
                        color = colorResource(id = R.color.text),
                        fontWeight = FontWeight.SemiBold,
                    )

//                state.detail?.startLocation?.takeIf { it.isNotBlank() }?.let { location ->
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            Icons.Outlined.LocationOn,
//                            contentDescription = "location icon",
//                            modifier = Modifier
//                                .size(16.dp)
//                        )
//                        Spacer(modifier = Modifier.width(4.dp))
//
//                        Text(
//                            text = location.toString(),
//                            fontSize = 16.sp,
//                            lineHeight = 1.sp,
//                            fontFamily = fontRns,
//                            color = colorResource(id = R.color.text),
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    }
//                }
                    Spacer(modifier = Modifier.height(32.dp))

                    Row {
                        // Column distance
                        Column(modifier = Modifier.weight(0.5f)) {
                            Text(
                                text = stringResource(id = R.string.label_distance),
                                fontSize = 16.sp,
                                lineHeight = 1.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.sub_text),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.distance),
                                    contentDescription = "distance icon",
                                    modifier = Modifier
                                        .size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "%.2f km",
                                        state.trip?.route?.distance ?: 0.0
                                    ),
                                    fontSize = 20.sp,
                                    lineHeight = 1.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Column elevation
                        Column(modifier = Modifier.weight(0.5f)) {
                            Text(
                                text = stringResource(id = R.string.label_elevation),
                                fontSize = 16.sp,
                                lineHeight = 1.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.sub_text),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.elevation),
                                    contentDescription = "elevation icon",
                                    modifier = Modifier
                                        .size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = state.trip?.route?.elevation.toString() + " m",
                                    fontSize = 20.sp,
                                    lineHeight = 1.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }   // close column elevation

                    }   // close row distance & elevation

//                // Bike and road type
//                state.detail?.roadType?.let { roadType ->
//                    Spacer(modifier = Modifier.height(16.dp))
//                    BikeRoadType(roadType)
//                }

                    Spacer(modifier = Modifier.height(40.dp))
                    // save, event, GPX
                    if (FeatureList.FeatureRoute) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.save),
                                    contentDescription = "save icon",
                                    modifier = Modifier
                                        .size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Save",
                                    fontSize = 16.sp,
                                    lineHeight = 1.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.event),
                                    contentDescription = "save icon",
                                    modifier = Modifier
                                        .size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Event",
                                    fontSize = 16.sp,
                                    lineHeight = 1.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.download),
                                    contentDescription = "download gpx icon",
                                    modifier = Modifier
                                        .size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "GPX",
                                    fontSize = 16.sp,
                                    lineHeight = 1.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }   // close row save, event, gpx

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    state.trip?.route?.roadType?.let { roadType ->
                        Spacer(modifier = Modifier.height(16.dp))
                        BikeRoadType(roadType)
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    // Map preview
                    MapPreview(
                        bounds = state.bounds,
                        routeWaypoint = state.routeWaypoint
                    )

                    HorizontalDivider(
                        modifier = Modifier
                            .height(1.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    // user statistic
                    Row {
                        // Column average speed
                        Column(modifier = Modifier.weight(0.5f)) {
                            Text(
                                text = stringResource(id = R.string.label_avg_speed),
                                fontSize = 16.sp,
                                lineHeight = 1.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.sub_text),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "%.2f",
                                        state.trip?.avgSpeed ?: 0.0
                                    ),
                                    fontSize = 26.sp,
                                    lineHeight = 1.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = " km/h",
                                    fontSize = 24.sp,
                                    lineHeight = 1.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Column max speed
                        Column(modifier = Modifier.weight(0.5f)) {
                            Text(
                                text = stringResource(id = R.string.label_max_speed),
                                fontSize = 16.sp,
                                lineHeight = 1.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.sub_text),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "%.2f",
                                        state.trip?.maxSpeed ?: 0.0
                                    ),
                                    fontSize = 26.sp,
                                    lineHeight = 1.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = " km/h",
                                    fontSize = 24.sp,
                                    lineHeight = 1.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                    }   // close row avg & max speed
                    Spacer(modifier = Modifier.height(24.dp))

                    Row {
                        // Column moving time
                        Column(modifier = Modifier.weight(0.5f)) {
                            Text(
                                text = stringResource(id = R.string.label_moving_time),
                                fontSize = 16.sp,
                                lineHeight = 1.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.sub_text),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = TimeUtil.formatSimpleTime(
                                        state.trip?.movingTime?.toInt() ?: 0
                                    ),
                                    fontSize = 26.sp,
                                    lineHeight = 1.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Column elapsed time
                        Column(modifier = Modifier.weight(0.5f)) {
                            Text(
                                text = stringResource(id = R.string.label_elapsed_time),
                                fontSize = 16.sp,
                                lineHeight = 1.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.sub_text),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = TimeUtil.formatSimpleTime(
                                        state.trip?.elapsedTime?.toInt() ?: 0
                                    ),
                                    fontSize = 26.sp,
                                    lineHeight = 1.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }   // close column elapsed time

                    }   // close row moving time & elapsed time
                    Spacer(modifier = Modifier.height(24.dp))

                    Row {
                        // Column bike
                        Column(modifier = Modifier.weight(0.5f)) {
                            Text(
                                text = stringResource(id = R.string.label_bike),
                                fontSize = 16.sp,
                                lineHeight = 1.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.sub_text),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            state.trip?.bikeName?.let { bikeName ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.PedalBike,
                                        contentDescription = "icon bike",
                                        tint = colorResource(id = R.color.text),
                                        modifier = Modifier
                                            .size(32.dp)
                                            .padding(end = 8.dp)
                                    )
                                    Text(
                                        text = bikeName,
                                        fontSize = 22.sp,
                                        lineHeight = 1.sp,
                                        fontFamily = fontRns,
                                        color = colorResource(id = R.color.text),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }   // close row bike
                    Spacer(modifier = Modifier.height(32.dp))

                    HorizontalDivider(
                        modifier = Modifier
                            .height(1.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    if (FeatureList.FeatureComment) {
                        Text(
                            text = "Comments (" + "${state.trip?.totalComments ?: "0"}" + ")",
                            fontFamily = fontRns,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = colorResource(id = R.color.text)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        CommentInputBox(
                            comment = state.comment,
                            isSendCommentEnabled = state.isSendCommentEnabled,
                            onInputComment = onInputComment,
                            onSendComment = onSendComment,
                            onSetCommentSending = onSetCommentSending
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                }   // main column
            }   // close item

            if (FeatureList.FeatureComment) {
                // comments
                val comments = state.trip?.comments.orEmpty()

                itemsIndexed(comments) { index, comment ->
                    comment?.let { detail ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                        ) {
                            Comment(detail)
                        }
                    }

                    if (index < comments.lastIndex) {
                        Spacer(modifier = Modifier.height(24.dp))

                        HorizontalDivider(
                            modifier = Modifier
                                .height(1.dp)
                                .padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }   // close lazy column
    }
}

@Composable
@Preview
fun DetailActivityPreview() {
    EbikeTheme {
        DetailActivityLayout(
            state = DetailActivityState(
                trip = TripBodyMiddleware(
//                    thumbnail = "https://example.com/image.jpg",
                    name = "Morning Ride",
                    route = RouteData(
                        distance = 25.40,
                        elevation = 300
                    ),
                    movingTime = 10,
                    elapsedTime = 1,
                    avgSpeed = 20.5,
                    maxSpeed = 35.0,
                    bikeName = "Strattos",
                )
            ),
            onPopBackStack = {},
            onInputComment = {},
            onSendComment = {},
            onSetCommentSending = {}
        )
    }
}