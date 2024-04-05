package team.b2.bingojango.domain.purchase.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import team.b2.bingojango.domain.purchase.model.Purchase
import team.b2.bingojango.domain.purchase.model.PurchaseStatus
import team.b2.bingojango.domain.refrigerator.model.Refrigerator

@Repository
interface PurchaseRepository : JpaRepository<Purchase, Long>, CustomPurchaseRepository {
    fun findAllByRefrigerator(refrigerator: Refrigerator): List<Purchase>

    fun existsByStatusAndRefrigerator(status: PurchaseStatus, refrigerator: Refrigerator): Boolean
}