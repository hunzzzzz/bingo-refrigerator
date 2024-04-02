package team.b2.bingojango.domain.vote.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import team.b2.bingojango.domain.vote.dto.request.VoteRequest
import team.b2.bingojango.domain.vote.service.VoteService
import team.b2.bingojango.global.security.util.UserPrincipal

@Tag(name = "vote", description = "투표")
@RestController
@RequestMapping("/refrigerator/{refrigeratorId}/vote")
class VoteController(
    private val voteService: VoteService
) {
    @Operation(summary = "현재 같이구매 목록에 대한 투표 현황 조회")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/current")
    fun showVote(
        @PathVariable refrigeratorId: Long
    ) =
        ResponseEntity.ok().body(voteService.showVote(refrigeratorId))

    @Operation(summary = "현재 같이구매 목록에 대한 투표 시작")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    fun startVote(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable refrigeratorId: Long,
        @Valid @RequestBody voteRequest: VoteRequest
    ) =
        ResponseEntity.ok().body(voteService.startVote(userPrincipal, refrigeratorId, voteRequest))

    @Operation(summary = "현재 같이구매 목록에 대한 투표")
    @PreAuthorize("isAuthenticated()")
    @PutMapping
    fun vote(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable refrigeratorId: Long,
        @RequestParam isAccepted: Boolean
    ) =
        ResponseEntity.ok().body(voteService.vote(userPrincipal, refrigeratorId, isAccepted))
}