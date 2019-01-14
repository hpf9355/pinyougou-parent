app.controller("brandController",function($scope,$controller,brandService){
    	
	//继承baseController中的公共代码
	$controller("baseController",{$scope:$scope})
	
    	//查询品牌列表
    	$scope.findAll=function(){
    		brandService.findAll().success(
    		function(response){
    			$scope.list=response;
    		}		
    		);
    	}
    	
    	//品牌分页
    	$scope.findPage=function(page,size){
    		brandService.findPage(page,size).success(
    		function(response)	{
    			$scope.list=response.rows;//显示当前页数据
    			$scope.paginationConf.totalItems=response.total;//更新总记录数
    		}	
    		)
    	}
    	
    	//新增品牌
    	$scope.save=function(){
    		var object=null;
    		if($scope.entity.id!=null){
    			object=brandService.update($scope.entity);
    		}else{
    			object=brandService.add($scope.entity);
    		}
    		object.success(
    		function(response){
    			if(response.success){
    				$scope.reloadList();//添加成功刷新列表
    			}else{
    				alert(response.message)//添加失败，弹出信息
    			}
    		}		
    		);
    	}
    	
    	//品牌修改，根据id查询实体
    	$scope.findOne=function(id){
    		brandService.findOne(id).success(
    			function(response){
    				$scope.entity=response;
    			}	
    		);
    	}
    	
    	
    		//删除
    		$scope.dele=function(){
    			brandService.dele($scope.selectIds).success(
    				function(response){
    					if(response.success){
    						$scope.reloadList();//删除成功，刷新列表
    					}else{
    						alert(response.message);//删除成功，弹出信息
    					}
    				}	
    			);
    		}
    	
    	//条件查询
    	$scope.searchEntity={};
    	$scope.search=function(page,size){
    		brandService.search(page,size,$scope.searchEntity).success(
    	    		function(response)	{
    	    			$scope.list=response.rows;//显示当前页数据
    	    			$scope.paginationConf.totalItems=response.total;//更新总记录数
    	    		}	
    	    		)
    	}


    });