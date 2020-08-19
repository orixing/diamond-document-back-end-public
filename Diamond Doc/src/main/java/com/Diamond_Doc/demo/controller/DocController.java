package com.Diamond_Doc.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

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
        System.out.println(res);
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
    @PostMapping("/delete")
    public Map<String, Object> delete(@RequestBody Map params) {
        Integer doc_id= (Integer) params.get("id");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select_sql = "SELECT Doc.recycle as recycle FROM Doc,User WHERE Doc.create_user=User.id and User.email = ? and Doc.id=?;";
        String update_sql = "DELETE FROM Doc WHERE id=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select_sql,email,doc_id);
        if(res.size()>0){
            if((int)res.get(0).get("recycle")==1){
                int i = jdbcTemplate.update(update_sql,doc_id);
                System.out.println("update success: " + i + " rows affected");
                response.put("code", 200);
                response.put("msg", "put in recycle bin");
            }
            else{
                response.put("code", 200);
                response.put("msg", "not in recycle bin");
            }
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
        System.out.println(params);
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
                int key=Integer.parseInt(jdbcTemplate.queryForMap("SELECT LAST_INSERT_ID() as 'key';").get("key").toString());
                response.put("doc_id",key);
                response.put("code", 200);
                response.put("msg", "personal doc saved");
            }
            else{
                i+=jdbcTemplate.update(insert2_sql,title,content,id,id,team_id);
                int key=Integer.parseInt(jdbcTemplate.queryForMap("SELECT LAST_INSERT_ID() as 'key';").get("key").toString());
                List<Map<String, Object>> members=jdbcTemplate.queryForList(select1_sql,team_id);
                for(Map<String, Object> item:members){
                    i+=jdbcTemplate.update(insert3_sql,key,(int)item.get("user"),7);
                }
                response.put("doc_id",key);
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

        String select1_sql = "SELECT id FROM User WHERE email = ?;";
        String select2_sql = "SELECT name,create_user FROM Team WHERE id = ?;";
        String select3_sql="SELECT create_user FROM Doc WHERE id=?;";
        String update_sql = "UPDATE Doc SET title=?,content=?,modify_user=?,modify_times=modify_times+1 where id=?;";
        String insert_sql = "INSERT INTO Modify(doc_id,modify_user) values(?,?);";
        String insert2_sql="INSERT INTO Message(type,receiver,sender,doc_id,doc_name) values(?,?,?,?,?)";
        String insert3_sql="INSERT INTO Message(type,receiver,sender,doc_id,doc_name,team_id,team_name) values(?,?,?,?,?,?,?)";
        String delete_sql = "DELETE FROM Edit WHERE doc_id=? and edit_user=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select1_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id"),i=0;
            i+=jdbcTemplate.update(update_sql,title,content,id,doc_id);
            i+=jdbcTemplate.update(insert_sql,doc_id,id);
            i+=jdbcTemplate.update(delete_sql,doc_id,id);
            if(team_id==0){
                List<Map<String, Object>> tmp = jdbcTemplate.queryForList(select3_sql,doc_id);
                if(tmp.size()>0){
                    i+=jdbcTemplate.update(insert2_sql,4,tmp.get(0).get("create_user").toString(),id,doc_id,title);
                    response.put("code", 200);
                    response.put("msg", "personal doc modified");
                }
                else{
                    response.put("code", 402);
                    response.put("msg", "doc not found");
                }
            }
            else{
                List<Map<String, Object>> tmp = jdbcTemplate.queryForList(select2_sql,team_id);
                if(tmp.size()>0){
                    i+=jdbcTemplate.update(insert3_sql,4,tmp.get(0).get("create_user").toString(),id,doc_id,title,team_id,tmp.get(0).get("name").toString());
                    response.put("code", 200);
                    response.put("msg", "team doc modified");
                }
                else{
                    response.put("code", 403);
                    response.put("msg", "team not found");
                }
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
    @PostMapping("/get_modify_history")
    public Map<String, Object> get_modify_history(@RequestBody Map params) {
        Integer doc_id= (Integer) params.get("id");
        Map<String,Object> response = new HashMap<>();

        String select0_sql = "SELECT User.id as modify_user_id,User.name as modify_user,User.email as modify_user_email,UNIX_TIMESTAMP(modify_time) as modify_time FROM Modify,User WHERE Modify.modify_user=User.id and doc_id = ? ORDER BY modify_time DESC;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select0_sql,doc_id);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(Map<String, Object> map:res){
            map.replace("modify_time",format.format(new Date((long)map.get("modify_time")*1000L)));
        }
        response.put("code", 200);
        response.put("msg", "get_modify_history");
        response.put("list",res);
        System.out.println(response);
        return response;
    }

    @PostMapping("/check_edit")
    public Map<String, Object> check_edit(@RequestBody Map params) {
        Integer doc_id= (Integer) params.get("id");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select0_sql = "SELECT id FROM User WHERE email = ?;";
        String select1_sql = "SELECT User.id as id,User.name as name FROM User,Edit WHERE User.id=Edit.edit_user and doc_id=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select0_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id"),i=0;
            List<Map<String, Object>> edit = jdbcTemplate.queryForList(select1_sql,doc_id);
            if(edit.size()>0){
                if((int)edit.get(0).get("id")==id){
                    response.put("flag", 2);
                    response.put("code", 200);
                    response.put("msg", "is editing");
                }
                else{
                    response.put("flag", 1);
                    response.put("edit_user_id",(int)edit.get(0).get("id"));
                    response.put("edit_user",edit.get(0).get("name").toString());
                    response.put("code", 200);
                    response.put("msg", "is editing");
                }
            }
            else {
                response.put("flag", 0);
                response.put("code", 200);
                response.put("msg", "not editing");
            }
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
        String select1_sql = "SELECT id FROM Doc WHERE create_user= ? and id=?;";
        String select2_sql = "SELECT permission FROM Doc WHERE id=?;";
        String select3_sql = "SELECT permission FROM Permission WHERE doc_id=? and user=?;";
        String select4_sql = "SELECT edit_user FROM Edit WHERE doc_id=?;";
        String insert_sql = "INSERT INTO Edit(doc_id,edit_user) values(?,?);";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select0_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id");
            boolean is_create = jdbcTemplate.queryForList(select1_sql,id,doc_id).size()>0;
            boolean is_share=(jdbcTemplate.queryForList(select2_sql,doc_id).size()>0)&&(((int)jdbcTemplate.queryForList(select2_sql,doc_id).get(0).get("permission"))&0x02)!=0;
            boolean is_team=(jdbcTemplate.queryForList(select3_sql,doc_id,id).size()>0)&&(((int)jdbcTemplate.queryForList(select3_sql,doc_id,id).get(0).get("permission"))&0x02)!=0;
            if(is_create||is_share||is_team){
                if(jdbcTemplate.queryForList(select4_sql,doc_id).size()>0&&(int)jdbcTemplate.queryForList(select4_sql,doc_id).get(0).get("edit_user")!=id){
                    response.put("code", 403);
                    response.put("msg", "someone is editing");
                }
                else{
                    int i=jdbcTemplate.update(insert_sql,doc_id,id);
                    System.out.println("update success: " + i + " rows affected");
                    response.put("code", 200);
                    response.put("msg", "doc editing");
                }
            }
            else{
                response.put("code", 402);
                response.put("msg", "permission denied");
            }
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return response;
    }
    @PostMapping("/cancel_edit_doc")
    public Map<String, Object> cancel_edit_doc(@RequestBody Map params) {
        Integer doc_id= (Integer) params.get("id");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select0_sql = "SELECT id FROM User WHERE email = ?;";
        String delete_sql="DELETE FROM Edit WHERE doc_id=? and edit_user=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select0_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id");
            int i=jdbcTemplate.update(delete_sql,doc_id,id);
            System.out.println("update success: " + i + " rows affected");
            response.put("code", 200);
            response.put("msg", "cancel editing");
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return response;
    }
    @PostMapping("/share_doc")
    public Map<String, Object> share_doc(@RequestBody Map params) {
        System.out.println(params);
        Integer doc_id= (Integer) params.get("doc_id");
        String email= (String) params.get("email");
        Integer permission=(Integer)params.get("permission");
        Map<String,Object> response = new HashMap<>();

        String select0_sql = "SELECT id FROM User WHERE email = ?;";
        String select1_sql = "SELECT id FROM Doc WHERE create_user= ? and id=?;";
        String update_sql = "UPDATE Doc SET permission=? WHERE id=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select0_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id");
            List<Map<String, Object>> tmp = jdbcTemplate.queryForList(select1_sql,id,doc_id);
            if(tmp.size()>0){
                int i=jdbcTemplate.update(update_sql,permission,doc_id);
                System.out.println("update success: " + i + " rows affected");
                response.put("code", 200);
                response.put("msg", "设置分享状态成功");
            }
            else{
                response.put("code", 402);
                response.put("msg", "permission denied");
            }
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return response;
    }
    @PostMapping("/like_doc")
    public Map<String, Object> like_doc(@RequestBody Map params) {
        Integer doc_id= (Integer) params.get("id");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select0_sql = "SELECT id FROM User WHERE email = ?;";
        String select1_sql = "SELECT id FROM Doc WHERE create_user= ? and id=?;";
        String select2_sql = "SELECT permission FROM Doc WHERE id=?;";
        String select3_sql = "SELECT permission FROM Permission WHERE doc_id=? and user=?;";
        String insert_sql = "INSERT INTO Favorite(doc_id,favorite_user) values(?,?);";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select0_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id");
            boolean is_create = jdbcTemplate.queryForList(select1_sql,id,doc_id).size()>0;
            boolean is_share=(jdbcTemplate.queryForList(select2_sql,doc_id).size()>0)&&(((int)jdbcTemplate.queryForList(select2_sql,doc_id).get(0).get("permission"))&0x01)!=0;
            boolean is_team=(jdbcTemplate.queryForList(select3_sql,doc_id,id).size()>0)&&(((int)jdbcTemplate.queryForList(select3_sql,doc_id,id).get(0).get("permission"))&0x01)!=0;
            if(is_create||is_share||is_team){
                int i=jdbcTemplate.update(insert_sql,doc_id,id);
                System.out.println("update success: " + i + " rows affected");
                response.put("code", 200);
                response.put("msg", "收藏成功");
            }
            else{
                response.put("code", 402);
                response.put("msg", "permission denied");
            }
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return response;
    }
    @PostMapping("/dislike_doc")
    public Map<String, Object> dislike_doc(@RequestBody Map params) {
        Integer doc_id= (Integer) params.get("id");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select0_sql = "SELECT id FROM User WHERE email = ?;";
        String delete_sql = "DELETE FROM Favorite WHERE doc_id=? and favorite_user=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select0_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id");
            int i=jdbcTemplate.update(delete_sql,doc_id,id);
            System.out.println("update success: " + i + " rows affected");
            response.put("code", 200);
            response.put("msg", "取消收藏");
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return response;
    }

    @PostMapping("/check_favorite_doc")
    public Map<String, Object> check_favorite_doc(@RequestBody Map params) {
        Integer doc_id= (Integer) params.get("id");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select0_sql = "SELECT id FROM User WHERE email = ?;";
        String select1_sql = "SELECT id FROM Favorite WHERE doc_id=? and favorite_user=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select0_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id");
            if(jdbcTemplate.queryForList(select1_sql,doc_id,id).size()>0){
                response.put("flag", 1);
                response.put("code", 200);
                response.put("msg", "favorite doc");
            }
            else{
                response.put("flag", 0);
                response.put("code", 200);
                response.put("msg", "not favorite doc");
            }
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return response;
    }
    @PostMapping("/get_doc")
    public Map<String, Object> get_doc(@RequestBody Map params) {
        Integer doc_id= (Integer) params.get("id");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select0_sql = "SELECT id FROM User WHERE email = ?;";
        String select1_sql = "SELECT id FROM Doc WHERE create_user= ? and id=?;";
        String select2_sql = "SELECT permission FROM Doc WHERE id=?;";
        String select3_sql = "SELECT permission FROM Permission WHERE doc_id=? and user=?;";
        String select4_sql = "SELECT id FROM Browse WHERE doc_id=? and browse_user=?;";
        String select5_sql = "SELECT id,title,content,create_user,UNIX_TIMESTAMP(create_time) as create_time,modify_user,UNIX_TIMESTAMP(modify_time) as modify_time,modify_times,team_id,permission  FROM Doc WHERE id=?;";
        String update1_sql = "INSERT INTO Browse(doc_id,browse_user) values(?,?);";
        String update2_sql = "UPDATE Browse SET browse_time =CURRENT_TIMESTAMP WHERE id=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select0_sql,email);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if(res.size()>0){
            int id= (int) res.get(0).get("id"),i=0;
            boolean is_create = jdbcTemplate.queryForList(select1_sql,id,doc_id).size()>0;
            boolean is_share=(jdbcTemplate.queryForList(select2_sql,doc_id).size()>0)&&(((int)jdbcTemplate.queryForList(select2_sql,doc_id).get(0).get("permission"))&0x01)!=0;
            boolean is_team=(jdbcTemplate.queryForList(select3_sql,doc_id,id).size()>0)&&(((int)jdbcTemplate.queryForList(select3_sql,doc_id,id).get(0).get("permission"))&0x01)!=0;
            System.out.println(is_create);
            System.out.println(jdbcTemplate.queryForList(select2_sql,doc_id).get(0));
            System.out.println(is_team);
            if(is_create||is_share||is_team){
                if(jdbcTemplate.queryForList(select4_sql,doc_id,id).size()>0){
                    i+=jdbcTemplate.update(update2_sql,(int)jdbcTemplate.queryForList(select4_sql,doc_id,id).get(0).get("id"));
                }
                else{
                    i+=jdbcTemplate.update(update1_sql,doc_id,id);
                }
                Map<String, Object> tmp=jdbcTemplate.queryForMap(select5_sql,doc_id);
                String create_time =format.format(new Date((long)tmp.get("create_time")*1000L));
                String modify_time =format.format(new Date((long)tmp.get("modify_time")*1000L));
                tmp.replace("create_time",create_time);
                tmp.replace("modify_time",modify_time);
                response.putAll(tmp);
                System.out.println("update success: " + i + " rows affected");
                response.put("code", 200);
                response.put("msg", "doc get");
            }
            else{
                response.put("code", 402);
                response.put("msg", "permission denied");
            }
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return response;
    }

    @PostMapping("/get_team_doc")
    public Map<String, Object> get_team_doc(@RequestBody Map params) {
        Integer id= (Integer) params.get("id");
        Map<String,Object> response = new LinkedHashMap();

        String select_sql = "SELECT Doc.id as doc_id,Doc.title,Doc.create_user as create_user_id,UNIX_TIMESTAMP(Doc.create_time) as create_time,Doc.modify_user as modify_user_id,UNIX_TIMESTAMP(Doc.modify_time) as modify_time FROM Doc WHERE Doc.team_id = ? ORDER BY Doc.create_time desc;";
        String select_sql_permission ="SELECT permission FROM Permission where doc_id=?";
        String select1_name_sql="SELECT name as create_user,email as create_user_email FROM User WHERE id=?;";
        String select2_name_sql="SELECT name as modify_user,email as modify_user_email FROM User WHERE id=?;";
        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> list = jdbcTemplate.queryForList(select_sql,id);
        Map<String, Object> tmp;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int i=0;
        for (Map<String, Object> map : list) {
            System.out.println(map);
            tmp=new HashMap<String, Object>();
            int create_user_id= (int) map.get("create_user_id");
            int modify_user_id= (int) map.get("modify_user_id");
            //int premmission=(int)(jdbcTemplate.queryForMap(select_name_sql,doc_id).get("premmission"));
            Map<String, Object> create_user=jdbcTemplate.queryForMap(select1_name_sql,create_user_id);
            Map<String, Object> modify_user=jdbcTemplate.queryForMap(select2_name_sql,modify_user_id);
            String create_time =format.format(new Date((long)map.get("create_time")*1000L));
            String modify_time =format.format(new Date((long)map.get("modify_time")*1000L));
            map.replace("create_time",create_time);
            map.replace("modify_time",modify_time);
            //tmp.put("premmission",premmission);
            tmp.putAll(map);
            tmp.putAll(create_user);
            tmp.putAll(modify_user);
            response.put("doc"+i++,tmp);
        }
        System.out.println(response);
        return response;
    }

    @PostMapping("/get_self_permission")
    public Map<String, Object> get_self_permission(@RequestBody Map params) {
        Integer doc_id= (Integer) params.get("id");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select0_sql = "SELECT id FROM User WHERE email = ?;";
        String select1_sql = "SELECT id FROM Doc WHERE create_user= ? and id=?;";
        String select2_sql = "SELECT permission FROM Doc WHERE id=?;";
        String select3_sql = "SELECT permission FROM Permission WHERE doc_id=? and user=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select0_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id"),i=0;
            if(jdbcTemplate.queryForList(select1_sql,id,doc_id).size()>0){
                response.put("doc_id", doc_id);
                response.put("permission",15);
                response.put("msg", "get permission");
                response.put("code",200);
            }
            else{
                int share=0,team=0;
                if(jdbcTemplate.queryForList(select2_sql,doc_id).size()>0)
                    share=(int)jdbcTemplate.queryForList(select2_sql,doc_id).get(0).get("permission");
                if(jdbcTemplate.queryForList(select3_sql,doc_id,id).size()>0)
                    team=(int)jdbcTemplate.queryForList(select3_sql,doc_id,id).get(0).get("permission");
                response.put("doc_id", doc_id);
                response.put("permission",share|team);
                response.put("msg", "get permission");
                response.put("code",200);
            }
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return response;
    }
    @PostMapping("/get_team_permission")
    public List<Map<String, Object>> get_team_permission(@RequestBody Map params) {
        Integer doc_id= (Integer) params.get("doc_id");
        Integer team_id= (Integer) params.get("team_id");
        Map<String,Object> tmp = new HashMap<>();

        String select1_sql = "SELECT member_user as member From Member WHERE team_id=?;";
        String select2_sql = "SELECT User.id as id,User.name as name,permission FROM Permission,User WHERE User.id=Permission.user and doc_id=? ";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select1_sql,team_id);
        int i=1;
        return jdbcTemplate.queryForList(select2_sql,doc_id);
    }
    @PostMapping("/set_team_permission")
    public Map<String, Object> set_team_permission(@RequestBody Map params) {
        Integer doc_id= (Integer) params.get("doc_id");
        Integer team_id= (Integer) params.get("team_id");
        List<Map<String, Object>> data= (List<Map<String, Object>>) params.get("data");
        Map<String,Object> response = new LinkedHashMap<>();

        String update_sql="UPDATE Permission SET permission=? where doc_id=? and user=?;";
        int i=0;
        for(Map<String, Object> map:data){
            i+=jdbcTemplate.update(update_sql,(int)map.get("permission"),doc_id,(int)map.get("id"));
        }
        System.out.println("update success: " + i + " rows affected");
        response.put("code", 200);
        response.put("msg", "set permission success");
        return response;
    }
    @PostMapping("/get_model_content")
    public Map<String, Object> get_model_content(@RequestBody Map params) {
        Integer moedl_id= (Integer) params.get("id");
        Map<String,Object> response = new LinkedHashMap<>();
        Map<String,Object> tmp = new HashMap<>();

        String select1_sql = "SELECT * FROM Model WHERE id=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select1_sql,moedl_id);
        if(res.size()>0){
            response.putAll(res.get(0));
            response.put("code", 200);
            response.put("msg", "model not found");
        }
        else{
            response.put("code", 401);
            response.put("msg", "model not found");
        }
        System.out.println(response);
        return response;
    }

    @PostMapping("/comment")
    public Map<String, Object> comment(@RequestBody Map params) {
        Integer doc_id= (Integer) params.get("id");
        String email= (String) params.get("email");
        String content=(String)params.get("content");
        Map<String,Object> response = new LinkedHashMap<>();

        String select0_sql = "SELECT id FROM User WHERE email = ?;";
        String select1_sql = "SELECT Doc.title,User.id as create_user_id,User.name as create_user,team_id FROM Doc,User WHERE Doc.create_user=User.id and Doc.id=?;";
        String select2_sql="SELECT id,name,create_user FROM Team WHERE id=?;";
        String insert1_sql="INSERT INTO Comment(doc_id,comment_user,content) values(?,?,?);";
        String insert2_sql = "INSERT INTO Message(type,receiver,sender,doc_id,doc_name) values(?,?,?,?,?);";
        String insert3_sql = "INSERT INTO Message(type,receiver,sender,doc_id,doc_name,team_id,team_name) values(?,?,?,?,?,?,?);";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select0_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id"),i=0;
            List<Map<String, Object>> tmp = jdbcTemplate.queryForList(select1_sql,doc_id);
            if(tmp.size()>0){
                i+=jdbcTemplate.update(insert1_sql,doc_id,id,content);
                if(tmp.get(0).get("team_id")==null){
                    i+=jdbcTemplate.update(insert2_sql,6,tmp.get(0).get("create_user_id").toString(),id,doc_id,tmp.get(0).get("title").toString());
                    response.put("code", 200);
                    response.put("msg", "comment success");
                }
                else{
                    List<Map<String, Object>> t = jdbcTemplate.queryForList(select2_sql,(int)tmp.get(0).get("team_id"));
                    if(t.size()>0){
                        i+=jdbcTemplate.update(insert3_sql,5,t.get(0).get("create_user").toString(),
                                id,doc_id,tmp.get(0).get("title").toString(),(int)t.get(0).get("id"),t.get(0).get("name").toString());
                        response.put("code", 200);
                        response.put("msg", "comment success");
                    }
                    else{
                        response.put("code", 403);
                        response.put("msg", "team not found");
                    }
                }
            }
            else{
                response.put("code", 402);
                response.put("msg", "doc not found");
            }
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return response;
    }

    @PostMapping("/get_comment")
    public Map<String, Object> get_comment(@RequestBody Map params) {
        Integer doc_id= (Integer) params.get("id");
        Map<String,Object> response = new LinkedHashMap<>();

        String select1_sql = "SELECT Comment.id,Comment.doc_id,User.id as comment_user_id,User.name as comment_user,User.email as comment_user_email,content,UNIX_TIMESTAMP(comment_time) as comment_time" +
                " FROM Comment,User WHERE Comment.comment_user=User.id and Comment.doc_id=? ORDER BY comment_time DESC;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> list = jdbcTemplate.queryForList(select1_sql,doc_id);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int i=1;
        for(Map<String,Object> map:list){
            String comment_time =format.format(new Date((long)map.get("comment_time")*1000L));
            map.replace("comment_time",comment_time);
            response.put("comment"+i++,map);
        }
        System.out.println(response);
        return response;
    }
}
