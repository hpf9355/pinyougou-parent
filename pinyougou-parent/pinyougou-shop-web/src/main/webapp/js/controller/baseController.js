app.controller("baseController",function($scope){//抽取公共代码
	
	//分页控件配置
	$scope.paginationConf = { 
			   currentPage: 1, 
			   totalItems: 10, 
			   itemsPerPage: 10, 
			   perPageOptions: [10, 20, 30, 40, 50], 
			   onChange: function(){ 
			            $scope.reloadList();//重新加载 
			   } 
			}; 
	//刷新列表
	$scope.reloadList=function(){
		$scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage)
	}
	
	//批量删除品牌，用户勾选的复选框
	$scope.selectIds=[];//定义集合 ，用来存储勾选的品牌id
	$scope.updateSelection=function($event,id){
		if($event.target.checked){
			$scope.selectIds.push(id);
		}else{
			var index=$scope.selectIds.indexOf(id);//查找该id在集合中的索引值
			$scope.selectIds.splice(index,1);//删除该id
		}
	}

	//在list集合中根据某key的值查询对象
	$scope.searchObjectByKey=function (list, key, keyValue) {

        for (var i=0;i<list.length;i++) {
            if (list[i][key] == keyValue) {
                return list[i];
            }

        }
        return null;
    }

	
});
