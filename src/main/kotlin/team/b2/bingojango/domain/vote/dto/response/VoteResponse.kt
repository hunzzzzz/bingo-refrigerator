package team.b2.bingojango.domain.vote.dto.response

import team.b2.bingojango.domain.member.model.Member
import team.b2.bingojango.domain.purchase_product.dto.response.PurchaseProductResponse
import team.b2.bingojango.domain.vote.model.Vote
import team.b2.bingojango.global.util.ZonedDateTimeConverter

data class VoteResponse(
    val description: String?,                                   // 투표 설명
    val dueDate: String,                                        // 투표 만료 기한
    val memberName: String,                                     // 투표 기안자
    val numberOfAgree: Long,                                    // 투표 찬성 인원 수
    val numberOfStaff: Long,                                    // 냉장고 내 STAFF 수
    val purchaseProductList: List<PurchaseProductResponse>      // 같이구매 목록
) {
    companion object {
        fun from(vote: Vote, member: Member, numberOfStaff: Long, purchaseProductList: List<PurchaseProductResponse>) =
            VoteResponse(
                description = vote.description,
                dueDate = ZonedDateTimeConverter.convertZonedDateTimeFromStringDateTime(vote.dueDate),
                memberName = member.user.nickname,
                numberOfAgree = vote.voters.size.toLong(),
                numberOfStaff = numberOfStaff,
                purchaseProductList = purchaseProductList
            )
    }
}
