package com.Diamond_Doc.demo.controller;

import com.Diamond_Doc.demo.controller.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.text.ParseException;

@CrossOrigin
@RestController
public class UserController {


    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    private EmailSender emailSender;

    @RequestMapping("/")
    public String index(){
        return "hello world";
    }
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


        String select_sql = "SELECT name,email,avatar,gender,phone,UNIX_TIMESTAMP(birthday) as birthday,address FROM User WHERE email = ? and password = ?;";

        // 通过jdbcTemplate查询数据库
        Map<String, Object> res = jdbcTemplate.queryForMap(select_sql,email,password);
        System.out.println(res);
        if(res.isEmpty()){
            response.put("code",401);
            response.put("msg","login fail");
        }
        else{
            response.put("code",200);
            response.put("msg","login success");
            if(res.get("birthday")!=null){
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String birthday_str=format.format(new Date((long)res.get("birthday")*1000L));
                res.put("birthday",birthday_str);
            }
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
            select_sql = "SELECT Doc.title,Doc.create_user,UNIX_TIMESTAMP(Browse.browse_time) as browse_time,Doc.modify_user,UNIX_TIMESTAMP(Doc.modify_time) as modify_time FROM Browse,Doc WHERE Browse.browse_user = ? and Browse.doc_id = Doc.id ORDER BY Browse.browse_time desc;";
        }
        if(kind==2)
        {
            select_sql = "SELECT Doc.title,Doc.create_user,UNIX_TIMESTAMP(Browse.browse_time) as browse_time,Doc.modify_user,UNIX_TIMESTAMP(Doc.modify_time) as modify_time FROM Browse,Doc WHERE Doc.create_user = ? and Browse.doc_id = Doc.id ORDER BY Doc.create_time desc;";
        }
        if(kind==3)
        {
            select_sql = "SELECT Doc.title,Doc.create_user,UNIX_TIMESTAMP(Browse.browse_time) as browse_time,Doc.modify_user,UNIX_TIMESTAMP(Doc.modify_time) as modify_time FROM Doc,Favorite WHERE Favorite.favorite_user = ? and Favorite.doc_id = Doc.id ORDER BY Favorite.favorite_time desc;";
        }

        String select_name_sql="SELECT name FROM User WHERE id=?;";
        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> list = jdbcTemplate.queryForList(select_sql,id);
        Map<String, Object> tmp=new HashMap<String, Object>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int i=0;
        for (Map<String, Object> map : list) {
            tmp.clear();
            int user_id= (int) map.get("create_user");
            String create_user=jdbcTemplate.queryForMap(select_name_sql,user_id).get("name").toString();
            user_id= (int) map.get("modify_user");
            String modify_user=jdbcTemplate.queryForMap(select_name_sql,user_id).get("name").toString();
            System.out.println(map.get("browse_time"));
            String browse_time =format.format(new Date((long)map.get("browse_time")*1000L));
            String modify_time =format.format(new Date((long)map.get("modify_time")*1000L));
            tmp.put("title",map.get("title"));
            tmp.put("create_user",create_user);
            tmp.put("browse_time",browse_time);
            tmp.put("modify_user",modify_user);
            tmp.put("modify_time",modify_time);
            response.put("user"+i++,tmp);
        }
        System.out.println(response);
        return response;
    }
    @PostMapping("/getinfo")
    public Map<String, Object> getinfo(@RequestBody Map params) {
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select_sql = "SELECT name,email,avatar,gender,phone,UNIX_TIMESTAMP(birthday) as birthday,address FROM User WHERE email = ?;";

        // 通过jdbcTemplate查询数据库
        Map<String, Object> res = jdbcTemplate.queryForMap(select_sql,email);

        response.put("code", 200);
        response.put("msg", "get info success");
        if(res.get("birthday")!=null){
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String birthday_str=format.format(new Date((long)res.get("birthday")*1000L));
            res.put("birthday",birthday_str);
        }
        System.out.println(response);
        return response;
    }

    @PostMapping("/info")
    public Map<String, Object> info(@RequestBody Map params) throws ParseException{
        String email= (String) params.get("email");
        String name= (String) params.get("name");
        String avatar= (String) params.get("avatar");
        Integer gender= (Integer) params.get("gender");
        String phone= (String) params.get("phone");
        String birthday= (String) params.get("birthday");
        String day = birthday.substring(0,10);
        String address= (String) params.get("address");
        Map<String,Object> response = new HashMap<>();

        String select_sql = "SELECT * FROM User WHERE email = ?;";
        String update_sql = "UPDATE User SET name=?,avatar=?,gender=?,phone=?,birthday=DATE_ADD(str_to_date(?, '%Y-%m-%d'),INTERVAL 1 DAY),address=? WHERE email=?;";


        // 通过jdbcTemplate查询数据库
        Map<String, Object> res = jdbcTemplate.queryForMap(select_sql,email);

        if(res.isEmpty()){
            response.put("code",401);
            response.put("msg","user not found");
        }
        else{
            int i = jdbcTemplate.update(update_sql,name,avatar,gender,phone,day,address,email);
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

        String update_sql = "UPDATE User SET password=? WHERE email=?;";

        // 通过jdbcTemplate查询数据库
        int i = jdbcTemplate.update(update_sql,new_password, email);
        System.out.println("update success: " + i + " rows affected");
        response.put("code", 200);
        response.put("msg", "change password success");
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
        String code= (String) params.get("code");
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

    @Value("${prop.upload-folder}")
    private String UPLOAD_FOLDER;

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam(name = "file", required = false) MultipartFile file, HttpServletRequest request) {
        Map<String,Object> response = new HashMap<>();

        String savePath = UPLOAD_FOLDER;
        String orginalFilename = file.getOriginalFilename();
        String suffix = "wrong_file";
        int beginIndex = orginalFilename.lastIndexOf(".");
        if (beginIndex != -1) {
            suffix = orginalFilename.substring(beginIndex);
        }
        String filename = UUID.randomUUID() + suffix;
        File dest = new File(savePath, filename);
        try {
            file.transferTo(dest);
            response.put("code", 200);
            response.put("msg", "upload success");
            response.put("url", "http://127.0.0.1:8080/img/"+filename);//175.24.53.216
        } catch (IOException e) {
            e.printStackTrace();
            response.put("code", 200);
            response.put("msg", "upload fail");
        }
        return response;
    }
}
