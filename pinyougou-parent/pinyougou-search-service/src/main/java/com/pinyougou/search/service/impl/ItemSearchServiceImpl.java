package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service(timeout = 50000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * 搜索方法
     * @param searchMap
     * @return
     */
    @Override
    public Map search(Map searchMap) {
        Map map=new HashMap();

        //关键字搜索空格处理
        String keywords = (String) searchMap.get("keywords");//获取搜索关键字
        searchMap.put("keywords",keywords.replace(" ",""));//关键字去空格

        //1.查询列表(高亮显示)
        map.putAll(searchList(searchMap));

        //2.分组查询商品分类列表
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);


        //3.根据商品分类名称获取品牌列表,规格列表
            //获取品牌分类名称
        String category = (String) searchMap.get("category");
        if (!category.equals("")){
            map.putAll(searchBrandAndSpecList(category));

        }else {
            if (categoryList.size()>0){

                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }


        return map;


    }



    /**
     * 查询列表(高亮显示)
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap) {
        Map map=new HashMap();
        //高亮显示
        HighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");//在对象标题设置高亮
        //设置前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //设置后缀
        highlightOptions.setSimplePostfix("</em>");
        query.setHighlightOptions(highlightOptions);//为查询对象设置高亮选项


        //1.1关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2按商品分类过滤
        if (!"".equals(searchMap.get("category"))){//如果用户选择了分类
            FilterQuery filterQuery=new SimpleFilterQuery();
            Criteria filterCriteria=new Criteria("item_keywords").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);

        }

        //1.3按品牌过滤
        if (!"".equals(searchMap.get("brand"))){//如果用户选择了品牌
            FilterQuery filterQuery=new SimpleFilterQuery();
            Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);

        }
        
        //1.4按规格过滤
        if (searchMap.get("spec")!=null){
            //转为map集合
            Map<String,String>specMap= (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                FilterQuery filterQuery=new SimpleFilterQuery();
                Criteria filterCriteria=new Criteria("item_spec_"+key).is(specMap.get(key));
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            
        }

        //1.5按价格过滤
        if (!"".equals(searchMap.get("price"))){
            //获取价格区间
            String[] price = ((String) searchMap.get("price")).split("-");
            if (!price[0].equals("0")){//最低价格不等于0
                FilterQuery filterQuery=new SimpleFilterQuery();
                Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

            if (!price[1].equals("*")){//最高价格不等于*
                FilterQuery filterQuery=new SimpleFilterQuery();
                Criteria filterCriteria=new Criteria("item_price").lessThanEqual(price[1]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.6分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");//获取页码
        if (pageNo==null){
            pageNo=1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");//获取页大小
        if (pageSize==null){
            pageSize=20;
        }

        query.setOffset((pageNo - 1) * pageSize);//获取起始索引
        query.setRows(pageSize);//每页记录数

        //1.7排序查询
        String sortValue = (String) searchMap.get("sort");//排序方式
        String sortField = (String) searchMap.get("sortField");//排序字段
        if (sortValue!=null && !sortField.equals("")){
            if (sortValue.equals("ASC")){
                Sort sort =new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }

            if (sortValue.equals("DESC")){
                Sort sort =new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }

        /*********高亮结果集*********/
        //高亮页对象
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //高亮入口集合(每条记录的高亮入口)
        List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
        for (HighlightEntry<TbItem> entry : entryList) {
            //获取高亮列表(高亮域的个数)
            List<HighlightEntry.Highlight> highlights = entry.getHighlights();
           /* for (HighlightEntry.Highlight h : highlights) {
                List<String> sns = h.getSnipplets();//每个域可能存储多值
            }*/
           if (highlights.size()>0&&highlights.get(0).getSnipplets().size()>0){
               TbItem item = entry.getEntity();
               item.setTitle(highlights.get(0).getSnipplets().get(0));
           }
        }
        map.put("rows",page.getContent());
        map.put("totalPages",page.getTotalPages());//总页数
        map.put("total",page.getTotalElements());//总记录数
        return map;
    }

    /**
     * 分组查询商品分类列表
     * @param searchMap
     * @return
     */
    private List<String> searchCategoryList(Map searchMap){

        //创建集合存储分组对象
        List<String> list=new ArrayList();

        Query query=new SimpleQuery("*:*");

        //关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));//相当于where...
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");//相当于group by..
        query.setGroupOptions(groupOptions);

        //获取分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);

        //获取分组结果对象
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");

        //获取分组入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();

        //获取分组入口集合
        List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : entryList) {
            list.add(entry.getGroupValue());//将获取的品牌字符串加入集合
        }
        return list;

    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据商品分类名称获取品牌列表,规格列表
     * @param category
     * @return
     */
    private Map searchBrandAndSpecList(String category){
        Map map=new HashMap();
        //根据商品分类名称获取模板id
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);

        if (templateId!=null){
            //根据模板id获取品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList",brandList);

            //根据模板id获取规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList",specList);

        }
        return map;
    }

    /**
     * 更新索引库，审核后更新
     * @param list
     */
    @Override
    public void importList(List list) {

        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }


    /**
     * 删除商品列表
     * @param goodIds
     */
    @Override
    public void deleteByGoodsIds(List goodIds) {

        Query query=new SimpleQuery("*:*");
        Criteria criteria=new Criteria("item_goodsid").in(goodIds);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

}
