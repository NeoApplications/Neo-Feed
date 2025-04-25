package com.saulhdev.feeder.manager.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.android.apps.gsa.nowoverlayservice.ConfigurationOverlayController
import com.google.android.libraries.gsa.d.a.OverlaysController

class DrawerOverlayService : Service() {
    private lateinit var overlaysController: OverlaysController
    override fun onCreate() {
        super.onCreate()
        this.overlaysController = ConfigurationOverlayController(this)
    }

    override fun onDestroy() {
        this.overlaysController.onDestroy()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = this.overlaysController.onBind(intent)

    override fun onUnbind(intent: Intent): Boolean {
        this.overlaysController.onUnbind(intent)
        return false
    }
}