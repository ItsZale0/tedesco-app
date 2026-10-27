package com.alessandro.tedesco.data.remote

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.updateSettingsStore by preferencesDataStore(name = "update_settings")

/**
 * Preferenze dell'auto-update, separate dal profilo: valgono per l'app intera.
 * - autoControllo: controllo periodico (24h) + all'avvio
 * - autoDownload: scarica l'APK in background e avvisa quando è pronto
 */
class UpdateSettings(private val context: Context) {

    suspend fun autoControllo(): Boolean =
        context.updateSettingsStore.data.first()[AUTO_CONTROLLO] ?: true

    suspend fun autoDownload(): Boolean =
        context.updateSettingsStore.data.first()[AUTO_DOWNLOAD] ?: false

    suspend fun setAutoControllo(attivo: Boolean) {
        context.updateSettingsStore.edit { it[AUTO_CONTROLLO] = attivo }
    }

    suspend fun setAutoDownload(attivo: Boolean) {
        context.updateSettingsStore.edit { it[AUTO_DOWNLOAD] = attivo }
    }

    companion object {
        val AUTO_CONTROLLO = booleanPreferencesKey("auto_controllo")
        val AUTO_DOWNLOAD = booleanPreferencesKey("auto_download")
    }
}
