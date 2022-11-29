/*
 * The MIT License
 *
 * Copyright 2022 Rza Asadov (rza dot asadov at gmail dot com).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.acceix.frontend.database;


import org.acceix.frontend.models.RoleModel;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acceix.ndatabaseclient.MachineDataException;
import org.acceix.ndatabaseclient.MachineDataSet;
import org.acceix.ndatabaseclient.DataConnector;
import org.acceix.ndatabaseclient.ResultSetConverter;


/**
 *
 * @author zrid
 */
public class AdminFunctions {
    


    
        private final Map<String,Object> envs;

        
        public AdminFunctions(Map<String, Object> envs,String username) {
            this.envs = envs;
            nDataConnector = new DataConnector(envs,username);
        }

        private final ResultSetConverter resultConverter = new ResultSetConverter();
        
        private Connection connection;
        private static String schemaName;
        
        
        private final DataConnector nDataConnector;        
    
    
        private Connection connect() throws ClassNotFoundException, SQLException  {
 
                        
                            Class.forName(envs.get("database_driver").toString());
                            
                            schemaName = envs.get("database_schema").toString() + ".";
                            
                            String connectionString = "jdbc:mariadb://" + envs.get("database_host") + ":" + envs.get("database_port") 
                                    + "/" + envs.get("database_schema") + "?user=" + envs.get("database_user") + "&password=" + envs.get("database_password") + "&useUnicode=yes;characterEncoding=utf16"; // + "&minPoolSize=20&maxPoolSize=1000&maxIdleTime=80&pool"
                           
                               
                            try {
                                connection = DriverManager.getConnection(connectionString);
                                connection.setAutoCommit(true);
                            } catch (SQLException ex) {
                                Logger.getLogger(AdminFunctions.class.getName()).log(Level.SEVERE, null, ex);
                            }


                    return connection;
        }
        
        private PreparedStatement getPreparedStatement(Connection c,String sql) throws SQLException {
                return c.prepareStatement(sql);
        }    
 
        private Statement getStatement(Connection c) throws SQLException {
                return c.createStatement();
        }          
        
        

        
        public boolean isUserExists(String username) throws MachineDataException, ClassNotFoundException, SQLException {
              
      
                MachineDataSet machineDataSet = nDataConnector.getTable("npt_users")
                                            .select()
                                                .getColumn("id")
                                            .where()
                                                .eq("username",username)
                                            .compile()
                                            .executeSelect();

                return machineDataSet.next();            
            
        }
        

        
        public boolean isGroupExists(String groupname,int owneruserid) throws MachineDataException, ClassNotFoundException, SQLException {
            
                MachineDataSet machineDataSet = nDataConnector.getTable("npt_groups")
                                            .select()
                                                .getColumn("id")
                                            .where()
                                                .eq("owneruserid",owneruserid)
                                                .eq("groupname", groupname)
                                            .compile()
                                            .executeSelect();     

                return machineDataSet.next();     
                
                
        }        
        
    
        
        public boolean isRoleSetNameExists(String rolesetname,int owneruserid) throws MachineDataException, ClassNotFoundException, SQLException {
            
                MachineDataSet machineDataSet = nDataConnector.getTable("npt_rolesets")
                                            .select()
                                                .getColumn("id")
                                            .where()
                                                .eq("owneruserid",owneruserid)
                                                .eq("rolesetname", rolesetname)
                                            .compile()
                                            .executeSelect();     

                return machineDataSet.next();              
        }        

        
        public void addUser(String username,String password,String desc,int status,int groupid,int rolesetid,String domaintoken,String defaultpage) throws MachineDataException, SQLException, ClassNotFoundException, NoSuchAlgorithmException {
                

                MessageDigest md5 = MessageDigest.getInstance("MD5"); // you can change it to SHA1 if needed!
                md5.update(password.getBytes(), 0, password.length());

            
                nDataConnector.getTable("npt_users")
                                       .insert()
                                            .add("username", username)
                                            .add("password", new BigInteger(1, md5.digest()).toString(16))
                                            .add("mdesc", desc)
                                            .add("status", status)
                                            .add("groupid", groupid)
                                            .add("rolesetid", rolesetid)
                                            .add("domain_token", domaintoken)
                                            .add("def_page", defaultpage)
                                        .compile()
                                        .execute();
            
        }        
        
        // Security checked
        
