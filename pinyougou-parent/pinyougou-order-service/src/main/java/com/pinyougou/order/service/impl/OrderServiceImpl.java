package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;
import com.sun.jdi.LongValue;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.order.service.OrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private TbPayLogMapper payLogMapper;
	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {

		//1.从redis中获取购物车
		List<Cart>cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		System.out.println("购物车"+cartList);
		List<String>orderIdList=new ArrayList();//订单id列表
		double total_money=0;//支付总金额

		//2.循环遍历购物车
		for (Cart cart : cartList) {
			long orderId = idWorker.nextId();//生成订单号
			System.out.println("购物车"+cart);

			TbOrder tbOrder=new TbOrder();
			tbOrder.setOrderId(orderId);//设置订单号
			tbOrder.setUserId(order.getUserId());//设置用户名
			tbOrder.setPaymentType(order.getPaymentType());//设置支付类型
			tbOrder.setStatus(order.getStatus());//设置支付状态
			tbOrder.setCreateTime(new Date());//设置下单时间
			tbOrder.setUpdateTime(new Date());//设置更新时间
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());//设置地址
			tbOrder.setReceiverMobile(order.getReceiverMobile());//设置联系人
			tbOrder.setReceiver(order.getReceiver());//收货人
			tbOrder.setSourceType(order.getSourceType());//订单来源
			tbOrder.setSellerId(order.getSellerId());//商家

			double money=0;//合计数
			//循环购物车明细记录
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				orderItem.setId(idWorker.nextId());//主键
				orderItem.setOrderId(orderId);//订单号
				orderItem.setSellerId(cart.getSellerId());//商家id
				orderItemMapper.insert(orderItem);
				money+=orderItem.getTotalFee().doubleValue();
			}

			tbOrder.setPayment(new BigDecimal(money));//合计
			orderMapper.insert(tbOrder);
			orderIdList.add(orderId+"");//添加到订单id列表
			total_money+=money;//支付总金额
		}

		//添加支付日志
		if (order.getPaymentType().equals("1")){//如果是微信支付
			TbPayLog payLog=new TbPayLog();
			payLog.setOutTradeNo(idWorker.nextId()+"");//支付订单号
			payLog.setCreateTime(new Date());//创建日期
			payLog.setUserId(order.getUserId());//用户id
			payLog.setOrderList(orderIdList.toString().replace("[","").replace("]",""));//订单id串
			payLog.setTotalFee((long)(total_money*100));//设置订单总金额（分）
			payLog.setTradeState("0");//设置交易状态
			payLog.setPayType("1");//微信支付
			payLogMapper.insert(payLog);
			//将支付日志存入缓存,用户id做key,支付日志对象做值
			redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);

		}
		//3.清除redis中的购物车
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 根据用户名从redis获取支付日志
	 * @param userId
	 * @return
	 */

	public TbPayLog searchPayLogFromRedis(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);

	}

	/**
	 *  支付成功修改订单支付状态
	 * @param out_trade_n
	 * @param transaction_id
	 */
    @Override
	public void updateOrderStatus(String out_trade_n, String transaction_id) {
		//修改支付日志状态
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_n);
		payLog.setTradeState("1");//已支付
		payLog.setPayTime(new Date());//支付时间
		payLog.setTransactionId(transaction_id);//交易号
		payLogMapper.updateByPrimaryKey(payLog);//更新支付日志状态

		//修改订单支付状态
		String orderList = payLog.getOrderList();
		String[] orderIds = orderList.split(",");//订单数组
		for (String orderId : orderIds) {
			TbOrder order = orderMapper.selectByPrimaryKey(Long.valueOf(orderId));
			order.setStatus("2");//已支付状态
			order.setPaymentTime(new Date());//支付时间
			orderMapper.updateByPrimaryKey(order);
		}

		//删除redis缓存中得数据
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());

	}

}
