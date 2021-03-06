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
    @PostMapping("/invite")
    public Map<String, Object> invite(@RequestBody Map params) {
        Integer team_id= (Integer) params.get("team_id");
        String email= (String) params.get("email");
        Integer target=(Integer)params.get("target");
        Map<String,Object> response = new HashMap<>();

        String select1_sql = "SELECT Team.name as name,User.id as id FROM Team,User WHERE create_user=User.id and User.email = ? and Team.id=?;";
        String select2_sql = "SELECT id FROM Member WHERE team_id=? and member_user=?;";
        String insert1_sql = "INSERT INTO Message(type,receiver,sender,team_id,team_name) values(?,?,?,?,?);";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select1_sql,email,team_id);
        if(res.size()>0){
            int id= (int) res.get(0).get("id");
            String team_name=res.get(0).get("name").toString();
            if(jdbcTemplate.queryForList(select2_sql,team_id,target).size()==0){
                int i=jdbcTemplate.update(insert1_sql,0,target,id,team_id, team_name);
                System.out.println("update success: " + i + " rows affected");
                response.put("code", 200);
                response.put("msg", "invitation send");
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
    @PostMapping("/apply")
    public Map<String, Object> apply(@RequestBody Map params) {
        Integer team_id= (Integer) params.get("team_id");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select1_sql = "SELECT id FROM User WHERE email = ?;";
        String select2_sql = "SELECT id FROM Member WHERE team_id=? and member_user=?;";
        String select3_sql = "SELECT Team.name as name,User.id as id FROM Team,User WHERE create_user=User.id and Team.id=?;";
        String insert1_sql = "INSERT INTO Message(type,receiver,sender,team_id,team_name) values(?,?,?,?,?);";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select1_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id");
            System.out.println(jdbcTemplate.queryForList(select2_sql,team_id,id));
            if(jdbcTemplate.queryForList(select2_sql,team_id,id).size()==0){
                List<Map<String, Object>> tmp = jdbcTemplate.queryForList(select3_sql,team_id);
                if(tmp.size()>0){
                    int i=jdbcTemplate.update(insert1_sql,1,(int)tmp.get(0).get("id"),id,team_id, tmp.get(0).get("name").toString());
                    System.out.println("update success: " + i + " rows affected");
                    response.put("code", 200);
                    response.put("msg", "application send");
                }
                else{
                    response.put("code", 403);
                    response.put("msg", "team not found");
                }

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
    @PostMapping("/jointeam")
    public Map<String, Object> jointeam(@RequestBody Map params) {
        Integer msg= (Integer) params.get("msg");
        Integer team_id= (Integer) params.get("team_id");
        Integer id= (Integer) params.get("id");
        Map<String,Object> response = new HashMap<>();

        String select1_sql = "SELECT id FROM User WHERE email = ?;";
        String select2_sql = "SELECT id FROM Member WHERE team_id=? and member_user=?;";
        String select3_sql = "SELECT id FROM Doc WHERE team_id=?;";
        String select4_sql="SELECT name,create_user as create_user FROM Team WHERE id=?;";
        String insert1_sql = "INSERT INTO Member(team_id,member_user) values(?,?);";
        String insert2_sql = "INSERT INTO Permission(doc_id,user,permission) values(?,?,?);";
        String insert3_sql = "INSERT INTO Message(type,receiver,sender,team_id,team_name) values(?,?,?,?,?);";

        if(jdbcTemplate.queryForList(select2_sql,team_id,id).size()==0){
            List<Map<String, Object>> docs=jdbcTemplate.queryForList(select3_sql,team_id);
            int i=jdbcTemplate.update(insert1_sql,team_id, id);
            for(Map<String, Object> item:docs){
                i+=jdbcTemplate.update(insert2_sql,(int)item.get("id"),id,7);
            }
            List<Map<String, Object>> tmp=jdbcTemplate.queryForList(select4_sql,team_id);
            if(msg==0){
                i+=jdbcTemplate.update(insert3_sql,8,id,(int)tmp.get(0).get("create_user"),team_id,tmp.get(0).get("name").toString());
            }
            else{
                i+=jdbcTemplate.update(insert3_sql,9,msg,id,team_id,tmp.get(0).get("name").toString());
            }
            System.out.println("update success: " + i + " rows affected");
            response.put("code", 200);
            response.put("msg", "join team success");
        }
        else{
            response.put("code", 402);
            response.put("msg", "already in team");
        }
        System.out.println(response);
        return response;
    }
    @PostMapping("/quitteam")
    public Map<String, Object> quitteam(@RequestBody Map params) {
        Integer team_id= (Integer) params.get("id");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select1_sql = "SELECT id FROM User WHERE email = ?;";
        String select2_sql = "SELECT name,create_user FROM Team WHERE id=?;";
        String delete_sql = "DELETE FROM Member WHERE team_id=? and member_user=?;";
        String insert_sql = "INSERT INTO Message(type,receiver,sender,team_id,team_name) values(?,?,?,?,?);";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select1_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id");
            int i = jdbcTemplate.update(delete_sql,team_id, id);
            if(i==0){
                response.put("code", 402);
                response.put("msg", "not in the team");
            }
            else{
                List<Map<String, Object>> tmp = jdbcTemplate.queryForList(select2_sql,team_id);
                if(tmp.size()>0){
                    i+=jdbcTemplate.update(insert_sql,2,tmp.get(0).get("create_user").toString(),id,team_id,tmp.get(0).get("name").toString());
                    response.put("code", 200);
                    response.put("msg", "quit team success");
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
    @PostMapping("/eliminate")
    public Map<String, Object> eliminate(@RequestBody Map params) {
        Integer team_id= (Integer) params.get("id");
        String email= (String) params.get("email");
        Map<String,Object> response = new HashMap<>();

        String select1_sql = "SELECT id FROM User WHERE email = ?;";
        String select2_sql = "SELECT id FROM Team WHERE id=? and create_user=?;";
        String delete1_sql = "DELETE FROM Member WHERE team_id=?;";
        String delete2_sql = "DELETE FROM Doc WHERE team_id=?;";
        String delete3_sql = "DELETE FROM Team WHERE id=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select1_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id");
            if(jdbcTemplate.queryForList(select2_sql,team_id,id).size()>0){
                int i = jdbcTemplate.update(delete2_sql,team_id);
                i += jdbcTemplate.update(delete3_sql,team_id);
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
        String select2_sql = "SELECT id,name FROM Team WHERE id=? and create_user=?;";
        String select3_sql = "SELECT id FROM Doc WHERE team_id=?;";
        String delete1_sql = "DELETE FROM Member WHERE team_id=? and member_user=?;";
        String delete2_sql = "DELETE FROM Permission WHERE doc_id=? and user=?;";
        String insert_sql = "INSERT INTO Message(type,receiver,sender,team_id,team_name) values(?,?,?,?,?);";
        String update_sql = "UPDATE Doc SET create_user=?,create_time=CURRENT_TIMESTAMP WHERE team_id=? and create_user=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select1_sql,email);
        if(res.size()>0){
            int id= (int) res.get(0).get("id");
            List<Map<String, Object>> tmp=jdbcTemplate.queryForList(select2_sql,team_id,id);
            if(tmp.size()>0){
                if(id==target){
                    response.put("code", 405);
                    response.put("msg", "cant dismiss leader");
                }
                else{
                    int i = jdbcTemplate.update(delete1_sql,team_id,target);
                    if(i!=0){
                        List<Map<String, Object>> docs=jdbcTemplate.queryForList(select3_sql,team_id);
                        for(Map<String, Object> item:docs){
                            i+=jdbcTemplate.update(delete2_sql,(int)item.get("id"),target);
                        }
                        i+=jdbcTemplate.update(insert_sql,7,target,id,team_id,tmp.get(0).get("name").toString());
                        i+=jdbcTemplate.update(update_sql,id,team_id,target);
                        System.out.println("update success: " + i + " rows affected");
                        response.put("code", 200);
                        response.put("msg", "dismiss member success");
                    }else{
                        response.put("code", 403);
                        response.put("msg", "not in the team");
                    }
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
    public List<Map<String, Object>> searchTeam(@RequestBody Map params) {
        String team_name= (String) params.get("team_name");
        Map<String,Object> response = new LinkedHashMap<>();
        Map<String, Object> tmp=new HashMap<String, Object>();

        String select_sql = "SELECT Team.id as id,Team.name as team_name,User.name as create_user_name FROM Team,User " +
                "WHERE Team.name LIKE CONCAT('%',CONCAT(?,'%')) and User.id=Team.create_user;";
        int i=0;
        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> list = jdbcTemplate.queryForList(select_sql,team_name);
        for (Map<String, Object> map : list){
            tmp=new HashMap<String, Object>();
            int id = (int) map.get("id");
            /*tmp.put("id",(int) map.get("id"));
            tmp.put("team_name",map.get("name").toString());
            tmp.put("create_user_name",map.get("create_user_name").toString());
            response.put("team"+i++,tmp);*/
        }
        System.out.println(response);
        return list;
    }

    @PostMapping("/myTeam")
    public List<Map<String, Object>> myTeam(@RequestBody Map params) {
        String email= (String) params.get("email");
        Map<String,Object> response = new LinkedHashMap<>();

        String select1_sql = "SELECT id FROM User WHERE email = ?;";
        String select2_sql = "SELECT Team.id as id,Team.name as name,User.id as create_user_id,User.name as create_user,UNIX_TIMESTAMP(Team.create_time) as create_time" +
                " FROM Team,Member,User WHERE Team.create_user=User.id and Team.id=Member.team_id and Member.member_user=?;";
        String select3_sql = "SELECT count(*) as number FROM Member WHERE team_id=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> res = jdbcTemplate.queryForList(select1_sql,email);
        List<Map<String, Object>> list=null;
        if(res.size()>0){
            int id= (int) res.get(0).get("id"),i=0;
            list =jdbcTemplate.queryForList(select2_sql,id);
            for(Map<String, Object> map:list){
                if(map.get("create_time")!=null){
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    String create_time_str=format.format(new Date((long)map.get("create_time")*1000L));
                    map.replace("create_time",create_time_str);
                }
                map.put("number",Integer.parseInt(jdbcTemplate.queryForList(select3_sql,(int)map.get("id")).get(0).get("number").toString()));
                response.put("team"+i++,map);
            }
        }
        else{
            response.put("code", 401);
            response.put("msg", "user not found");
        }
        System.out.println(response);
        return list;
    }
    @PostMapping("/getTeamMember")
    public List<Map<String, Object>> getTeamMember(@RequestBody Map params) {
        Integer team_id= (Integer) params.get("id");
        List<Map<String, Object>> response = new ArrayList<>();

        String select1_sql = "SELECT team_id,User.id as member_id,User.name as member_name FROM User,Member WHERE Member.member_user=User.id and Member.team_id=? ORDER BY CONVERT(member_name USING gbk) DESC;";
        String select2_sql = "SELECT create_user FROM Team WHERE Team.id=?;";
        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> list = jdbcTemplate.queryForList(select1_sql,team_id);
        System.out.println(list);
        return list;
    }
    @PostMapping("/getTeamMemberWithoutLeader")
    public List<Map<String, Object>> getTeamMemberWithoutLeader(@RequestBody Map params) {
        Integer team_id= (Integer) params.get("id");
        List<Map<String, Object>> response = new ArrayList<>();

        String select1_sql = "SELECT team_id,User.id as member_id,User.name as member_name FROM User,Member WHERE Member.member_user=User.id and Member.team_id=? ORDER BY CONVERT(member_name USING gbk) DESC;";
        String select2_sql = "SELECT create_user FROM Team WHERE Team.id=?;";
        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> list = jdbcTemplate.queryForList(select1_sql,team_id);
        List<Map<String, Object>> leader = jdbcTemplate.queryForList(select2_sql,team_id);
        int i=0;
        if(leader.size()>0)
            i=(int)leader.get(0).get("create_user");
        for(Map<String, Object> map:list){
            if((int)map.get("member_id")!=i)
                response.add(map);
        }
        System.out.println(response);
        return response;
    }
    @PostMapping("/getTeamLeader")
    public Map<String, Object> getTeamLeader(@RequestBody Map params) {
        Integer team_id= (Integer) params.get("id");
        Map<String,Object> response = new LinkedHashMap<>();

        String select1_sql = "SELECT Team.name as name,User.id as create_user_id,User.name as create_user FROM User,Team WHERE User.id=Team.create_user and Team.id=?;";

        // 通过jdbcTemplate查询数据库
        List<Map<String, Object>> list = jdbcTemplate.queryForList(select1_sql,team_id);
        if(list.size()>0){
            response.put("list",list);
            response.put("code", 200);
            response.put("msg", "get team leader");
        }
        else{
            response.put("code", 401);
            response.put("msg", "team not found");
        }
        System.out.println(response);
        return response;
    }
}
