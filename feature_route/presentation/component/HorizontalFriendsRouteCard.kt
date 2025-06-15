package com.polygonbikes.ebike.v3.feature_route.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.component.loadingSkeleton
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_route.data.entities.response.RouteData
import java.util.Locale

@Composable
fun FriendsRouteCard(
    detail : RouteData,
    onClick: (RouteData) -> Unit,
    isFriendsRouteLoading: Boolean
) {
    //  Card friend's route
    Card(
        modifier = Modifier
            .width(275.dp)
            .wrapContentHeight()
            .clickable{ onClick(detail) }
            .loadingSkeleton(isFriendsRouteLoading)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.background_apps))
        ) {
            Image(
                painter = rememberAsyncImagePainter(detail.thumbnail?.url ?: R.drawable.route_image),
                contentDescription = "route image",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .height(165.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier
                .height(4.dp)
                .fillMaxWidth())

            Text(
                text = detail.name ?: "Unknown route",
                fontSize = 16.sp,
                fontFamily = fontRns,
                color = colorResource(id = R.color.text),
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier
                .height(4.dp)
                .fillMaxWidth())

            //  route detail row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = detail.startLocationData?.city?.toString() ?: "City not set",
                    fontSize = 14.sp,
                    fontFamily = fontRns,
                    lineHeight = 1.sp,
                    color = colorResource(id = R.color.text),
                    fontWeight = FontWeight.SemiBold
                )

                HorizontalDivider(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colorResource(id = R.color.sub_text))
                )

                //  distance
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.distance),
                        contentDescription = "distance icon"
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = detail.distance?.let {
                            "%.2f".format(Locale.US, it).trimEnd('0').trimEnd('.')
                        }?.plus(" km") ?: "- km",
                        fontSize = 14.sp,
                        lineHeight = 1.sp,
                        fontFamily = fontRns,
                        color = colorResource(id = R.color.text),
                        fontWeight = FontWeight.SemiBold
                    )
                }   // close distance

                HorizontalDivider(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colorResource(id = R.color.sub_text))
                )

                //  elevation
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.elevation),
                        contentDescription = "elevation icon"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${detail.elevation ?: "-"} m",
                        fontSize = 14.sp,
                        lineHeight = 1.sp,
                        fontFamily = fontRns,
                        color = colorResource(id = R.color.text),
                        fontWeight = FontWeight.SemiBold
                    )
                }   // close elevation

            }   // close route detail row

            Spacer(
                modifier = Modifier
                    .height(8.dp)
                    .fillMaxWidth()
            )

            //  Row road type
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
            }   //  close row road type

            Spacer(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth()
            )

            // Distance from current place & comment row
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
                        modifier = Modifier.size(12.dp)
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
        }   // close column card image
    } // close card
}

@Composable
@Preview
fun FriendsRouteCardPreview() {
    EbikeTheme {
        FriendsRouteCard(
            detail = RouteData(),
            isFriendsRouteLoading = false,
            onClick = { RouteData() }
        )
    }
}

@Composable
@Preview
fun FriendsRouteCardLoadingPreview() {
    EbikeTheme {
        FriendsRouteCard(
            detail = RouteData(),
            isFriendsRouteLoading = true,
            onClick = { RouteData() }
        )
    }
}