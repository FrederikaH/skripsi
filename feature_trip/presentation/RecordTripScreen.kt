package com.polygonbikes.ebike.v3.feature_trip.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerState
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.feature_trip.domain.enum.MapModeEnum
import com.polygonbikes.ebike.feature_trip.presentation.component.googlemaps.GoogleMapsComponent
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontLuxora
import com.polygonbikes.ebike.ui.theme.fontRns

@Composable
fun RecordTripScreen() {
    RecordTripLayout(false)

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecordTripLayout(
    fullMapMode: Boolean
) {
    val bgSpeedbar = colorResource(id = R.color.bg_speedbar)
    val speedbar = Color.White


    Scaffold {
        //  main column
        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.background_apps))
                .fillMaxSize()
                .padding(
                    top = it.calculateTopPadding() + 16.dp,
                    start = it.calculateStartPadding(LayoutDirection.Ltr),
                    end = it.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = it.calculateBottomPadding() + 16.dp
                )
        ) {

            if (fullMapMode == true) {
                val markerStateList = remember {
                    mutableStateListOf(
                        MarkerState(
                            position = LatLng(
                                37.7749,
                                -122.4194
                            )
                        ), // Marker at San Francisco
                        MarkerState(position = LatLng(37.8044, -122.2711))  // Marker at Oakland
                    )
                }

                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.6f)
                            .background(color = colorResource(id = R.color.white))
                    ) {
                        GoogleMapsComponent(
                            lastLocation = LatLng(
                                37.7749,
                                -122.4194
                            ), // Dummy location: San Francisco
                            polylinePoints = listOf(
                                LatLng(37.7749, -122.4194), // Start point
                                LatLng(37.8044, -122.2711), // End point
                            ), // Example polyline between two dummy locations
                            listMarkerState = markerStateList,
                            meetPoint = LatLng(37.7924, -122.4105) // Example midpoint marker
                        )
                    }   // close box

                    Row (
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Distance (km)",
                                fontSize = 18.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(id = R.color.sub_text)
                            )
                            Text(
                                text = "46.2",
                                fontSize = 40.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(id = R.color.text)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Distance (km)",
                                fontSize = 18.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(id = R.color.sub_text)
                            )
                            Text(
                                text = "46.2",
                                fontSize = 40.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(id = R.color.text)
                            )
                        }

                    }
                }
            } else {
                // column bluetooth, speed bar, dan statistik
                Column(modifier = Modifier.weight(0.85f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.BluetoothDisabled,
                            contentDescription = "bluetooth icon"
                        )
                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "08.04",
                            fontSize = 26.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(50.dp))

                    // Box seluruh komponen speed
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .weight(0.5f)
                    ) {

                        // Box speed bar
                        Box {
                            // background speed bar (default abu2 nya)
                            Canvas(modifier = Modifier.size(240.dp)) {
                                drawArc(
                                    color = bgSpeedbar,
                                    -220f, //start angle
                                    260f, //sweep angle (how far the arc extends)
                                    useCenter = false,
                                    style = Stroke(24.dp.toPx(), cap = StrokeCap.Square)
                                )
                            }
                            // speed bar (putih) kecepatannya
                            Canvas(modifier = Modifier.size(240.dp)) {
                                drawArc(
                                    color = speedbar,
                                    -220f, //start angle
                                    200f, //sweep angle (how far the arc extends)
                                    useCenter = false,
                                    style = Stroke(22.dp.toPx(), cap = StrokeCap.Square)
                                )
                            }

                            Column(
                                modifier = Modifier.align(alignment = Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "35.7",
                                    fontSize = 50.sp,
                                    fontFamily = fontLuxora,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "km/h",
                                    fontSize = 20.sp,
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(id = R.color.sub_text)
                                )
                            }
                        }

                        // keterangan assist
                        Column(
                            modifier = Modifier
                                .align(alignment = Alignment.Center)
                                .padding(top = 150.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.assist_icon),
                                    contentDescription = "assist icon",
                                    tint = colorResource(id = R.color.sub_text)
                                )
                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = "Assist",
                                    fontSize = 20.sp,
                                    fontFamily = fontLuxora,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorResource(id = R.color.sub_text)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row {
                                Text(
                                    text = "Assist limit",
                                    fontSize = 18.sp,
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorResource(id = R.color.sub_text)
                                )
                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = "45" + " km/h",
                                    fontSize = 18.sp,
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(id = R.color.text)
                                )

                            }
                        }   // close column assist

                    }   // close box seluruh komponen speed

                    // cycling stats
                    Row(
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Duration & battery
                        Column {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text(
                                    text = "Duration",
                                    fontSize = 18.sp,
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorResource(id = R.color.sub_text)
                                )
                                Text(
                                    text = "51:42",
                                    fontSize = 40.sp,
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(id = R.color.text)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text(
                                    text = "Battery",
                                    fontSize = 18.sp,
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorResource(id = R.color.sub_text)
                                )
                                Text(
                                    text = "61%",
                                    fontSize = 40.sp,
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(id = R.color.text)
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "20" + " km",
                                    fontSize = 18.sp,
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorResource(id = R.color.text)
                                )
                            }
                        }

                        // Distance & assist
                        Column {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text(
                                    text = "Distance (km)",
                                    fontSize = 18.sp,
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorResource(id = R.color.sub_text)
                                )
                                Text(
                                    text = "46.2",
                                    fontSize = 40.sp,
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(id = R.color.text)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text(
                                    text = "Assist",
                                    fontSize = 18.sp,
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorResource(id = R.color.sub_text)
                                )
                                Text(
                                    text = "4",
                                    fontSize = 40.sp,
                                    fontFamily = fontRns,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(id = R.color.text)
                                )
                            }
                        }

                    }   // close cycling stat

                }   // close column bluetooth, speed bar, dan statistik


                // column button action
                Column(modifier = Modifier.weight(0.1f)) {
                    // row action button
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        // row button play, pause, stop
                        Row { ButtonPause() }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            IconButton(
                                onClick = {},
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = colorResource(id = R.color.white),
                                        shape = CircleShape
                                    )
                                    .clip(CircleShape)
                                    .wrapContentSize(align = Alignment.Center)
                            ) {
                                Icon(
                                    Icons.Outlined.Map,
                                    contentDescription = "map icon",
                                    tint = colorResource(id = R.color.text)
                                )
                            }
                        }

                    }   // close row action button

                }   // close column action button

                Column(modifier = Modifier.weight(0.05f)) {}
            }   // close else map mode

        }   // close main column
    }   // close scaffold
}

