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
package com.saulhdev.feeder.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.databinding.ContentSortingBinding
import com.saulhdev.feeder.databinding.ContentSourcesBinding
import com.saulhdev.feeder.databinding.ContentTagsBinding
import com.saulhdev.feeder.databinding.SortFilterSheetBinding
import com.saulhdev.feeder.viewmodels.SortFilterViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

@SuppressLint("ViewConstructor")
class FilterBottomSheet(
    context: Context,
    private val callback: () -> Unit
) : FrameLayout(context), View.OnClickListener {

    private val mainScope = CoroutineScope(Dispatchers.Main + Job())
    private val viewModel: SortFilterViewModel by inject(SortFilterViewModel::class.java)
    private val prefs: FeedPreferences by inject(FeedPreferences::class.java)
    private var _binding: SortFilterSheetBinding? = null
    private val binding get() = _binding!!
    private var sortingBinding: ContentSortingBinding
    private var sourcesBinding: ContentSourcesBinding
    private var tagsBinding: ContentTagsBinding

    init {
        _binding = SortFilterSheetBinding.inflate(LayoutInflater.from(context), this, true)

        sortingBinding = ContentSortingBinding.inflate(LayoutInflater.from(context))
        binding.sortingCard.setContentView(sortingBinding.root)

        sourcesBinding = ContentSourcesBinding.inflate(LayoutInflater.from(context))
        binding.sourcesCard.setContentView(sourcesBinding.root)

        tagsBinding = ContentTagsBinding.inflate(LayoutInflater.from(context))
        binding.tagsCard.setContentView(tagsBinding.root)

        binding.btnApply.setOnClickListener(this)
        binding.btnReset.setOnClickListener(this)
        tagsBinding.btnSelectAllTags.setOnClickListener(this)
        tagsBinding.btnDeselectAllTags.setOnClickListener(this)

        sourcesBinding.btnSelectAllSources.setOnClickListener(this)
        sourcesBinding.btnDeselectAllSources.setOnClickListener(this)

        getSources()
        getAllTags()
    }

    private fun getAllTags() {
        mainScope.launch {
            val selectedTags = prefs.tagsFilter.getValue().toCollection(ArrayList())
            viewModel.sheetState.collect { state ->
                tagsBinding.allTagsGroup.removeAllViews()
                state.activeTags.forEach { tagName ->
                    val chip = createChip(
                        chipName = tagName,
                        checked = selectedTags.contains(tagName),
                        callback = { isChecked ->
                            if (isChecked) {
                                selectedTags.add(tagName)
                            } else {
                                selectedTags.remove(tagName)
                            }
                            updateTagButtonsVisibility()
                        }
                    )
                    tagsBinding.allTagsGroup.addView(chip)
                }
                updateTagButtonsVisibility()
            }
        }
    }

    private fun getSources() {
        mainScope.launch {
            val selectedSources = prefs.sourcesFilter.getValue().toCollection(ArrayList())
            viewModel.sheetState.collect { state ->
                val sources = state.activeSources.map { it.title }
                sourcesBinding.allSourcesGroup.removeAllViews()
                sources.forEach { sourceName ->
                    val chip = createChip(
                        chipName = sourceName,
                        checked = selectedSources.contains(sourceName),
                        callback = { isChecked ->
                            if (isChecked) {
                                selectedSources.add(sourceName)
                            } else {
                                selectedSources.remove(sourceName)
                            }
                        }
                    )
                    sourcesBinding.allSourcesGroup.addView(chip)
                }
            }
        }
    }

    private fun createChip(chipName: String, checked: Boolean, callback: (Boolean) -> Unit): Chip {
        return Chip(context).apply {
            text = chipName
            isCheckable = true
            isChecked = checked
            checkedIcon = AppCompatResources.getDrawable(context, R.drawable.ic_check_24)
            checkedIconTint = getColorStateList()
            chipIcon = if (isChecked) {
                AppCompatResources.getDrawable(context, R.drawable.ic_check_24)
            } else {
                AppCompatResources.getDrawable(context, R.drawable.ic_circle_24dp)
            }
            chipStrokeColor = getColorStateList()
            chipStrokeWidth = 1f
            setOnCheckedChangeListener { _, isChecked ->
                chipIcon = if (isChecked) {
                    AppCompatResources.getDrawable(context, R.drawable.ic_check_24)
                } else {
                    AppCompatResources.getDrawable(context, R.drawable.ic_circle_24dp)
                }
                callback(isChecked)
            }
        }
    }

    private fun getColorStateList(): android.content.res.ColorStateList? {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(
            com.google.android.material.R.attr.colorPrimaryFixed,
            typedValue,
            true
        )
        return ContextCompat.getColorStateList(context, typedValue.resourceId)
    }

    private fun updateTagButtonsVisibility() {
        val anyChecked = tagsBinding.allTagsGroup.children
            .filterIsInstance<Chip>()
            .any { it.isChecked }
        tagsBinding.btnSelectAllTags.visibility = if (anyChecked) GONE else VISIBLE
        tagsBinding.btnDeselectAllTags.visibility = if (anyChecked) VISIBLE else GONE
    }

    private fun updateSourcesButtonsVisibility() {
        val anyChecked = sourcesBinding.allSourcesGroup.children
            .filterIsInstance<Chip>()
            .any { it.isChecked }
        sourcesBinding.btnSelectAllSources.visibility = if (anyChecked) GONE else VISIBLE
        sourcesBinding.btnDeselectAllSources.visibility = if (anyChecked) VISIBLE else GONE
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_apply                -> {
                val selectedSources = sourcesBinding.allSourcesGroup.children
                    .filterIsInstance<Chip>()
                    .filter { it.isChecked }
                    .map { it.text.toString() }
                    .toSet()

                val selectedTags = tagsBinding.allTagsGroup.children
                    .filterIsInstance<Chip>()
                    .filter { it.isChecked }
                    .map { it.text.toString() }
                    .toSet()

                val sortingFilter = sortingBinding.toggleSortDirection.checkedButtonId
                val sortingOption = when (sortingFilter) {
                    R.id.btn_sort_asc -> true
                    R.id.btn_sort_desc -> false
                    else -> true
                }

                val sorting = sortingBinding.cgSortOptions.children
                    .filterIsInstance<Chip>()
                    .filter { it.isChecked }
                    .map { it.text.toString() }
                    .firstOrNull() ?: context.getString(R.string.sorting_chronological)

                prefs.sourcesFilter.setValue(selectedSources)
                prefs.tagsFilter.setValue(selectedTags)
                prefs.sortingAsc.setValue(sortingOption)
                prefs.sortingFilter.setValue(sorting)

                callback()
            }

            R.id.btn_reset                -> {
                prefs.sourcesFilter.setValue(emptySet())
                prefs.tagsFilter.setValue(emptySet())
                prefs.sortingAsc.setValue(false)
                prefs.sortingFilter.setValue(context.getString(R.string.sorting_chronological))

                sourcesBinding.allSourcesGroup.children
                    .filterIsInstance<Chip>()
                    .forEach { it.isChecked = false }
                tagsBinding.allTagsGroup.children
                    .filterIsInstance<Chip>()
                    .forEach { it.isChecked = false }
                sortingBinding.cgSortOptions.check(R.id.chip_sort_chronological)
                sortingBinding.toggleSortDirection.check(R.id.btn_sort_desc)

                updateTagButtonsVisibility()
                callback()
            }

            R.id.btn_deselect_all_tags    -> {
                tagsBinding.allTagsGroup.children
                    .filterIsInstance<Chip>()
                    .forEach { it.isChecked = false }
                updateTagButtonsVisibility()
            }

            R.id.btn_select_all_tags      -> {
                tagsBinding.allTagsGroup.children
                    .filterIsInstance<Chip>()
                    .forEach { it.isChecked = true }
                updateTagButtonsVisibility()
            }

            R.id.btn_deselect_all_sources -> {
                sourcesBinding.allSourcesGroup.children
                    .filterIsInstance<Chip>()
                    .forEach { it.isChecked = false }
                updateSourcesButtonsVisibility()
            }

            R.id.btn_select_all_sources   -> {
                sourcesBinding.allSourcesGroup.children
                    .filterIsInstance<Chip>()
                    .forEach { it.isChecked = true }
                updateSourcesButtonsVisibility()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _binding = null
    }

    companion object {
        fun show(context: Context, animate: Boolean) {
            val sheet = BaseBottomSheet.inflate(context)
            sheet.show(FilterBottomSheet(context) { sheet.close(true) }, animate)
        }
    }
}