package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 添加商品到购物车
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {

        //1.根据商品SKU ID查询SKU商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item==null){
            throw  new RuntimeException("该商品不存在");
        }
        if (!item.getStatus().equals("1")){
            throw  new RuntimeException("该商品不合法");
        }
        //2.获取商家ID
        String sellerId = item.getSellerId();

        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);

        if (cart==null){//4.如果购物车列表中不存在该商家的购物车
            //4.1 新建购物车对象
            cart=new Cart();
            cart.setSellerId(item.getSellerId());//设置商家id
            cart.setSellerName(item.getSeller());//设置商家名称

            //新建购物车明细列表
            List<TbOrderItem>orderItemList=new ArrayList<>();
            //新建购物车明细对象
            TbOrderItem orderItem = createOrderItem(item, num);
            //购物车明细对象放入购物车明细列表
            orderItemList.add(orderItem);
            //购物车明细列表放入购物车对象
            cart.setOrderItemList(orderItemList);
            //4.2 将新建的购物车对象添加到购物车列表
            cartList.add(cart);

        }else { //5.如果购物车列表中存在该商家的购物车
            // 查询购物车明细列表中是否存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);

            //5.1. 如果没有，新增购物车明细
            if (orderItem==null){
                 orderItem = createOrderItem(item, num);
                 cart.getOrderItemList().add(orderItem);
            }else {
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));

                //当明细数量小于等于0时  在购物车明细列表中移除该明细对象
                if (orderItem.getNum()<=0){
                    cart.getOrderItemList().remove(orderItem);
                }

                //当购物车明细列表等于0时  在购物车列表移除该购物车对象
                if (cart.getOrderItemList().size()==0){
                    cartList.remove(cart);
                }
            }


        }

        return cartList;
    }

    /**
     * 根据商家id查找该商家的购物车对象
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){

        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }

    /**
     * 新建购物车明细对象
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item,Integer num){
        if (num<=0){
            throw  new RuntimeException("数量非法");
        }

        TbOrderItem orderItem=new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));

        return orderItem;
    }

    /**
     * 根据itemId查询购物车明细列表中是否存在该商品
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){

        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }



    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 从redis中获取购物车
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {

        List<Cart>  cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList==null){
            cartList=new ArrayList<>();
        }
        return cartList;
    }

    /**
     * 向redis中存入购物车
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {

        redisTemplate.boundHashOps("cartList").put(username,cartList);

    }

    /**
     * 合并购物车
     * @param cartList1
     * @param cartList2
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                 cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }

}
