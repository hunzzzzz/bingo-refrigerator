package team.b2.bingojango.domain.member.dto

import team.b2.bingojango.domain.member.model.MemberRole
import team.b2.bingojango.domain.user.model.User
import java.time.ZonedDateTime

data class MemberResponse(
    val name: String,
    val role: MemberRole,
    val memberId: Long,
    val createdAt: ZonedDateTime,
    val imageUrl: String?,
    val userId: Long,
)