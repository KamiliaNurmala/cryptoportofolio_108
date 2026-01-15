package com.example.cryptoportfolio.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val EMAIL_KEY = stringPreferencesKey("email")
    }

    val userIdFlow: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    val usernameFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USERNAME_KEY]
    }

    val emailFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[EMAIL_KEY]
    }

    suspend fun saveUser(userId: Int, username: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USERNAME_KEY] = username
            preferences[EMAIL_KEY] = email
        }
    }

    suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}