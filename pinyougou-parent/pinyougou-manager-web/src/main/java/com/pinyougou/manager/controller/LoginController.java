package com.pinyougou.manager.controller;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/login")
public class LoginController {

    //获取当前登录用户名
    @RequestMapping("/name")
    public Map name(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
       //System.out.println("name = " + name);
        Map map=new HashMap();
        map.put("loginName",name);
        return map;
    }
}
