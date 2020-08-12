package com.test.dcs_lock.redislock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liyu
 * @date 2020/8/11 22:50
 * redis分布式锁
 */
@RestController
public class RedisDcsLock {
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/testLock")
    public void testLock(){
        System.out.println("我进入了方法");
        RedisLock redisLock=new RedisLock(redisTemplate,"redisKey",30);
        if(redisLock.getLock()){
            System.out.println("我获取到分布式锁，开始执行任务");
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                redisLock.unLock();
                System.out.println("任务执行完成，释放分布式锁");
            }
        }else {
            System.out.println("我没有拿到分布式锁，无法执行任务");
        }

    }
}
