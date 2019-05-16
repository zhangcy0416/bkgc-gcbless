package com.bkgc.bless.service.impl;

import com.bkgc.bean.bless.MysqlLock;
import com.bkgc.bless.mapper.MysqlLockMapper;
import com.xiaoleilu.hutool.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * <p>Title:      MysqlLockServiceImpl </p>
 * <p>Description MYSQL实现分布式锁 </p>
 * <p>Company:    北控国彩 </p>
 *
 * @Author         zhangft
 * @CreateDate     2018/8/27 上午11:09
 */
@Service("mysqlLockService")
@Slf4j
public class MysqlLockService implements Lock {


    private String LockID = "getEnvelope";

    private int LockTimeSecond = 10;

    private ThreadLocal<String> threadLocal = new ThreadLocal<>();

    @Autowired
    private MysqlLockMapper mysqlLockMapper;


    public void setLockID(String lockID){
        log.info("设置锁ID={}", lockID);
        this.LockID = lockID;
    }

    public void setLockTimeSecond(int lockTimeSecond){
        log.info("设置锁失效时间={}", lockTimeSecond);
        this.LockTimeSecond = lockTimeSecond;
    }

    @Override
    public void lock() {
        if(!tryLock()){
            try {
                log.info("lock加锁失败，正在等待尝试");
                Thread.sleep(new Random().nextInt(LockTimeSecond) * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            lock();
        }

        log.info("lock加锁成功");
    }

    @Override
    public boolean tryLock() {

        String randStr = UUID.randomUUID().toString();

        Date createTime = new Date();

        Date expireTime = DateUtil.offsiteSecond(createTime, LockTimeSecond);

        try {
            log.info("randStr={}正在尝试加锁", randStr);
            mysqlLockMapper.addMysqlLock(LockID, randStr, createTime, expireTime);
            threadLocal.set(randStr);

            log.info("randStr={}加锁成功", randStr);

        }catch (Exception e){

            log.error("加锁出现异常，异常信息={}", e.getMessage(), e);

            //判断锁是否超时，超时的话，直接删掉
            MysqlLock mysqlLock = mysqlLockMapper.queryMysqlLock(LockID);
            if(mysqlLock.getExpireTime().before(new Date())){   //锁已过期
                int eff = mysqlLockMapper.delMysqlLock4Expire(LockID);
                if (eff == 1) {
                    log.info("{}锁已过期，已删除", mysqlLock.getRandstr());
                }else{
                    log.info("{}锁过期，删除失败", mysqlLock.getRandstr());
                }
            }

            return false;
        }

        return true;
    }

    @Override
    public void unlock() {
        String randStr = threadLocal.get();
        int eff = mysqlLockMapper.delMysqlLock(LockID, randStr);

        if (eff == 1) {
            log.info("锁被正常删除，randStr={}", randStr);
        }else{
            log.info("锁可能已过期，被其他线程删除,当前randStr={}", randStr);
        }
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
