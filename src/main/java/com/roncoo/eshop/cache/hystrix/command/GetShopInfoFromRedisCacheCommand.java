package com.roncoo.eshop.cache.hystrix.command;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.roncoo.eshop.cache.model.ShopInfo;
import com.roncoo.eshop.cache.spring.SpringContext;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 从Redis获取店铺信息Command
 * @author yangfan
 * @date 2018/06/12
 */
public class GetShopInfoFromRedisCacheCommand extends HystrixCommand<ShopInfo> {

    private Long shopId;

    public GetShopInfoFromRedisCacheCommand(Long shopId) {

        super(HystrixCommandGroupKey.Factory.asKey("RedisGroup"));
        this.shopId = shopId;
    }


    @Override
    protected ShopInfo run() {
        StringRedisTemplate redisTemplate = SpringContext.getApplicationContext().getBean(StringRedisTemplate.class);

        String key = "shop_info_" + shopId;
        String json = redisTemplate.opsForValue().get(key);
        if (json != null) {
            return JSONObject.parseObject(json, ShopInfo.class);
        }

        return null;
    }

    @Override
    protected ShopInfo getFallback() {
        return null;
    }
}
