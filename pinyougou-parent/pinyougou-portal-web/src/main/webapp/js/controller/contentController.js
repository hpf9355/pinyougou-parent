app.controller("contentController",function ($scope,contentService) {

    //根据广告分类id查询广告列表
    $scope.contentList=[];
    $scope.findByCategoryId=function (categoryId) {
        contentService.findByCategoryId(categoryId).success(
            function (response) {
                $scope.contentList[categoryId] =response;
            }
        );
    }

    //首页跳转至搜索页
    $scope.search=function () {
        location.href='http://localhost:9104/search.html#?keywords='+$scope.keywords;
    }

})