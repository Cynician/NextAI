package com.android.nextai.viewmodel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.nextai.domain.remote.Model
import com.android.nextai.domain.remote.entity.GenerationEvent
import com.android.nextai.domain.repository.ChatRepository
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import com.android.nextai.ui.component.markdown.utils.MarkdownNodeUtils
import com.android.nextai.viewmodel.chat.entity.Role
import com.android.nextai.viewmodel.chat.entity.getAssistantMessage
import com.android.nextai.viewmodel.chat.entity.getUserMessage
import com.android.nextai.viewmodel.chat.holder.ChatMessageHolder
import com.android.nextai.viewmodel.chat.holder.ChatUiStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    var generationJob: Job? = null
    private var currentBuffer = StringBuilder()
    private var currentBlocks = mutableListOf<MarkdownNode>()

    fun sendUserQuery(query: String) {
        val isSportStreamingGen = true
        val query = query.trim()
        val initBlock = MarkdownNode.Text("")
        messageHolder.addMessage(getUserMessage(query))
        messageHolder.addMessage(getAssistantMessage(listOf(initBlock)))
        messageHolder.updateCurPrompt(query)
        generationJob = viewModelScope.launch {
            if (isSportStreamingGen) {
                currentBuffer.clear()
                currentBlocks.clear()
                currentBlocks.add(initBlock)
                messageHolder.updateIsGenerating(true)
                messageHolder.updateIsTextStreaming(true)
                uiStateHolder.updateScrollToHeadAssistMessage(true)
                startStreamingGen()
            } else {
                val assistantAnswer = chatRepository.getAIAnswer(Model.TEST, messageHolder.messageList)
                val nodes = MarkdownNodeUtils.parseMarkDown(assistantAnswer)
                messageHolder.updateLastMessage(getAssistantMessage(nodes))
                messageHolder.updateIsGenerating(false)
            }
        }

    }

    /**
     * Stream generates AI responses
     */
    private suspend fun startStreamingGen(){
        messageHolder.updateIsTextStreaming(true)
        chatRepository.streamingGen(model = Model.TEST, messageList = messageHolder.messageList)
            .collect{ event ->
            when (event) {
                is GenerationEvent.Word -> {
                    Log.d(TAG, event.content)
                    val text = event.content
                    uiStateHolder.updateStreamingTick(text.length)
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
                    //  messageHolder.updateIsGenerating(false)
                    //  messageHolder.updateIsTextStreaming(false)
                    uiStateHolder.updateScrollToHeadAssistMessage(false)
                    generationJob?.cancel()
                }
            }
        }
    }

    private fun updateLastMessageBlocks(blocks: List<MarkdownNode>) {
        if (messageHolder.messageList.isEmpty()) return
        val oldMessage = messageHolder.messageList.last()
        if(oldMessage.role != Role.Assistant) return
        val newMessage = oldMessage.copy(blocks = blocks)
        messageHolder.updateLastMessage(newMessage)
    }
}