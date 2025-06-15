package com.polygonbikes.ebike.v3.feature_route.presentation.search_result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.polygonbikes.ebike.R
import com.polygonbikes.ebike.core.util.UiEvent
import com.polygonbikes.ebike.ui.theme.fontRns
import com.polygonbikes.ebike.v3.feature_route.domain.state.RouteState
import com.polygonbikes.ebike.v3.feature_route.presentation.RouteViewModel
import com.polygonbikes.ebike.v3.feature_route.presentation.component.FriendsRoute

@Composable
fun RouteSearchResult(
    onNavigate: (UiEvent.Navigate) -> Unit,
    viewModel: RouteViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onNavigate(event)
                else -> Unit
            }
        }
    }

    val state by viewModel.state

//    if (FeatureList.UpdateWhenReachBottom) {
//        lazyListState.OnBottomReached(buffer = 0) {
//            if (stateRoute.listRoute.isNotEmpty()) {
//                viewModel.onEvent(RouteEvent.FetchNextRoute)
//            }
//        }
//    }

    RouteSearchResultLayout(
        state = state,
        isFriendRoute = false
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RouteSearchResultLayout(
    state: RouteState,
    isFriendRoute: Boolean
) {
    var showFilterRoute by remember { mutableStateOf(value = false) }
    Scaffold {
        //  main column
        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.background_apps))
                .fillMaxSize()
                .padding(
                    top = it.calculateTopPadding() + 16.dp,
                    start = it.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                    end = it.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                    bottom = it.calculateBottomPadding() + 8.dp
                )
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.08f)
                            .background(colorResource(id = R.color.background_apps)),
                        verticalArrangement = Arrangement.Center
                    ) {

                        // row filter
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            //  exercise chip
                            var selectedExercise by remember { mutableStateOf(false) }
                            FilterChip(
                                onClick = { selectedExercise = !selectedExercise },
                                selected = selectedExercise,
                                modifier = Modifier
                                    .height(32.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = colorResource(id = R.color.background_apps),
                                    labelColor = colorResource(id = R.color.text),
                                    selectedLabelColor = colorResource(id = R.color.background_apps),
                                    selectedContainerColor = colorResource(id = R.color.white)
                                ),
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.FitnessCenter,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = stringResource(id = R.string.purpose_exercise),
                                            fontSize = 14.sp,
                                            lineHeight = 1.sp,
                                            fontFamily = fontRns,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            )   // close exercise chip

                            //  Recreation chip
                            var selectedRecreation by remember { mutableStateOf(false) }
                            FilterChip(
                                onClick = {
                                    selectedRecreation = !selectedRecreation
                                }, // Toggle the selected state
                                selected = selectedRecreation,
                                modifier = Modifier
                                    .height(32.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = colorResource(id = R.color.background_apps),
                                    labelColor = colorResource(id = R.color.text),
                                    selectedLabelColor = colorResource(id = R.color.background_apps),
                                    selectedContainerColor = colorResource(id = R.color.white)
                                ),
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.recreation),
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = stringResource(id = R.string.purpose_recreation),
                                            fontSize = 14.sp,
                                            lineHeight = 18.sp,
                                            fontFamily = fontRns,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            )   // close recreation chip

                            //  Touring chip
                            var selectedTouring by remember { mutableStateOf(false) }
                            FilterChip(
                                onClick = { selectedTouring = !selectedTouring },
                                selected = selectedTouring,
                                modifier = Modifier
                                    .height(32.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = colorResource(id = R.color.background_apps),
                                    labelColor = colorResource(id = R.color.text),
                                    selectedLabelColor = colorResource(id = R.color.background_apps),
                                    selectedContainerColor = colorResource(id = R.color.white)
                                ),
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.touring),
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = stringResource(id = R.string.purpose_touring),
                                            fontSize = 14.sp,
                                            lineHeight = 18.sp,
                                            fontFamily = fontRns,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (selectedTouring) colorResource(id = R.color.background_apps) else colorResource(
                                                id = R.color.text
                                            )
                                        )
                                    }
                                }
                            )   // close touring chip

                            //  filter icon card
                            Card(
                                onClick = { showFilterRoute = true },
                                modifier = Modifier
                                    .height(32.dp)
                                    .wrapContentWidth(),
                                colors = CardDefaults.cardColors(colorResource(id = R.color.background_apps)),
                                border = BorderStroke(
                                    1.dp,
                                    color = colorResource(id = R.color.sub_text)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(start = 16.dp, end = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Tune,
                                        contentDescription = "",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    //  jika user menggunakan filter, maka muncul jumlah filter yg digunakan
                                    var filterCount by remember { mutableIntStateOf(0) }
                                    if (filterCount > 0) {
                                        Text(
                                            text = "3", //filterCount
                                            fontSize = 14.sp,
                                            lineHeight = 18.sp,
                                            fontFamily = fontRns,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colorResource(id = R.color.text),
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    } else {
                                        null
                                    }
                                }
                            }   // close filter icon card
                        }   // close row filter
                    }   // Box background filter
                    Spacer(modifier = Modifier.height(16.dp))

                    // Scrollable column
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.94f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        //  routes list
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            repeat(3) {
                                if (isFriendRoute) {
//                                    FriendsRoute()
                                } else {
//                                    RouteCard(
//                                        route = route,
//                                        onClick = { onOpenRouteDetail(route) }
//                                    )
                                }

                            }
                        }
                    }   // close scrollable column
                }
            }

        }   // close main column
    }   // close scaffold

//    if (showFilterRoute) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(color = colorResource(R.color.background_apps))
//        ) {
//            RouteFilter()
//        }
//    }

}

//@Composable
//@Preview
//fun RouteSearchResultPreview() {
//    EbikeTheme {
//        RouteSearchResultLayout(
//            isFriendRoute = false
//        )
//    }
//}