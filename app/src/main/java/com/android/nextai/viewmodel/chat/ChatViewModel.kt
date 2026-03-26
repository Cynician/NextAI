package com.android.nextai.viewmodel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.nextai.domain.remote.Model
import com.android.nextai.domain.repository.ChatRepository
import com.android.nextai.viewmodel.chat.entity.EmptyMessage
import com.android.nextai.viewmodel.chat.entity.Message
import com.android.nextai.viewmodel.chat.entity.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    val messageHolder: ChatMessageHolder,
    val uiStateHolder: ChatUiStateHolder,
    private val chatRepository: ChatRepository,
) : ViewModel() {
    companion object {
        private const val TAG = "ChatViewModel"
    }

    fun sendUserMessage(currentPrompt: String) {
        currentPrompt.trim()
        messageHolder.updateCurPrompt(currentPrompt)

        val userMessage = Message(
            msgId = System.currentTimeMillis(),
            role = Role.User,
            content = currentPrompt
        )
        messageHolder.messageList.add(userMessage)

        messageHolder.updateLastUserMarkdownElementCnt(
            messageHolder.totalMarkdownElementCnt.value +
                    userMessage.markdownElements.size
        )
        messageHolder.messageList.add(EmptyMessage)

        messageHolder.updateIsGenerating(true)
        viewModelScope.launch {
            val assistantAnswer = chatRepository.getAIAnswer(Model.QIANWEN, messageHolder.messageList)
            val assistantMessage = Message(
                msgId = System.currentTimeMillis(),
                role = Role.Assistant,
                content = assistantAnswer
            )
            messageHolder.messageList.add(assistantMessage)
            messageHolder.messageList.add(EmptyMessage)
            messageHolder.updateIsGenerating(false)

            messageHolder.updateTotalMarkdownElementCnt(
                userMessage.markdownElements.size +
                        assistantMessage.markdownElements.size + 2
            )
            Log.d(TAG, "bubbleList size : ${messageHolder.messageList.size}")
        }



    }
}