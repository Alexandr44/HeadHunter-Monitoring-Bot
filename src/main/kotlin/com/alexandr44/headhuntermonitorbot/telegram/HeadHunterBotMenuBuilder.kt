package com.alexandr44.headhuntermonitorbot.telegram

import com.alexandr44.headhuntermonitorbot.dto.Constants
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@Service
class HeadHunterBotMenuBuilder {

    fun mainMenu(): ReplyKeyboardMarkup {

        val row1 = KeyboardRow()
        row1.add(Constants.MENU_CONFIGS)

        val row2 = KeyboardRow()
        row2.add(Constants.MENU_CREDS)

        val row3 = KeyboardRow()
        row3.add(Constants.MENU_SUPPORT)
        row3.add(Constants.MENU_HELP)

        return buildReplyKeyboard(row1, row2, row3)
    }

    fun searchSettingsMenu(): ReplyKeyboardMarkup {
        val row1 = KeyboardRow().apply {
            add(Constants.CONFIG_MENU_ENTER_SEARCH_TEXT)
        }

        val row2 = KeyboardRow().apply {
            add(Constants.CONFIG_MENU_ENTER_EXCLUDE_TEXT)
        }

        val row3 = KeyboardRow().apply {
            add(Constants.CONFIG_MENU_SWITCH_MONITORING)
        }

        val rowBack = KeyboardRow().apply {
            add(Constants.MENU_BACK)
        }

        return buildReplyKeyboard(row1, row2, row3, rowBack)
    }

    fun vacancyAlgaMenu(): ReplyKeyboardMarkup {
        val row1 = KeyboardRow().apply {
            add(Constants.CREDS_MENU_ADD_CREDS)
        }

        val row2 = KeyboardRow().apply {
            add(Constants.CREDS_MENU_ADD_CV_ID)
        }

        val row3 = KeyboardRow().apply {
            add(Constants.CREDS_MENU_ADD_TEMPLATE)
        }

        val row4 = KeyboardRow().apply {
            add(Constants.CREDS_MENU_AUTO_REPLY)
        }

        val rowBack = KeyboardRow().apply {
            add(Constants.MENU_BACK)
        }

        return buildReplyKeyboard(row1, row2, row3, row4, rowBack)
    }

    private fun buildReplyKeyboard(vararg rows: KeyboardRow): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.keyboard = listOf(*rows)
        keyboard.resizeKeyboard = true
        keyboard.oneTimeKeyboard = false
        return keyboard
    }

}