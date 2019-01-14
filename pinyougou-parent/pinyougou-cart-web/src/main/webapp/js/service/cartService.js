app.service("cartService",function ($http) {

    //查询购物车列表
    this.findCartList=function () {
        return $http.get('cart/findCartList.do')
    }

    //购物车商品数量加减
    this.addGoodsToCartList=function (itemId, num) {
        return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
    }

    //求合计数
    this.sum=function (cartList) {
        var totalValue={totalNum:0,totalMoney:0}
        for(var i=0;i<cartList.length;i++){
            var cart=cartList[i];
            for (var j=0;j<cart.orderItemList.length;j++){
                var orderItem=cart.orderItemList[j];
                totalValue.totalNum+=orderItem.num;//商品总数
                totalValue.totalMoney+=orderItem.totalFee;//商品总金额
            }
        }
        return totalValue;

    }

    //获取用户地址列表
    this.findAddressList=function () {
        return $http.get('address/findListByLoginUser.do')
    }

    //提交订单
    this.submitOrder=function (order) {
       return $http.post('order/add.do',order);
    }

})