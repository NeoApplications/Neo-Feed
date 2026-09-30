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

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.entity.NeoTheme
import com.saulhdev.feeder.data.entity.SORT_CHRONOLOGICAL
import com.saulhdev.feeder.data.weather.OWMWeatherProvider
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.Asterisk
import com.saulhdev.feeder.ui.icons.phosphor.BookBookmark
import com.saulhdev.feeder.ui.icons.phosphor.BracketsSquare
import com.saulhdev.feeder.ui.icons.phosphor.Browser
import com.saulhdev.feeder.ui.icons.phosphor.Bug
import com.saulhdev.feeder.ui.icons.phosphor.CaretUp
import com.saulhdev.feeder.ui.icons.phosphor.Clock
import com.saulhdev.feeder.ui.icons.phosphor.CloudArrowDown
import com.saulhdev.feeder.ui.icons.phosphor.FunnelSimple
import com.saulhdev.feeder.ui.icons.phosphor.Hash
import com.saulhdev.feeder.ui.icons.phosphor.Info
import com.saulhdev.feeder.ui.icons.phosphor.Nut
import com.saulhdev.feeder.ui.icons.phosphor.SubtractSquare
import com.saulhdev.feeder.ui.icons.phosphor.Swatches
import com.saulhdev.feeder.ui.icons.phosphor.WifiHigh
import com.saulhdev.feeder.ui.navigation.NavRoute
import com.saulhdev.feeder.utils.Utilities
import com.saulhdev.feeder.utils.getItemsPerFeed
import com.saulhdev.feeder.utils.getSortingOptions
import com.saulhdev.feeder.utils.getSyncFrequency
import com.saulhdev.feeder.utils.getSyncRange
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import kotlin.math.roundToInt

class FeedPreferences private constructor(val context: Context) : KoinComponent {
    private val dataStore: DataStore<Preferences> by inject()
    /* Theme */
    val appTheme = AppThemePref(
        titleId = R.string.theme,
        icon = Phosphor.Swatches,
        key = APP_THEME,
        dataStore = dataStore,
        defaultValue = NeoTheme.DynamicSystem,
    )

    val overlayTransparency = FloatPref(
        titleId = R.string.pref_transparency,
        icon = Phosphor.SubtractSquare,
        key = OVERLAY_OPACITY,
        dataStore = dataStore,
        defaultValue = 1f,
        maxValue = 1f,
        minValue = 0f,
        steps = 100,
        specialOutputs = { "${(it * 100).roundToInt()}%" }
    )

    val openInBrowser = BooleanPref(
        titleId = R.string.pref_browser_theme,
        icon = Phosphor.Browser,
        key = OPEN_IN_BROWSER,
        dataStore = dataStore,
        defaultValue = false
    )

    val removeDuplicates = BooleanPref(
        titleId = R.string.pref_remove_duplicates,
        icon = Phosphor.FunnelSimple,
        key = REMOVE_DUPLICATES,
        dataStore = dataStore,
        defaultValue = true
    )

    val offlineReader = BooleanPref(
        titleId = R.string.pref_offline_reader,
        icon = Phosphor.BookBookmark,
        key = OFFLINE_READER,
        dataStore = dataStore,
        defaultValue = true
    )

    /* Sync */
    val syncOnlyOnWifi = BooleanPref(
        titleId = R.string.pref_sync_wifi,
        icon = Phosphor.WifiHigh,
        key = SYNC_ON_WIFI,
        dataStore = dataStore,
        defaultValue = true
    )

    val syncFrequency = StringSelectionPref(
        titleId = R.string.sync_interval,
        icon = Phosphor.Clock,
        key = SYNC_FREQUENCY,
        dataStore = dataStore,
        defaultValue = "1",
        entries = getSyncFrequency(context)
    )

    var syncRange = StringSelectionPref(
        titleId = R.string.pref_sync_range,
        icon = Phosphor.Clock,
        key = SYNC_RANGE,
        dataStore = dataStore,
        defaultValue = "1w",
        entries = getSyncRange(context)
    )

    val itemsPerFeed = StringSelectionPref(
        titleId = R.string.pref_items_per_feed,
        icon = Phosphor.Hash,
        key = ITEMS_PER_FEED,
        dataStore = dataStore,
        defaultValue = "25",
        entries = getItemsPerFeed()
    )

    val blockedWords = StringSetPref(
        titleId = R.string.pref_blocked_words,
        summaryId = R.string.pref_blocked_words_summary,
        icon = Phosphor.Hash,
        key = BLOCKED_WORDS,
        dataStore = dataStore,
        defaultValue = emptySet(),
        route = NavRoute.BlockedWords,
    )

    /* Weather */
    val weatherProvider = TwoStatePref(
        dataStore = dataStore,
        key1 = WEATHER_ENABLED,
        key2 = WEATHER_PROVIDER,
        icon = Phosphor.CloudArrowDown,
        titleId = R.string.pref_show_weather,
        summaryId = R.string.pref_show_weather_summary,
        defaultValue1 = true,
        defaultValue2 = OWMWeatherProvider::class.java.name,
        entries = Utilities.weatherProviders(context)
    )

