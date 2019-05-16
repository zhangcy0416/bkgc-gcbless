package com.bkgc.game.principal;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.bless.RewardStep;
import com.bkgc.bean.game.*;
import com.bkgc.bless.mapper.RewardStepMapper;
import com.bkgc.game.mapper.RewardFactorMapper;
import com.bkgc.game.mapper.RewardMapper;
import com.bkgc.game.mapper.RewardOfUserMapper;
import com.bkgc.game.mapper.RewardPrincipalMapper;
import com.bkgc.game.model.enums.AwardWeigntEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Created by admin on 2017/12/28.
 * 游戏规则
 */
@Service(value = "rewardPrincipal")
public class GamePrincipal {

    private static Logger logger = LoggerFactory.getLogger(GamePrincipal.class);

    @Autowired
    private RewardFactorMapper rewardFactorMapper;

    @Autowired
    private RewardPrincipalMapper rewardPrincipalMapper;

    @Autowired
    private RewardMapper rewardMapper;

    @Autowired
    private RewardStepMapper rewardStepMapper;

    @Autowired
    private RewardOfUserMapper rewardOfUserMapper;

    private static RewardFactorMapper staticRewardFactorMapper;

    private static List<Reward> rewards;

    private static List<RewardStep> rewardStepList;


    /**
     * 初始化奖品Map
     * Map<String,Reward>:
     * {"TWO_BLESS"=, "FIVE_BLESS=", "TEN_BLESS=", "NINETY"}
     */

    @PostConstruct
    public void init() {
        rewards = rewardMapper.queryAll();
        if (rewards.isEmpty()) {
            logger.error("**************奖品库没有初始化数据*************");
            System.exit(-1);
        }

        for (Reward reward : rewards) {
            AwardWeigntEnum.setWeightByAwardId(reward.getId(), reward.getWeight());
        }

        for (AwardWeigntEnum awardWeigntEnum : AwardWeigntEnum.values()) {
            logger.info("初始化后，奖品id={},权重={}", awardWeigntEnum.getAwardId(), awardWeigntEnum.getValue());
        }

        staticRewardFactorMapper = rewardFactorMapper;

        rewardStepList = rewardStepMapper.queryAll();

        if(rewardStepList.isEmpty()){
            logger.error("初始化奖品阶梯失败，启动失败");
            System.exit(-1);
        }

        logger.info("初始化奖品阶梯");
        for (RewardStep rewardStep : rewardStepList) {
            logger.info("step={}", rewardStep.getStep());
        }
    }

    private void resetWeight(){
        for (Reward reward : rewards) {
            AwardWeigntEnum.setWeightByAwardId(reward.getId(), reward.getWeight());
        }
    }

    private List<String> getRewardIdPool(List<RewardPool> rewardPoolList){

        List<String> awardIdList = new ArrayList<>();

        for (RewardPool rewardPool : rewardPoolList) {
            logger.info("{}剩余{}个", rewardPool.getRewardName(), rewardPool.getRemain());
            awardIdList.add(rewardPool.getRewardId());
        }

        return awardIdList;

    }



    /**
     * <p>Title:      gts版匹配规则 </p>
     * <p>Description  </p>
     *
     * @Author        zhangft
     * @CreateDate    2018/7/17 下午2:03
     */
    public JSONObject match(String userId, List<RewardPool> rewardPoolList) {

        JSONObject retJson = new JSONObject();

        List<String> rewardIdList = getRewardIdPool(rewardPoolList);

        List<RewardPrincipal> list = rewardPrincipalMapper.selectByGuid(userId);
        if (!list.isEmpty()) { //规则库中指定用户获奖奖品
            logger.info("查询到{}条用户奖品信息", list.size());
            Random rand = new Random();
            int next = rand.nextInt(list.size());
            logger.info("随机值为={}", next);
            RewardPrincipal r = list.get(next);

            if(rewardIdList.contains(r.getRewardId())){

                retJson.put("available", "0");
                retJson.put("rewardPrincipalId", r.getId());
                retJson.put("rewardId", r.getRewardId());
                retJson.put("reward_principal", "1");  //表示规则表要更新

                return retJson;
            }else{
                retJson.put("rewardId", AwardWeigntEnum.TWO_BLESS.getAwardId());
                return retJson;
            }
        } else {

            logger.info("未查询到用户的奖品规则信息");
            return getRewardOfUser(userId, rewardIdList);
        }
    }

