package com.alessandro.tedesco.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsStore @Inject constructor(
    private val context: Context
) {
    private object Keys {
        val FEED_URL = stringPreferencesKey("feed_url")
        val ETAG = stringPreferencesKey("etag")
        val LAST_SYNC = longPreferencesKey("last_sync")
        val POLL_MINUTES = intPreferencesKey("poll_minutes")
        val NOTIFY_HOUR = intPreferencesKey("notify_hour")
    }

    /** URL di default: quello del repo di contenuti dell'insegnante. */
    val DEFAULT_FEED_URL =
        "https://raw.githubusercontent.com/alessandro-tedesco/" +
            "vokabeln/main/vokabeln.json"

    val feedUrlFlow: Flow<String> =
        context.dataStore.data.map { it[Keys.FEED_URL] ?: DEFAULT_FEED_URL }

    val pollMinutesFlow: Flow<Int> =
        context.dataStore.data.map { it[Keys.POLL_MINUTES] ?: 15 }

    val notifyHourFlow: Flow<Int> =
        context.dataStore.data.map { it[Keys.NOTIFY_HOUR] ?: 19 }

    suspend fun feedUrl(): String =
        context.dataStore.data.first()[Keys.FEED_URL] ?: DEFAULT_FEED_URL

    suspend fun setFeedUrl(url: String) {
        context.dataStore.edit { it[Keys.FEED_URL] = url }
    }

    suspend fun etag(): String? =
        context.dataStore.data.first()[Keys.ETAG]?.takeIf { it.isNotBlank() }

    suspend fun setEtag(value: String) {
        context.dataStore.edit { it[Keys.ETAG] = value }
    }

    suspend fun setLastSync(ts: Long) {
        context.dataStore.edit { it[Keys.LAST_SYNC] = ts }
    }

    val lastSyncFlow: Flow<Long> =
        context.dataStore.data.map { it[Keys.LAST_SYNC] ?: 0L }

    suspend fun setPollMinutes(v: Int) {
        context.dataStore.edit { it[Keys.POLL_MINUTES] = v.coerceIn(15, 1440) }
    }

    suspend fun setNotifyHour(h: Int) {
        context.dataStore.edit { it[Keys.NOTIFY_HOUR] = h.coerceIn(0, 23) }
    }
}
