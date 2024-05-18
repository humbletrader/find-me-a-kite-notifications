package com.github.humbletrader.fmak.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class MainApp implements CommandLineRunner {

    private static Logger log = LoggerFactory.getLogger(MainApp.class);

    @Autowired
    private MainService mainService;

    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("executing : command line runner");

        mainService.business();
    }
}
