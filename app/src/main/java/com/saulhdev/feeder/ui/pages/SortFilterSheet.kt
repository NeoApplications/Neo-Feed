package com.saulhdev.feeder.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.ui.components.ActionButton
import com.saulhdev.feeder.ui.components.ChipsSwitch
import com.saulhdev.feeder.ui.components.DeSelectAll
import com.saulhdev.feeder.ui.components.ExpandableItemsBlock
import com.saulhdev.feeder.ui.components.SelectChip
import com.saulhdev.feeder.ui.icons.Phosphor
import com.saulhdev.feeder.ui.icons.phosphor.ArrowUUpLeft
import com.saulhdev.feeder.ui.icons.phosphor.Check
import com.saulhdev.feeder.ui.icons.phosphor.SortAscending
import com.saulhdev.feeder.ui.icons.phosphor.SortDescending
import com.saulhdev.feeder.utils.extensions.koinNeoViewModel
import com.saulhdev.feeder.viewmodels.SortFilterViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.compose.koinInject

@OptIn(
    ExperimentalCoroutinesApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun SortFilterSheet(
    viewModel: SortFilterViewModel = koinNeoViewModel(),
    prefs: FeedPreferences = koinInject(),
    onDismiss: () -> Unit,
) {
    val nestedScrollConnection = rememberNestedScrollInteropConnection()
    val state by viewModel.sheetState.collectAsState()

    var sortPrefVar by prefs.sortingFilter
    var sortAscPrefVar by prefs.sortingAsc
    var sourcesPrefVar by prefs.sourcesFilter
    var tagsPrefVar by prefs.tagsFilter

    var sortOption by remember(state.sortFilter.sort) {
        mutableStateOf(state.sortFilter.sort)
    }
    var sortAscOption by remember(state.sortFilter.sortAsc) {
        mutableStateOf(state.sortFilter.sortAsc)
    }
    val sourcesOption = remember(state.sortFilter.sourcesFilter) {
        mutableStateListOf(*state.sortFilter.sourcesFilter.toTypedArray())
    }

    val tagsOption = remember(state.sortFilter.tagsFilter) {
        mutableStateListOf(*state.sortFilter.tagsFilter.toTypedArray())
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                HorizontalDivider(thickness = 2.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = R.string.action_reset),
                        icon = Phosphor.ArrowUUpLeft,
                        positive = false
                    ) {
                        sortPrefVar = prefs.sortingFilter.defaultValue
                        sortAscPrefVar = prefs.sortingAsc.defaultValue
                        sourcesPrefVar = prefs.sourcesFilter.defaultValue
                        tagsPrefVar = prefs.tagsFilter.defaultValue
                        onDismiss()
                    }
                    ActionButton(
                        text = stringResource(id = R.string.action_apply),
                        icon = Phosphor.Check,
                        modifier = Modifier.weight(1f),
                        positive = true,
                        onClick = {
                            sortPrefVar = sortOption
                            sortAscPrefVar = sortAscOption
                            sourcesPrefVar = sourcesOption.toSet()
                            tagsPrefVar = tagsOption.toSet()
                            onDismiss()
                        }
                    )
                }
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(
                    bottom = paddingValues.calculateBottomPadding(),
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                )
                .nestedScroll(nestedScrollConnection)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            item {
                ExpandableItemsBlock(
                    heading = stringResource(id = R.string.sorting_order),
                    preExpanded = true,
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        prefs.sortingFilter.entries.forEach {
                            SelectChip(
                                text = it.value,
                                checked = it.key == sortOption,
                                alwaysShowIcon = false,
                            ) {
                                sortOption = it.key
                            }
                        }
                    }
                    ChipsSwitch(
                        firstTextId = R.string.sort_ascending,
                        firstIcon = Phosphor.SortAscending,
                        secondTextId = R.string.sort_descending,
                        secondIcon = Phosphor.SortDescending,
                        firstSelected = sortAscOption,
                        onCheckedChange = { checked ->
                            sortAscOption = checked
                        }
                    )
                }
            }

            item {
                ExpandableItemsBlock(
                    heading = stringResource(id = R.string.title_sources),
                    preExpanded = sourcesOption.isNotEmpty(),
                ) {
                    DeSelectAll(state.activeSources.map { it.id.toString() }, sourcesOption)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.activeSources.sortedBy { it.title.lowercase() }.forEach {
                            val checked by remember(sourcesOption.toString()) {
                                mutableStateOf(!sourcesOption.contains(it.id.toString()))
                            }

                            SelectChip(
                                text = it.title,
                                checked = checked,
                            ) {
                                if (checked) sourcesOption.add(it.id.toString())
                                else sourcesOption.remove(it.id.toString())
                            }
                        }
                    }
                }
            }

            item {
                ExpandableItemsBlock(
                    heading = stringResource(id = R.string.source_tags),
                    preExpanded = tagsOption.isNotEmpty(),
                ) {
                    DeSelectAll(state.activeTags.map { it }, tagsOption)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.activeTags.sortedBy { it.lowercase() }.forEach {
                            val checked by remember(tagsOption.toString()) {
                                mutableStateOf(!tagsOption.contains(it))
                            }

                            SelectChip(
                                text = it,
                                checked = checked,
                            ) {
                                if (checked) tagsOption.add(it)
                                else tagsOption.remove(it)
                            }
                        }
                    }
                }
            }
        }
    }
}
