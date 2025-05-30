/*
 * This file is part of Neo Feed
 * Copyright (c) 2023   Saul Henriquez <henriquez.saul@gmail.com>
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

package com.saulhdev.feeder.ui.overlay

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.google.android.libraries.gsa.d.a.OverlayController
import com.saulhdev.feeder.MainActivity
import com.saulhdev.feeder.R
import com.saulhdev.feeder.ui.compose.theme.AppTheme
import com.saulhdev.feeder.ui.navigation.LocalNavController
import com.saulhdev.feeder.ui.pages.OverlayPage
import com.saulhdev.feeder.utils.extensions.isDarkTheme
import org.koin.java.KoinJavaComponent.inject

class ComposeOverlayView(val context: Context) :
    OverlayController(context, R.style.AppTheme, R.style.WindowTheme),
    SavedStateRegistryOwner, ViewModelStoreOwner,
    OnBackPressedDispatcherOwner, ActivityResultRegistryOwner {
    private lateinit var rootView: View
    private lateinit var composeView: ComposeView
    private lateinit var navController: NavHostController
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val onBackPressedDispatcher: OnBackPressedDispatcher
        get() = OnBackPressedDispatcher()

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val mainActivity: MainActivity by inject(MainActivity::class.java)

    init {
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        rootView = View.inflate(
            ContextThemeWrapper(this, R.style.AppTheme),
            R.layout.compose_overlay,
            container
        )
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        rootView.setViewTreeSavedStateRegistryOwner(this)
        rootView.setViewTreeLifecycleOwner(this)
        rootView.setViewTreeViewModelStoreOwner(this)
        rootView.setViewTreeOnBackPressedDispatcherOwner(this)
        composeView = rootView.findViewById(R.id.compose_view)
        composeView.setContent {
            navController = rememberNavController()
            AppTheme(
                darkTheme = context.isDarkTheme
            ) {
                CompositionLocalProvider(
                    LocalNavController provides navController
                ) {
                    OverlayPage(true)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    override fun onResume() {
        super.onResume()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override val viewModelStore: ViewModelStore
        get() = ViewModelStore()

    override val activityResultRegistry: ActivityResultRegistry
        get() = mainActivity.activityResultRegistry
    //get() = NFApplication.mainActivity!!.activityResultRegistry

}