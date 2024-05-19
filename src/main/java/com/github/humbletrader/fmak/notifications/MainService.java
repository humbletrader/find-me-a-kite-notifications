package com.github.humbletrader.fmak.notifications;

import com.github.humbletrader.fmak.notifications.email.EmailService;
import com.github.humbletrader.fmak.notifications.notifications.NotificationDbEntity;
import com.github.humbletrader.fmak.notifications.notifications.NotificationEmailFile;
import com.github.humbletrader.fmak.notifications.notifications.NotificationRepository;
import com.github.humbletrader.fmak.notifications.search.JsonQueryService;
import com.github.humbletrader.fmak.notifications.search.NotificationSearchResRepository;
import com.github.humbletrader.fmak.notifications.search.SearchItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.humbletrader.fmak.notifications.RunMode.AFTER;

@Service
public class MainService {

    private static Logger log = LoggerFactory.getLogger(MainService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JsonQueryService queryService;

    @Autowired
    private NotificationSearchResRepository notificationResultsRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public void business(RunMode runMode){
        //get the list of notifications
        List<NotificationDbEntity> notifications = notificationRepository.readNotifications();

        //for each notification:
        var sendEmailFileContent = notifications.stream().flatMap(notification -> {
            Optional<NotificationEmailFile> result = Optional.empty();
            // 1. get the first page of data
            List<SearchItem> firstPage = queryService.queryResultsForNotification(notification);
            // 2. save the first page of data
            notificationResultsRepository.insertResults(firstPage, notification);

            if(AFTER.equals(runMode)){
                // 3. do the diff (if configured) except for th first run
                List<SearchItem> diff = notificationResultsRepository.diffBetween(
                        notification,
                        notification.runId(),
                        notification.runId() + 1
                );

                // 4. send email (if configured)
                if(!diff.isEmpty()){
                    try {
                        Path emailFilePath = emailService.createEmail(notification, diff);
                        result = Optional.of(new NotificationEmailFile(emailFilePath, notification.email()));
                    }catch (IOException ioException){
                        log.error("error writing email file for notification "+notification.id(), ioException);
                    }
                }else{
                    log.info("email not created for notification {} ane email {} because diff is empty", notification.id(), notification.email());
                }

            } //end if mode = after

            // 5. update the run_count for current notification
            notificationRepository.updateRunId(
                    notification,
                    notification.runId()+1
            );

            notificationResultsRepository.deleteSearchResultsFor(
                    notification,
                    notification.runId()
            );

            return result.stream();
        }).map(notificationEmailFile -> {
            return "cat " + notificationEmailFile.emailFilePath().toString() + "| msmtp " + notificationEmailFile.emailRecipient();
        }).collect(Collectors.joining("\n", "#! /bin/bash\n", ""));

        if(AFTER.equals(runMode)){
            try {
                Path sendEmailFile = Paths.get("./mail/sendEmails.sh");
                log.info("writing the script for sending emails {}", sendEmailFile.toString());
                Files.writeString(sendEmailFile, sendEmailFileContent);
            }catch (IOException ioException){
                log.error("error writing the sendEmails.sh", ioException);
            }
        }else{
            log.info("no sendEmail.sh file written because we run in {} mode", runMode);
        }


    }

}
