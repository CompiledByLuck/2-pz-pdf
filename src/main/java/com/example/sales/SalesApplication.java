package com.example.sales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableScheduling
public class SalesApplication {

    private static final Logger log = LoggerFactory.getLogger(SalesApplication.class);

     public static void main(String[] args) {
        log.info("Starting SalesPdfReportApplication");
        SpringApplication.run(SalesApplication.class, args);
     }
 }
