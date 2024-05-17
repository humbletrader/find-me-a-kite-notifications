package com.github.humbletrader.fmak.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
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
        return jdbcTemplate.query("select * from notifications", new RowMapper<NotificationDbEntity>() {
            @Override
            public NotificationDbEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new NotificationDbEntity(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("query_as_json"));
            }
        });
    }

}
