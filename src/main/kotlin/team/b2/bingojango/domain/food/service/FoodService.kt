package team.b2.bingojango.domain.food.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.b2.bingojango.domain.food.dto.AddFoodRequest
import team.b2.bingojango.domain.food.dto.FoodResponse
import team.b2.bingojango.domain.food.dto.UpdateFoodRequest
import team.b2.bingojango.domain.food.model.Food
import team.b2.bingojango.domain.food.model.FoodCategory
import team.b2.bingojango.domain.food.model.SortFood
import team.b2.bingojango.domain.food.repository.FoodRepository
import team.b2.bingojango.domain.member.repository.MemberRepository
import team.b2.bingojango.domain.refrigerator.model.RefrigeratorStatus
import team.b2.bingojango.domain.refrigerator.repository.RefrigeratorRepository
import team.b2.bingojango.domain.user.repository.UserRepository
import team.b2.bingojango.global.exception.cases.AlreadyExistsFoodException
import team.b2.bingojango.global.exception.cases.InvalidCredentialException
import team.b2.bingojango.global.exception.cases.ModelNotFoundException
import team.b2.bingojango.global.security.util.UserPrincipal
import team.b2.bingojango.global.util.EntityFinder
import team.b2.bingojango.global.util.ZonedDateTimeConverter

