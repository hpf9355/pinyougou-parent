app.controller("searchController",function ($scope,$location, searchService) {


    //定义搜索条件的结构
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};

    //搜索
    $scope.search=function () {
        //将当前页码转换为数字
        $scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);

        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap=response;

                buildPageLabel(); //构建分页栏
                //$scope.searchMap.pageNo=1;//查询后显示第一页

            }
        );

    }

    //构建分页栏
    buildPageLabel=function () {
        //构建分页栏
        $scope.pageLabel=[];
        var firstPage=1;//页面显示的起始页码
        var lastPage=$scope.resultMap.totalPages;//截至页码
         $scope.firstDot=true;
         $scope.lastDot=true;
        if ($scope.resultMap.totalPages>5){//如果总页码数大于5
            if ($scope.searchMap.pageNo<=3){//如果当前页码小于等于3,显示前5页
                lastPage=5;
                $scope.firstDot=false;
            }else if($scope.searchMap.pageNo>=$scope.resultMap.totalPages-2){
                firstPage=$scope.resultMap.totalPages-4;//如果当前页码大于等于总页码-2，显示后五页
                $scope.lastDot=false;
            }else {
                firstPage=$scope.searchMap.pageNo-2;
                lastPage=$scope.searchMap.pageNo+2;
            }
        }else {
            $scope.firstDot=false;//前面无点
            $scope.lastDot=false;//后面无点
        }

        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }




    //添加搜索项(改变searchMap的值)
    $scope.addSearchItem=function (key, value) {

        if (key=='category'||key=='brand'||key=='price'){//如果用户点击的分类或品牌

            $scope.searchMap[key]=value;

        }else {//用户点击的规格

            $scope.searchMap.spec[key]=value;
        }
        //搜索
        $scope.search();

    }

    //移除搜索项(重置searchMap的值)
    $scope.removeSearchItem=function (key) {

        if (key=='category'||key=='brand'||key=='price'){//如果用户点击的分类或品牌

            $scope.searchMap[key]='';

        }else {//用户点击的规格

          delete  $scope.searchMap.spec[key];
        }
        //搜索
        $scope.search();

    }


    //分页查询
    $scope.queryByPage=function (pageNo) {
        if (pageNo<1||pageNo>$scope.resultMap.totalPages){
            return
        }
        $scope.searchMap.pageNo=pageNo;
        $scope.search();//查询
    }

    //判断当前页是否为第一页
    $scope.isTopPage=function () {
        if ($scope.searchMap.pageNo==1){
            return true;
        }else {
            return false;
        }
    }

    //判断当前页是否为最后一页
    $scope.isEndPage=function () {
        if ($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else {
            return false;
        }
    }


    //排序查询
    $scope.searchSort=function (sortField, sort) {
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;
        //搜索
        $scope.search();
    }

    //判断关键字是否为品牌
    $scope.keywordsIsBrand=function () {
        for (var i=0;i<$scope.resultMap.brandList.length;i++){
            //判断品牌是否为关键字的子字符串
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                return true;
            }
        }
        return false;
    }

    //获取首页传递的搜索关键字
    $scope.loadkeywords=function () {
        $scope.searchMap.keywords=$location.search()['keywords']
        $scope.search();
    }


});