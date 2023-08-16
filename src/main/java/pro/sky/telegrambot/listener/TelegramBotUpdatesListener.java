package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private static final Pattern PATTERN = Pattern.compile("(\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{2}) ([А-яA-z\\d,\\s?!:.]+)");

    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private NotificationTaskService notificationTaskService;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            long chatId = update.message().chat().id();
            String text = update.message().text();
            Matcher matcher = PATTERN.matcher(text);
            if ("/start".equals(text)) {
                sendMessage(chatId, "Hello");

            } else if (matcher.matches()) {
                LocalDateTime dateTime = pars(matcher.group(1));
                String message = matcher.group(2);
                if (dateTime != null) {
                    notificationTaskService.save(chatId, message, dateTime);
                    sendMessage(chatId, "Задача записана");
                }
            } else {
                sendMessage(chatId, "введите /start либо задачу в формате дд.мм.гггг вв:вв Задача");
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Nullable
    private LocalDateTime pars(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage(chatId,
                text);
        SendResponse response = telegramBot.execute(message);
        if (!response.isOk()) {
            logger.error("failed: " + response.description());
        }
    }
}