        public void addGroup(String groupname,String groupdesc,int owneruserid,int mandatory) throws SQLException, ClassNotFoundException {

            
            nDataConnector.getTable("npt_groups")
                                    .insert()
                                        .add("groupname",groupname)
                                        .add("groupdesc",groupdesc)
                                        .add("owneruserid",owneruserid)
                                        .add("mandatory",mandatory)
                                    .compile()
                                    .execute();

        }   
        
        // Security checked
        public void addLinkedGroup(int groupid,int userid) throws SQLException, ClassNotFoundException {

            
            nDataConnector.getTable("npt_linked_groups")
                                    .insert()
                                        .add("groupid",groupid)
                                        .add("userid",userid)
                                    .compile()
                                    .execute();

        }           
        
        // Security checked
        public int addRoleToRoleSet(int roleid,String rolesetname,String rolesetdesc,int owneruserid) throws MachineDataException {
            return addRoleToRoleSet(-1, roleid, rolesetname, rolesetdesc, owneruserid);
        }    
        
        public void addActivityLog(int activity_type, int user_id, String ip_address, int country_id, String system_name) throws MachineDataException, SQLException, ClassNotFoundException {
                
                nDataConnector.getTable("npt_activity_logs")
                                       .insert()
                                            .add("activity_type", activity_type)
                                            .add("user_id", user_id)
                                            .add("ip_address", ip_address)
                                            .add("country_id", country_id)
                                            .add("system_name", system_name)
                                        .compile()
                                        .execute();
            
        }           
        
        // Security checked
        public int addRoleToRoleSet(int setid,int roleid,String rolesetname,String rolesetdesc,int owneruserid) {

            
            try {
            
                if (setid < 0) {
                    MachineDataSet dataSet = nDataConnector.getTable("npt_rolesets")
                                                                    .select()
                                                                        .getColumn("MAX(setid) as maxsetidvalue")
                                                                    .where()
                                                                        .eq("owneruserid", owneruserid)
                                                                    .compile()
                                                                    .executeSelect();
                    if (dataSet.next()) {
                        setid = dataSet.getInteger("maxsetidvalue") + 1;
                    }
                }

                nDataConnector.getTable("npt_rolesets")
                                        .insert()
                                            .add("setid",setid)
                                            .add("roleid",roleid)
                                            .add("rolesetname",rolesetname)
                                            .add("rolesetdesc", rolesetdesc)
                                            .add("owneruserid",owneruserid)
                                        .compile()
                                        .execute();
                return setid;
            } catch (ClassNotFoundException | SQLException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                return -1;
            }



        }  
        
        // Security checked
        public void delRoleSet(int setid,int owneruserid) throws MachineDataException, ClassNotFoundException, SQLException {
            
            
            nDataConnector.getTable("npt_rolesets")
                                    .delete()
                                    .where()
                                        .eq("setid", setid)
                                        .eq("owneruserid", owneruserid)
                                    .compile()
                                    .execute();

        }          
        
        // Security checked
        public void delRoleFromRoleSet(int setid,int roleid,int owneruserid) throws MachineDataException, ClassNotFoundException, SQLException {
            
            nDataConnector.getTable("npt_rolesets")
                                    .delete()
                                    .where()
                                        .eq("setid", setid)
                                        .eq("roleid", roleid)
                                        .eq("owneruserid", owneruserid)
                                    .compile()
                                    .execute();
            
        }          
        
        // Security checked
        public void delGroup(int groupid,int owneruserid) throws MachineDataException, ClassNotFoundException, SQLException {
            
            nDataConnector.getTable("npt_groups")
                                    .delete()
                                    .where()
                                        .eq("id", groupid)         
                                    .compile()
                                    .execute();

        } 
        
        
        // Security checked
        public void executeStatement(String sqlStatement) throws MachineDataException, ClassNotFoundException, SQLException {

            try (Connection c = connect(); Statement stmt = getStatement(c)) {
                stmt.execute(sqlStatement);
            }


        }          
        
