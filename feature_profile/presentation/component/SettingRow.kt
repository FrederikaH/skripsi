package com.polygonbikes.ebike.v3.feature_profile.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns

@Composable
fun SettingRow(
    icon: Any,
    text: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        when (icon) {
            is ImageVector -> Icon(
                icon,
                contentDescription = "setting icon",
                modifier = Modifier.size(22.dp),
                tint = colorResource(id = R.color.text)
            )
            is Painter -> Icon(
                painter = icon,
                contentDescription = "setting icon",
                modifier = Modifier.size(22.dp),
                tint = colorResource(id = R.color.text)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = fontRns,
            color = colorResource(id = R.color.text),
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = "right chevron icon",
            tint = colorResource(id = R.color.text))
    }
}

@Composable
@Preview
//    (showBackground = true)
fun SettingRowPreview() {
    EbikeTheme {
        Column(modifier = Modifier.padding(8.dp)) {
            SettingRow(
                icon = Icons.Default.Settings,
                text = "My Account",
                onClick = {}
            )
        }
    }
}
