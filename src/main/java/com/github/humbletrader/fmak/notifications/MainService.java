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

    @Value("${fmak.notifications.cleanup:true}")
    private boolean deletePrevResults;

    @Transactional
    public void business(){
        //get the list of notifications
        List<NotificationDbEntity> notifications = notificationRepository.readNotifications();

        //for each notification:
        var sendEmailFileContent = notifications.stream().flatMap(currNotification -> {
            Optional<NotificationEmailFile> result = Optional.empty();
            // 1. get the first page of data
            List<SearchItem> firstPage = queryService.queryResultsForNotification(currNotification);
            // 2. save the first page of data
            notificationResultsRepository.insertResults(firstPage, currNotification);

            // 3. do the diff (if configured) except for th first run
            List<SearchItem> diff = notificationResultsRepository.diffBetween(
                    currNotification,
                    currNotification.runId(),
                    currNotification.runId() + 1
            );

            // 4. send email (if configured)
            if(!diff.isEmpty()){
                try {
                    Path emailFilePath = emailService.createEmail(currNotification, diff);
                    result = Optional.of(new NotificationEmailFile(emailFilePath, currNotification.email()));
                }catch (IOException ioException){
                    log.error("error writing email file for notification "+currNotification.id(), ioException);
                }
            }else{
                log.info("email not created for notification {} ane email {} because diff is empty", currNotification.id(), currNotification.email());
            }

            // 5. update the run_count for current notification
            notificationRepository.updateRunId(
                    currNotification,
                    currNotification.runId() + 1
            );

            //6. delete the search results from 3 runs ago
            if(deletePrevResults) {
                notificationResultsRepository.deleteSearchResultsFor(
                        currNotification,
                        currNotification.runId() - 3
                );
            }

            return result.stream();
        }).map(notificationEmailFile -> {
            return "cat " + notificationEmailFile.emailFilePath().toString() + "| msmtp " + notificationEmailFile.emailRecipient();
        }).collect(Collectors.joining("\n", "#! /bin/bash\n", ""));

        //7. create the send emails script
        try {
            Path sendEmailFile = Paths.get("./mail/sendEmails.sh");
            log.info("writing the script for sending emails {}", sendEmailFile.toString());
            Files.writeString(sendEmailFile, sendEmailFileContent);
        }catch (IOException ioException){
            log.error("error writing the sendEmails.sh", ioException);
        }
    }

}
