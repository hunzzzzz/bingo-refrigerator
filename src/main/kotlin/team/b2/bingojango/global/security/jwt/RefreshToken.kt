package team.b2.bingojango.global.security.jwt

import jakarta.persistence.*
import team.b2.bingojango.domain.user.model.User

@Entity
@Table(name = "refresh_token")
data class RefreshToken(
    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,
    var refreshToken: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id", nullable = false)
    val id: Long? = null
}

