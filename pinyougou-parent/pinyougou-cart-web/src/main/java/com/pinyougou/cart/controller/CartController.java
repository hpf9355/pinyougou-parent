package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Reference
    private CartService cartService;

    /**
     * 获取购物车列表
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        //获取当前登陆人
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //从cookie中获取购物车
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString==null||cartListString.equals("")){
            cartListString="[]";
        }
        //转为集合
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);

        if (username.equals("anonymousUser")){//未登录

            return cartList_cookie;
        }else {//已登陆

            //从redis中获取购物车
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);

            if (cartList_cookie.size()>0){//本地购物车有数据
                //合并购物车
                List<Cart> cartList = cartService.mergeCartList(cartList_cookie, cartList_redis);
                //将合并后的购物车存入redis
                cartService.saveCartListToRedis(username,cartList);
                //清空本地购物车
                CookieUtil.deleteCookie(request,response,"cartList");
                return cartList;
            }

            return cartList_redis;
        }

    }

    /**
     * 添加商品到购物车
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    //@CrossOrigin(origins = "http://localhost:9105",allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId,Integer num){
        //获取当前登陆人
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");//可以访问的域(不操作cookie)
        response.setHeader("Access-Control-Allow-Credentials", "true");//操作cookie需添加
        try {

            //从cookie中获取购物车
            List<Cart> cartList = findCartList();
            //调用服务方法操作购物车
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);

            if (name.equals("anonymousUser")){//未登录

                String cartListString = JSON.toJSONString(cartList);
                //将新的购物车存入cookie
                CookieUtil.setCookie(request,response,"cartList",cartListString,3600*24,"utf-8");
            }else {//已登录

                cartService.saveCartListToRedis(name,cartList);
            }

            return  new Result(true,"存入购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false,"存入购物车失败");
        }

    }


}
