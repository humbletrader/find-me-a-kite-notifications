package com.github.humbletrader.fmak.notifications.search;

import com.github.humbletrader.fmak.notifications.notifications.NotificationDbEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Repository
public class NotificationSearchResRepository {

    private static Logger log = LoggerFactory.getLogger(NotificationSearchResRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public NotificationSearchResRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertResults(List<SearchItem> results,
                              NotificationDbEntity notification){
        int newRunId = notification.runId() + 1;
        log.info("inserting {} results for notification {} and runid {}", results.size(), notification.id(), newRunId);
        jdbcTemplate.batchUpdate(
                "insert into notification_search_results " +
                        "(notification_id, notification_run_count, link, brand_name_version, price, size, condition) " +
                        "values (?, ?, ?, ?, ?, ?, ?)",
                results,
                results.size(),
                new ParameterizedPreparedStatementSetter<SearchItem>() {
                    @Override
                    public void setValues(PreparedStatement ps, SearchItem searchItem) throws SQLException {
                        ps.setInt(1, notification.id());
                        ps.setInt(2, newRunId);
                        ps.setString(3, searchItem.link());
                        ps.setString(4, searchItem.brandNameVersion());
                        ps.setDouble(5, searchItem.price());
                        ps.setString(6, searchItem.size());
                        ps.setString(7, searchItem.condition());
                    }
                }
        );
    }

    public void deleteSearchResultsFor(NotificationDbEntity notification, int maxRunId){
        log.info("deleting notification search results for notification {} and runIds previous to {}", notification.id(), maxRunId);
        jdbcTemplate.update(
                "delete from notification_search_results " +
                        "where " +
                        "notification_id = ? and notification_run_count < ?",
                notification.id(), maxRunId
        );
    }

    public List<SearchItem> diffBetween(NotificationDbEntity notification,
                                        int prevRunId,
                                        int currentRunId){
        log.info("checking diff search results for notification {} and runIds prev={} last={}", notification.id(), prevRunId, currentRunId);
        return jdbcTemplate.query(
                "(select brand_name_version, link, floor(price), size, condition from notification_search_results " +
                        "where notification_id = ? and notification_run_count = ?)" +
                        " except" +
                        " (select brand_name_version, link, floor(price), size, condition from notification_search_results " +
                        " where notification_id = ? and notification_run_count = ?)",
                (rs, rowCount) -> new SearchItem(
                        rs.getString("link"),
                        rs.getString("brand_name_version"),
                        rs.getDouble("price"),
                        rs.getString("size"),
                        rs.getString("condition")
                ),
                notification.id(), currentRunId, notification.id(), prevRunId
        );
    }
}
