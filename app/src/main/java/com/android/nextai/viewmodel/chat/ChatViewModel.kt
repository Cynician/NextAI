package com.android.nextai.viewmodel.chat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.nextai.domain.remote.Model
import com.android.nextai.domain.remote.entity.GenerationEvent
import com.android.nextai.domain.repository.ChatRepository
import com.android.nextai.ui.component.markdown.entity.MarkdownElement
import com.android.nextai.ui.component.markdown.utils.MarkdownUtils
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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


    private val _curMarkdown = MutableStateFlow<MarkdownElement?>(null)
    val curMarkdown: StateFlow<MarkdownElement?> = _curMarkdown.asStateFlow()

    private val _curMsgId = MutableStateFlow(0)
    val curMsgId: StateFlow<Int> = _curMsgId.asStateFlow()

    private val _isTextStreaming = MutableStateFlow(true)
    val isTextStreaming: StateFlow<Boolean> = _isTextStreaming.asStateFlow()

    private val _curContent = MutableStateFlow("")
    val curContent: StateFlow<String> = _curContent.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun sendUserMessage(currentPrompt: String) {
        currentPrompt.trim()
        messageHolder.updateCurPrompt(currentPrompt)
        val userMessage = Message(
            msgId = messageHolder.totalMarkdownElementCnt.value + 1,
            role = Role.User,
            markdown = MarkdownElement.Body(currentPrompt)
        )
        val messageList = messageHolder.messageList
        messageList.add(userMessage)
        messageHolder.updateLastUserMarkdownElementCnt(messageHolder.totalMarkdownElementCnt.value + 1)
        messageHolder.updateTotalMarkdownElementCnt(1)

        messageHolder.updateIsGenerating(true)
        val isSportStreamingGen = true
        generationJob = viewModelScope.launch {
            if (isSportStreamingGen) {
                _curMsgId.value =  messageHolder.totalMarkdownElementCnt.value + 1
                _isTextStreaming.value = true
                messageList.add(Message(
                    msgId = _curMsgId.value,
                    role = Role.Assistant,
                    markdown = MarkdownElement.Body("")
                ))
                var fullTextBuffer = ""
                streamingGen(messageList = messageList).collect { event ->
                    when (event) {
                        is GenerationEvent.Word -> {
                            _curContent.value += event.content
                            Log.d(TAG, _curContent.value)
                            fullTextBuffer += event.content
                            val elements = MarkdownUtils.parseMarkdown(fullTextBuffer)
                            if(elements.size == 1){
                                val element = elements.first()
                                messageList.removeLast()
                                messageList.add(Message(
                                    msgId = _curMsgId.value,
                                    role = Role.Assistant,
                                    markdown = element
                                ))
                            }else if(elements.size>1){
                                messageHolder.updateTotalMarkdownElementCnt(1)
                                _curMsgId.value =  messageHolder.totalMarkdownElementCnt.value + 1
                                fullTextBuffer = event.content
                                messageList.add(Message(
                                    msgId = _curMsgId.value,
                                    role = Role.Assistant,
                                    markdown = elements.last()
                                ))
                            }
                            Log.d(TAG, fullTextBuffer +"element type : ${elements.last()}")
                        }
                        is GenerationEvent.Error,
                        GenerationEvent.Done -> {
//                            _isTextStreaming.value = false
                            generationJob?.cancel()
                        }
                    }

                }
            }
            // 非流式输出
            else {
                val assistantAnswer = chatRepository.getAIAnswer(Model.DOUBAO, messageList)
                messageList.add(EmptyMessage)
                MarkdownUtils.parseMarkdown(assistantAnswer).forEachIndexed { index, element ->
                    val assistantMessage = Message(
                        msgId = messageHolder.totalMarkdownElementCnt.value + 1,
                        role = Role.Assistant,
                        markdown = element
                    )
                    messageList.add(assistantMessage)
                    messageHolder.updateTotalMarkdownElementCnt(1)
                }
                messageHolder.updateIsGenerating(false)

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