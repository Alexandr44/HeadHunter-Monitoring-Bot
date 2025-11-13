package com.alexandr44.headhuntermonitorbot.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "vacancies_ids")
@EntityListeners(AuditingEntityListener::class)
class VacancyId(

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, name = "vacancy_id", length = 255)
    var vacancyId: Long,

    @Column(nullable = false, name = "user_id", length = 255)
    var userId: Long,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: Instant? = null

) {

    constructor() : this(
        vacancyId = 0,
        userId = 0
    )
}