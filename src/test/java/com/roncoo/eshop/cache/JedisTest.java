package com.roncoo.eshop.cache;

import org.junit.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

/**
 * @author yangfan
 * @date 2018/02/21
 */
public class JedisTest {
    @Test
    public void test() {
        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        jedisClusterNodes.add(new HostAndPort("192.168.2.202", 7003));
        jedisClusterNodes.add(new HostAndPort("192.168.2.202", 7004));
        jedisClusterNodes.add(new HostAndPort("192.168.2.203", 7006));
        JedisCluster jedisCluster = new JedisCluster(jedisClusterNodes);
        System.out.println(jedisCluster.get("product_info_1"));
        System.out.println(jedisCluster.get("shop_info_1"));
    }
}
