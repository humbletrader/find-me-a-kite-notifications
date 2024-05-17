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

    public List<SearchItem> search(ParameterizedStatement paramStmt){
        log.info("running query {}", paramStmt.getSqlWithoutParameters());
        return jdbcTemplate.query(
                paramStmt.getSqlWithoutParameters(),
                (rs, rowCount) -> new SearchItem(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getDouble(3),
                        rs.getString(4),
                        rs.getString(5)),
                paramStmt.getParamValues().toArray()
        );
    }

}
