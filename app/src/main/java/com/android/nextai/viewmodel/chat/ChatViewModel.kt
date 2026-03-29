package com.android.nextai.viewmodel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.nextai.domain.remote.Model
import com.android.nextai.domain.remote.entity.GenerationEvent
import com.android.nextai.domain.repository.ChatRepository
import com.android.nextai.viewmodel.chat.entity.EmptyMessage
import com.android.nextai.viewmodel.chat.entity.Message
import com.android.nextai.viewmodel.chat.entity.Role
import com.android.nextai.viewmodel.chat.holder.ChatMessageHolder
import com.android.nextai.viewmodel.chat.holder.ChatUiStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
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

    private var generationJob: Job? = null


    fun sendUserMessage(currentPrompt: String) {
        currentPrompt.trim()
        messageHolder.updateCurPrompt(currentPrompt)

        val userMessage = Message(
            msgId = System.currentTimeMillis(),
            role = Role.User,
            content = currentPrompt
        )
        val messageList = messageHolder.messageList
        messageList.add(userMessage)

        messageHolder.updateLastUserMarkdownElementCnt(
            messageHolder.totalMarkdownElementCnt.value +
                    userMessage.markdownElements.size
        )
        messageList.add(EmptyMessage)

        messageHolder.updateIsGenerating(true)
        val isSportStreamingGen = true
        generationJob = viewModelScope.launch {
            if (isSportStreamingGen) {
                val msgId = System.currentTimeMillis()
                var tmpContent = ""
                streamingGen(messageList = messageList).collect { event ->
                    when (event) {
                        is GenerationEvent.Word -> {
                            tmpContent += event.content
                            Log.d(TAG, tmpContent)
                        }

                        is GenerationEvent.Done -> {
                            generationJob?.cancel()
                        }

                        else -> {}
                    }

                }
            } else {
                val assistantAnswer = chatRepository.getAIAnswer(Model.DOUBAO, messageList)
                val assistantMessage = Message(
                    msgId = System.currentTimeMillis(),
                    role = Role.Assistant,
                    content = assistantAnswer
                )
                messageList.add(assistantMessage)
                messageList.add(EmptyMessage)
                messageHolder.updateIsGenerating(false)

                messageHolder.updateTotalMarkdownElementCnt(
                    userMessage.markdownElements.size +
                            assistantMessage.markdownElements.size + 2
                )
                Log.d(TAG, "bubbleList size : ${messageList.size}")
            }
        }

    }

    private fun streamingGen(messageList: List<Message>): Flow<GenerationEvent> =
        callbackFlow<GenerationEvent> {

            val callback: (GenerationEvent) -> Unit = { event ->
                val result = trySend(event)
                if (result.isFailure) {
                    // 如果下游取消收集了，我们需要告诉服务停止生成
                    close() // 关闭 Flow
                }
            }

            try {
                Log.d(TAG, "[Flow] 开始启动生成任务...")
                chatRepository.getAIStreamingAnswer(Model.DOUBAO, messageList, callback)
            } catch (e: Exception) {
                trySend(GenerationEvent.Error("启动失败：${e.message}"))
                close()
            }
            // 当 collect 结束或取消时执行
            awaitClose {
                Log.d(TAG, "[Flow] 收集者取消，清理资源...")
            }

        }.buffer(Channel.UNLIMITED)
            .flowOn(Dispatchers.IO)

}