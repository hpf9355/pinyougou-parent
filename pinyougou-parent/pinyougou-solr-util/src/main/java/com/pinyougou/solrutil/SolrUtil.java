package com.pinyougou.solrutil;


import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;

import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    //查询需要导入的数据
    public void importItemData(){

        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//审核通过的商品才能被搜索
        //返回商品列表数据
        List<TbItem> itemList = itemMapper.selectByExample(example);

        for (TbItem item : itemList) {
            //从数据库中获取规格列表的json字符串转为map
            Map map = JSON.parseObject(item.getSpec(), Map.class);
            item.setSpecMap(map);
        }


        //加入索引库

        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();

    }

    //删除solr索引库全部索引
    public void deleteAll(){
        Query query=new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }



    public static void main(String[] args) {

        ApplicationContext context=new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");

        solrUtil.importItemData();

       // solrUtil.deleteAll();


    }
}
