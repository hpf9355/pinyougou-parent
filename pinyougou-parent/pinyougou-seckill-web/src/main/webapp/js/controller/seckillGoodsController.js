app.controller("seckillGoodsController",function ($scope,$location,$interval,seckillGoodsService) {

    //返回参与秒杀得商品列表
    $scope.findList=function () {
        seckillGoodsService.findList().success(
            function (response) {
                $scope.list=response;
            }
        )
    }

    //根据ID从redis缓存中获取实体
       //从页面接收商品id
        var id = $location.search()['id'];
    $scope.findOne=function () {
        seckillGoodsService.findOne(id).success(
            function (response) {
                $scope.entity=response;

                //倒计时读秒
                var allsecond=Math.floor((new Date($scope.entity.endTime).getTime()-new Date().getTime())/1000);//秒杀活动总秒数

                time=$interval(function () {
                    allsecond=allsecond-1;
                    $scope.timeString=convertTimeString(allsecond);//转换秒数
                    if (allsecond<=0){
                        $interval.cancel(time);
                    }
                },1000);
            }
        )
    }


    //转换秒数为  XXX 天 10:22:33 格式
    convertTimeString=function (allsecond) {
        var days=Math.floor(allsecond/(60*60*24));//天数
        var hours=Math.floor((allsecond-days*24*60*60)/(60*60));//小时
        var minutes=Math.floor((allsecond-days*24*60*60-hours*60*60)/60);//分钟
        var second=allsecond-days*24*60*60-hours*60*60-minutes*60;//秒
        var timeString="";
        if (days>=0){
            timeString=days+"天"
        }
        return timeString+hours+":"+minutes+":"+second;

    }

    //秒杀下单
    $scope.submitOrder=function () {
        seckillGoodsService.submitOrder($scope.entity.id).success(
            function (response) {
                if (response.success){
                    alert("抢购成功，请在5分钟内支付!")
                    location.href='pay.html';
                }else {
                    alert(response.message);
                }
            }
        )
    }





})