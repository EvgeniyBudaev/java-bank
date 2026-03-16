package com.cashservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Микросервис операций с наличными (пополнение и снятие денег).
 */
@SpringBootApplication
public class CashServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CashServiceApplication.class, args);
    }

}
