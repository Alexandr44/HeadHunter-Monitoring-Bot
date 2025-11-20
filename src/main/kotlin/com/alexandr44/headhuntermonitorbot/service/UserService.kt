package com.alexandr44.headhuntermonitorbot.service

import com.alexandr44.headhuntermonitorbot.entity.User
import com.alexandr44.headhuntermonitorbot.enums.UserState
import com.alexandr44.headhuntermonitorbot.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    fun getUser(tgChatId: Long): User? {
        return userRepository.findByUserChatId(tgChatId)
    }

    fun getUserState(tgChatId: Long): UserState {
        return getUser(tgChatId)!!.userState
    }

    fun saveUser(user: User): User {
        return userRepository.save(user)
    }

    fun addNewUser(tgChatId: Long, userName: String) {
        if (getUser(tgChatId) == null) {
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

    @Transactional
    fun switchMonitoringUser(tgChatId: Long): Boolean {
        getUser(tgChatId)!!.let {
            it.active = !it.active
            return it.active
        }
    }

    @Transactional
    fun setUserState(tgChatId: Long, userState: UserState) {
        getUser(tgChatId)!!.userState = userState
    }

    @Transactional
    fun setSearchText(tgChatId: Long, searchText: String) {
        getUser(tgChatId)!!.searchText = searchText
    }

    @Transactional
    fun setExcludeText(tgChatId: Long, excludeText: String) {
        getUser(tgChatId)!!.excludeText = excludeText
    }

    @Transactional
    fun setCvId(tgChatId: Long, cvId: String) {
        getUser(tgChatId)!!.cvId = cvId
    }

}