package com.example.fastleadstelegrambot.bot;

import com.example.fastleadstelegrambot.config.TelegramBotConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final TelegramBotConfiguration config;

    private long waitingForInput;

    public TelegramBot(TelegramBotConfiguration config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    // Обработчик входящих сообщений
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Получение текста сообщения пользователя
            String messageText = update.getMessage().getText();
            // Получение chatId пользователя, чтобы знать, кому отправлять ответ
            long chatId = update.getMessage().getChatId();

            //Проверка на то, есть ли ожидание данных пользователя
            if (chatId != waitingForInput) {
                // Создание сообщения-ответа//
                switch (messageText){
                    case "/start":
                        sendMessage(chatId, "бот заработал");
                        break;
                        //Панель с кнопками
                    case "Расчет цены оффера для брокера":
                        try {
                        functionalPanel(chatId);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                //Проверка айди пользователя на ожидание получения цены оффера
            } else if(chatId == waitingForInput) {
                //Проверка для метода .parseInt
                try{
                    //Логика если введено верное значение
                    int value = Integer.parseInt(messageText);
                    sendMessage(chatId, "Вы ввели число: " + value);
                    waitingForInput = 0;
                } catch(Exception e){
                    //Лошика если введено неверное значение
                    String wrongAnswer = "Ошибка! Возможно вы ввели неверный формат ответа. Попробуйте ещё раз!";
                    sendMessage(chatId, wrongAnswer);
                }
            }
        }
    }
    //Код отвечающий за логику отправки
    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        executionMessage(message);
    }

    private void functionalPanel(long chatId) throws TelegramApiException {
        // Создаем объект класса ReplyKeyboardMarkup
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
// Устанавливаем, что кнопки будут отображаться в вертикальном порядке
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

// Создаем объект класса KeyboardRow, который будет содержать кнопки
        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton button = new KeyboardButton();
        button.setText("Расчет цены оффера для брокера");
        keyboardRow.add(button);

// Добавляем объект KeyboardRow в ReplyKeyboardMarkup
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboardRows);

// Отправляем сообщение с панелью кнопок
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Чтобы узнать цену нашего оффера, пожалуйста," +
                " введите цену в формате числа" +
                " и нажмите кнопку отправки. Спасибо!");
        message.setReplyMarkup(replyKeyboardMarkup);
        waitingForInput = chatId;
        execute(message);
    }

    //Проверка на ошибку
    private void executionMessage(SendMessage message){
        try{
            execute(message);
        }
        catch (TelegramApiException exception){
            System.out.println("!!!!!!Telegram exception in telegram bot!!!!!!");
            exception.printStackTrace();
        }
    }
}
