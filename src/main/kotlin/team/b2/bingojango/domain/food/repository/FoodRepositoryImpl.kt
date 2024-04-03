package team.b2.bingojango.domain.food.repository

import com.querydsl.core.BooleanBuilder
import org.springframework.data.domain.*
import org.springframework.stereotype.Repository
import team.b2.bingojango.domain.food.model.Food
import team.b2.bingojango.domain.food.model.FoodCategory
import team.b2.bingojango.domain.food.model.QFood
import team.b2.bingojango.domain.food.model.SortFood
import team.b2.bingojango.global.querydsl.QueryDslSupport

@Repository
class FoodRepositoryImpl : QueryDslSupport(), CustomFoodRepository {

    private val food = QFood.food
    override fun findByFood(
        refrigeratorId: Long,
        page: Int,
        sort: SortFood?,
        category: FoodCategory?,
        count: Int?,
        keyword: String?
    ): Page<Food> {
        val whereClause = BooleanBuilder()
        val pageable: PageRequest = PageRequest.of(page - 1, 10, Sort.by(sort.toString())) //0페이지가 아닌 1페이지 부터 시작하도록 함.

        refrigeratorId.let { whereClause.and(food.refrigerator.id.eq(refrigeratorId)) }
        category?.let { whereClause.and(food.category.eq(category)) } //카테고리와 동일한 음식
        count?.let { whereClause.and(food.count.loe(count)) } //count 이하 갯수의 음식
        keyword?.let { whereClause.and(food.name.contains(keyword)) } //검색어를 포함한 음식

        val totalCount = queryFactory.select(food.count()).from(food).where(whereClause).fetchOne() ?: 0L
        val query = queryFactory.selectFrom(food)
            .where(whereClause)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
        if (pageable.sort.isSorted) {
            when (pageable.sort.first()?.property) {
                "FOOD_NAME" -> query.orderBy(food.name.asc()) //음식이름순
                "EXPIRATION_DATE" -> query.orderBy(food.expirationDate.asc()) //유통기한순
                "CREATED_AT" -> query.orderBy(food.createdAt.asc()) //생성일자순
                "UPDATED_AT" -> query.orderBy(food.updatedAt.desc()) //수정일자순
                else -> query.orderBy(food.id.asc())
            }
        }

        val contents = query.fetch()

        return PageImpl(contents, pageable, totalCount)
    }

    override fun findFirstPage(refrigeratorId: Long, pageable: Pageable): List<Food> {
        val query = queryFactory
            .selectFrom(food)
            .where(food.refrigerator.id.eq(refrigeratorId))
            .orderBy(food.name.asc())
            .limit(pageable.pageSize.toLong())
        return query.fetch()
    }

    override fun findNextPage(refrigeratorId: Long, cursorName: String, pageable: Pageable): List<Food> {
        val query = queryFactory
            .selectFrom(food)
            .where(
                food.refrigerator.id.eq(refrigeratorId),
                food.name.gt(cursorName)
            )
            .orderBy(food.name.asc())
            .limit(pageable.pageSize.toLong())
        return query.fetch()
    }
}