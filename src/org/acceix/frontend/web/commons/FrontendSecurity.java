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

package org.acceix.frontend.web.commons;

import org.acceix.frontend.database.AdminFunctions;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.acceix.ndatabaseclient.MachineDataException;
import org.acceix.ndatabaseclient.MachineDataSet;

/**
 *
 * @author Rza Asadov <rza.asadov at gmail.com>
 */
public class FrontendSecurity {
    
    
        private final Map<String,Object> ENVS;

        public FrontendSecurity(Map<String, Object> envs) {
            this.ENVS = envs;
        }

        
    
        public String getTokenByUserId(int user_id) throws MachineDataException {
            
                if (user_id==0) return "ADMINTOKENPROHIBITED";
            
                MachineDataSet machineDataSet =  new AdminFunctions(ENVS,"system").getUserInfo(user_id);
                
                if (machineDataSet.next()) {
                
                    String password = machineDataSet.getFirstString("password");
                    int domain_id = machineDataSet.getFirstInt("domain_id");

                    return generateToken(user_id, password, domain_id);
                
                } else {
                    return null;
                }
            
        }    
    
        public String generateToken (int userid,String password,int domain_id) {
            

                                String token;
                                
                                String tokenOfUserId =  Base64.getEncoder().encodeToString(String.valueOf(userid).getBytes());

                                    try {
                                        
                                            MessageDigest m = MessageDigest.getInstance("MD5");
                                            m.update(password.getBytes(),0,password.length());

                                            String tokenOfPassword = Base64.getEncoder().encodeToString(new BigInteger(1,m.digest()).toString(16).getBytes());

                                            token = tokenOfUserId + ":" + tokenOfPassword;


                                            return token;
                                        
                                        
                                    } catch (NoSuchAlgorithmException ex) {
                                        Logger.getLogger(FrontendSecurity.class.getName()).log(Level.SEVERE, null, ex);                                        
                                        return null;
                                    }
                             
       
        }    

    public static void securityCaseHandler(HttpServletRequest request,HttpServletResponse response) {
            //request.getSession().setAttribute("authenticated",false); 
            System.err.println("Frontend Security: error on URL '" +  request.getRequestURI() + "'");
            
            Enumeration reuquestVars = request.getParameterNames();
            
            while (reuquestVars.hasMoreElements()) {
                String key = reuquestVars.nextElement().toString() ;
                System.err.println("Key = " + key + " , Value = " + request.getParameter(key));
            }
         
            
            
            try {
                response.setContentType("text/html;charset=UTF-8"); 
                response.getWriter().println("<h3 style=\"color: red;\">Security violation/Attack detected !!!</h3>");
                response.getWriter().flush();
                response.getWriter().close();
                for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
                    System.err.println(ste);
                }                        
            } catch (IOException ex) {
                Logger.getLogger(FrontendSecurity.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    public static boolean isRoleEnabled(List<String> roles,String rolename) {
        
        
      if (rolename.charAt(rolename.length()-1)=='*') {
            return roles.stream().anyMatch(role -> (role.startsWith(rolename.substring(0, rolename.length()-1))));          
        } else {
            return roles.stream().anyMatch(role -> (role.equals(rolename)));
        }
    }      
    
    
}
