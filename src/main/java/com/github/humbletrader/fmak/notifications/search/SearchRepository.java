package com.github.humbletrader.fmak.notifications.search;

import com.github.humbletrader.fmak.query.ParameterizedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SearchRepository {

    private static Logger log = LoggerFactory.getLogger(SearchRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public SearchRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> search(ParameterizedStatement paramStmt){
        log.info("running query {}", paramStmt.getSqlWithoutParameters());
        return jdbcTemplate.query(
                paramStmt.getSqlWithoutParameters(),
                (rs, rowCount) -> rs.getString(1),
                paramStmt.getParamValues().toArray()
        );
    }

}
