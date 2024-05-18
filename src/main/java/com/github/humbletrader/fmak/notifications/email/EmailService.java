package com.github.humbletrader.fmak.notifications.email;

import com.github.humbletrader.fmak.notifications.notifications.NotificationDbEntity;
import com.github.humbletrader.fmak.notifications.search.SearchItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private static Logger log = LoggerFactory.getLogger(EmailService.class);

    public String emailContentFrom(List<SearchItem> diff){
        return diff.stream()
                .map(searchItem -> searchItem.brandNameVersion()+" "+searchItem.link())
                .collect(Collectors.joining());
    }

    public void sendEmail(NotificationDbEntity notification,
                          List<SearchItem> diff){
        if(!diff.isEmpty()){
            log.info("sending email to {}", notification.email());
            String diffAsHtml = emailContentFrom(diff);
            log.info("email content is {}", diffAsHtml);
        }else{
            log.info("diff list is empty. won't send email for notification {}", notification.id());
        }
    }

}