    /**
     * @param userId
     * @return
     */
    private JSONObject getRewardOfUser(String userId, List<String> rewardIdList) {
        JSONObject retJson = new JSONObject();
        retJson.put("rewardId", getRewardIdByUserId(userId, rewardIdList));
        return retJson;
    }

    /**
     * 计算新权重
     *
     * @return
     */
    private List<AwardWeigntEnum> calWeight(List<AwardWeigntEnum> awardWeigntEnumList, RewardFactor rewardFactor, List<String> rewardIdList) {

        //1.查询当前各个奖品的中奖次数
        //2.找到该阶段的各个奖品的中奖次数
        //3.结算差值，作为权重

        int rewardCount = rewardFactor.getThreeAwardCount() % 10;
        RewardStep currentRewardStep = null;

        for (RewardStep rewardStep : rewardStepList) {
            if (rewardStep.getStep() == rewardCount) {
                currentRewardStep = rewardStep;
                break;
            }
        }

        if (currentRewardStep == null) {  //未设置中奖阶梯，则返回空集合，让用户夺得2元福包奖金
            logger.error("当前情况未设置阶梯奖品");
            awardWeigntEnumList = new ArrayList<>();
            return awardWeigntEnumList;
        }

        int currCount = rewardFactor.getTwoBless() + rewardFactor.getFiveBless() + rewardFactor.getTenBless() + rewardFactor.getNinetyDiscount() + rewardFactor.getEightyDiscount() + rewardFactor.getDoubleAward();
        int stepCount = currentRewardStep.getTwoBless() + currentRewardStep.getFiveBless() + currentRewardStep.getTenBless() + currentRewardStep.getNinetyDiscount() + currentRewardStep.getEightyDiscount() + currentRewardStep.getDoubleAward();

        if (currCount >= stepCount) {  //达到阶梯次数，如果奖池存在3倍卡，直接返回
            logger.info("达到阶梯次数，当前用户总次数={},当前阶梯总次数={}", currCount, stepCount);
            if (rewardIdList.contains(AwardWeigntEnum.THREE_AWARD.getAwardId())) {
                logger.info("奖池存在3倍卡，该用户将获得3倍卡奖品");
                List<AwardWeigntEnum> threeAward = new ArrayList<>();
                threeAward.add(AwardWeigntEnum.THREE_AWARD);
                return threeAward;
            }
        }

        int tCount = 0;
        int tTotal = 0;

        int totalWeight = 0;


        AwardWeigntEnum lastAwardWeight = null;
        int lastAwardWeightValue = 0;


        for (AwardWeigntEnum awardWeigntEnum : awardWeigntEnumList) {
            if(awardWeigntEnum.getAwardId().equals(AwardWeigntEnum.TWO_BLESS.getAwardId())){

                awardWeigntEnum.setValue(currentRewardStep.getTwoBless() - rewardFactor.getTwoBless());

                logger.info("2元福包的权重设置={}", awardWeigntEnum.getValue());

                tCount = rewardFactor.getTwoBless();
                tTotal = currentRewardStep.getTwoBless();

            }else if(awardWeigntEnum.getAwardId().equals(AwardWeigntEnum.FIVE_BLESS.getAwardId())){

                awardWeigntEnum.setValue(currentRewardStep.getFiveBless() - rewardFactor.getFiveBless());

                logger.info("5元福包的权重设置={}", awardWeigntEnum.getValue());

                tCount = rewardFactor.getFiveBless();
                tTotal = currentRewardStep.getFiveBless();

            }else if(awardWeigntEnum.getAwardId().equals(AwardWeigntEnum.TEN_BLESS.getAwardId())){

                awardWeigntEnum.setValue(currentRewardStep.getTenBless() - rewardFactor.getTenBless());

                logger.info("10元福包的权重设置={}", awardWeigntEnum.getValue());

                tCount = rewardFactor.getTenBless();
                tTotal = currentRewardStep.getTenBless();

            }else if(awardWeigntEnum.getAwardId().equals(AwardWeigntEnum.NINE_DISCOUNT.getAwardId())){

                awardWeigntEnum.setValue(currentRewardStep.getNinetyDiscount() - rewardFactor.getNinetyDiscount());

                logger.info("九折卡的权重设置={}", awardWeigntEnum.getValue());

                tCount = rewardFactor.getNinetyDiscount();
                tTotal = currentRewardStep.getNinetyDiscount();

            }else if(awardWeigntEnum.getAwardId().equals(AwardWeigntEnum.EIGHT_DISCOUNT.getAwardId())){

                awardWeigntEnum.setValue(currentRewardStep.getEightyDiscount() - rewardFactor.getEightyDiscount());

                logger.info("八折卡的权重设置={}", awardWeigntEnum.getValue());

                tCount = rewardFactor.getEightyDiscount();
                tTotal = currentRewardStep.getEightyDiscount();

            }else if(awardWeigntEnum.getAwardId().equals(AwardWeigntEnum.DOUBLE_AWARD.getAwardId())){

                awardWeigntEnum.setValue(currentRewardStep.getDoubleAward() - rewardFactor.getDoubleAward());

                logger.info("翻倍卡的权重设置={}", awardWeigntEnum.getValue());

                tCount = rewardFactor.getDoubleAward();
                tTotal = currentRewardStep.getDoubleAward();

            }

            if (awardWeigntEnum.getValue() < 0) {

                logger.error("奖品id={}的奖品，中奖次数已经超过阶梯设置的奖品次数", awardWeigntEnum.getAwardId());
                awardWeigntEnum.setValue(0);
            }


            //判断是否和上次的奖品一样
            if(awardWeigntEnum.getAwardId().equals(rewardFactor.getLastRewardId())){

                lastAwardWeight = awardWeigntEnum;
                lastAwardWeightValue = awardWeigntEnum.getValue();

                //同一奖品超过3次，则权重置为0
                if (rewardFactor.getLastRewardCount() > 3) {
                    logger.error("奖品id={}的奖品，当前连续中奖超过3次，权重设置为0", awardWeigntEnum.getAwardId());
                    awardWeigntEnum.setValue(0);
                }

            }

            if (currCount > 0) {
                double x = tTotal / stepCount;
                double y = tCount / currCount;

                if (y > x) {
                    logger.error("奖品id={}的奖品，当前中奖太频繁，权重设置为0", awardWeigntEnum.getAwardId());
                    awardWeigntEnum.setValue(0);
                }
            }


            totalWeight += awardWeigntEnum.getValue();

        }

        //总权重为0
        if (totalWeight == 0) {
            logger.error("总权重为0，返回空集合");
            awardWeigntEnumList = new ArrayList<>();

            if (lastAwardWeightValue > 0) {
                lastAwardWeight.setValue(lastAwardWeightValue);
                awardWeigntEnumList.add(lastAwardWeight);
            }

            return awardWeigntEnumList;
        }

        return awardWeigntEnumList;
    }

