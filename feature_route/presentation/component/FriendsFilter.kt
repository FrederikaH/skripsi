package com.polygonbikes.ebike.v3.feature_route.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns

@Composable
fun FriendsFilter() {
    FriendsFilterLayout()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsFilterLayout() {

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
                        text = "Search friend",
                        fontSize = 18.sp,
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
        }   // close column search bar

        // scrollable city result
        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.background_apps))
                .padding(start = 16.dp, top = 8.dp, bottom = 16.dp, end = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            var checked by remember { mutableStateOf(false) }

            val totalLocations = 5
            repeat(totalLocations) { index ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Image(
                        painter = painterResource(id = R.drawable.route_image),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Column{
                        Text (
                            text = "User1234",
                            fontSize = 16.sp,
                            lineHeight = 1.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Text (
                            text = "Sidoarjo",
                            fontSize = 14.sp,
                            lineHeight = 1.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.text)
                        )
                    }
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
fun FriendsFilterPreview() {
    EbikeTheme {
        FriendsFilterLayout()
    }
}
