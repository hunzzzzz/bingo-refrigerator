package team.b2.bingojango.domain.purchase.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.b2.bingojango.domain.food.model.Food
import team.b2.bingojango.domain.product.model.Product
import team.b2.bingojango.domain.product.repository.ProductRepository
import team.b2.bingojango.domain.purchase.dto.response.AddNewFoodInPurchaseRequest
import team.b2.bingojango.domain.purchase.dto.response.PurchaseResponse
import team.b2.bingojango.domain.purchase.model.Purchase
import team.b2.bingojango.domain.purchase.model.PurchaseSort
import team.b2.bingojango.domain.purchase.model.PurchaseStatus
import team.b2.bingojango.domain.purchase.repository.PurchaseRepository
import team.b2.bingojango.domain.purchase_product.dto.response.PurchaseProductResponse
import team.b2.bingojango.domain.purchase_product.model.PurchaseProduct
import team.b2.bingojango.domain.purchase_product.repository.PurchaseProductRepository
import team.b2.bingojango.domain.refrigerator.model.Refrigerator
import team.b2.bingojango.domain.vote.repository.VoteRepository
import team.b2.bingojango.global.exception.cases.*
import team.b2.bingojango.global.security.util.UserPrincipal
import team.b2.bingojango.global.util.EntityFinder

