package com.alexandr44.headhuntermonitorbot.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "secrets")
@EntityListeners(AuditingEntityListener::class)
class Secret(

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, name = "user_id", unique = true)
    var userId: Long,

    @Column(name = "clent_id")
    var clentId: String? = null,

    @Column(name = "client_secret")
    var clientSecret: String? = null,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: Instant? = null

) {

    constructor() : this(
        userId = 0,
        clentId = "",
        clientSecret = "",
    )
}