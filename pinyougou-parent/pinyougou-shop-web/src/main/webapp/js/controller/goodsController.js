 //控制层 
app.controller('goodsController' ,function($scope,$controller ,$location  ,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		var id = $location.search()['id'];

		if(id==null){

           return;
        }
        goodsService.findOne(id).success(
            function(response){
                $scope.entity= response;
                //获取商品的富文本编辑器内容
                editor.html($scope.entity.goodsDesc.introduction);
                //获取商品图片
                $scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
                //获取商品的扩展属性
                $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //获取商品规格
                $scope.entity.goodsDesc.specificationItems=JSON.parse( $scope.entity.goodsDesc.specificationItems);
                //转换sku列表中的规格对象
				for(var i=0;i< $scope.entity.itemList.length;i++){
                     $scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
				}

            }
        );
	}

	//商品增加
	$scope.add=function(){
		$scope.entity.goodsDesc.introduction=editor.html();

       goodsService.add( $scope.entity  ).success(
			function(response){
				if(response.success){
                    //提示
					alert("新增成功")
                    $scope.entity={};//清空列表
                    editor.html("");//清空富文本编辑器
				}else{
					alert(response.message);
				}
			}		
		);				
	}

	//商品更新修改
    $scope.save=function(){

		//提取文本编辑器的内容
        $scope.entity.goodsDesc.introduction=editor.html();
        var serviceObject=null;//服务层对象
        if($scope.entity.goods.id!=null){//有id,商品信息修改
            serviceObject=goodsService.update($scope.entity);
        }else{
            serviceObject=goodsService.add($scope.entity);//无id,商品增加
        }
        serviceObject.success(
            function(response){
                if(response.success){
                    alert("保存成功");//保存成功
					location.href="goods.html";
                }else{
                    alert(response.message)//添加失败，弹出信息
                }
            }
        );
    }
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//文件上传
	$scope.uploadFile=function () {
		uploadService.uploadFile().success(
			function (response) {
			if (response.success){

				$scope.image_entity.url=response.message;


			}else {
				alert(response.message)
			}
        });
    }

    //将商品图片添加到图片列表
    $scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}}
	$scope.add_image_entity=function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    //删除添加的商品图片
	$scope.remove_image_entity=function (index) {
		$scope.entity.goodsDesc.itemImages.splice(index,1);
    }

    //添加商品的分类 ，第一级分类
	$scope.selectItemCat1List=function () {
		itemCatService.findByParentId(0).success(
            function (response) {
                $scope.itemCat1List=response;
            }
		);
    };

    //添加商品的分类 ，第二级分类,采用变量监控
    $scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {

        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat2List=response;
            }
        );
    });

    //添加商品的分类 ，第三级分类,采用变量监控
    $scope.$watch('entity.goods.category2Id',function (newValue,oldValue) {

        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat3List=response;
            }
        );
    });

    //获取模板id,根据第三级分类
    $scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {

        itemCatService.findOne(newValue).success(
            function (response) {
                $scope.entity.goods.typeTemplateId=response.typeId;
            }
        );
    });

    //根据模板id,获取商品品牌列表,规格列表
    $scope.$watch('entity.goods.typeTemplateId',function (newValue,oldValue) {

    	//根据模板id,获取商品品牌列表，扩展属性列表
        typeTemplateService.findOne(newValue).success(
            function (response) {
                $scope.typeTemplate=response;

                //将查询到的品牌列表字符串转为json
                $scope.typeTemplate.brandIds=JSON.parse( $scope.typeTemplate.brandIds);
                if($location.search()['id']==null){//增加商品
                    //将查询到的扩展属性列表字符串转为json
                    $scope.entity.goodsDesc.customAttributeItems=JSON.parse( $scope.typeTemplate.customAttributeItems);

                }

                //将查询到的扩展属性列表字符串转为json
               // $scope.entity.goodsDesc.customAttributeItems=JSON.parse( $scope.typeTemplate.customAttributeItems);

            }
        );

        //根据模板id,获取规格列表
        typeTemplateService.findSpecList(newValue).success(
        	function (response) {
			    $scope.specList=response;
        }
		);

    });

    //规格选项的勾选
	$scope.updateSpecAttribute=function ($event,name, value) {
        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
        if (object!=null){
        	if ($event.target.checked){
                object.attributeValue.push(value);
			}else {//取消勾选
                object.attributeValue.splice(object.attributeValue.indexOf(value),1);
                //如果选项都取消，则删除此条记录
                if (object.attributeValue.length==0){
                    $scope.entity.goodsDesc.specificationItems.splice( $scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}
			}

		}else {
            $scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});

		}

    }

    //创建SKU列表
	$scope.createItemList=function () {
		$scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0'}];//列表初始化
		var items=$scope.entity.goodsDesc.specificationItems;
		for(var i=0;i<items.length;i++){
            $scope.entity.itemList=addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);

		}
    }

    //深克隆
	addColumn=function (list, columnName, columnValues) {
		var newList=[];
		for(var i=0;i<list.length;i++){
			var oldRow=list[i];
			for(var j=0;j<columnValues.length;j++){
				var newRow=JSON.parse(JSON.stringify(oldRow));//克隆
				newRow.spec[columnName]=columnValues[j];
				newList.push(newRow);
			}
		}
		return newList;
    }

    //商家看到的商品状态
	$scope.status=["未审核","已审核","审核未通过","已关闭"]

	//商家看到的商品分类
	$scope.itemCatList=[];//定义商家商品分类
	$scope.findItemCatList=function () {
		itemCatService.findAll().success(
			function (response) {
				for(var i=0;i<response.length;i++){
                    $scope.itemCatList[response[i].id]=response[i].name;
				}
            }
		);
    }


    $scope.checkAttributeValue=function (specName, optionName) {
        var items = $scope.entity.goodsDesc.specificationItems;
        var object = $scope.searchObjectByKey(items, 'attributeName', specName);
        if (object != null) {

        	//获取规格选项
			if(object.attributeValue.indexOf(optionName)>=0){
				return true;
			}else {
				return false;
			}
        } else {
            return false;
        }
    }
    
});	
