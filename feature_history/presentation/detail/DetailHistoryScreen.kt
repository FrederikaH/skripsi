package com.polygonbikes.ebike.v3.feature_history.presentation.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.rememberCameraPositionState
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.FeatureList
import com.polygonbikes.ebike.core.component.BikeRoadType
import com.polygonbikes.ebike.core.component.CommentInputBox
import com.polygonbikes.ebike.core.component.MapPreview
import com.polygonbikes.ebike.core.entities.FileEntity
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.core.util.TimeUtil
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_history.domain.state.DetailHistoryState
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.Photo
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ProfileData
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.v3.feature_route.presentation.component.Comment
import com.polygonbikes.ebike.v3.feature_trip.data.entities.Strava
import com.polygonbikes.ebike.v3.feature_trip.data.entities.TripBodyMiddleware
import java.util.Locale

@Composable
fun DetailHistoryScreen(
    onPopBackStack: () -> Unit,
    onNavigate: (UiEvent.Navigate) -> Unit,
    trip: TripBodyMiddleware,
    viewModel: DetailHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state

    val stateBounds by viewModel.state.value.bounds.collectAsState()

    LaunchedEffect(key1 = viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onNavigate(event)
                else -> Unit
            }
        }
    }

    LaunchedEffect(key1 = viewModel) {
        viewModel.onEvent(DetailHistoryEvent.GetData(trip))
    }

    LaunchedEffect(true) {
        viewModel.onEvent(DetailHistoryEvent.InitScreen)
    }

    DetailHistoryLayout(
        state = state,
        stateBounds = stateBounds,
        onPopBackStack = onPopBackStack,
        onShareToStrava = {
            viewModel.onEvent(DetailHistoryEvent.ShareToStrava)
        },

        onOpenConnectStrava = {
            viewModel.onEvent(DetailHistoryEvent.OpenConnectStrava)
        },

        onInputComment = {
            viewModel.onEvent(DetailHistoryEvent.OnInputComment(it))
        },

        onSendComment = {
            viewModel.onEvent(DetailHistoryEvent.SendComment)
        },

        onSetCommentSending = {
            viewModel.onEvent(DetailHistoryEvent.OnSetCommentSending(it))
        }
    )
}

