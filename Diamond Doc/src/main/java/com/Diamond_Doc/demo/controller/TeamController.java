package com.Diamond_Doc.demo.controller;

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
public class TeamController {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    private EmailSender emailSender;

    @PostMapping("/buildteam")
    public Map<String, Object> buildteam(@RequestBody Map params) {
        String team_name= (String) params.get("team_name");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select_sql = "SELECT id FROM User WHERE email = ?;";
        String insert1_sql = "INSERT INTO Team(name,create_user) values(?,?);";
        String insert2_sql = "INSERT INTO Member(team_id,member_user) values(?,?);";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id");
            int i = jdbcTemplate.update(insert1_sql,team_name, id);
            int key=Integer.parseInt(jdbcTemplate.queryForMap("SELECT LAST_INSERT_ID() as 'key';").get("key").toString());
            i+=jdbcTemplate.update(insert2_sql,key,id);
            System.out.println("update success: " + i + " rows affected");
            response.put("code", 200);
            response.put("msg", "build team success");
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return response;
    }
    @PostMapping("/jointeam")
    public Map<String, Object> jointeam(@RequestBody Map params) {
        Integer team_id= (Integer) params.get("team_id");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select1_sql = "SELECT id FROM User WHERE email = ?;";
        String select2_sql = "SELECT id FROM Member WHERE team_id=? and member_user=?;";
        String select3_sql = "SELECT id FROM Doc WHERE team_id=?;";
        String insert1_sql = "INSERT INTO Member(team_id,member_user) values(?,?);";
        String insert2_sql = "INSERT INTO Permission(doc_id,user,permission) values(?,?,?);";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select1_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id");
            if(jdbcTemplate.queryForList(select2_sql,team_id,id).size()>0){
                List<Map<String, Object>> docs=jdbcTemplate.queryForList(select3_sql,team_id);
                int i=jdbcTemplate.update(insert1_sql,team_id, id);
                for(Map<String, Object> item:docs){
                    i+=jdbcTemplate.update(insert2_sql,(int)item.get("id"),id,15);
                }
                response.put("code", 200);
                response.put("msg", "join team success");
            }
            else{
                response.put("code", 402);
                response.put("msg", "already in team");
            }
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return response;
    }
    @PostMapping("/quitteam")
    public Map<String, Object> quitteam(@RequestBody Map params) {
        Integer team_id= (Integer) params.get("id");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select_sql = "SELECT id FROM User WHERE email = ?;";
        String delete_sql = "DELETE FROM Member WHERE team_id=? and member_user=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id");
            int i = jdbcTemplate.update(delete_sql,team_id, id);
            System.out.println("update success: " + i + " rows affected");
            if(i==0){
                response.put("code", 402);
                response.put("msg", "not in the team");
            }
            else{
                response.put("code", 200);
                response.put("msg", "quit team success");
            }
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return response;
    }
    @PostMapping("/eliminate")
    public Map<String, Object> eliminate(@RequestBody Map params) {
        Integer team_id= (Integer) params.get("id");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select1_sql = "SELECT id FROM User WHERE email = ?;";
        String select2_sql = "SELECT id FROM Team WHERE id=? and create_user=?;";
        String delete1_sql = "DELETE FROM Member WHERE team_id=?;";
        String delete2_sql = "DELETE FROM Doc WHERE team_id=?;";
        String delete3_sql = "DELETE FROM Team WHERE team_id=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select1_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id");
            if(jdbcTemplate.queryForList(select2_sql,team_id,id).size()>0){
                int i = jdbcTemplate.update(delete3_sql,team_id);
                System.out.println("update success: " + i + " rows affected");
                response.put("code", 200);
                response.put("msg", "eliminate team success");
            }
            else{
                response.put("code", 402);
                response.put("msg", "not team leader");
            }
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return response;
    }
    @PostMapping("/dismiss")
    public Map<String, Object> dismiss(@RequestBody Map params) {
        Integer team_id= (Integer) params.get("id");
        String email= (String) params.get("email");
        Integer target= (Integer) params.get("target");
        Map<String,Object> response = new HashMap<>();

        String select1_sql = "SELECT id FROM User WHERE email = ?;";
        String select2_sql = "SELECT id FROM Team WHERE id=? and create_user=?;";
        String select3_sql = "SELECT id FROM Doc WHERE team_id=?;";
        String delete1_sql = "DELETE FROM Member WHERE team_id=? and member_user=?;";
        String delete2_sql = "DELETE FROM Permission WHERE doc_id=? and user=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select1_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id");
            if(jdbcTemplate.queryForList(select2_sql,team_id,id).size()>0){
                int i = jdbcTemplate.update(delete1_sql,team_id,id);
                if(i!=0){
                    List<Map<String, Object>> docs=jdbcTemplate.queryForList(select3_sql,team_id);
                    for(Map<String, Object> item:docs){
                        i+=jdbcTemplate.update(delete2_sql,(int)item.get("id"),id);
                    }
                    System.out.println("update success: " + i + " rows affected");
                    response.put("code", 200);
                    response.put("msg", "dismiss member success");
                }else{
                    response.put("code", 403);
                    response.put("msg", "not in the team");
                }
            }
            else{
                response.put("code", 402);
                response.put("msg", "not team leader");
            }
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return response;
    }

    @PostMapping("/searchTeam")
    public Map<String, Object> searchTeam(@RequestBody Map params) {
        String team_name= (String) params.get("team_name");
        Map<String,Object> response = new LinkedHashMap<>();
        Map<String, Object> tmp=new HashMap<String, Object>();

        String select_sql = "SELECT Team.id as id,Team.name as name,User.name as create_user_name FROM Team,User " +
                "WHERE Team.name LIKE CONCAT('%',CONCAT(?,'%')) and User.id=Team.create_user;";
        System.out.println(select_sql);
        int i=0;
        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> list = jdbcTemplate.queryForList(select_sql,team_name);
        for (Map<String, Object> map : list){
            tmp=new HashMap<String, Object>();
            int id = (int) map.get("id");
            tmp.put("id",(int) map.get("id"));
            tmp.put("team_name",map.get("name").toString());
            tmp.put("create_user_name",map.get("create_user_name").toString());
            response.put("team"+i++,tmp);
        }
        System.out.println(response);
        return response;
    }

    @PostMapping("/myTeam")
    public Map<String, Object> myTeam(@RequestBody Map params) {
        String email= (String) params.get("email");
        Map<String,Object> response = new LinkedHashMap<>();

        String select1_sql = "SELECT id FROM User WHERE email = ?;";
        String select2_sql = "SELECT Team.id as id,Team.name as name,Team.create_user as create_user,UNIX_TIMESTAMP(Team.create_time) as create_time" +
                " FROM Team,Member WHERE Team.id=Member.team_id and Member.member_user=?;";
        String select3_sql = "SELECT ;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select1_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id"),i=0;
            List<Map<String, Object>> list =jdbcTemplate.queryForList(select2_sql,id);
            for(Map<String, Object> map:list){
                response.put("team"+i++,map);
                if(map.get("create_time")!=null){
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    String create_time_str=format.format(new Date((long)map.get("create_time")*1000L));
                    map.replace("create_time",create_time_str);
                }
            }
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return response;
    }
}
