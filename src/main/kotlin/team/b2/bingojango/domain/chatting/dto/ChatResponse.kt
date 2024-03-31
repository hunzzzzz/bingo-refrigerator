package team.b2.bingojango.domain.chatting.dto

import team.b2.bingojango.domain.chatting.model.ChatStatus
import java.time.ZonedDateTime

data class ChatResponse(
    val chatRoomId: Long,
    val nickname: String,
    val content: String,
    val status: ChatStatus,
    val createdAt: String,
    val userId: Long,
)
