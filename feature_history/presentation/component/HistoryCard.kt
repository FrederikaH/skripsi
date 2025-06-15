package com.polygonbikes.ebike.v3.feature_history.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.component.loadingSkeleton
import com.polygonbikes.ebike.core.util.TimeUtil
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_event.presentation.component.DateTag
import com.polygonbikes.ebike.v3.feature_trip.data.entities.TripBodyMiddleware
import java.util.Locale

@Composable
fun HistoryCard(
    detail: TripBodyMiddleware,
    onClick: (TripBodyMiddleware) -> Unit,
    isHistoryLoading: Boolean,
) {
    HistoryCardLayout(
        detail = detail,
        onClick = { onClick(detail) },
        isHistoryLoading = isHistoryLoading
    )
}

@Composable
fun HistoryCardLayout(
    detail: TripBodyMiddleware,
    onClick: () -> Unit,
    isHistoryLoading: Boolean
) {

    //  recommended route card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(122.dp)
            .background(colorResource(id = R.color.secondary))
            .clickable { onClick() }
            .loadingSkeleton(isHistoryLoading)
    ) {
        //  divide image and content
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            //  box image
            Box(
                modifier = Modifier
                    .weight(0.4f)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(detail.thumbnail?.url ?: R.drawable.route_image),
                    contentDescription = "trip image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .height(165.dp),
                    contentScale = ContentScale.Crop
                )

//                //  row location
//                Row(
//                    modifier = Modifier
//                        .wrapContentSize()
//                        .padding(end = 8.dp, bottom = 8.dp)
//                        .clip(RoundedCornerShape(4.dp))
//                        .background(colorResource(id = R.color.darkest_gray))
//                        .padding(top = 4.dp, bottom = 4.dp, start = 6.dp, end = 6.dp)
//                        .align(Alignment.BottomEnd),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        Icons.Default.LocationOn,
//                        contentDescription = "Location icon",
//                        modifier = Modifier
//                            .size(12.dp)
//                    )
//                    Spacer(modifier = Modifier.width(4.dp))
//
//                    Text(
//                        text = "Sidoarjo",
//                        fontSize = 14.sp,
//                        lineHeight = 1.sp,
//                        fontFamily = fontRns,
//                        fontWeight = FontWeight.SemiBold,
//                        color = colorResource(id = R.color.text)
//                    )
//                }   // close row location

                //  date tag
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 8.dp, top = 8.dp)
                ) {
                    DateTag(detail.date)
                }

            }   // close box image

            // column content
            Column(
                modifier = Modifier
                    .weight(0.7f)
                    .padding(10.dp)
                    .padding(start = 4.dp)
            ) {
                Text(
                    text = detail.name.toString(),
                    fontSize = 18.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 1.sp,
                    color = colorResource(id = R.color.text),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

                Spacer(
                    modifier = Modifier
                        .height(4.dp)
                        .fillMaxWidth()
                )

                Column {

                    // row distance
                    Row (verticalAlignment = Alignment.CenterVertically){
                        Image(
                            painter = painterResource(id = R.drawable.distance),
                            contentDescription = "distance icon"
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            buildAnnotatedString {
                                withStyle(style = SpanStyle(
                                    fontSize = 14.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Bold
                                )) {
                                    append(String.format(Locale.getDefault(), "%.2f", detail.route?.distance ?: 0f))
                                }

                                withStyle(style = SpanStyle(
                                    fontSize = 14.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Medium
                                )) {
                                    append(" km")
                                }
                            }
                        )
                    }   // close row distance

                    // row speed
                    Row (verticalAlignment = Alignment.CenterVertically){
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = "speed icon",
                            Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            buildAnnotatedString {
                                withStyle(style = SpanStyle(
                                    fontSize = 14.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Bold
                                )) {
                                    append(String.format(Locale.getDefault(), "%.1f", detail.avgSpeed))
                                }
                                withStyle(style = SpanStyle(
                                    fontSize = 14.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Medium
                                )) {
                                    append(" km/h")
                                }
                            }
                        )
                    }   // close row speed

                    // row duration
                    Row (verticalAlignment = Alignment.CenterVertically){

                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "duration icon",
                            Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = TimeUtil.formatSimpleTime(detail.elapsedTime?.toInt() ?: 0),
                            fontSize = 14.sp,
                            fontFamily = fontRns,
                            color = colorResource(id = R.color.text),
                            fontWeight = FontWeight.Bold
                        )
                    }   // close row duration

                }

            }   // close column content
        }
    }   // close card

}


@Composable
@Preview
fun HistoryCardPreview() {
    EbikeTheme {
        HistoryCardLayout(
            detail = TripBodyMiddleware(
                id = 2,
                name = "Evening Commute",
                date = "2024-03-11",
                avgSpeed = 18.2,
                bikeName = "Urban Cruiser"
            ),
            onClick = {},
            isHistoryLoading = false
        )
    }
}