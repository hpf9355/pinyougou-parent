package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;

import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 秒杀下单
	 * @param seckillId 商品id
	 * @param userId 用户id
	 */
	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Override
	public void submitOrder(Long seckillId, String userId) {

		//从redis中获取秒杀商品数据
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);

		if (seckillGoods==null){
			throw new RuntimeException("该商品不存在");
		}
		if (seckillGoods.getStockCount()<=0){
			throw new RuntimeException("该商品已售完");
		}

		//减少库存
		seckillGoods.setStockCount(seckillGoods.getStockCount()-1);//库存减1
		redisTemplate.boundHashOps("seckillGoods").put(seckillId,seckillGoods);//更新缓存中的商品库存

		//在redis中保存订单
		TbSeckillOrder seckillOrder = new TbSeckillOrder();
		seckillOrder.setId(idWorker.nextId());//订单号
		seckillOrder.setSeckillId(seckillId);//秒杀商品id
		seckillOrder.setUserId(userId);//登录用户id
		seckillOrder.setMoney(seckillGoods.getCostPrice());//商品秒杀价
		seckillOrder.setCreateTime(new Date());//下单时间
		seckillOrder.setStatus("0");//支付状态
		seckillOrder.setSellerId(seckillGoods.getSellerId());//商家id

		redisTemplate.boundHashOps("seckillOrder").put(userId,seckillOrder);

	}

	/**
	 * 根据用户名从redis中查询订单
	 * @param userId
	 * @return
	 */
	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {

		return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
	}



    /**
     * 支付成功，将缓存中的订单存入数据库
     * @param userId
     * @param orderId
     * @param transactionId
     */
    @Override
    public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {

	    //从redis中查询订单
        TbSeckillOrder seckillOrder = searchOrderFromRedisByUserId(userId);
        if (seckillOrder==null){
            throw new RuntimeException("该订单不存在");
        }

        if (seckillOrder.getId().longValue()!=orderId.longValue()){
            throw new RuntimeException("订单号不符");
        }

        //订单存入数据库
        seckillOrder.setPayTime(new Date());//支付时间
        seckillOrder.setStatus("1");//支付状态
        seckillOrder.setTransactionId(transactionId);//微信返回的流水号
        seckillOrderMapper.insert(seckillOrder);

        //清除redis中的订单
        redisTemplate.boundHashOps("seckillOrder").delete(userId);

    }

	@Override
	public void deleteOrderFromRedis(String userId,Long orderId) {
		//从redis缓存中获取订单
		TbSeckillOrder seckillOrder = searchOrderFromRedisByUserId(userId);
		if (seckillOrder!=null){
			//清除缓存中的订单
			redisTemplate.boundHashOps("seckillOrder").delete(userId);
			//恢复库存
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
			if (seckillGoods!=null){
				seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
				redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(),seckillGoods);
			}

		}
		System.out.println("订单取消"+orderId);
	}

}
