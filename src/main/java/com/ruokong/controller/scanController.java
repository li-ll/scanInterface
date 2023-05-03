package com.ruokong.controller;

import com.ruokong.util.RestResult;
import com.ruokong.util.scanSpecificFile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author ruo
 * @version 1.0
 * @date 2023/5/2
 */
@RestController
@RequestMapping("/scan")
public class scanController {

    @PostMapping("/filepath")
    public RestResult scan(@RequestBody Map<String,String> map){
        System.out.println(map.get("path"));
        RestResult result= scanSpecificFile.readSpecificFile(map.get("path"));
        return result;
    }


}