@Composable
fun ButtonStart() {
    IconButton(
        onClick = {},
        modifier = Modifier
            .clip(CircleShape)
            .aspectRatio(1f)
            .fillMaxWidth(0.5f)
            .background(color = colorResource(id = R.color.green_material))
            .wrapContentSize(align = Alignment.Center)
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Icons play arrow",
            modifier = Modifier.fillMaxSize(),
            tint = colorResource(id = R.color.white)
        )
    }
}

@Composable
fun ButtonStop() {
    IconButton(
        onClick = {},
        modifier = Modifier
            .clip(CircleShape)
            .aspectRatio(1f)
            .fillMaxWidth(0.5f)
            .background(color = colorResource(id = R.color.red_material))
            .wrapContentSize(align = Alignment.Center)
    ) {
        Icon(
            imageVector = Icons.Default.Stop,
            contentDescription = "Icons stop",
            modifier = Modifier.fillMaxSize(),
            tint = colorResource(id = R.color.white)
        )
    }
}

@Composable
fun ButtonPause() {
    IconButton(
        onClick = {},
        modifier = Modifier
            .clip(CircleShape)
            .aspectRatio(1f)
            .fillMaxWidth(0.5f)
            .background(color = colorResource(id = R.color.lighter_gray))
            .wrapContentSize(align = Alignment.Center)
    ) {
        Icon(
            imageVector = Icons.Default.Pause,
            contentDescription = "Icons pause",
            modifier = Modifier.fillMaxSize(),
            tint = colorResource(id = R.color.white)
        )
    }
}

@Composable
@Preview
fun RecordTripPreview() {
    EbikeTheme {
        RecordTripLayout(
            fullMapMode = false
        )
    }
}