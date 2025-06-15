package com.polygonbikes.ebike.v3.feature_profile.presentation.my_account

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.FeatureList
import com.polygonbikes.ebike.core.component.DeleteDialog
import com.polygonbikes.ebike.core.model.LocationData
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ProfileData
import com.polygonbikes.ebike.v3.feature_profile.domain.state.ProfileState

@Composable
fun MyAccountScreen(
    onNavigate: (UiEvent.Navigate) -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: MyAccountViewModel = hiltViewModel()
) {
    val state = viewModel.stateFlow.collectAsState().value

    LaunchedEffect(key1 = viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onNavigate(event)
                else -> Unit
            }
        }
    }

    LaunchedEffect(true) {
        viewModel.onEvent(MyAccountEvent.InitDataChanges)
    }

    MyAccountLayout(
        state = state,
        onPopBackStack = onPopBackStack,
        openChangeNickname = {
            viewModel.onEvent(MyAccountEvent.OpenChangeNickname)
        },
        openChangeLocation = {
            viewModel.onEvent(MyAccountEvent.OpenChangeLocation)
        },
        openChangeCyclingStyle = {
            viewModel.onEvent(MyAccountEvent.OpenChangeCyclingStyle)
        },
        onDeleteAccount = {
            viewModel.onEvent(MyAccountEvent.DeleteAccount)
        }
    )
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalPagerApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun MyAccountLayout(
    state: ProfileState,
    onPopBackStack: () -> Unit,
    openChangeNickname: () -> Unit,
    openChangeLocation: () -> Unit,
    openChangeCyclingStyle: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(value = false) }

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
                            text = stringResource(id = R.string.setting_myaccount_label),
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
                .fillMaxSize()
                .padding(
                    top = it.calculateTopPadding() + 16.dp,
                    start = it.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                    end = it.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    bottom = it.calculateBottomPadding() + 8.dp
                ),
        ) {
            if (showDeleteDialog) {
                DeleteDialog(
                    title = stringResource(R.string.delete_account_dialog_title),
                    message = stringResource(R.string.dialog_delete_account_description),
                    onConfirm = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    },
                    onCancel = { showDeleteDialog = false }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { openChangeNickname() }
            ) {
                Text(
                    text = stringResource(id = R.string.myaccount_nickname_label),
                    fontSize = 18.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.sub_text),
                    modifier = Modifier
                        .padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.weight(1f))

                Row {
                    Text(
                        text = state.profile?.username ?: "Not set".toString(),
                        fontSize = 18.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(id = R.color.text),
                        modifier = Modifier
                            .padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "icon arrow"
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))

            if (FeatureList.FeatureRoute) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { openChangeLocation() }
                ) {
                    Text(
                        text = "Location",
                        fontSize = 18.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(id = R.color.sub_text),
                        modifier = Modifier
                            .padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    Row {
                        Text(
                            text = state.profile?.city?.city ?: "Not set".toString(),
                            fontSize = 18.sp,
                            fontFamily = fontRns,
                            fontWeight = FontWeight.Medium,
                            color = colorResource(id = R.color.text),
                            modifier = Modifier
                                .padding(start = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "icon arrow"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { openChangeCyclingStyle() }
                ) {
                    Text(
                        text = "Cycling style",
                        fontSize = 18.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(id = R.color.sub_text),
                        modifier = Modifier
                            .padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    Row {
                        val cyclingStyleMap = mapOf(
                            "road" to "Road",
                            "gravel" to "Gravel",
                            "mountain" to "Off-road"
                        )

                        // Cyling style
                        state.profile?.cyclingStyle?.takeIf { it.isNotEmpty() }?.let { styles ->

                            Text(
                                text = styles.joinToString(", ") { cyclingStyleMap[it] ?: it },
                                fontSize = 18.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.Medium,
                                color = colorResource(id = R.color.text),
                                modifier = Modifier
                                    .padding(start = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "icon arrow"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.myaccount_email_label),
                    fontSize = 18.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.sub_text),
                    modifier = Modifier
                        .padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.weight(1f))

                Row {
                    Text(
                        text = state.profile?.email ?: "Not set".toString(),
                        fontSize = 18.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(id = R.color.text),
                        modifier = Modifier
                            .padding(start = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.myaccount_phone_number_label),
                    fontSize = 18.sp,
                    fontFamily = fontRns,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.sub_text),
                    modifier = Modifier
                        .padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.weight(1f))

                Row {
                    Text(
                        text = state.profile?.mobilePhone ?: "Not set".toString(),
                        fontSize = 18.sp,
                        fontFamily = fontRns,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(id = R.color.text),
                        modifier = Modifier
                            .padding(start = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(26.dp))

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp),
                color = colorResource(id = R.color.sub_text)
            )
            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "Delete my account",
                fontSize = 18.sp,
                fontFamily = fontRns,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.red_accent),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .clickable{
                        showDeleteDialog = true
                    }
            )

        }
    }
}

@Composable
@Preview
fun MyAccountPreview() {
    EbikeTheme {
        MyAccountLayout(
            state = ProfileState(
                ProfileData(
                    username = "Catalina",
                    city = LocationData(
                        city = "Sidoarjo"
                    ),
                    cyclingStyle = listOf("road", "gravel", "off-road"),
                    email = "Catalina@gmail.com",
                    mobilePhone = "+629543045799"
                )
            ),
            onPopBackStack = {},
            openChangeNickname = {},
            openChangeLocation = {},
            openChangeCyclingStyle = {},
            onDeleteAccount = {}
        )
    }
}