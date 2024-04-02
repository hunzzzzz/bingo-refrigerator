package team.b2.bingojango.domain.purchase.dto.response

import jakarta.validation.constraints.NotBlank

data class AddNewFoodInPurchaseRequest(
    @field:NotBlank(message = "식품 이름은 필수 입력값입니다.")
    val name: String,

    @field:NotBlank(message = "식품 수량은 필수 입력값입니다.")
    val count: Int
)
