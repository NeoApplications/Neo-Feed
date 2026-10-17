package com.saulhdev.feeder.ui.pages

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saulhdev.feeder.R
import com.saulhdev.feeder.manager.models.exportBookmarks
import com.saulhdev.feeder.manager.models.exportOpml
import com.saulhdev.feeder.manager.models.importBookmarks
import com.saulhdev.feeder.manager.models.importOpml
import com.saulhdev.feeder.ui.components.OverflowMenu
import com.saulhdev.feeder.ui.components.SegmentedTabButton
import com.saulhdev.feeder.ui.components.ViewWithActionBar
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.BookBookmark
import com.saulhdev.feeder.ui.icons.phosphor.Bookmarks
import com.saulhdev.feeder.ui.icons.phosphor.CloudArrowDown
import com.saulhdev.feeder.ui.icons.phosphor.CloudArrowUp
import com.saulhdev.feeder.ui.icons.phosphor.PuzzlePiece
import com.saulhdev.feeder.ui.icons.phosphor.RssSimple
import com.saulhdev.feeder.utils.ApplicationCoroutineScope
import com.saulhdev.feeder.utils.FILE_DATETIME_FORMAT
import com.saulhdev.feeder.utils.extensions.koinNeoViewModel
import com.saulhdev.feeder.viewmodels.SourcesPluginsViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.koin.java.KoinJavaComponent.inject
import kotlin.time.Clock

@Composable
fun SourcesPluginsPage(viewModel: SourcesPluginsViewModel = koinNeoViewModel()) {
    val pageTab = rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current
    val coroutineScope: ApplicationCoroutineScope by inject(ApplicationCoroutineScope::class.java)
    val localTime =
        Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .format(FILE_DATETIME_FORMAT)
    val state by viewModel.state.collectAsStateWithLifecycle()

    val opmlExporter =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/opml")
        ) { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    context.contentResolver.exportOpml(
                        uri,
                        state.tagsSourcesMap,
                    )
                }
            }
        }

    val opmlImporter =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    context.contentResolver.importOpml(uri)
                }
            }
        }

    val bookmarksExporter =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/opml")
        ) { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    context.contentResolver.exportBookmarks(
                        context,
                        uri,
                        state.bookmarked,
                    )
                }
            }
        }

    val bookmarksImporter =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    context.contentResolver.importBookmarks(
                        context,
                        uri,
                    )
                }
            }
        }

    ViewWithActionBar(
        title =
            stringResource(
                id =
                    if (pageTab.intValue == 0) R.string.title_sources
                    else R.string.plugins_and_accounts
            ),
        showBackButton = false,
        actions = {
            OverflowMenu {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Phosphor.CloudArrowDown,
                            contentDescription = stringResource(id = R.string.sources_import_opml),
                        )
                    },
                    onClick = {
                        hideMenu()
                        opmlImporter.launch(
                            arrayOf(
                                "text/plain",
                                "text/xml",
                                "text/opml",
                                "*/*",
                            )
                        )
                    },
                    text = { Text(text = stringResource(id = R.string.sources_import_opml)) },
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Phosphor.CloudArrowUp,
                            contentDescription = stringResource(id = R.string.sources_export_opml),
                        )
                    },
                    onClick = {
                        hideMenu()
                        opmlExporter.launch("NF-${localTime}.opml")
                    },
                    text = { Text(text = stringResource(id = R.string.sources_export_opml)) },
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Phosphor.Bookmarks,
                            contentDescription =
                                stringResource(id = R.string.sources_import_bookmarks),
                        )
                    },
                    onClick = {
                        hideMenu()
                        bookmarksImporter.launch(
                            arrayOf(
                                "text/plain",
                                "text/xml",
                                "text/bkm",
                                "*/*",
                            )
                        )
                    },
                    text = { Text(text = stringResource(id = R.string.sources_import_bookmarks)) },
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Phosphor.BookBookmark,
                            contentDescription =
                                stringResource(id = R.string.sources_export_bookmarks),
                        )
                    },
                    onClick = {
                        hideMenu()
                        bookmarksExporter.launch("NF-${localTime}.bkm")
                    },
                    text = { Text(text = stringResource(id = R.string.sources_export_bookmarks)) },
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.background(Color.Transparent).fillMaxSize().padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier =
                    Modifier.padding(top = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = MaterialTheme.shapes.extraLarge,
                        )
                        .padding(horizontal = 4.dp)
            ) {
                SegmentedTabButton(
                    text = stringResource(id = R.string.title_sources),
                    icon = Phosphor.RssSimple,
                    selected = {
                        pageTab.intValue == 0
                    },
                    onClick = {
                        pageTab.intValue = 0
                    },
                )
                SegmentedTabButton(
                    text = stringResource(id = R.string.accounts_label),
                    icon = Phosphor.PuzzlePiece,
                    selected = {
                        pageTab.intValue == 1
                    },
                    onClick = {
                        pageTab.intValue = 1
                    },
                )
            }

            when (pageTab.intValue) {
                0 -> {
                    SourceListPage()
                }
                1 -> {
                    PluginsPage()
                }
            }
        }
    }
}
