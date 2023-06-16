package cen.xiaoyuan.epub.ui.excerpt

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import cen.xiaoyuan.epub.databinding.FragmentExcerptBinding
import cen.xiaoyuan.epub.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExcerptFragment: BaseFragment<FragmentExcerptBinding>() {

    override fun setBinding() = FragmentExcerptBinding.inflate(layoutInflater)

    private val excerptAdapter = ExcerptAdapter{
        excerpt.toggleExpandStatus(it)
    }

    private val excerpt: ExcerptViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.excerptAdapter = excerptAdapter
        binding.lifecycleOwner = this
        binding.executePendingBindings()
    }

    override fun launchFlow() {
        repeatWithViewLifecycle {
            launch {
                excerpt.excerpt.collect {
                    excerptAdapter.submitList(it)
                }
            }
        }
    }

}