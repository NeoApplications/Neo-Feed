package ua.itaysonlab.homefeeder

import android.app.Notification
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.drawable.toDrawable
import com.saulhdev.feeder.BuildConfig
import ua.itaysonlab.hfsdk.HFPluginApplication
import ua.itaysonlab.homefeeder.pluginsystem.PluginFetcher
import ua.itaysonlab.homefeeder.utils.Logger
import ua.itaysonlab.homefeeder.utils.OverlayBridge

class HFApplication : HFPluginApplication() {
    override fun onCreate() {
        super.onCreate()
        Logger.log(
            "Application",
            "Starting HomeFeeder ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})..."
        )
        instance = this
        PluginFetcher.init(instance)
    }

    companion object {
        const val ACTION_MANAGE_LISTENERS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"

        lateinit var instance: HFApplication
        val bridge = OverlayBridge()

        fun getAppNameByPkg(pkg: String): CharSequence {
            val ai = try {
                instance.packageManager.getApplicationInfo(pkg, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
            return if (ai != null) instance.packageManager.getApplicationLabel(ai) else "Unknown"
        }

        fun getSmallIcon(notification: Notification, pkg: String): Drawable? {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    notification.smallIcon.loadDrawable(instance)
                } else {
                    instance.packageManager.getResourcesForApplication(pkg)
                        .getDrawable(notification.icon, null)
                }
            } catch (e: Exception) {
                instance.packageManager.getApplicationIcon(pkg)
            }
        }

        fun getSmallIcon(pkg: String): Drawable {
            return instance.packageManager.getApplicationIcon(pkg)
        }

        fun getLargeIcon(notification: Notification): BitmapDrawable? {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    notification.getLargeIcon().loadDrawable(instance) as? BitmapDrawable
                } else {
                    notification.largeIcon.toDrawable(instance.resources)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}