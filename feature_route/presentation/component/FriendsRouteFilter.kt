package com.polygonbikes.ebike.v3.feature_route.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns

@Composable
fun FriendsRouteFilter() {
    FriendsRouteFilterLayout()
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FriendsRouteFilterLayout() {

//    topBar = {
//        TopAppBar(
//            title = {
//                Column(
//                    modifier = Modifier
//                        .fillMaxHeight()
//                        .wrapContentSize(align = Alignment.Center)
//                ) {
//                    Text(
//                        text = "Route filter",
//                        fontFamily = fontRns,
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 18.sp,
//                        color = colorResource(id = R.color.text)
//                    )
//                }
//            },
//            modifier = Modifier.height(60.dp),
//            navigationIcon = {
//                Column(
//                    modifier = Modifier
//                        .fillMaxHeight()
//                        .wrapContentSize(align = Alignment.Center)
//                ) {
//                    IconButton(onClick = onPopBackStack) {
//                        Icon(
//                            Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = "Back Icon",
//                            tint = colorResource(id = R.color.text)
//                        )
//                    }
//                }
//            },
//            actions = {
//                Box(
//                    modifier = Modifier
//                        .fillMaxHeight()
//                        .padding(end = 16.dp)
//                ) {
//                    Text(
//                        text = "Reset",
//                        fontSize = 18.sp,
//                        fontFamily = fontRns,
//                        color = colorResource(id = R.color.text),
//                        fontWeight = FontWeight.Bold,
//                        modifier = Modifier
//                            .align(Alignment.Center)
//                    )
//                }
//
//            },
//            colors = TopAppBarDefaults.topAppBarColors().copy(
//                containerColor = colorResource(id = R.color.black)
//            )
//        )
//    }

    Box {
        //  Column filter
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(colorResource(id = R.color.background_apps)),
            verticalArrangement = Arrangement.spacedBy(16.dp)

        ) {

            //  Column surface
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "Surface",
                    fontSize = 20.sp,
                    lineHeight = 1.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    var paved by remember { mutableStateOf(false) }
                    var gravel by remember { mutableStateOf(false) }
                    var offroad by remember { mutableStateOf(false) }

                    // row paved road
                    Row {
                        Text(
                            text = "Paved road",
                            fontSize = 18.sp,
                            lineHeight = 1.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Checkbox(
                            checked = paved,
                            onCheckedChange = { paved = it },
                            modifier = Modifier.size(20.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color.White,
                                checkmarkColor = Color.Black,
                                uncheckedColor = Color.White
                            )
                        )
                    }   // close row paved road
                    // row gravel
                    Row {
                        Text(
                            text = "Gravel",
                            fontSize = 18.sp,
                            lineHeight = 1.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Checkbox(
                            checked = gravel,
                            onCheckedChange = { gravel = it },
                            modifier = Modifier.size(20.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color.White,
                                checkmarkColor = Color.Black,
                                uncheckedColor = Color.White
                            )
                        )
                    }   // Close row gravel
                    // row off road
                    Row {
                        Text(
                            text = "Off-road",
                            fontSize = 18.sp,
                            lineHeight = 1.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Checkbox(
                            checked = offroad,
                            onCheckedChange = { offroad = it },
                            modifier = Modifier.size(20.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color.White,
                                checkmarkColor = Color.Black,
                                uncheckedColor = Color.White
                            )
                        )
                    }   // Close row off road
                }   // column surface type options
            }   // close column surface
            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.text_field_border))
            )

            // column start from
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Start from",
                    fontSize = 20.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text)
                )
                Spacer(modifier = Modifier.height(16.dp))
                // row untuk memisah 2 bagian
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // column within
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Within",
                            fontSize = 18.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // start from text field
                        var startFrom by remember { mutableStateOf("") }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(
                                    1.dp,
                                    colorResource(id = R.color.text_field_border),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = startFrom,
                                onValueChange = { startFrom = it },
                                textStyle = TextStyle(
                                    fontSize = 18.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(colorResource(id = R.color.text)),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "km",
                                fontSize = 16.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(id = R.color.text_field_border)
                            )
                        }   // close start from text field
                    }   // close start from

                    Column(modifier = Modifier.weight(1f)) {} // spy column within jadi separuh
                }   // close row start from
            }   // close column start
            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.text_field_border))
            )

            // column distance
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Distance",
                    fontSize = 20.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // row untuk memisah from dan to
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // column from
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = "From",
                            fontSize = 18.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // distance from text field
                        var distanceFrom by remember { mutableStateOf("") }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(
                                    1.dp,
                                    colorResource(id = R.color.text_field_border),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = distanceFrom,
                                onValueChange = { distanceFrom = it },
                                textStyle = TextStyle(
                                    fontSize = 18.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(colorResource(id = R.color.text)),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "km",
                                fontSize = 16.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(id = R.color.text_field_border)
                            )
                        }   // close distance from text field
                    }   // close distance from

                    // column distance to
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = "To",
                            fontSize = 18.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // distance to text field
                        var distanceTo by remember { mutableStateOf("") }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(
                                    1.dp,
                                    colorResource(id = R.color.text_field_border),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = distanceTo,
                                onValueChange = { distanceTo = it },
                                textStyle = TextStyle(
                                    fontSize = 18.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(colorResource(id = R.color.text)),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "km",
                                fontSize = 16.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(id = R.color.text_field_border)
                            )
                        }   // close distance to text field
                    }   // close distance to

                }   // close row from & to
            }   // close column distance
            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.text_field_border))
            )

            // column elevation
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Elevation",
                    fontSize = 20.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // row untuk memisah from dan to
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // column from
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = "From",
                            fontSize = 18.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // elevation from text field
                        var elevationFrom by remember { mutableStateOf("") }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(
                                    1.dp,
                                    colorResource(id = R.color.text_field_border),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = elevationFrom,
                                onValueChange = { elevationFrom = it },
                                textStyle = TextStyle(
                                    fontSize = 18.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(colorResource(id = R.color.text)),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "km",
                                fontSize = 16.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(id = R.color.text_field_border)
                            )
                        }   // close elevation from text field
                    }   // close elevation from

                    // column elevation to
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = "To",
                            fontSize = 18.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // elevation to text field
                        var elevationTo by remember { mutableStateOf("") }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(
                                    1.dp,
                                    colorResource(id = R.color.text_field_border),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = elevationTo,
                                onValueChange = { elevationTo = it },
                                textStyle = TextStyle(
                                    fontSize = 18.sp,
                                    fontFamily = fontRns,
                                    color = colorResource(id = R.color.text),
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(colorResource(id = R.color.text)),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "km",
                                fontSize = 16.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(id = R.color.text_field_border)
                            )
                        }   // close elevation to text field
                    }   // close elevation to

                }   // close row from & to
            }   // close column elevation
            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.text_field_border))
            )

            // showBottomSheet menggunakan variabel karena bottomSheet tdk bs dimasukkan ke if di button search location
            var showBottomSheetLoc by remember { mutableStateOf(false) } //Bottom sheet muncul saat search location diklik
            // column location
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Location",
                    fontSize = 20.sp,
                    lineHeight = 1.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // location card
                Card(
                    onClick = { showBottomSheetLoc = true }, //seharusnya bottom sheet masuk disini
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, colorResource(id = R.color.text_field_border)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Row(
                        modifier = Modifier
                            .background(colorResource(id = R.color.background_apps))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Search location",
                            fontSize = 18.sp,
                            lineHeight = 1.sp,
                            fontFamily = fontRns,
                            color = colorResource(id = R.color.sub_text),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "",
                            Modifier
                                .padding(end = 4.dp)
                                .size(24.dp),
                            tint = colorResource(id = R.color.sub_text)
                        )
                    }
                }   // close location card
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
//                    repeat(4) { LocationChip() }
                }

                // bottom sheet
                if (showBottomSheetLoc) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheetLoc = false },
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ) { LocationFilter() }
                }
            }   // close column location

            Spacer(modifier = Modifier.height(4.dp))

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.text_field_border))
            )

            // column friend
            var showBottomSheetFriend by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Friend's route",
                    fontSize = 20.sp,
                    lineHeight = 1.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Choose a friend to see their routes",
                    fontSize = 16.sp,
                    lineHeight = 1.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(id = R.color.sub_text)
                )
                Spacer(modifier = Modifier.height(10.dp))

                //  search friend card
                Card(
                    onClick = { showBottomSheetFriend = true },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, colorResource(id = R.color.text_field_border)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Row(
                        modifier = Modifier
                            .background(colorResource(id = R.color.background_apps))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Search friend",
                            fontSize = 18.sp,
                            lineHeight = 1.sp,
                            fontFamily = fontRns,
                            color = colorResource(id = R.color.sub_text),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "",
                            Modifier
                                .padding(end = 4.dp)
                                .size(24.dp),
                            tint = colorResource(id = R.color.sub_text)
                        )
                    }
                }   // close search friend card
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = colorResource(id = R.color.text_field_border),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    val totalLocations = 3
                    repeat(totalLocations) { index ->
                        AvatarFriend()

                        // divider terakhir tidak perlu muncul
                        if (index < totalLocations - 1) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp),
                                color = colorResource(id = R.color.sub_text)
                            )
                        }
                    }   // close friend result looping
                }

                // bottom sheet
                if (showBottomSheetFriend) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheetFriend = false },
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ) { FriendsFilter() }
                }
            }   // close column friend
            Spacer(modifier = Modifier.height(100.dp))

        }   // close column filter

        var totalResult by remember { mutableIntStateOf(0) }
        // box floating button
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(color = colorResource(id = R.color.background_apps))
            ) {
                FloatingActionButton(
                    onClick = { },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(vertical = 20.dp, horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(40.dp),
                    containerColor = colorResource(id = R.color.red_accent)
                ) {
                    Text(
                        text = "See $totalResult routes",
                        color = colorResource(id = R.color.text),
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
        }   // close box floating button

    }   // close main box

}


@Composable
@Preview
fun FriendsRouteFilterPreview() {
    EbikeTheme {
        FriendsRouteFilterLayout()
    }
}