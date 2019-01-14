app.service("uploadService",function ($http) {


    //文件上传
    this.uploadFile=function () {
        var formData = new FormData();
        formData.append("file",file.files[0]);//文件上传框的name
        return $http({
            url:'../upload.do',
            method:'post',
            data:formData,
            headers:{'Content-Type':undefined},
            transformRequest:angular.identity
        });

    }

})