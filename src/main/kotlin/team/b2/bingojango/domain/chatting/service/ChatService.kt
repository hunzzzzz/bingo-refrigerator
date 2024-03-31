package team.b2.bingojango.domain.chatting.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.repository.findByIdOrNull
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.ui.Model
import team.b2.bingojango.domain.chatting.dto.ChatRequest
import team.b2.bingojango.domain.chatting.dto.ChatResponse
import team.b2.bingojango.domain.chatting.model.Chat
import team.b2.bingojango.domain.chatting.model.ChatRoom
import team.b2.bingojango.domain.chatting.model.ChatStatus
import team.b2.bingojango.domain.chatting.repository.ChatRepository
import team.b2.bingojango.domain.chatting.repository.ChatRoomRepository
import team.b2.bingojango.domain.member.repository.MemberRepository
import team.b2.bingojango.domain.user.model.User
import team.b2.bingojango.domain.user.repository.UserRepository
import team.b2.bingojango.global.exception.cases.ModelNotFoundException
import team.b2.bingojango.global.security.util.UserPrincipal
import team.b2.bingojango.global.util.EntityFinder
import java.time.format.DateTimeFormatter

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val memberRepository: MemberRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val messageTemplate: SimpMessageSendingOperations,
    private val channelTopic: ChannelTopic,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val entityFinder: EntityFinder,
    private val userRepository: UserRepository,
) {

    // 채팅 전송 v1, 단일 서버
    @Transactional
    fun sendMessage(
        userPrincipal: UserPrincipal,
        request: ChatRequest,
    ): ChatResponse {
        val user = entityFinder.getUser(userPrincipal.id)
        val chatRoom = getChatRoomInfo(request.chatRoomId.toLong())
        val member = getMemberInfo(user, chatRoom)

        val save = chatRepository.save(
            Chat(
                content = request.content,
                status = ChatStatus.CHAT,
                chatRoom = chatRoom,
                member = member,
            )
        )
        val response = toResponse(save, user)
        messageTemplate.convertAndSend("/sub/chatroom/${response.chatRoomId}", response)

        return response
    }

    // 채팅 전송 v2, 스케일 아웃 고려 (v1, v2 구조가 비슷해서 duplicate 경고가 뜨는 중,
    // 추후 한 기능이 확정 시 다른 한 쪽은 삭제)
    @Transactional
    fun sendMessage2(
        userPrincipal: UserPrincipal,
        request: ChatRequest,
    ): ChatResponse {
        val user = entityFinder.getUser(userPrincipal.id)
        val chatRoom = getChatRoomInfo(request.chatRoomId.toLong())
        val member = getMemberInfo(user, chatRoom)

        val save = chatRepository.save(
            Chat(
                content = request.content,
                status = ChatStatus.CHAT,
                chatRoom = chatRoom,
                member = member,
            )
        )
        val response = toResponse(save, user)
        val topic = channelTopic.topic
        redisTemplate.convertAndSend(topic, response)
        return response
    }

    // [API] 채팅 내역 불러오기 v1 모든 내역 호출
    fun getAllChat(userPrincipal: UserPrincipal, chatRoomId: Long): List<ChatResponse> {
        val user = entityFinder.getUser(userPrincipal.id)
        val chatRoom = getChatRoomInfo(chatRoomId)
        val member = getMemberInfo(user, chatRoom)

        if (member.chatRoom.id != chatRoomId) throw IllegalArgumentException("냉장고의 멤버가 아니에요.")
        else {
            val chats = chatRepository.findAllByChatRoomId(chatRoomId)
            return chats.map {
                toResponse(it, it.member.user)
            }
        }
    }

    // [API] 채팅 내역 불러오기 v2 커서 페이지네이션 적용
    fun getAllChat2(userPrincipal: UserPrincipal, chatRoomId: Long, cursor: Long?, size: Int): List<ChatResponse> {
        val user = entityFinder.getUser(userPrincipal.id)
        val chatRoom = getChatRoomInfo(chatRoomId)
        val member = getMemberInfo(user, chatRoom)

        if (member.chatRoom.id != chatRoomId) throw IllegalArgumentException("냉장고의 멤버가 아니에요.")
        else {
            val pageable = PageRequest.of(0, size)
            return if (cursor == null) {
                chatRepository.findFirstPage(chatRoomId, pageable).map {
                    toResponse(it, user)
                }
            } else {
                chatRepository.findNextPage(chatRoomId, cursor, pageable).map {
                    toResponse(it, user)
                }
            }
        }
    }

    // 테스트용 채팅방 불러오기 (추후 삭제)
    fun getAllChatRoom(userPrincipal: UserPrincipal): List<Long> {
        val members = memberRepository.findAllByUserId(userPrincipal.id)
        val chatRooms = members.map { it.chatRoom.id!! }
        return chatRooms
    }

    fun getChatRoomNumber(refrigeratorId: Long):Long{
        return chatRoomRepository.findByRefrigeratorId(refrigeratorId).id ?: throw ModelNotFoundException("chatRoomId")
    }

    // 채팅방 ID에서 채팅방 정보 취득
    private fun getChatRoomInfo(chatRoomId: Long) =
        chatRoomRepository.findByIdOrNull(chatRoomId) ?: throw ModelNotFoundException("chatRoomId")

    // 유저, 채팅방 정보로부터 채팅방 멤버 정보 취득
    private fun getMemberInfo(user: User, chatRoom: ChatRoom) =
        memberRepository.findByUserAndChatRoom(user, chatRoom) ?: throw ModelNotFoundException("chatRoomId")

    // 채팅 및 유저 정보로 ChatResponse 생성 (+채팅의 본인 여부 확인)
    private fun toResponse(chat: Chat, user: User) =
        ChatResponse(
            chatRoomId = chat.chatRoom.id!!,
            content = chat.content,
            nickname = chat.member.user.nickname,
            status = chat.status,
            createdAt = chat.createdAt.format(DateTimeFormatter.ofPattern("M/d H:mm")),
            userId = user.id!!,
        )
}