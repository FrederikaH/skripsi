package com.polygonbikes.ebike.v3.feature_profile.presentation.my_account


sealed class MyAccountEvent {
    object OpenChangeNickname : MyAccountEvent()
    object OpenChangeCyclingStyle : MyAccountEvent()
    object OpenChangeLocation : MyAccountEvent()
    object InitDataChanges : MyAccountEvent()
    object DeleteAccount : MyAccountEvent()
}