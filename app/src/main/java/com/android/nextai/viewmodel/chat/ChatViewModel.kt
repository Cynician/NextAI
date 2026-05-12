package com.android.nextai.viewmodel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.nextai.domain.remote.Model
import com.android.nextai.domain.remote.entity.GenerationEvent
import com.android.nextai.domain.repository.ChatDatabaseRepository
import com.android.nextai.domain.repository.ChatRemoteRepository
import com.android.nextai.ui.component.markdown.entity.MarkdownNode
import com.android.nextai.ui.component.markdown.utils.MarkdownNodeUtils
import com.android.nextai.viewmodel.chat.entity.Role
import com.android.nextai.viewmodel.chat.entity.getAssistantMessage
import com.android.nextai.viewmodel.chat.entity.getUserMessage
import com.android.nextai.viewmodel.chat.holder.ChatMessageHolder
import com.android.nextai.viewmodel.chat.holder.ChatSessionHolder
import com.android.nextai.viewmodel.chat.holder.ChatUiStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    val messageHolder: ChatMessageHolder,
    val uiStateHolder: ChatUiStateHolder,
    val sessionHolder: ChatSessionHolder,
    val chatRemoteRepository: ChatRemoteRepository,
    val chatDatabaseRepository: ChatDatabaseRepository
) : ViewModel() {
    companion object {
        private const val TAG = "ChatViewModel"
    }
    var generationJob: Job? = null
    private var currentBuffer = StringBuilder()
    private var currentBlocks = mutableListOf<MarkdownNode>()

    init{
        viewModelScope.launch {
            try {
                sessionHolder.loadSessions()
            }catch (e: Exception){
                Log.e(TAG, "Could not load sessions: $e")
            }
        }
    }

    /**
     * Get ai response
     */
    fun sendUserQuery(query: String) {
        val isSportStreamingGen = true
        val query = query.trim()
        generationJob = viewModelScope.launch {
            messageHolder.init()
            val initBlock = MarkdownNode.Text("")
            messageHolder.updateCurQuery(query)
            Log.d(TAG,"update query :$query")
            val lastId = chatDatabaseRepository.messageDao.getMaxId()?:0
            messageHolder.addMessage(getUserMessage(lastId+1,query))
            messageHolder.addMessage(getAssistantMessage(lastId+2, listOf(initBlock)))
            if (isSportStreamingGen) {
                currentBuffer.clear()
                currentBlocks.clear()
                currentBlocks.add(initBlock)
                messageHolder.updateIsGenerating(true)
                messageHolder.updateIsTextStreaming(true)
                uiStateHolder.updateScrollToHeadAssistMessage(true)
                startStreamingGen()
            } else {
                val assistantAnswer = chatRemoteRepository.getAIAnswer(Model.TEST, messageHolder.messageList)
                val nodes = MarkdownNodeUtils.parseMarkDown(assistantAnswer)
                messageHolder.updateLastMessage(getAssistantMessage(lastId+2,nodes))
                messageHolder.updateIsGenerating(false)
            }
        }

    }

    /**
     * Get ai response in stream way
     */
    private suspend fun startStreamingGen(){
        Log.d(TAG, "startStreamingGen# get ai response")
        chatRemoteRepository.streamingGen(model = Model.TEST, messageList = messageHolder.messageList)
            .collect{ event ->
                when (event) {
                    is GenerationEvent.Word -> {
                        val text = event.content
                        messageHolder.updateCurResponse(text)
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
                            currentBlocks.add(MarkdownNode.Text(""))
                        }
                        updateLastMessageBlocks(currentBlocks.toList())
                    }
                    is GenerationEvent.Done -> {
                        try {
                            // create new session
                            if (!sessionHolder.getIsInSession()) {
                                val session = sessionHolder.createSession(messageHolder.getCurQuery())
                                Log.d(TAG, "title: ${messageHolder.getCurQuery()}")
                                Log.i(TAG, "create new session, id: ${session.id}")
                            }
                            val (uId, aId) = chatDatabaseRepository.insertPairMessage(
                                sessionHolder.getCurSessionId(),
                                messageHolder.getCurQuery(),
                                messageHolder.getCurResponse()
                            )
                            Log.i(TAG, "save pair message, uId: $uId, aId: $aId")
                        } catch (e: Exception) {

                        }
                        messageHolder.updateIsGenerating(false)
                        messageHolder.updateIsTextStreaming(false)
                        uiStateHolder.updateScrollToHeadAssistMessage(false)
                        generationJob?.cancel()
                    }
                    is GenerationEvent.Error -> {

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

    /**
     * Session init
     *
     * The initialization method when creating a new session or selecting a different session
     */
    fun initSession(){
        messageHolder.init()
        sessionHolder.init()
        generationJob?.cancel()
    }

    /**
     * Delete sessions in batch
     */
    fun batchDeleteSessions(idList:List<Long>){
        viewModelScope.launch {
            if(idList.contains(sessionHolder.getCurSessionId())){
                initSession()
            }
            sessionHolder.batchDeleteSessions(idList)
        }
    }

    /**
     * Pin sessions in batch
     */
    fun batchPinSessions(idList:List<Long>){
        viewModelScope.launch {
            sessionHolder.batchPinSessions(idList)
        }
    }

    /**
     * Unpinned session
     */
    fun unpinnedSession(id:Long){
        viewModelScope.launch {
            sessionHolder.unpinnedSession(id)
        }
    }
}