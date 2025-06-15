package com.polygonbikes.ebike.v3.feature_route.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns

@Composable
fun AvatarFriend() {
    AvatarFriendLayout()
}

@Composable
fun AvatarFriendLayout() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ){
        Image(
            painter = painterResource(id = R.drawable.route_image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))

        Text (
            text = "User4321",
            fontSize = 20.sp,
            lineHeight = 1.sp,
            fontFamily = fontRns,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(id = R.color.text),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Spacer(modifier = Modifier.weight(1f))

        Icon(
            Icons.Default.Close,
            contentDescription = "",
            Modifier
                .padding(end = 4.dp)
                .size(18.dp),
            tint = colorResource(id = R.color.text)
        )
    }

}

@Preview
@Composable
fun AvatarFriendPreview() {
    EbikeTheme {
        AvatarFriend()
    }
}