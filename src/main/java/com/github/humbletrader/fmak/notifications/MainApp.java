package com.github.humbletrader.fmak.notifications;

import com.github.humbletrader.fmak.notifications.search.JsonQueryService;
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
            String queryAsJson = notificationDbEntity.queryAsJson();
            //     1. get the first page of data
            List<String> firstPage = queryService.query(queryAsJson);
            //     2. save the first page of data
            //     3. do the diff (if configured)
            //     4. send email (if configured)
            emailService.sendEmail(notificationDbEntity.email(),
                    "New items have occured in your search"
            );

        });


    }
}
