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

package org.acceix.frontend.models;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author zrid
 */
public class RoleModel {
    
    
    private static List<RoleModel> rolelist = new LinkedList<>();
        
    private static int updateAfterRequestCount=1;
    
    private static int currentRequestCount=-1;

    public static List<RoleModel> getRolelist() {
        return rolelist;
    }
    
    public static void addRole(RoleModel roleModel) {
        rolelist.add(roleModel);
    }
    
    public static void reset() {
        rolelist = new LinkedList<>();
    }
    
    int id;
    String rolename;
    String roledesc;

    public RoleModel(int id, String rolename, String roledesc) {
        this.id = id;
        this.rolename = rolename;
        this.roledesc = roledesc;
    }


    public int getId() {
        return id;
    }

    public String getRoledesc() {
        return roledesc;
    }

    public String getRolename() {
        return rolename;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRoledesc(String roledesc) {
        this.roledesc = roledesc;
    }

    public void setRolename(String rolename) {
        this.rolename = rolename;
    }

    public static boolean isNeedUpdate() {
        
        if (currentRequestCount==-1) {
            currentRequestCount = 0;
            return true;
        } else {
        
            if (currentRequestCount >= updateAfterRequestCount) {
                currentRequestCount=0;
                return true;
            } else {
                currentRequestCount++;
                return false;
            }
            
        }
    }


    
    
    
    
}