        // Security checked
        public void delLinkedGroup(int groupid,int userid) throws MachineDataException, ClassNotFoundException, SQLException {

            nDataConnector.getTable("npt_linked_groups")
                                    .delete()
                                    .where()
                                        .eq("groupid", groupid)
                                        .eq("userid", userid)
                                    .compile()
                                    .execute();

        }         
                
        
        // Security checked
        public void delUser(int userid,int owneruserid) throws MachineDataException {


            try {
                try (Connection c = connect()) {
                    String sql = "delete from " + schemaName + "npt_users where id = ?";
                    try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                        pstmt.setInt(1, userid);
                        //pstmt.setInt(2, owneruserid);
                        pstmt.executeUpdate();
                    }
                }
            } catch (ClassNotFoundException | SQLException ex) {
                throw new MachineDataException(ex);
            }
        }          
        
        public List<String> getUserRoles(int userid) throws MachineDataException {
            List<String> result = new ArrayList<>();
            try {
                    try (Connection c = connect()) {
                        String sql = "select rolename from " + schemaName + "npt_rolelist where id IN (SELECT roleid FROM npt_rolesets WHERE setid = (select rolesetid from npt_users where id = ?))";
                        try (PreparedStatement stmt = getPreparedStatement(c, sql)) {
                            stmt.setInt(1, userid);
                            //stmt.setInt(2, groupid);
                            try (ResultSet rs = stmt.executeQuery()) {
                                while (rs.next()) {
                                    result.add(rs.getString("rolename"));
                                } 
                            }
                        }
                    }
                    return result;
            } catch (SQLException | ClassNotFoundException ex) {
                throw new MachineDataException(ex);
            }
        }  
        
        
        
        public String getDefaultPageOfUser(int userid) throws  ClassNotFoundException, SQLException {
            
            
                MachineDataSet machineDataSet = nDataConnector.getTable("npt_users")
                                            .select()
                                                .getColumn("def_page")
                                            .where()
                                                .eq("id",userid)
                                            .compile()
                                            .executeSelect();     
                
                if (machineDataSet.next()) {
                    return machineDataSet.getString("def_page");
                } else {
                    return null;
                }

        } 
        
        public String getPasswordOfUser(int userid) throws MachineDataException, ClassNotFoundException, SQLException {
            
                MachineDataSet machineDataSet = nDataConnector.getTable("npt_users")
                                            .select()
                                                .getColumn("password")
                                            .where()
                                                .eq("id",userid)
                                            .compile()
                                            .executeSelect();  
                
                
                if (machineDataSet.next()) {
                    return machineDataSet.getString("password");
                } else {
                    return null;
                }                
            

        } 

        public String getUsernameOfUser(int userid) throws MachineDataException, ClassNotFoundException, SQLException {
            
                MachineDataSet machineDataSet = nDataConnector.getTable("npt_users")
                                            .select()
                                                .getColumn("username")
                                            .where()
                                                .eq("id",userid)
                                            .compile()
                                            .executeSelect();  
                
                
                if (machineDataSet.next()) {
                    return machineDataSet.getString("username");
                } else {
                    return null;
                }                

        }   
        
        public String getDomainIdOfUser(int userid) throws MachineDataException, ClassNotFoundException, SQLException {
            
                MachineDataSet machineDataSet = nDataConnector.getTable("npt_users")
                                            .select()
                                                .getColumn("domain_id")
                                            .where()
                                                .eq("id",userid)
                                            .compile()
                                            .executeSelect();  
                
                
                if (machineDataSet.next()) {
                    return String.valueOf(machineDataSet.getInteger("domain_id"));
                } else {
                    return null;
                }                

        }         

        public int getGroupIdOfUser(int userid) throws MachineDataException, ClassNotFoundException, SQLException {
            
            
                MachineDataSet machineDataSet = nDataConnector.getTable("npt_users")
                                            .select()
                                                .getColumn("groupid")
                                            .where()
                                                .eq("id",userid)
                                            .compile()
                                            .executeSelect();  
                
                
                if (machineDataSet.next()) {
                    return machineDataSet.getInteger("groupid");
                } else {
                    return -1;
                }             
            

        } 
        
        public int getGroupId(String groupname) throws MachineDataException, ClassNotFoundException, SQLException {
            
                MachineDataSet machineDataSet = nDataConnector.getTable("npt_groups")
                                            .select()
                                                .getColumn("id")
                                            .where()
                                                .eq("groupname",groupname)
                                            .compile()
                                            .executeSelect();  
                
                
                if (machineDataSet.next()) {
                    return machineDataSet.getInteger("id");
                } else {
                    return -1;
                }              
            

        }             

        public int getRolesetIdOfUser(int userid) throws MachineDataException, ClassNotFoundException, SQLException {
            
                MachineDataSet machineDataSet = nDataConnector.getTable("npt_users")
                                            .select()
                                                .getColumn("rolesetid")
                                            .where()
                                                .eq("id",userid)
                                            .compile()
                                            .executeSelect();  
                
                
                if (machineDataSet.next()) {
                    return machineDataSet.getInteger("rolesetid");
                } else {
                    return -1;
                }              
            

        }    
        
        public String getTokenOfDomain (String domain) throws ClassNotFoundException, SQLException  {

                MachineDataSet machineDataSet = nDataConnector.getTable("npt_domains")
                                            .select()
                                                .getColumn("domain_token")
                                            .where()
                                                .eq("url",domain)
                                                .eq("active",true)
                                            .compile()
                                            .executeSelect();   
                
                if (machineDataSet.next()) {
                    return machineDataSet.getString("domain_token");
                } else {
                    return null;
                }                  
        }
        
        public int getIdOfDomain (String domain) throws ClassNotFoundException, SQLException  {

                MachineDataSet machineDataSet = nDataConnector.getTable("npt_domains")
                                            .select()
                                                .getColumn("id")
                                            .where()
                                                .eq("url",domain)
                                                .eq("active",true)
                                            .compile()
                                            .executeSelect();   
                
                if (machineDataSet.next()) {
                    return machineDataSet.getInteger("id");
                } else {
                    return -1;
                }                  
        }        

        public String getTitleOfDomain (String domain) throws ClassNotFoundException, SQLException  {

                MachineDataSet machineDataSet = nDataConnector.getTable("npt_domains")
                                            .select()
                                                .getColumn("title")
                                            .where()
                                                .eq("url",domain)
                                                .eq("active",true)
                                            .compile()
                                            .executeSelect();   
                
                if (machineDataSet.next()) {
                    return machineDataSet.getString("title");
                } else {
                    return null;
                }                  
        }        
        
        
        public String getMainpageOfDomain (String domain) throws ClassNotFoundException, SQLException  {

                MachineDataSet machineDataSet = nDataConnector.getTable("npt_domains")
                                            .select()
                                                .getColumn("mainpage")
                                            .where()
                                                .eq("url",domain)
                                                .eq("active",1)
                                            .compile()
                                            .executeSelect();   
                
                if (machineDataSet.next()) {
                    return machineDataSet.getString("mainpage");
                } else {
                    return null;
                }                  
        }      
        
        public String getSignInPageOfDomain (String domain) throws ClassNotFoundException, SQLException  {

                MachineDataSet machineDataSet = nDataConnector.getTable("npt_domains")
                                            .select()
                                                .getColumn("signinpage")
                                            .where()
                                                .eq("url",domain)
                                                .eq("active",1)
                                            .compile()
                                            .executeSelect();   
                
                if (machineDataSet.next()) {
                    return machineDataSet.getString("signinpage");
                } else {
                    return null;
                }                  
        }
        
        public boolean getWebRegistrationStatus (String domain) throws ClassNotFoundException, SQLException  {

                MachineDataSet machineDataSet = nDataConnector.getTable("npt_domains")
                                            .select()
                                                .getColumn("enable_web_registration")
                                            .where()
                                                .eq("url",domain)
                                                .eq("active",1)
                                            .compile()
                                            .executeSelect();   
                
                if (machineDataSet.next()) {
                    return machineDataSet.getBoolean("enable_web_registration");
                } else {
                    return false;
                }                  
        }         

        public String getSignUpPageOfDomain (String domain) throws ClassNotFoundException, SQLException  {

                MachineDataSet machineDataSet = nDataConnector.getTable("npt_domains")
                                            .select()
                                                .getColumn("signuppage")
                                            .where()
                                                .eq("domain",domain)
                                                .eq("active",1)
                                            .compile()
                                            .executeSelect();
                
                if (machineDataSet.next()) {
                    return machineDataSet.getString("signuppage");
                } else {
                    return null;
                }                  
        }   
        
        
        public String getDomainByToken (String token) throws ClassNotFoundException, SQLException  {
            
                MachineDataSet machineDataSet = nDataConnector.getTable("npt_domains")
                                            .select()
                                                .getColumn("url")
                                            .where()
                                                .eq("domain_token",token)
                                            .compile()
                                            .executeSelect();   
                
                if (machineDataSet.next()) {
                    return machineDataSet.getString("url");
                } else {
                    return null;
                }                  
        } 
        
        public String getDomainById (String id) throws ClassNotFoundException, SQLException  {
            
                MachineDataSet machineDataSet = nDataConnector.getTable("npt_domains")
                                            .select()
                                                .getColumn("url")
                                            .where()
                                                .eq("id",id)
                                            .compile()
                                            .executeSelect();   
                
                if (machineDataSet.next()) {
                    return machineDataSet.getString("url");
                } else {
                    return null;
                }                  
        }         
        
        public Map<String,String> getDomainList () throws ClassNotFoundException, SQLException  {
            
                Map<String,String> domainTokenList = new LinkedHashMap<>();
            
                MachineDataSet machineDataSet = nDataConnector.getTable("npt_domains")
                                            .select()
                                                .getColumn("domain")
                                                .getColumn("domain_token")
                                            .compile()
                                            .executeSelect();   
                
                while (machineDataSet.next()) {
                    String domain =  machineDataSet.getString("domain");
                    String token = machineDataSet.getString("domain_token");
                    domainTokenList.put(domain, token);
                }      
                
                return domainTokenList;
        } 
                
        
        
        // Security checked
        public int getUserId(String username,String password,int domain_id,int status) throws ClassNotFoundException, SQLException, NoSuchAlgorithmException {
            
            
                MessageDigest md5 = MessageDigest.getInstance("MD5"); // you can change it to SHA1 if needed!
                md5.update(password.getBytes(), 0, password.length());

                MachineDataSet machineDataSet = nDataConnector.getTable("npt_users")
                                            .select()
                                                .getColumn("id")
                                            .where()
                                                .eq("username",username)
                                                .eq("password",new BigInteger(1, md5.digest()).toString(16))
                                                .eq("domain_id",domain_id)
                                                .eq("status",status)
                                            .compile()
                                            .executeSelect();  
                
                
                if (machineDataSet.next()) {
                    return machineDataSet.getInteger("id");
                } else {
                    return -1;
                }            


        }       
        
        // Security checked
        public MachineDataSet getUserInfo(int userid) throws MachineDataException {
            
            
            try {
                MachineDataSet resultDataSet;
                try (Connection c = connect()) {            
                    String sql = "select u.username as username,u.mdesc as mdesc,u.groupid as groupid,u.rolesetid as rolesetid,u.password as password,u.domain_id as domain_id from " + schemaName + "npt_users as u," + schemaName + "npt_groups as g where u.id = ? and u.groupid = g.id";
                    try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                        pstmt.setInt(1, userid);
                        resultDataSet = resultConverter.resultSetToMachineDataSet(pstmt.executeQuery());
                    }
                }
                    return resultDataSet;                    
            } catch (SQLException | ClassNotFoundException ex) {
                throw new MachineDataException(ex);
            } 
        }           
        
       
        // Security checked
        public MachineDataSet getUsersByOwner(int owneruserid) throws MachineDataException {
            
           
            try {
                MachineDataSet resultDataSet;
                try (Connection c = connect()) {
                    String sql = "select u.id as id,u.username as username,u.mdesc as mdesc,g.groupname as groupname,r.rolesetname as rolesetname from " + schemaName + "npt_users as u," + schemaName + "npt_groups as g," + schemaName + "npt_rolesets as r where u.groupid = g.id and u.rolesetid = r.setid and u.groupid IN (select id from npt_groups where owneruserid = ?)  group by u.id,u.username,u.mdesc,g.groupname,r.rolesetname order by u.id DESC";
                    try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                        pstmt.setInt(1, owneruserid);
                        resultDataSet = resultConverter.resultSetToMachineDataSet(pstmt.executeQuery());
                    }
                }
                return resultDataSet;
            } catch (ClassNotFoundException | SQLException ex) {
                throw new MachineDataException(ex);
            } 
        }    
        
        // Security checked
        public MachineDataSet getAllGroupsByOwner(int owneruserid) throws MachineDataException {
            

            try {
                MachineDataSet resultDataSet;
                try (Connection c = connect()) {                                
                    String sql = "select id,groupname,groupdesc from " + schemaName + "npt_groups where owneruserid = ? OR id IN (select groupid from " + schemaName + "npt_linked_groups where userid = ?) order by id DESC";
                    try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                        pstmt.setInt(1, owneruserid);
                        pstmt.setInt(2, owneruserid);
                        resultDataSet = resultConverter.resultSetToMachineDataSet(pstmt.executeQuery());
                    }
                }
                    return resultDataSet;
            } catch (SQLException | ClassNotFoundException ex) {
                throw new MachineDataException(ex);
            } 
        }    
        
        
        // Security checked
        public MachineDataSet getUserGroupsByOwner(int owneruserid) throws MachineDataException {
            

            try {
                MachineDataSet resultDataSet;
                try (Connection c = connect()) {                                
                    String sql = "select gr.id as id,gr.groupname as groupname,gr.groupdesc as groupdesc,count(agents.id) as groupsize from " + schemaName + "npt_groups as gr," + schemaName + "npt_users as agents where (gr.owneruserid = ? OR gr.id IN (select groupid from npt_linked_groups where userid = ?)) and agents.groupid = gr.id group by gr.id,gr.groupname,gr.groupdesc order by gr.id";
                    try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                        pstmt.setInt(1, owneruserid);
                        pstmt.setInt(2, owneruserid);
                        resultDataSet = resultConverter.resultSetToMachineDataSet(pstmt.executeQuery());
                    }
                }
                    return resultDataSet;
            } catch (SQLException | ClassNotFoundException ex) {
                throw new MachineDataException(ex);
            } 
        }           
        
        // Security checked
        public MachineDataSet getAgentGroupsByOwner(int owneruserid) throws MachineDataException {
            

            try {
                MachineDataSet resultDataSet;
                try (Connection c = connect()) {            
                    String sql = "select gr.id as id,gr.groupname as groupname,gr.groupdesc as groupdesc,count(agents.id) as groupsize from " + schemaName + "npt_groups as gr," + schemaName + "online_agents as agents where (gr.owneruserid = ? OR gr.id IN (select groupid from npt_linked_groups where userid = ?)) and agents.ownergroupid = gr.id and agents.muser is not null group by gr.id,gr.groupname,gr.groupdesc order by gr.id";
                    try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                        pstmt.setInt(1, owneruserid);
                        pstmt.setInt(2, owneruserid);
                        resultDataSet = resultConverter.resultSetToMachineDataSet(pstmt.executeQuery());
                    }
                }
                    return resultDataSet;
            } catch (SQLException | ClassNotFoundException ex) {
                throw new MachineDataException(ex);
            } 
        }         
        
        // Security checked
        public MachineDataSet getGroupsByLinkeduser(int linkeduserid) throws MachineDataException {
            

            try {
                MachineDataSet resultDataSet;
                try (Connection c = connect()) {            
                    String sql = "select lg.groupid as groupid,g.groupname as groupname,g.groupdesc as groupdesc from " + schemaName + "npt_linked_groups as lg," + schemaName + "npt_groups as g where lg.userid = ? and lg.groupid = g.id order by lg.groupid DESC";
                    try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                        pstmt.setInt(1, linkeduserid);
                        resultDataSet = resultConverter.resultSetToMachineDataSet(pstmt.executeQuery());
                    }
                }
                    return resultDataSet;
            } catch (SQLException | ClassNotFoundException ex) {
                throw new MachineDataException(ex);
            } 
            
        }         
        
        
        // Security checked
        public List<RoleModel> getAllRoleList() throws MachineDataException {
            
            if (!RoleModel.isNeedUpdate()) return RoleModel.getRolelist();
            
            RoleModel.reset();
            
            try {
                
                MachineDataSet resultDataSet;
                try (Connection c = connect()) {            
                    String sql = "select id,rolename,roledesc from " + schemaName + "npt_rolelist order by roledesc ASC";
                    try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                        resultDataSet = resultConverter.resultSetToMachineDataSet(pstmt.executeQuery());
                        while (resultDataSet.next()) {
                            RoleModel.addRole(new RoleModel(resultDataSet.getInteger("id"), resultDataSet.getString("rolename"), resultDataSet.getString("roledesc")));
                        }
                    }
                }
                    return RoleModel.getRolelist();
            } catch (SQLException | ClassNotFoundException ex) {
                throw new MachineDataException(ex);
            }
        }        
       
        // Security checked
        public MachineDataSet getRolesetsByOwner(int owneruserid) throws ClassNotFoundException, SQLException {
            
            MachineDataSet resultDataSet;
            try (Connection c = connect()) {
                String sql = "select setid,rolesetname,rolesetdesc from " + schemaName + "npt_rolesets where owneruserid = ? group by rolesetname,rolesetdesc,setid order by setid DESC";
                try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                    pstmt.setInt(1, owneruserid);
                    resultDataSet = resultConverter.resultSetToMachineDataSet(pstmt.executeQuery());
                }
            }
                    return resultDataSet;
        }    
        
        // Security checked
        public int getRolesetIdByName(String rolesetName) throws MachineDataException, ClassNotFoundException, SQLException  {
                    
            
            
            try {
                
                int rolesetIdFromDb;
                try (Connection c = connect()) {            
                    String sql = "select setid from " + schemaName + "npt_rolesets where rolesetname = ? group by setid";
                    try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                        pstmt.setString(1, rolesetName);
                        ResultSet resultSet = pstmt.executeQuery();
                        if (resultSet.next()) {
                            rolesetIdFromDb = resultSet.getInt("setid");
                        } else {
                            rolesetIdFromDb =  -1;
                        }
                    }
                }
                    return rolesetIdFromDb;
                    
            } catch (SQLException | ClassNotFoundException ex) {
                throw new MachineDataException(ex);
            }
            
        }    
                
        
        // Security checked
        public MachineDataSet getOneRoleSetRoleList(int owneruserid,int setid) throws MachineDataException {
            

            try {
                MachineDataSet resultDataSet;
                try (Connection c = connect()) {
                    String sql = "select rs.roleid as roleid,rl.rolename as rolename from " + schemaName + "npt_rolesets as rs," + schemaName + "npt_rolelist as rl where rs.owneruserid = ? and rs.setid = ? and rs.roleid > 1 and rl.id = rs.roleid order by rs.id";
                    try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                        pstmt.setInt(1, owneruserid);
                        pstmt.setInt(2, setid);
                        resultDataSet = resultConverter.resultSetToMachineDataSet(pstmt.executeQuery());
                    }
                }
                    return resultDataSet;
            } catch (ClassNotFoundException | SQLException ex) {
                throw new MachineDataException(ex);
            }
        }    
        
        // Security checked
        public MachineDataSet getOneRoleSetInfo(int owneruserid,int setid) throws MachineDataException {
            
            
            try {
                MachineDataSet resultDataSet;
                try (Connection c = connect()) {            
                    String sql = "select rolesetname,rolesetdesc from " + schemaName + "npt_rolesets where owneruserid = ? and setid = ? group by rolesetname,rolesetdesc";
                    try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                        pstmt.setInt(1, owneruserid);
                        pstmt.setInt(2, setid);
                        resultDataSet = resultConverter.resultSetToMachineDataSet(pstmt.executeQuery());
                    }
                }
                    return resultDataSet;
            } catch (SQLException | ClassNotFoundException ex) {
                throw new MachineDataException(ex);
            } 
        }    
        
        // Security checked
        public MachineDataSet getGroupInfo(int owneruserid,int groupid) throws MachineDataException {
            

            try {
                MachineDataSet resultDataSet;
                try (Connection c = connect()) {
                    String sql = "select groupname,groupdesc from " + schemaName + "npt_groups where owneruserid = ? and id = ?";
                    try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                        pstmt.setInt(1, owneruserid);
                        pstmt.setInt(2, groupid);
                        resultDataSet = resultConverter.resultSetToMachineDataSet(pstmt.executeQuery());
                    }
                }
                    return resultDataSet;
            } catch (SQLException | ClassNotFoundException ex) {
                throw new MachineDataException(ex);
            } 
        }          
    
        
        // Security checked
        public int getGroupUsersCount(int groupid,int owneruserid) throws MachineDataException {
            

            try {
                int userCountInGroup;
                    try (Connection c = connect()) {
                        String sql = "select count(id) as userscount from " + schemaName + "npt_users where groupid = (select id from " + schemaName + "npt_groups where owneruserid = ? and id = ?)";
                        try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                            pstmt.setInt(1, owneruserid);
                            pstmt.setInt(2, groupid);
                            try (ResultSet rs = pstmt.executeQuery()) {
                                if (rs.next()) {
                                    userCountInGroup = rs.getInt("npt_userscount");
                                } else {
                                    userCountInGroup = 0;
                                }
                            }
                        }
                    }
                    return userCountInGroup;
            } catch (ClassNotFoundException | SQLException ex) {
                throw new MachineDataException(ex);
            }
        }       
        
        // Security checked
        public MachineDataSet getUsersOfGroup(int groupid,int owneruserid) throws MachineDataException {
            

            try {
                MachineDataSet resultDataSet;
                try (Connection c = connect()) {
                    String sql = "select id,username,mdesc from " + schemaName + "npt_users where groupid = ?";
                    //pstmt.setInt(1, owneruserid);
                    try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                        //pstmt.setInt(1, owneruserid);
                        pstmt.setInt(1, groupid);
                        resultDataSet = resultConverter.resultSetToMachineDataSet(pstmt.executeQuery());
                    }
                }
                    return resultDataSet;
            } catch (ClassNotFoundException | SQLException ex) {
                throw new MachineDataException(ex);
            }
        }         
        
        // Security checked
        public int getRolesetUsersCount(int rolesetid,int owneruserid) throws MachineDataException {
            
            
            try {
                int usersCountUsingRoleset;
                    try (Connection c = connect()) {
                        String sql = "select count(id) as npt_userscount from " + schemaName + "npt_users where rolesetid = ? and groupid IN (select id from " + schemaName + "npt_groups where owneruserid = ?)";
                    try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                        pstmt.setInt(1, rolesetid);
                        pstmt.setInt(2, owneruserid);
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                usersCountUsingRoleset =  rs.getInt("npt_userscount");
                            } else {
                                usersCountUsingRoleset = 0;
                            }
                        }
                    }
                    }
                    return usersCountUsingRoleset;
            } catch (ClassNotFoundException | SQLException ex) {
                throw new MachineDataException(ex);
            }
        }            
        
        // Security checked
        public int getRoleIdByName(String rolename) throws MachineDataException {
            
            
            try {
                int roleId;
                    try (Connection c = connect()) {
                        String sql = "SELECT id FROM npt_rolelist WHERE rolename = ?";
                        try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                            pstmt.setString(1, rolename);
                            try (ResultSet rs = pstmt.executeQuery()) {
                                if (rs.next()) {
                                    roleId = rs.getInt("id");
                                } else {
                                    throw new SQLException("no role name = " + rolename);
                                }
                            }
                        }
                    }
                    return roleId;
            } catch (SQLException | ClassNotFoundException ex) {
                throw new MachineDataException(ex);                
            } 
        }      
        
        // Security checked
        public void updateUserSettings(int owneruserid,int userid,String columnName,Object rowValue) throws MachineDataException {
            
            try {
                try (Connection c = connect()) {
                    String sql = "UPDATE " + schemaName + "npt_users set "+ columnName +" = ? WHERE ID = ? and GROUPID IN (select id from npt_groups where owneruserid = ?)";
                    try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                        if (rowValue instanceof String) {
                            pstmt.setString(1, (String) rowValue);
                        } else if (rowValue instanceof Integer) {
                            pstmt.setInt(1, (int) rowValue);
                        } else if (rowValue instanceof Long) {
                            pstmt.setLong(1, (long) rowValue);
                        }
                        pstmt.setInt(2, userid);
                        pstmt.setInt(3, owneruserid);
                        pstmt.executeUpdate();
                    }
                }
            } catch (ClassNotFoundException | SQLException ex) {
                throw new MachineDataException(ex);
            }
        }   
        
        // Security checked
        public void updateUserPassword(int userid,String password) throws MachineDataException {
            
            try {
                try (Connection c = connect()) {
                    String sql = "UPSERT into " + schemaName + "npt_users (ID,PASSWORD) SELECT ID,? FROM " + schemaName + "npt_users WHERE ID = ?";
                    try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                        pstmt.setString(1, password);
                        pstmt.setInt(2, userid);
                        pstmt.executeUpdate();
                    }
                }
            } catch (SQLException | ClassNotFoundException ex) {
                throw new MachineDataException(ex);
            } 
        }          
        
        
        // Security checked
        public void updateGroupSettings(int owneruserid,int groupid,String columnName,String rowValue) throws MachineDataException, ClassNotFoundException, SQLException {
            
            nDataConnector.getTable("npt_groups")
                                    .update()
                                        .updateVarchar(columnName, rowValue)
                                    .where()
                                        .eq("id", groupid)
                                        .eq("owneruserid",owneruserid)
                                    .compile()
                                    .execute();

        }         
        
        
        
}
