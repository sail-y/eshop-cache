package com.roncoo.eshop.cache.rebuild;

import com.roncoo.eshop.cache.model.ProductInfo;
import com.roncoo.eshop.cache.service.CacheService;
import com.roncoo.eshop.cache.spring.SpringContext;
import com.roncoo.eshop.cache.zk.ZooKeeperSession;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author yangfan
 * @date 2018/02/21
 */
public class RebuildCacheThread implements Runnable {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");

    @Override
    public void run() {

        RebuildCacheQueue rebuildCacheQueue = RebuildCacheQueue.getInstance();

        ZooKeeperSession zkSession = ZooKeeperSession.getInstance();

        while (true) {
            ProductInfo productInfo = rebuildCacheQueue.takeProductInfo();

            // 获取分布式锁
            zkSession.acquireDistributedLock(productInfo.getId());
            CacheService cacheService = SpringContext.getApplicationContext().getBean(CacheService.class);

            // 获取到了锁，先从Redis中获取数据，进行时间上的比较
            ProductInfo existedProductInfo = cacheService.getProductInfoFromRedisCache(productInfo.getId());
            if (existedProductInfo != null) {
                // 比较当前数据的时间版本比已有数据的时间版本是新还是旧
                try {
                    Date date = sdf.parse(productInfo.getModifiedTime());
                    Date existedDate = sdf.parse(existedProductInfo.getModifiedTime());

                    if (date.before(existedDate)) {
                        System.out.println("current date=" + productInfo.getModifiedTime() + " is before existed date[" + existedProductInfo.getModifiedTime() + "]");
                        continue;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                System.out.println("current date=" + productInfo.getModifiedTime() + " is after existed date[" + existedProductInfo.getModifiedTime() + "]");
            }else {
                System.out.println("existed productInfo is null");

            }


            cacheService.saveProductInfo2LocalCache(productInfo);
            cacheService.saveProductInfo2RedisCache(productInfo);

            // 释放分布式锁
            zkSession.releaseDistributedLock(productInfo.getId());
        }
    }
}
