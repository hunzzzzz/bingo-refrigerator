package team.b2.bingojango.domain.purchase.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import team.b2.bingojango.domain.purchase.dto.response.AddNewFoodInPurchaseRequest
import team.b2.bingojango.domain.purchase.model.PurchaseSort
import team.b2.bingojango.domain.purchase.model.PurchaseStatus
import team.b2.bingojango.domain.purchase.service.PurchaseService
import team.b2.bingojango.global.security.util.UserPrincipal

@Tag(name = "purchase", description = "공동구매")
@RestController
@RequestMapping("/refrigerator/{refrigeratorId}/purchase")
class PurchaseController(
    private val purchaseService: PurchaseService
) {
    @Operation(summary = "특정 식품에 대한 같이구매 신청")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/foods/{foodId}")
    fun addFoodToPurchase(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable refrigeratorId: Long,
        @PathVariable foodId: Long,
        @RequestParam count: Int
    ) =
        ResponseEntity.ok().body(purchaseService.addFoodToPurchase(userPrincipal, refrigeratorId, foodId, count))

    @Operation(summary = "새로운 식품에 대한 같이구매 신청")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/foods")
    fun addNewFoodToPurchase(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable refrigeratorId: Long,
        @RequestBody addNewFoodInPurchaseRequest: AddNewFoodInPurchaseRequest
    ) =
        ResponseEntity.ok()
            .body(purchaseService.addNewFoodToPurchase(userPrincipal, refrigeratorId, addNewFoodInPurchaseRequest))

    @Operation(summary = "공동구매 목록에 포함된 식품의 개수 수정")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/product/{productId}")
    fun updateFoodInPurchase(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable refrigeratorId: Long,
        @PathVariable productId: Long,
        @RequestParam count: Int
    ) =
        ResponseEntity.ok().body(purchaseService.updateFoodInPurchase(userPrincipal, refrigeratorId, productId, count))

    @Operation(summary = "같이구매 목록에서 특정 식품 삭제")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/product/{productId}")
    fun deleteFoodFromPurchase(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable refrigeratorId: Long,
        @PathVariable productId: Long
    ) =
        ResponseEntity.ok().body(purchaseService.deleteFoodFromPurchase(userPrincipal, refrigeratorId, productId))

    @Operation(summary = "현재 진행 중인 같이구매를 출력")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    fun showPurchase(
        @PathVariable refrigeratorId: Long
    ) =
        purchaseService.showPurchase(refrigeratorId)

    @Operation(summary = "현재까지 진행된 모든 공동구매 목록을 출력")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/all")
    fun showPurchaseList(
        @PathVariable refrigeratorId: Long,

        @Parameter(description = "APPROVED = 승인된 공동구매, REJECTED = 취소된 공동구매, ACTIVE = 현재 진행 중인 공동구매")
        @RequestParam(required = false) status: PurchaseStatus?,

        @Parameter(description = "CREATED_AT = 최신순")
        @RequestParam(required = false) sort: PurchaseSort?,

        @Parameter(description = "페이지")
        @RequestParam(value = "page", defaultValue = "1") page: Int
    ) = purchaseService.showPurchaseList(refrigeratorId, status, sort, page)

    @Operation(summary = "완료/거절된 이전 공동구매를 선택 후 똑같은 내용의 공동구매 생성")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{purchaseId}")
    fun copyPurchase(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable refrigeratorId: Long,
        @PathVariable purchaseId: Long
    ) = purchaseService.copyPurchase(userPrincipal, refrigeratorId, purchaseId)
}