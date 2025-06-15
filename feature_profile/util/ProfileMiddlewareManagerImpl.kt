package com.polygonbikes.ebike.v3.feature_profile.util

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.clear
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import com.polygonbikes.ebike.feature_me.util.profilemanager.ProfileManagerImpl
import com.polygonbikes.ebike.feature_me.util.profilemanager.ProfileManagerImpl.Companion
import com.polygonbikes.ebike.v3.feature_profile.domain.model.UserProfileMiddleware
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import kotlinx.serialization.encodeToString

class ProfileMiddlewareManagerImpl @Inject constructor(context: Context) : ProfileMiddlewareManager {
    val dataStore: DataStore<Preferences> = context.createDataStore(
        name = PROFILE_MIDDLEWARE_MANAGER,
        migrations = listOf(SharedPreferencesMigration(context, PROFILE_MIDDLEWARE_MANAGER))
    )

    override suspend fun userId(): Long? {
        return dataStore.data.first()[KEY_USER_ID]
    }

    override val userProfileFlow: Flow<UserProfileMiddleware> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserProfileMiddleware(
                userId = preferences[KEY_USER_ID],
                username = preferences[KEY_USERNAME] ?: "",
                location = preferences[KEY_LOCATION] ?: "",
                cyclingStyle = preferences[KEY_CYCLING_STYLE]?.let { Json.decodeFromString(it) } ?: emptyList(),
                email = preferences[KEY_EMAIL] ?: "",
                phoneNumber = preferences[KEY_PHONE_NUMBER] ?: ""
            )
        }

    override suspend fun updateData(userId: Long?, username: String?, location: String?, cyclingStyle: List<String>?, email: String?, phoneNumber: String?) {
        dataStore.edit { preferences ->
            userId?.let { preferences[KEY_USER_ID] = it }
            username?.let { preferences[KEY_USERNAME] = it }
            location?.let { preferences[KEY_LOCATION] = it }
            cyclingStyle?.let { preferences[KEY_CYCLING_STYLE] = Json.encodeToString(it) }
            email?.let { preferences[KEY_EMAIL] = it }
            phoneNumber?.let { preferences[KEY_PHONE_NUMBER] = it }
        }
    }

    override suspend fun clearData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        private val KEY_USER_ID = preferencesKey<Long>("user_id")
        private val KEY_USERNAME = preferencesKey<String>("username")
        private val KEY_LOCATION = preferencesKey<String>("location")
        private val KEY_CYCLING_STYLE = preferencesKey<String>("cycling_style")
        private val KEY_EMAIL = preferencesKey<String>("email")
        private val KEY_PHONE_NUMBER = preferencesKey<String>("phone_number")
        private const val PROFILE_MIDDLEWARE_MANAGER = "profile_middleware_manager"
    }
}