package team.b2.bingojango.domain.product.model

import jakarta.persistence.*
import team.b2.bingojango.domain.food.model.Food
import team.b2.bingojango.domain.refrigerator.model.Refrigerator

@Entity
@Table(name = "Products")
class Product(
    @OneToOne
    @JoinColumn(name = "food_id")
    var food: Food?,

    @ManyToOne
    @JoinColumn(name = "refrigerator_id")
    val refrigerator: Refrigerator,

    @Column(name = "new_food_name", nullable = true)
    val newFoodName: String?
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false)
    val id: Long? = null

    fun updateFoodInNullWhenDeleteIt(){
        this.food = null
    }
}