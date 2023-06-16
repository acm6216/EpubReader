package cen.xiaoyuan.epub.ui.reader

import android.content.Context
import androidx.recyclerview.widget.LinearSmoothScroller

class TopLinearSmoothScroller(context: Context) : LinearSmoothScroller(context) {
    override fun getVerticalSnapPreference(): Int = SNAP_TO_START
}