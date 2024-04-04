package team.b2.bingojango.domain.vote.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import team.b2.bingojango.domain.purchase.model.Purchase
import team.b2.bingojango.domain.refrigerator.model.Refrigerator
import team.b2.bingojango.domain.vote.model.Vote

@Repository
interface VoteRepository : JpaRepository<Vote, Long> {
    fun existsByPurchaseAndRefrigerator(purchase: Purchase, refrigerator: Refrigerator): Boolean

    fun existsByPurchase(purchase: Purchase): Boolean

    fun findByPurchase(purchase: Purchase): Vote?
}