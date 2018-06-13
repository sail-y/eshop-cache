package com.roncoo.eshop.cache.hystrix.command;

import com.alibaba.fastjson.JSON;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.roncoo.eshop.cache.model.ProductInfo;
import com.roncoo.eshop.cache.spring.SpringContext;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author yangfan
 * @date 2018/06/12
 */
public class SaveProductInfo2RedisCacheCommand extends HystrixCommand<Boolean> {

    private ProductInfo productInfo;

    public SaveProductInfo2RedisCacheCommand(ProductInfo productInfo) {

        super(HystrixCommandGroupKey.Factory.asKey("RedisGroup"));
        this.productInfo = productInfo;
    }


    @Override
    protected Boolean run() {
        StringRedisTemplate redisTemplate = SpringContext.getApplicationContext().getBean(StringRedisTemplate.class);

        String key = "product_info_" + productInfo.getId();
        redisTemplate.opsForValue().set(key, JSON.toJSONString(productInfo));

        return true;
    }

    @Override
    protected Boolean getFallback() {
        return false;
    }
}
