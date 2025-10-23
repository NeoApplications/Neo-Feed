package com.saulhdev.feeder.utils.extensions

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.component.getScopeId

open class NeoViewModel : ViewModel() {
    init {
        Log.d(this::class.toString(), "neoviewmodel@koinscope: ${getScopeId()}")
    }
}

@Composable
inline fun <reified T : ViewModel> koinNeoViewModel() = koinViewModel<T>(
    viewModelStoreOwner = LocalActivity.current as ComponentActivity
)