package team.b2.bingojango.domain.user.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import team.b2.bingojango.domain.user.dto.request.*
import team.b2.bingojango.domain.user.dto.response.FindEmailResponse
import team.b2.bingojango.domain.user.dto.response.LoginResponse
import team.b2.bingojango.domain.user.dto.response.UploadImageResponse
import team.b2.bingojango.domain.user.dto.response.UserResponse
import team.b2.bingojango.domain.user.service.UserService
import team.b2.bingojango.global.security.util.UserPrincipal
import java.net.URI

@Tag(name = "user", description = "유저")
@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {
    @Operation(summary = "로그인")
    @PreAuthorize("isAnonymous()")
    @PostMapping("/login")
    fun login(
        @RequestBody @Valid loginRequest: LoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(userService.login(loginRequest, response))
    }

    @Operation(summary = "로그아웃")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<Unit> {
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .body(userService.logout(userPrincipal, request, response))
    }

    @Operation(summary = "회원가입")
    @PreAuthorize("isAnonymous()")
    @PostMapping("/signup")
    fun signUp(
        @RequestBody signUpRequest: SignUpRequest
    ): ResponseEntity<UserResponse> {
        return ResponseEntity
            .created(URI.create("/login"))
            .body(userService.signUp(signUpRequest))
    }

    @Operation(summary = "이메일 찾기")
    @PreAuthorize("isAnonymous()")
    @PostMapping("/finding/email")
    fun findEmail(@RequestBody @Valid request: FindEmailRequest): ResponseEntity<FindEmailResponse> {
        return ResponseEntity.ok().body(userService.findEmail(request))
    }

    @Operation(summary = "비밀번호 찾기")
    @PreAuthorize("isAnonymous()")
    @PostMapping("/finding/password")
    fun findPassword(@RequestBody request: FindPasswordRequest): ResponseEntity<Any> {
        return ResponseEntity.ok().body(userService.findPassword(request))
    }

    @Operation(summary = "본인 프로필 조회")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    fun getProfileByMe(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<UserResponse> {
        return ResponseEntity.ok().body(userService.getProfileByMe(userPrincipal))
    }

    @Operation(summary = "타인 프로필 조회")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{userId}")
    fun getProfileById(@PathVariable userId: Long): ResponseEntity<UserResponse> {
        return ResponseEntity.ok().body(userService.getProfileById(userId))
    }

    @Operation(summary = "프로필 수정")
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/change/profile")
    fun updateProfile(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @RequestBody @Valid profileUpdateRequest: ProfileUpdateRequest
    ): ResponseEntity<String> {
        userService.updateProfile(userPrincipal, profileUpdateRequest)
        return ResponseEntity.status(HttpStatus.OK).build()
    }

    @Operation(summary = "비밀번호 변경")
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/change/password")
    fun updatePassword(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @RequestBody passwordRequest: PasswordRequest
    ): ResponseEntity<String> {
        userService.updatePassword(userPrincipal, passwordRequest)
        return ResponseEntity.status(HttpStatus.OK).build()
    }

    @Operation(summary = "회원 탈퇴 (SoftDelete, Scheduled)")
    @PutMapping("/withdraw")
    fun withdrawUser(
        @RequestBody withdrawRequest: WithdrawRequest,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<String> {
        userService.withdrawUser(withdrawRequest, userPrincipal)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body("탈퇴가 정상적으로 완료되었습니다.")
    }

    @Operation(summary = "프로필 이미지 업로드")
    @PreAuthorize("isAuthenticated()")
    @PostMapping(
        "/images",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun uploadImage(
        @RequestParam("image") multipartFile: MultipartFile,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    )
            : ResponseEntity<UploadImageResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(userService.uploadImage(multipartFile, userPrincipal))
    }

}