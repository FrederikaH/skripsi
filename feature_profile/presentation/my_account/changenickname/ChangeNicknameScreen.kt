package com.polygonbikes.ebike.v3.feature_profile.presentation.my_account.changenickname

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontLuxora
import com.polygonbikes.ebike.ui.theme.fontRns
import com.smartlook.android.core.api.extension.smartlook

@Composable
fun ChangeNicknameScreen(
    onPopBackStack: () -> Unit,
    viewModel: ChangeNicknameViewModel = hiltViewModel(),
) {
    val state = viewModel.state.value
    val newNickname = state.newNickname
    val isLoading = state.isLoading

    LaunchedEffect(key1 = viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.PopBackStack -> onPopBackStack()
                else -> Unit
            }
        }
    }

    ChangeNicknameLayout(
        onPopBackStack = onPopBackStack,
        newNickname = newNickname,
        onNicknameChange = { newNickname ->
            viewModel.onEvent(ChangeNicknameEvent.InputChangeNickname(newNickname))
        },
        onChangeNickname = {
            viewModel.onEvent(ChangeNicknameEvent.ChangeNickname)
        },
        isLoading = isLoading
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeNicknameLayout(
    onPopBackStack: () -> Unit,
    newNickname: String,
    onNicknameChange: (String) -> Unit,
    onChangeNickname: () -> Unit,
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
                            text = "Nickname",
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
                                Icons.Filled.ArrowBack,
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
            TextField(
                value = newNickname,
                onValueChange = onNicknameChange,
                modifier = Modifier
                    .smartlook(isSensitive = true)
                    .fillMaxWidth()
                    .height(48.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically),
                textStyle = TextStyle(fontSize = 14.sp),
                colors = TextFieldDefaults.colors().copy(
                    focusedTextColor = colorResource(id = R.color.black),
                    unfocusedTextColor = colorResource(id = R.color.black),
                    focusedContainerColor = colorResource(id = R.color.white),
                    unfocusedContainerColor = colorResource(id = R.color.white)
                ),
                placeholder = {
                    Text(
                        "New nickname",
                        color = colorResource(id = R.color.light_gray),
                        fontSize = 14.sp
                    )
                },
                shape = MaterialTheme.shapes.small.copy()
            )
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    onChangeNickname()
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
fun ChangeNicknamePreview() {
    EbikeTheme {
        ChangeNicknameLayout(
            onPopBackStack = {},
            newNickname = "Catalina",
            onNicknameChange = {},
            onChangeNickname = {},
            isLoading = false
        )
    }
}