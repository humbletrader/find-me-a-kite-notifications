package com.github.humbletrader.fmak.notifications.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NotificationRepository {

    private static final Logger logger = LoggerFactory.getLogger(NotificationRepository.class);

    private JdbcTemplate jdbcTemplate;

    public NotificationRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<NotificationDbEntity> readNotifications(){
        logger.info("reading notifications ... ");
        return jdbcTemplate.query(
                "select * from notifications",
                (rs,  rowNum) -> new NotificationDbEntity(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("query_as_json"),
                        rs.getInt("run_count")
                )
        );
    }

    public void updateRunId(NotificationDbEntity oldNotification, int newRunId){
        logger.info("upgrading the run id for notification {} to {}", oldNotification.id(), newRunId);
        jdbcTemplate.update(
                "update notifications set run_count = ? where id = ?",
                newRunId, oldNotification.id()
        );
    }

}
