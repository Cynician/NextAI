package com.android.nextai.domain.usecase.chat

import com.android.nextai.domain.model.chat.GroupType
import com.android.nextai.domain.model.chat.Session
import com.android.nextai.domain.repository.ChatRepository
import com.android.nextai.domain.usecase.BaseUseCase
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GetGroupSessionsUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) : BaseUseCase() {

    suspend operator fun invoke(): Result<Map<Int, List<Session>>> = execute {
        val sessionList = chatRepository.getAllSessions()
        sessionList.toGroup()
    }

    private fun List<Session>.toGroup(): Map<Int, List<Session>> {
        val pinedList = mutableListOf<Session>()
        val todayList = mutableListOf<Session>()
        val weekList = mutableListOf<Session>()
        val monthList = mutableListOf<Session>()
        val earlierList = mutableListOf<Session>()

        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        val oneWeek = 7 * oneDay
        val oneMonth = 30 * oneDay
        val startOfToday = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val endOfToday = startOfToday + oneDay

        for (session in this) {
            val uTime = session.updatedAt
            val diff = now - uTime
            when {
                session.isPinned == 1 -> pinedList.add(session)
                uTime in startOfToday..endOfToday -> todayList.add(session)
                diff <= oneWeek -> weekList.add(session)
                diff <= oneMonth -> monthList.add(session)
                else -> earlierList.add(session)
            }
        }

        return mapOf(
            GroupType.PIN to pinedList,
            GroupType.TODAY to todayList,
            GroupType.IN_WEEK to weekList,
            GroupType.IN_MONTH to monthList,
            GroupType.EARLIER to earlierList
        ).filterValues { it.isNotEmpty() }
    }

}