package com.example.dockerapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, Docker World!";
    }

    @GetMapping("/hoge")
    public String sayHoge() {
        return "hogehogehoge";
    }

    @GetMapping("/check-db")
    public String checkDbConnection() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class); // MySQLへの接続確認
            return "Database connection is successful!";
        } catch (Exception e) {
            return "Database connection failed!";
        }
    }
}
