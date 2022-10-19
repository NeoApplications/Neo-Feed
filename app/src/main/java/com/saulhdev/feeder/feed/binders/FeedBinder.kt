package com.saulhdev.feeder.feed.binders

import android.util.SparseIntArray
import android.view.View
import ua.itaysonlab.hfsdk.FeedItem

interface FeedBinder {
    fun bind(theme: SparseIntArray?, item: FeedItem, view: View)
}