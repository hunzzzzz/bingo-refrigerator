package team.b2.bingojango.domain.purchase_product.model

import jakarta.persistence.*
import team.b2.bingojango.domain.product.model.Product
import team.b2.bingojango.domain.purchase.model.Purchase

@Entity
@Table(name = "Purchase_Product")
class PurchaseProduct(
    @Column(name = "count", nullable = false)
    val count: Int,

    @ManyToOne
    @JoinColumn(name = "purchase_id")
    val purchase: Purchase,

    @ManyToOne
    @JoinColumn(name = "product_id")
    val product: Product
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_product_id")
    val id: Long? = null
}