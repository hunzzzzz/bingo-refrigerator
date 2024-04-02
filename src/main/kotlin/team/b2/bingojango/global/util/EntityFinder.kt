package team.b2.bingojango.global.util

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.b2.bingojango.domain.food.repository.FoodRepository
import team.b2.bingojango.domain.member.repository.MemberRepository
import team.b2.bingojango.domain.product.repository.ProductRepository
import team.b2.bingojango.domain.refrigerator.repository.RefrigeratorRepository
import team.b2.bingojango.domain.user.repository.UserRepository
import team.b2.bingojango.global.exception.cases.ModelNotFoundException

@Service
@Transactional
class EntityFinder(
    private val refrigeratorRepository: RefrigeratorRepository,
    private val memberRepository: MemberRepository,
    private val userRepository: UserRepository,
    private val foodRepository: FoodRepository,
    private val productRepository: ProductRepository
) {
    fun getRefrigerator(refrigeratorId: Long) =
        refrigeratorRepository.findByIdOrNull(refrigeratorId) ?: throw ModelNotFoundException("냉장고")

    fun getMember(userId: Long, refrigeratorId: Long) =
        memberRepository.findByUserAndRefrigerator(
            user = getUser(userId),
            refrigerator = getRefrigerator(refrigeratorId)
        ) ?: throw ModelNotFoundException("멤버")

    fun getUser(userId: Long) =
        userRepository.findByIdOrNull(userId) ?: throw ModelNotFoundException("유저")

    fun getUserByEmail(email: String) =
        userRepository.findByEmail(email) ?: throw ModelNotFoundException("유저")

    fun getUserByNameAndPhone(name: String, phone: String) =
        userRepository.findByNameAndPhone(name, phone) ?: throw ModelNotFoundException("유저")

    fun getFood(foodId: Long) =
        foodRepository.findByIdOrNull(foodId) ?: throw ModelNotFoundException("식품")

    fun getProduct(productId: Long) =
        productRepository.findByIdOrNull(productId) ?: throw ModelNotFoundException("상품")
}