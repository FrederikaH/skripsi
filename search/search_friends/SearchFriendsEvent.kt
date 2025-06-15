package com.polygonbikes.ebike.v3.feature_home.presentation.search.search_friends

sealed class SearchFriendsEvent {
    data class OpenUserProfile(val userId: Int) : SearchFriendsEvent()

    object InitDataChanges : SearchFriendsEvent()
    data class SearchKeywordChanged(val keyword: String) : SearchFriendsEvent()

    object  ShowLocationFilter : SearchFriendsEvent()
    data class UpdateSelectedCities(val selectedCities: List<String>) : SearchFriendsEvent()

    object ShowCyclingTypeFilter : SearchFriendsEvent()
    data class UpdateCyclingTypeFilter(val cyclingType: List<String>) : SearchFriendsEvent()

    data class Follow(val userId: Int) : SearchFriendsEvent()
    data class Unfollow(val userId: Int) : SearchFriendsEvent()
}