    /**
     * <p>Title:      根据用户id获取奖品id,如果规则池中存在可以的奖品，则直接赋予该用户，否则按指标进行随机 </p>
     * <p>Description  </p>
     *
     * @Author zhangft
     * @CreateDate 2018/7/12 上午10:49
     */
    public String getRewardIdByUserId(String userId, List<String> rewardIdList) {

        //1 个人规则表查询，有的话直接随机一个返回，并更新使用状态
        //2 查询计算公司盈利情况
        //3 查询计算用户盈利情况
        //4 查询用户幸运值【分等级】
        //5 根据幸运值等级和公司盈利及个人盈利，不同幸运值等级对应不同的奖品，不同的奖品对应不同的概率 ，把不同情况分成不同的奖品组合【奖品组合等级】
        //6 根据随机数和奖品组合，奖品组合根据每个奖品的中奖概率重新计算权重，然后使用随机数生成一个奖品


        resetWeight();   //重新设置权重


        //查询用户卡包记录
        List<RewardOfUser> rewardOfUserList = rewardOfUserMapper.selectByuserId(userId);

        if (rewardOfUserList == null || rewardOfUserList.size() == 0) {  //新用户，八折卡或九折卡

            logger.info("游戏新用户随机八折卡和九折卡");

            List<AwardWeigntEnum> awardWeigntEnumList = new ArrayList<>();

            if (rewardIdList.contains(AwardWeigntEnum.NINE_DISCOUNT.getAwardId())) {
                awardWeigntEnumList.add(AwardWeigntEnum.NINE_DISCOUNT);
            }

            if (rewardIdList.contains(AwardWeigntEnum.EIGHT_DISCOUNT.getAwardId())) {
                awardWeigntEnumList.add(AwardWeigntEnum.EIGHT_DISCOUNT);
            }

            return getRewardIdByCombine(awardWeigntEnumList);
        }

        //个人幸运值
        RewardFactor rewardFactor = rewardFactorMapper.selectByPrimaryKey(userId);


        List<AwardWeigntEnum> awardWeigntEnumList = new ArrayList<>();

        if (rewardIdList.contains(AwardWeigntEnum.TWO_BLESS.getAwardId())) {
             awardWeigntEnumList.add(AwardWeigntEnum.TWO_BLESS);
        }

        if(rewardIdList.contains(AwardWeigntEnum.FIVE_BLESS.getAwardId())) {
             awardWeigntEnumList.add(AwardWeigntEnum.FIVE_BLESS);
        }

        if (rewardIdList.contains(AwardWeigntEnum.TEN_BLESS.getAwardId())) {
             awardWeigntEnumList.add(AwardWeigntEnum.TEN_BLESS);
        }

        if(rewardIdList.contains(AwardWeigntEnum.NINE_DISCOUNT.getAwardId())) {
            awardWeigntEnumList.add(AwardWeigntEnum.NINE_DISCOUNT);
        }

        if(rewardIdList.contains(AwardWeigntEnum.EIGHT_DISCOUNT.getAwardId())) {
            awardWeigntEnumList.add(AwardWeigntEnum.EIGHT_DISCOUNT);
        }

        if(rewardIdList.contains(AwardWeigntEnum.DOUBLE_AWARD.getAwardId())) {
            awardWeigntEnumList.add(AwardWeigntEnum.DOUBLE_AWARD);
        }


        awardWeigntEnumList = calWeight(awardWeigntEnumList, rewardFactor, rewardIdList);

        return getRewardIdByCombine(awardWeigntEnumList);


//        if (rewardFactor.getLuckyValue() >= 0 && rewardFactor.getLuckyValue() < 100) {
//            List<AwardWeigntEnum> awardWeigntEnumList = new ArrayList<>();
//
//            if(rewardFactor.getLuckyValue()>3 && isLuckyNum(rewardFactor.getLuckyValue())){
//
//                if(rewardIdList.contains(AwardWeigntEnum.NINE_DISCOUNT.getAwardId())) {
//                    awardWeigntEnumList.add(AwardWeigntEnum.NINE_DISCOUNT);
//                }
//
//                if(rewardIdList.contains(AwardWeigntEnum.EIGHT_DISCOUNT.getAwardId())) {
//                    awardWeigntEnumList.add(AwardWeigntEnum.EIGHT_DISCOUNT);
//                }
//
//                if(rewardIdList.contains(AwardWeigntEnum.DOUBLE_AWARD.getAwardId())) {
//                    awardWeigntEnumList.add(AwardWeigntEnum.DOUBLE_AWARD);
//                }
//
//                return getRewardIdByCombine(awardWeigntEnumList);
//
//             }else{
//
//                 if (rewardIdList.contains(AwardWeigntEnum.TWO_BLESS.getAwardId())) {
//                     awardWeigntEnumList.add(AwardWeigntEnum.TWO_BLESS);
//                 }
//
//                 if(rewardIdList.contains(AwardWeigntEnum.FIVE_BLESS.getAwardId())) {
//                     awardWeigntEnumList.add(AwardWeigntEnum.FIVE_BLESS);
//                 }
//
//                 if (rewardIdList.contains(AwardWeigntEnum.TEN_BLESS.getAwardId())) {
//                     awardWeigntEnumList.add(AwardWeigntEnum.TEN_BLESS);
//                 }
//
//                 return getRewardIdByCombine(awardWeigntEnumList);
//             }
//        }
//
//
//        if (rewardFactor.getLuckyValue() == 100) {
//            logger.info("幸运值大于200");
//
//            List<AwardWeigntEnum> awardWeigntEnumList = new ArrayList<>();
//
//            if (rewardIdList.contains(AwardWeigntEnum.TWO_BLESS.getAwardId())) {
//                awardWeigntEnumList.add(AwardWeigntEnum.TWO_BLESS);
//            }
//
//            if (rewardIdList.contains(AwardWeigntEnum.FIVE_BLESS.getAwardId())) {
//                awardWeigntEnumList.add(AwardWeigntEnum.FIVE_BLESS);
//            }
//
//            if (rewardIdList.contains(AwardWeigntEnum.EIGHT_DISCOUNT.getAwardId())) {
//                awardWeigntEnumList.add(AwardWeigntEnum.EIGHT_DISCOUNT);
//            }
//
//            if (rewardIdList.contains(AwardWeigntEnum.THREE_AWARD.getAwardId())) {
//
//                if (rewardFactor.getLuckyNumber() >= 200) {
//                    return AwardWeigntEnum.THREE_AWARD.getAwardId();
//                }
//
//                if (rewardFactor.getLuckyNumber() > 100) {
//                    int w = rewardFactor.getLuckyNumber() % 100;
//                    int x = w / 10 + 1;
//                    AwardWeigntEnum.THREE_AWARD.setValue(AwardWeigntEnum.THREE_AWARD.getValue() * x * 10);
//                }
//
//                awardWeigntEnumList.add(AwardWeigntEnum.THREE_AWARD);
//            }
//
//
//            return getRewardIdByCombine(awardWeigntEnumList);
//        }

//        logger.info("没有进入幸运值等级方法");
//        return AwardWeigntEnum.TWO_BLESS.getAwardId();

    }

