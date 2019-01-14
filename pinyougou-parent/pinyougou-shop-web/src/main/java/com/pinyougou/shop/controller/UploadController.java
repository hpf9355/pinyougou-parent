package com.pinyougou.shop.controller;


import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;

@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String file_server_url;

    @RequestMapping("/upload")
    public Result upload(MultipartFile file){

        //获取全文件名
        String originalFilename = file.getOriginalFilename();

        //获取文件扩展名
        String exName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);


        try {
            FastDFSClient client = new FastDFSClient("classpath:config/fdfs_client.conf");
            String fileId= client.uploadFile(file.getBytes(), exName);
            String url=file_server_url+fileId;//文件服务器路径


            return new Result(true,url);

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }

    }
}
