package com.Diamond_Doc.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
public class UserController {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @PostMapping("/signup")
    public Map<String, Object> signup(@RequestBody Map params) {
        String name= (String) params.get("uname");
        String password= (String) params.get("passwd1");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select_sql = "SELECT * FROM User WHERE email = ?;";
        String insert_sql = "INSERT INTO User(name,password,email) values(?,?,?);";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select_sql,email);
        if(!res.isEmpty()){
            System.out.println("email exists");
            response.put("code",401);
            response.put("msg","email exists");
        }

        else {
            int i = jdbcTemplate.update(insert_sql, name, password, email);
            System.out.println("insert success: " + i + " rows affected");
            response.put("code", 200);
            response.put("msg", "insert success");
        }
        System.out.println(response);
        return response;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map params) {
        String email= (String) params.get("email");
        String password= (String) params.get("password");
        Map<String,Object> response = new HashMap<>();


        String select_sql = "SELECT * FROM User WHERE email = ? and password = ?;";

        // 通过jdbcTemplate查询数据库
        Map<String, Object> res = jdbcTemplate.queryForMap(select_sql,email,password);
        if(!res.isEmpty()){
            response.put("code",401);
            response.put("msg","login fail");
        }
        else{
            response.put("code",200);
            response.put("msg","login success");
            response.putAll(res);
        }
        System.out.println(response);
        return response;
    }
}
