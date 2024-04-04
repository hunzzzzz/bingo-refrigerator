package team.b2.bingojango.domain.purchase.repository

import com.querydsl.core.BooleanBuilder
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import team.b2.bingojango.domain.purchase.model.PurchaseStatus
import team.b2.bingojango.domain.purchase.model.QPurchase
import team.b2.bingojango.domain.refrigerator.model.Refrigerator
import team.b2.bingojango.global.querydsl.QueryDslSupport

@Repository
class PurchaseRepositoryImpl : QueryDslSupport(), CustomPurchaseRepository {
    private val purchase = QPurchase.purchase
    override fun searchPurchase(
        refrigerator: Refrigerator,
        status: PurchaseStatus?,
        pageable: Pageable
    ) =
        BooleanBuilder().let {
            it.and(purchase.refrigerator.eq(refrigerator))
            status?.let { status -> it.and(purchase.status.eq(status)) }

            PageImpl(
                getContents(it, pageable),
                pageable,
                queryFactory.select(purchase.count()).from(purchase).where(it).fetchOne() ?: 0L
            )
        }

    // contents 추출
    private fun getContents(whereClaus: BooleanBuilder, pageable: Pageable) = queryFactory.selectFrom(purchase)
        .where(whereClaus)
        .offset(pageable.offset)
        .limit(pageable.pageSize.toLong())
        .orderBy(getOrderConditions(pageable))
        .fetch()

    // 정렬 조건 지정
    private fun getOrderConditions(pageable: Pageable) = when (pageable.sort.first()?.property) {
        "CREATED_AT" -> purchase.createdAt.desc()
        else -> purchase.createdAt.desc()
    }
}