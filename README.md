## 基于Redis的Setnx实现分布式锁
- 获取锁的Redis命令

```bash
Set resource_name my_random_value NX PX 30000
```

- resource_name ：资源名称，可根据不同的业务区分不同的锁
- my_random_value：随机值，每个线程的随机值都不同，用于释放锁时的校验
- NX：key不存在时设置成功。key存在则设置不成功
- PX：自动失效时间，出现异常情况，锁可以过期

实现原理：利用redis单线程的NX的原子性，多个线程并发时候，只有一个线程可以设置成功，设置成功即获得锁，可以执行后续的业务处理，业务执行完成后**释放锁**，如果出现异常，过了锁的有效期，锁自动释放。
释放锁：释放锁采用redis的delete命令，在释放锁时校验之前设置的随机数，相同才能释放锁，释放锁采用LUA脚本。

```bash
if redis.call("get"，KEYS[1]==ARGV[1]) then
	return redis.call("del",keys[1])
else 
    return 0
end
```
redis多任务重复执行的问题