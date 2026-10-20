package com.saulhdev.feeder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.lifecycle.asLiveData
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.invoke
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.data.weather.WeatherRepository
import com.saulhdev.feeder.manager.localrss.FeedSyncer
import com.saulhdev.feeder.ui.navigation.AppNavDisplay
import com.saulhdev.feeder.ui.navigation.NAV_BASE
import com.saulhdev.feeder.ui.navigation.NavRoute
import com.saulhdev.feeder.ui.navigation.Routes
import com.saulhdev.feeder.ui.theme.AppTheme
import com.saulhdev.feeder.utils.extensions.isDarkTheme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.serializer
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class MainActivity : ComponentActivity() {
    private val deepLinkChannel = Channel<Intent>(Channel.UNLIMITED)
    private val prefs: FeedPreferences by inject(FeedPreferences::class.java)
    private val weatherRepo: WeatherRepository by inject(WeatherRepository::class.java)

    private val deepLinkMatchers: List<UriDeepLinkMatcher<NavRoute>> =
        listOf(
            UriDeepLinkMatcher(
                uriPattern = DeepLinkUri("$NAV_BASE${Routes.MAIN}/{page}"),
                serializer = serializer<NavRoute.Main>(),
            ),
            UriDeepLinkMatcher(
                uriPattern = DeepLinkUri("nf-mastodon://callback?code={code}&state={state}"),
                serializer = serializer<NavRoute.MastodonCallback>(),
            ),
            UriDeepLinkMatcher(
                uriPattern = DeepLinkUri("$NAV_BASE${Routes.WEB_VIEW}/{url}"),
                serializer = serializer<NavRoute.WebView>(),
            ),
            UriDeepLinkMatcher(
                uriPattern = DeepLinkUri("$NAV_BASE${Routes.ARTICLE_VIEW}/{uuid}"),
                serializer = serializer<NavRoute.ArticleView>(),
            ),
        )

    override fun onResume() {
        super.onResume()
        weatherRepo.refreshWeather(force = false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val backStack = rememberNavBackStack(*initBackStack(intent).toTypedArray())

            LaunchedEffect(Unit) {
                for (navIntent in deepLinkChannel) {
                    val route = handleDeepLink(navIntent) ?: continue
                    if (backStack.firstOrNull() !is NavRoute.Main) {
                        backStack.add(0, NavRoute.Main())
                    }
                    backStack.add(route)
                }
            }

            TransparentSystemBars()
            AppTheme(prefs.appTheme.getValue()) {
                AppNavDisplay(
                    modifier = Modifier.imePadding(),
                    backStack = backStack,
                )
            }
        }

        configurePeriodicSync()
        observePrefs()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkChannel.trySend(intent)
    }

    private fun handleDeepLink(intent: Intent?): NavRoute? {
        if (intent == null) return null
        val request = DeepLinkRequest(intent)
        return deepLinkMatchers.mapNotNull { it.match(request) }.maxOrNull()?.key
    }

    private fun initBackStack(intent: Intent?): List<NavRoute> =
        when (val route = handleDeepLink(intent)) {
            null -> listOf(NavRoute.Main())
            is NavRoute.Main -> listOf(route)
            else -> listOf(NavRoute.Main(), route)
        }

    @Composable
    fun TransparentSystemBars() {
        DisposableEffect(isDarkTheme, prefs.appTheme.getValue()) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                ) { isDarkTheme },
                navigationBarStyle = SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                ) { isDarkTheme },
            )
            onDispose {}
        }
    }

    private fun observePrefs() {
        val oldTheme = prefs.appTheme.getValue()
        val oldTransparency = prefs.overlayTransparency.getValue()

        prefs.appTheme.get().asLiveData().observe(this) {
            if (it != oldTheme) {
                recreate()
            }
        }
        prefs.overlayTransparency.get().asLiveData().observe(this) {
            if (it != oldTransparency) {
                recreate()
            }
        }
    }

    private fun configurePeriodicSync() {
        val workManager = WorkManager.getInstance(this)
        val freqHours = prefs.syncFrequency.getValue().toDoubleOrNull() ?: 1.0
        val shouldSync = freqHours > 0
        val replace = true
        if (shouldSync) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)

            if (prefs.syncOnlyOnWifi.getValue()) {
                constraints.setRequiredNetworkType(NetworkType.UNMETERED)
            } else {
                constraints.setRequiredNetworkType(NetworkType.CONNECTED)
            }
            val timeInterval = maxOf(15L, (freqHours * 60).toLong())

            val workRequestBuilder = PeriodicWorkRequestBuilder<FeedSyncer>(
                timeInterval,
                TimeUnit.MINUTES,
            )

            val syncWork = workRequestBuilder
                .setConstraints(constraints.build())
                .addTag("PeriodicFeedSyncer")
                .build()

            workManager.enqueueUniquePeriodicWork(
                "feeder_periodic_3",
                when (replace) {
                    true  -> ExistingPeriodicWorkPolicy.UPDATE
                    false -> ExistingPeriodicWorkPolicy.KEEP
                },
                syncWork
            )

        } else {
            workManager.cancelUniqueWork("feeder_periodic_3")
        }
    }

    companion object {
        fun navigateIntent(context: Context, destination: String): Intent {
            val uri = "$NAV_BASE$destination".toUri()
            return Intent(Intent.ACTION_VIEW, uri, context, MainActivity::class.java)
        }

        private suspend fun start(
            activity: Activity,
            targetIntent: Intent,
            extras: Bundle
        ): ActivityResult {
            return suspendCancellableCoroutine { continuation ->
                val intent = Intent(activity, MainActivity::class.java)
                    .putExtras(extras)
                    .putExtra("intent", targetIntent)
                val resultReceiver = createResultReceiver {
                    if (continuation.isActive) {
                        continuation.resume(it)
                    }
                }
                activity.startActivity(intent.putExtra("callback", resultReceiver))
            }
        }

        private fun createResultReceiver(callback: (ActivityResult) -> Unit): ResultReceiver {
            return object : ResultReceiver(Handler(Looper.myLooper()!!)) {

                override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                    val data = Intent()
                    if (resultData != null) {
                        data.putExtras(resultData)
                    }
                    callback(ActivityResult(resultCode, data))
                }
            }
        }
    }
}