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

import org.acceix.frontend.web.commons.SecurityException;

/**
 *
 * @author zrid
 */
public class SharedFunctions {
    
        private static final char[] CRYPTKEY = {'b','4','c','5','d','6','w','y','h','k'};
        
        
        // AGENT UID GENERATION LIMITS
        private static final int LENGTH_USERID = 8;
        private static final int LENGTH_UID = 4;          


        public static int getLENGTH_UID() {
            return LENGTH_UID;
        }

        public static int getLENGTH_USERID() {
            return LENGTH_USERID;
        }        

        
        public static char[] getCRYPTKEY() {
            return CRYPTKEY;
        }
        

        public static String cryptString(String str) {

                char[] charStr = str.toCharArray();
                char[] newCharStr = new char[charStr.length];
                
                for (int i=0; i < charStr.length; i++) {
                    if (Character.isDigit(charStr[i])) {
                        newCharStr[i] = getCRYPTKEY()[Character.getNumericValue(charStr[i])];
                    } else {
                        newCharStr[i] = charStr[i];
                    }
                }
                
                return new String(newCharStr);
                
        }
        
        public static String decryptString(String str) {

               char[] charStr = str.toCharArray();
               char[] newCharStr = new char[charStr.length];
                
               for (int i=0; i < charStr.length; i++) {
                    for (int x=0; x < 10; x++) {
                        if (getCRYPTKEY()[x] == charStr[i]) {
                            newCharStr[i] = (char)('0' + x);
                        }
                    }
               }

               return new String(newCharStr);       
                
        }        
        
        public static String generateCryptedUID (String userid,String lastuid) throws SecurityException {
            
            
                int lengthUserId = getLENGTH_USERID();
                int lengthUid = getLENGTH_UID();
            
                String useridCrypted = cryptString(userid);
                String uidCrypted = cryptString(lastuid);
                
                if (useridCrypted.length() > lengthUserId) {
                    throw new SecurityException("userid length can not be more than " + lengthUserId);
                } else if (useridCrypted.length() < lengthUserId) {
                    useridCrypted = "a" + useridCrypted;
                }
                
                if (uidCrypted.length() > lengthUid) {
                    throw new SecurityException("uid length can not be more than " + lengthUid);
                } else if (uidCrypted.length() < lengthUid) {
                    uidCrypted = "a" + uidCrypted;                    
                }

                
                int x=0,z=0;
                
                while (useridCrypted.length() < lengthUserId) {
                    useridCrypted = getCRYPTKEY()[x] + useridCrypted;
                    x++;
                }

                while (uidCrypted.length() < lengthUid) {
                    uidCrypted = getCRYPTKEY()[z] + uidCrypted;
                    z++;
                }
                
                return useridCrypted + "-" + uidCrypted;
                
        }
        
        public static String decryptUID_ID(String uid) throws SecurityException {
            
            int indexOfFirstSeparator = uid.indexOf('-');
            if (indexOfFirstSeparator < 0) {
                throw new SecurityException("UID format unrecognised");
            }
            String uidPartID = uid.substring(indexOfFirstSeparator+1, uid.length());
            
            int indexOfSecondSeparator = uidPartID.indexOf('a');
            
            if (indexOfSecondSeparator < 0) {
                throw new SecurityException("UID format unrecognised");
            }            
            
            String id = uidPartID.substring(indexOfSecondSeparator+1, uidPartID.length());            
            
            return decryptString(id);

        }
        
        public static String decryptUID_USERID(String uid) throws SecurityException {
            
            int indexOfFistSeparator = uid.indexOf('-');
            
            if (indexOfFistSeparator < 0) {
                throw new SecurityException("UID format unrecognised");
            }
            
            String uidPartUSERID = uid.substring(0,indexOfFistSeparator);
            
            int indexOfSecondSeparator = uidPartUSERID.indexOf('a');
            
            if (indexOfSecondSeparator < 0) {
                throw new SecurityException("UID format unrecognised");
            }            
            
            String userId = uidPartUSERID.substring(indexOfSecondSeparator+1, uidPartUSERID.length());  

            
            return decryptString(userId);

        }
        
        public static String toTitleCase(String givenString) {
            String[] arr = givenString.split(" ");
            StringBuilder sb = new StringBuilder();

            for (String arr1 : arr) {
                sb.append(Character.toUpperCase(arr1.charAt(0))).append(arr1.substring(1)).append(" ");
            }          
            return sb.toString().trim();
        }          
        
        


}
