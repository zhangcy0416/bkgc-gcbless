package com.bkgc.bless.utils;

import com.bkgc.bless.config.Config;
import com.bkgc.game.util.SerializeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisUtil {

    @Autowired
    private StringRedisTemplate template;

    @Autowired
    private RedisTemplate redisTemplate;

    @Resource
    private Config config;

//    public void setKey(String key,String value){
//        ValueOperations<String, String> ops = template.opsForValue();
//        ops.set(key,value,1, TimeUnit.MINUTES);   //1分钟过期
//    }

    public String getValue(String key){
        ValueOperations<String, String> ops = template.opsForValue();
        return ops.get(key);
    }

    public String getSet(String key,String value){
        ValueOperations<String, String> ops = template.opsForValue();
        String retStr = ops.getAndSet(key, value);
        template.expire(key, config.getQrExpireMinutes(), TimeUnit.MINUTES);  //设置过期
        return retStr;
    }

    public void removeKey(String key){
        template.delete(key);
    }

    public int setnx(String key,String expireTime){

        boolean flag = redisTemplate.getConnectionFactory().getConnection().setNX(key.getBytes(), expireTime.getBytes());

        if(flag){
            return 1;
        }else {
            return 0;
        }
    }

    public void setRewardList(String key,String value){
        //redisTemplate.opsForList().leftPushAll(key, value);
        ValueOperations<String, String> ops = template.opsForValue();

        String temp = getRewardStr(key);

        if("".equals(temp)){
            temp = value;
        }else{
            temp = value + "," + temp;

            String[] tempArr = temp.split(",");

            if (tempArr.length > 10) {
                temp = tempArr[0];
                for (int i = 1; i <= 9; i++) {
                    temp = temp + "," + tempArr[i];
                }
            }
        }

        ops.getAndSet(key, temp);
    }

    public String getRewardStr(String key){
        ValueOperations<String, String> ops = template.opsForValue();
        String value = ops.get(key);

        if (value == null) {
            return "";
        }

        return value;
    }

    public List<String> getRewardList(String key){
        ValueOperations<String, String> ops = template.opsForValue();
        String value = ops.get(key);

        if (value == null) {
            return new ArrayList<>();
        }

        String[] awardList = value.split(",");
        return new ArrayList<>(Arrays.asList(awardList));
    }
}
