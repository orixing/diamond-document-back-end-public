package com.Diamond_Doc.demo.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@CrossOrigin
@RestController
public class MessageController {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    private EmailSender emailSender;

    @PostMapping("/get_message")
    public Map<String, Object> get_message(@RequestBody Map params) {
        String token=(String) params.get("token");
        DecodedJWT decoded = Encrypt.decoded(token);
        int receiver_id=decoded.getClaim("id").asInt();
        Map<String,Object> response = new LinkedHashMap<>();

        String select1_sql="SELECT Message.id,type,status,Message.sender as sender_id,User.name as sender_name,UNIX_TIMESTAMP(send_time) as send_time,team_id,team_name,doc_id,doc_name from User,Message " +
                "WHERE User.id=Message.sender and Message.receiver=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> list = jdbcTemplate.queryForList(select1_sql,receiver_id);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int i=1;
        for(Map<String, Object> map:list){
            String send_time =format.format(new Date((long)map.get("send_time")*1000L));
            map.replace("send_time",send_time);
            if((int)map.get("team_id")==0){
                map.remove("team_id");
                map.remove("team_name");
            }
            if((int)map.get("doc_id")==0){
                map.remove("doc_id");
                map.remove("doc_name");
            }
            response.put("message"+i++,map);
        }
        System.out.println(response);
        return response;
    }
}
