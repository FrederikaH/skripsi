package com.polygonbikes.ebike.v3.feature_profile.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.component.loadingSkeleton
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.core.model.Thumbnail
import com.polygonbikes.ebike.core.util.TimeUtil
import com.polygonbikes.ebike.core.util.TimeUtil.FormatDayMonth
import com.polygonbikes.ebike.core.util.TimeUtil.FormatSimpleDate
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_event.data.entities.EventData
import com.polygonbikes.ebike.core.model.Images
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.Photo
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ProfileData

@Composable
fun UpcomingEventCard(
    detail: EventData,
    onClick: (EventData) -> Unit,
    isUpcomingEventLoading: Boolean
) {
    UpcomingEventCardLayout(
        detail = detail,
        onClick = {onClick(detail)},
        isUpcomingEventLoading = isUpcomingEventLoading
    )
}

@Composable
fun UpcomingEventCardLayout(
    detail: EventData,
    onClick: () -> Unit,
    isUpcomingEventLoading: Boolean
) {
    Card(
        modifier = Modifier
            .width(320.dp)
            .height(120.dp)
            .background(colorResource(id = R.color.secondary))
            .clickable { onClick() }
            .loadingSkeleton(isUpcomingEventLoading)
    ) {
        //  divide image and content
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            //  box image
            Box(
                modifier = Modifier
                    .weight(0.35f)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(detail.thumbnail?.url),
                    contentDescription = "route image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(165.dp),
                    contentScale = ContentScale.Crop
                )
            }   // close box image

            // Box content
            Box(
                modifier = Modifier
                    .weight(0.65f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                ) {
                    Text(
                        text = detail.name.toString(),
                        fontSize = 18.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.text)
                    )

                    Spacer(
                        modifier = Modifier
                            .height(6.dp)
                            .fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.EventNote,
                            contentDescription = "Schedule icon",
                            modifier = Modifier
                                .size(16.dp),
                            tint = colorResource(id = R.color.sub_text)
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = TimeUtil.getFormattedDate(detail.startAt.toString(), FormatSimpleDate, FormatDayMonth),
                            fontSize = 16.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.sub_text)
                        )
                    }
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = "Location icon",
                            modifier = Modifier
                                .size(16.dp),
                            tint = colorResource(id = R.color.sub_text)
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = detail.locationData?.city.toString(),
                            fontSize = 16.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.sub_text)
                        )
                    }
                }   // close column content

                // Shape overlay
                val trapezoidShape = GenericShape { size, _ ->
                    moveTo(size.width, 0f)
                    lineTo(size.width * 0.1f, 0f)
                    lineTo(0f, size.height)
                    lineTo(size.width, size.height)
                    close()
                }


                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .wrapContentSize()
                        .background(
                            colorResource(id = R.color.red_accent),
                            shape = trapezoidShape
                        )
                        .padding(horizontal = 4.dp)
                        .padding(bottom = 2.dp)
                        .padding(start = 8.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = if (TimeUtil.getDaysLeft(detail.startAt.toString()).toInt() == 0) {
                            stringResource(id = R.string.upcoming_event_today)
                        } else {
                            "${TimeUtil.getDaysLeft(detail.startAt.toString())} " + stringResource(id = R.string.upcoming_event_days_left)
                        },
                        color = colorResource(id = R.color.text),
                        fontSize = 14.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }   // Close box content
        }   // close main row
    }   // close card

}

@Composable
@Preview
fun UpcomingEventCardPreview() {
    EbikeTheme {
        UpcomingEventCardLayout(
            detail = EventData(
                id = 17,
                name = "Sepedaan seharian",
                locationData = LocationData(
                    1,
                    "Indonesia",
                    "Jawa Timur",
                    "Sidoarjo"),
                date = "2025-04-09",
                startAt = "2025-03-20 08:00:00",
                endAt = "2025-04-09 15:59:59",
                description = "Ini deskripsi data nya",
                thumbnail = Thumbnail(
                    url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/images/gLnJ1M9ZJSGJxwfRzUGp1G1gFFx8y2LSEzQUJ9jH.jpg"
                ),
                creator = ProfileData(
                    userId = 1,
                    username = "Ahmadfaris",
                    email = "ahmadfarish82@gmail.com",
                    mobilePhone = "+6285210363230",
                    cyclingStyle = listOf("gravel"),
                    city = LocationData(
                        city = "Sidoarjo"
                    ),
                    photo = Photo(
                        url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/images/1-f53f660d-c1e3-4937-9b84-55a36b8b8754.jpg"
                    )
                ),
                members = listOf(
                    ProfileData(
                        userId = 1,
                        username = "Ahmadfaris",
                        email = "ahmadfarish82@gmail.com",
                        mobilePhone = "+6285210363230",
                        cyclingStyle = listOf("gravel"),
                        city = LocationData(
                            city = "Sidoarjo"
                        ),
                        photo = Photo(
                            url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/images/1-f53f660d-c1e3-4937-9b84-55a36b8b8754.jpg"
                        )
                    )
                ),
                isMember = false,
                images = listOf(
                    Images(
                        url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/images/gLnJ1M9ZJSGJxwfRzUGp1G1gFFx8y2LSEzQUJ9jH.jpg"
                    )
                )
            ),
            onClick = {},
            isUpcomingEventLoading = false
        )
    }
}

@Composable
@Preview
fun UpcomingEventCardLoadingPreview() {
    EbikeTheme {
        UpcomingEventCardLayout(
            detail = EventData(
                id = 17,
                name = "Sepedaan seharian",
                locationData = LocationData(
                    1,
                    "Indonesia",
                    "Jawa Timur",
                    "Sidoarjo"),
                date = "2025-04-09",
                startAt = "2025-03-20 08:00:00",
                endAt = "2025-04-09 15:59:59",
                description = "Ini deskripsi data nya",
                thumbnail = Thumbnail(
                    url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/images/gLnJ1M9ZJSGJxwfRzUGp1G1gFFx8y2LSEzQUJ9jH.jpg"
                ),
                creator = ProfileData(
                    userId = 1,
                    username = "Ahmadfaris",
                    email = "ahmadfarish82@gmail.com",
                    mobilePhone = "+6285210363230",
                    cyclingStyle = listOf("gravel"),
                    city = LocationData(
                        city = "Sidoarjo"
                    ),
                    photo = Photo(
                        url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/images/1-f53f660d-c1e3-4937-9b84-55a36b8b8754.jpg"
                    )
                ),
                members = listOf(
                    ProfileData(
                        userId = 1,
                        username = "Ahmadfaris",
                        email = "ahmadfarish82@gmail.com",
                        mobilePhone = "+6285210363230",
                        cyclingStyle = listOf("gravel"),
                        city = LocationData(
                            city = "Sidoarjo"
                        ),
                        photo = Photo(
                            url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/images/1-f53f660d-c1e3-4937-9b84-55a36b8b8754.jpg"
                        )
                    )
                ),
                isMember = false,
                images = listOf(
                    Images(
                        url = "https://polygonbikes-app-dev.s3.ap-southeast-3.amazonaws.com/images/gLnJ1M9ZJSGJxwfRzUGp1G1gFFx8y2LSEzQUJ9jH.jpg"
                    )
                )
            ),
            onClick = {},
            isUpcomingEventLoading = true
        )
    }
}