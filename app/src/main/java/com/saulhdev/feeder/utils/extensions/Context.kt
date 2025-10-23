package com.saulhdev.feeder.utils.extensions

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import com.google.android.material.color.DynamicColors
import com.saulhdev.feeder.MainActivity
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.content.FeedPreferences
import org.koin.java.KoinJavaComponent.get
import kotlin.system.exitProcess

interface ToastMaker {
    suspend fun makeToast(text: String)
    suspend fun makeToast(@StringRes resId: Int)
}

fun Context.makeToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Context.restartApp() {
    val intent = Intent(this, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    startActivity(intent)
    exitProcess(0)
}

private fun Context.restartFeed() {
    val pm: PackageManager = packageManager
    var intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_HOME)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    val componentName: ComponentName = intent.resolveActivity(pm)
    if (this.packageName != componentName.packageName) {
        intent = pm.getLaunchIntentForPackage(this.packageName)!!
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    startActivity(intent)
    Log.d(this.javaClass.name, "restartFeed: $intent")

    // Create a pending intent so the application is restarted after System.exit(0) was called.
    // We use an AlarmManager to call this intent in 100ms
    val mPendingIntent: PendingIntent = PendingIntent.getActivity(
        this,
        0,
        intent,
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val mgr: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    mgr[AlarmManager.RTC, System.currentTimeMillis() + 100] = mPendingIntent

    // Kill the application
    exitProcess(0)
}

fun Context.launchView(url: String) {
    startActivity(
        Intent(
            Intent.ACTION_VIEW,
            url.toUri()
        )
    )
}

fun Context.shareIntent(url: String, title: String) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TITLE, title)
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, title)
    shareIntent.putExtra(Intent.EXTRA_TEXT, url)

    startActivity(Intent.createChooser(shareIntent, "Where to Send?"))
}


fun Context.setCustomTheme() {
    AppCompatDelegate.setDefaultNightMode(nightMode)
    if (!(isDynamicTheme && DynamicColors.isDynamicColorAvailable())) {
        setTheme(R.style.AppTheme)
    }
}

val Context.isDynamicTheme
    get() = listOf("auto_system", "auto_system_black")
        .contains(get<FeedPreferences>(FeedPreferences::class.java).overlayTheme.getValue())

val Context.nightMode
    get() = when (get<FeedPreferences>(FeedPreferences::class.java).overlayTheme.getValue()) {
        "light"         -> AppCompatDelegate.MODE_NIGHT_NO
        "dark", "black" -> AppCompatDelegate.MODE_NIGHT_YES
        else            -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

val Context.isDarkTheme: Boolean
    get() = when (get<FeedPreferences>(FeedPreferences::class.java).overlayTheme.getValue()) {
        "dark", "black"
            -> true

        "light"
            -> false

        else -> resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES // "auto_system"
    }

val Context.isBlackTheme: Boolean
    get() = when (get<FeedPreferences>(FeedPreferences::class.java).overlayTheme.getValue()) {
        "black", "auto_system_black"
            -> true

        else -> false
    }