package cen.xiaoyuan.epub.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import cen.xiaoyuan.epub.ui.reader.nav.EpubBookCatalogue
import cen.xiaoyuan.epub.ui.reader.nav.EpubBookNote
import cen.xiaoyuan.epub.ui.reader.nav.EpubBookmark

class NavPointAdapter :FragmentStateAdapter{

    private val fragments = listOf(EpubBookCatalogue(), EpubBookmark(), EpubBookNote())

    constructor(fragmentActivity: FragmentActivity):super(fragmentActivity)
    constructor(fragment: Fragment):super(fragment)
    constructor(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle
    ):super(fragmentManager, lifecycle)

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}