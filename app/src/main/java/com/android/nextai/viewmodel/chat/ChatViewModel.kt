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
import com.android.nextai.ui.component.markdown.entity.MarkdownType
import com.android.nextai.ui.component.markdown.utils.MarkdownUtils
import com.android.nextai.ui.component.markdown.utils.MarkdownUtils.parseText2Markdown
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
import kotlinx.coroutines.flow.flowOn
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
            markdown = MarkdownElement.Body(currentPrompt)
        )
        val messageList = messageHolder.messageList
        messageList.add(userMessage)

        messageHolder.updateIsGenerating(true)
        val isSportStreamingGen = true
        generationJob = viewModelScope.launch {
            if (isSportStreamingGen) {
                messageHolder.updateIsGenerating(true)
                streamingGen(messageList = messageList).collect { event ->
                    when (event) {
                        is GenerationEvent.Word -> {
                            Log.d(TAG, event.content)
                            var text = event.content
                            fullTextBuffer.append(text)
                            if (currentMessageId == null) {
                                currentMessageId = UUID.randomUUID().toString()
                                messageList.add(Message(
                                    msgId = currentMessageId!!,
                                    role = Role.Assistant,
                                    markdown = MarkdownElement.Body("")
                                ))
                            }
                            currentBuffer.append(text)
                            val fullContent = currentBuffer.toString()

                            if (currentType == MarkdownType.UNKNOWN) {
                                val trimmed = fullContent.trimStart()
                                Log.i(TAG, "trimmed:|$trimmed|")
                                when {
                                    trimmed.startsWith("```") -> {
                                        currentType = MarkdownType.CODE
                                        val lines = trimmed.split("\n")
                                        val firstLine = lines.first()
                                        val lang = firstLine.replace("```", "").trim()
                                        updateLastMessage(MarkdownElement.CodeBlock("", lang))
                                    }
                                    trimmed.startsWith("#") -> {
                                        currentType = MarkdownType.HEADING
                                    }
                                    trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                                        currentType = MarkdownType.LIST
                                        var level = 0
                                        for (i in fullTextBuffer.trimEnd().lastIndex - 1 downTo 0) {
                                            if(fullTextBuffer[i] == ' ') { level += 1 }
                                            else break
                                        }
                                        updateLastMessage(MarkdownElement.BulletPoint("", level))
                                    }
                                    trimmed == "---" || trimmed == "___" || trimmed == "***" -> {
                                        currentType = MarkdownType.DIVIDER
                                    }
                                    trimmed.isNotEmpty() -> currentType = MarkdownType.BODY
                                    else -> {}
                                }
                            }
                            Log.i(TAG, "currentType:$currentType")
                            when (currentType) {
                                MarkdownType.CODE -> {
                                    val trimmed = fullContent.trim()
                                    // check a block is to the end
                                    if (trimmed.startsWith("```") && trimmed.endsWith("```") && trimmed.length >= 6 ) {
                                        val code  = messageList.last().markdown.getContent()
                                        val lang = messageList.last().markdown.getLang()
                                        updateLastMessage(MarkdownElement.CodeBlock(code, lang, enableHighlightCode = true))
                                        resetState()
                                    } else {
                                        val code  = messageList.last().markdown.getContent()
                                        val lang = messageList.last().markdown.getLang()
                                        if(text.startsWith("```")) text = ""
                                        updateLastMessage(MarkdownElement.CodeBlock(code + text, lang))
                                    }
                                }
                                MarkdownType.LIST-> {
                                    if(text.last() == '\n'){
                                        val mText  = messageList.last().markdown.getContent()
                                        val level = messageList.last().markdown.getLevel()
                                        val markdown = MarkdownElement.BulletPoint(mText + text.trim(), level)
                                        updateLastMessage(markdown)
                                        resetState()
                                    }else{
                                        val mText  = messageList.last().markdown.getContent()
                                        val level = messageList.last().markdown.getLevel()
                                        if(text.startsWith("- ")) text = text.replace("- ", "")
                                        val markdown = MarkdownElement.BulletPoint(mText + text, level)
                                        updateLastMessage(markdown)
                                    }
                                }
                                MarkdownType.HEADING, MarkdownType.BODY -> {
                                    val markdown = parseText2Markdown(fullContent)
                                    updateLastMessage(markdown)
                                    if (text == "\n") resetState()
                                }

                                MarkdownType.DIVIDER ->{
                                    resetState()
                                }
                                else -> {}
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
                val assistantAnswer = chatRepository.getAIAnswer(Model.DOUBAO, messageList)
                MarkdownUtils.parseMarkdown(assistantAnswer).forEachIndexed { index, element ->
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

    private fun updateLastMessage(newMarkdown: MarkdownElement) {
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


    private fun streamingGen(messageList: List<Message>): Flow<GenerationEvent> =
        callbackFlow<GenerationEvent> {
            val callback: (GenerationEvent) -> Unit = { event ->
                val result = trySend(event)
                if (result.isFailure) {
                    close() // close the flow
                }
            }
            try {
                Log.d(TAG, "[streamingGen] start to steaming...")
                chatRepository.getAIStreamingAnswer(Model.QIANWEN, messageList, callback)
            } catch (e: Exception) {
                trySend(GenerationEvent.Error("fail to streaming：${e.message}"))
                close()
            }
            awaitClose {
                Log.d(TAG, "[streamingGen] cancel...")
            }

        }.buffer(Channel.UNLIMITED)
            .flowOn(Dispatchers.IO)

}