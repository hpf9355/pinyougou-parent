package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

/**
 * 品牌接口
 * @author 胡鹏飞
 *
 */
public interface BrandService {

	public List<TbBrand>findAll();
	
	/**
	 * 品牌分页
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	/**
	 * 品牌增加
	 */
	public void add(TbBrand brand);
	
	/**
	 * 品牌修改
	 */
	//根据id查询实体
	public TbBrand findOne(long id);
	
	//将修改后的品牌保存
	public void update(TbBrand brand);
	
	/**
	 * 品牌批量删除
	 */
	public void delete(Long[] ids);
	
	/**
	 * 条件查询
	 */
	public PageResult findPage(TbBrand brand,int pageNum,int pageSize);

	/**
	 * 品牌下拉列表
	 */
	public List<Map> selectOptionList();
}
