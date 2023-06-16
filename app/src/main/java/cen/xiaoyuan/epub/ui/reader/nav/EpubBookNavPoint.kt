package cen.xiaoyuan.epub.ui.reader.nav

import android.os.Bundle
import android.view.View
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.databinding.EpubNavPointBinding
import cen.xiaoyuan.epub.ui.BaseFragment
import cen.xiaoyuan.epub.ui.adapters.NavPointAdapter
import com.google.android.material.tabs.TabLayoutMediator

class EpubBookNavPoint: BaseFragment<EpubNavPointBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tabTexts = resources.getStringArray(R.array.nav_point_tab)
        binding.viewPager.offscreenPageLimit = 1
        binding.viewPager.adapter = NavPointAdapter(requireActivity())
        TabLayoutMediator(binding.tab,binding.viewPager,true,true){
                tab,position -> tab.text = tabTexts[position]
        }.attach()
    }

    override fun setBinding(): EpubNavPointBinding = EpubNavPointBinding.inflate(layoutInflater)
}