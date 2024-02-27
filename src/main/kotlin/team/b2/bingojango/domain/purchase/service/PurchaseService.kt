package team.b2.bingojango.domain.purchase.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.b2.bingojango.domain.purchase.model.Purchase
import team.b2.bingojango.domain.purchase.model.PurchaseStatus
import team.b2.bingojango.domain.purchase.repository.PurchaseRepository
import team.b2.bingojango.domain.purchase_food.dto.response.PurchaseFoodResponse
import team.b2.bingojango.domain.purchase_food.repository.PurchaseFoodRepository
import team.b2.bingojango.domain.refrigerator.repository.RefrigeratorRepository

@Service
@Transactional
class PurchaseService(
    private val purchaseRepository: PurchaseRepository,
    private val purchaseFoodRepository: PurchaseFoodRepository,
    private val refrigeratorRepository: RefrigeratorRepository
) {

    // [API] 현재 진행 중인 Purchase 목록을 출력
    // TODO : 추후 조회 과정 리팩토링 필요
    fun showPurchase(refrigeratorId: Long) =
        purchaseFoodRepository.findAll()
            .filter { it.purchase.status == PurchaseStatus.ON_VOTE }
            .map { PurchaseFoodResponse.from(it) }

    // [내부 메서드] Purchase 객체 생성 (FoodService > getCurrentPurchase 에서만 사용되는 메서드)
    fun makePurchase(refrigeratorId: Long) =
        purchaseRepository.save(
            Purchase(
                status = PurchaseStatus.ON_VOTE,
                refrigerator = getRefrigerator(refrigeratorId)
            )
        )

    // [내부 메서드] id로 Refrigerator 객체 가져오기
    private fun getRefrigerator(refrigeratorId: Long) =
        refrigeratorRepository.findByIdOrNull(refrigeratorId) ?: throw Exception("") // TODO
}