    private boolean isLuckyNum(int num){
        if(isPrimeNumber(num)){
//            String numStr = "" + num;
//            if (numStr.endsWith("5") || numStr.endsWith("7")) {
//                return false;
//            }else{
//                return true;
//            }
            return true;
        }else{
            return false;
        }
    }

    //判断一个数是否是质数（素数）
    private boolean isPrimeNumber(int num){

        if(num == 2) return true;//2特殊处理
        if(num < 2 || num % 2 == 0) return false;//识别小于2的数和偶数
        for(int i=3; i<=Math.sqrt(num); i+=2){
            if(num % i == 0){//识别被奇数整除
                return false;
            }
        }

        return true;
    }
    
    /**
     * <p>Title:      根据组合奖品和随机数得到奖品id </p>
     * <p>Description 将中奖概率大的奖品放在数组前边【非必须】 </p>
     *
     * @Author        zhangft
     * @CreateDate    2018/7/12 下午1:51
     */
    public static String getRewardIdByCombine(List<AwardWeigntEnum> awardWeigntEnumList){


        if (awardWeigntEnumList.size() == 0) {
            return AwardWeigntEnum.TWO_BLESS.getAwardId();
        }

        int sum = 0;  //权重总数

        for (AwardWeigntEnum awardWeigntEnum : awardWeigntEnumList) {
            logger.info("奖品id={},权重值={}", awardWeigntEnum.getAwardId(), awardWeigntEnum.getValue());
            sum += awardWeigntEnum.getValue();
        }

        int randNum = new Random().nextInt(sum); //在权重总数范围内随机一个数

        Integer m = 0;

        for (AwardWeigntEnum awardWeigntEnum : awardWeigntEnumList) {

            if (m <= randNum && randNum < m + awardWeigntEnum.getValue()) {
                logger.info("随机生成的奖品id={}", awardWeigntEnum.getAwardId());
                return awardWeigntEnum.getAwardId();
            }

            m += awardWeigntEnum.getValue();

        }

        logger.info("未成功返回奖品id,不存在的情况，打印出来说明代码出问题了");
        return null;
    }


//    public static void main(String[] args) {
//        GamePrincipal gp = new GamePrincipal();
//
//        System.out.println(gp.isLuckyNum(75));
//    }

    
}
