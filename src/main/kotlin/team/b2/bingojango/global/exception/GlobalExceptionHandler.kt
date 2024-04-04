package team.b2.bingojango.global.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import team.b2.bingojango.global.exception.cases.*

@RestControllerAdvice
class GlobalExceptionHandler(
    private val httpServletRequest: HttpServletRequest
) {

    @ExceptionHandler(DuplicateValueException::class)
    fun handleDuplicateValueException(e: DuplicateValueException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(
                ErrorResponse(
                    httpStatus = "409 Conflict",
                    message = e.message.toString(),
                    path = httpServletRequest.requestURI
                )
            )
    }

    // 특정 엔터티 조회 실패
    @ExceptionHandler(ModelNotFoundException::class)
    fun handleModelNotFoundException(e: ModelNotFoundException)
            : ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    httpStatus = "404 Not Found",
                    message = e.message.toString(),
                    path = httpServletRequest.requestURI
                )
            )
    }

    // 잘못된 요청
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException)
            : ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    httpStatus = "400 Bad Request",
                    message = e.message.toString(),
                    path = httpServletRequest.requestURI
                )
            )
    }

    // role 에 맞지 않는 요청
    @ExceptionHandler(InvalidRoleException::class)
    fun handleInvalidRoleException(e: InvalidRoleException) =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(getErrorResponse(HttpStatus.UNAUTHORIZED, e))

    // 권한 없음
    @ExceptionHandler(InvalidCredentialException::class)
    fun handleInvalidCredentialException(e: InvalidCredentialException) =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(getErrorResponse(HttpStatus.UNAUTHORIZED, e))

    // 현재 진행 중인 공동구매가 존재
    @ExceptionHandler(AlreadyHaveActivePurchaseException::class)
    fun handleAlreadyHaveActivePurchaseException(e: AlreadyHaveActivePurchaseException) =
        ResponseEntity.badRequest().body(getErrorResponse(HttpStatus.BAD_REQUEST, e))

    // 현재 진행 중인 투표가 존재
    @ExceptionHandler(AlreadyHaveActiveVoteException::class)
    fun handleAlreadyHaveActiveVoteException(e: AlreadyHaveActiveVoteException) =
        ResponseEntity.badRequest().body(getErrorResponse(HttpStatus.BAD_REQUEST, e))

    // 현재 진행 중인 공동구매 없음
    @ExceptionHandler(NoCurrentPurchaseException::class)
    fun handleNoCurrentPurchaseException(e: NoCurrentPurchaseException) =
        ResponseEntity.badRequest().body(getErrorResponse(HttpStatus.BAD_REQUEST, e))

    // 이미 공동구매 목록 안에 신청하고자 하는 식품이 존재
    @ExceptionHandler(AlreadyInPurchaseException::class)
    fun handleAlreadyInPurchaseException(e: AlreadyInPurchaseException) =
        ResponseEntity.badRequest().body(getErrorResponse(HttpStatus.BAD_REQUEST, e))

    // 현재 진행 중인 투표 없음
    @ExceptionHandler(NoCurrentVoteException::class)
    fun handleNoCurrentVoteException(e: NoCurrentVoteException) =
        ResponseEntity.badRequest().body(getErrorResponse(HttpStatus.BAD_REQUEST, e))

    // 공동구매 목록 안에 식품이 없는 상태에서 투표 시작
    @ExceptionHandler(UnableToStartVoteException::class)
    fun handleUnableToStartVoteException(e: UnableToStartVoteException) =
        ResponseEntity.badRequest().body(getErrorResponse(HttpStatus.BAD_REQUEST, e))

    // 투표가 이미 진행 중인 공동구매에 대한 식품 추가/수정/삭제 시도
    @ExceptionHandler(AlreadyOnVoteException::class)
    fun handleAlreadyOnVoteException(e: AlreadyOnVoteException) =
        ResponseEntity.badRequest().body(getErrorResponse(HttpStatus.BAD_REQUEST, e))

    // 중복 투표
    @ExceptionHandler(DuplicatedVoteException::class)
    fun handleDuplicatedVoteException(e: DuplicatedVoteException) =
        ResponseEntity.badRequest().body(getErrorResponse(HttpStatus.BAD_REQUEST, e))

    // Validation 미통과
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException) =
        ResponseEntity.badRequest().body(
            ErrorResponse(
                httpStatus = "400 Bad Request",
                message = e.bindingResult.allErrors.toMutableList().first().defaultMessage!!,
                path = httpServletRequest.requestURI.toString()
            )
        )

    // 이미 동일한 음식 존재 (추가 불가)
    @ExceptionHandler(AlreadyExistsFoodException::class)
    fun handleAlreadyExistsFoodException(e: AlreadyExistsFoodException) =
        ResponseEntity.badRequest().body(getErrorResponse(HttpStatus.BAD_REQUEST, e))

    // 이미 STAFF 권한 있음 (권한 위임 불가)
    @ExceptionHandler(AlreadyHaveStaffAccessException::class)
    fun handleAlreadyHaveStaffAccessException(e: AlreadyHaveStaffAccessException) =
        ResponseEntity.badRequest().body(getErrorResponse(HttpStatus.BAD_REQUEST, e))

    // 권한 위임을 해야 함 (STAFF 인 본인 외 다른 STAFF 는 없고 다른 MEMBER 존재하면 위임 필수)
    @ExceptionHandler(MustAssignException::class)
    fun handleMustAssignException(e: MustAssignException) =
        ResponseEntity.badRequest().body(getErrorResponse(HttpStatus.BAD_REQUEST, e))

    private fun getErrorResponse(httpStatus: HttpStatus, e: Exception) = when (httpStatus) {
        HttpStatus.BAD_REQUEST -> ErrorResponse(
            httpStatus = "400 Bad Request",
            message = e.message.toString(),
            path = httpServletRequest.requestURI
        )

        HttpStatus.UNAUTHORIZED -> ErrorResponse(
            httpStatus = "401 Unauthorized",
            message = e.message.toString(),
            path = httpServletRequest.requestURI
        )

        else -> null
    }
}