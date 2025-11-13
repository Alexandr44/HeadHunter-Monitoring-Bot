package com.alexandr44.headhuntermonitorbot.service

import com.alexandr44.headhuntermonitorbot.entity.User
import com.alexandr44.headhuntermonitorbot.enums.UserState
import com.alexandr44.headhuntermonitorbot.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    fun getUserState(tgChatId: Long): UserState {
        return userRepository.findByUserChatId(tgChatId).userState
    }

    fun saveUserState(tgChatId: Long, userState: UserState) {
        userRepository.save(
            userRepository.findByUserChatId(tgChatId).apply {
                this.userState = userState
            }
        )
    }

    fun switchUser(tgChatId: Long): Boolean {
        val enabled: Boolean
        userRepository.save(
            userRepository.findByUserChatId(tgChatId).apply {
                this.active = !this.active
                enabled = this.active
            }
        )
        return enabled
    }

    fun setSearchText(tgChatId: Long, searchText: String) {
        userRepository.save(
            userRepository.findByUserChatId(tgChatId).apply {
                this.searchText = searchText
            }
        )
    }

    fun setExcludeText(tgChatId: Long, excludeText: String) {
        userRepository.save(
            userRepository.findByUserChatId(tgChatId).apply {
                this.excludeText = excludeText
            }
        )
    }

    fun addNewUser(tgChatId: Long, userName: String) {
        userRepository.save(
            User(
                username = userName,
                userChatId = tgChatId,
                active = false,
                searchText = "",
                excludeText = "",
                userState = UserState.OK
            )
        )
    }

}