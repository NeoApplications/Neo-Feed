package com.saulhdev.feeder.manager.service

class OverlayBridge {
    private var callback: OverlayBridgeCallback? = null

    fun setCallback(callback: OverlayBridgeCallback?) {
        this.callback = callback
    }

    interface OverlayBridgeCallback {
        fun applyNewTheme(value: String)
        fun applyCompactCard(value: Boolean)
        fun applyNewTransparency(value: Float)
        fun onClientMessage(action: String)
    }
}