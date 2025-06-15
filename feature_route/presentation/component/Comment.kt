package com.polygonbikes.ebike.v3.feature_route.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.component.DefaultProfilePicture
import com.polygonbikes.ebike.core.model.CommentData
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.core.util.TimeUtil
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ProfileData

@Composable
fun Comment(
    detail: CommentData
) {
    Column (
        modifier = Modifier.background(colorResource(id = R.color.background_apps))
    ){
        Row(verticalAlignment = Alignment.CenterVertically){
            if (detail.user?.photo != null) {
                Image(
                    painter = rememberAsyncImagePainter(detail.user?.photo),
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                )
            } else {
                DefaultProfilePicture(labelName = detail.user?.username.toString(), size = 38)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text (
                    text = "${detail.user?.username}",
                    fontSize = 16.sp,
                    lineHeight = 1.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(id = R.color.text)
                )
                Spacer(modifier = Modifier.height(4.dp))
                

                Text (
                    text = TimeUtil.getFormattedDate(detail.createdAt.toString(), TimeUtil.FormatSQLDateTime, TimeUtil.FormatDayMonth),
                    fontSize = 14.sp,
                    lineHeight = 1.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.sub_text)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text (
            text = "${detail.message}",
            fontSize = 16.sp,
            fontFamily = fontRns,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(id = R.color.text)
        )

    }
}

@Preview
@Composable
fun CommentPreview() {
    EbikeTheme {
        Comment(
            detail = CommentData(
                user = ProfileData(
                    userId = 1,
                    username = "User 45",
                    city = LocationData(
                        id = 1,
                        country = "indonesia",
                        region = "jawa timur",
                        city = "sidoarjo"
                    )
                ),
                message = "Semangat guys",
                createdAt = "2025-05-01 21:04:38"
            )
        )
    }
}