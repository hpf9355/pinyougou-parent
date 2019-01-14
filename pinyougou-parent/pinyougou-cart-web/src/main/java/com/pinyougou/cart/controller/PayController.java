package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference(timeout = 50000)
    private WeixinPayService weixinPayService;

    @Reference
    private OrderService orderService;

    /**
     * 生成二维码
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative(){

        //获取当前登录名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //根据用户名从redis中获取支付日志
        TbPayLog payLog = orderService.searchPayLogFromRedis(username);
        //调用微信支付接口
        if (payLog!=null){
            return weixinPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        }else {
            return new HashMap();
        }


    }


    /**
     * 查询订单状态
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        System.out.println(out_trade_no);
        Result result=null;

        int x=0;
        while (true){
            Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
            //返回结果为空
            if (map==null){
                result=new Result(false,"支付发生错误");
                break;
            }
            System.out.println("支付状态"+map.get("trade_state"));
            if (map.get("trade_state").equals("SUCCESS")){
                result=new Result(true,"支付成功");
                //修改订单状态
                orderService.updateOrderStatus(out_trade_no, (String) map.get("transaction_id"));
                break;
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            x++;
            if (x>=100){
                result=new Result(false,"二维码超时");
                break;
            }
        }

        return result;
    }

}
