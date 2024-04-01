package team.b2.bingojango.domain.food.dto

data class FoodResponse(
    val id: Long,
    val category: String,
    val name: String,
    val expirationDate: String,
    val count: Int
)