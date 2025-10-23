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

package com.saulhdev.feeder.manager.models

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.utils.extensions.ToastMaker
import com.saulhdev.feeder.manager.sync.requestFeedSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject
import kotlin.system.measureTimeMillis

/**
 * Exports OPML on a background thread
 */
suspend fun ContentResolver.exportOpml(uri: Uri, tagsFeedMap: Map<String, List<Feed>>) =
    withContext(Dispatchers.IO) {
        try {
            val time = measureTimeMillis {
                takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                openOutputStream(uri)?.let {
                    writeOutputStream(
                        it,
                        tagsFeedMap
                    )
                }
            }
            Log.d("OPML", "Exported OPML in $time ms on ${Thread.currentThread().name}")
        } catch (e: Throwable) {
            Log.e("OPML", "Failed to export OPML", e)
            val toastMaker: ToastMaker by inject(ToastMaker::class.java)
            toastMaker.makeToast(R.string.failed_to_export_OPML)
            (e.localizedMessage ?: e.message)?.let { message ->
                toastMaker.makeToast(message)
            }
        }
    }

/**
 * Imports OPML on a background thread
 */
suspend fun ContentResolver.importOpml(uri: Uri) = withContext(Dispatchers.IO) {
    try {
        val time = measureTimeMillis {
            val parser = OPMLParser(OPMLToRoom())

            openInputStream(uri).use {
                it?.let { stream ->
                    parser.parseInputStream(stream)
                }
            }
            requestFeedSync()
        }
        Log.d("OPML", "Imported OPML in $time ms on ${Thread.currentThread().name}")
    } catch (e: Throwable) {
        Log.e("OPML", "Failed to import OPML", e)
        val toastMaker: ToastMaker by inject(ToastMaker::class.java)
        toastMaker.makeToast(R.string.failed_to_import_OPML)
        (e.localizedMessage ?: e.message)?.let { message ->
            toastMaker.makeToast(message)
        }
    }
}
