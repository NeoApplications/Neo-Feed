/*
 * This file is part of Neo Feed
 * Copyright (c) 2025   Neo Feed Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

@file:Suppress("DEPRECATION")

package com.saulhdev.feeder.manager.mastodon

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

@Suppress("DEPRECATION")
class MastodonStorage(context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveAppCredentials(instance: String, clientId: String, clientSecret: String) {
        prefs.edit()
            .putString(appClientIdKey(instance), clientId)
            .putString(appClientSecretKey(instance), clientSecret)
            .apply()
    }

    fun getAppCredentials(instance: String): Pair<String, String>? {
        val clientId = prefs.getString(appClientIdKey(instance), null) ?: return null
        val clientSecret = prefs.getString(appClientSecretKey(instance), null) ?: return null
        return clientId to clientSecret
    }

    fun savePendingState(state: String, instance: String) {
        prefs.edit()
            .putString(KEY_PENDING_STATE, state)
            .putString(KEY_PENDING_INSTANCE, instance)
            .apply()
    }

    fun getPendingInstance(state: String): String? {
        val pending = prefs.getString(KEY_PENDING_STATE, null)
        return if (pending == state) {
            prefs.getString(KEY_PENDING_INSTANCE, null)
        } else null
    }

    fun clearPendingState() {
        prefs.edit()
            .remove(KEY_PENDING_STATE)
            .remove(KEY_PENDING_INSTANCE)
            .apply()
    }

    fun saveAccessToken(instance: String, account: String, token: String) {
        prefs.edit()
            .putString(tokenKey(instance, account), token)
            .apply()
    }

    fun getAccessToken(instance: String, account: String): String? {
        return prefs.getString(tokenKey(instance, account), null)
    }

    fun deleteAccessToken(instance: String, account: String) {
        prefs.edit()
            .remove(tokenKey(instance, account))
            .apply()
    }

    private fun appClientIdKey(instance: String) = "app_${instance}_client_id"
    private fun appClientSecretKey(instance: String) = "app_${instance}_client_secret"
    private fun tokenKey(instance: String, account: String) = "token_${instance}_${account}"

    companion object {
        private const val PREFS_FILE = "mastodon_tokens"
        private const val KEY_PENDING_STATE = "pending_state"
        private const val KEY_PENDING_INSTANCE = "pending_instance"
    }
}