    val owmWeatherApiKey = StringTextPref(
        dataStore = dataStore,
        key = WEATHER_OWM_API_KEY,
        icon = Phosphor.Nut,
        titleId = R.string.weather_api_key,
        defaultValue = "",
    )
    val weatherUnit = StringSelectionPref(
        titleId = R.string.pref_weather_unit,
        icon = Phosphor.Asterisk,
        key = WEATHER_UNIT,
        dataStore = dataStore,
        defaultValue = "celsius",
        entries = mapOf(
            "celsius" to context.getString(R.string.pref_weather_unit_celsius),
            "fahrenheit" to context.getString(R.string.pref_weather_unit_fahrenheit)
        )
    )

    val weatherCity = StringPref(
        titleId = R.string.pref_weather_city,
        summaryId = R.string.pref_weather_city_summary,
        icon = Phosphor.BracketsSquare,
        key = WEATHER_CITY,
        dataStore = dataStore,
        defaultValue = ""
    )

    val weatherPermissionDismissed = BooleanPref(
        titleId = R.string.weather_dismiss_permission,
        icon = Phosphor.CloudArrowDown,
        key = WEATHER_PERMISSION_DISMISSED,
        dataStore = dataStore,
        defaultValue = false
    )

    /* Others */
    val about = StringPref(
        titleId = R.string.title_about,
        icon = Phosphor.Info,
        key = ABOUT,
        dataStore = get(),
        route = NavRoute.About
    )

    val debugging = BooleanPref(
        titleId = R.string.debug_logcat_printing,
        defaultValue = false,
        icon = Phosphor.Bug,
        key = DEBUG,
        dataStore = dataStore,
    )

    /* Sort & Filter */
    val sourcesFilter = StringSetPref(
        titleId = R.string.title_sources,
        icon = Phosphor.Info,
        key = FILTER_SOURCES,
        dataStore = dataStore,
        defaultValue = emptySet(),
    )

    val tagsFilter = StringSetPref(
        titleId = R.string.source_tags,
        icon = Phosphor.Info,
        key = FILTER_TAGS,
        dataStore = dataStore,
        defaultValue = emptySet(),
    )

    val sortingFilter = StringSelectionPref(
        titleId = R.string.sorting_order,
        icon = Phosphor.Info,
        key = FILTER_SORT,
        dataStore = dataStore,
        defaultValue = SORT_CHRONOLOGICAL,
        entries = getSortingOptions(context),
    )

    val sortingAsc = BooleanPref(
        titleId = R.string.sorting_order,
        defaultValue = false,
        icon = Phosphor.CaretUp,
        key = FILTER_SORT_ASC,
        dataStore = dataStore,
    )

    companion object {
        val prefsModule = module {
            singleOf(::FeedPreferences)
            singleOf(::provideDataStore)
        }

        private fun provideDataStore(context: Context): DataStore<Preferences> {
            return PreferenceDataStoreFactory.create(
                produceFile = {
                    context.preferencesDataStoreFile("neo_feed")
                },
                migrations = listOf(
                    SharedPreferencesMigration(
                        context,
                        "com.saulhdev.neofeed.prefs"
                    )
                )
            )
        }

        val APP_THEME = stringPreferencesKey("pref_app_theme")
        val OVERLAY_OPACITY = floatPreferencesKey("pref_overlay_opacity")
        val OPEN_IN_BROWSER = booleanPreferencesKey("pref_open_browser")
        val REMOVE_DUPLICATES = booleanPreferencesKey("pref_remove_duplicates")
        val OFFLINE_READER = booleanPreferencesKey("pref_offline_reader")
        val SYNC_ON_WIFI = booleanPreferencesKey("pref_sync_only_wifi")
        val SYNC_FREQUENCY = stringPreferencesKey("pref_sync_frequency")
        val SYNC_RANGE = stringPreferencesKey("pref_sync_range")
        val ITEMS_PER_FEED = stringPreferencesKey("pref_items_per_feed")
        val BLOCKED_WORDS = stringSetPreferencesKey("pref_blocked_words")
        val ABOUT = stringPreferencesKey("pref_about")
        val DEBUG = booleanPreferencesKey("pref_debugging")
        val WEATHER_ENABLED = booleanPreferencesKey("pref_weather_enabled")
        val WEATHER_PROVIDER = stringPreferencesKey("pref_weather_provider")
        val WEATHER_PERMISSION_DISMISSED = booleanPreferencesKey("pref_weather_permission_dismissed")
        val WEATHER_OWM_API_KEY = stringPreferencesKey("pref_weather_owm_api")
        val WEATHER_UNIT = stringPreferencesKey("pref_weather_unit")
        val WEATHER_CITY = stringPreferencesKey("pref_weather_city")

        // Filter & Sort
        val FILTER_SOURCES = stringSetPreferencesKey("filter_sources")
        val FILTER_TAGS = stringSetPreferencesKey("filter_tags")
        val FILTER_SORT = stringPreferencesKey("filter_sorting")
        val FILTER_SORT_ASC = booleanPreferencesKey("filter_sorting_ascending")
    }
}