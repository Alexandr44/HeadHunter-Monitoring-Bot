package com.alexandr44.headhuntermonitorbot.telegram

import com.alexandr44.headhuntermonitorbot.dto.Constants
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@Service
class HeadHunterBotMenuBuilder {

    fun mainMenu(): ReplyKeyboardMarkup {

        val row1 = KeyboardRow()
        row1.add(Constants.MENU_ENTER_SEARCH_TEXT)

        val row2 = KeyboardRow()
        row2.add(Constants.MENU_ENTER_EXCLUDE_TEXT)

        val row3 = KeyboardRow()
        row2.add(Constants.MENU_SWITCH_MONITORING)

        val row4 = KeyboardRow()
        row4.add(Constants.MENU_SUPPORT)
        row4.add(Constants.MENU_HELP)

        return buildReplyKeyboard(row1, row2, row3, row4)
    }

    private fun buildReplyKeyboard(vararg rows: KeyboardRow): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.keyboard = listOf(*rows)
        keyboard.resizeKeyboard = true
        keyboard.oneTimeKeyboard = false
        return keyboard
    }

}