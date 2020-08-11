package com.Diamond_Doc.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.*;
import java.text.SimpleDateFormat;

@RestController
public class UserController {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    private EmailSender emailSender;

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
        if(res.isEmpty()){
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


    @PostMapping("/home")
    public Map<String, Object> home(@RequestBody Map params) {
        Integer id= (Integer) params.get("id");
        Integer kind= (Integer) params.get("kind");
        Map<String,Object> response = new HashMap<>();
        String select_sql="";
        if(kind==1)
        {
            select_sql = "SELECT Doc.title,Doc.create_user,Browse.browse_time,Doc.modify_user,Doc.modify_time FROM Browse,Doc WHERE Browse.browse_user = ? and Browse.doc_id = Doc.id ORDER BY Browse.browse_time desc;";
        }
        if(kind==2)
        {
            select_sql = "SELECT Doc.title,Doc.create_user,Browse.browse_time,Doc.modify_user,Doc.modify_time FROM Browse,Doc WHERE Doc.create_user = ? and Browse.doc_id = Doc.id ORDER BY Doc.create_time desc;";
        }
        if(kind==3)
        {
            select_sql = "SELECT Doc.title,Doc.create_user,Browse.browse_time,Doc.modify_user,Doc.modify_time FROM Doc,Favorite WHERE Favorite.favorite_user = ? and Favorite.doc_id = Doc.id ORDER BY Favorite.favorite_time desc;";
        }


        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> list = jdbcTemplate.queryForList(select_sql,id);

        for (Map<String, Object> map : list) {
            System.out.println(map);
        }

        //response.putAll(res);

        System.out.println(response);
        return response;
    }
    @PostMapping("/getinfo")
    public Map<String, Object> getinfo(@RequestBody Map params) {
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();
        System.out.println(email);
        String select_sql = "SELECT email,name,avatar,gender,phone,birthday,address FROM User WHERE email = ?;";

        // 通过jdbcTemplate查询数据库
        Map<String, Object> res = jdbcTemplate.queryForMap(select_sql,email);
        response.put("code", 200);
        response.put("msg", "get info success");
        response.putAll(res);
        System.out.println(response);
        return response;
    }

    @PostMapping("/info")
    public Map<String, Object> info(@RequestBody Map params) throws ParseException {
        String email= (String) params.get("email");
        String name= (String) params.get("name");
        String avatar= (String) params.get("avatar");
        Integer gender= (Integer) params.get("gender");
        String phone= (String) params.get("phone");
        String birthday= (String) params.get("birthday");
        System.out.println(birthday);
        String day = birthday.substring(0,10);
        System.out.println(day);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = simpleDateFormat.parse(day);
        System.out.println(date);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE,1); //把日期往后增加一天,整数  往后推,负数往前移动
        date=calendar.getTime(); //这个时间就是日期往后推一天的结果
        System.out.println(date);
        String address= (String) params.get("address");
        Map<String,Object> response = new HashMap<>();

        String select_sql = "SELECT * FROM User WHERE email = ?;";
        String update_sql = "UPDATE User SET name=?,avatar=?,gender=?,phone=?,birthday=?,address=? WHERE email=?;";


        // 通过jdbcTemplate查询数据库
        Map<String, Object> res = jdbcTemplate.queryForMap(select_sql,email);

        if(res.isEmpty()){
            response.put("code",401);
            response.put("msg","user not found");
        }
        else{
            int i = jdbcTemplate.update(update_sql,name,avatar,gender,phone,date,address,email);
            System.out.println(date);
            System.out.println("update success: " + i + " rows affected");
            response.put("code", 200);
            response.put("msg", "change info success");
        }
        System.out.println(response);
        return response;
    }

    @PostMapping("/changepassword")
    public Map<String, Object> changepassword(@RequestBody Map params) {
        String email= (String) params.get("email");
        String new_password= (String) params.get("new_password");
        Map<String,Object> response = new HashMap<>();

        String select_sql = "SELECT * FROM User WHERE email = ?;";
        String update_sql = "UPDATE User SET password=? WHERE email=?;";

        // 通过jdbcTemplate查询数据库
        Map<String, Object> res = jdbcTemplate.queryForMap(select_sql,email);

        if(res.isEmpty()){
            response.put("code",401);
            response.put("msg","wrong password");
        }
        else{
            int i = jdbcTemplate.update(update_sql,new_password, email);
            System.out.println("update success: " + i + " rows affected");
            response.put("code", 200);
            response.put("msg", "change password success");
        }
        System.out.println(response);
        return response;
    }

    @PostMapping("/forgetpassword")
    public Map<String, Object> forgetpassword(@RequestBody Map params) {
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String update_sql = "UPDATE User SET password=? WHERE email=?;";

        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<14;i++){
            int number=random.nextInt(62);
            sb.append(str.charAt(number));
        }

        int i = jdbcTemplate.update(update_sql,sb.toString(), email);
        System.out.println("update success: " + i + " rows affected");
        emailSender.sendSimpleMail(email,"Diamond Doc Reset Password","New Password："+sb.toString());
        response.put("code", 200);
        response.put("msg", "reset password success");
        System.out.println(response);
        return response;
    }


    @PostMapping("/verification")
    public Map<String, Object> verification(@RequestBody Map params) {
        String type= (String) params.get("type");//1-sign up ,2-forget password
        String email= (String) params.get("email");
        Integer code= (Integer) params.get("code");
        Map<String,Object> response = new HashMap<>();

        String select_sql = "SELECT * FROM User WHERE email = ?;";

        if(type.equals("1")){
            emailSender.sendSimpleMail(email,"Diamond Doc Verification","Verification code："+code);
            response.put("code", 200);
            response.put("msg", "send email success");
        }
        else{
            Map<String, Object> res = jdbcTemplate.queryForMap(select_sql,email);

            if(res.isEmpty()){
                response.put("code",401);
                response.put("msg","user not found");
            }
            else{
                emailSender.sendSimpleMail(email,"Diamond Doc Verification","Verification code："+code);
                response.put("code", 200);
                response.put("msg", "send email success");
            }
        }

        return response;
    }
}
