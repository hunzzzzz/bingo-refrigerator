package team.b2.bingojango.domain.purchase_product.dto.response

import team.b2.bingojango.domain.purchase_product.model.PurchaseProduct

data class PurchaseProductResponse(
    val productId: Long,
    val foodName: String,
    val foodCount: Int
) {
    companion object {
        fun from(purchaseProduct: PurchaseProduct) = PurchaseProductResponse(
            productId = purchaseProduct.product.id!!,
            foodName = purchaseProduct.product.food?.name ?: purchaseProduct.product.newFoodName ?: "",
            foodCount = purchaseProduct.count
        )
    }
}