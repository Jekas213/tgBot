package pro.sky.telegrambot.timer;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Component
public class NotificationTaskTimer {
    @Autowired
    private NotificationTaskRepository notificationTaskRepository;
    @Autowired
    private TelegramBot telegramBot;

    private Logger logger = LoggerFactory.getLogger(NotificationTaskTimer.class);

    @Scheduled(timeUnit = TimeUnit.MINUTES, fixedDelay = 1)
    public void task() {
        notificationTaskRepository.findAllByDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))
                .forEach(notificationTask -> {
                    SendMessage message = new SendMessage(notificationTask.getChatId(),
                            notificationTask.getText());
                    SendResponse response = telegramBot.execute(message);
                    if (!response.isOk()) {
                        logger.error("failed: " + response.description());
                    }
                });
    }

}
