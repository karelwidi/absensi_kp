package id.co.psplauncher.data.local

import android.content.Context
import android.service.autofill.UserData
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import id.co.psplauncher.data.network.response.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "psp_mobile_data_store")

class UserPreferences @Inject constructor(@ApplicationContext context: Context){

    private val appContext = context.applicationContext

    val accessToken: Flow<String>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN] ?: ""
        }

    suspend fun saveAccessToken(accessToken: String) {
        appContext.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
        }
    }

    suspend fun saveUserName(name: String) {
        appContext.dataStore.edit { preferences ->
            preferences[USER_NAME] = name
        }
    }

    suspend fun clearAccessToken() {
        appContext.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
        }
    }
    suspend fun getUserName(): String {
        return appContext.dataStore.data
            .map { preferences ->
                preferences[USER_NAME] ?: ""
            }
            .first()
    }

    fun getAccessToken() = runBlocking(Dispatchers.IO) {
        accessToken.first()
    }

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val USER_NAME = stringPreferencesKey("user_name")
    }
}