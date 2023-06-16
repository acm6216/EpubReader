package cen.xiaoyuan.epub.ui.interfaces

import android.view.View
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener

abstract class SimpleDrawerListener:DrawerListener {
    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

    override fun onDrawerOpened(drawerView: View) {}

    override fun onDrawerClosed(drawerView: View) {}
}