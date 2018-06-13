package com.roncoo.eshop.cache.hystrix.command;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.roncoo.eshop.cache.model.ProductInfo;
import com.roncoo.eshop.cache.spring.SpringContext;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 从Redis获取商品Command
 *
 * @author yangfan
 * @date 2018/06/12
 */
public class GetProductInfoFromRedisCacheCommand extends HystrixCommand<ProductInfo> {

    private Long productId;

    public GetProductInfoFromRedisCacheCommand(Long productId) {

        super(HystrixCommandGroupKey.Factory.asKey("RedisGroup"));
        this.productId = productId;
    }


    @Override
    protected ProductInfo run() {
        StringRedisTemplate redisTemplate = SpringContext.getApplicationContext().getBean(StringRedisTemplate.class);

        String key = "product_info_" + productId;
        String json = redisTemplate.opsForValue().get(key);
        if (json != null) {
            return JSONObject.parseObject(json, ProductInfo.class);
        }
        return null;
    }

    @Override
    protected ProductInfo getFallback() {
        return null;
    }
}

