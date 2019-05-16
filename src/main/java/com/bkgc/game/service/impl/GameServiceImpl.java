package com.bkgc.game.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.award.LottoAutoawardOrder;
import com.bkgc.bean.game.*;
import com.bkgc.bean.game.dto.RewardOfUserDto;
import com.bkgc.bean.pay.InOutWayEnum;
import com.bkgc.bean.user.AuthMember;
import com.bkgc.bless.config.Config;
import com.bkgc.bless.config.SignClient;
import com.bkgc.bless.consumer.GtsFeignService;
import com.bkgc.bless.consumer.PaymentFeignService;
import com.bkgc.bless.mapper.AuthMemberMapper;
import com.bkgc.bless.service.impl.GenerateOrderNoService;
import com.bkgc.bless.utils.Reward4Position;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.RWrapper;
import com.bkgc.common.utils.ResultUtil;
import com.bkgc.common.utils.StringUtil;
import com.bkgc.common.utils.WrapperUtil;
import com.bkgc.game.mapper.*;
import com.bkgc.game.model.enums.AwardWeigntEnum;
import com.bkgc.game.model.vo.GameBlessVo;
import com.bkgc.game.model.vo.LottoAutoawardOrderExpend;
import com.bkgc.game.principal.GamePrincipal;
import com.bkgc.game.service.AwardService;
import com.bkgc.game.service.GameService;
import com.bkgc.game.service.RewardFactorService;
import com.bkgc.game.socket.StartMessageQueue;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xiaoleilu.hutool.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class GameServiceImpl implements GameService {

    @Autowired
    RewardFactorMapper rewardFactorMapper;

    @Autowired
    DiscountCardUsageMapper discountCardUsageMapper;

    @Autowired
    AwardService awardService;

    @Autowired
    AuthAddressMapper authAddressMapper;

    @Autowired
    AwardCardUsageMapper awardCardUsageMapper;

    @Autowired
    RewardOfUserMapper rewardOfUserMapper;

    @Autowired
    RewardMapper rewardMapper;

    @Resource
    private Config config;

    @Resource
    private SignClient signClient;

    @Autowired
    GamePrincipal gamePrincipal;

    @Autowired
    MailedAwardMapper mailedAwardMapper;

    @Autowired
    private RewardPoolMapper rewardPoolMapper;

    @Autowired
    private AuthMemberMapper authMemberMapper;

    @Autowired
    private PaymentFeignService paymentFeignService;

    @Autowired
    private GtsFeignService gtsFeignService;

    @Autowired
    private StartMessageQueue startMessageQueue;

    @Autowired
    private RewardFactorService rewardFactorService;

    @Autowired
    private GenerateOrderNoService generateOrderNoService;


    Reward4Position reward4Position = Reward4Position.getInstance();

    private void setRewardFactorCount(String rewardId, JSONObject rewardFactorJson){

         if(rewardId.equals(AwardWeigntEnum.TWO_BLESS.getAwardId())){
             rewardFactorJson.put("twoBless", rewardFactorJson.getIntValue("twoBless") + 1);
         }else if(rewardId.equals(AwardWeigntEnum.FIVE_BLESS.getAwardId())){
             rewardFactorJson.put("fiveBless", rewardFactorJson.getIntValue("fiveBless") + 1);
         }else if(rewardId.equals(AwardWeigntEnum.TEN_BLESS.getAwardId())){
             rewardFactorJson.put("tenBless", rewardFactorJson.getIntValue("tenBless") + 1);
         }else if(rewardId.equals(AwardWeigntEnum.NINE_DISCOUNT.getAwardId())){
             rewardFactorJson.put("ninetyDiscount", rewardFactorJson.getIntValue("ninetyDiscount") + 1);
         }else if(rewardId.equals(AwardWeigntEnum.EIGHT_DISCOUNT.getAwardId())){
             rewardFactorJson.put("eightyDiscount", rewardFactorJson.getIntValue("eightyDiscount") + 1);
         }else if(rewardId.equals(AwardWeigntEnum.DOUBLE_AWARD.getAwardId())){
             rewardFactorJson.put("doubleAward", rewardFactorJson.getIntValue("doubleAward") + 1);
         }

         if(rewardId.equals(rewardFactorJson.getString("lastRewardId"))){   //和上次中奖奖品相同，累计
             rewardFactorJson.put("lastRewardCount", rewardFactorJson.getIntValue("lastRewardCount") + 1);
         }else{  //和上次奖品不同，设置为1
             rewardFactorJson.put("lastRewardCount", 1);
             rewardFactorJson.put("lastRewardId", rewardId);
         }

    }


    /**
     * <p>Title:      使用gts重写游戏开始方法 </p>
     * <p>Description  </p>
     *
     * @Author zhangft
     * @CreateDate 2018/7/17 下午1:53
     */
    public String gameStart(String userId) {

        JSONObject totalRet = new JSONObject();

        JSONObject payJson = new JSONObject();
        payJson.put("userId", userId);

        BigDecimal amount = new BigDecimal(config.getGameAmount());
        amount = amount.multiply(new BigDecimal("-1"));
        payJson.put("amount", amount);

        //payJson.put("orderNo", UUID.randomUUID().toString());
        payJson.put("orderNo", generateOrderNoService.generate());
        payJson.put("inOutWay", InOutWayEnum.Out_Bless_Game_Pay.getKey());

        totalRet.put("payJson", payJson);

        //获得奖池信息,将奖池信息传进去，防止中奖的奖品没有了
        List<RewardPool> rewardPoolList = rewardPoolMapper.selectAll();

        //计算出当前用户可获得的奖品Id
        JSONObject rewardJson = gamePrincipal.match(userId, rewardPoolList);

        totalRet.put("rewardJson", rewardJson);

        String rewardId = rewardJson.getString("rewardId");
        log.info("匹配用户的奖品id={}", rewardId);
        String potision = "1";

        Map<String, String> map = reward4Position.getMap();
        Set<String> keySet = map.keySet();
        log.info("奖品数量={}", keySet.size());
        for (String s : keySet) {
            if (rewardId.equals(s)) {
                String position = map.get(s);
                String[] split = position.split(",");
                log.info("数组大小={}", split.length);
                Random random = new Random();
                int nextInt = random.nextInt(split.length);
                log.info("随机数={}", nextInt);
                potision = split[nextInt];
                log.info("potision={}", position);
                break;
            }
        }
        //获取奖品信息
        Reward reward = rewardMapper.selectByPrimaryKey(rewardId);
        //保存奖品信息
        JSONObject awardInfoJson = saveReward(userId, rewardId);
        totalRet.put("awardInfoJson", awardInfoJson);

        JSONObject dataJson = new JSONObject();

        dataJson.put("guid", userId);
        dataJson.put("position", potision);
        dataJson.put("rewardId", rewardId);
        dataJson.put("rewardCode", reward.getCode());
        dataJson.put("rewardName", reward.getName());
        dataJson.put("rewardType", reward.getType());
        dataJson.put("cardId", awardInfoJson.getString("cardId"));//cardId

        RewardFactor rewardFactor = rewardFactorService.getByUserId(userId);

        JSONObject rewardFactorJson = new JSONObject();

        if (rewardFactor != null) {

            if (rewardFactor.getLuckyValue() < 100) {
                rewardFactorJson.put("luckyValue", rewardFactor.getLuckyValue() + 1);
            } else {
                rewardFactorJson.put("luckyValue", rewardFactor.getLuckyValue());
            }

            rewardFactorJson.put("luckyNumber", rewardFactor.getLuckyNumber() + 1);


            rewardFactorJson.put("twoBless", rewardFactor.getTwoBless());
            rewardFactorJson.put("fiveBless", rewardFactor.getFiveBless());
            rewardFactorJson.put("tenBless", rewardFactor.getTenBless());
            rewardFactorJson.put("ninetyDiscount", rewardFactor.getNinetyDiscount());
            rewardFactorJson.put("eightyDiscount", rewardFactor.getEightyDiscount());
            rewardFactorJson.put("doubleAward", rewardFactor.getDoubleAward());

            rewardFactorJson.put("threeAwardCount", rewardFactor.getThreeAwardCount());
            rewardFactorJson.put("lastThreeAward", rewardFactor.getLastThreeAward());

            rewardFactorJson.put("lastRewardId", rewardFactor.getLastRewardId());
            rewardFactorJson.put("lastRewardCount", rewardFactor.getLastRewardCount());

            log.info("设置因子表之前的数据为={}", rewardFactorJson.toString());
            setRewardFactorCount(rewardId, rewardFactorJson);
            log.info("设置因子表之后的数据为={}", rewardFactorJson.toString());

            //如果中大奖，将幸运值清0
            if (rewardId.equals(AwardWeigntEnum.THREE_AWARD.getAwardId())
                    || rewardId.equals(AwardWeigntEnum.IPHONE_X.getAwardId())) {
                log.info("中得大奖，幸运值清0");
                rewardFactorJson.put("luckyValue", 0);
                rewardFactorJson.put("luckyNumber", 0);

                rewardFactorJson.put("twoBless", 0);
                rewardFactorJson.put("fiveBless", 0);
                rewardFactorJson.put("tenBless", 0);
                rewardFactorJson.put("ninetyDiscount", 0);
                rewardFactorJson.put("eightyDiscount", 0);
                rewardFactorJson.put("doubleAward", 0);

                rewardFactorJson.put("threeAwardCount", rewardFactor.getThreeAwardCount() + 1);
                rewardFactorJson.put("lastThreeAward", DateUtil.now());

                log.info("中3倍卡后，因子表的数据={}", rewardFactorJson.toString());

                rewardFactorJson.put("lastRewardId", rewardId);
                rewardFactorJson.put("lastRewardCount", 1);
            }

            rewardFactorJson.put("userId", userId);
            rewardFactorJson.put("luckySum", rewardFactor.getLuckySum() + 1);
            rewardFactorJson.put("gameTimes", rewardFactor.getGameTimes() + 1);
            rewardFactorJson.put("gameRewardPrice", rewardFactor.getGameRewardPrice().add(reward.getPrice()));
            rewardFactorJson.put("reward_factor_of_user", "2");

            totalRet.put("rewardFactorJson", rewardFactorJson);
        } else {

            rewardFactorJson.put("userId", userId);
            rewardFactorJson.put("gameRewardPrice", reward.getPrice());
            rewardFactorJson.put("reward_factor_of_user", "1");
            rewardFactorJson.put("luckyNumber", 1);


            rewardFactorJson.put("twoBless", 0);
            rewardFactorJson.put("fiveBless", 0);
            rewardFactorJson.put("tenBless", 0);
            rewardFactorJson.put("ninetyDiscount", 0);
            rewardFactorJson.put("eightyDiscount", 0);
            rewardFactorJson.put("doubleAward", 0);

            rewardFactorJson.put("lastRewardId", "");
            rewardFactorJson.put("lastRewardCount", 1);

            log.info("设置因子表之前的数据为={}", rewardFactorJson.toString());
            setRewardFactorCount(rewardId, rewardFactorJson);
            log.info("设置因子表之后的数据为={}", rewardFactorJson.toString());

            totalRet.put("rewardFactorJson", rewardFactorJson);
        }


        //1.封装参数，调用gts,把账户余额和幸运值返回即可，其余返回值在此方法封装，
        //2.把是不是要插入或操作表写在参数中，gts直接判断就可以，参数为 表名，值=1/0,必须操作的就无需传递,gts也无需判断
        //3.把参数分开，多个json放在一个json中，这样比较清晰，key按照业务取名
        RWrapper<JSONObject> ret = gtsFeignService.gameStart(totalRet);
        log.info("返回结果={}", ret.toString());

        if ("1000".equals(ret.getCode())) {
            JSONObject temp = ret.getData();

            dataJson.put("blessamount", temp.getBigDecimal("blessamount"));
            dataJson.put("luckyValue", temp.getIntValue("luckyValue"));

            log.info("返回data结果为：" + temp.toString());

            //将获奖人信息加入前端展示
            AuthMember authMember = authMemberMapper.selectByguid(userId);
            if (authMember.getPhone() != null || authMember.getPhone().length() == 11) {
                startMessageQueue.addMessage("[" + authMember.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2") + "]夺得" + reward.getName());
            }

            return WrapperUtil.ok(dataJson).toString();
        } else {
            log.error("gts返回错误信息={}", ret.toString());
            return ret.toString();
        }


    }

    /**
     * 领取奖品, 夺宝游戏一轮结束后需要保存用户获得的奖品至数据库.
     * 奖品不同,领取奖品的流程也不同,根据设计的奖品类别领取流程实现该方法
     *
     * @param guid     用户id
     * @param rewardId 奖品id
     * @return
     * @throws Exception
     */
    @Override
    public JSONObject saveReward(String guid, String rewardId) {

        RewardPool rewardPool = rewardPoolMapper.selectRewardPoolByRewardId(rewardId);

        JSONObject retJson = new JSONObject();

        String cardId = UUID.randomUUID().toString();

        retJson.put("cardId", cardId);
        retJson.put("rewardId", rewardId);
        retJson.put("userId", guid);

        Reward reward = rewardMapper.selectByPrimaryKey(rewardId);
        retJson.put("rewardCode", reward.getCode());
        retJson.put("rewardName", reward.getName());
        retJson.put("rewardType", reward.getType());
        retJson.put("rewardPoolNum", rewardPool.getRewardPoolNum());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");

        Calendar calendarExpire = Calendar.getInstance();//过期时间
        calendarExpire.add(Calendar.DATE, reward.getExpireDay());
        String expireTime = simpleDateFormat.format(calendarExpire.getTime());
        Date expireDate = null;
        try {
            expireDate = simpleDateFormat.parse(expireTime);
        } catch (Exception e) {
            log.error("过期时间转换出现异常，异常信息={}", e.getMessage(), e);
        }
        retJson.put("cardExpired", expireDate);
        if (1 == reward.getType()) {//福包

            if ("two_bless".equals(reward.getCode())) {
                retJson.put("amount", "2");
            } else if ("five_bless".equals(reward.getCode())) {
                retJson.put("amount", "5");
            } else {
                retJson.put("amount", "10");
            }
            retJson.put("userId", guid);
            //retJson.put("orderNo", UUID.randomUUID().toString());
            retJson.put("orderNo", generateOrderNoService.generate());
            retJson.put("orderCardId", cardId);
            retJson.put("orderCardType", reward.getType().toString());
            retJson.put("inOutWay", InOutWayEnum.In_Game_Award_Bless.getKey());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new java.util.Date());
            calendar.add(Calendar.SECOND, 10);  //加10s，防止账单显示顺序问题

            retJson.put("useTime", calendar.getTime());
            retJson.put("createTime", calendar.getTime());

            retJson.put("cardStatus", 1);
            retJson.put("cardId", cardId);

            retJson.put("auth_account", "1");

        } else {//非福包
            retJson.put("cardStatus", 0);
            retJson.put("cardId", cardId);
            retJson.put("useTime", "");
        }

        return retJson;

    }

    /**
     * 打折卡使用方法,具体流程参考文档
     *
     * @param param
     * @return
     * @throws Exception
     */
    @Override
    public String useDiscountCard(JSONObject param) {
        log.info("使用打折卡 接口：参数为：" + param.toString());
        String prePayNo = param.getString("prePayNo");//订单号
        String code = param.getString("code");//奖品类型
        String key = param.getString("key");//支付密码
        BigDecimal disCountAmount = param.getBigDecimal("disCountAmount");//折扣金额
        BigDecimal payAmount = param.getBigDecimal("payAmount");//实际支付金额
        String guid = param.getString("guid");//用户id
        if (StringUtil.isNullOrEmpty(key, prePayNo, guid)) {
            return WrapperUtil.error(ResultCodeEnum.PARAM_MISS, "guid,key,prePayNo不能为空").toString();
        }
        //Map<String, Object> disCountParams = new LinkedHashMap<String, Object>();
        JSONObject disCountParams = new JSONObject();
        disCountParams.put("userId", guid);//用户
        disCountParams.put("key", key);//支付密码
        disCountParams.put("prePayNo", prePayNo);//预支付单号
        disCountParams.put("clientID", signClient.getClientId());
        disCountParams.put("nonce_str", UUID.randomUUID().toString());


        if (StringUtil.isNullOrEmpty(code)) {
            log.info("未使用打折卡福金支付");
            JSONObject jsonData = paymentFeignService.blessPay(disCountParams).getData();
            return jsonData.toString();
        }


        if (StringUtil.isNullOrEmpty(disCountAmount.toString(), payAmount.toString())) {
            return WrapperUtil.error(ResultCodeEnum.PARAM_MISS, "code,disCountAmount,payAmount 参数 必须同时存在").toString();
        }
        RewardOfUser params = new RewardOfUser();
        params.setRewardCode(code);
        params.setStatus(0);
        params.setUserId(guid);
        List<RewardOfUser> list = rewardOfUserMapper.selectByObject(params);
        if (list.size() == 0) {
            return WrapperUtil.error(ResultCodeEnum.ERR_41038).toString();
        }
        RewardOfUser rewardOfUser = list.get(list.size() - 1);//列表按照领取时间倒叙排列，获取过期时间最近的一条记录
        Reward reward = rewardMapper.selectByPrimaryKey(rewardOfUser.getRewardId());
        disCountParams.put("discountAmount", disCountAmount.toString());//折扣金额
        disCountParams.put("orderCardId", rewardOfUser.getId());//reward_of_user 主键Id（待定）
        disCountParams.put("orderCardType", reward.getType().toString());//折扣卡类型
        disCountParams.put("payAmount", payAmount.toString());//实际支付金额
        //调用payment折扣方法,待写
        log.info("调用payment 福金支付 参数={}", disCountParams.toString());
        //JSONObject jsonData = HttpUtils.sendPost4Object(config.getPayment_url() + config.getBless_pay(), disCountParams);
        RWrapper<JSONObject> jsonData = paymentFeignService.blessPay(disCountParams);
        if (!"1000".equals(jsonData.getCode())) {
            return jsonData.toString();
        }

        DiscountCardUsage d = new DiscountCardUsage();
        //折扣金额
        d.setDiscountAmount(disCountAmount.longValue());
        //折扣订单号
        d.setDiscountOrderNo(prePayNo);
        //折扣用户
        d.setUserId(guid);
        //需要打折的原始订单总金额
        BigDecimal add = disCountAmount.add(payAmount);
        d.setDiscountOrderOriginalAmount(add.longValue());
        //奖品打折卡id
        d.setRewardId(rewardOfUser.getRewardId());
        //卡包Id
        d.setCardId(rewardOfUser.getId());
        //状态 1：成功 0 ：失败
        d.setPayStatus(1);
        d.setId(UUID.randomUUID().toString());
        discountCardUsageMapper.insertSelective(d);

        rewardOfUser.setStatus(1);//该打折卡已使用，将状态置为1
        rewardOfUser.setUseTime(new Date());
        rewardOfUserMapper.updateByPrimaryKey(rewardOfUser);

        //更改用户获奖规则因子表
        RewardFactor rewardFactor = rewardFactorService.getByUserId(guid);
        rewardFactor.setGameRewardPrice(rewardFactor.getGameRewardPrice().add(disCountAmount));
        rewardFactorMapper.updateByPrimaryKeySelective(rewardFactor);

        JSONObject returnData = new JSONObject();
        returnData.put("code", 1000);
        returnData.put("msg", "打折卡使用成功");
        return returnData.toString();
    }


    /**
     * 奖金翻倍卡使用方法, 具体流程参考文档
     *
     * @param param
     * @return
     * @throws Exception
     */
