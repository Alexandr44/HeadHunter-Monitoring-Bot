package com.alexandr44.headhuntermonitorbot.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "tokens")
@EntityListeners(AuditingEntityListener::class)
class Token(

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, name = "user_id", unique = true)
    var userId: Long,

    @Column(nullable = false, name = "access_token")
    var accessToken: String? = null,

    @Column(nullable = false, name = "refresh_token")
    var refreshToken: String? = null,

    @Column(nullable = false, name = "expired_at")
    var expiredAt: Long? = null,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: Instant? = null

) {

    constructor() : this(
        userId = 0,
        accessToken = "",
        refreshToken = "",
        expiredAt = 0,
    )
}