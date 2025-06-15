package com.polygonbikes.ebike.v3.feature_profile.presentation.my_account.changecyclingstyle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.component.CardRoadStyle
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontLuxora
import com.polygonbikes.ebike.ui.theme.fontRns

@Composable
fun ChangeCyclingStyleScreen(
    onPopBackStack: () -> Unit,
    viewModel: ChangeCyclingStyleViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val newCyclingStyle = state.newCyclingStyle
    val isLoading = state.isLoading

    LaunchedEffect(key1 = viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.PopBackStack -> onPopBackStack()
                else -> Unit
            }
        }
    }

    ChangeCyclingStyleLayout(
        onPopBackStack = onPopBackStack,
        newCyclingStyle = newCyclingStyle,
        onCyclingStyleChange = { cyclingStyle ->
            val updatedCyclingStyles = if (newCyclingStyle.contains(cyclingStyle)) {
                newCyclingStyle - cyclingStyle
            } else {
                newCyclingStyle + cyclingStyle
            }
            viewModel.onEvent(ChangeCyclingStyleEvent.InputChangeCyclingStyle(updatedCyclingStyles))
        },
        onChangeCyclingStyle = {
            viewModel.onEvent(ChangeCyclingStyleEvent.ChangeCyclingStyle)
        },
        isLoading = isLoading
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeCyclingStyleLayout(
    onPopBackStack: () -> Unit,
    newCyclingStyle: List<String>,
    onCyclingStyleChange: (String) -> Unit,
    onChangeCyclingStyle: () -> Unit,
    isLoading: Boolean
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                    ) {
                        Text(
                            text = "Cycling Style",
                            fontFamily = fontLuxora,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colorResource(id = R.color.white)
                        )
                    }
                },
                navigationIcon = {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentHeight(align = Alignment.CenterVertically)
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
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = it.calculateTopPadding() + 12.dp,
                    start = it.calculateStartPadding(LayoutDirection.Ltr) + 20.dp,
                    end = it.calculateEndPadding(LayoutDirection.Ltr) + 20.dp
                )
        ) {
            Text(
                text = "Select your preferred cycling style",
                fontSize = 18.sp,
                fontFamily = fontRns,
                fontWeight = FontWeight.Medium,
                color = colorResource(id = R.color.white),
                modifier = Modifier
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                CardRoadStyle(
                    icon = R.drawable.road,
                    label = "Road\ncycling",
                    style = "road",
                    value = newCyclingStyle,
                    onAdd = onCyclingStyleChange,
                    onRemove = onCyclingStyleChange
                )

                CardRoadStyle(
                    icon = R.drawable.gravel,
                    label = "Gravel\ncycling",
                    style = "gravel",
                    value = newCyclingStyle,
                    onAdd = onCyclingStyleChange,
                    onRemove = onCyclingStyleChange
                )

                CardRoadStyle(
                    icon = R.drawable.offroad,
                    label = "Mountain\nbiking",
                    style = "off-road",
                    value = newCyclingStyle,
                    onAdd = onCyclingStyleChange,
                    onRemove = onCyclingStyleChange
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    onChangeCyclingStyle()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(colorResource(id = R.color.red)),
                enabled = !isLoading
            ) {
                Text(
                    text = "Continue",
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.white)
                )
            }
        }
    }
}

@Preview
@Composable
fun ChangeCyclingStylePreview() {
    EbikeTheme {
        ChangeCyclingStyleLayout(
            onPopBackStack = {},
            newCyclingStyle = listOf("road"),
            onCyclingStyleChange = {},
            onChangeCyclingStyle = {},
            isLoading = false
        )
    }
}
