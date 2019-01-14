package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;

import util.HttpClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;
    /**
     * 生成二维码
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //1.创建参数
        Map param=new HashMap();
        param.put("appid",appid);//公众号id
        param.put("mch_id",partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body","品优购");
        param.put("out_trade_no",out_trade_no);//商户订单号
        param.put("total_fee",total_fee);//金额
        param.put("spbill_create_ip","127.0.0.1");//终端ip
        param.put("notify_url","http://test.itcast.cn");//回调地址
        param.put("trade_type","NATIVE");//交易类型


        try {
            //2.生成要发送得xml
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);

            HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);//https请求
            httpClient.setXmlParam(paramXml);
            httpClient.post();

            //3.获取结果
            String xmlResult = httpClient.getContent();

            Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);

            Map map=new HashMap();
            map.put("code_url",mapResult.get("code_url"));//二维码链接
            map.put("out_trade_no",out_trade_no);//商户订单号
            map.put("total_fee",total_fee);//金额

            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }

    }

    /**
     * 查询订单状态
     * @param out_trade_no
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {

       //1.封装参数
        Map param=new HashMap();
        param.put("appid",appid);//公众号id
        param.put("mch_id",partner);//商户号
        param.put("out_trade_no",out_trade_no);//商户订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串

        //2.生成要发送得xml
        try {
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);

            HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);//https请求
            httpClient.setXmlParam(paramXml);
            httpClient.post();
        //3.获取结果
            String xmlResult = httpClient.getContent();

            Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);

            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }



    }

    @Override
    public Map closePay(String out_trade_no) {
        //1.封装参数
        Map param=new HashMap();
        param.put("appid",appid);//公众号id
        param.put("mch_id",partner);//商户号
        param.put("out_trade_no",out_trade_no);//商户订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串

        //2.生成要发送得xml
        try {
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);

            HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setHttps(true);//https请求
            httpClient.setXmlParam(paramXml);
            httpClient.post();
            //3.获取结果
            String xmlResult = httpClient.getContent();

            Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);

            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
