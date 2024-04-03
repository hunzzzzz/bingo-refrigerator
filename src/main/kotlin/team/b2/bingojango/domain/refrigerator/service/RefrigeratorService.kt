package team.b2.bingojango.domain.refrigerator.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.b2.bingojango.domain.chatting.service.ChatRoomService
import team.b2.bingojango.domain.mail.repository.MailRepository
import team.b2.bingojango.domain.member.model.Member
import team.b2.bingojango.domain.member.model.MemberRole
import team.b2.bingojango.domain.member.repository.MemberRepository
import team.b2.bingojango.domain.refrigerator.dto.request.AddRefrigeratorRequest
import team.b2.bingojango.domain.refrigerator.dto.request.JoinByInvitationCodeRequest
import team.b2.bingojango.domain.refrigerator.dto.request.JoinByPasswordRequest
import team.b2.bingojango.domain.refrigerator.dto.response.RefrigeratorResponse
import team.b2.bingojango.domain.refrigerator.model.QRefrigerator.refrigerator
import team.b2.bingojango.domain.refrigerator.model.Refrigerator
import team.b2.bingojango.domain.refrigerator.model.RefrigeratorStatus
import team.b2.bingojango.domain.refrigerator.repository.RefrigeratorRepository
import team.b2.bingojango.global.exception.cases.DuplicateValueException
import team.b2.bingojango.global.exception.cases.ModelNotFoundException
import team.b2.bingojango.global.security.util.UserPrincipal
import team.b2.bingojango.global.util.EntityFinder

@Service
class RefrigeratorService(
    private val refrigeratorRepository: RefrigeratorRepository,
    private val memberRepository: MemberRepository,
    private val chatRoomService: ChatRoomService,
    private val mailRepository: MailRepository,
    private val entityFinder: EntityFinder,
) {
    /*
        [API] 냉장고 목록 조회
           - 로그인한 유저 정보로 멤버 조회
           - 해당 멤버의 냉장고 조회
           - 삭제된 냉장고는 제외하기
           - 냉장고 목록 반환
   */
    fun getRefrigerator(userPrincipal: UserPrincipal): List<RefrigeratorResponse> {
        val member = memberRepository.findAllByUserId(userPrincipal.id)
        val refrigerator = member.map { it.refrigerator }
        val filtered = refrigerator.filter { it.status == RefrigeratorStatus.NORMAL }
        return filtered.map { it.toResponse() }
    }

    /*
        [API] 신규 냉장고 생성
           - 냉장고 이름 중복 여부 확인
           - 비밀번호와 비밀번호확인의 일치 여부
           - 냉장고 생성
           - 채팅방 생성
           - 멤버 생성 후, 생성한 사람에게 STAFF 권한 부여
           - RefrigeratorResponse 반환
   */
    @Transactional
    fun addRefrigerator(userPrincipal: UserPrincipal, request: AddRefrigeratorRequest): RefrigeratorResponse {
        if (refrigeratorRepository.existsRefrigeratorByName(request.name)) throw DuplicateValueException("중복된 냉장고 이름 입니다.")
        if (request.password != request.rePassword) throw IllegalArgumentException("비밀번호와 비밀번호확인이 일치하지 않습니다.")
        val user = entityFinder.getUser(userPrincipal.id)
        val refrigerator = refrigeratorRepository.save(Refrigerator.toEntity(request))
        val chatRoom = chatRoomService.buildChatRoom(refrigerator, userPrincipal)
        memberRepository.save(Member.toEntity(user, MemberRole.STAFF, refrigerator, chatRoom))
        return refrigerator.toResponse()
    }

    /*
        [API] 기존 냉장고 참여 - 비밀번호 이용
           - 냉장고 존재 유무 확인
           - 냉장고 비밀번호 일치 여부 확인
           - 채팅방 참여
           - Member 권한으로 멤버로 참여
           - RefrigeratorResponse 반환
   */
    @Transactional
    fun joinRefrigeratorByPassword(userPrincipal: UserPrincipal, request: JoinByPasswordRequest): RefrigeratorResponse {
        val user = entityFinder.getUser(userPrincipal.id)
        val refrigerator =
            refrigeratorRepository.findByName(request.name) ?: throw ModelNotFoundException("Refrigerator")
        if (refrigerator.password != request.password) throw IllegalArgumentException("냉장고의 비밀번호가 일치하지 않습니다.")
        if (memberRepository.existsByUserAndRefrigerator(user, refrigerator)) throw IllegalStateException("이미 해당 냉장고에 참여하셨습니다.")

        val chatRoom = chatRoomService.getChatRoom(refrigerator)
        memberRepository.save(Member.toEntity(user, MemberRole.MEMBER, refrigerator, chatRoom))

        return refrigerator.toResponse()
    }

    /*
        [API] 기존 냉장고 참여 - 초대코드 이용
           - 유저 확인
           - 메일 코드 확인
           - 냉장고 참여
           - 채팅방 참여
           - Member 권한으로 멤버로 참여
           - RefrigeratorResponse 반환
   */
    @Transactional
    fun joinRefrigeratorByInvitationCode(
        userPrincipal: UserPrincipal,
        request: JoinByInvitationCodeRequest
    ): RefrigeratorResponse {
        val user = entityFinder.getUser(userPrincipal.id)
        val mail = mailRepository.findByCode(request.invitationCode) ?: throw ModelNotFoundException("Mail")
        val refrigerator = mail.refrigerator
        if (memberRepository.existsByUserAndRefrigerator(user, refrigerator)) throw IllegalStateException("이미 해당 냉장고에 참여하셨습니다.")
        val chatRoom = chatRoomService.getChatRoom(refrigerator)

        memberRepository.save(Member.toEntity(user, MemberRole.MEMBER, refrigerator, chatRoom))

        return refrigerator.toResponse()
    }
}