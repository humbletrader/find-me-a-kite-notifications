package com.github.humbletrader.fmak.notifications;

import com.github.humbletrader.fmak.notifications.email.EmailService;
import com.github.humbletrader.fmak.notifications.notifications.NotificationDbEntity;
import com.github.humbletrader.fmak.notifications.notifications.NotificationRepository;
import com.github.humbletrader.fmak.notifications.search.JsonQueryService;
import com.github.humbletrader.fmak.notifications.search.NotificationSearchResRepository;
import com.github.humbletrader.fmak.notifications.search.SearchItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MainService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JsonQueryService queryService;

    @Autowired
    private NotificationSearchResRepository notificationResultsRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public void business(){
        //get the list of notifications
        List<NotificationDbEntity> notifications = notificationRepository.readNotifications();

        //for each notification:
        notifications.forEach(notification -> {
            // 1. get the first page of data
            List<SearchItem> firstPage = queryService.queryResultsForNotification(notification);
            // 2. save the first page of data
            notificationResultsRepository.insertResults(firstPage, notification);

            if(notification.runId() != 0){
                // 3. do the diff (if configured) except for th first run
                List<SearchItem> diff = notificationResultsRepository.diffBetween(
                        notification,
                        notification.runId(),
                        notification.runId() + 1
                );

                // 4. send email (if configured)
                emailService.sendEmail(notification, diff);
            } //end if

            // 5. update the run_count for current notification
            notificationRepository.updateRunId(
                    notification,
                    notification.runId()+1
            );

//            notificationResultsRepository.deleteSearchResultsFor(
//                    notification,
//                    notification.runId()
//            );
        });
    }

}
