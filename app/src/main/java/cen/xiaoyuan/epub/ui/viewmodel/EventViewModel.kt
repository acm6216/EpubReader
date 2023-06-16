package cen.xiaoyuan.epub.ui.viewmodel

import androidx.lifecycle.ViewModel
import cen.xiaoyuan.epub.data.Book
import cen.xiaoyuan.epub.data.Chapter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class EventViewModel:ViewModel() {

    fun addEpub(){
        _addEpub.trySend(Unit)
    }
    private val _addEpub = Channel<Unit>(capacity = Channel.CONFLATED)
    val addEpub = _addEpub.receiveAsFlow()

}