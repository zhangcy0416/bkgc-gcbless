package com.bkgc.bless.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DistributeLockUtil {

    // 锁超时时间, 防止死锁
    private static final long LOCK_TIMEOUT = 60;

    @Autowired
    private RedisUtil redisUtil;

    private boolean locked = false;

    public boolean lock(String key) {
        log.info("开始加锁，锁的key=" + key);
        String expireTime = String.valueOf(System.currentTimeMillis() + LOCK_TIMEOUT * 1000);
        /*
        *   setnx 返回1
        *   说明: 1)key不存在, 2)成功写入锁, 并更新锁的生存时间
        *   也就是get锁
        * */
        if (redisUtil.setnx(key, expireTime) == 1) {
            log.info("加锁成功");
            locked = true;
            return true;
        }else{
            log.info("setnx加锁失败");
        }
        /*
        *  没有get锁, 下面进入判断锁超时逻辑
        * */
        String currentExpireTime = redisUtil.getValue(key);
        /*
        *   锁生存时间已经过了, 说明锁已经超时
        * */
        if (Long.parseLong(currentExpireTime) < System.currentTimeMillis()) {
            log.info("锁过期");
            String oldValueStr = redisUtil.getSet(key, expireTime);
            /*
            *   判断锁生存时间和你改的写那个时间是否相等
            *   相当于你竞争了一个更新锁
            * */
            if (oldValueStr.equals(currentExpireTime)) {
                log.info("过期的锁重新被加锁");
                locked = true;
                return true;
            }
        }
        log.info("锁正在被使用");
        return false;
    }

    public void release(String key) {
        if (locked) {
            log.info("已释放锁");
            redisUtil.removeKey(key);
            locked = false;
        }else {
            log.info("释放锁失败");
        }
    }

}

