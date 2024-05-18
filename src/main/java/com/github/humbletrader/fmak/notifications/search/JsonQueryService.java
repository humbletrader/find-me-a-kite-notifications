package com.github.humbletrader.fmak.notifications.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.humbletrader.fmak.notifications.notifications.NotificationDbEntity;
import com.github.humbletrader.fmak.query.FmakSqlBuilder;
import com.github.humbletrader.fmak.query.ParameterizedStatement;
import com.github.humbletrader.fmak.query.SearchValAndOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class JsonQueryService {

    private static Logger log = LoggerFactory.getLogger(JsonQueryService.class);

    private final FmakSqlBuilder sqlBuilder = new FmakSqlBuilder(20);

    private final ObjectMapper jsonToJava = new ObjectMapper();
    private final TypeReference TYPE_REFERENCE = new TypeReference<Map<String, SearchValAndOp>>(){};


    private final SearchRepository searchRepository;

    public JsonQueryService(SearchRepository searchRepository){
        this.searchRepository = searchRepository;
    }

    public List<SearchItem> queryResultsForNotification(NotificationDbEntity notification){
        try{
            String json = notification.queryAsJson();
            Map<String, SearchValAndOp> jsonAsObject = (Map<String, SearchValAndOp>)jsonToJava.readValue(json, TYPE_REFERENCE);
            ParameterizedStatement peramStmt = sqlBuilder.buildSearchSql(jsonAsObject, 0);
            return searchRepository.search(peramStmt);
        }catch (JsonProcessingException jsonExc){
            throw new RuntimeException(jsonExc);
        }
    }

}
