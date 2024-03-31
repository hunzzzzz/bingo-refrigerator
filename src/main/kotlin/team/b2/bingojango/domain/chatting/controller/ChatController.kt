package team.b2.bingojango.domain.chatting.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.b2.bingojango.domain.chatting.dto.ChatRequest
import team.b2.bingojango.domain.chatting.dto.ChatResponse
import team.b2.bingojango.domain.chatting.model.ChatRoom
import team.b2.bingojango.domain.chatting.service.ChatRoomService
import team.b2.bingojango.domain.chatting.service.ChatService
import team.b2.bingojango.global.security.jwt.JwtAuthenticationToken
import team.b2.bingojango.global.security.util.UserPrincipal

@Tag(name = "chat", description = "채팅")
//@RequestMapping("/refrigerator/{refrigeratorId}")
@RestController
class ChatController(
    private val chatService: ChatService,
) {

    // [API] 채팅 전송
    @Operation(summary = "채팅 발송 (싱글 서버)")
    @PreAuthorize("isAuthenticated()")
    @MessageMapping("/chatroom/sendMessage")
    fun sendMessage(
        @Payload chatRequest: ChatRequest,
        accessor: StompHeaderAccessor,
    ): ChatResponse {
        val userPrincipal = (accessor.user as JwtAuthenticationToken).principal
        return chatService.sendMessage(userPrincipal, chatRequest)
    }

    // [API] 스케일 아웃을 고려한 채팅 전송
    @Operation(summary = "채팅 발송 (서버 확장 레디스)")
    @PreAuthorize("isAuthenticated()")
    @MessageMapping("api/v2/chatroom/sendMessage2")
    fun sendMessage2(
        chatRequest: ChatRequest,
        accessor: StompHeaderAccessor,
    ): ChatResponse {
        val userPrincipal = (accessor.user as JwtAuthenticationToken).principal
        return chatService.sendMessage2(userPrincipal, chatRequest)
    }

    // 퇴장 시 감지 코드 (현재 미도입)
//        @EventListener
//        fun webSocketDisconnectListener(event: SessionDisconnectEvent) {
//            val headerAccessor = StompHeaderAccessor.wrap(event.message)
//            val userUUID = headerAccessor.sessionAttributes?.get("userUUID")
//            val roomId = headerAccessor.sessionAttributes?.get("roomId")
//        }

    // 채팅 목록 가져오기 v1
    @Operation(summary = "채팅 내역 출력 (임시)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/v1/chatroom/{chatRoomId}")
    fun getAllChat(
        @PathVariable chatRoomId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<List<ChatResponse>> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(chatService.getAllChat(userPrincipal, chatRoomId))
    }

    // 채팅 목록 가져오기 v2 커서 페이지네이션 적용, cursor는 불러온 메시지 중 가장 첫번째 id값
    @Operation(summary = "채팅 내역 출력 (커서 페이지네이션)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/v2/chatroom/{chatRoomId}")
    fun getAllChat2(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable chatRoomId: Long,
        @RequestParam(name = "cursor", required = false) cursor: Long?,
        @RequestParam(name = "size") size: Int,
    ): ResponseEntity<List<ChatResponse>> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(chatService.getAllChat2(userPrincipal, chatRoomId, cursor, size))
    }

    // 테스트용 채팅방 목록 호출 (추후 삭제)
    @Operation(summary = "구독한 채팅방 번호들 호출 (임시)")
    @GetMapping("/v1/chatroom/list")
    fun getAllChatRoom(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
    ): ResponseEntity<List<Long>> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(chatService.getAllChatRoom(userPrincipal))
    }

    @Operation(summary = "냉장고 id로 채팅방 id 호출")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/v1/chatroom/get-number")
    fun getChatRoomNumber(
        refrigeratorId: Long
    ): ResponseEntity<Long>{
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(chatService.getChatRoomNumber(refrigeratorId))
    }
}