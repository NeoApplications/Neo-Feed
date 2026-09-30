/*
 * This file is part of Neo Feed
 * Copyright (c) 2026   Neo Feed Team
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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.account.AccountConfig
import com.saulhdev.feeder.data.account.AccountType
import com.saulhdev.feeder.ui.components.preferences.PreferenceGroupHeading
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.ArrowCounterClockwise
import com.saulhdev.feeder.ui.icons.phosphor.Plus
import com.saulhdev.feeder.ui.pages.plugins.AddAccountSheet
import com.saulhdev.feeder.ui.pages.plugins.MastodonConfigDialog
import com.saulhdev.feeder.ui.pages.plugins.MinifluxConfigDialog
import com.saulhdev.feeder.ui.pages.plugins.NextcloudConfigDialog
import com.saulhdev.feeder.utils.extensions.koinNeoViewModel
import com.saulhdev.feeder.viewmodels.PluginsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginsPage(viewModel: PluginsViewModel = koinNeoViewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var editingNextcloudAccount by remember {
        mutableStateOf<AccountConfig.NextcloudNewsAccount?>(null)
    }
    var isCreatingNextcloud by remember { mutableStateOf(false) }

    var editingMastodonAccount by remember { mutableStateOf<AccountConfig.MastodonAccount?>(null) }
    var isCreatingMastodon by remember { mutableStateOf(false) }

    var editingMinifluxAccount by remember { mutableStateOf<AccountConfig.MinifluxAccount?>(null) }
    var isCreatingMiniflux by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                modifier = Modifier.padding(16.dp),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Icon(
                    imageVector = Phosphor.Plus,
                    contentDescription = stringResource(id = R.string.add_account),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.add_account))
            }
        },
    ) { _ ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(id = R.string.plugins_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            val nextcloudAccounts =
                uiState.accounts.filterIsInstance<AccountConfig.NextcloudNewsAccount>()
            if (nextcloudAccounts.isNotEmpty()) {
                item {
                    PreferenceGroupHeading(
                        heading = stringResource(id = R.string.service_nextcloud_news)
                    )
                }
                items(nextcloudAccounts, key = { it.id }) { account ->
                    AccountCard(
                        title = account.displayName,
                        subtitle = "${account.serverUrl} • ${account.syncIntervalMinutes}m",
                        ImageVector.vectorResource(id = R.drawable.nextcloud),
                        isEnabled = account.isEnabled,
                        isSyncing = uiState.isSyncing,
                        onSwitch = { viewModel.toggleAccountEnabled(account) },
                        onSyncNow = { viewModel.syncAccount(account, context) },
                        onClick = { editingNextcloudAccount = account },
                    )
                }
            }

            val mastodonAccounts =
                uiState.accounts.filterIsInstance<AccountConfig.MastodonAccount>()
            if (mastodonAccounts.isNotEmpty()) {
                item {
                    PreferenceGroupHeading(heading = stringResource(id = R.string.service_mastodon))
                }
                items(mastodonAccounts, key = { it.id }) { account ->
                    AccountCard(
                        title = account.displayName,
                        subtitle =
                            stringResource(
                                id = R.string.mastodon_instance,
                                account.instance,
                            ),
                        icon = ImageVector.vectorResource(id = R.drawable.mastodon),
                        isEnabled = account.isEnabled,
                        isSyncing = uiState.isSyncing,
                        onSwitch = { viewModel.toggleAccountEnabled(account) },
                        onSyncNow = { viewModel.syncAccount(account, context) },
                        onClick = { editingMastodonAccount = account },
                    )
                }
            }

            val minifluxAccounts =
                uiState.accounts.filterIsInstance<AccountConfig.MinifluxAccount>()
            if (minifluxAccounts.isNotEmpty()) {
                item {
                    PreferenceGroupHeading(heading = stringResource(id = R.string.service_miniflux))
                }
                items(minifluxAccounts, key = { it.id }) { account ->
                    AccountCard(
                        title = account.displayName,
                        subtitle = account.serverUrl,
                        icon = ImageVector.vectorResource(id = R.drawable.miniflux),
                        isEnabled = account.isEnabled,
                        isSyncing = uiState.isSyncing,
                        onSwitch = { viewModel.toggleAccountEnabled(account) },
                        onSyncNow = { viewModel.syncAccount(account, context) },
                        onClick = { editingMinifluxAccount = account },
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showAddSheet) {
        AddAccountSheet(
            sheetState = sheetState,
            onDismissRequest = { showAddSheet = false },
            onSelectAccountType = { type ->
                when (type) {
                    AccountType.NEXTCLOUD_NEWS -> isCreatingNextcloud = true
                    AccountType.MASTODON -> isCreatingMastodon = true
                    AccountType.MINIFLUX -> isCreatingMiniflux = true
                }
            },
        )
    }

    if (isCreatingNextcloud || editingNextcloudAccount != null) {
        NextcloudConfigDialog(
            account = editingNextcloudAccount,
            isTestingConnection = uiState.isTestingConnection,
            testConnectionMessage = uiState.testConnectionMessage,
            testConnectionSuccess = uiState.testConnectionSuccess,
            onTestConnection = { url, user, token ->
                viewModel.testNextcloudConnection(url, user, token)
            },
            onSave = { account ->
                viewModel.saveAccount(account, context)
                viewModel.clearTestConnectionState()
                editingNextcloudAccount = null
                isCreatingNextcloud = false
            },
            onDelete = { id ->
                viewModel.deleteAccount(id)
                viewModel.clearTestConnectionState()
                editingNextcloudAccount = null
                isCreatingNextcloud = false
            },
            onDismiss = {
                viewModel.clearTestConnectionState()
                editingNextcloudAccount = null
                isCreatingNextcloud = false
            },
        )
    }

    if (isCreatingMastodon || editingMastodonAccount != null) {
        MastodonConfigDialog(
            account = editingMastodonAccount,
            onSave = { acc ->
                viewModel.saveAccount(acc, context)
                editingMastodonAccount = null
                isCreatingMastodon = false
            },
            onDelete = { id ->
                viewModel.deleteAccount(id)
                editingMastodonAccount = null
                isCreatingMastodon = false
            },
            onDismiss = {
                editingMastodonAccount = null
                isCreatingMastodon = false
            },
        )
    }

    if (isCreatingMiniflux || editingMinifluxAccount != null) {
        MinifluxConfigDialog(
            account = editingMinifluxAccount,
            isTestingConnection = uiState.isTestingConnection,
            testConnectionMessage = uiState.testConnectionMessage,
            testConnectionSuccess = uiState.testConnectionSuccess,
            onTestConnection = { url, token ->
                viewModel.testMinifluxConnection(url, token)
            },
            onSave = { account ->
                viewModel.saveAccount(account, context)
                viewModel.clearTestConnectionState()
                editingMinifluxAccount = null
                isCreatingMiniflux = false
            },
            onDelete = { id ->
                viewModel.deleteAccount(id)
                viewModel.clearTestConnectionState()
                editingMinifluxAccount = null
                isCreatingMiniflux = false
            },
            onDismiss = {
                viewModel.clearTestConnectionState()
                editingMinifluxAccount = null
                isCreatingMiniflux = false
            },
        )
    }
}

@Composable
private fun AccountCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isEnabled: Boolean,
    isSyncing: Boolean,
    onSwitch: (Boolean) -> Unit,
    onSyncNow: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint =
                    if (isEnabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color =
                        if (isEnabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.outline,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = onSyncNow,
                enabled = isEnabled && !isSyncing,
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Phosphor.ArrowCounterClockwise,
                        contentDescription = stringResource(id = R.string.sync_now),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onSwitch,
            )
        }
    }
}
