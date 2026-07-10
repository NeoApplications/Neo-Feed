/*
 * This file is part of Neo Feed
 * Copyright (c) 2022   Neo Feed Team
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
package com.saulhdev.feeder.data.content

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.saulhdev.feeder.ui.navigation.NavRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class StringPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    val icon: ImageVector,
    val key: Preferences.Key<String>,
    val dataStore: DataStore<Preferences>,
    val defaultValue: String = "",
    val onClick: (() -> Unit)? = null,
    val route: NavRoute? = null,
) : PrefDelegate<String>(titleId, summaryId, dataStore, key, defaultValue)

class StringSelectionPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    val icon: ImageVector,
    val key: Preferences.Key<String>,
    val dataStore: DataStore<Preferences>,
    val defaultValue: String = "",
    val entries: Map<String, String>
) : PrefDelegate<String>(titleId, summaryId, dataStore, key, defaultValue)

class StringSetPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    val icon: ImageVector,
    val key: Preferences.Key<Set<String>>,
    val dataStore: DataStore<Preferences>,
    val defaultValue: Set<String> = emptySet(),
    val route: NavRoute? = null,
    val onClick: (() -> Unit)? = null,
) : PrefDelegate<Set<String>>(titleId, summaryId, dataStore, key, defaultValue)

class BooleanPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    val icon: ImageVector,
    val key: Preferences.Key<Boolean>,
    val dataStore: DataStore<Preferences>,
    val defaultValue: Boolean = false,
) : PrefDelegate<Boolean>(titleId, summaryId, dataStore, key, defaultValue)

class FloatPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    val icon: ImageVector,
    val key: Preferences.Key<Float>,
    val dataStore: DataStore<Preferences>,
    val defaultValue: Float = 0f,
    val minValue: Float,
    val maxValue: Float,
    val steps: Int,
    val specialOutputs: ((Float) -> String) = Float::toString,
) : PrefDelegate<Float>(titleId, summaryId, dataStore, key, defaultValue)

abstract class PrefDelegate<T>(
    @StringRes var titleId: Int,
    @StringRes var summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<T>,
    private val defaultValue: T
) : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return runBlocking(Dispatchers.IO) {
            get().firstOrNull() ?: defaultValue
        }
    }

    fun getValue(): T {
        return runBlocking(Dispatchers.IO) {
            get().firstOrNull() ?: defaultValue
        }
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        return runBlocking(Dispatchers.IO) {
            set(value)
        }
    }

    fun setValue(value: T) {
        return runBlocking(Dispatchers.IO) {
            set(value)
        }
    }

    fun get(): Flow<T> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    private suspend fun set(value: T) {
        dataStore.edit { it[key] = value }
    }
}
