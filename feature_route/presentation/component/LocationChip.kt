package com.polygonbikes.ebike.v3.feature_route.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns

@Composable
fun LocationChip(
    locationData: LocationData,
    onRemove: (LocationData) -> Unit
) {
    LocationChipLayout(locationData, onRemove)
}

@Composable
fun LocationChipLayout(
    locationData: LocationData,
    onRemove: (LocationData) -> Unit
) {
    InputChip(
        onClick = { onRemove(locationData) },

        colors = InputChipDefaults.inputChipColors(
            containerColor = colorResource(id = R.color.background_apps),
            selectedContainerColor = colorResource(id = R.color.background_apps),
            disabledContainerColor = colorResource(id = R.color.background_apps)
        ),

        border = BorderStroke(1.dp, colorResource(id = R.color.text_field_border)),
        shape = RoundedCornerShape(4.dp),

        label = {
            Text(
                text = locationData.city ?: "Unknown",
                fontSize = 14.sp,
                fontFamily = fontRns,
                fontWeight = FontWeight.Medium,
                color = colorResource(id = R.color.text),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },

        selected = true,

        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    )
}

@Composable
@Preview
fun LocationChipPreview() {
    EbikeTheme {
        LocationChipLayout(
            locationData = LocationData(
                id = 1,
                country = "Indonesia",
                region = "Jawa Timur",
                city = "Surabaya"
            ),
            onRemove = {}
        )
    }
}