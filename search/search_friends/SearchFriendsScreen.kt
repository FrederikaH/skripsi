package com.polygonbikes.ebike.v3.feature_home.presentation.search.search_friends

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.ui.theme.EbikeTheme
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_event.presentation.component.LocationFilter
import com.polygonbikes.ebike.v3.feature_group.presentation.component.CyclingTypeFilter
import com.polygonbikes.ebike.v3.feature_home.domain.state.SearchFriendsState
import com.polygonbikes.ebike.v3.feature_home.presentation.component.UserProfileCard
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ListUserResponseMiddleware
import com.polygonbikes.ebike.v3.feature_profile.data.entities.response.ProfileData

@Composable
fun SearchFriendsScreen(
    onNavigate: (UiEvent.Navigate) -> Unit,
    viewModel: SearchFriendsViewModel = hiltViewModel()
) {
    val state by viewModel.state

    LaunchedEffect(key1 = viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onNavigate(event)
                else -> Unit
            }
        }
    }

    LaunchedEffect(true) {
        viewModel.onEvent(SearchFriendsEvent.InitDataChanges)
    }

    val lazyListState = rememberLazyListState()

    SearchFriendsLayout(
        state = state,
        lazyListState = lazyListState,
        onSearchKeywordChanged = {
            viewModel.onEvent(SearchFriendsEvent.SearchKeywordChanged(it))
        },

        onOpenUserProfile = {
            viewModel.onEvent(SearchFriendsEvent.OpenUserProfile(it))
        },

        onShowLocationFilter = {
            viewModel.onEvent(SearchFriendsEvent.ShowLocationFilter)
        },

        onUpdateSelectedCities = { selectedCities ->
            viewModel.onEvent(
                SearchFriendsEvent.UpdateSelectedCities(
                    selectedCities
                )
            )
        },

        onShowCyclingTypeFilter = {
            viewModel.onEvent(SearchFriendsEvent.ShowCyclingTypeFilter)
        },

        onUpdateCyclingTypeFilter = { cyclingType ->
            viewModel.onEvent(
                SearchFriendsEvent.UpdateCyclingTypeFilter(
                    cyclingType
                )
            )
        },

        onFollow = { userId ->
            viewModel.onEvent(SearchFriendsEvent.Follow(userId))
        },

        onUnfollow = { userId ->
            viewModel.onEvent(SearchFriendsEvent.Unfollow(userId))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun SearchFriendsLayout(
    state: SearchFriendsState,
    lazyListState: LazyListState,
    onSearchKeywordChanged: (String) -> Unit,
    onOpenUserProfile: (Int) -> Unit,

    onShowLocationFilter: () -> Unit,
    onUpdateSelectedCities: (List<String>) -> Unit,

    onShowCyclingTypeFilter: () -> Unit,
    onUpdateCyclingTypeFilter: (List<String>) -> Unit,

    onFollow: (Int) -> Unit,
    onUnfollow: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background_apps))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
            ) {
                var searchKeyword by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = searchKeyword,
                    onValueChange = {
                        val safeInput = it.toString()
                        searchKeyword = safeInput
                        onSearchKeywordChanged(safeInput)
                    },
                    placeholder = {
                        Text(
                            "Search",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = fontRns,
                            color = colorResource(id = R.color.sub_text),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchKeyword.isNotEmpty()) {
                            IconButton(onClick = {
                                searchKeyword = ""
                                onSearchKeywordChanged("")
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant, // Corrected
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // filter location
                    Card(
                        onClick = { onShowLocationFilter() },
                        modifier = Modifier
                            .height(32.dp)
                            .wrapContentWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.selectedCities.isEmpty())
                                colorResource(id = R.color.transparent)
                            else
                                colorResource(id = R.color.text)
                        ),
                        border = BorderStroke(
                            1.dp,
                            color = if (state.selectedCities.isEmpty())
                                colorResource(R.color.sub_text)
                            else
                                colorResource(id = R.color.text)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "Location",
                                fontSize = 16.sp,
                                lineHeight = 1.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold,
                                color = if (state.selectedCities.isEmpty())
                                    colorResource(id = R.color.text)
                                else
                                    colorResource(id = R.color.background_apps)
                            )
                        }

                        if (state.showLocationFilter) {
                            ModalBottomSheet(
                                onDismissRequest = { onShowLocationFilter() },
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                            ) {
                                LocationFilter(
                                    state.listCityFilter,
                                    selectedCities = state.selectedCities,
                                    onSaveSelectedCities = { selectedCities ->
                                        onUpdateSelectedCities(selectedCities)
                                    }
                                )
                            }
                        }
                    }   // close filter location


                    // filter cycling type
                    Card(
                        onClick = { onShowCyclingTypeFilter() },
                        modifier = Modifier
                            .height(32.dp)
                            .wrapContentWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.selectedCyclingType.isEmpty())
                                colorResource(id = R.color.transparent)
                            else
                                colorResource(id = R.color.text)
                        ),
                        border = BorderStroke(
                            1.dp,
                            color = if (state.selectedCyclingType.isEmpty())
                                colorResource(R.color.sub_text)
                            else
                                colorResource(id = R.color.text)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "Cycling type",
                                fontSize = 16.sp,
                                lineHeight = 1.sp,
                                fontFamily = fontRns,
                                fontWeight = FontWeight.SemiBold,
                                color = if (state.selectedCyclingType.isEmpty())
                                    colorResource(id = R.color.text)
                                else
                                    colorResource(id = R.color.background_apps)
                            )
                        }
                    }   // close filter cycling type
                    if (state.showCyclingTypeFilter) {
                        ModalBottomSheet(
                            onDismissRequest = { onShowCyclingTypeFilter() },
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        ) {
                            CyclingTypeFilter(
                                cyclingType = state.selectedCyclingType,
                                onUpdateCyclingTypeFilter = { cyclingType ->
                                    onUpdateCyclingTypeFilter(
                                        cyclingType
                                    )
                                },
                                onShowCyclingTypeFilter = { onShowCyclingTypeFilter() }
                            )
                        }
                    }

                }   // close row filter
            }
        }   // close box

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
        ) {
            item{
                Spacer(Modifier.height(8.dp))
            }

            state.listUser.takeIf { it.isNotEmpty() }?.let { user ->
//                if (state.isSearchFilterEmpty == true) {
//                    item {
//                        Text(
//                            text = "People you may know",
//                            fontSize = 18.sp,
//                            fontFamily = fontRns,
//                            fontWeight = FontWeight.Bold,
//                            color = colorResource(id = R.color.text),
//                            modifier = Modifier.padding(bottom = 4.dp)
//                        )
//                    }
//                }

                items(user.size) { index ->
                    UserProfileCard(
                        detail = user[index],
                        onOpenUserProfile = { userId -> onOpenUserProfile(userId) },
                        onFollow = { userId -> onFollow(userId) },
                        onUnfollow = { userId -> onUnfollow(userId) }
                    )

                    if (index < user.size - 1) {
                        HorizontalDivider(
                            color = colorResource(id = R.color.secondary),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

}

@Preview
@Composable
fun SearchFriendsPreview() {
    EbikeTheme {
        SearchFriendsLayout(
            lazyListState = rememberLazyListState(),
            state = SearchFriendsState(
                listUser = remember {
                    mutableStateListOf(
                        ProfileData(
                            userId = 1,
                            username = "User 45"
                        )
                    )
                }
            ),
            onSearchKeywordChanged = {},
            onOpenUserProfile = {},
            onShowLocationFilter = {},
            onUpdateSelectedCities = {},
            onShowCyclingTypeFilter = {},
            onUpdateCyclingTypeFilter = {},
            onFollow = {},
            onUnfollow = {}
        )
    }
}