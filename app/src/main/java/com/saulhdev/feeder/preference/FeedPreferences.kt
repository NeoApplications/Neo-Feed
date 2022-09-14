/*
 * This file is part of Omega Feeder
 * Copyright (c) 2022   Saul Henriquez <henriquez.saul@gmail.com>
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

package com.saulhdev.feeder.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import com.saulhdev.feeder.R
import com.saulhdev.feeder.utils.getThemes
import kotlin.reflect.KProperty

class FeedPreferences(val context: Context) {
    var sharedPrefs: SharedPreferences =
        context.getSharedPreferences("com.saulhdev.feeder.prefs", Context.MODE_PRIVATE)
    private var doNothing = {}

    /*PREFERENCES*/
    var feedList = StringSetPref(
        key = "pref_feed_list",
        titleId = R.string.title_feed_list,
        summaryId = R.string.summary_feed_list,
        defaultValue = setOf(),
        onChange = doNothing
    )

    var overlayTheme = StringSelectionPref(
        key = "pref_overlay_theme",
        titleId = R.string.pref_ovr_theme,
        defaultValue = "",
        entries = getThemes(),
        onChange = doNothing
    )

    var developer = StringPref(
        key = "pref_developer",
        titleId = R.string.pref_tg_author,
        summaryId = R.string.about_developer,
        onChange = doNothing
    )

    var telegramChannel = StringPref(
        key = "pref_channel",
        titleId = R.string.pref_tg,
        summaryId = R.string.telegram_channel,
        onChange = doNothing
    )

    var sourceCode = StringPref(
        key = "pref_source_code",
        titleId = R.string.pref_git,
        summaryId = R.string.source_code_url,
        onChange = doNothing
    )

    /*HELPER CLASSES FOR PREFERENCES*/
    inner class StringPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: String = "",
        val onClick: (() -> Unit)? = null,
        onChange: () -> Unit = doNothing,
        val navRoute: String = ""
    ) : PrefDelegate<String>(key, titleId, summaryId, defaultValue, onChange) {
        override fun onGetValue(): String = sharedPrefs.getString(key, defaultValue)!!

        override fun onSetValue(value: String) {
            edit { putString(key, value) }
        }
    }

    inner class StringSetPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: Set<String>,
        val onClick: (() -> Unit)? = null,
        onChange: () -> Unit = doNothing
    ) : PrefDelegate<Set<String>>(key, titleId, summaryId, defaultValue, onChange) {
        override fun onGetValue(): Set<String> = sharedPrefs.getStringSet(getKey(), defaultValue)!!

        override fun onSetValue(value: Set<String>) {
            edit { putStringSet(getKey(), value) }
        }
    }

    open inner class StringSelectionPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: String = "",
        val entries: Map<String, String>,
        onChange: () -> Unit = doNothing
    ) : PrefDelegate<String>(key, titleId, summaryId, defaultValue, onChange) {
        override fun onGetValue(): String = sharedPrefs.getString(getKey(), defaultValue)!!
        override fun onSetValue(value: String) {
            edit { putString(getKey(), value) }
        }
    }

    abstract inner class PrefDelegate<T : Any>(
        val key: String,
        @StringRes val titleId: Int,
        @StringRes var summaryId: Int = -1,
        val defaultValue: T,
        private val onChange: () -> Unit
    ) {
        private var cached = false
        private lateinit var value: T

        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (!cached) {
                value = onGetValue()
                cached = true
            }
            return value
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            cached = false
            onSetValue(value)
        }

        abstract fun onGetValue(): T

        abstract fun onSetValue(value: T)

        protected inline fun edit(body: SharedPreferences.Editor.() -> Unit) {
            val editor = sharedPrefs.edit()
            body(editor)
            commitOrApply(editor, false)
        }

        internal fun getKey() = key

        private fun onValueChanged() {
            discardCachedValue()
            onChange.invoke()
        }

        private fun discardCachedValue() {
            if (cached) {
                cached = false
                value.let(::disposeOldValue)
            }
        }

        open fun disposeOldValue(oldValue: T) {}
    }

    fun commitOrApply(editor: SharedPreferences.Editor, commit: Boolean) {
        if (commit) {
            editor.commit()
        } else {
            editor.apply()
        }
    }
}