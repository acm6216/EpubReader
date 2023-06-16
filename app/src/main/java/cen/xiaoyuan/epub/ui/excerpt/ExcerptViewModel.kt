package cen.xiaoyuan.epub.ui.excerpt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cen.xiaoyuan.epub.data.BookInfoUseCase
import cen.xiaoyuan.epub.utils.WhileViewSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExcerptViewModel @Inject constructor(
    bookInfoUseCase: BookInfoUseCase,
):ViewModel() {

    private val expanded = MutableStateFlow<List<Expand>>(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val notes = bookInfoUseCase.queryNotes().transformLatest { n ->
        if(!n.isNullOrEmpty()) {
            val exp = n.map { it.bookUri }.toSet().map { Expand(it, false) }
            val expands = expanded.value.map { it.uri }
            if (expanded.value.isEmpty()) expanded.emit(exp)
            else {
                val uri = exp.map { it.uri }
                val intersect = uri intersect expands.toSet()
                val subtract = uri subtract expands.toSet()
                expanded.emit(expanded.value.filter { it.uri in intersect } + subtract.map {
                    Expand(it, false)
                })
            }
        }
        emit(n)
    }

    fun toggleExpandStatus(header: ExcerptItem.ExcerptHeader){
        viewModelScope.launch {
            val target = expanded.value.first{ it.uri==header.uri }
            val index = expanded.value.indexOf(target)
            ArrayList<Expand>().also {
                it.addAll(expanded.value)
                it.remove(target)
                it.add(index,Expand(target.uri,!target.expanded))
                expanded.emit(it)
            }
        }
    }

    val excerpt = combine(notes,expanded){ note,expand ->
        if(expand.isNotEmpty() && note!=null) ExcerptCreator().execute(note, expand)
        else emptyList()
    }.stateIn(viewModelScope, WhileViewSubscribed, emptyList())

}