@Composable
fun DeleteButton() {
    val openAlertDialog = remember { mutableStateOf(false) }

    IconButton(onClick = { openAlertDialog.value = true }) {
        Icon(
            Icons.Outlined.Delete,
            contentDescription = "delete icon"
        )
    }

    if (openAlertDialog.value) {
        AlertDialog(
            onDismissRequest = { openAlertDialog.value = false },
            shape = RoundedCornerShape(4.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(id = R.drawable.warning),
                        tint = colorResource(id = R.color.red_accent),
                        contentDescription = "delete icon"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Discard Trip", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    BasicText(
                        text = AnnotatedString.Builder().apply {
                            append("Are you sure you want to ")
                            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                            append("discard")
                            pop()
                            append(" this ")
                            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                            append("trip?")
                            pop()
                        }.toAnnotatedString(),

                        style = TextStyle(
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = colorResource(id = R.color.text)
                        )

                    )

                }
            },

            confirmButton = {
                Button(
                    onClick = { openAlertDialog.value = false },
                    modifier = Modifier
                        .defaultMinSize(
                            minWidth = 4.dp,
                            minHeight = 6.dp
                        ),
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        bottom = 8.dp,
                        top = 6.dp
                    ),
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = colorResource(id = R.color.red_accent)
                    ),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(
                        text = "Discard",
                        fontFamily = fontRns,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorResource(id = R.color.text)
                    )
                }
            },

            dismissButton = {
                TextButton(onClick = { openAlertDialog.value = false }) {
                    Text(
                        text = "Cancel",
                        fontFamily = fontRns,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorResource(id = R.color.text)
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailHistoryLayout(
    state: DetailHistoryState,
    stateBounds: LatLngBounds?,
    onPopBackStack: () -> Unit,
    onShareToStrava: () -> Unit,
    onOpenConnectStrava: () -> Unit,
    onInputComment: (String) -> Unit,
    onSendComment: () -> Unit,
    onSetCommentSending: (Boolean) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(key1 = stateBounds) {
        stateBounds?.let { bounds ->
            val target = CameraUpdateFactory.newLatLngBounds(bounds, 36)
            cameraPositionState.move(target)
        }
    }

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
                            text = stringResource(id = R.string.trip_detail_caption),
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
//                actions = {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxHeight()
//                            .wrapContentSize(align = Alignment.Center)
//                    ){
//                        // Edit button
//                        IconButton(onClick = { /* masuk ke page edit */ }) {
//                            Icon(
//                                imageVector = Icons.Outlined.Edit,
//                                contentDescription = "edit icon"
//                            )
//                        }
//                        // Delete button
//                        DeleteButton()
//                    }
//                },
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = colorResource(id = R.color.black)
                )
            )
        },
    ) {

        LazyColumn {
            item {
                // carousel box
                Box(
                    modifier = Modifier
                        .padding(top = it.calculateTopPadding())
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
//                val  CarouselItem(0, R.drawable.route_image, "image 1"),
//                    CarouselItem(1, R.drawable.route_image, "image 2"),
//                    CarouselItem(2, R.drawable.route_image, "image 3")
//                )
//                CarouselLayout(items = routeImages)

                    Image(
                        painter = rememberAsyncImagePainter(state.trip?.thumbnail?.url),
                        contentDescription = "Activity image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Shape overlay
                    val trapezoidShape = GenericShape { size, _ ->
                        moveTo(0f, 0f)
                        lineTo(size.width * 0.8f, 0f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }

//                val context = LocalContext.current
//                val formattedPurpose = when (state.trip?.purpose?.lowercase()) {
//                    "exercise" -> context.getString(R.string.purpose_exercise)
//                    "recreation" -> context.getString(R.string.purpose_recreation)
//                    "touring" -> context.getString(R.string.purpose_touring)
//                    else -> state.trip?.purpose.orEmpty()
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
                        text = state.trip?.name ?: "-",
                        fontSize = 26.sp,
                        fontFamily = fontRns,
                        color = colorResource(id = R.color.text),
                        fontWeight = FontWeight.SemiBold,
                    )
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Row(
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = state.trip.startLocation,
//                        fontSize = 16.sp,
//                        lineHeight = 1.sp,
//                        fontFamily = fontRns,
//                        color = colorResource(id = R.color.text),
//                        fontWeight = FontWeight.SemiBold
//                    )
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
                                Spacer(modifier = Modifier.width(6.dp))

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
                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "%d m",
                                        state.trip?.route?.elevation ?: 0
                                    ),
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
//                state.trip?.roadType?.let { roadType ->
//                    Spacer(modifier = Modifier.height(16.dp))
//                    BikeRoadType(roadType)
//                }

//                Spacer(modifier = Modifier.height(40.dp))

                    // save, event, GPX
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceAround
//                ) {
//                    Column(
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.save),
//                            contentDescription = "save icon",
//                            modifier = Modifier
//                                .size(20.dp)
//                        )
//                        Spacer(modifier = Modifier.height(6.dp))
//
//                        Text(
//                            text = "Save",
//                            fontSize = 16.sp,
//                            lineHeight = 1.sp,
//                            fontFamily = fontRns,
//                            color = colorResource(id = R.color.text),
//                            fontWeight = FontWeight.SemiBold
//                        )
//                        Text(
//                            text = "0",
//                            fontSize = 16.sp,
//                            lineHeight = 1.sp,
//                            fontFamily = fontRns,
//                            color = colorResource(id = R.color.text),
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    }
//
//                    Column(
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.event),
//                            contentDescription = "save icon",
//                            modifier = Modifier
//                                .size(20.dp)
//                        )
//                        Spacer(modifier = Modifier.height(6.dp))
//
//                        Text(
//                            text = "Event",
//                            fontSize = 16.sp,
//                            lineHeight = 1.sp,
//                            fontFamily = fontRns,
//                            color = colorResource(id = R.color.text),
//                            fontWeight = FontWeight.SemiBold
//                        )
//                        Text(
//                            text = "0",
//                            fontSize = 16.sp,
//                            lineHeight = 1.sp,
//                            fontFamily = fontRns,
//                            color = colorResource(id = R.color.text),
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    }
//
//                    Column(
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.download),
//                            contentDescription = "download gpx icon",
//                            modifier = Modifier
//                                .size(20.dp)
//                        )
//                        Spacer(modifier = Modifier.height(6.dp))
//
//                        Text(
//                            text = "GPX",
//                            fontSize = 16.sp,
//                            lineHeight = 1.sp,
//                            fontFamily = fontRns,
//                            color = colorResource(id = R.color.text),
//                            fontWeight = FontWeight.SemiBold
//                        )
//                        Text(
//                            text = "0",
//                            fontSize = 16.sp,
//                            lineHeight = 1.sp,
//                            fontFamily = fontRns,
//                            color = colorResource(id = R.color.text),
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    }
//                }
                    // close row save, event, gpx

                    Spacer(modifier = Modifier.height(16.dp))

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

                    Button(
                        onClick = if (!state.connectedToStrava) onOpenConnectStrava else onShareToStrava,
                        enabled = !state.connectedToStrava || !(state.alreadyShareToStrava || state.queueUploadStrava),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors().copy(
                            containerColor = colorResource(id = R.color.international_orange_aerospace)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .wrapContentWidth(align = Alignment.CenterHorizontally),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!state.connectedToStrava)
                                Image(
                                    painter = painterResource(id = R.drawable.strava_icon),
                                    contentDescription = "logo strava",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            else if (state.alreadyShareToStrava)
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "icon check",
                                    modifier = Modifier.size(20.dp)
                                )
                            else if (state.queueUploadStrava)
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = "icon access time",
                                    modifier = Modifier.size(20.dp)
                                )
                            else
                                Image(
                                    painter = painterResource(id = R.drawable.strava_icon),
                                    contentDescription = "logo strava",
                                    modifier = Modifier.padding(end = 8.dp)
                                )

                            Text(
                                text = if (!state.connectedToStrava)
                                    stringResource(id = R.string.label_button_connect_to_strava)
                                else if (state.alreadyShareToStrava)
                                    stringResource(id = R.string.label_button_already_shared_to_strava)
                                else if (state.queueUploadStrava)
                                    stringResource(id = R.string.label_button_queue_upload_strava)
                                else
                                    stringResource(id = R.string.label_button_share_to_strava),
                                fontSize = 14.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.Medium,
                                color = colorResource(
                                    id = R.color.white
                                ),
                                lineHeight = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
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
                                        Locale.getDefault(),
                                        "%.1f",
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
                                        Locale.getDefault(),
                                        "%.1f",
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
                                    text = state.trip?.bikeName ?: "-",
                                    fontSize = 26.sp,
                                    lineHeight = 1.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Bold
                                )
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
                            text = "Comments (" + "${state.trip?.totalComments}" + ")",
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

                } // close column
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
                    Spacer(
                        modifier = Modifier
                            .height(40.dp)
                            .fillMaxWidth()
                            .background(color = colorResource(id = R.color.background_apps))
                    )
                }
            }

        }   // close lazy column
    }
}

