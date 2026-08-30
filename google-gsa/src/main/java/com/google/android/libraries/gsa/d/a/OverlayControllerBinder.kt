package com.google.android.libraries.gsa.d.a

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.util.Pair
import android.view.WindowManager.LayoutParams
import com.google.android.libraries.launcherclient.ILauncherOverlayCallback
import com.google.android.libraries.launcherclient.LauncherOverlayInterfaceBinder

class OverlayControllerBinder(
    private val overlaysController: OverlaysController,
    val callerUid: Int,
    val packageName: String,
    val serverVersion: Int,
    val clientVersion: Int
) : LauncherOverlayInterfaceBinder(), Runnable {
    private var baseCallback: BaseCallback = BaseCallback()
    private var mainThreadHandler = Handler(Looper.getMainLooper(), baseCallback)
    private var callback: ILauncherOverlayCallback? = null
    var options: Int = 0
    private var lastAttachWasLandscape: Boolean = false

    private fun checkCallerId() {
        if (getCallingUid() != callerUid) {
            throw RuntimeException("Invalid client")
        }
    }


    override fun startScroll() {
        checkCallerId()
        Message.obtain(this.mainThreadHandler, 3).sendToTarget()
    }

    override fun endScroll() {
        checkCallerId()
        Message.obtain(this.mainThreadHandler, 5).sendToTarget()
    }

    override fun onScroll(progress: Float) {
        checkCallerId()
        Message.obtain(this.mainThreadHandler, 4, progress).sendToTarget()
    }

    override fun onPause() {
        closeOverlay(0)
    }

    override fun onResume() {
        closeOverlay(3)
    }

    override fun openOverlay(options: Int) {
        checkCallerId()
        this.mainThreadHandler.removeMessages(6)
        Message.obtain(this.mainThreadHandler, 6, 1, options).sendToTarget()
    }

    override fun closeOverlay(options: Int) {
        checkCallerId()
        mainThreadHandler.removeMessages(6)
        Message.obtain(mainThreadHandler, 6, 0, options).sendToTarget()
    }

    override fun requestVoiceDetection(start: Boolean) {
        checkCallerId()
        Message.obtain(this.mainThreadHandler, 0, 0, 0).sendToTarget()
        this.overlaysController.handler.postDelayed(this, (if (start) 5000 else 0).toLong())
    }

    @Synchronized
    override fun windowAttached(attrs: LayoutParams, callbacks: ILauncherOverlayCallback, options: Int) {
        Log.d("Google gsa", "windowAttached called with options: $options")
        val bundle = Bundle().apply {
            putParcelable("layout_params", attrs)
            putInt("client_options", options)
        }
        checkCallerId()
        overlaysController.handler.removeCallbacks(this)
        val configuration: Configuration? = Configuration()
        lastAttachWasLandscape = configuration?.orientation == 2
        callback = callbacks
        BL(bundle.getInt("client_options", 7))
        Message.obtain(mainThreadHandler, 0, 1, 0, Pair.create(bundle, callback)).sendToTarget()
    }

    @Synchronized
    override fun windowAttached(bundle: Bundle?, callback: ILauncherOverlayCallback?) {
        checkCallerId()
        this.overlaysController.handler.removeCallbacks(this)
        val configuration = bundle!!.getParcelable<Configuration?>("configuration")
        lastAttachWasLandscape = configuration != null && configuration.orientation == 2
        BL(bundle.getInt("client_options", 7))
        Message.obtain(
            this.mainThreadHandler,
            0,
            1,
            0,
            Pair.create<Bundle?, ILauncherOverlayCallback?>(bundle, callback)
        ).sendToTarget()
    }

    @Synchronized
    fun BL(i: Int) {
        val newOptions = if ((i and 11) != 0 && (i and 1) != 0) 1 else i and 11
        if (options != newOptions) {
            mainThreadHandler.removeCallbacksAndMessages(null)
            Message.obtain(mainThreadHandler, 0, 0, 0).sendToTarget()
            of(true)
            options = newOptions
            baseCallback = if (options == 1) {
                MinusOneOverlayCallback(overlaysController, this)
            } else {
                BaseCallback()
            }
            mainThreadHandler = Handler(Looper.getMainLooper(), baseCallback)
        }
    }


    override fun windowDetached(isChangingConfigurations: Boolean) {
        var i = 0
        synchronized(this) {
            checkCallerId()
            this.mainThreadHandler.removeMessages(7)
            val handler: Handler? = this.mainThreadHandler
            if (isChangingConfigurations) {
                i = 1
            }
            Message.obtain(handler, 7, i, 0).sendToTarget()
        }
    }

    override fun run() {
        destroy()
    }

    @Synchronized
    fun destroy() {
        synchronized(overlaysController) {
            overlaysController.handler.removeCallbacks(this)
            of(false)
        }
    }
    @Synchronized
    private fun of(isChangingConfigurations: Boolean) {
        Message.obtain(mainThreadHandler, 11, if (isChangingConfigurations) 1 else 0, 0).sendToTarget()
    }

    fun a(callback: ILauncherOverlayCallback?, i: Int) {
        callback?.let {
            try {
                it.overlayStatusChanged(overlaysController.defaultVersion or i)
            } catch (_: Throwable) {
                // Ignored as per original
            }
        }
    }
}