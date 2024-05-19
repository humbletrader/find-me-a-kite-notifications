package com.github.humbletrader.fmak.notifications.email;

import com.github.humbletrader.fmak.notifications.notifications.NotificationDbEntity;
import com.github.humbletrader.fmak.notifications.search.SearchItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private static Logger log = LoggerFactory.getLogger(EmailService.class);

    public String diffAsHtml(List<SearchItem> diff){
        StringBuilder result = new StringBuilder();
        result.append("<html>\n" +
                "  <head>New items in your search</head>\n" +
                "  <body>\n" +
                "    <h2>New Items in your search</h2>\n");
        var body = diff.stream()
                .map(searchItem -> {
                    return "<div>" +
                            "<a href=\"" + searchItem.link()+"\">"+searchItem.brandNameVersion() +"</a> " +
                            "<span>" + searchItem.price()+"</span>" +
                            "</div>";
                })
                .collect(Collectors.joining("\n"));
        result.append(body);
        result.append("  </body>\n" +
                        "</html>");
        return result.toString();
    }

    public Path createEmail(NotificationDbEntity notification,
                            List<SearchItem> diff) throws IOException {
        var emailContentFilePath = Paths.get("./mail/notification_" + notification.id() + ".mail");

        log.info("creating email file for notification {} and email {}", notification.id(), notification.email());
        StringBuilder emailFileContent = new StringBuilder();
        emailFileContent.append(
                "From: findmeakite@outlook.com\n" +
                "To: " + notification.email()+"\n"+
                "Subject: new items in your search\n" +
                "Mime-Version: 1.0\n" +
                "Content-Type: text/html\n" +
                "\n");
        String diffAsHtml = diffAsHtml(diff);
        emailFileContent.append(diffAsHtml);
        Files.writeString(emailContentFilePath, emailFileContent.toString());

        return emailContentFilePath;
    }

}
