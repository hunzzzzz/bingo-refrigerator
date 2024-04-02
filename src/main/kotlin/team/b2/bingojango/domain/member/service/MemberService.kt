package team.b2.bingojango.domain.member.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.b2.bingojango.domain.chatting.service.ChatRoomService
import team.b2.bingojango.domain.member.dto.MemberResponse
import team.b2.bingojango.domain.member.model.Member
import team.b2.bingojango.domain.member.model.MemberRole
import team.b2.bingojango.domain.member.repository.MemberRepository
import team.b2.bingojango.domain.refrigerator.model.Refrigerator
import team.b2.bingojango.domain.refrigerator.model.RefrigeratorStatus
import team.b2.bingojango.global.exception.cases.*
import team.b2.bingojango.global.security.util.UserPrincipal
import team.b2.bingojango.global.util.EntityFinder

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val chatRoomService: ChatRoomService,
    private val entityFinder: EntityFinder
) {
    /*
        [API] 냉장고 참여 멤버 조회
            - 냉장고 소속일 경우에만 멤버 조회 가능
    */
    @Transactional
    fun getMembers(userPrincipal: UserPrincipal, refrigeratorId: Long): List<MemberResponse> {
        val refrigerator = entityFinder.getRefrigerator(refrigeratorId)
        if (refrigerator.status != RefrigeratorStatus.NORMAL) throw ModelNotFoundException("Refrigerator")

        val user = entityFinder.getUser(userPrincipal.id)
        if (!memberRepository.existsByUserAndRefrigerator(user, refrigerator)) throw InvalidCredentialException()

        val members = memberRepository.findAllByRefrigerator(refrigerator)
        return members.sortedWith(compareBy<Member> { it.role }.thenBy { it.createdAt }).map { member ->
            MemberResponse(
                name = member.user.nickname,
                role = member.role,
                memberId = member.id!!,
                createdAt = member.createdAt
            )
        }
    }

    /*
        [API] 스태프 권한 위임
            - 검증 조건 1 : 본인 ROlE 이 STAFF 일 때만 권한을 위임할 수 있다
            - 검증 조건 2 : ROLE 이 MEMBER 인 다른 멤버가 존재해야 위임할 수 있다
            - 검증 조건 3 : 본인이 소속된 냉장고만 STAFF 권한을 위임할 수 있다
            - 검증 조건 4 : 본인 냉장고에 소속된 MEMBER 에게만 권한을 위임할 수 있다
    */
    @Transactional
    fun assignStaff(refrigeratorId: Long, memberId: Long, userPrincipal: UserPrincipal) {
        val refrigerator = entityFinder.getRefrigerator(refrigeratorId)
        if (refrigerator.status != RefrigeratorStatus.NORMAL) throw ModelNotFoundException("Refrigerator")
        val user = entityFinder.getUser(userPrincipal.id)
        val userMember =
            memberRepository.findByUserAndRefrigerator(user,refrigerator) ?: throw ModelNotFoundException("User's member")
        if (userMember.role != MemberRole.STAFF) {
            throw InvalidRoleException()
        }

        val member = memberRepository.findByIdOrNull(memberId) ?: throw ModelNotFoundException("memberId")
        if (member.refrigerator.id != refrigeratorId) {
            throw InvalidCredentialException()
        }
        if (member.role == MemberRole.STAFF) {
            throw AlreadyHaveStaffAccessException()
        }
        member.role = MemberRole.STAFF
        memberRepository.save(member)
    }

    /*
        [API] 멤버 탈퇴
            - 검증 조건 1 : MEMBER -> 자동탈퇴
            - 검증 조건 2 : STAFF -> 다른 STAFF 있으면 -> 자동탈퇴
            - 검증 조건 3 : STAFF -> 다른 MEMBER 있으면 -> 권한 위임 하라고 예외문구
            - 검증 조건 4 : STAFF -> 혼자 있을때 -> 냉장고 삭제
    */
    @Transactional
    fun withdrawMember(refrigeratorId: Long, userPrincipal: UserPrincipal) {
        val refrigerator = entityFinder.getRefrigerator(refrigeratorId)
        if (refrigerator.status != RefrigeratorStatus.NORMAL) throw ModelNotFoundException("Refrigerator")
        val member = entityFinder.getMember(userPrincipal.id, refrigeratorId)
        // staff 인지 확인 (아니면 자동 탈퇴)
        if (member.role != MemberRole.STAFF) {
            memberRepository.delete(member)
        }

        // staff 이면 -> 다른 멤버 있고, 그 중 staff 있으면 삭제 / 다른 멤버 없으면 냉장고 삭제
        else {
            val findMember = memberRepository.findAllByRefrigerator(member.refrigerator)
            val countMember = findMember.size
            val staffMemberList = findMember.filter { it.role == MemberRole.STAFF }

            if (countMember > 1 && staffMemberList.size > 1) {
                memberRepository.delete(member) // soft 처리 고려 (나간 멤버의 채팅 목록을 불러올 수 없을 듯)
            }
            if (countMember > 1 && staffMemberList.size == 1) {
                throw MustAssignException()
            }
            if (countMember == 1) {
                refrigerator.status = RefrigeratorStatus.DELETED
                chatRoomService.deleteChatRoom(refrigerator)
            }
        }
    }

    // [내부 메소드] 유저 아이디로 냉장고 찾기
    private fun findRefrigeratorByUserId(userId: Long): Refrigerator {
        val member = memberRepository.findByUserId(userId)
            ?: throw ModelNotFoundException("Member")
        return member.refrigerator
    }
}