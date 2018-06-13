package com.roncoo.eshop.cache.hystrix.command;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.roncoo.eshop.cache.model.ShopInfo;
import com.roncoo.eshop.cache.spring.SpringContext;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 保存商品信息到Redis
 *
 * @author yangfan
 * @date 2018/06/12
 */
public class SaveShopInfo2RedisCacheCommand extends HystrixCommand<Boolean> {

    private ShopInfo shopInfo;

    public SaveShopInfo2RedisCacheCommand(ShopInfo shopInfo) {

        super(HystrixCommandGroupKey.Factory.asKey("RedisGroup"));
        this.shopInfo = shopInfo;
    }


    @Override
    protected Boolean run() {
        StringRedisTemplate redisTemplate = SpringContext.getApplicationContext().getBean(StringRedisTemplate.class);

        String key = "shop_info_" + shopInfo.getId();
        redisTemplate.opsForValue().set(key, JSONObject.toJSONString(shopInfo));

        return true;
    }

    @Override
    protected Boolean getFallback() {
        return false;
    }
}
