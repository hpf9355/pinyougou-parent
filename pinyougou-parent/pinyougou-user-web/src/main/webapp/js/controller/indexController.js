app.controller("indexController",function (loginService,$scope) {

    //显示登录用户名
    $scope.showName=function () {
        loginService.showName().success(
            function (response) {
            $scope.loginName=response.loginName;
        });
    }

})