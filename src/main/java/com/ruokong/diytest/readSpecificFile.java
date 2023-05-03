package com.ruokong.diytest;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**测试方法
 * @author ruo
 * @version 1.0
 * @date 2023/5/2
 */
public class readSpecificFile {
    public static void main(String[] args) {
        String directoryPath = "E:\\desktop\\pro\\src\\main\\java\\org\\example\\controller"; // 指定目录的路径

        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            System.err.println("指定路径不是一个目录");
            return;
        }

        File[] files = directory.listFiles(); // 获取目录中的所有文件
        if (files == null || files.length == 0) {
            System.out.println("目录中没有文件");
            return;
        }

        List<String> list = new ArrayList<>();
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
                        String ans=requestParameter+mappingValue;
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
    }
}

