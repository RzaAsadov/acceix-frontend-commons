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

package org.acceix.frontend.web.commons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 *
 * @author zrid
 */
public class DataUtils {
    
    public String mapToJsonString(Object model) throws IOException {

        return new Gson().toJson(model); 
        
    }   
    
    public String listToJsonString(Object model) throws IOException {

        return new Gson().toJson(model); 
        
    }       


    public JSONArray mapToJsonArray(Map<String, Object> model) {
        JSONArray jSONArray = new JSONArray();
        Collection<Object> collection = model.values();
        jSONArray.addAll(collection);
        return jSONArray;
    }
    
    public JSONArray listToJsonArray(List<Object> model) {
        JSONArray jSONArray = new JSONArray();
        jSONArray.addAll(model);
        return jSONArray;
    }    
    
    
    
    Map<String,Object> errorsList = new HashMap<>();
    
    
    

    public Map<String,Object> jsonFormDataToMap(List jsonArray,String keyName,String keyValue) {

                            final Map<String,Object> resultMap = new LinkedHashMap<>();
                            

                            jsonArray.forEach((Object t) -> {

                                Map jSONObjectInArray = (Map)t;
                                
                                String name = (String)jSONObjectInArray.get(keyName);
                                Object value = jSONObjectInArray.get(keyValue);
                                resultMap.put(name, value);
                                
                            });    

                            return resultMap;

    }
    
    public String readRequestBody (InputStream in)  {
        
                        BufferedReader bufr = new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));
                
                        String jsonSTR = "";
                        String inbuffer;
                            try {
                                while (( inbuffer = bufr.readLine()) != null) {
                                    jsonSTR = jsonSTR + inbuffer;
                                }
                            } catch (IOException ex) {
                                return null;
                            }
                        
                        if (jsonSTR.length()==0) { 
                            return null;
                        } else {
                            return jsonSTR;
                        }
                        
                       
                        //return readJsonArrayFromString(jsonSTR);

    }

       

    
    public Map readJsonObjectFromStream (InputStream in) throws IOException, ParseException {
        
                        BufferedReader bufr = new BufferedReader(new InputStreamReader(in));
                
                        String jsonSTR = "";
                        String inbuffer;
                        while (( inbuffer = bufr.readLine()) != null) {
                            jsonSTR = jsonSTR + inbuffer;
                        }
                        
                        if (jsonSTR.length()==0) return null;
                       
                        

                        return readJsonObjectFromString(jsonSTR);

    }  
    
    public Map readJsonObjectFromString (String in) throws IOException, ParseException {
        
        
                        JSONParser jSONParser = new JSONParser();
                        
                        ContainerFactory containerFactory = new ContainerFactory() {
                                 @Override
                                 public Map createObjectContainer() {
                                    return new LinkedHashMap<>();
                                 }
                                 @Override
                                 public List creatArrayContainer() {
                                    return new LinkedList<>();
                                 }
                        };
                        
                        
                        return (Map) jSONParser.parse(in,containerFactory);

    } 

    public Object readJsonArrayFromString (String in) throws IOException, ParseException {
        
                        JSONParser jSONParser = new JSONParser();
                        
                        ContainerFactory containerFactory = new ContainerFactory() {
                                 @Override
                                 public Map createObjectContainer() {
                                    return new LinkedHashMap<>();
                                 }
                                 @Override
                                 public List creatArrayContainer() {
                                    return new LinkedList<>();
                                 }
                        };
                        

                         return jSONParser.parse(in,containerFactory);
                        
    }     
    
    public String beautyfyJson (String jsonStr) {
              JsonParser parser = new JsonParser();
              JsonElement jElem = parser.parse(jsonStr);
              
              Gson gson = new GsonBuilder().setPrettyPrinting().create();
              
              if (jElem.isJsonArray()) {
                JsonArray json = parser.parse(jsonStr).getAsJsonArray();  
                return gson.toJson(json);
              } else if (jElem.isJsonObject()) {
                JsonObject json = parser.parse(jsonStr).getAsJsonObject();
                return gson.toJson(json); 
              }

              return null;
                     
    }
    
 
    

    public Map<String, Object> getErrorsList() {
        return errorsList;
    }
    
    
    
    
}
