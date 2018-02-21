package com.roncoo.eshop.cache;

import com.roncoo.eshop.cache.zk.ZooKeeperSession;
import org.junit.Test;

/**
 * @author yangfan
 * @date 2018/02/21
 */
public class ZookeeperTest {

    @Test
    public void test() {
        ZooKeeperSession.init();
    }
}
