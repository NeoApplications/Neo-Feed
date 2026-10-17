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

package com.saulhdev.feeder.ui.pages

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.content.AppThemePref
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.data.content.StringPref
import com.saulhdev.feeder.data.content.StringSelectionPref
import com.saulhdev.feeder.data.content.StringTextPref
import com.saulhdev.feeder.data.weather.OWMWeatherProvider
import com.saulhdev.feeder.data.weather.WeatherRepository
import com.saulhdev.feeder.ui.components.ViewWithActionBar
import com.saulhdev.feeder.ui.components.dialog.BaseDialog
import com.saulhdev.feeder.ui.components.dialog.StringSelectionPrefDialogUI
import com.saulhdev.feeder.ui.components.dialog.StringTextPrefDialogUI
import com.saulhdev.feeder.ui.components.dialog.ThemePrefDialogUI
import com.saulhdev.feeder.ui.components.preferences.PreferenceGroup
import com.saulhdev.feeder.utils.LocationHelper
import org.koin.compose.koinInject

@Composable
fun PreferencesPage(
    prefs: FeedPreferences = koinInject(),
    locationHelper: LocationHelper = koinInject(),
    weatherRepo: WeatherRepository = koinInject(),
) {
    val context = LocalContext.current
    val title = stringResource(id = R.string.title_settings)

    var hasLocationPermission by remember { mutableStateOf(locationHelper.hasLocationPermission()) }
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            hasLocationPermission = granted
            if (granted) {
                weatherRepo.refreshWeather(force = true)
            }
        }

    val servicePrefs =
        listOf(
            prefs.itemsPerFeed,
            prefs.syncFrequency,
            prefs.syncRange,
            prefs.syncOnlyOnWifi,
            prefs.openInBrowser,
            prefs.offlineReader,
            prefs.removeDuplicates,
        )
    val filterPrefs = listOf(prefs.blockedWords)
    val themePrefs = listOf(prefs.appTheme)
    val isWeatherEnabled by prefs.weatherProvider.getState()
    val selectedWeatherProvider by prefs.weatherProvider.getState2()

    val weatherPrefs =
        listOfNotNull(
            prefs.weatherProvider,
            if (
                isWeatherEnabled && selectedWeatherProvider == OWMWeatherProvider::class.java.name
            ) {
                prefs.owmWeatherApiKey
            } else null,
            prefs.weatherUnit,
            prefs.weatherCity,
        )
    val debugPrefs = listOf(prefs.about)

    val openDialog = remember { mutableStateOf(false) }
    var dialogPref by remember { mutableStateOf<Any?>(null) }
    val onPrefDialog = { pref: Any ->
        dialogPref = pref
        openDialog.value = true
    }

    ViewWithActionBar(
        title = title,
        showBackButton = false,
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier.padding(
                    start = 8.dp,
                    end = 8.dp,
                    top = paddingValues.calculateTopPadding(),
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = R.string.pref_cat_overlay) {
                PreferenceGroup(
                    stringResource(id = R.string.pref_cat_overlay),
                    prefs = themePrefs,
                    onPrefDialog = onPrefDialog,
                )
            }
            item(key = R.string.title_service) {
                PreferenceGroup(
                    stringResource(id = R.string.title_service),
                    prefs = servicePrefs,
                    onPrefDialog = onPrefDialog,
                )
            }
            item(key = R.string.pref_cat_filters) {
                PreferenceGroup(
                    stringResource(id = R.string.pref_cat_filters),
                    prefs = filterPrefs,
                    onPrefDialog = onPrefDialog,
                )

                if (!Settings.canDrawOverlays(context)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(modifier = Modifier.padding(horizontal = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = stringResource(R.string.draw_permission_required))
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}"),
                                        )
                                    )
                                }
                            ) {
                                Text(text = stringResource(R.string.go_to_settings))
                            }
                        }
                    }
                }
            }
            item(key = R.string.pref_cat_weather) {
                PreferenceGroup(
                    stringResource(id = R.string.pref_cat_weather),
                    prefs = weatherPrefs,
                    onPrefDialog = onPrefDialog,
                )

                if (
                    isWeatherEnabled &&
                        prefs.weatherCity.getValue().isBlank() &&
                        !hasLocationPermission
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(modifier = Modifier.padding(horizontal = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.weather_location_permission_required)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                        )
                                    )
                                }
                            ) {
                                Text(text = stringResource(R.string.weather_enable_location))
                            }
                        }
                    }
                }
            }
            item(key = R.string.title_other) {
                PreferenceGroup(
                    stringResource(id = R.string.title_other),
                    prefs = debugPrefs,
                    onPrefDialog = onPrefDialog,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    var showCityDialog by remember { mutableStateOf(false) }
    prefs.weatherCity.onClick = {
        showCityDialog = true
    }

    if (showCityDialog) {
        CityInputDialog(
            pref = prefs.weatherCity,
            onRequestLocationPermission = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    )
                )
            },
            onDismiss = { showCityDialog = false },
        )
    }

    if (openDialog.value) {
        BaseDialog(openDialogCustom = openDialog) {
            when (dialogPref) {
                is StringSelectionPref ->
                    StringSelectionPrefDialogUI(
                        pref = dialogPref as StringSelectionPref,
                        openDialogCustom = openDialog,
                    )

                is StringTextPref ->
                    StringTextPrefDialogUI(
                        pref = dialogPref as StringTextPref,
                        openDialogCustom = openDialog,
                    )

                is AppThemePref ->
                    ThemePrefDialogUI(
                        pref = dialogPref as AppThemePref,
                        transparencyPref = prefs.overlayTransparency,
                        openDialogCustom = openDialog,
                    )
            }
        }
    }
}

@Composable
private fun CityInputDialog(
    pref: StringPref,
    onRequestLocationPermission: () -> Unit,
    onDismiss: () -> Unit,
) {
    var textValue by remember { mutableStateOf(pref.getValue()) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = pref.titleId))
        },
        text = {
            Column {
                Text(
                    text = stringResource(id = R.string.pref_weather_city_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = { Text(stringResource(id = R.string.pref_weather_city)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = textValue.trim()
                    pref.setValue(trimmed)
                    if (trimmed.isEmpty()) {
                        onRequestLocationPermission()
                    }
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = android.R.string.cancel))
            }
        },
    )
}