@Service
@Transactional
class FoodService(
    private val foodRepository: FoodRepository,
    private val refrigeratorRepository: RefrigeratorRepository,
    private val memberRepository: MemberRepository,
    private val userRepository: UserRepository,
    private val entityFinder: EntityFinder
) {
    /*
        [API] 음식 조회
            - 검증조건 : 본인이 속한 냉장고 이면서 / 속한 냉장고에 들어있는 음식일 경우 조회 가능
    */
    fun getFoodList(userPrincipal: UserPrincipal, refrigeratorId: Long): List<FoodResponse> {
        validateAccessToRefrigerator(userPrincipal, refrigeratorId)
        return foodRepository.findByRefrigeratorId(refrigeratorId)
            .map {
                FoodResponse(
                    id = it.id!!,
                    category = it.category.name,
                    name = it.name,
                    expirationDate = ZonedDateTimeConverter.convertZonedDateTimeFromStringDateTime(it.expirationDate),
                    count = it.count,
                )
            }
    }

    /*
        [API] 음식 조회
    */
    fun getFoodByCursor(
        userPrincipal: UserPrincipal,
        refrigeratorId: Long,
        cursorName: String?,
        size: Int
    ): List<FoodResponse> {
        validateAccessToRefrigerator(userPrincipal, refrigeratorId)
        val pageable = PageRequest.of(0, size)
        return if (cursorName == null) {
            foodRepository.findFirstPage(refrigeratorId, pageable).map { it.toResponse() }
        } else {
            foodRepository.findNextPage(refrigeratorId, cursorName, pageable).map { it.toResponse() }
        }
    }

    /*
        [API] 음식 검색 및 정렬
    */
    fun searchFood(
        userPrincipal: UserPrincipal,
        refrigeratorId: Long,
        page: Int,
        sort: SortFood?,
        category: FoodCategory?,
        count: Int?,
        keyword: String?
    ): Page<FoodResponse> {
        validateAccessToRefrigerator(userPrincipal, refrigeratorId)
        return foodRepository.findByFood(refrigeratorId, page, sort, category, count, keyword).map { it.toResponse() }
    }

    /*
        [API] 음식 추가
            - 검증 조건 1 : 본인이 속한 냉장고에만 음식 추가 가능
            - 검증 조건 2 : 같은 음식 이름이 존재할 경우 추가 불가능
    */
    @Transactional
    fun addFood(userPrincipal: UserPrincipal, refrigeratorId: Long, request: AddFoodRequest) {
        validateAccessToRefrigerator(userPrincipal, refrigeratorId)
        val existsFood = foodRepository.existsByRefrigeratorIdAndName(refrigeratorId, request.name)
        if (existsFood) {
            throw AlreadyExistsFoodException()
        }
        val newFood = Food(
            category = request.category,
            name = request.name,
            expirationDate = ZonedDateTimeConverter.convertStringDateFromZonedDateTime(request.expirationDate),
            count = request.count,
            refrigerator = refrigeratorRepository.findByIdOrNull(refrigeratorId)
        )
        foodRepository.save(newFood)
    }

    /*
        [API] 음식 수정
            - 검증 조건 1 : 본인이 속한 냉장고에만 음식 수정 가능
            - 검증 조건 2-1 : 기존 음식 이름과 변경할 음식 이름이 같으면, 변경 값 저장
            - 검증 조건 2-2 : 기존 음식 이름과 변경할 음식 이름이 다르고, 해당 냉장고에 동일 음식 이름 없으면 변경 값 저장
            - 검증 조건 2-3 : 기존 음식 이름과 변경할 음식 이름이 다르고, 해당 냉장고에 동일 음식 이름 있으면 저장 불가능
    */
    @Transactional
    fun updateFood(userPrincipal: UserPrincipal, refrigeratorId: Long, foodId: Long, request: UpdateFoodRequest) {
        validateAccessToRefrigerator(userPrincipal, refrigeratorId)
        val food = findFood(refrigeratorId, foodId)
        if (food.name != request.name) {
            val existsFoodName = foodRepository.existsByRefrigeratorIdAndName(refrigeratorId, request.name)
            if (existsFoodName) {
                throw AlreadyExistsFoodException()
            }
        }
        food.category = FoodCategory.valueOf(request.category)
        food.name = request.name
        food.expirationDate = ZonedDateTimeConverter.convertStringDateFromZonedDateTime(request.expirationDate)
        foodRepository.save(food)
    }

    /*
        [API] 음식 수량 수정
            - 검증 조건 : 본인이 속한 냉장고에만 수량 수정 가능
    */
    @Transactional
    fun updateFoodCount(userPrincipal: UserPrincipal, refrigeratorId: Long, foodId: Long, count: Int) {
        validateAccessToRefrigerator(userPrincipal, refrigeratorId)
        val food = findFood(refrigeratorId, foodId)
        food.count = count
        foodRepository.save(food)
    }

    /*
        [API] 음식 삭제
            - 검증 조건 : 본인이 속한 냉장고에만 음식 삭제 가능
    */
    @Transactional
    fun deleteFood(userPrincipal: UserPrincipal, refrigeratorId: Long, foodId: Long) {
        validateAccessToRefrigerator(userPrincipal, refrigeratorId)
        val food = findFood(refrigeratorId, foodId)
        val product = entityFinder.getProductByFoodAndRefrigerator(foodId, refrigeratorId)

        product.updateFoodInNullWhenDeleteIt()
        foodRepository.delete(food)
    }

    // [내부 메서드] 존재하는 냉장고인지 확인 (soft delete 된 냉장고 제외)
    // 로그인한 유저가 가지고 있는 냉장고 맞는지 검증
    private fun validateAccessToRefrigerator(userPrincipal: UserPrincipal, refrigeratorId: Long) {
        val refrigerator =
            refrigeratorRepository.findByIdOrNull(refrigeratorId) ?: throw ModelNotFoundException("Refrigerator")
        if (refrigerator.status != RefrigeratorStatus.NORMAL) {
            throw ModelNotFoundException("Refrigerator")
        }
        val user = userRepository.findByIdOrNull(userPrincipal.id) ?: throw ModelNotFoundException("User")
        if (!memberRepository.existsByUserAndRefrigerator(user, refrigerator)) {
            throw InvalidCredentialException()
        }
    }

    // [내부 메서드] 냉장고에 속한 음식 맞는지 확인
    private fun findFood(refrigeratorId: Long, foodId: Long): Food {
        return foodRepository.findByIdAndRefrigeratorId(foodId, refrigeratorId) ?: throw ModelNotFoundException("Food")
    }

}