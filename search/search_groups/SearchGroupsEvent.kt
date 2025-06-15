package com.polygonbikes.ebike.v3.feature_home.presentation.search.search_groups

import com.polygonbikes.ebike.v3.feature_group.data.entities.GroupData

sealed class SearchGroupsEvent {
    data class OpenGroupsDetail(
        val group: GroupData
    ) : SearchGroupsEvent()

    object InitDataChanges : SearchGroupsEvent()
    data class SearchKeywordChanged(val keyword: String) : SearchGroupsEvent()

    object  ShowLocationFilter : SearchGroupsEvent()
    data class UpdateSelectedCities(val selectedCities: List<String>) : SearchGroupsEvent()

    object ShowCyclingTypeFilter : SearchGroupsEvent()
    data class UpdateCyclingTypeFilter(val cyclingType: List<String>) : SearchGroupsEvent()

    object ShowPurposeFilter : SearchGroupsEvent()
    data class UpdatePurposeFilter(val purpose: List<String>) : SearchGroupsEvent()
}