package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.pinyougou.pay.service.WeixinPayService;

import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference(timeout = 50000)
    private WeixinPayService weixinPayService;

    @Reference
    private SeckillOrderService seckillOrderService;

    /**
     * 生成二维码
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative(){

        //获取当前登录名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //根据用户名从redis中获取支付日志
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(username);

        //调用微信支付接口
        if (seckillOrder!=null){
            return weixinPayService.createNative(seckillOrder.getId()+"",(long)(seckillOrder.getMoney().doubleValue()*100)+"");
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
        //获取当前登录名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Result result=null;

        int x=0;
        while (true){
            Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
            //返回结果为空
            if (map==null){
                result=new Result(false,"支付发生错误");
                break;
            }

            if (map.get("trade_state").equals("SUCCESS")){
                result=new Result(true,"支付成功");
                //订单存入数据库
                seckillOrderService.saveOrderFromRedisToDb(username,Long.valueOf(out_trade_no),map.get("tranaction_id"));
                break;
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            x++;
            if (x>=4){
                result=new Result(false,"二维码超时");
                //二维码超时关闭支付
                Map<String,String> payResult = weixinPayService.closePay(out_trade_no);
                //判断在支付关闭瞬间支付成功的情况
                if (!"SUCCESS".equals(payResult.get("result_code"))){//返回结果为正常关闭
                    if ("ORDERPAID".equals(payResult.get("err_code"))){
                        result=new Result(true,"支付成功");
                        //订单存入数据库
                        seckillOrderService.saveOrderFromRedisToDb(username,Long.valueOf(out_trade_no),map.get("tranaction_id"));

                    }

                }
                if (result.isSuccess()==false){//超时且未支付
                    //删除订单
                    seckillOrderService.deleteOrderFromRedis(username,Long.valueOf(out_trade_no));

                }
                break;

            }
        }

        return result;
    }

}
