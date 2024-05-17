package com.github.humbletrader.fmak.notifications;

import com.github.humbletrader.fmak.notifications.search.SearchItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Repository
public class NotificationSearchResRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationSearchResRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertResults(List<SearchItem> results,
                              NotificationDbEntity notification){
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
                        ps.setInt(2, notification.runCount() + 1);
                        ps.setString(3, searchItem.link());
                        ps.setString(4, searchItem.brandNameVersion());
                        ps.setDouble(5, searchItem.price());
                        ps.setString(6, searchItem.size());
                        ps.setString(7, searchItem.condition());
                    }
                }
        );
    }

    public List<SearchItem> diffBetween(int runReference, int newRun){
        return Collections.emptyList();
    }
}
