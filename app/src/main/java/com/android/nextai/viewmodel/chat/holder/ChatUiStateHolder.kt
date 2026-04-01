package com.android.nextai.viewmodel.chat.holder

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@ViewModelScoped
class ChatUiStateHolder @Inject constructor(
    @ApplicationContext context: Context,
) {
    // record items num in a lazy column, a assistant's answer can be split into multiple markdown items
    private val _totalMarkdownItemCnt = MutableStateFlow(0)
    val totalMarkdownItemCnt: MutableStateFlow<Int> = _totalMarkdownItemCnt
    fun updateTotalMarkdownItemCnt(num:Int){
        _totalMarkdownItemCnt.value += num
    }

    // record the latest location of user's markdown item, help to auto scroll the page
    private val _lastUserMarkdownItemCnt = MutableStateFlow(0)
    val lastUserMarkdownItemCnt: MutableStateFlow<Int> = _lastUserMarkdownItemCnt
    fun updateLastUserMarkdownItemCnt(num:Int){
        _lastUserMarkdownItemCnt.value = num
    }
}