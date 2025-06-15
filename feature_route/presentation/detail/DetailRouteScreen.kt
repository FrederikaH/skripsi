package com.polygonbikes.ebike.v3.feature_route.presentation.detail

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.polygonbikes.ebike.core.component.CommentInputBox
import com.polygonbikes.ebike.core.FeatureList
import com.polygonbikes.ebike.core.component.MapPreview
import com.polygonbikes.ebike.core.entities.FileEntity
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import com.polygonbikes.ebike.v3.feature_route.domain.state.DetailRouteState
import com.polygonbikes.ebike.v3.feature_route.presentation.component.Comment
import com.polygonbikes.ebike.v3.feature_route.presentation.component.SaveRouteDialog
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.collections.lastIndex
import kotlin.collections.orEmpty

@Composable
fun DetailRouteScreen(
    onPopBackStack: () -> Unit,
    onNavigate: (UiEvent.Navigate) -> Unit,
    route: RouteData,
    viewModel: DetailRouteViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    val snackbarScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDialog by remember { mutableStateOf(value = false) }

    LaunchedEffect(key1 = viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onNavigate(event)
                else -> Unit
            }
        }
    }

    LaunchedEffect(key1 = viewModel) {
        viewModel.onEvent(DetailRouteEvent.GetData(route))
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackBar -> {
                    snackbarScope.launch {
                        val resSnackBar = snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = event.duration,
                            actionLabel = event.actionLabel
                        )
                        when (resSnackBar) {
                            SnackbarResult.ActionPerformed -> {
                                event.onAction?.invoke()
                            }

                            SnackbarResult.Dismissed -> {}
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    DetailRouteLayout(
        state = state,
        onPopBackStack = onPopBackStack,
        snackbarHostState = snackbarHostState,
        onDownloadGPX = {
            viewModel.onEvent(DetailRouteEvent.DownloadGPX)
        },

        onSaveRoute = { name ->
            viewModel.onEvent(DetailRouteEvent.SaveRoute(name))
        },

        onDismissDialog = {
            showDialog = false
        },

        onShowSaveRouteDialog = {
            viewModel.onEvent(DetailRouteEvent.SaveRouteDialog)
        },

        onOpenCreateEventScreen = { route ->
            viewModel.onEvent(DetailRouteEvent.OpenCreateEventScreen(route))
        },

        onInputComment = {
            viewModel.onEvent(DetailRouteEvent.OnInputComment(it))
        },

        onSendComment = {
            viewModel.onEvent(DetailRouteEvent.SendComment)
        },

        onSetCommentSending = {
            viewModel.onEvent(DetailRouteEvent.OnSetCommentSending(it))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailRouteLayout(
    state: DetailRouteState,
    snackbarHostState: SnackbarHostState,
    onPopBackStack: () -> Unit,
    onDownloadGPX: () -> Unit,
    onSaveRoute: (String) -> Unit,
    onDismissDialog: () -> Unit,
    onShowSaveRouteDialog: () -> Unit,
    onOpenCreateEventScreen: (RouteData) -> Unit,
    onInputComment: (String) -> Unit,
    onSendComment: () -> Unit,
    onSetCommentSending: (Boolean) -> Unit
//    isSaved: Boolean
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
                            text = stringResource(id = R.string.route_detail_caption),
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
        if (state.showDialog) {
            SaveRouteDialog(
                title = "Save route as",
                onDismiss = { onShowSaveRouteDialog() },
                onConfirm = { routeName ->
                    onSaveRoute(routeName)
                }
            )
        }

        LazyColumn {
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
                        painter = rememberAsyncImagePainter(state.route?.thumbnail?.url),
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

                    val context = LocalContext.current
                    val formattedPurpose = when (state.route?.purpose?.lowercase()) {
                        "exercise" -> context.getString(R.string.purpose_exercise)
                        "recreation" -> context.getString(R.string.purpose_recreation)
                        "touring" -> context.getString(R.string.purpose_touring)
                        else -> state.route?.purpose.orEmpty()
                    }

                    if (formattedPurpose.isNotEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .offset(x = (-2).dp)
                                .padding(bottom = 30.dp)
                                .wrapContentSize()
                                .background(
                                    colorResource(id = R.color.background_apps),
                                    shape = trapezoidShape
                                )
                                .border(width = 1.dp, color = Color.White, shape = trapezoidShape)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .padding(bottom = 2.dp)
                                .padding(end = 14.dp)
                                .align(Alignment.BottomStart)
                        ) {
                            Text(
                                text = formattedPurpose,
                                color = colorResource(id = R.color.text),
                                fontSize = 18.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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
                        text = state.route?.name ?: "Unknown route",
                        fontSize = 26.sp,
                        fontFamily = fontRns,
                        color = colorResource(id = R.color.text),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = "location icon",
                            modifier = Modifier
                                .size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = state.route?.startLocationData?.city ?: "City not set",
                            fontSize = 16.sp,
                            lineHeight = 1.sp,
                            fontFamily = fontRns,
                            color = colorResource(id = R.color.text),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    Row {
                        // Column distance
                        Column(modifier = Modifier.weight(0.5f)) {
                            Text(
                                text = stringResource(id = R.string.label_distance),
                                fontSize = 14.sp,
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
                                    text = state.route?.distance?.let {
                                        "%.2f".format(Locale.US, it).trimEnd('0').trimEnd('.')
                                    }?.plus(" km") ?: "- km",
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
                                    text = String.format(state.route?.elevation.toString()) + " m",
                                    fontSize = 20.sp,
                                    lineHeight = 1.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }   // close column elevation

                    }   // close row distance & elevation
                    Spacer(modifier = Modifier.height(16.dp))

                    // Bike and road type
                    Column {
                        Text(
                            text = stringResource(id = R.string.label_bike_road_type),
                            fontSize = 16.sp,
                            lineHeight = 1.sp,
                            fontFamily = fontRns,
                            color = colorResource(id = R.color.sub_text),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val context = LocalContext.current

                            // Define road type mappings
                            val roadTypeMap = mapOf(
                                "paved" to context.getString(R.string.road_type_paved),
                                "gravel" to context.getString(R.string.road_type_gravel),
                                "off" to context.getString(R.string.road_type_offroad)
                            )

                            // Define the correct order
                            val roadTypesOrder = listOf(
                                context.getString(R.string.road_type_paved),
                                context.getString(R.string.road_type_gravel),
                                context.getString(R.string.road_type_offroad)
                            )

                            // Map and sort road types
                            val roadTypes = state.route?.roadType
                                ?.map { roadType ->
                                    roadTypeMap.entries.find {
                                        roadType.contains(
                                            it.key,
                                            ignoreCase = true
                                        )
                                    }?.value ?: roadType
                                }
                                ?.sortedBy {
                                    roadTypesOrder.indexOf(it).takeIf { it != -1 } ?: Int.MAX_VALUE
                                }

                            val imageResource = when {
                                roadTypes?.any {
                                    it.contains(
                                        "off",
                                        ignoreCase = true
                                    )
                                } == true -> R.drawable.offroad

                                roadTypes?.any {
                                    it.contains(
                                        "gravel",
                                        ignoreCase = true
                                    )
                                } == true -> R.drawable.gravel

                                roadTypes?.any { it == "paved" || it == "road" } == true -> R.drawable.road
                                else -> R.drawable.road
                            }

                            Image(
                                painter = painterResource(id = imageResource),
                                contentDescription = "road type icon",
                                modifier = Modifier.size(20.dp)
                            )

                            if (roadTypes.isNullOrEmpty()) {
                                Text(
                                    text = "Not set",
                                    fontSize = 14.sp,
                                    lineHeight = 1.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Bold,
                                )
                            } else {
                                roadTypes.forEach { roadType ->
                                    Text(
                                        text = roadType,
                                        fontSize = 14.sp,
                                        lineHeight = 1.sp,
                                        fontFamily = fontRns,
                                        color = colorResource(id = R.color.text),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(colorResource(id = R.color.badge_background))
                                            .padding(
                                                start = 6.dp,
                                                end = 6.dp,
                                                top = 2.dp,
                                                bottom = 4.dp
                                            )
                                    )
                                }
                            }

                        }
                    }   // close column bike and road type
                    Spacer(modifier = Modifier.height(40.dp))

                    // save, event, GPX
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                onShowSaveRouteDialog()
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.save),
                                contentDescription = "save icon",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(id = R.string.label_button_save),
                                fontSize = 16.sp,
                                lineHeight = 16.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.text),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                state.route?.let { route -> onOpenCreateEventScreen(route) }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.event),
                                contentDescription = "event icon",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(id = R.string.route_feature_event_label),
                                fontSize = 16.sp,
                                lineHeight = 16.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.text),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onDownloadGPX() }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.download),
                                contentDescription = "download gpx icon",
                                modifier = Modifier
                                    .size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = stringResource(id = R.string.route_feature_download_gpx_label),
                                fontSize = 16.sp,
                                lineHeight = 1.sp,
                                fontFamily = fontRns,
                                color = colorResource(id = R.color.text),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Map preview
                    MapPreview(
                        bounds = state.bounds,
                        routeWaypoint = state.routeWaypoint
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    HorizontalDivider(
                        modifier = Modifier
                            .height(1.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    if (FeatureList.FeatureComment) {
                        Text(
                            text = "Comments (" + "${state.route?.totalComments ?: "0"}" + ")",
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
                val comments = state.route?.comments.orEmpty()

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
fun DetailRoutePreview() {
    EbikeTheme {
        DetailRouteLayout(
            onPopBackStack = {},
            state = DetailRouteState(
                route = RouteData(
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
//                    roadType = listOf("gravel", "road"),
                    distance = 2.4,
                    elevation = 24,
                    purpose = "exercise",
                    polyline = "vdhl@}r`oTCFAHAFAFCHAFAFCHAFAFAHCFAHAFAFCHAFAFCHAHADAHCFAFAHCFAHAFAFCHAF?@AHCF",
                    gpx = FileEntity(
                        url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/gpx/8-bae85d62-4398-4a13-ab00-a798b70645d6.gpx",
                        type = "gpx"
                    )
                )
            ),
            onDownloadGPX = {},
            snackbarHostState = remember { SnackbarHostState() },
            onSaveRoute = {},
            onDismissDialog = {},
            onShowSaveRouteDialog = {},
            onOpenCreateEventScreen = {},
            onInputComment = {},
            onSendComment = {},
            onSetCommentSending = {}
        )
    }
}