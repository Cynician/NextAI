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
    private var currentBuffer = StringBuilder()
    @Volatile
    private var currentBlocks = mutableListOf<MarkdownNode>()
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun sendUserMessage(currentPrompt: String) {
        val isSportStreamingGen = true
        val currentPrompt = currentPrompt.trim()
        val messageList = messageHolder.messageList
        val userMessage = Message(
            msgId = UUID.randomUUID().toString(),
            role = Role.User,
            blocks = listOf(MarkdownNode.Text(currentPrompt))
        )
        val initBlock = MarkdownNode.Text("")
        val assistantMessage = Message(
            msgId = UUID.randomUUID().toString(),
            role = Role.Assistant,
            blocks = listOf(initBlock)
        )
        messageList.add(userMessage)
        messageList.add(assistantMessage)
        messageHolder.updateIsGenerating(true)
        messageHolder.updateCurPrompt(currentPrompt)
        currentBlocks.add(initBlock)
        generationJob = viewModelScope.launch {
            if (isSportStreamingGen) {
                messageHolder.updateIsTextStreaming(true)
                chatRepository.streamingGen(model = Model.TEST,messageList = messageList).collect { event ->
                    when (event) {
                        is GenerationEvent.Word -> {
                            Log.d(TAG, event.content)
                            val text = event.content
                            currentBuffer.append(text)
                            val nodes = MarkdownNodeUtils.parseMarkDown(currentBuffer.toString())
                            if (nodes.size == 1) {
                                val last = nodes[0]
                                currentBlocks[currentBlocks.lastIndex] = last
                            } else if(nodes.size > 1) {
                                val first = nodes[0]
                                currentBlocks[currentBlocks.lastIndex] = first
                                currentBuffer.clear()
                                currentBuffer.append(text)
                                currentBlocks.add(
                                    MarkdownNode.Text("")
                                )
                            }
                            updateLastMessageBlocks(currentBlocks.toList())
                        }
                        is GenerationEvent.Error,
                        GenerationEvent.Done -> {
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
                        blocks = mutableListOf(element)
                    )
                    messageList.add(assistantMessage)
                }
                messageHolder.updateIsGenerating(false)
                Log.d(TAG, "messageList size : ${messageList.size}")
            }
        }

    }

    private fun updateLastMessageBlocks(blocks: List<MarkdownNode>) {
        if (messageHolder.messageList.isEmpty()) return
        val lastIndex = messageHolder.messageList.size - 1
        val oldMessage = messageHolder.messageList[lastIndex]
        val newMessage = oldMessage.copy(blocks = blocks)
        messageHolder.messageList[lastIndex] = newMessage
    }
}