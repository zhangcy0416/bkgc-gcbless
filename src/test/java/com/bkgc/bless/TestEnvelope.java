package com.bkgc.bless;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bkgc.common.utils.SnowflakeIdWorker;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangft on 2018/8/25.
 */
public class TestEnvelope {

    private String url = "http://t20000.8fubao.com/api-bless/envelope/getBlessEnvelope";
    private String url2 = "http://t20000.8fubao.com/api-vemtp/blessenvelope/getBlessEnvelopeURL";
    private String charset = "utf-8";
    private HttpClientUtil httpClientUtil = null;

    public TestEnvelope(){
        httpClientUtil = new HttpClientUtil();
    }


    public BigDecimal testGet(String userId, String randNum){
        String httpOrgCreateTest = url;
        Map<String,String> createMap = new HashMap<>();
        createMap.put("userId", userId);
        createMap.put("randNum", randNum);

        String httpOrgCreateTestRtn = httpClientUtil.doPost(httpOrgCreateTest, createMap, charset);
        //System.out.println("result=" + httpOrgCreateTestRtn);

        JSONObject jsonObject = JSON.parseObject(httpOrgCreateTestRtn);

        if("1000".equals(jsonObject.getString("code"))){
            JSONObject dataJson = jsonObject.getJSONObject("data");

            BigDecimal amount = dataJson.getBigDecimal("amount");

            return amount;
        }else{
            System.out.println(jsonObject.toString());
        }

        System.out.println("未领到福包");
        return BigDecimal.ZERO;

    }

    public String testGetRandNum(String machineId){
        String httpOrgCreateTest = url2;
        Map<String,String> createMap = new HashMap<>();
        createMap.put("machineId", machineId);

        String httpOrgCreateTestRtn = httpClientUtil.doPost(httpOrgCreateTest, createMap, charset);
        //System.out.println("result=" + httpOrgCreateTestRtn);

        JSONObject jsonObject = JSON.parseObject(httpOrgCreateTestRtn);

        if("1000".equals(jsonObject.getString("code"))){
            JSONObject dataJson = jsonObject.getJSONObject("data");

            //System.out.println("url=" + dataJson.getString("url"));
            String randNum = dataJson.getString("url").substring(dataJson.getString("url").indexOf("?") + 1);
            //System.out.println(randNum);
            return randNum;
        }

        return null;

    }

    public static void main2(String[] args) {

        //TODO 模拟两台服务器，t环境，2个bless,端口不同，从api网关处负载

        TestEnvelope testEnvelope = new TestEnvelope();

        String userId = "2311d327648945c3b8f6b94acf61e90c";




        new Thread(
            new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 20; i++) {

                        //通过机器id获取随机码
                        String randNum = testEnvelope.testGetRandNum("8001680");

                        BigDecimal amount = testEnvelope.testGet(userId, randNum);
                        System.out.println("线程1，获取福包金额=" + amount);

                    }
                }
            }
        ).start();

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 20; i++) {

                            //通过机器id获取随机码
                            String randNum = testEnvelope.testGetRandNum("8001680");

                            BigDecimal amount = testEnvelope.testGet(userId, randNum);
                            System.out.println("线程2，获取福包金额=" + amount);

                        }
                    }
                }
        ).start();

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 20; i++) {

                            //通过机器id获取随机码
                            String randNum = testEnvelope.testGetRandNum("8001680");

                            BigDecimal amount = testEnvelope.testGet(userId, randNum);
                            System.out.println("线程3，获取福包金额=" + amount);

                        }
                    }
                }
        ).start();

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 20; i++) {

                            //通过机器id获取随机码
                            String randNum = testEnvelope.testGetRandNum("8001680");

                            BigDecimal amount = testEnvelope.testGet(userId, randNum);
                            System.out.println("线程4，获取福包金额=" + amount);

                        }
                    }
                }
        ).start();

    }

    public static void main(String[] args) {
        GenerateOrderNoService generateOrderNoService = new GenerateOrderNoService();
        generateOrderNoService.generate();
    }


    static class GenerateOrderNoService {



        public String generate() {

            SnowflakeIdWorker snowflakeIdWorker = SnowflakeIdWorker.getInstance(1, 7);

            System.out.println("对象=" + snowflakeIdWorker);

            long id = snowflakeIdWorker.nextId();

            System.out.println("生成的id=" + id);

            return String.valueOf(id);
        }

    }

}
