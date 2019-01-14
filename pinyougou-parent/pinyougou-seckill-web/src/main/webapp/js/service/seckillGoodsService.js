app.service("seckillGoodsService",function ($http) {

    //返回参与秒杀得商品列表
    this.findList=function () {
    return $http.get('seckillGoods/findList.do')
   }

   //根据ID从redis缓存中获取实体
    this.findOne=function (id) {
        return $http.get('seckillGoods/findOne.do?id='+id);
    }

    //秒杀下单
    this.submitOrder=function (seckillId) {

        return $http.get('seckillOrder/submitOrder.do?seckillId='+seckillId);
    }

})