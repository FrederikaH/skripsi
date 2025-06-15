package com.polygonbikes.ebike.v3.feature_route.presentation.route_management

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.component.LocationTag
import com.polygonbikes.ebike.core.component.loadingSkeleton
import com.polygonbikes.ebike.core.entities.FileEntity
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData

@Composable
fun RouteManagementCard(
    detail: RouteData,
    onOpenDetailRoute: (RouteData) -> Unit,
    isDeleted: Boolean? = false,
    onActionRoute: (Int) -> Unit,
    isRouteLoading: Boolean
) {
    RouteManagementCardLayout(
        detail = detail,
        onOpenDetailRoute = { onOpenDetailRoute(detail) },
        isDeleted = isDeleted,
        onActionRoute = { onActionRoute(detail.id) },
        isRouteLoading = isRouteLoading
    )
}

@Composable
fun RouteManagementCardLayout(
    detail: RouteData,
    onOpenDetailRoute: () -> Unit,
    isDeleted: Boolean? = false,
    onActionRoute: () -> Unit,
    isRouteLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .loadingSkeleton(isRouteLoading),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.secondary)
        ),
        onClick = onOpenDetailRoute
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Box image
            Box(
                modifier = Modifier.weight(0.4f)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = detail.thumbnail?.url ?: R.drawable.route_image
                    ),
                    contentDescription = "route image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(165.dp),
                    contentScale = ContentScale.Crop
                )

                if (isDeleted == false) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 8.dp, top = 8.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(colorResource(id = R.color.red_accent))
                            .padding(4.dp)
                            .clickable{ onActionRoute() }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "location",
                            modifier = Modifier
                                .size(18.dp),
                            tint = colorResource(id = R.color.text)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 8.dp, top = 8.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(colorResource(id = R.color.background_apps))
                            .padding(4.dp)
                            .clickable{ onActionRoute() }
                    ) {
                        Icon(
                            Icons.Default.SettingsBackupRestore,
                            contentDescription = "location",
                            modifier = Modifier
                                .size(18.dp),
                            tint = colorResource(id = R.color.text)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 8.dp, bottom = 8.dp)
                ) {
                    LocationTag(detail.startLocationData?.city.toString())
                }
            } // close box image

            // Column content
            Column(
                modifier = Modifier
                    .weight(0.7f)
                    .padding(10.dp)
                    .padding(start = 4.dp)
            ) {
                Text(
                    text = detail.name.toString(),
                    fontSize = 16.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Distance & Elevation Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.distance),
                        contentDescription = "distance icon"
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "%.2f".format(detail.distance)
                            .trimEnd('0')
                            .trimEnd('.') + " km",
                        fontSize = 14.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(id = R.color.text)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    VerticalDivider(
                        modifier = Modifier
                            .width(1.5.dp)
                            .background(colorResource(id = R.color.sub_text))
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Image(
                        painter = painterResource(id = R.drawable.elevation),
                        contentDescription = "elevation icon"
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "${detail.elevation} m",
                        fontSize = 14.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(id = R.color.text)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val context = LocalContext.current

                val orderedRoadTypes = listOf("paved", "gravel", "off")

                val roadTypes = detail.roadType
                    ?.mapNotNull { roadType ->
                        when {
                            roadType.contains(
                                "paved",
                                ignoreCase = true
                            ) -> context.getString(R.string.road_type_paved)

                            roadType.contains(
                                "gravel",
                                ignoreCase = true
                            ) -> context.getString(R.string.road_type_gravel)

                            roadType.contains(
                                "off",
                                ignoreCase = true
                            ) -> context.getString(R.string.road_type_offroad)

                            else -> null
                        }
                    }
                    ?.distinct()
                    ?.sortedBy { type ->
                        orderedRoadTypes.indexOfFirst { order ->
                            type.contains(
                                order,
                                ignoreCase = true
                            )
                        }
                    }

                // Row road type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (roadTypes.isNullOrEmpty()) {
                        Text(
                            text = "Road type not set",
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
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(colorResource(id = R.color.badge_background))
                                    .padding(start = 4.dp, end = 4.dp, top = 1.dp, bottom = 2.dp)
                            )
                        }
                    }
                } // close row road type

                Row(
                    modifier = Modifier
                        .fillMaxWidth()

                ) {
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(4.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        Icons.Default.NearMe,
//                        contentDescription = "distance to route icon",
//                        modifier = Modifier.size(12.dp)
//                    )
//                    Text(
//                        text = "124" + " km away",
//                        fontFamily = fontRns,
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        color = colorResource(id = R.color.text)
//                    )
//                }
//
                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Forum,
                            contentDescription = "distance to route icon",
                            modifier = Modifier.size(12.dp),
                            tint = colorResource(id = R.color.text)
                        )
                        Text(
                            text = "${detail.totalComments ?: 0}",
                            fontFamily = fontRns,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text),
                        )
                    }
                }   // distance & comment row


            } // close column content
        }
    } // close card
}

@Composable
@Preview
fun RouteManagementCardPreview() {
    EbikeTheme {
        RouteManagementCardLayout(
            detail = RouteData(
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
                roadType = listOf("gravel", "paved"),
                distance = 2.4,
                elevation = 24,
                purpose = "exercise",
                polyline = "vdhl@}r`oTCFAHAFAFCHAFAFCHAFAFAHCFAHAFAFCHAFAFCHAHADAHCFAFAHCFAHAFAFCHAF?@AHCF",
                gpx = FileEntity(
                    url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/gpx/8-bae85d62-4398-4a13-ab00-a798b70645d6.gpx",
                    type = "gpx"
                )
            ),
            onOpenDetailRoute = {},
            isDeleted = true,
            isRouteLoading = false,
            onActionRoute = {}
        )
    }
}


@Composable
@Preview
fun RouteManagementCardLoadingPreview() {
    EbikeTheme {
        RouteManagementCardLayout(
            detail = RouteData(
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
                roadType = listOf("gravel", "paved"),
                distance = 2.4,
                elevation = 24,
                purpose = "exercise",
                polyline = "vdhl@}r`oTCFAHAFAFCHAFAFCHAFAFAHCFAHAFAFCHAFAFCHAHADAHCFAFAHCFAHAFAFCHAF?@AHCF",
                gpx = FileEntity(
                    url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/gpx/8-bae85d62-4398-4a13-ab00-a798b70645d6.gpx",
                    type = "gpx"
                )
            ),
            onOpenDetailRoute = {},
            isDeleted = false,
            isRouteLoading = true,
            onActionRoute = {}
        )
    }
}
