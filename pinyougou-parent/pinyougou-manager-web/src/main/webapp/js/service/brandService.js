//自定义服务
    app.service("brandService",function($http){
    	
    	//查询品牌列表，未分页
    	this.findAll=function(){
    		return $http.get("../brand/findAll.do")
    	}
    	
    	//品牌分页
    	this.findPage=function(page,size){
    		return $http.get('../brand/findPage.do?page='+page+'&size='+size)
    	}
    	
    	//品牌修改，根据id查询实体
    	this.findOne=function(id){
    		return $http.get('../brand/findOne.do?id='+id)
    	}
    	
    	    	
    	//新增品牌(品牌新增)
    	this.add=function(entity){
    		return $http.post('../brand/add.do',entity)
    	}
    	
    	//品牌修改
    	this.update=function(entity){
    		return $http.post('../brand/update.do',entity)
    	}
    	
    	//批量删除品牌
    	this.dele=function(ids){
    		return $http.get('../brand/delete.do?ids='+ids)
    	}
    	
    	//品牌条件查询
    	this.search=function(page,size,searchEntity){
    		return $http.post('../brand/search.do?page='+page+'&size='+size,searchEntity)
    	}

    	//品牌下拉列表
		this.selectOptionList=function () {
			return $http.get('../brand/selectOptionList.do')
        }



    });