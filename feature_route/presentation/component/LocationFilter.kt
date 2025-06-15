package com.polygonbikes.ebike.v3.feature_route.presentation.component

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns

@Composable
fun LocationFilter() {
    LocationFilterLayout()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationFilterLayout() {

    Column(
        modifier = Modifier
            .background(colorResource(id = R.color.background_apps))
            .fillMaxSize()
    ) {
        // fixed at top
        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.background_apps))
                .padding(16.dp)
        ) {
            var searchKeyword by remember { mutableStateOf("") }
            TextField(
                value = searchKeyword,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                onValueChange = { newText: String ->
                    searchKeyword = newText
                },
                // hint text
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.route_filter_search_location_label),
                        fontSize = 16.sp,
                        lineHeight = 1.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(id = R.color.sub_text)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorResource(id = R.color.secondary),
                    unfocusedContainerColor = colorResource(id = R.color.secondary),
                    focusedIndicatorColor = colorResource(id = R.color.background_apps),
                    unfocusedIndicatorColor = colorResource(id = R.color.background_apps)
                ),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 1.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.text)
                ),

                trailingIcon = {
                    IconButton(
                        onClick = { /* do something*/ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "",
                            modifier = Modifier
                                .size(26.dp)
                        )
                    }
                }
            )   // close text field search location
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "current location icon",
                    modifier = Modifier
                        .size(20.dp),
                    tint = colorResource(id = R.color.text)
                )
                Text(
                    text = stringResource(id = R.string.location_filter_current_location_label),
                    fontSize = 16.sp,
                    lineHeight = 1.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.text)
                )
            }
        }

        // scrollable city result
        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.background_apps))
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var checked by remember { mutableStateOf(false) }

            val totalLocations = 5
            repeat(totalLocations) { index ->
                Row {
                    Text(
                        text = "Nama kota",
                        fontSize = 16.sp,
                        lineHeight = 1.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(id = R.color.text)
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    Checkbox(
                        checked = checked,
                        onCheckedChange = { checked = it },
                        modifier = Modifier.size(20.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            checkmarkColor = Color.Black,
                            uncheckedColor = Color.White
                        )
                    )
                }   // close row location option

                // divider terakhir tidak perlu muncul
                if (index < totalLocations - 1) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = colorResource(id = R.color.sub_text)
                    )
                }
            }   // close location result looping

        }   // close scrollable location result column

        // box floating button
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(color = colorResource(id = R.color.background_apps))
            ) {
                FloatingActionButton(
                    onClick = { },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .padding(bottom = 20.dp, top = 20.dp)
                        .fillMaxWidth()
                        .height(40.dp)
                        .padding(start = 16.dp, end = 16.dp),
                    containerColor = colorResource(id = R.color.red_accent)
                ) {
                    Text(
                        text = "Save",
                        color = colorResource(id = R.color.text),
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
        }   // close box floating button


    }
}


@Composable
@Preview
fun LocationFilterPreview() {
    EbikeTheme {
        LocationFilterLayout()
    }
}

