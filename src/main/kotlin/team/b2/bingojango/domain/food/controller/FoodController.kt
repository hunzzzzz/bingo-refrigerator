package team.b2.bingojango.domain.food.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import team.b2.bingojango.domain.food.dto.AddFoodRequest
import team.b2.bingojango.domain.food.dto.FoodResponse
import team.b2.bingojango.domain.food.dto.UpdateFoodRequest
import team.b2.bingojango.domain.food.model.FoodCategory
import team.b2.bingojango.domain.food.model.SortFood
import team.b2.bingojango.domain.food.service.FoodService
import team.b2.bingojango.global.security.util.UserPrincipal

@Tag(name = "food", description = "음식")
@RestController
@RequestMapping("/refrigerator/{refrigeratorId}/foods")
class FoodController(
    private val foodService: FoodService
) {
    @Operation(summary = "음식 조회")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    fun getFoodList(
            @PathVariable refrigeratorId: Long,
            @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<List<FoodResponse>>{
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(foodService.getFoodList(userPrincipal, refrigeratorId))
    }

    @Operation(summary = "음식 조회")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/cursor")
    fun getFoodByCursor(
        @PathVariable refrigeratorId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @RequestParam(name = "cursorName", required = false) cursorName: String?,
        @RequestParam(name = "size") size: Int
    ): ResponseEntity<List<FoodResponse>> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(foodService.getFoodByCursor(userPrincipal, refrigeratorId, cursorName, size))
    }

    @Operation(summary = "음식 추가")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    fun addFood(
        @PathVariable refrigeratorId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @RequestBody addFoodRequest: AddFoodRequest
    ): ResponseEntity<Unit> {
        foodService.addFood(userPrincipal, refrigeratorId, addFoodRequest)
        return ResponseEntity(HttpStatus.CREATED)
    }

    @Operation(summary = "음식 수정")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{foodId}")
    fun updateFood(
        @PathVariable refrigeratorId: Long,
        @PathVariable foodId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @RequestBody updateFoodRequest: UpdateFoodRequest
    ): ResponseEntity<Unit> {
        foodService.updateFood(userPrincipal, refrigeratorId, foodId, updateFoodRequest)
        return ResponseEntity(HttpStatus.OK)
    }

    @Operation(summary = "음식 수량 수정")
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{foodId}")
    fun updateFoodCount(
        @PathVariable refrigeratorId: Long,
        @PathVariable foodId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @RequestParam count: Int
    ): ResponseEntity<Unit> {
        foodService.updateFoodCount(userPrincipal, refrigeratorId, foodId, count)
        return ResponseEntity(HttpStatus.OK)
    }

    @Operation(summary = "음식 삭제")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{foodId}")
    fun deleteFood(
        @PathVariable refrigeratorId: Long,
        @PathVariable foodId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<Unit> {
        foodService.deleteFood(userPrincipal, refrigeratorId, foodId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "음식 검색 및 정렬")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    fun searchFood(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable refrigeratorId: Long,
        @RequestParam
            (defaultValue = "1") page: Int,
        sort: SortFood?,
        category: FoodCategory?,
        count: Int?,
        keyword: String?
    ): ResponseEntity<Page<FoodResponse>> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(foodService.searchFood(userPrincipal, refrigeratorId, page, sort, category, count, keyword))
    }
}