/*    @Override
    @Transactional
    public String useAwardCard(JSONObject param) {
        //奖品类型 卡包Id 订单 原始订单金额
        String code = param.getString("code");
        String cardId = param.getString("cardId");
        String awardNo = param.getString("awardNo");
        String awardAmount = param.getString("awardAmount");
        String guid = param.getString("guid");
        if (StringUtil.isNullOrEmpty(awardNo, awardAmount, guid, cardId)) {
            throw new BusinessException(ResultCodeEnum.ERR_41003, "奖金翻倍卡的参数不能为空!");
        }
        RewardOfUser rewardOfUser = rewardOfUserMapper.selectByPrimaryKey(cardId);
        log.info("根据cardId={}，查询数据RewardOfUser={}", cardId, rewardOfUser);
        //检查卡是否可用
        checkRewardOfUser(rewardOfUser);

        Reward reward = rewardMapper.selectByPrimaryKey(rewardOfUser.getRewardId());
        log.info("根据奖品id={}，查询奖品reward={}", rewardOfUser.getRewardId(), reward);
        if (reward == null) {
            throw new BusinessException(ResultCodeEnum.FAIL, "奖品不存在!");
        }
        //检查金额
        Long multipleAmount = checkDoublingMoney(reward.getCode(), awardAmount);

        //向奖金翻倍卡使用表中添加数据
        AwardCardUsage awardCardUsage = new AwardCardUsage(UUID.randomUUID().toString(), rewardOfUser.getId(), awardNo, Long.parseLong(awardAmount), new Date(), guid, rewardOfUser.getRewardId());
        awardCardUsage.setDescription(code + awardNo);
        awardCardUsage.setMultipleAmount(multipleAmount);
        int insertStatus = awardCardUsageMapper.insertSelective(awardCardUsage);

        if (insertStatus != 1) {
            throw new BusinessException(ResultCodeEnum.ERR_41040, "数据写入数据库失败!");
        }

        //账户加钱
        JSONObject map = new JSONObject();
        map.put("userId", guid);
        map.put("orderNo", awardNo);
        map.put("amount", multipleAmount.toString());
        map.put("orderCardId", cardId);
        //1福包、2购彩折扣卡、3奖金翻倍卡、4实物奖品
        map.put("orderCardType", reward.getType().toString());
        RWrapper<JSONObject> data = paymentFeignService.doubleAward(map);
        if (!"1000".equals(data.getCode())) {
            throw new BusinessException(ResultCodeEnum.FAIL, data.getMsg());
        }

        rewardOfUser.setStatus(1);
        int updateStatus = rewardOfUserMapper.updateByPrimaryKey(rewardOfUser);
        if (updateStatus != 1) {
            throw new BusinessException(ResultCodeEnum.ERR_41040, "数据写入数据库失败!");
        }

        //RewardFactor rewardFactor = RewardFactorServiceImpl.getByUserId(guid);
        RewardFactor rewardFactor = rewardFactorMapper.selectByPrimaryKey(guid);
        if (rewardFactor == null) {
            throw new BusinessException(ResultCodeEnum.FAIL, "在因子表中没有到查询数据!");
        }
        rewardFactor.setGameRewardPrice(rewardFactor.getGameRewardPrice().add(new BigDecimal(multipleAmount)));
        int u = rewardFactorMapper.updateByPrimaryKeySelective(rewardFactor);
        if (u != 1) {
            throw new BusinessException(ResultCodeEnum.ERR_41040, "数据写入数据库失败!");
        }
        return data.toString();
    }*/


    /**
     * 奖金翻倍卡使用
     *
     * @param param
     * @return
     */
    @Override
    public String useAwardCard(JSONObject param) {
        //卡包Id 订单 原始订单金额 guid 翻倍后实际多得的金额
        String cardId = param.getString("cardId");
        String orderNo = param.getString("orderNo");
        String awardAmount = param.getString("awardAmount");
        String guid = param.getString("guid");
        String multipleAmount = param.getString("multipleAmount");

        if (StringUtil.isNullOrEmpty(orderNo, awardAmount, guid, cardId, multipleAmount)) {
            throw new BusinessException(ResultCodeEnum.ERR_41003, "奖金翻倍卡的参数不能为空!");
        }
        RewardOfUser rewardOfUser = rewardOfUserMapper.selectByPrimaryKey(cardId);
        log.info("根据cardId={}，查询数据RewardOfUser={}", cardId, rewardOfUser);
        //检查卡是否可用 是否过期
        checkRewardOfUser(rewardOfUser);
        Reward reward = rewardMapper.selectByPrimaryKey(rewardOfUser.getRewardId());
        log.info("根据奖品id={}，查询奖品reward={}", rewardOfUser.getRewardId(), reward);
        if (reward == null) {
            throw new BusinessException(ResultCodeEnum.FAIL, "奖品不存在!");
        }
        //检查金额
        Long multipleMonery = checkDoublingMoney(reward.getCode(), awardAmount);
        if (!multipleAmount.equals(multipleMonery.toString())) {
            log.info("前端出过来的翻倍实际获得金额={},后台计算金额={}", multipleAmount, multipleMonery);
            throw new BusinessException(ResultCodeEnum.FAIL, "金额校验出问题!");
        }

        //调用payment账户加钱
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", guid);
        jsonObject.put("orderNo", orderNo);
        //翻倍获得的金额
        jsonObject.put("amount", multipleAmount);
        jsonObject.put("orderCardId", cardId);
        //1福包、2购彩折扣卡、3奖金翻倍卡、4实物奖品
        jsonObject.put("orderCardType", reward.getType());
        //原始金额
        jsonObject.put("originalAmount", awardAmount);
        jsonObject.put("rewardId", rewardOfUser.getRewardId());
        log.info("调用payment传的参数={}", jsonObject);
        RWrapper<JSONObject> data = paymentFeignService.doubleAward(jsonObject);
        log.info("调用payment返回数据={}", data);
        if (!"1000".equals(data.getCode())) {
            throw new BusinessException(ResultCodeEnum.FAIL, data.getMsg());
        }
        JSONObject json = JSON.parseObject(data.getData().toString());
        log.info("调用gts传的参数", json);
        RWrapper<JSONObject> resData = gtsFeignService.rewardMultiple(json);
        log.info("调用gts返回数据={}", resData);
        if (!"1000".equals(resData.getCode())) {
            throw new BusinessException(ResultCodeEnum.FAIL, resData.getMsg());
        }
        JSONObject resjson = new JSONObject();
        resjson.put("useDate", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        return WrapperUtil.ok(resjson).toString();
    }


    /**
     * 使用实物寄送的奖品,具体流程参考文档
     *
     * @param param
     * @return
     * @throws Exception
     */
    @Override
    public String useMailedAward(JSONObject param) {
        String recipient = param.getString("recipient");//收件人
        String phone = param.getString("phone");//收件人手机号
        String address = param.getString("address");//收件人具体地址
        String guid = param.getString("guid");//收件人Id
        String cardId = param.getString("cardId");//卡包Id
//		String rewardId = param.getString("rewardId");//奖品Id
        if (StringUtil.isNullOrEmpty(guid, cardId, address, phone, recipient)) {
            return WrapperUtil.error(ResultCodeEnum.PARAM_MISS, "guid,cardId,address,phone,recipient不能为空").toString();
//			return WrapperUtil.error(ResultCodeEnum.PARAM_MISS, "guid,cardId,address,phone,recipient不能为空");
        }
        RewardOfUser rewardOfUser = rewardOfUserMapper.selectByPrimaryKey(cardId);
        if (rewardOfUser.getStatus() == 1) {
            return WrapperUtil.error(ResultCodeEnum.CARD_USED, "该实物已填写订单信息，请等待快递").toString();
//			return WrapperUtil.error(ResultCodeEnum.CARD_USED, "该实物已填写订单信息，请等待快递");
        }
        Reward reward = rewardMapper.selectByPrimaryKey(rewardOfUser.getRewardId());
        RewardFactor rewardFactor = rewardFactorService.getByUserId(guid);
        if (rewardFactor != null) {
            rewardFactor.setGameRewardPrice(rewardFactor.getGameRewardPrice().add(reward.getPrice()));
            rewardFactorService.updateByUserId(rewardFactor);
        }

        MailedAward m = new MailedAward();
        m.setCardId(cardId);
        m.setUserId(guid);
        m.setRewardId(rewardOfUser.getRewardId());
        m.setCreateTime(new Date());
        m.setId(UUID.randomUUID().toString());
        m.setStatus(1);//等待配送
        m.setName(recipient);
        m.setAddress(address);
        m.setPhone(phone);
        int mcount = mailedAwardMapper.insertSelective(m);
        if (mcount != 1) {
            // to do
            return WrapperUtil.error(ResultCodeEnum.ERR_41040, "数据写入数据库失败").toString();
        }
        rewardOfUserMapper.updateByPrimaryKeySelective(rewardOfUser);

        RWrapper r = new RWrapper<>("1000", "请等待快递", null);
        return r.toString();
    }

    @Override
    public RWrapper getAddress(JSONObject param) {
        String guid = param.getString("guid");
        if (StringUtil.isNullOrEmpty(guid)) {
            RWrapper r = new RWrapper("2020", "请检查参数[guid]", null);
            return r;
        }
        AuthAddress a = authAddressMapper.selectByGuid(guid);
        RWrapper rw = new RWrapper("1000", "获取用户地址成功", a);
        return rw;
    }

    @Override
    public String getValidAwardList(JSONObject param) {
        String guid = param.getString("guid");
        String cardId = param.getString("cardId");
        String pageNum = param.getString("pageNum");
        String pageSize = param.getString("pageSize");
        if (StringUtil.isNullOrEmpty(guid, cardId, pageNum, pageSize)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "缺少必填参数!");
        }
        //获取该卡包
        RewardOfUser rewardOfUser = rewardOfUserMapper.selectByPrimaryKey(cardId);
        if (rewardOfUser == null) {
            throw new BusinessException(ResultCodeEnum.FAIL, "该卡包不存在！");
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date gainedTime = rewardOfUser.getGainedTime();
        Date expiredTime = rewardOfUser.getExpired();
        if (gainedTime == null || expiredTime == null) {
            throw new BusinessException(ResultCodeEnum.FAIL, "该卡包的获取时间或者过期时间不存在！");
        }
        String startTime = simpleDateFormat.format(gainedTime);
        String endTime = simpleDateFormat.format(expiredTime);
        pageNum = String.valueOf(Integer.parseInt(pageNum) + 1);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", guid);
        jsonObject.put("startTime", startTime);
        jsonObject.put("endTime", endTime);
        jsonObject.put("pageNum", pageNum);
        jsonObject.put("pageSize", pageSize);
        RWrapper<PageInfo<LottoAutoawardOrder>> rWrapper = awardService.getValidAwardList(jsonObject);
        log.info("调用award返回数据={}", rWrapper);
        if (!"1000".equals(rWrapper.getCode())) {
            return rWrapper.toString();
        }
        List<LottoAutoawardOrder> lottoAutoAwardOrderList = rWrapper.getData().getList();
        log.info("调用award返回数据个数={}", lottoAutoAwardOrderList.size());
        List<LottoAutoawardOrderExpend> validLottoAutoAwardOrderList = new ArrayList<>();
        LottoAutoawardOrderExpend lottoAutoawardOrderExpend;
        if (lottoAutoAwardOrderList.size() > 0 && lottoAutoAwardOrderList != null) {
            List<AwardCardUsage> awardCardUsage;
            for (LottoAutoawardOrder lottoAutoawardOrder : lottoAutoAwardOrderList) {
                awardCardUsage = awardCardUsageMapper.selectByOrderno(lottoAutoawardOrder.getOrderno());
                if (awardCardUsage.isEmpty()) {
                    lottoAutoawardOrderExpend = new LottoAutoawardOrderExpend();
                    lottoAutoawardOrderExpend.setTicketno(lottoAutoawardOrder.getTicketno());
                    //lottoAutoawardOrderExpend.setAwardtime(lottoAutoawardOrder.getAwardtime());
                    lottoAutoawardOrderExpend.setAwardtime(lottoAutoawardOrder.getRequesttime());
                    lottoAutoawardOrderExpend.setAwardmoney(lottoAutoawardOrder.getAwardmoney());
                    lottoAutoawardOrderExpend.setLottoCode(lottoAutoawardOrder.getInstantgameno());
                    lottoAutoawardOrderExpend.setLottoName(lottoAutoawardOrder.getInstantgamename());
                    lottoAutoawardOrderExpend.setOrderno(lottoAutoawardOrder.getOrderno());
                    validLottoAutoAwardOrderList.add(lottoAutoawardOrderExpend);
                }
            }
        }
        log.info("最终返回个数={}", validLottoAutoAwardOrderList.size());
        return WrapperUtil.ok(validLottoAutoAwardOrderList).toString();
    }

    @Override
    public String updateAddress(JSONObject param) {
        String recipient = param.getString("recipient");//收件人
        String phone = param.getString("phone");//收件人手机号
        String address = param.getString("address");//收件人具体地址
        String guid = param.getString("guid");//创建人Id
        if (StringUtil.isNullOrEmpty(guid)) {
            return WrapperUtil.error(ResultCodeEnum.PARAM_MISS, "guid不能为空").toString();
        }
        AuthAddress a = authAddressMapper.selectByGuid(guid);
        if (a == null) {
            AuthAddress authAddress = new AuthAddress();
            authAddress.setId(UUID.randomUUID().toString());
            authAddress.setName(recipient);
            authAddress.setPhone(phone);
            authAddress.setAddress(address);
            authAddress.setCreatetime(new Date());
            authAddress.setUpdateTime(new Date());
            authAddress.setCreateuserid(guid);
            int insertSelective = authAddressMapper.insertSelective(authAddress);
            if (insertSelective == 0) {
                return WrapperUtil.error(ResultCodeEnum.ERR_41040, "数据写入数据库失败").toString();
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", "1000");
            jsonObject.put("msg", "更改用户收货地址成功");
            return jsonObject.toString();
        }

        a.setName(recipient);
        a.setPhone(phone);
        a.setAddress(address);
        a.setUpdateTime(new Date());

        int count = authAddressMapper.updateByPrimaryKeySelective(a);
        if (count == 0) {
            return WrapperUtil.error(ResultCodeEnum.ERR_41040, "数据写入数据库失败").toString();
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "1000");
        jsonObject.put("msg", "更改用户收货地址成功");
        return jsonObject.toString();
    }

    @Override
    public String getAliveReward(JSONObject params) {

        String guid = params.getString("guid");
        Integer rewardType = params.getInteger("rewardType");
        if (StringUtil.isNullOrEmpty(guid, rewardType.toString())) {
            return WrapperUtil.error(ResultCodeEnum.PARAM_MISS, "guid,rewardType不能为空").toString();
        }
        Reward reward = new Reward();
        reward.setType(rewardType);
        reward.setGuid(guid);
        List<Reward> list = rewardMapper.selectByReward(reward);
        return WrapperUtil.ok(list).toString();
    }

    @Override
    public Map<String, Object> getmailRewardOrderList(MailRewardOrder mailRewardOrder) {
        Map map = new HashMap<String, Object>();
        List<MailRewardOrder> list = mailedAwardMapper.getmailRewardOrderList(mailRewardOrder);
        int count = mailedAwardMapper.getmailRewardOrderListCount(mailRewardOrder);
        map.put("list", list);
        map.put("count", count);
        return map;
    }

    @Override
    public RWrapper updateByPrimaryKeySelective(MailedAward record) {
        record.setStatus(2);//配送状态（1.等待配送，2.已配送）
        int result = mailedAwardMapper.updateByPrimaryKeySelective(record);
        if (result != 1) {
            return WrapperUtil.error(ResultCodeEnum.ERR_41040, "更新实物奖品邮寄订单失败");
        }

        RewardOfUser rewardOfUser = new RewardOfUser();
        rewardOfUser.setStatus(1);//1.已使用
        rewardOfUser.setUseTime(new Date());
        rewardOfUser.setId(record.getCardId());
        int count = rewardOfUserMapper.updateByPrimaryKeySelective(rewardOfUser);
        if (count != 1) {
            return WrapperUtil.error(ResultCodeEnum.ERR_41040, "更新用户奖品表（卡包）失败");
        }

        return WrapperUtil.ok();
    }

    @Override
    public RWrapper getAwardList() {
        List<String> list = startMessageQueue.getRewardList();

        return WrapperUtil.ok(list);

    }

    @Override
    public String rewardRecord(JSONObject jsonObject) {
        String guid = jsonObject.getString("guid");
        String pageNum = jsonObject.getString("pageNum");
        String pageSize = jsonObject.getString("pageSize");
        PageHelper.startPage(Integer.parseInt(pageNum), Integer.parseInt(pageSize));
        List<RewardOfUser> rewardOfUserLists = rewardOfUserMapper.selectRewardsByUserId(guid);
        log.info("根据guid={},查询奖品个数={}", guid, rewardOfUserLists.size());
        JSONArray jsonArray = new JSONArray();
        JSONObject json;
        if (!rewardOfUserLists.isEmpty()) {
            for (RewardOfUser rewardOfUser : rewardOfUserLists) {
                String rewardName = rewardOfUser.getRewardName();
                Date gainedDate = rewardOfUser.getGainedTime();
                json = new JSONObject();
                json.put("rewardName", rewardName == null ? "" : rewardName);
                json.put("gainedDate", gainedDate == null ? "" : DateUtil.format(gainedDate, "yyyy-MM-dd HH:mm:ss"));
                jsonArray.add(json);
            }
        }
        JSONObject res = new JSONObject();
        res.put("List", jsonArray);
        String resData = WrapperUtil.ok(res).toString();
        return resData;
    }

    /**
     * 检查是否可用
     *
     * @param rewardOfUser
     */
    private void checkRewardOfUser(RewardOfUser rewardOfUser) {
        if (rewardOfUser == null) {
            throw new BusinessException(ResultCodeEnum.FAIL, "该卡包不存在!");
        }
        //1.已使用 0.未使用，2：已过期
        if (rewardOfUser.getStatus() == 1) {
            throw new BusinessException(ResultCodeEnum.CARD_USED, "该卡包已被使用!");
        }
        if (rewardOfUser.getStatus() == 2) {
            throw new BusinessException(ResultCodeEnum.FAIL, "该卡包已经过期!");
        }
    }


    /**
     * 检查使用翻倍和三倍后的金额
     *
     * @param code
     * @param awardAmount 原始中奖金额
     * @return
     */
    private Long checkDoublingMoney(String code, String awardAmount) {
        //翻倍和三倍后最高金额
        Long maxMonery = 5000L;
        Long amount = Long.parseLong(awardAmount);
        //使用卡后的金额
        Long multipleAmount;
        if ("double_award".equals(code)) {
            multipleAmount = amount * 2;
            if (multipleAmount > maxMonery) {
                multipleAmount = maxMonery;
            }
        } else if ("treble_award".equals(code)) {
            multipleAmount = amount * 3;
            if (multipleAmount > maxMonery) {
                multipleAmount = maxMonery;
            }
        } else {
            throw new BusinessException(ResultCodeEnum.FAIL, "使用卡的类型不正确!");
        }

        log.info("使用卡后金额={}", multipleAmount);
        multipleAmount = multipleAmount - amount;
        log.info("真正获得的金额={}", multipleAmount);
        if (multipleAmount <= 0) {
            throw new BusinessException(ResultCodeEnum.FAIL, "等于或者高于5000的金额,不需要使用翻倍卡!");
        }
        return multipleAmount;
    }

    @Override
    public String updateUseMailedAward(JSONObject param) throws RuntimeException {

        String phone = param.getString("phone");//收件人手机号
        String address = param.getString("address");//收件人具体地址
        String recipient = param.getString("recipient");//收件人姓名
        String id = param.getString("id");//物流订单id
        String guid = param.getString("guid");//用户编号

        if (StringUtil.isNullOrEmpty(phone, recipient, address, id, guid)) {
            return WrapperUtil.error(ResultCodeEnum.PARAM_MISS, "phone,recipient,address,id,guid不能为空").toString();
        }

        MailedAward record = new MailedAward();
        record.setAddress(address);
        record.setPhone(phone);
        record.setName(recipient);
        record.setId(id);
        record.setStatus(1);//等待配送
        record.setUserId(guid);
        int count = mailedAwardMapper.updateByPrimaryKeySelective(record);
        if (count == 0) {
            return WrapperUtil.error(ResultCodeEnum.ERR_41090, "更新物流订单失败").toString();
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code","1000");
        jsonObject.put("mdg","更新物流订单成功");
        return jsonObject.toString();
    }

    @Override
    public RWrapper getUseMailedAward(JSONObject param) {
        String guid = param.getString("guid");
        if (StringUtil.isNullOrEmpty(guid)) {
            RWrapper r = new RWrapper("2020", "请检查参数[guid]", null);
            return r;
        }

        String cardId = param.getString("cardId");
        if (StringUtil.isNullOrEmpty(guid)) {
            RWrapper r = new RWrapper("2020", "请检查参数[cardId]", null);
            return r;
        }

        MailedAward a = mailedAwardMapper.selectByCardIdAndUserId(cardId, guid);
        RWrapper rw = new RWrapper("1000", "查看实物配送信息成功", a);
        return rw;
    }

    @Override
    public String showMultipleCards(JSONObject jsonObject) {
        String guid = jsonObject.getString("guid");
        String rewardType = jsonObject.getString("rewardType");
        List<RewardOfUser> rewardOfUserLists = rewardOfUserMapper.showMultipleCards(guid, rewardType);
        log.info("根据guid={}rewardType={},查询可使用的个数={}", guid, rewardType, rewardOfUserLists.size());
        JSONArray jsonArray = new JSONArray();
        JSONObject json;
        if (!rewardOfUserLists.isEmpty()) {
            for (RewardOfUser rewardOfUser : rewardOfUserLists) {
                json = new JSONObject();
                json.put("cardId", rewardOfUser.getId());
                json.put("rewardId", rewardOfUser.getRewardId());
                json.put("guid", rewardOfUser.getUserId());
                json.put("gainedTime", rewardOfUser.getGainedTime());
                Date expiredTime = rewardOfUser.getExpired();
                json.put("expiredTime", expiredTime == null ? "" : DateUtil.format(expiredTime, "yyyy-MM-dd"));
                json.put("rewardCode", rewardOfUser.getRewardCode());
                json.put("rewardName", rewardOfUser.getRewardName());
                json.put("rewardType", rewardOfUser.getRewardType());
                jsonArray.add(json);
            }
        }
        String resData = WrapperUtil.ok(jsonArray).toString();
        return resData;
    }

    @Override
    public void updateOverdueCardStatus() {
        //1.查询出(状态为0)所有可以使用的卡包  1.已使用 0.未使用，2：已过期
        //2.判断时间过期时间是否过期
        //3.更新状态为2
        List<RewardOfUser> rewardOfUserLists = rewardOfUserMapper.getAllNotUsedCard();
        log.info("查询出过期的卡包个数={}", rewardOfUserLists.size());
        int i = 0;
        if (!rewardOfUserLists.isEmpty()) {
            for (RewardOfUser rewardOfUser : rewardOfUserLists) {
                Date expiredTime = rewardOfUser.getExpired();
                if (expiredTime != null) {
                    Date overdue = DateUtil.parse(DateUtil.format(expiredTime, "yyyy-MM-dd"));
                    Date newDate = DateUtil.parse(DateUtil.format(new Date(), "yyyy-MM-dd"));
                    //比较为false时需要改状态为2
                    //Date1.after(Date2),当Date1大于Date2时，返回TRUE，当小于等于时，返回false；
                    if (!overdue.after(newDate)) {
                        //如果为实物  去查询用户有没有填写邮寄地址  如果填写不更新  没有填写 更新为过期  4:实物
                        String rewardType = rewardOfUser.getRewardType();
                        if (!StringUtil.isNullOrEmpty(rewardType) && "4".equals(rewardType)) {
                            String cardId = rewardOfUser.getId();
                            MailedAward mailedAward = mailedAwardMapper.selectByCardId(cardId);
                            if (mailedAward != null && !StringUtil.isNullOrEmpty(mailedAward.getAddress())) {
                                continue;
                            }
                        }
                        i = i + 1;
                        rewardOfUser.setStatus(2);
                        rewardOfUserMapper.updateByPrimaryKey(rewardOfUser);
                    }
                }
            }
        }
        log.info("本次修改的条数={}", i);
    }

    @Override
    public RWrapper getReward(JSONObject param) {

        String guid = param.getString("guid");//用户Id
        Integer pageStart = param.getInteger("pageStart");//分页起始位置
        Integer pageSize = param.getInteger("pageSize");//分页大小
        Integer status = param.getInteger("status");//使用情况（1.已使用 0.未使用，2：已过期）
        String condition = null;//排序

        if (StringUtil.isNullOrEmpty(guid)) {
            RWrapper r = new RWrapper("2020", "请检查参数[guid]", null);
            return r;
        }

        if (pageStart == null) {
            RWrapper r = new RWrapper("2020", "请检查参数[pageStart]", null);
            return r;
        }

        if (pageSize == null) {
            RWrapper r = new RWrapper("2020", "请检查参数[pageSize]", null);
            return r;
        }

        if (status == null) {
            RWrapper r = new RWrapper("2020", "请检查参数[status]", null);
            return r;
        }

        //设置排序规则
        switch (status) {
            case 0:
                condition = "gained_time";//领取时间
                break;
            case 1:
                condition = "use_time";//使用时间
                break;
            case 2:
                condition = "expired";//失效时间
                break;
            default:
                condition = "gained_time";//领取时间
                break;
        }

        PageHelper.startPage(pageStart + 1, pageSize);
        List<RewardOfUserDto> list = rewardOfUserMapper.selectRewardOfUser(guid, condition, status);

        for (RewardOfUserDto rewardOfUser : list) {
            //对物品的物流订单状态进行判断
            if ("4".equals(rewardOfUser.getRewardType())) {
                String id = rewardOfUser.getId();
                MailedAward m = mailedAwardMapper.selectByCardId(id);
                if (m != null) {
                    /**
                     * 物流订单状态（0.未使用, 1.等待配送，2.已配送），
                     * 卡包状态为已使用时，物流订单状态为已配送，物流订单才有快递公司，快递单号
                     */
                    if (m.getStatus() == 2) {
                        rewardOfUser.setDistributionStatus(2);//已配送
                        rewardOfUser.setCourier(m.getCourier());
                        rewardOfUser.setMailNo(m.getMailNo());
                    } else {
                        rewardOfUser.setDistributionStatus(m.getStatus());//未使用
                    }

                }
            }
        }
        RWrapper rw = new RWrapper("1000", "卡包列表获取成功", list);
        return rw;
    }

    /**
     * 打折卡使用方法,具体流程参考文档
     *
     * @param param
     * @return
     * @throws Exception
     */
    @Override
    public String discountCardPay(JSONObject param) {

        log.info("使用打折卡 接口：参数为：" + param.toString());
        String prePayNo = param.getString("prePayNo");//订单号
        String code = param.getString("code");//奖品类型
        String key = param.getString("key");//支付密码
        BigDecimal disCountAmount = param.getBigDecimal("disCountAmount");//折扣金额
        BigDecimal payAmount = param.getBigDecimal("payAmount");//实际支付金额
        String guid = param.getString("guid");//用户id
        BigDecimal originalAmount = param.getBigDecimal("originalAmount");//原始金额
        String cardId = param.getString("cardId");//卡包id
        String fingerprintStr = param.getString("fingerprintStr");

        if (StringUtil.isNullOrEmpty(prePayNo, guid)) {
            return WrapperUtil.error(ResultCodeEnum.PARAM_MISS, "guid,key,prePayNo不能为空").toString();
        }

        if (StringUtil.isNullOrEmpty(guid)) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVIDATE, "用户编号不能为空");
        }

        if (StringUtil.isNullOrEmpty(prePayNo)) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVIDATE, "预支付单号不能为空");
        }

        JSONObject disCountParams = new JSONObject();
        disCountParams.put("userId", guid);//用户
        disCountParams.put("key", key);//支付密码
        disCountParams.put("prePayNo", prePayNo);//预支付单号
        disCountParams.put("fingerprintStr", fingerprintStr);
        disCountParams.put("clientID", signClient.getClientId());
        disCountParams.put("nonce_str", UUID.randomUUID().toString());

        /**String plaJson = updatePurchesLotteryAmount(guid, payAmount);
         log.info("获奖因子增加购彩金额={}",plaJson);*/

        if (StringUtil.isNullOrEmpty(code)) {
            return useNotDiscountCardPay(disCountParams, disCountAmount, payAmount, guid);
        }

        if (StringUtil.isNullOrEmpty(disCountAmount.toString(), payAmount.toString(), originalAmount.toString(), cardId)) {
            return WrapperUtil.error(ResultCodeEnum.PARAM_MISS, "code,disCountAmount,payAmount,originalAmount,cardId 参数必须同时存在").toString();
        }

        /**
         * 判断用户输入的折扣金额与计算的金额是否相等
         */
        JSONObject resultData = isDisCountAmount(cardId, originalAmount, payAmount);
        log.info("判断用户输入的折扣金额与计算的金额是否相等={}", resultData);

        String rewardId = resultData.getJSONObject("data").getString("rewardId");
        String rewardType = resultData.getJSONObject("data").getString("rewardType");
        return useDiscountCardPay(guid, key, prePayNo, disCountAmount, payAmount, cardId, rewardType, rewardId,fingerprintStr);
    }

    /**
     * 获取折扣值
     *
     * @param rewardCode 奖品代码
     * @return
     */
    public BigDecimal getDisCount(String rewardCode) {

        BigDecimal disCount = null;
        switch (rewardCode) {
            case "ninety_discount":
                disCount = new BigDecimal("0.9");
                break;
            case "eighty_discount":
                disCount = new BigDecimal("0.8");
                break;
        }

        return disCount;
    }


    /**
     * 判断折扣金额是否正确
     *
     * @param cardId         卡包id
     * @param originalAmount 原始金额
     * @param payAmount      支付金额
     */
    public JSONObject isDisCountAmount(String cardId, BigDecimal originalAmount, BigDecimal payAmount) {

        /**
         * 判断用户输入的折扣金额与计算的金额是否相等
         */
        RewardOfUserDto bean = new RewardOfUserDto();
        bean.setId(cardId);
        List<RewardOfUserDto> rewardOfUserList = rewardOfUserMapper.selectRewardOfUserList(bean);
        if (rewardOfUserList.size() == 0) {
            throw new BusinessException(ResultCodeEnum.ERR_41038, "查询无数据记录！");
        }

        RewardOfUserDto rou = rewardOfUserList.get(0);
        String rewardCode = rou.getRewardCode();
        if (!StringUtil.isNullOrEmpty(rewardCode)) {

            BigDecimal disCount = getDisCount(rewardCode);
            BigDecimal newDisCountAmount = originalAmount.multiply(disCount);//折扣后的金额
            int flag = payAmount.compareTo(newDisCountAmount);
            if (flag != 0) {
                throw new BusinessException(ResultCodeEnum.ERR_41091, "折扣卡的折扣金额有误");
            }
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("rewardId", rou.getRewardId());
        jsonObject.put("rewardType", rou.getRewardType());
        return ResultUtil.buildSuccessResult(jsonObject);
    }


    /**
     * 使用折扣卡支付
     *
     * @param guid           用户id
     * @param key            支付密码
     * @param prePayNo       预支付单号
     * @param disCountAmount 折扣金额
     * @param payAmount      支付金额
     * @param cardId         卡包id
     * @param rewardType     奖品类型
     * @param rewardId       奖品id
     */
    public String useDiscountCardPay(String guid, String key, String prePayNo, BigDecimal disCountAmount, BigDecimal payAmount, String cardId, String rewardType, String rewardId,String fingerprintStr) {

        JSONObject disCountParams = new JSONObject();
        disCountParams.put("userId", guid);//用户
        disCountParams.put("key", key);//支付密码
        disCountParams.put("prePayNo", prePayNo);//预支付单号
        disCountParams.put("clientID", signClient.getClientId());
        disCountParams.put("nonce_str", UUID.randomUUID().toString());
        disCountParams.put("discountAmount", disCountAmount.toString());//折扣金额
        disCountParams.put("orderCardId", cardId);//卡包id
        disCountParams.put("orderCardType", rewardType);//折扣卡类型
        disCountParams.put("payAmount", payAmount.toString());//实际支付金额
        disCountParams.put("payAmount", payAmount.toString());//实际支付金额
        disCountParams.put("fingerprintStr", fingerprintStr);

        //调用payment折扣方法,待写
        log.info("调用payment 福金支付 参数={}", disCountParams.toString());
        RWrapper<JSONObject> totalJson = paymentFeignService.discountCardPay(disCountParams);
        if ("1000".equals(totalJson.getCode())) {

            JSONObject totalRetJson = totalJson.getData();
            JSONObject retJson = totalJson.getData().getJSONObject("retJson");

            if ("1000".equals(retJson.getString("code"))) {

                JSONObject dcUsageJson = new JSONObject();
                JSONObject plaJson = new JSONObject();

                if (!StringUtil.isNullOrEmpty(rewardId)) {
                    dcUsageJson.put("rewardId", rewardId);
                }

                if (!StringUtil.isNullOrEmpty(cardId)) {
                    dcUsageJson.put("cardId", cardId);
                }

                if (disCountAmount != null) {
                    dcUsageJson.put("discountAmount", disCountAmount);
                }

                if (payAmount != null) {
                    dcUsageJson.put("payAmount", payAmount);
                    plaJson.put("payAmount", payAmount);
                }

                dcUsageJson.put("prePayNo", prePayNo);
                dcUsageJson.put("userId", guid);
                log.info("封装dcUsageJson参数返回结果={}", dcUsageJson.toString());


                totalRetJson.put("dcUsageJson", dcUsageJson);
                //totalJson.setData(totalRetJson);
                if (!StringUtil.isNullOrEmpty(guid)) {
                    plaJson.put("userId", guid);
                }
                totalRetJson.put("plaJson", plaJson);
            }

            log.info("调用gts入参={}", totalRetJson.toString());
            RWrapper<JSONObject> result = gtsFeignService.useDisCountCard(totalRetJson);
            log.info("调用gts返回结果={}", result.toString());
            if("1000".equals(result.getCode())){
                return result.getData().toString();
            }else{
                log.error("gts返回错误信息={}", result.toString());
                return result.toString();
            }

        } else {
            return totalJson.toString();
        }

    }


    /**
     * 未使用折扣卡支付
     *
     * @param   disCountParams    用户id
     * @param  disCountAmount    折扣金额
     * @param  payAmount         支付金额
     * @param  guid               用户id
     */
    public String useNotDiscountCardPay(JSONObject disCountParams, BigDecimal disCountAmount, BigDecimal payAmount, String guid) {

        log.info("未使用打折卡福金支付");
        RWrapper<JSONObject> jsonData = paymentFeignService.discountCardPay(disCountParams);
        log.info("未使用打折卡调用payment返回结果={}", jsonData);

        if ("1000".equals(jsonData.getCode())) {

            JSONObject totalRetJson = jsonData.getData();
            JSONObject retJson = jsonData.getData().getJSONObject("retJson");

            if ("1000".equals(retJson.getString("code"))) {

                JSONObject plaJson = new JSONObject();
                if (payAmount != null) {
                    plaJson.put("payAmount", payAmount);
                }

                if (!StringUtil.isNullOrEmpty(guid)) {
                    plaJson.put("userId", guid);
                }
                totalRetJson.put("plaJson", plaJson);
            }

            log.info("调用gts入参={}", totalRetJson.toString());
            RWrapper<JSONObject> result = gtsFeignService.useDisCountCard(totalRetJson);
            log.info("调用gts返回结果={}", result.toString());
            if("1000".equals(result.getCode())){
                return result.getData().toString();
            }else{
                log.error("gts返回错误信息={}", result.toString());
                return result.toString();
            }
        } else {
            return jsonData.toString();
        }

    }

    @Override
    public JSONObject queryGameBless(String date){

        //1.查询夺宝次数
        //2.查询折扣卡使用情况
        //3.查询翻倍卡使用情况
        //4.计算福包金额

        JSONObject dataJson = new JSONObject();

        List<GameBlessVo> gameBlessVoList = rewardOfUserMapper.queryGameBlessList(date);

        dataJson.put("gameCount", 0);
        dataJson.put("gameAmount", 0);
        dataJson.put("twoBlessCount", 0);
        dataJson.put("fiveBlessCount", 0);
        dataJson.put("tenBlessCount", 0);
        dataJson.put("nineDiscountCount", 0);
        dataJson.put("eightDiscountCount", 0);
        dataJson.put("doubleCount", 0);
        dataJson.put("threeCount", 0);
        dataJson.put("blessAmount", 0);
        dataJson.put("discountAmount", 0);
        dataJson.put("multiAmount", 0);
        dataJson.put("amountSum", 0);
        dataJson.put("profitSum", 0);



        if (gameBlessVoList != null && gameBlessVoList.size() > 0) {
            int totalCount = 0;
            for (GameBlessVo gameBlessVo : gameBlessVoList) {
                if("two_bless".equals(gameBlessVo.getRewardCode())){
                    dataJson.put("twoBlessCount", gameBlessVo.getNum());
                }else if("five_bless".equals(gameBlessVo.getRewardCode())){
                    dataJson.put("fiveBlessCount", gameBlessVo.getNum());
                }else if("ten_bless".equals(gameBlessVo.getRewardCode())){
                    dataJson.put("tenBlessCount", gameBlessVo.getNum());
                }else if("ninety_discount".equals(gameBlessVo.getRewardCode())){
                    dataJson.put("nineDiscountCount", gameBlessVo.getNum());
                }else if("eighty_discount".equals(gameBlessVo.getRewardCode())){
                    dataJson.put("eightDiscountCount", gameBlessVo.getNum());
                }else if("double_award".equals(gameBlessVo.getRewardCode())){
                    dataJson.put("doubleCount", gameBlessVo.getNum());
                }else if("treble_award".equals(gameBlessVo.getRewardCode())){
                    dataJson.put("threeCount", gameBlessVo.getNum());
                }

                //计算总次数
                totalCount += gameBlessVo.getNum();
            }
            dataJson.put("gameCount", totalCount);
            dataJson.put("gameAmount", totalCount * 5);
        }

        int discountAmount = discountCardUsageMapper.queryDiscountAmountByDate(date);

        int awardAmount = awardCardUsageMapper.queryAwardBlessByDate(date);

        dataJson.put("discountAmount", discountAmount);
        dataJson.put("multiAmount", awardAmount);


        //计算福包奖品总金额
        int twoAmount = dataJson.getInteger("twoBlessCount") * 2;
        int fiveAmount = dataJson.getInteger("fiveBlessCount") * 5;
        int tenAmount = dataJson.getInteger("tenBlessCount") * 10;

        int blessTotal = twoAmount + fiveAmount + tenAmount;
        dataJson.put("blessAmount", blessTotal);

        //小计,用户中奖加补贴数据(平台亏损)
        int amountSum = blessTotal + discountAmount + awardAmount;

        dataJson.put("amountSum", amountSum);

        //盈亏计算  平台收入-平台亏损
        int profitSum = dataJson.getIntValue("gameAmount") - amountSum;
        dataJson.put("profitSum", profitSum);

        log.info("查询游戏福包情况返回数据={}", dataJson.toString());

        return dataJson;
    }


}
