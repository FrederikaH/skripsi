package com.polygonbikes.ebike.v3.feature_history.presentation.edit_trip

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.google.accompanist.pager.ExperimentalPagerApi
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.core.component.InputImage

@Composable
fun EditTrip(
    onPopBackStack: () -> Unit
) {
    EditTripLayout(
        onPopBackStack = onPopBackStack
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun EditTripLayout(
    onPopBackStack: () -> Unit
){
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentSize(align = Alignment.Center)
                    ) {
                        Text(
                            text = "Edit trip",
                            fontFamily = fontRns,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colorResource(id = R.color.white)
                        )
                    }
                },
                modifier = Modifier.height(60.dp),
                navigationIcon = {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentSize(align = Alignment.Center)
                    ) {
                        IconButton(onClick = onPopBackStack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back Icon",
                                tint = colorResource(id = R.color.white)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = colorResource(id = R.color.black)
                )
            )
        },
    ) {
        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.background_apps))
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(
                    top = it.calculateTopPadding() + 16.dp,
                    start = it.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                    end = it.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    bottom = it.calculateBottomPadding() + 8.dp
                ),
        ) {

            Text(
                text = "Trip name",
                fontSize = 20.sp,
                lineHeight = 1.sp,
                fontFamily = fontRns,
                color = colorResource(id = R.color.text),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            var tripName by remember { mutableStateOf("") }
            var textFieldBorder = colorResource(id = R.color.text_field_border)
            BasicTextField(
                value = tripName,
                onValueChange = { newText: String ->
                    tripName = newText
                },
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 1.sp,
                    fontFamily = fontRns,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .drawBehind {
                        val borderSize = 1.dp.toPx()
                        drawLine(
                            color = textFieldBorder,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = borderSize
                        )
                    }
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Add images (Max 3)",
                fontSize = 20.sp,
                lineHeight = 1.sp,
                fontFamily = fontRns,
                color = colorResource(id = R.color.text),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

//            InputImage()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Maximum file size 20 mb",
                fontSize = 16.sp,
                lineHeight = 1.sp,
                fontFamily = fontRns,
                color = colorResource(id = R.color.sub_text),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Bike",
                fontSize = 20.sp,
                lineHeight = 1.sp,
                fontFamily = fontRns,
                color = colorResource(id = R.color.text),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            var bikeName by remember { mutableStateOf("") }
            var expanded by remember { mutableStateOf(false) }
            val bikeList = listOf("Mountain Bike", "Road Bike", "Kalosi", "Siskiu")

            // input bike name
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .border(
                        width = 1.dp,
                        color = colorResource(id = R.color.text_field_border),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) { // text field bike name
                BasicTextField(
                    value = bikeName,
                    onValueChange = { newText: String ->
                        bikeName = newText
                        expanded = newText.isNotEmpty()
                    },
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = fontRns,
                        color = colorResource(id = R.color.text),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    ),
                    cursorBrush = SolidColor(colorResource(id = R.color.text)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                expanded = false
                            }
                        }
                )   // close text field
                // hint text
                if (bikeName.isEmpty()) {
                    Text(
                        text = "Enter your bike name...",
                        style = TextStyle(
                            fontSize = 18.sp,
                            color = colorResource(id = R.color.sub_text),
                            fontFamily = fontRns,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    )
                }
                // dropdown (suggestion)
                if (expanded) {
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(),
                        properties = PopupProperties(focusable = false)
                    ) {
                        bikeList.filter { it.contains(bikeName, ignoreCase = true) }.forEach { bike ->
                            DropdownMenuItem(
                                onClick = {
                                    bikeName = bike
                                    expanded = false
                                },
                                text = {Text(bike)}
                            )
                        }
                    }
                }   // close dropdown
            }   // close bike input
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Surface",
                fontSize = 20.sp,
                lineHeight = 1.sp,
                fontFamily = fontRns,
                color = colorResource(id = R.color.text),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // surface checkbox option
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                var isRoad by remember { mutableStateOf(false) }
                var isGravel by remember { mutableStateOf(false) }
                var isOffroad by remember { mutableStateOf(false) }

                // row road
                Row {
                    Text(
                        text = "Road",
                        fontSize = 16.sp,
                        lineHeight = 1.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(id = R.color.text)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = isRoad,
                        onCheckedChange = { isRoad = it },
                        modifier = Modifier.size(20.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            checkmarkColor = Color.Black,
                            uncheckedColor = Color.White
                        )
                    )
                }   // close row road

                // row gravel
                Row {
                    Text(
                        text = "Gravel",
                        fontSize = 16.sp,
                        lineHeight = 1.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(id = R.color.text)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = isGravel,
                        onCheckedChange = { isGravel = it },
                        modifier = Modifier.size(20.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            checkmarkColor = Color.Black,
                            uncheckedColor = Color.White
                        )
                    )
                }   // Close row gravel

                // row off-road
                Row {
                    Text(
                        text = "Off-road",
                        fontSize = 16.sp,
                        lineHeight = 1.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(id = R.color.text)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = isOffroad,
                        onCheckedChange = { isOffroad = it },
                        modifier = Modifier.size(20.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            checkmarkColor = Color.Black,
                            uncheckedColor = Color.White
                        )
                    )
                }   // close row off-road
            }   // close surface type options
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Trip's purpose",
                fontSize = 20.sp,
                lineHeight = 1.sp,
                fontFamily = fontRns,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.text)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // trip's purpose options
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                var exercise by remember { mutableStateOf(false) }
                var recreation by remember { mutableStateOf(false) }
                var touring by remember { mutableStateOf(false) }

                // row Exercise
                Row {
                    Text(
                        text = "Exercise",
                        fontSize = 16.sp,
                        lineHeight = 1.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(id = R.color.text)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = exercise,
                        onCheckedChange = { exercise = it },
                        modifier = Modifier.size(20.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            checkmarkColor = Color.Black,
                            uncheckedColor = Color.White
                        )
                    )
                }   // close row Exercise

                // row Recreation
                Row {
                    Text(
                        text = "Recreation",
                        fontSize = 16.sp,
                        lineHeight = 1.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(id = R.color.text)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = recreation,
                        onCheckedChange = { recreation = it },
                        modifier = Modifier.size(20.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            checkmarkColor = Color.Black,
                            uncheckedColor = Color.White
                        )
                    )
                }   // Close row Recreation

                // row Touring
                Row {
                    Text(
                        text = "Touring",
                        fontSize = 16.sp,
                        lineHeight = 1.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(id = R.color.text)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = touring,
                        onCheckedChange = { touring = it },
                        modifier = Modifier.size(20.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            checkmarkColor = Color.Black,
                            uncheckedColor = Color.White
                        )
                    )
                }   // close row Touring
            }   // close column trip's purpose options
            Spacer(modifier = Modifier.height(32.dp))

            // trip privacy
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {

                    Text(
                        text = "Keep trip private",
                        fontSize = 20.sp,
                        lineHeight = 1.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(id = R.color.text)
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()
                    val tooltipState = rememberBasicTooltipState(isPersistent = false)

                    BasicTooltipBox(
                        positionProvider = tooltipPosition,
                        tooltip = {
                            Text(
                                text = "Your activity will be shared to public if you turn the privacy off",
                                modifier = Modifier
                                    .background(colorResource(id = R.color.secondary))
                                    .padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = 12.dp,
                                        bottom = 18.dp
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .width(200.dp)
                            )
                        },
                        state = tooltipState
                    ) {

                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = "info icon",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))

                var checked by remember { mutableStateOf(true) }
                Box(
                    modifier = Modifier
                        .size(41.6.dp, 25.6.dp)
                ) {
                    Switch(
                        checked = checked,
                        onCheckedChange = {
                            checked = it
                        },

                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorResource(id = R.color.text),
                            checkedTrackColor = colorResource(id = R.color.switch_track),
                            uncheckedThumbColor = colorResource(id = R.color.text),
                            uncheckedBorderColor = colorResource(id = R.color.sub_text),
                            uncheckedTrackColor = colorResource(id = R.color.background_apps),
                        ),
                        modifier = Modifier.scale(0.8f)
                    )
                }   // close switch
            }   // close trip privacy
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = colorResource(id = R.color.red_accent)
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Save change",
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.text)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

        }   // close main column
    }   // close scaffold
}

@Composable
@Preview
fun EditTripPreview() {
    EbikeTheme {
        EditTripLayout(
            onPopBackStack = {}
        )
    }
}
