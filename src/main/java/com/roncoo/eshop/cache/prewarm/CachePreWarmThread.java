package com.roncoo.eshop.cache.prewarm;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.roncoo.eshop.cache.model.ProductInfo;
import com.roncoo.eshop.cache.service.CacheService;
import com.roncoo.eshop.cache.spring.SpringContext;
import com.roncoo.eshop.cache.zk.ZooKeeperSession;

/**
 *
 * 缓存预热线程
 *
 * @author yangfan
 * @date 2018/03/02
 */
public class CachePreWarmThread extends Thread {


    @Override
    public void run() {

        CacheService cacheService = SpringContext.getApplicationContext().getBean(CacheService.class);

        ZooKeeperSession zkSession = ZooKeeperSession.getInstance();

        // 获取storm taskid列表
        String taskIdList = zkSession.getNodeData("/taskid-list");
        System.out.println("【CachePrwarmThread获取到taskid列表】taskidList=" + taskIdList);

        if (taskIdList != null && !"".equals(taskIdList)) {
            String[] taskIdListSplited = taskIdList.split(",");
            for (String taskId : taskIdListSplited) {
                // 获取每个锁
                String taskIdLockPath = "/taskid-lock-" + taskId;
                // 依次遍历每个taskid，尝试获取分布式锁，如果获取不到，快速报错，不要等待，因为说明已经有其他服务实例在预热了
                boolean result = zkSession.acquireFastFailedDistributedLock(taskIdLockPath);

                if (!result) {
                    continue;
                }

                // 直接尝试获取下一个taskid的分布式锁
                String taskIdStatusLockPath = "/taskid-status-lock-" + taskId;
                zkSession.acquireDistributedLock(taskIdStatusLockPath);

                String taskIdStatus = zkSession.getNodeData("/taskid-status-" + taskId);
                System.out.println("【CachePrewarmThread获取task的预热状态】taskid=" + taskId + ", status=" + taskIdStatus);
                // 即使获取到了分布式锁，也要检查一下这个taskid的预热状态，如果已经被预热过了，就不再预热了
                if ("".equals(taskIdStatus)) {
                    String productIdList = zkSession.getNodeData("/task-hot-product-list-" + taskId);
                    System.out.println("【CachePrewarmThread获取到task的热门商品列表】productidList=" + productIdList);

                    JSONArray productIdJSONArray = JSONArray.parseArray(productIdList);
                    for (int i = 0; i < productIdJSONArray.size(); i++) {
                        Long productId = productIdJSONArray.getLong(i);

                        String productInfoJSON = "{\"id\": " + productId + ", \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1, \"modifiedTime\": \"2017-01-01 12:00:00\"}";

                        ProductInfo productInfo = JSONObject.parseObject(productInfoJSON, ProductInfo.class);
                        // 执行预热操作，遍历productid列表，查询数据，然后写ehcache和redis
                        cacheService.saveProductInfo2LocalCache(productInfo);
                        System.out.println("【CachePrwarmThread将商品数据设置到本地缓存中】productInfo=" + productInfo);
                        cacheService.saveProductInfo2RedisCache(productInfo);
                        System.out.println("【CachePrwarmThread将商品数据设置到redis缓存中】productInfo=" + productInfo);
                    }

                    // 预热完成后，设置taskid对应的预热状态
                    zkSession.createNode("/taskid-status-" + taskId);
                    zkSession.setNodeData("/taskid-status-" + taskId, "success");
                }



                zkSession.releaseDistributedLock(taskIdStatusLockPath);
                zkSession.releaseDistributedLock(taskIdLockPath);
            }
                    
        }
    }
}
