package com.polygonbikes.ebike.v3.feature_profile.presentation.setting

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
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
import com.polygonbikes.ebike.core.component.LoadingIndicator
import com.polygonbikes.ebike.core.util.AlertDialogData
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.feature_me.presentation.component.DialogChangeLanguage
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_profile.domain.state.SettingState
import com.polygonbikes.ebike.v3.feature_profile.presentation.component.SettingRow
import java.util.Locale

@Composable
fun SettingScreen(
    onNavigate: (UiEvent.Navigate) -> Unit,
    onPopBackStack: () -> Unit,
    onRecreateActivity: () -> Unit,
    viewModel: SettingViewModel = hiltViewModel()
) {
    val state by viewModel.state

    var showDialog by remember { mutableStateOf(false) }
    var dialogData by remember { mutableStateOf(AlertDialogData()) }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onNavigate(event)
                is UiEvent.AlertDialog -> {
                    showDialog = true
                    dialogData = event.data
                }
                is UiEvent.RecreateActivity -> onRecreateActivity()
                else -> Unit
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(SettingEvent.OnInitScreen)
    }

    SettingLayout(
        state = state,
        onPopBackStack = onPopBackStack,
        onOpenMyAccountScreen = { viewModel.onEvent(SettingEvent.OpenMyAccountScreen) },
        onSetLanguage = { viewModel.onEvent(SettingEvent.SetLanguage(it)) },
        onOpenStrava = { viewModel.onEvent(SettingEvent.OpenStrava) },
        onOpenDetailStrava = { viewModel.onEvent(SettingEvent.OpenDetailStrava) },
        onLogout = { viewModel.onEvent(SettingEvent.Logout) }
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class, ExperimentalFoundationApi::class)
@Composable
fun SettingLayout(
    state: SettingState,
    onPopBackStack: () -> Unit,
    onOpenMyAccountScreen: () -> Unit,
    onSetLanguage: (String) -> Unit,
    onOpenStrava: () -> Unit,
    onOpenDetailStrava: () -> Unit,
    onLogout: () -> Unit
    ) {
    val showDialogChangeLanguage = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) LoadingIndicator(onDismiss = {})

        if (showDialogChangeLanguage.value) {
            DialogChangeLanguage(
                defaultLocaleTag = Locale.getDefault().toLanguageTag(),
                onDismiss = { showDialogChangeLanguage.value = false },
                onSaveLanguage = onSetLanguage
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                    ){
                        Text(
                            text = stringResource(id = R.string.setting_caption),
                            fontFamily = fontRns,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colorResource(id = R.color.white),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                modifier = Modifier.height(60.dp),
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(id = R.color.black))
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.background_apps))
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    bottom = paddingValues.calculateBottomPadding() + 8.dp
                ),
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            SettingRow(
                icon = Icons.Outlined.AccountCircle,
                text = stringResource(id = R.string.setting_myaccount_label),
                onClick = onOpenMyAccountScreen
            )
            Spacer(modifier = Modifier.height(28.dp))

            SettingRow(
                icon = Icons.Outlined.Language,
                text = stringResource(id = R.string.setting_language_label),
                onClick = { showDialogChangeLanguage.value = true }
            )
            Spacer(modifier = Modifier.height(28.dp))

            if (FeatureList.FeatureFriendship) {
                SettingRow(
                    icon = Icons.Outlined.Lock,
                    text = "Activity's privacy",
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(28.dp))
            }

            SettingRow(
                icon = painterResource(id = R.drawable.strava_icon),
                text = stringResource(id = R.string.setting_strava_label),
                onClick = { if (state.userStravaName == null) onOpenStrava() else onOpenDetailStrava() }
            )
            Spacer(modifier = Modifier.height(26.dp))

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth().height(1.dp),
                color = colorResource(id = R.color.sub_text)
            )
            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = stringResource(id = R.string.setting_logout_label),
                fontSize = 18.sp,
                fontFamily = fontRns,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.red_accent),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .clickable { onLogout() }
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SettingPreview() {
    EbikeTheme {
        SettingLayout(
            state = SettingState(),
            onPopBackStack = {},
            onOpenMyAccountScreen = {},
            onSetLanguage = {},
            onOpenStrava = {},
            onOpenDetailStrava = {},
            onLogout = {}
        )
    }
}
