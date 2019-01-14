app.service("loginService",function ($http) {

    //获取当前登录用户名
    this.loginName=function () {
       return $http.get("../login/name.do")
    }
})