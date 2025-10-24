package com.saulhdev.feeder.ui.components.dialog

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.R

@Composable
fun DrawPermissionRequestDialog() {
    val context = LocalContext.current

    val askForDrawPermission = remember { mutableStateOf(false) }

    if (askForDrawPermission.value) BaseDialog(askForDrawPermission) {
        Card (Modifier.padding(30.dp)) { Column (horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.draw_permission_required), textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
            Button({
                askForDrawPermission.value = false
                context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
            }, Modifier.padding(20.dp))
            {
                Text(stringResource(R.string.go_to_settings))
            }
        } }
    }
}
