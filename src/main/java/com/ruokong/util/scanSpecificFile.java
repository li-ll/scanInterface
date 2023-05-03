package com.ruokong.util;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ruo
 * @version 1.0
 * @date 2023/5/3
 */
public   class scanSpecificFile {
    public static RestResult readSpecificFile(String path){
        RestResult result=new RestResult();
        List<String>list=new ArrayList<>();
        File directory = new File(path);
        if (!directory.isDirectory()) {
            result.setCode(200);
            result.setMessage("指定路径不是一个目录");
            return result;
        }

        File[] files = directory.listFiles(); // 获取目录中的所有文件
        if (files == null || files.length == 0) {
            result.setCode(200);
            result.setMessage("目录中没有文件");
            return result;
        }
        for (File file : files) {
            if (file.isFile()) {
                System.out.println(file.getName()); // 输出文件名
                // 在这里可以使用FileInputStream等类读取文件内容
                Pattern requestMappingPattern = Pattern.compile("@RequestMapping\\(\"([^\"]+)\""); // 匹配@RequestMapping注解的正则表达式模式
                Pattern mappingPattern = Pattern.compile("@(PostMapping|PutMapping|DeleteMapping|GetMapping)\\(\"([^\"]*)\"\\)");

                // 正则表达式匹配模式
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    StringBuilder contentBuilder = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        contentBuilder.append(line).append(System.lineSeparator());
                    }
                    String content = contentBuilder.toString();
                    // 匹配@RequestMapping注解
                    Matcher requestMappingMatcher = requestMappingPattern.matcher(content);
                    String requestParameter="";
                    while (requestMappingMatcher.find()) {
                        requestParameter = requestMappingMatcher.group(1); // 获取注解参数
                    }
                    Matcher mappingMatcher = mappingPattern.matcher(content);
                    while (mappingMatcher.find()) {
                        String mappingType = mappingMatcher.group(1);
                        String mappingValue = mappingMatcher.group(2);
                        //String ans=requestParameter+mappingValue;
                        String ans=joinPath(requestParameter,mappingValue);
                        if(StringUtils.equals(mappingType,"PostMapping")){
                            list.add("post "+ans);
                        }else if(StringUtils.equals(mappingType,"PutMapping")){
                            list.add("put "+ans);
                        }else if(StringUtils.equals(mappingType,"DeleteMapping")){
                            list.add("delete "+ans);
                        }else if(StringUtils.equals(mappingType,"GetMapping")){
                            list.add("get "+ans);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for (String s:list) {
            System.out.println(s);
        }
        result.setCode(200);
        if (list.size()>0){
            result.setMessage("成功扫描到数据");
        }else {
            result.setMessage("该目录下没有扫描到数据");
        }
        result.setData(list);
        return result;

    }
    public static String joinPath(String firstPath, String secondPath) {
        if (secondPath.charAt(0) != '/') {
            firstPath = firstPath.endsWith("/") ? firstPath : firstPath + "/";
        }
        return firstPath + secondPath;
    }

}
