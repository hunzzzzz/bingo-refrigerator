package team.b2.bingojango.domain.food.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import team.b2.bingojango.domain.food.service.FoodService
import team.b2.bingojango.domain.food.dto.AddFoodRequest
import team.b2.bingojango.domain.food.dto.UpdateFoodRequest
import org.springframework.web.bind.annotation.*
import team.b2.bingojango.domain.food.dto.FoodResponse
import team.b2.bingojango.domain.food.model.FoodCategory
import team.b2.bingojango.domain.food.model.SortFood

@RestController
@RequestMapping("/api/v1/refrigerator/{refrigeratorId}/foods")
class FoodController(
    private val foodService: FoodService
) {

    @GetMapping
    fun getFood(
            @PathVariable refrigeratorId: Long
    ): ResponseEntity<List<FoodResponse>>{
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(foodService.getFood(refrigeratorId))
    }

    @PostMapping
    fun addFood(
        @PathVariable refrigeratorId: Long,
        @RequestBody addFoodRequest: AddFoodRequest
    ): ResponseEntity<Unit> {
        foodService.addFood(refrigeratorId, addFoodRequest)
        return ResponseEntity(HttpStatus.CREATED)
    }

    @PutMapping("/{foodId}")
    fun updateFood(
        @PathVariable refrigeratorId: Long,
        @PathVariable foodId: Long,
        @RequestBody updateFoodRequest: UpdateFoodRequest
    ): ResponseEntity<Unit> {
        foodService.updateFood(refrigeratorId, foodId, updateFoodRequest)
        return ResponseEntity(HttpStatus.OK)
    }

    @PatchMapping("/{foodId}")
    fun updateFoodCount(
        @PathVariable refrigeratorId: Long,
        @PathVariable foodId: Long,
        @RequestParam count: Int
    ): ResponseEntity<Unit> {
        foodService.updateFoodCount(refrigeratorId, foodId, count)
        return ResponseEntity(HttpStatus.OK)
    }

    @DeleteMapping("/{foodId}")
    fun deleteFood(
        @PathVariable refrigeratorId: Long,
        @PathVariable foodId: Long
    ): ResponseEntity<Unit> {
        foodService.deleteFood(refrigeratorId, foodId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "냉장고의 음식 검색 및 정렬")
    @GetMapping("/search")
    fun searchFood(
        @PathVariable refrigeratorId: Long,
        @RequestParam
        (defaultValue = "0") page: Int,
        sort: SortFood?,
        category: FoodCategory?,
        count: Int?,
        keyword: String
    ): ResponseEntity<Page<FoodResponse>> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(foodService.searchFood(refrigeratorId, page, sort, category, count, keyword))
    }
}