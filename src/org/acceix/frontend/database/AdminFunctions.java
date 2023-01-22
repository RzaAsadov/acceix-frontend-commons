/*
 * The MIT License
 *
 * Copyright 2022 Rza Asadov (rza at asadov dot me).
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
import org.acceix.ndatabaseclient.exceptions.MachineDataException;
import org.acceix.ndatabaseclient.dataset.MachineDataSet;
import org.acceix.ndatabaseclient.mysql.DataConnector;
import org.acceix.ndatabaseclient.mysql.ResultSetConverter;


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
        
        public MachineDataSet getUserInfo(int userid) throws MachineDataException {
            
            
            try {
                MachineDataSet resultDataSet;
                try (Connection c = connect()) {            
                    String sql = "select u.username as username,u.mdesc as mdesc,u.groupid as groupid,u.rolesetid as rolesetid,u.password as password,u.domain_id as domain_id from " + schemaName + "npt_users as u," + schemaName + "npt_groups as g where u.id = ? and u.groupid = g.id";
                    try (PreparedStatement pstmt = getPreparedStatement(c, sql)) {
                        pstmt.setInt(1, userid);
                        resultDataSet =  new ResultSetConverter().resultSetToMachineDataSet(pstmt.executeQuery());
                    }
                }
                    return resultDataSet;                    
            } catch (SQLException | ClassNotFoundException ex) {
                throw new MachineDataException(ex);
            } 
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
        public void executeStatement(String sqlStatement) throws MachineDataException, ClassNotFoundException, SQLException {

            try (Connection c = connect(); Statement stmt = getStatement(c)) {
                stmt.execute(sqlStatement);
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
  
        
        
        
}
