/*
 * This file is part of Neo Feed
 * Copyright (c) 2026   NeoApplications Team
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

package com.saulhdev.feeder.ui.weather

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.data.weather.WeatherCode
import com.saulhdev.feeder.data.weather.WeatherData
import com.saulhdev.feeder.data.weather.WeatherRepository
import com.saulhdev.feeder.data.weather.WeatherState
import com.saulhdev.feeder.ui.components.RoundButton
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.X
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Composable
fun WeatherWidget(
    modifier: Modifier = Modifier,
    prefs: FeedPreferences = koinInject(),
    weatherRepo: WeatherRepository = koinInject(),
) {
    val showWeather by prefs.weatherProvider.get().collectAsState(initial = false)
    if (!showWeather) return
    val permissionDismissed by prefs.weatherPermissionDismissed.get().collectAsState(initial = true)

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (granted) {
                weatherRepo.refreshWeather(force = true)
            }
        }

    LaunchedEffect(Unit) {
        weatherRepo.refreshWeather(false)
    }

    val weatherState by weatherRepo.weatherState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    when (val state = weatherState) {
        is WeatherState.Success -> {
            WeatherSuccessCard(
                weather = state.weather,
                modifier = modifier,
                onClick = { showDialog = true },
            )
            if (showDialog) {
                WeatherDetailsDialog(
                    weather = state.weather,
                    onDismiss = { showDialog = false },
                )
            }
        }

        is WeatherState.Loading -> {
            WeatherLoadingCard(modifier = modifier)
        }

        is WeatherState.Error -> {
            WeatherErrorCard(
                modifier = modifier,
                onClick = { weatherRepo.refreshWeather(force = true) },
            )
        }

        is WeatherState.LocationPermissionRequired -> {
            if (!permissionDismissed) {
                WeatherPermissionCard(
                    modifier = modifier,
                    onGrantClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                            )
                        )
                    },
                    onDismiss = {
                        prefs.weatherPermissionDismissed.setValue(true)
                    },
                )
            }
        }

        is WeatherState.Idle -> {
            // Trigger initial load if needed
            weatherRepo.refreshWeather(false)
        }
    }
}

@Composable
fun WeatherSuccessCard(
    weather: WeatherData,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        onClick = onClick,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_weather_location),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = weather.cityName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text =
                        stringResource(
                            id = R.string.weather_max_min,
                            "${weather.maxTemp.roundToInt()}${weather.unit}",
                            "${weather.minTemp.roundToInt()}${weather.unit}",
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter =
                        painterResource(
                            id = WeatherCode.getIconRes(weather.weatherCode, weather.isDay)
                        ),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = Color.Unspecified,
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${weather.temperature.roundToInt()}${weather.unit}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text =
                            stringResource(id = WeatherCode.getDescriptionRes(weather.weatherCode)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_weather_humidity),
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = Color.Unspecified,
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${weather.humidity}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_weather_wind),
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = Color.Unspecified,
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${weather.windSpeed.roundToInt()} km/h",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherLoadingCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(id = R.string.weather_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WeatherErrorCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        onClick = onClick,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(id = R.string.weather_error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = stringResource(id = R.string.weather_tap_to_retry),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun WeatherPermissionCard(
    modifier: Modifier = Modifier,
    onGrantClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        onClick = onGrantClick,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_weather_location),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(id = R.string.weather_location_permission_required),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onGrantClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.weather_enable_location),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            RoundButton(
                icon = Phosphor.X,
                description = stringResource(id = R.string.weather_dismiss_permission),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onDismiss,
            )
        }
    }
}
