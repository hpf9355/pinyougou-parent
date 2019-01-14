 //控制层 
app.controller('userController' ,function($scope,$controller   ,userService){	
	

    //用户注册
    $scope.reg=function () {

        //判断两次输入的密码是否一致
        if ($scope.password!=$scope.entity.password){
            alert("两次输入的密码不一致!")
            //清空密码
            $scope.password="";
            $scope.entity.password="";
            return;
        }

        userService.add($scope.entity,$scope.smscode).success(
            function (response) {
                alert(response.message);
            }
        );

    }

    //生成验证码
    $scope.sendCode=function () {
        if ($scope.entity.phone==null||$scope.entity.phone==''){
            alert("请填写手机号")
            return;
        }
        userService.sendCode($scope.entity.phone).success(
            function (response) {
                alert(response.message)
            }
        );
    }
});	
