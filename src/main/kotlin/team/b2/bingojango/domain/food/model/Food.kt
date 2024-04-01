package team.b2.bingojango.domain.food.model

import jakarta.persistence.*
import team.b2.bingojango.domain.food.dto.FoodResponse
import team.b2.bingojango.domain.refrigerator.model.Refrigerator
import team.b2.bingojango.global.entity.BaseEntity
import team.b2.bingojango.global.util.ZonedDateTimeConverter
import java.time.ZonedDateTime

@Entity
@Table(name = "Foods")
class Food(
    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    var category: FoodCategory,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "expiration_date", nullable = false)
    var expirationDate: ZonedDateTime,

    @Column(name = "count", nullable = false)
    var count: Int,

    @ManyToOne
    @JoinColumn(name = "refrigerator_id")
    val refrigerator: Refrigerator?
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_id", nullable = false)
    val id: Long? = null

    fun toResponse(): FoodResponse {
        return FoodResponse(
            id = id!!,
            category = category.name,
            name = name,
            expirationDate = ZonedDateTimeConverter.convertZonedDateTimeFromStringDateTime(expirationDate),
            count = count,
        )
    }
}