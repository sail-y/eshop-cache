package com.roncoo.eshop.cache.hystrix.command;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.*;
import com.roncoo.eshop.cache.model.ProductInfo;

/**
 * @author yangfan
 * @date 2018/07/02
 */
public class GetProductInfoCommand extends HystrixCommand<ProductInfo> {


    private Long productId;

    public GetProductInfoCommand(Long productId) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ProductInfoService"))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("GetProductInfoPool"))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                        .withCoreSize(10)
                        .withMaxQueueSize(12)
                        .withQueueSizeRejectionThreshold(8)
                        .withMaximumSize(30)
                        .withAllowMaximumSizeToDivergeFromCoreSize(true)
                        .withKeepAliveTimeMinutes(1)
                        .withMaxQueueSize(50)
                        .withQueueSizeRejectionThreshold(100))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        // 多少个请求以上才会判断断路器是否需要开启。
                        .withCircuitBreakerRequestVolumeThreshold(30)
                        // 错误的请求达到40%的时候就开始断路。
                        .withCircuitBreakerErrorThresholdPercentage(40)
                        // 3秒以后尝试恢复
                        .withCircuitBreakerSleepWindowInMilliseconds(4000))
        );
        this.productId = productId;
    }

    @Override
    protected ProductInfo run() throws Exception {
        if (productId == 100) {
            // 模拟从源服务查询某个商品ID，没有查询到数据
            // 在实际的生产环境中，就是没有查到数据，就写一个这样的数据返回

            ProductInfo productInfo = new ProductInfo();
            productInfo.setId(100L);
            return productInfo;
        } else {

            String productInfoJSON = "{\"id\": " + productId + ", \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1, \"modifiedTime\": \"2017-01-01 12:01:00\"}";
            return JSONObject.parseObject(productInfoJSON, ProductInfo.class);
        }

    }
}
