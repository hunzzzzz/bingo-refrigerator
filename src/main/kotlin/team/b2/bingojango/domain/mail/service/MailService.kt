package team.b2.bingojango.domain.mail.service

import org.springframework.stereotype.Service
import team.b2.bingojango.domain.mail.dto.MailResponse
import team.b2.bingojango.domain.mail.model.Mail
import team.b2.bingojango.domain.mail.repository.MailRepository
import team.b2.bingojango.domain.refrigerator.model.RefrigeratorStatus
import team.b2.bingojango.domain.user.repository.UserRepository
import team.b2.bingojango.global.exception.cases.ModelNotFoundException
import team.b2.bingojango.global.util.EntityFinder
import team.b2.bingojango.global.util.MailUtility

@Service
class MailService(
    private val mailRepository: MailRepository,
    private val mailUtility: MailUtility,
    private val entityFinder: EntityFinder,
    private val userRepository: UserRepository,
) {
    /*
        [API] 냉장고 초대 코드 발송
    */
    fun sendInvitationCode(refrigeratorId: Long, email: String): MailResponse {
        val refrigerator = entityFinder.getRefrigerator(refrigeratorId)
        if (refrigerator.status != RefrigeratorStatus.NORMAL) throw ModelNotFoundException("Refrigerator")
        val code = mailUtility.sendMail(email)
        val userWithEmailExists = userRepository.existsByEmail(email)
        if (!userWithEmailExists) throw IllegalArgumentException("해당 이메일을 가진 유저가 존재하지 않습니다.")

        mailRepository.save(Mail.toEntity(refrigerator, email, code))

        return MailResponse(message = "요청하신 이메일로 초대코드를 보냈습니다.", code = code)
    }


    /*
        [API] "비밀번호 찾기"메소드의 유저 임시 비밀번호 발급
    */
    fun sendEmail(to: String, subject: String, body: String) {

        mailUtility.sendMailFindingPassword(to, subject, body)


    }
}
