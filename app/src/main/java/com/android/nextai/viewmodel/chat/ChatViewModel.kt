package com.android.nextai.viewmodel.chat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.nextai.domain.remote.Model
import com.android.nextai.domain.remote.entity.GenerationEvent
import com.android.nextai.domain.repository.ChatRepository
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import com.android.nextai.ui.component.markdown.entity.MarkdownType
import com.android.nextai.ui.component.markdown.utils.MarkdownNodeUtils
import com.android.nextai.viewmodel.chat.entity.Message
import com.android.nextai.viewmodel.chat.entity.Role
import com.android.nextai.viewmodel.chat.holder.ChatMessageHolder
import com.android.nextai.viewmodel.chat.holder.ChatUiStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID
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

    private var generationJob: Job? = null

    @Volatile
    private var currentMessageId: String? = null // current streaming message ID
    @Volatile
    private var currentType: MarkdownType = MarkdownType.UNKNOWN
    @Volatile
    private var currentBuffer = StringBuilder()
    @Volatile
    private var fullTextBuffer = StringBuilder()

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun sendUserMessage(currentPrompt: String) {
        val currentPrompt = currentPrompt.trim()
        messageHolder.updateCurPrompt(currentPrompt)
        val userMessage = Message(
            msgId = UUID.randomUUID().toString(),
            role = Role.User,
            markdown = MarkdownNode.Text(currentPrompt)
        )
        val messageList = messageHolder.messageList
        messageList.add(userMessage)

        messageHolder.updateIsGenerating(true)
        val isSportStreamingGen = true
        generationJob = viewModelScope.launch {
            if (isSportStreamingGen) {
                messageHolder.updateIsTextStreaming(true)
                chatRepository.streamingGen(model = Model.TEST,messageList = messageList).collect { event ->
                    when (event) {
                        is GenerationEvent.Word -> {
                            Log.d(TAG, event.content)
                            val text = event.content
                            if (currentMessageId == null) {
                                currentMessageId = UUID.randomUUID().toString()
                                Log.d(TAG, currentMessageId!!)
                                messageList.add(Message(
                                    msgId = currentMessageId!!,
                                    role = Role.Assistant,
                                    markdown = MarkdownNode.Text("")
                                ))
                            }
                            currentBuffer.append(text)

                            val nodes = MarkdownNodeUtils.parseMarkDown(currentBuffer.toString())
                            if(nodes.size == 1){
                                updateLastMessage(nodes[0])
                            }else if(nodes.size > 1){
                                updateLastMessage(nodes[0])
                                resetState()
                                currentBuffer.append(text)
                            }

                        }
                        is GenerationEvent.Error,
                        GenerationEvent.Done,
                            -> {
//                            _isTextStreaming.value = false
                            generationJob?.cancel()
                        }

                    }

                }
            } else {
                val assistantAnswer = chatRepository.getAIAnswer(Model.TEST, messageList)
                MarkdownNodeUtils.parseMarkDown(assistantAnswer).forEachIndexed { index, element ->
                    val assistantMessage = Message(
                        msgId = UUID.randomUUID().toString(),
                        role = Role.Assistant,
                        markdown = element
                    )
                    messageList.add(assistantMessage)
                }
                messageHolder.updateIsGenerating(false)
                Log.d(TAG, "messageList size : ${messageList.size}")
            }
        }

    }

    private fun updateLastMessage(newMarkdown: MarkdownNode) {
        if (messageHolder.messageList.isEmpty()) return
        val lastIndex = messageHolder.messageList.size - 1
        val oldMessage = messageHolder.messageList[lastIndex]
        // create new object but keep original id
        val newMessage = oldMessage.copy(markdown = newMarkdown)
        // Replace the last element in the message list
        messageHolder.messageList[lastIndex] = newMessage
    }

    private fun resetState() {
        currentType = MarkdownType.UNKNOWN
        currentBuffer.clear()
        currentMessageId = null
    }
}