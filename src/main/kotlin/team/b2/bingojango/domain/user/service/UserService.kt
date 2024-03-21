package team.b2.bingojango.domain.user.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import team.b2.bingojango.domain.mail.service.MailService
import team.b2.bingojango.domain.member.repository.MemberRepository
import team.b2.bingojango.domain.user.dto.request.*
import team.b2.bingojango.domain.user.dto.response.*
import team.b2.bingojango.domain.user.model.User
import team.b2.bingojango.domain.user.model.UserStatus
import team.b2.bingojango.domain.user.repository.UserRepository
import team.b2.bingojango.global.aws.S3Service
import team.b2.bingojango.global.exception.cases.InvalidCredentialException
import team.b2.bingojango.global.exception.cases.ModelNotFoundException
import team.b2.bingojango.global.exception.cases.UserNotFoundException
import team.b2.bingojango.global.security.TokenGenerator
import team.b2.bingojango.global.security.jwt.JwtPlugin
import team.b2.bingojango.global.security.util.CookieUtil
import team.b2.bingojango.global.security.util.TokenUtil
import team.b2.bingojango.global.security.util.UserPrincipal
import java.time.LocalDateTime
import java.time.ZonedDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtPlugin: JwtPlugin,
    private val mailService: MailService,
    private val tokenGenerator: TokenGenerator,
    private val tokenUtil: TokenUtil,
    private val memberRepository: MemberRepository,
    private val s3Service: S3Service
) {
    //로그인
    fun login(request: LoginRequest,
              response: HttpServletResponse
    ): LoginResponse {
        //이메일, 비밀번호 확인
        val user = userRepository.findByEmail(request.email) ?: throw InvalidCredentialException("이메일 또는 비밀번호를 확인해주세요.")
        if (!passwordEncoder.matches(request.password, user.password)) throw InvalidCredentialException("이메일 또는 비밀번호를 확인해주세요.")

        //RefreshToken 생성 후 쿠키 반환, DB 저장
        val refreshToken = jwtPlugin.generateRefreshToken(user.id.toString(), user.email, user.role.name)
        val cookieExpirationHour = 24 * 60 * 60 // 쿠키유효시간(24시간, 초 단위)
        CookieUtil.addCookie(response,"refreshToken",refreshToken, cookieExpirationHour) //RefreshToken 쿠키 반환
        tokenUtil.storeToken(user, refreshToken) //RefreshToken DB 저장

        //AccessToken 생성 후 반환
        val accessToken = jwtPlugin.generateAccessToken(user.id.toString(), user.email, user.role.name)
        return LoginResponse(accessToken)
    }

    //로그아웃
    @Transactional
    fun logout(
        userPrincipal: UserPrincipal,
        request: HttpServletRequest,
        response: HttpServletResponse
    ){
        tokenUtil.deleteToken(userPrincipal) //RefreshToken DB 삭제
        CookieUtil.deleteCookie(request,response,"refreshToken") //RefreshToken 쿠키 삭제
    }

    // 회원가입 성공 및 실패
    fun signUp(signUpRequest: SignUpRequest): SignUpResponse {
//        try {
            // 비밀번호 유효성 검사
            validatePassword(signUpRequest.password, signUpRequest.passwordConfirm)

            // 필수 필드에 빈 문자열("")이 있는지 확인
            if (signUpRequest.name.isBlank() || signUpRequest.email.isBlank() || signUpRequest.phone.isBlank() || signUpRequest.password.isBlank()) {
                throw IllegalArgumentException("모든 필수 필드를 입력해주세요.")
            }

            // 이메일 형식 검사
            if (!isValidEmail(signUpRequest.email)) {
                throw IllegalArgumentException("올바른 이메일 형식이 아닙니다.")
            }

            // 전화번호 형식 검사
            if (!isValidPhoneNumber(signUpRequest.phone)) {
                throw IllegalArgumentException("올바른 전화번호 형식이 아닙니다.")
            }

            // 이메일 중복 검사
            if (userRepository.existsByEmail(signUpRequest.email)) {
                throw IllegalArgumentException("이미 등록된 이메일 주소입니다.")
            }

            // 닉네임 중복 검사
            if (userRepository.existsByNickname(signUpRequest.nickname)) {
                throw IllegalArgumentException("이미 사용 중인 닉네임입니다.")
            }

            // 회원가입 처리
            val createdUserId = create(signUpRequest)

            val success = true
            val message = "회원가입이 성공적으로 완료되었습니다."

            return SignUpResponse(
                name = signUpRequest.name,
                nickname = signUpRequest.nickname,
                email = signUpRequest.email,
                phone = signUpRequest.phone,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                id = createdUserId,
                success = success,
                message = message
            )
//        } catch (e: IllegalArgumentException) {
//            val success = false
//            val message = e.message ?: "회원가입에 실패하였습니다. 잠시 후 다시 시도해주세요."
//
//            return SignUpResponse(
//                name = signUpRequest.name,
//                nickname = signUpRequest.nickname,
//                email = signUpRequest.email,
//                phone = signUpRequest.phone,
//                createdAt = LocalDateTime.now(),
//                updatedAt = LocalDateTime.now(),
//                id = 0,
//                success = success,
//                message = message
//            )
//        }
    }

    // 비밀번호 유효성 검사 메서드 추가
    private fun validatePassword(password: String, passwordConfirm: String) {
        // 비밀번호 길이 확인
        if (password.length !in 8..16) {
            throw IllegalArgumentException("비밀번호는 8~16자 이어야 합니다.")
        }

        // 비밀번호 패턴 확인
        val passwordPattern = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$")
        if (!password.matches(passwordPattern)) {
            throw IllegalArgumentException("비밀번호는 알파벳 대소문자, 숫자, 특수문자를 포함해야 합니다.")
        }

        // 비밀번호 확인과의 일치 확인
        if (password != passwordConfirm) {
            throw IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.")
        }
    }

    // 이메일 형식 검사 함수
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,6}\$")
        return emailPattern.matches(email)
    }

    // 전화번호 형식 검사 함수
    private fun isValidPhoneNumber(phone: String): Boolean {
        val phonePattern = Regex("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}\$")
        return phonePattern.matches(phone)
    }

    fun create(signUpRequest: SignUpRequest): Long {
        //회원가입 요청에서 받은 정보로 User 객체 생성
        val newUser = User(
                name = signUpRequest.name,
                nickname = signUpRequest.nickname,
                email = signUpRequest.email,
                phone = signUpRequest.phone,
                password = passwordEncoder.encode(signUpRequest.password),
                provider = null,
                providerId = null,
                image = null
        )

        // 사용자 저장
        val savedUser = userRepository.save(newUser)

        // 저장된 사용자의 ID 반환
        return savedUser.id ?: throw IllegalStateException("Failed to create user")
    }

    // 이메일 찾기
    fun findEmail(request: FindEmailRequest): FindEmailResponse {
        // 실명과 폰 번호로 사용자를 찾습니다.
        val user = userRepository.findByNameAndPhone(request.name, request.phone)
            ?: throw UserNotFoundException("사용자를 찾을 수 없습니다.")

        // 사용자의 이메일을 가져옵니다.
        val email = user.email

        // 이메일을 마스킹하여 반환합니다.
        val maskedEmail = maskEmail(email)

        return FindEmailResponse(maskedEmail)
    }

    // 이메일 마스킹 함수
    fun maskEmail(email: String): String {
        val atIndex = email.indexOf('@')
        if (atIndex == -1) {
            // @ 문자가 없는 이메일 형식일 경우 그대로 반환
            return email
        }

        val username = email.substring(0, atIndex)
        val domain = email.substring(atIndex)
        val maskedUsername = maskString(username, 5) // 아이디 부분을 5글자로 숨김 처리

        return "$maskedUsername$domain"
    }

    // 문자열 마스킹 함수
    fun maskString(str: String, visibleChars: Int): String {
        val maskedLength = str.length - visibleChars
        if (maskedLength <= 0) {
            // 숨길 문자가 없는 경우 그대로 반환
            return str
        }

        val maskedChars = "*".repeat(maskedLength)
        val visibleCharsStr = str.substring(0, visibleChars)

        return "$visibleCharsStr$maskedChars"
    }

    //비밀번호 찾기
    fun findPassword(request: FindPasswordRequest) {
        // 여기에서 요청된 이메일을 가진 사용자를 데이터베이스에서 찾습니다.
        val user = userRepository.findByEmail(request.email)
            ?: throw UserNotFoundException("User not found with email: ${request.email}")

        // 임시 비밀번호 생성
        val newPassword = tokenGenerator.generateToken()

        // 비밀번호 업데이트
        user.password = newPassword

        // 사용자 정보 저장 (비밀번호 업데이트)
        userRepository.save(user)

        // 이메일로 임시 비밀번호 전송
        val subject = "임시 비밀번호 발급 안내"
        val body = "안녕하세요, ${user.email}님.\n\n새로운 임시 비밀번호는 다음과 같습니다: $newPassword\n\n로그인 후 비밀번호를 변경해주세요."
        mailService.sendEmail(user.email, subject, body)
    }
    
    //비밀번호 재설정
    fun resetPassword(request: PasswordResetRequest) {
        // 여기에서 요청된 이메일을 가진 사용자를 데이터베이스에서 찾습니다.
        val user = userRepository.findByEmail(request.email)
            ?: throw UserNotFoundException("User not found with email: ${request.email}")

        // 임시 비밀번호 생성
        val newPassword = tokenGenerator.generateToken()

        // 비밀번호 업데이트
        user.password = newPassword

        // 사용자 정보 저장 (비밀번호 업데이트)
        userRepository.save(user)

        // 이메일로 임시 비밀번호 전송
        val subject = "임시 비밀번호 발급 안내"
        val body = "안녕하세요, ${user.email}님.\n\n새로운 임시 비밀번호는 다음과 같습니다: $newPassword\n\n로그인 후 비밀번호를 변경해주세요."
        mailService.sendEmail(user.email, subject, body)
    }
    
    // [API] 로그인한 본인 프로필 보기
    @Transactional
    fun getMyProfile(userPrincipal: UserPrincipal): MyProfileResponse {
        val user = userRepository.findByIdOrNull(userPrincipal.id) ?: throw ModelNotFoundException("Id")
        return MyProfileResponse (
                name = user.name,
                nickname = user.nickname,
                email = user.email,
                phone = user.phone,
                refrigerators = memberRepository.findAllByUserId(user.id!!).map { it.refrigerator.toResponse() },
                createdAt = user.createdAt
        )
    }

    // [API] 타인 프로필 보기
    // 로그인한 유저가 본인 userId 조회 시 내 프로필보기로 넘어가기 (더 상세 정보 조회됨)
    @Transactional
    fun getUser(userId: Long, userPrincipal: UserPrincipal): UserResponse {
        val user = userRepository.findByIdOrNull(userId) ?: throw ModelNotFoundException("userId")
        if (userId == userPrincipal.id) {return getMyProfile(userPrincipal)}
        return UserResponse(
                nickname = user.nickname,
                email = user.email,
                refrigerators = memberRepository.findAllByUserId(userId).map { it.refrigerator.toResponse() },
                createdAt = user.createdAt
        )
    }

    // 유저 프로필 수정
    @Transactional
    fun updateUserProfile(request: EditRequest, userPrincipal: UserPrincipal) {
        val user = getUserInfo(userPrincipal)
        if (userRepository.existsByNickname(request.nickname)) throw IllegalArgumentException("존재하는 닉네임이에요.")
        if (userRepository.existsByEmail(request.email)) throw IllegalArgumentException("존재하는 이메일이에요.")

        user.name = request.name
        user.nickname = request.nickname
        user.email = request.email
        user.phone = request.phone

        userRepository.save(user)
    }

    // 유저 비밀번호 수정
    @Transactional
    fun updateUserPassword(request: PasswordRequest, userPrincipal: UserPrincipal) {
        val user = getUserInfo(userPrincipal)
        if (!passwordEncoder.matches(user.password, request.password)) throw IllegalArgumentException("기존의 비밀번호가 일치하지 않아요.")
        if (request.newPassword != request.reNewPassword) throw IllegalArgumentException("새로운 비밀번호과 비밀번호 확인이 일치하지 않아요.")

        user.password = passwordEncoder.encode(request.newPassword)

        userRepository.save(user)
    }

    // 유저 탈퇴
    @Transactional
    fun withdrawUser(request: PasswordRequest, userPrincipal: UserPrincipal) {
        val user = getUserInfo(userPrincipal)
        if (!passwordEncoder.matches(user.password, request.password)) throw IllegalArgumentException("비밀번호가 일치하지 않아요.")

        user.status = UserStatus.WITHDRAWN

        userRepository.save(user)
    }

    //탈퇴한 유저 영구 삭제 (90일 유예), 매일 04시에 확인
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    fun deleteWithdrawnUser() {
        val daysOver = ZonedDateTime.now().minusDays(90)
        val users = userRepository.findAllByStatus(UserStatus.WITHDRAWN)
                .filter { it.updatedAt.isAfter(daysOver) }
        if (users.isNotEmpty()) {
            for (user in users) {
                userRepository.delete(user)
            }
        }
    }

    // [API] 프로필 이미지 업로드
    @Transactional
    fun uploadImage(multipartFile: MultipartFile, userPrincipal: UserPrincipal): UploadImageResponse {
        val user = userRepository.findByIdOrNull(userPrincipal.id) ?: throw ModelNotFoundException("User")
        val url = s3Service.upload(multipartFile)
        user.image = url
        userRepository.save(user)
        return UploadImageResponse(url)
    }

    private fun getUserInfo(userPrincipal: UserPrincipal) = userRepository.findByIdOrNull(userPrincipal.id)
            ?: throw ModelNotFoundException("id")

}