@Composable
@Preview
fun DetailHistoryPreview() {
    EbikeTheme {
        DetailHistoryLayout(
            state = DetailHistoryState(
                trip = TripBodyMiddleware(
                    name = "Sepedaan pagi",
                    route = RouteData(
                        distance = 19.2,
                        elevation = 120
                    ),
                    id = 100,
                    timestamp = 1744612044796,
                    user = ProfileData(
                        userId = 1,
                        username = "Ahmadfaris",
                        email = "ahmadfarish82@gmail.com",
                        mobilePhone = "+6285210363230",
                        cyclingStyle = listOf("gravel"),
                        city = LocationData(
                            id = 1,
                            city = "Sidoarjo"
                        ),
                        photo = Photo(
                            url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/images/1-f53f660d-c1e3-4937-9b84-55a36b8b8754.jpg"
                        )
                    ),
                    date = "2025-04-14 06:28:15",
                    movingTime = 9,
                    elapsedTime = 17,
                    avgSpeed = 6.6,
                    maxSpeed = 8.4,
                    bikeName = "budi",
                    thumbnail = FileEntity(
                        url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/images/41-4b6c4ab0-d1a8-4c3c-992a-7b2ef5849046.jpg",
                        type = "image"
                    ),
                    strava = Strava()
                )
            ),
            stateBounds = null,
            onPopBackStack = {},
            onShareToStrava = {},
            onOpenConnectStrava = {},
            onInputComment = {},
            onSendComment = {},
            onSetCommentSending = {},
        )
    }
}