@Service
@Transactional
class PurchaseService(
    private val purchaseRepository: PurchaseRepository,
    private val purchaseProductRepository: PurchaseProductRepository,
    private val productRepository: ProductRepository,
    private val voteRepository: VoteRepository,
    private val entityFinder: EntityFinder
) {
    /*
        [API] 해당 식품을 n개 만큼 공동구매 신청
            - 검증 조건 1 : 관리자(STAFF)만 공동구매를 신청할 수 있음 [X]
            - 검증 조건 2 : 다른 관리자가 시작한 공동구매가 있는 경우 신청할 수 없음 [X]
            - 검증 조건 3 : 현재 공동구매 중인 식품은 추가할 수 없음
            - 검증 조건 4 : 이미 투표가 시작된 공동구매에 식품을 추가할 수 없음
    */
    fun addFoodToPurchase(userPrincipal: UserPrincipal, refrigeratorId: Long, foodId: Long, count: Int) =
        getCurrentPurchase(userPrincipal, refrigeratorId).let {
//            if (entityFinder.getMember(userPrincipal.id, refrigeratorId).role != MemberRole.STAFF)
//                throw InvalidRoleException()
//            else if (purchaseRepository.existsByStatus(PurchaseStatus.ACTIVE) && getCurrentPurchase(refrigeratorId).proposedBy != userPrincipal.id)
//                throw AlreadyHaveActivePurchaseException()
            if (purchaseProductRepository.findAllByPurchase(getCurrentPurchase(refrigeratorId))
                    .map { purchaseProduct -> purchaseProduct.product.food }
                    .contains(entityFinder.getFood(foodId))
            ) throw AlreadyInPurchaseException()
            else if (voteRepository.existsByPurchaseAndRefrigerator(
                    purchase = getCurrentPurchase(refrigeratorId),
                    refrigerator = entityFinder.getRefrigerator(refrigeratorId)
                )
            ) throw AlreadyOnVoteException("추가")

            purchaseProductRepository.save(
                PurchaseProduct(
                    count = count,
                    purchase = it,
                    product = getProduct(foodId, refrigeratorId),
                    refrigerator = entityFinder.getRefrigerator(refrigeratorId)
                )
            )
            Unit
        }

    /*
            [API] 새로운 식품을 n개 만큼 공동구매 신청
            - 검증 조건 1 : 관리자(STAFF)만 공동구매를 신청할 수 있음 [X]
            - 검증 조건 2 : 다른 관리자가 시작한 공동구매가 있는 경우 신청할 수 없음 [X]
            - 검증 조건 3 : 이미 투표가 시작된 공동구매에 식품을 추가할 수 없음
     */
    fun addNewFoodToPurchase(
        userPrincipal: UserPrincipal,
        refrigeratorId: Long,
        request: AddNewFoodInPurchaseRequest
    ) =
        getCurrentPurchase(userPrincipal, refrigeratorId).let {
//            if (entityFinder.getMember(userPrincipal.id, refrigeratorId).role != MemberRole.STAFF)
//                throw InvalidRoleException()
//            else if (purchaseRepository.existsByStatus(PurchaseStatus.ACTIVE) && getCurrentPurchase(refrigeratorId).proposedBy != userPrincipal.id)
//                throw AlreadyHaveActivePurchaseException()
            if (voteRepository.existsByPurchaseAndRefrigerator(
                    purchase = getCurrentPurchase(refrigeratorId),
                    refrigerator = entityFinder.getRefrigerator(refrigeratorId)
                )
            ) throw AlreadyOnVoteException("추가")

            entityFinder.getRefrigerator(refrigeratorId)
                .let { refrigerator ->
                    purchaseProductRepository.save(
                        PurchaseProduct(
                            count = request.count,
                            purchase = it,
                            product = productRepository.save(
                                Product(
                                    food = null,
                                    refrigerator = refrigerator,
                                    request.name
                                )
                            ),
                            refrigerator = refrigerator
                        )
                    )
                }
            Unit
        }

    /*
        [API] 공동구매 목록에서 특정 식품 개수 수정
            - 검증 조건 1 : 해당 공동구매를 올린 사람만 수정을 할 수 있음 [X]
            - 검증 조건 2 : 현재 공동구매에 존재하는 식품만 수정할 수 있음
            - 검증 조건 3 : 이미 투표가 시작된 공동구매에 식품을 수정할 수 없음
    */
    fun updateFoodInPurchase(userPrincipal: UserPrincipal, refrigeratorId: Long, productId: Long, count: Int) {
//        if (getCurrentPurchase(refrigeratorId).proposedBy != userPrincipal.id)
//        if (entityFinder.getMember(userPrincipal.id, refrigeratorId).role != MemberRole.STAFF)
//            throw InvalidRoleException()
        if (voteRepository.existsByPurchaseAndRefrigerator(
                purchase = getCurrentPurchase(refrigeratorId),
                refrigerator = entityFinder.getRefrigerator(refrigeratorId)
            )
        ) throw AlreadyOnVoteException("수정")

        (purchaseProductRepository.findByRefrigeratorAndProductAndPurchase(
            refrigerator = entityFinder.getRefrigerator(refrigeratorId),
            product = entityFinder.getProduct(productId),
            purchase = getCurrentPurchase(refrigeratorId)
        ) ?: throw ModelNotFoundException("식품")).updateCount(count)
    }

    /*
        [API] 공동구매 목록에서 특정 식품 삭제
            - 검증 조건 1 : 해당 공동구매를 올린 사람만 삭제를 할 수 있음 [X]
            - 검증 조건 2 : 현재 공동구매에 존재하는 식품만 삭제할 수 있음
            - 검증 조건 3 : 이미 투표가 시작된 공동구매에 식품을 삭제할 수 없음
     */
    fun deleteFoodFromPurchase(userPrincipal: UserPrincipal, refrigeratorId: Long, productId: Long) {
//        if (getCurrentPurchase(refrigeratorId).proposedBy != userPrincipal.id)
//        if (entityFinder.getMember(userPrincipal.id, refrigeratorId).role != MemberRole.STAFF)
//            throw InvalidRoleException()
        if (voteRepository.existsByPurchaseAndRefrigerator(
                purchase = getCurrentPurchase(refrigeratorId),
                refrigerator = entityFinder.getRefrigerator(refrigeratorId)
            )
        ) throw AlreadyOnVoteException("삭제")

        purchaseProductRepository.delete(
            purchaseProductRepository.findByRefrigeratorAndProductAndPurchase(
                refrigerator = entityFinder.getRefrigerator(refrigeratorId),
                product = entityFinder.getProduct(productId),
                purchase = getCurrentPurchase(refrigeratorId)
            ) ?: throw ModelNotFoundException("식품")
        )
    }

    // [API] 현재 진행 중인 Purchase 를 출력
    fun showPurchase(refrigeratorId: Long) =
        getCurrentPurchase(refrigeratorId)
            .let {
                PurchaseResponse.from(
                    purchase = it,
                    member = entityFinder.getMember(it.proposedBy, refrigeratorId),
                    purchaseProductList = purchaseProductRepository.findAllByPurchase(it)
                        .map { purchaseProduct -> PurchaseProductResponse.from(purchaseProduct) }
                )
            }

    // [API] 현재까지 진행된 모든 Purchase 목록을 출력
    fun showPurchaseList(refrigeratorId: Long, status: PurchaseStatus?, sort: PurchaseSort?, page: Int) =
        purchaseRepository.searchPurchase(
            status = status,
            pageable = PageRequest.of(page - 1, 5, Sort.by(sort?.name))
        ).map {
            PurchaseResponse.from(
                purchase = it,
                member = entityFinder.getMember(it.proposedBy, refrigeratorId),
                purchaseProductList = purchaseProductRepository.findAllByPurchase(it)
                    .map { purchaseProduct -> PurchaseProductResponse.from(purchaseProduct) }
            )
        }

    /*
        [API] 이전 공동구매와 같은 PurchaseProduct 가 담긴 새로운 Purchase 를 생성
            - 검증 조건 1 : 관리자(STAFF)만 공동구매를 신청할 수 있음 [X]
            - 검증 조건 2 : 이미 진행 중인 공동구매가 있는 경우 신청할 수 없음
     */
    fun copyPurchase(userPrincipal: UserPrincipal, refrigeratorId: Long, purchaseId: Long) {
//        if (entityFinder.getMember(userPrincipal.id, refrigeratorId).role != MemberRole.STAFF)
//            throw InvalidRoleException()
        if (purchaseRepository.existsByStatus(PurchaseStatus.ACTIVE))
            throw AlreadyHaveActivePurchaseException()

        val currentPurchase = purchaseRepository.findByIdOrNull(purchaseId) ?: throw ModelNotFoundException("공동구매")
        val newPurchase = makePurchase(userPrincipal, entityFinder.getRefrigerator(refrigeratorId))

        purchaseProductRepository.findAllByPurchase(currentPurchase).forEach {
            purchaseProductRepository.save(
                PurchaseProduct(
                    count = it.count,
                    purchase = newPurchase,
                    product = it.product,
                    refrigerator = it.refrigerator
                )
            )
        }
    }

    // [내부 메서드] 현재 냉장고 내에 등록된 (해당 식품에 대한) Product 를 리턴 (없으면 새로운 Product 객체 생성 후 리턴)
    private fun getProduct(foodId: Long, refrigeratorId: Long): Product =
        productRepository.findByFoodAndRefrigerator(
            entityFinder.getFood(foodId),
            entityFinder.getRefrigerator(refrigeratorId)
        ) ?: addProduct(entityFinder.getFood(foodId), entityFinder.getRefrigerator(refrigeratorId))

    // [내부 메서드] Product 객체 생성
    fun addProduct(food: Food, refrigerator: Refrigerator) =
        productRepository.save(
            Product(food = food, refrigerator = refrigerator, newFoodName = null)
        )

    // [내부 메서드] 현재 진행 중인(status 가 ACTIVE 한) Purchase 를 리턴 (없으면 예외 처리)
    private fun getCurrentPurchase(refrigeratorId: Long) =
        purchaseRepository.findAllByRefrigerator(entityFinder.getRefrigerator(refrigeratorId))
            .firstOrNull { it.status == PurchaseStatus.ACTIVE }
            ?: throw NoCurrentPurchaseException()

    // [내부 메서드] 현재 진행 중인(status 가 ACTIVE 한) Purchase 를 리턴 (없으면 새로운 Purchase 객체 생성 후 리턴)
    private fun getCurrentPurchase(userPrincipal: UserPrincipal, refrigeratorId: Long) =
        purchaseRepository.findAllByRefrigerator(entityFinder.getRefrigerator(refrigeratorId))
            .firstOrNull { it.status == PurchaseStatus.ACTIVE }
            ?: makePurchase(userPrincipal, entityFinder.getRefrigerator(refrigeratorId))

    // [내부 메서드] Purchase 객체 생성
    private fun makePurchase(userPrincipal: UserPrincipal, refrigerator: Refrigerator) =
        purchaseRepository.save(
            Purchase(
                status = PurchaseStatus.ACTIVE,
                proposedBy = userPrincipal.id,
                refrigerator = refrigerator
            )
        )
}