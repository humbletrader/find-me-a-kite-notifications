package com.github.humbletrader.fmak.notifications;

import com.github.humbletrader.fmak.notifications.email.EmailService;
import com.github.humbletrader.fmak.notifications.search.JsonQueryService;
import com.github.humbletrader.fmak.notifications.search.SearchItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

import java.util.List;


@SpringBootApplication
public class MainApp implements CommandLineRunner {

    private static Logger LOG = LoggerFactory.getLogger(MainApp.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JsonQueryService queryService;

    @Autowired
    private NotificationSearchResRepository notificationResultsRepository;

    @Autowired
    private EmailService emailService;

    public static void main(String[] args) {
        LOG.info("STARTING THE APPLICATION");
        SpringApplication.run(MainApp.class, args);
        LOG.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) {
        LOG.info("executing : command line runner");

        //get the list of notifications
        List<NotificationDbEntity> notifications = notificationRepository.readNotifications();

        //for each notification:
        notifications.forEach(notificationDbEntity -> {
            // 1. get the first page of data
            List<SearchItem> firstPage = queryService.queryResultsForNotification(notificationDbEntity);
            // 2. save the first page of data
            notificationResultsRepository.insertResults(firstPage, notificationDbEntity);
            // 3. do the diff (if configured) except for th first run
            if(notificationDbEntity.runCount() != 0){
                notificationResultsRepository.diffBetween(
                        notificationDbEntity.runCount(),
                        notificationDbEntity.runCount() + 1
                );
            }
            // 4. send email (if configured)
            emailService.sendEmail(notificationDbEntity.email(),
                    "New items have occured in your search"
            );

            // 5. update the run_count for current notification
            notificationRepository.updateRunCount(
                    notificationDbEntity,
                    notificationDbEntity.runCount()+1
            );
        });


    }
}
