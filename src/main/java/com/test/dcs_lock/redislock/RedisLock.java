package com.test.dcs_lock.redislock;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.lang.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author liyu
 * @date 2020/8/11 22:58
 * TODO
 */
public class RedisLock {

    private RedisTemplate redisTemplate;
    private String key;
    private String value;
    //锁自动过期时，单位秒
    private int expireTime;

    public RedisLock(RedisTemplate redisTemplate, String key, int expireTime) {
        this.redisTemplate = redisTemplate;
        this.key = key;
        this.value= UUID.randomUUID().toString();
        this.expireTime = expireTime;
    }

    /**
     * Set key value NX PX 30000
     * 通过setnx，来实现分布式锁
     * @return
     */
    public boolean getLock(){
        //redisCallback作用是让RedisTemplate进行回调,通过它们可以在同一条连接下执行多个redis命令,避免多次连接，并能利用原生redis的一些方法
        return (Boolean) redisTemplate.execute(new RedisCallback() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                //设置NX
                RedisStringCommands.SetOption setOption=RedisStringCommands.SetOption.ifAbsent();
                //设置过期时间
                Expiration expiration=Expiration.seconds(30);
                //序列化KEY
                byte[] redisKey=redisTemplate.getKeySerializer().serialize(key);
                byte[] redisValue=redisTemplate.getKeySerializer().serialize(value);
                //执行setnx操作
                Boolean result=redisConnection.set(redisKey,redisValue,expiration,setOption);
                return result;
            }
        });
    }

    public boolean unLock(){
        //lua脚本
        String script="if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
                "  return redis.call(\"del\",KEYS[1])\n" +
                "else\n" +
                "  return 0\n" +
                "end";
        RedisScript<Boolean> redisScript=RedisScript.of(script,Boolean.class);
        List<String> keys= Arrays.asList(key);
        return (Boolean) redisTemplate.execute(redisScript,keys,value);
    }

}
