package team.b2.bingojango.domain.vote.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import team.b2.bingojango.domain.member.model.Member
import team.b2.bingojango.domain.purchase.model.Purchase
import team.b2.bingojango.domain.refrigerator.model.Refrigerator
import team.b2.bingojango.domain.vote.model.Vote
import team.b2.bingojango.global.util.ZonedDateTimeConverter.convertStringDateTimeFromZonedDateTime

data class VoteRequest(
    val description: String?,

    @field:NotBlank(message = "투표 마감 시간은 필수 입력 사항입니다.")
    @field:Pattern(
        regexp = "\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01]) ([0-1][0-9]|2[0-3]):([0-5][0-9])",
        message = "투표 마감 시간을 다시 한 번 확인해주세요. (yyyy-MM-dd HH:mm)"
    )
    val dueDate: String,
) {
    fun to(refrigerator: Refrigerator, member: Member, purchase: Purchase) = Vote(
        description = description,
        dueDate = convertStringDateTimeFromZonedDateTime(dueDate),
        refrigerator = refrigerator,
        voters = mutableSetOf(),
        purchase = purchase
    )
}