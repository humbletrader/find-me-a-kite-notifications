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
        //p.brand_name_version, p.link, a.price, a.size, p.condition, p.visible_to_public
        return jdbcTemplate.query(
                paramStmt.getSqlWithoutParameters(),
                (rs, rowCount) -> new SearchItem(
                        rs.getString("link"),
                        rs.getString("brand_name_version"),
                        rs.getDouble("price"),
                        rs.getString("size"),
                        rs.getString("condition")),
                paramStmt.getParamValues().toArray()
        );
    }

}
