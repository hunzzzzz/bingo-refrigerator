package team.b2.bingojango.global.entity

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZoneId
import java.time.ZonedDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: ZonedDateTime

    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: ZonedDateTime

    @PrePersist
    fun prePersist() {
        createdAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        updatedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
    }
}