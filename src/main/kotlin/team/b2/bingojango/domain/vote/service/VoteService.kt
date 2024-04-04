package team.b2.bingojango.domain.vote.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.b2.bingojango.domain.member.model.MemberRole
import team.b2.bingojango.domain.member.repository.MemberRepository
import team.b2.bingojango.domain.purchase.model.PurchaseStatus
import team.b2.bingojango.domain.purchase.repository.PurchaseRepository
import team.b2.bingojango.domain.purchase_product.dto.response.PurchaseProductResponse
import team.b2.bingojango.domain.purchase_product.repository.PurchaseProductRepository
import team.b2.bingojango.domain.vote.dto.request.VoteRequest
import team.b2.bingojango.domain.vote.dto.response.VoteResponse
import team.b2.bingojango.domain.vote.repository.VoteRepository
import team.b2.bingojango.global.exception.cases.*
import team.b2.bingojango.global.security.util.UserPrincipal
import team.b2.bingojango.global.util.EntityFinder

@Service
@Transactional
class VoteService(
    private val memberRepository: MemberRepository,
    private val purchaseRepository: PurchaseRepository,
    private val purchaseProductRepository: PurchaseProductRepository,
    private val voteRepository: VoteRepository,
    private val entityFinder: EntityFinder
) {
    /*
        [API] 현재 공동구매 목록에 대한 투표 현황 조회
   */
    fun showVote(refrigeratorId: Long) =
        VoteResponse.from(
            vote = getCurrentVote(refrigeratorId),
            member = entityFinder.getMember(
                userId = getCurrentPurchaseOnVote(refrigeratorId).proposedBy,
                refrigeratorId = refrigeratorId
            ),
            numberOfStaff = getNumberOfStaff(refrigeratorId),
            purchaseProductList = purchaseProductRepository.findAllByPurchase(getCurrentPurchaseOnVote(refrigeratorId))
                .map { purchaseProduct -> PurchaseProductResponse.from(purchaseProduct) }
        )

    /*
        [API] 현재 공동구매 목록에 대한 투표 시작
            - 검증 조건 1 : 공동구매를 신청한 회원 본인만 투표를 시작할 수 있음 [X] -> STAFF 모두 가능
            - 검증 조건 2 : 공동구매 목록에 물품이 존재하지 않는 경우, 투표를 시작할 수 없음
            - 검증 조건 3 : STAFF 의 수가 1명인 경우, 투표 과정을 생략하고 현재 Purchase 의 status 를 APPROVED 로 변경
     */
    fun startVote(userPrincipal: UserPrincipal, refrigeratorId: Long, voteRequest: VoteRequest) =
        getCurrentPurchase(refrigeratorId).let {
//            if (it.proposedBy != userPrincipal.id)
            if (entityFinder.getMember(userPrincipal.id, refrigeratorId).role != MemberRole.STAFF)
                throw InvalidRoleException()
            else if (purchaseProductRepository.countByPurchase(it) == 0L)
                throw UnableToStartVoteException()
            else if (getNumberOfStaff(refrigeratorId) == 1L)
                it.updateStatus(PurchaseStatus.APPROVED)
            else
                it.updateStatus(PurchaseStatus.ON_VOTE)

            voteRepository.save(
                voteRequest.to(
                    refrigerator = entityFinder.getRefrigerator(refrigeratorId),
                    member = entityFinder.getMember(userPrincipal.id, refrigeratorId),
                    purchase = it
                )
            )
            Unit
        }

    /*
        [API] 투표 실시
            - 검증 조건 1 : 관리자(STAFF)만 투표를 할 수 있음
            - 검증 조건 2 : 이미 투표한 경우, 투표 결과를 수정할 수 없음

            - 분기 조건 1-1 : 찬성에 투표한 경우, voters 에 해당 Member 객체를 add
            - 분기 조건 1-2 : 만장일치가 완성된 경우, 투표를 종료하고 현재 Purchase 의 status 를 APPROVED 로 변경
            - 분기 조건 2 : 반대에 투표한 경우, 투표를 종료하고 현재 Purchase 의 status 를 REJECTED 로 변경
     */
    fun vote(userPrincipal: UserPrincipal, refrigeratorId: Long, isAccepted: Boolean) =
        entityFinder.getMember(userPrincipal.id, refrigeratorId)
            .let {
                if (it.role != MemberRole.STAFF)
                    throw InvalidRoleException()
                else if (getCurrentVote(refrigeratorId).voters.contains(it))
                    throw DuplicatedVoteException()

                getCurrentVote(refrigeratorId)
            }.let {
                if (isAccepted) {
                    it.updateVote(entityFinder.getMember(userPrincipal.id, refrigeratorId))
                    if (getNumberOfStaff(refrigeratorId) == it.voters.size.toLong())
                        getCurrentPurchaseOnVote(refrigeratorId).updateStatus(PurchaseStatus.APPROVED)
                } else
                    getCurrentPurchaseOnVote(refrigeratorId).updateStatus(PurchaseStatus.REJECTED)
            }

    // [내부 메서드] ACTIVE (같이구매 진행 중인) 한 Purchase 를 리턴
    private fun getCurrentPurchase(refrigeratorId: Long) =
        purchaseRepository.findAllByRefrigerator(entityFinder.getRefrigerator(refrigeratorId))
            .firstOrNull { it.status == PurchaseStatus.ACTIVE }
            ?: throw NoCurrentPurchaseException()

    // [내부 메서드] ON_VOTE (투표 진행 중인) Purchase 를 리턴
    private fun getCurrentPurchaseOnVote(refrigeratorId: Long) =
        purchaseRepository.findAllByRefrigerator(entityFinder.getRefrigerator(refrigeratorId))
            .firstOrNull { it.status == PurchaseStatus.ON_VOTE }
            ?: throw NoCurrentPurchaseException()

    // [내부 메서드] 현재 진행 중인 Purchase 에 대한 Vote 를 리턴
    private fun getCurrentVote(refrigeratorId: Long) =
        voteRepository.findByPurchase(getCurrentPurchaseOnVote(refrigeratorId))
            ?: throw NoCurrentVoteException()

    // [내부 메서드] 현재 Refrigerator 내 관리자(STAFF) 의 수
    private fun getNumberOfStaff(refrigeratorId: Long) =
        memberRepository.countByRoleAndRefrigerator(MemberRole.STAFF, entityFinder.getRefrigerator(refrigeratorId))
}