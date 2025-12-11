package com.alexandr44.headhuntermonitorbot.entity

import com.alexandr44.headhuntermonitorbot.enums.UserState
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class User(

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true, length = 255)
    var username: String,

    @Column(nullable = false, name = "user_chat_id", length = 255)
    var userChatId: Long,

    @Column(nullable = false, name = "is_active")
    var active: Boolean,

    @Column(nullable = false, name = "search_text", length = 255)
    var searchText: String,

    @Column(nullable = false, name = "exclude_text", length = 255)
    var excludeText: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "user_state", length = 255)
    var userState: UserState,

    @Column(name = "cv_id", length = 255)
    var cvId: String? = null,

    @Column(name = "message_pattern", length = 255)
    var messagePattern: String? = null,

    @Column(nullable = false, name = "auto_reply_enabled")
    var autoReplyEnabled: Boolean = false,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: Instant? = null

) {

    constructor() : this(
        username = "",
        userChatId = 0,
        active = true,
        searchText = "",
        excludeText = "",
        userState = UserState.OK
    )
}