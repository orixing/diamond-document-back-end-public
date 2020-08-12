package com.Diamond_Doc.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
public class DocController {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    private EmailSender emailSender;

    @PostMapping("/recycle")
    public Map<String, Object> recycle(@RequestBody Map params) {
        Integer doc_id= (Integer) params.get("id");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select_sql = "SELECT Doc.id as id FROM Doc,User WHERE Doc.create_user=User.id and User.email = ? and Doc.id=?;";
        String update_sql = "UPDATE Doc SET recycle=1 WHERE id=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select_sql,email,doc_id);
        if(res.size()>0){
            int i = jdbcTemplate.update(update_sql,doc_id);
            System.out.println("update success: " + i + " rows affected");
            response.put("code", 200);
            response.put("msg", "put in recycle bin");
        }
        else{
            response.put("code", 401);
            response.put("msg", "permission denied");
        }
        System.out.println(response);
        return response;
    }

    @PostMapping("/recover")
    public Map<String, Object> recover(@RequestBody Map params) {
        Integer doc_id= (Integer) params.get("id");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select_sql = "SELECT Doc.id as id FROM Doc,User WHERE Doc.create_user=User.id and User.email = ? and Doc.id=?;";
        String update_sql = "UPDATE Doc SET recycle=0 WHERE id=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select_sql,email,doc_id);
        if(res.size()>0){
            int i = jdbcTemplate.update(update_sql,doc_id);
            System.out.println("update success: " + i + " rows affected");
            response.put("code", 200);
            response.put("msg", "recover success");
        }
        else{
            response.put("code", 401);
            response.put("msg", "permission denied");
        }
        System.out.println(response);
        return response;
    }

    @PostMapping("/save_new_doc")
    public Map<String, Object> save_new_doc(@RequestBody Map params) {
        int team_id=(int)params.get("team_id");//0-个人文档，其他-团队id
        String title= (String) params.get("title");
        String content= (String) params.get("content");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select0_sql = "SELECT id FROM User WHERE email = ?;";
        String select1_sql = "SELECT member_user as user FROM Member WHERE team_id=?;";
        String insert1_sql = "INSERT INTO Doc(title,content,create_user,modify_user) values(?,?,?,?);";
        String insert2_sql = "INSERT INTO Doc(title,content,create_user,modify_user,team_id) values(?,?,?,?,?);";
        String insert3_sql = "INSERT INTO Permission(doc_id,user,permission) values(?,?,?);";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select0_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id"),i=0;
            if(team_id==0){
                i+=jdbcTemplate.update(insert1_sql,title,content,id,id);
                response.put("code", 200);
                response.put("msg", "personal doc saved");
            }
            else{
                i+=jdbcTemplate.update(insert2_sql,title,content,id,id,team_id);
                int key=(int)jdbcTemplate.queryForMap("SELECT LAST_INSERT_ID() as key;").get("key");
                List<Map<String, Object>> members=jdbcTemplate.queryForList(select1_sql,team_id);
                for(Map<String, Object> item:members){
                    i+=jdbcTemplate.update(insert3_sql,key,(int)item.get("user"),15);
                }
                response.put("code", 200);
                response.put("msg", "team doc saved");
            }
            System.out.println("update success: " + i + " rows affected");
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return response;
    }
    @PostMapping("/modify_doc")
    public Map<String, Object> modify_doc(@RequestBody Map params) {
        int doc_id=(int)params.get("doc_id");
        int team_id=(int)params.get("team_id");//0-个人文档，其他-团队id
        String title= (String) params.get("title");
        String content= (String) params.get("content");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select_sql = "SELECT id FROM User WHERE email = ?;";
        String update_sql = "UPDATE Doc SET title=?,content=?,modify_user=?,modify_time+=1 where id=?;";
        String insert_sql = "INSERT INTO Modify(doc_id,modify_user) values(?,?);";
        String delete_sql = "DELETE FROM Edit WHERE doc_id=? and edit_user=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id"),i=0;
            i+=jdbcTemplate.update(update_sql,title,content,id,doc_id);
            i+=jdbcTemplate.update(insert_sql,doc_id,id);
            if(team_id==0){
                response.put("code", 200);
                response.put("msg", "personal doc modified");
            }
            else{
                response.put("code", 200);
                response.put("msg", "team doc modified");
            }
            System.out.println("update success: " + i + " rows affected");
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return response;
    }
    @PostMapping("/edit_doc")
    public Map<String, Object> edit_doc(@RequestBody Map params) {
        Integer doc_id= (Integer) params.get("id");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select0_sql = "SELECT id FROM User WHERE email = ?;";
        String select1_sql = "SELECT Doc.id as id FROM Doc,User WHERE Doc.create_user=User.id and User.id = ? and Doc.id=?;";
        String select2_sql = "SELECT Doc.permission as permission FROM Doc WHERE Doc.id=?;";
        String select3_sql = "SELECT permission as id FROM Doc,User WHERE Doc.create_user=User.id and User.email = ? and Doc.id=?;";
        String update2_sql = "UPDATE Doc SET edit=1 WHERE id=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select0_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id");
            boolean is_create = jdbcTemplate.queryForList(select1_sql,id,doc_id).size()>0;
            boolean is_share=(((int)jdbcTemplate.queryForList(select2_sql,doc_id).get(0).get("permission"))&0x02)!=0;
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return response;
    }
}
