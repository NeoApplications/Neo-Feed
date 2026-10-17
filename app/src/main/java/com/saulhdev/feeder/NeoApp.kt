package com.saulhdev.feeder

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.multidex.MultiDexApplication
import androidx.work.WorkManager
import com.google.android.material.color.DynamicColors
import com.jakewharton.threetenabp.AndroidThreeTen
import com.saulhdev.feeder.data.account.AccountStorage
import com.saulhdev.feeder.data.content.FeedPreferences.Companion.prefsModule
import com.saulhdev.feeder.data.db.NeoFeedDb
import com.saulhdev.feeder.data.repository.ArticleRepository
import com.saulhdev.feeder.data.repository.SourcesRepository
import com.saulhdev.feeder.data.source.NewsSourceRegistry
import com.saulhdev.feeder.data.weather.OWMWeatherProvider
import com.saulhdev.feeder.data.weather.OpenMeteoProvider
import com.saulhdev.feeder.data.weather.WeatherRepository
import com.saulhdev.feeder.manager.localrss.SyncRestClient
import com.saulhdev.feeder.manager.mastodon.MastodonApi
import com.saulhdev.feeder.manager.mastodon.MastodonAuth
import com.saulhdev.feeder.manager.mastodon.MastodonStorage
import com.saulhdev.feeder.manager.miniflux.MinifluxClient
import com.saulhdev.feeder.manager.nextcloud.NextcloudNewsClient
import com.saulhdev.feeder.manager.service.OverlayBridge
import com.saulhdev.feeder.utils.AndroidResourceProvider
import com.saulhdev.feeder.utils.ApplicationCoroutineScope
import com.saulhdev.feeder.utils.LocationHelper
import com.saulhdev.feeder.utils.ResourceProvider
import com.saulhdev.feeder.utils.Utilities.Companion.userAgent
import com.saulhdev.feeder.utils.extensions.ToastMaker
import com.saulhdev.feeder.utils.extensions.restartApp
import com.saulhdev.feeder.viewmodels.ArticleListViewModel
import com.saulhdev.feeder.viewmodels.ArticleViewModel
import com.saulhdev.feeder.viewmodels.MastodonAuthViewModel
import com.saulhdev.feeder.viewmodels.PluginsViewModel
import com.saulhdev.feeder.viewmodels.SearchFeedViewModel
import com.saulhdev.feeder.viewmodels.SortFilterViewModel
import com.saulhdev.feeder.viewmodels.SourceEditViewModel
import com.saulhdev.feeder.viewmodels.SourceListViewModel
import com.saulhdev.feeder.viewmodels.SourcesPluginsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androix.startup.KoinStartup
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.GlobalContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.TimeUnit

@OptIn(KoinExperimentalAPI::class)
class NeoApp : MultiDexApplication(), KoinStartup {
    val activityHandler = ActivityHandler()
    private val applicationCoroutineScope = ApplicationCoroutineScope()
    private val wm: WorkManager by inject(WorkManager::class.java)

    private fun savedStateHandle() = SavedStateHandle()

    private val modelModule = module {
        single {
            savedStateHandle()
        }
        viewModelOf(::SourceEditViewModel)
        viewModelOf(::SearchFeedViewModel)
        viewModelOf(::ArticleListViewModel)
        viewModelOf(::SourceListViewModel)
        viewModelOf(::SourcesPluginsViewModel)
        viewModelOf(::ArticleViewModel)
        viewModelOf(::SortFilterViewModel)
        viewModelOf(::MastodonAuthViewModel)
        viewModelOf(::PluginsViewModel)
    }

    // TODO Move to its class
    private val dataModule = module {
        single<NeoFeedDb> { NeoFeedDb.getInstance(this@NeoApp) }
        single { get<NeoFeedDb>().feedArticleDao() }
        single { get<NeoFeedDb>().feedSourceDao() }
        single { get<NeoFeedDb>().syncQueueDao() }
        singleOf(::ArticleRepository)
        singleOf(::SourcesRepository)
        singleOf(::SyncRestClient)
        singleOf(::MastodonStorage)
        singleOf(::MastodonAuth)
        singleOf(::MastodonApi)
        singleOf(::AccountStorage)
        singleOf(::NextcloudNewsClient)
        singleOf(::MinifluxClient)
        singleOf(::NewsSourceRegistry)
        single { OpenMeteoProvider() }
        single { OWMWeatherProvider() }
        single { LocationHelper(this@NeoApp) }
        singleOf(::WeatherRepository)
    }

    private val coreModule = module {
        single { contentResolver }
        single { WorkManager.getInstance(this@NeoApp) }
        single<ResourceProvider> { AndroidResourceProvider(androidContext()) }
        single<ToastMaker> {
            object : ToastMaker {
                override suspend fun makeToast(text: String) = withContext(Dispatchers.Main) {
                    Toast.makeText(get(), text, Toast.LENGTH_SHORT).show()
                }

                override suspend fun makeToast(resId: Int) = withContext(Dispatchers.Main) {
                    Toast.makeText(get(), resId, Toast.LENGTH_SHORT).show()
                }
            }
        }
        single { applicationCoroutineScope }
        single<NeoApp> { this@NeoApp }
        single<OkHttpClient> {
            val cacheDir = java.io.File(cacheDir, "http_cache")
            val cacheSize = 20L * 1024L * 1024L
            OkHttpClient.Builder()
                .cache(Cache(cacheDir, cacheSize))
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", userAgent)
                        .build()
                    chain.proceed(request)
                }
                .build()
        }
    }

    fun onAppStarted() {
        registerActivityLifecycleCallbacks(activityHandler)
    }

    @KoinExperimentalAPI
    override fun onKoinStartup() = koinConfiguration {
        androidLogger()
        androidContext(this@NeoApp)
        modules(coreModule, prefsModule, dataModule, modelModule)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // TODO remove on future release
        AndroidThreeTen.init(this)
        DynamicColors.applyToActivitiesIfAvailable(
            this,
            com.google.android.material.color.DynamicColorsOptions.Builder()
                .setPrecondition { _, _ -> DynamicColors.isDynamicColorAvailable() }
                .build()
        )
        wm.pruneWork()
        onAppStarted()
    }

    override fun onTerminate() {
        super.onTerminate()
        GlobalContext.get().close()
    }

    fun restart(recreate: Boolean = false) {
        if (recreate) {
            activityHandler.finishAll(true)
        } else {
            restartApp()
        }
    }

    companion object {
        private const val TAG = "NeoFeed"

        @JvmStatic
        var instance: NeoApp? = null
            private set

        val bridge = OverlayBridge()
    }
}

class ActivityHandler : ActivityLifecycleCallbacks {
    val activities = HashSet<Activity>()
    var foregroundActivity: Activity? = null
    private var startedActivities = 0


    fun finishAll(recreateApp: Boolean = true) {
        HashSet(activities).forEach { if (recreateApp) it.recreate() else it.finish() }
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity == foregroundActivity) {
            foregroundActivity = null
        }
    }

    override fun onActivityResumed(activity: Activity) {
        foregroundActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        startedActivities += 1
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity == foregroundActivity)
            foregroundActivity = null
        activities.remove(activity)
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivities = (startedActivities - 1).coerceAtLeast(0)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activities.add(activity)
    }
}