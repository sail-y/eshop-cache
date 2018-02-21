package com.roncoo.eshop.cache.controller;

import com.alibaba.fastjson.JSONObject;
import com.roncoo.eshop.cache.model.ProductInfo;
import com.roncoo.eshop.cache.model.ShopInfo;
import com.roncoo.eshop.cache.rebuild.RebuildCacheQueue;
import com.roncoo.eshop.cache.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yangfan
 * @date 2018/02/20
 */
@RestController
public class CacheController {


    @Autowired
    private CacheService cacheService;

    @RequestMapping("/testPutCache")
    public String testPutCache(ProductInfo productInfo) {
        cacheService.saveProductInfo2LocalCache(productInfo);
        return "success";
    }

    @RequestMapping("/testGetCache")
    public ProductInfo testPutCache(Long id) {
        return cacheService.getLocalCache(id);
    }

    @GetMapping("/getProductInfo")
    public ProductInfo getProductInfo(Long productId) {
        // 先从Redis从获取数据
        ProductInfo productInfo = cacheService.getProductInfoFromRedisCache(productId);
        System.out.println("================从redis从获取缓存，商品信息=" + productInfo);

        if (productInfo == null) {
            productInfo = cacheService.getProductInfoFromLocalCache(productId);
            System.out.println("================从ehcache从获取缓存，商品信息=" + productInfo);
        }

        if (productInfo == null) {
            // 就需要从数据源重新拉取数据，重建缓存，模拟获取
            String productInfoJSON = "{\"id\": 2, \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 2, \"modifiedTime\": \"2018-02-21 22:11:34\"}";
            productInfo = JSONObject.parseObject(productInfoJSON, ProductInfo.class);
            // 将数据推送到一个内存队列中
            RebuildCacheQueue rebuildCacheQueue = RebuildCacheQueue.getInstance();
            rebuildCacheQueue.putProductInfo(productInfo);
        }

        return productInfo;
    }

    @GetMapping("/getShopInfo")
    public ShopInfo getShopInfo(Long shopId) {
        // 先从Redis从获取数据
        ShopInfo shopInfo = cacheService.getShopInfoFromRedisCache(shopId);
        System.out.println("================从redis从获取缓存，店铺信息=" + shopInfo);

        if (shopInfo == null) {
            shopInfo = cacheService.getShopInfoFromLocalCache(shopId);
            System.out.println("================从ehcache从获取缓存，店铺信息=" + shopInfo);
        }

        if (shopInfo == null) {
            // 就需要从数据源重新拉取数据，重建缓存，这里先不做
        }

        return shopInfo;
    }
}
