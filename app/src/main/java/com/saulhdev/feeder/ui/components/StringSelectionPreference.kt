package com.saulhdev.feeder.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.saulhdev.feeder.data.content.StringSelectionPref

@Composable
fun StringSelectionPreference(
    modifier: Modifier = Modifier,
    pref: StringSelectionPref,
    index: Int = 1,
    groupSize: Int = 1,
    isEnabled: Boolean = true,
    onClick: (() -> Unit) = {},
) {
    val summary by remember { derivedStateOf { pref.entries[pref.getValue()] } }
    BasePreference(
        modifier = modifier,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = summary,
        index = index,
        groupSize = groupSize,
        startWidget = {
            Icon(
                imageVector = pref.icon,
                contentDescription = stringResource(id = pref.titleId),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        isEnabled = isEnabled,
        onClick = onClick
    )
}