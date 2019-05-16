package com.bkgc.bless.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.bless.*;
import com.bkgc.bean.pay.Const;
import com.bkgc.bean.user.AuthMember;
import com.bkgc.bless.config.Config;
import com.bkgc.bless.config.SignClient;
import com.bkgc.bless.consumer.PaymentFeignService;
import com.bkgc.bless.mapper.*;
import com.bkgc.bless.utils.RedisUtil;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.DateTimeUtils;
import com.bkgc.common.utils.RWrapper;
import com.bkgc.common.utils.RandomUtil;
import com.bkgc.common.utils.StringUtil;
import com.bkgc.common.utils.signature.MD5;
import com.xiaoleilu.hutool.date.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>Title:      EnvelopeService </p>
 * <p>Description  </p>
 * <p>Company:    北控国彩 </p>
 *
 * @Author zhangft
 * @CreateDate 2017/12/21 上午10:37
 */
@Service
public class EnvelopeService {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BlessEnvelopeGroupMapper blessEnvelopeGroupMapper;

    @Autowired
    private BlessEnvelopeMapper blessEnvelopeMapper;

    @Autowired
    private BlessEnvelopeVaildMapper blessEnvelopeVaildMapper;

    @Autowired
    private BlessEnvelopeMachineRMapper blessEnvelopeMachineRMapper;

    @Autowired
    private BlessEnvelopeGroupGrandsMapper blessEnvelopeGroupGrandsMapper;

    @Autowired
    private AuthMemberMapper authMemberMapper;

    @Autowired
    private PaymentFeignService paymentFeignService;

    @Resource
    private Config config;

    @Resource
    private SignClient signClient;

    @Autowired
    private RedisUtil redisUtil;

//    @Autowired
//    private DistributeLockUtil distributeLockUtil;

    @Autowired
    private MysqlLockService mysqlLockService;

    private final int fixBlessMoney = 6;

    private BigDecimal calGrandationMoney(String gradeData) {
        JSONArray jsonArray = JSONArray.parseArray(gradeData);
        BigDecimal totalMoney = new BigDecimal("0");
        if (!jsonArray.isEmpty() && jsonArray.size() != 0) {
            JSONObject blessInfoDetail;
            int count;
            BigDecimal perAmount;
            for (int i = 0; i <= jsonArray.size() - 1; i++) {
                blessInfoDetail = jsonArray.getJSONObject(i);
                count = blessInfoDetail.getInteger("key");
                perAmount = blessInfoDetail.getBigDecimal("value");
                totalMoney = totalMoney.add(new BigDecimal(count).multiply(perAmount));
            }
        }
        return totalMoney;
    }

    /**
     * <p>Title:      将有效时间字符串转换为map </p>
     * <p>Description  </p>
     *
     * @Author zhangft
     * @CreateDate 2018/1/16 下午2:12
     */
    private Map<String, String> getValidTimeMap(String validTimeStr) {
        Map<String, String> timeMap = new HashMap<>();
        String[] dailys = validTimeStr.split("-");
        for (String vaild : dailys) {
            String[] dailyTime = vaild.split("_");
            String startTime = dailyTime[0];
            String endTime = dailyTime[1];
            timeMap.put(startTime, endTime);
        }

        return timeMap;
    }

   /* @Deprecated
    private String getValidTimeStr2(List<BlessEnvelopeVaild> blessEnvelopeVaildList) {
        String vaileTime = "";
        for (int i = 0; i < blessEnvelopeVaildList.size(); i++) {
            BlessEnvelopeVaild bv = blessEnvelopeVaildList.get(i);
            String startTime = bv.getStartTime();
            String endTime = bv.getEndTime();
            String time = startTime + "_" + endTime;
            vaileTime += time + "," + "\n";
        }
        return vaileTime;
    }*/

    private String getValidTimeStr(List<BlessEnvelopeVaild> blessEnvelopeVaildList) {
        String vaileTime = null;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd");
        String nowDateStr = sdfNow.format(new Date());
        for (int i = 0; i < blessEnvelopeVaildList.size(); i++) {
            BlessEnvelopeVaild bv = blessEnvelopeVaildList.get(i);
            String startTime = nowDateStr + " " + bv.getStartTime();
            log.info("startTime={}", startTime);
            Date start;
            try {
                start = sdf.parse(startTime);
            } catch (ParseException e) {
                e.printStackTrace();
                log.info("福包有效时间转换异常");
                throw new RuntimeException("福包有效时间转换异常");
            }
            long diffTime = start.getTime() - System.currentTimeMillis();
            if (diffTime > 0) {
                String[] arr = bv.getStartTime().split(":");
                return "于" + arr[0] + ":" + arr[1] + "开抢";
            }
        }

        if (vaileTime == null) {
            String[] arr = blessEnvelopeVaildList.get(0).getStartTime().split(":");
            return "于" + arr[0] + ":" + arr[1] + "开抢";
        }
        return vaileTime;
    }

    @Transactional
    public JSONObject createBlessEnvelope(JSONObject params) {
        log.info("进入创建福包组接口，参数为：" + params);
        String accessToken = params.getString("accessToken");
        //支付密码
        String key = params.getString("key");
        //用户Id
        String userId = params.getString("userId");
        //用户类型：1 个人  2 企业  默认就是2
        String fromUserType = params.getString("fromUserType");
        //1普通随机福包，2等级福包，3推广福包
        String blessType = params.getString("blessType");
        //8001763,8001788,8001799
        String machineIds = params.getString("machineIds");
        //领取过期时间  2017-06-01 02:03:55
        String expireDate = params.getString("expireDate");
        //08:00:00_09:00:00-12:00:00_13:00:00-15:00:00_17:00:00
        String validTime = params.getString("validTime");
        //福包说明
        String blessDesc = params.getString("blessDesc");
        //总金额（正常发福包）
        BigDecimal money = params.getBigDecimal("totalAmount");
        //分等级发福包，格式为：[{"key":10,"value":100},{"key":100,"value":10},{"key":1000,"value":1}]
        String gradeData = params.getString("gradeData");
        //是否是独立  是1 否0
        String independent = params.getString("independent");

        if (StringUtil.isNullOrEmpty(validTime, key, fromUserType, machineIds, userId, blessType, expireDate, blessDesc)) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVIDATE, "请检查参数【validTime, key, fromUserType, machineIds, userId, blessType, expireDate, blessDesc】是否为空");
        }
        String groupId = "";

        //1普通随机福包，2等级福包，3推广福包【4线下单码福包，5线下多码福包，6公益福包 后期开发】
        if ("1".equals(blessType)) {
            if ("0".equals(independent)) {
                groupId = createRandom(params);
            }
            if ("1".equals(independent)) {
                //批量独立
                createRandomBatch(params);
            }
        } else if ("2".equals(blessType)) {
            if (StringUtil.isNullOrEmpty(gradeData)) {
                throw new BusinessException(ResultCodeEnum.PARAM_INVIDATE, "gradeData参数不能为空");
            }

            if ("0".equals(independent)) {
                //总金额
                money = calGrandationMoney(gradeData);
                groupId = createGrandation(params);
            }
            if ("1".equals(independent)) {
                //总金额
                money = params.getBigDecimal("total");
                //批量独立
                createGrandationBatch(params);
            }
        } else if ("3".equals(blessType)) {
            groupId = createSpreadBless(params);
        } else {
            throw new BusinessException(ResultCodeEnum.ERR_41021, "暂不支持的福包组类型");
        }

        JSONObject data = new JSONObject();
        //福金支付
        JSONObject payParam = new JSONObject();
        payParam.put("accessToken", accessToken);
        payParam.put("userId", userId);
        payParam.put("partnerId", Const.FUJIN_PARTNER);
        payParam.put("amount", money);
        payParam.put("clientID", signClient.getClientId());
        payParam.put("nonce_str", UUID.randomUUID().toString());
        payParam.put("key", key);//支付密码
        log.info("请求参数={}", payParam.toString());
        RWrapper<JSONObject> payResult = paymentFeignService.sendPackage(payParam);
        log.info("返回结果={}", payResult.toString());
        if (!"1000".equals(payResult.getCode())) {
            log.info("支付失败，失败原因={}", payResult.getMsg());
            throw new BusinessException(ResultCodeEnum.ERR_41022, payResult.getMsg());
        }
        data.put("GroupId", groupId);
        return data;
    }

    /**
     * <p>Title:      推广福包可以重复创建，最终在配置文件中配置使用哪个groupId即可 </p>
     * <p>Description  </p>
     *
     * @Author zhangft
     * @CreateDate 2018/1/16 下午3:22
     */
    private String createSpreadBless(JSONObject params) {
        log.info("进入创建推广福包，参数={}", params.toString());
        String userId = params.getString("userId");//用户Id
        String adURL = params.getString("blessImage");
        String fromUserType = params.getString("fromUserType");//用户类型：1 个人  2 企业
        String blessName = params.getString("blessName");
        int number = params.getInteger("number");//福包数量（正常发福包）
        BigDecimal money = params.getBigDecimal("money");//总金额（正常发福包）
        String machineId = params.getString("machineId");//8001763,8001788,8001799
        String expireDate = params.getString("expireDate");//领取过期时间  2017-06-01 02:03:55
        String validTime = params.getString("validTime");//08:00:00_09:00:00-12:00:00_13:00:00-15:00:00_17:00:00
        String blessDesc = params.getString("blessDesc");//备注
        Map<String, String> timeMap = getValidTimeMap(validTime);
        String[] machines = machineId.split(",");
        String groupId = UUID.randomUUID().toString().replace("-", "");
        if (StringUtil.isNullOrEmpty(number + "", money + "")) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVIDATE, "请检查参数[number,money]是否为空");
        }
        BlessEnvelopeGroup blessEnvelopeGroup = new BlessEnvelopeGroup();//创建福包组
        if (!StringUtil.isNullOrEmpty(adURL)) {
            blessEnvelopeGroup.setAdURL(adURL);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            blessEnvelopeGroup.setExpiredtime(format.parse(expireDate));
        } catch (ParseException e) {
            throw new BusinessException(ResultCodeEnum.ERR_41024, "过期时间格式转换异常");
        }
        blessEnvelopeGroup.setId(groupId);
        blessEnvelopeGroup.setName(blessName);
        blessEnvelopeGroup.setCreatetime(DateTimeUtils.getCurrentDateTime());
        blessEnvelopeGroup.setFromuserid(userId);
        blessEnvelopeGroup.setFromusertype(Integer.parseInt(fromUserType));
        blessEnvelopeGroup.setIssinglecode(1);
        blessEnvelopeGroup.setRemark(blessDesc);
        blessEnvelopeGroup.setIspay(1);//是否已支付 0 未支付 1 已支付
        blessEnvelopeGroup.setIsbackpay(0);
        blessEnvelopeGroup.setAmount(money);
        blessEnvelopeGroup.setBalance(money);
        blessEnvelopeGroup.setGrands(1);// 1 正常发福包
        blessEnvelopeGroup.setNumber(number);
        blessEnvelopeGroup.setAliveCount(number);
        blessEnvelopeGroup.setSendtype(0);
        blessEnvelopeGroup.setIsapplybackpay(0);
        blessEnvelopeGroup.setType("3");  //推广福包
        int insertCount = blessEnvelopeGroupMapper.insertSelective(blessEnvelopeGroup); //生成一条福包组数据插入数据库

        if (insertCount == 0) {
            throw new BusinessException(ResultCodeEnum.ERR_41025, "福包组创建失败");
        }
        Set<Map.Entry<String, String>> DailyVaild = timeMap.entrySet();//获取每日领取该福包的有效时间段
        List<BlessEnvelopeVaild> blessEnvelopeVaildList = new ArrayList<>();
        for (Map.Entry<String, String> vaildTime : DailyVaild) {
            String id = UUID.randomUUID().toString().replace("-", "");
            BlessEnvelopeVaild blessEnvelopeVaild = new BlessEnvelopeVaild();
            blessEnvelopeVaild.setId(id);
            blessEnvelopeVaild.setGroupid(groupId);
            blessEnvelopeVaild.setStartTime(vaildTime.getKey());
            blessEnvelopeVaild.setEndTime(vaildTime.getValue());
            blessEnvelopeVaild.setType(3);
            blessEnvelopeVaildList.add(blessEnvelopeVaild);
        }
        blessEnvelopeVaildMapper.insertBlessEnvelopeVaildList(blessEnvelopeVaildList);
        List<BlessEnvelopeGroupMachineR> blessEnvelopeGroupMachineRList = new ArrayList<>();
        for (String machine : machines) {
            Integer MachineId = Integer.parseInt(machine);
            BlessEnvelopeGroupMachineR blessEnvelopeGroupMachineR = new BlessEnvelopeGroupMachineR(groupId, MachineId);
            blessEnvelopeGroupMachineRList.add(blessEnvelopeGroupMachineR);
        }
        blessEnvelopeMachineRMapper.addEnvelopeMachineList(blessEnvelopeGroupMachineRList);
        return blessEnvelopeGroup.getId();
    }


    /**
     * 随机福包
     *
     * @param params
     * @return
     */
    private String createRandom(JSONObject params) {
        log.info("进入创建随机福包，参数={}", params.toString());
        String userId = params.getString("userId");
        //福包广告图片
        String adURL = params.getString("blessImage");
        //用户类型：1 个人  2 企业
        String fromUserType = params.getString("fromUserType");
        //福包名称
        String blessName = params.getString("blessName");
        //随机最大金额
        BigDecimal randMaxMoney = params.getBigDecimal("randMaxMoney");
        //总金额
        BigDecimal totalAmount = params.getBigDecimal("totalAmount");
        //8001763,8001788,8001799
        String machineIds = params.getString("machineIds");
        //领取过期时间  2017-06-01 02:03:55
        String expireDate = params.getString("expireDate");
        //08:00:00_09:00:00-12:00:00_13:00:00-15:00:00_17:00:00
        String validTime = params.getString("validTime");
        //福包说明
        String blessDesc = params.getString("blessDesc");
        //福包个数
        int blessNum = params.getInteger("blessNum");
        String groupId = UUID.randomUUID().toString().replace("-", "");
        if (totalAmount == null) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVIDATE, "请检查参数[totalAmount]是否为空");
        }
        BlessEnvelopeGroup blessEnvelopeGroup = new BlessEnvelopeGroup();
        if (!StringUtil.isNullOrEmpty(adURL)) {
            blessEnvelopeGroup.setAdURL(adURL);
        }
        blessEnvelopeGroup.setExpiredtime(DateUtil.parseDateTime(expireDate));
        blessEnvelopeGroup.setId(groupId);
        blessEnvelopeGroup.setName(blessName);
        blessEnvelopeGroup.setCreatetime(DateTimeUtils.getCurrentDateTime());
        blessEnvelopeGroup.setFromuserid(userId);
        blessEnvelopeGroup.setFromusertype(Integer.parseInt(fromUserType));
        blessEnvelopeGroup.setRandMax(randMaxMoney);
        blessEnvelopeGroup.setIssinglecode(1);
        blessEnvelopeGroup.setRemark(blessDesc);
        //是否已支付 0 未支付 1 已支付
        blessEnvelopeGroup.setIspay(1);
        blessEnvelopeGroup.setIsbackpay(0);
        blessEnvelopeGroup.setAmount(totalAmount);
        blessEnvelopeGroup.setBalance(totalAmount);
        blessEnvelopeGroup.setGrands(1);
        //个数修改，随机福包不需要判断个数，只需要判断金额即可，默认给10000个
        blessEnvelopeGroup.setNumber(blessNum);
        //福包剩余数量
        blessEnvelopeGroup.setAliveCount(blessNum);
        //送出类型(0未发出1分享2好友)
        blessEnvelopeGroup.setSendtype(0);
        blessEnvelopeGroup.setIsapplybackpay(0);
        //1普通随机福包，2等级福包
        blessEnvelopeGroup.setType("1");
        blessEnvelopeGroupMapper.insertSelective(blessEnvelopeGroup);
        //向有效时间表中添加数据
        addBlessenvelopeVaild(validTime, groupId, 1);
        //向自助机和福包组对应的表中添加数据
        addBlessenvelopeMachine(machineIds, groupId);
        return groupId;
    }

    /**
     * 随机福包(批量)
     *
     * @param params
     * @return
     */
    private void createRandomBatch(JSONObject params) {
        log.info("进入创建随机福包(批量)，参数={}", params.toString());
        String userId = params.getString("userId");
        //福包广告图片
        String adURL = params.getString("blessImage");
        //用户类型：1 个人  2 企业
        String fromUserType = params.getString("fromUserType");
        //福包名称
        String blessName = params.getString("blessName");
        //随机最大金额
        BigDecimal randMaxMoney = params.getBigDecimal("randMaxMoney");
        //总金额
        BigDecimal totalAmount = params.getBigDecimal("totalAmount");
        //单个机器金额
        BigDecimal oneAmount = params.getBigDecimal("oneAmount");
        //8001763,8001788,8001799
        String machineIds = params.getString("machineIds");
        //领取过期时间  2017-06-01 02:03:55
        String expireDate = params.getString("expireDate");
        //08:00:00_09:00:00-12:00:00_13:00:00-15:00:00_17:00:00
        String validTime = params.getString("validTime");
        //福包说明
        String blessDesc = params.getString("blessDesc");
        //福包个数
        int blessNum = params.getInteger("blessNum");

        if (totalAmount == null) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVIDATE, "请检查参数[totalAmount]是否为空");
        }
        String[] machines = machineIds.split(",");
        for (String machine : machines) {
            String groupId = UUID.randomUUID().toString().replace("-", "");
            BlessEnvelopeGroup blessEnvelopeGroup = new BlessEnvelopeGroup();
            if (!StringUtil.isNullOrEmpty(adURL)) {
                blessEnvelopeGroup.setAdURL(adURL);
            }
            blessEnvelopeGroup.setExpiredtime(DateUtil.parseDateTime(expireDate));
            blessEnvelopeGroup.setId(groupId);
            blessEnvelopeGroup.setName(blessName);
            blessEnvelopeGroup.setCreatetime(DateTimeUtils.getCurrentDateTime());
            blessEnvelopeGroup.setFromuserid(userId);
            blessEnvelopeGroup.setFromusertype(Integer.parseInt(fromUserType));
            blessEnvelopeGroup.setRandMax(randMaxMoney);
            blessEnvelopeGroup.setIssinglecode(1);
            blessEnvelopeGroup.setRemark(blessDesc);
            //是否已支付 0 未支付 1 已支付
            blessEnvelopeGroup.setIspay(1);
            blessEnvelopeGroup.setIsbackpay(0);
            blessEnvelopeGroup.setAmount(oneAmount);
            blessEnvelopeGroup.setBalance(oneAmount);
            blessEnvelopeGroup.setGrands(1);
            //个数修改，随机福包不需要判断个数，只需要判断金额即可，默认给10000个
            blessEnvelopeGroup.setNumber(blessNum);
            //福包剩余数量
            blessEnvelopeGroup.setAliveCount(blessNum);
            //送出类型(0未发出1分享2好友)
            blessEnvelopeGroup.setSendtype(0);
            blessEnvelopeGroup.setIsapplybackpay(0);
            //1普通随机福包，2等级福包
            blessEnvelopeGroup.setType("1");
            blessEnvelopeGroupMapper.insertSelective(blessEnvelopeGroup);

            //向有效时间表中添加数据
            addBlessenvelopeVaild(validTime, groupId, 1);
            //向自助机和福包组对应的表中添加数据
            List<BlessEnvelopeGroupMachineR> blessEnvelopeGroupMachineRList = new ArrayList<>();
            Integer MachineId = Integer.parseInt(machine);
            BlessEnvelopeGroupMachineR blessEnvelopeGroupMachineR = new BlessEnvelopeGroupMachineR(groupId, MachineId);
            blessEnvelopeGroupMachineRList.add(blessEnvelopeGroupMachineR);
            blessEnvelopeMachineRMapper.addEnvelopeMachineList(blessEnvelopeGroupMachineRList);
        }
        log.info("进入创建随机福包(批量)添加完成！");
    }


    /**
     * 等级福包
     *
     * @param params
     * @return
     */
    private String createGrandation(JSONObject params) {
        log.info("进入创建等级福包，请求参数={}", params.toString());
        String userId = params.getString("userId");
        String adURL = params.getString("blessImage");
        String fromUserType = params.getString("fromUserType");
        String blessName = params.getString("blessName");
        //8001763,8001788,8001799
        String machineIds = params.getString("machineIds");
        //领取过期时间  2017-06-01 02:03:55
        String expireDate = params.getString("expireDate");
        //08:00:00_09:00:00-12:00:00_13:00:00-15:00:00_17:00:00
        String validTime = params.getString("validTime");
        //备注
        String blessDesc = params.getString("blessDesc");
        //分等级发福包，格式为：[{"key":10,"value":100},{"key":100,"value":10},{"key":1000,"value":1}]
        String gradeData = params.getString("gradeData");

        String groupId = UUID.randomUUID().toString().replace("-", "");
        //向等级表中添加数据
        JSONObject jsonObject = addBlessEnvelopeGroupGrands(gradeData, groupId);
        int intNumber = jsonObject.getIntValue("intNumber");
        BigDecimal dMoney = jsonObject.getBigDecimal("dMoney");
        BlessEnvelopeGroup blessEnvelopeGroup = new BlessEnvelopeGroup();
        if (!StringUtil.isNullOrEmpty(adURL)) {
            blessEnvelopeGroup.setAdURL(adURL);
        }
        blessEnvelopeGroup.setExpiredtime(DateUtil.parseDateTime(expireDate));
        blessEnvelopeGroup.setId(groupId);
        blessEnvelopeGroup.setName(blessName);
        blessEnvelopeGroup.setCreatetime(DateTimeUtils.getCurrentDateTime());
        blessEnvelopeGroup.setFromuserid(userId);
        blessEnvelopeGroup.setFromusertype(Integer.parseInt(fromUserType));
        blessEnvelopeGroup.setIssinglecode(1);
        blessEnvelopeGroup.setRemark(blessDesc);
        blessEnvelopeGroup.setIspay(1);//是否已支付 0 未支付 1 已支付
        blessEnvelopeGroup.setIsbackpay(0);
        blessEnvelopeGroup.setAmount(dMoney);
        blessEnvelopeGroup.setBalance(dMoney);
        blessEnvelopeGroup.setGrands(2);//1 正常发福包  2 分等级发福包
        blessEnvelopeGroup.setNumber(intNumber);
        blessEnvelopeGroup.setAliveCount(intNumber);
        blessEnvelopeGroup.setSendtype(0);
        blessEnvelopeGroup.setType("2");
        blessEnvelopeGroup.setIsapplybackpay(0);
        blessEnvelopeGroupMapper.insertSelective(blessEnvelopeGroup);
        //向有效时间表中添加数据
        addBlessenvelopeVaild(validTime, groupId, 2);
        //向自助机和福包组对应的表中添加数据
        addBlessenvelopeMachine(machineIds, groupId);
        return blessEnvelopeGroup.getId();
    }

    /**
     * 等级福包(批量)
     *
     * @param params
     * @return
     */
    private void createGrandationBatch(JSONObject params) {
        log.info("进入批量创建等级福包，请求参数={}", params.toString());
        String userId = params.getString("userId");
        String adURL = params.getString("blessImage");
        String fromUserType = params.getString("fromUserType");
        String blessName = params.getString("blessName");
        //8001763,8001788,8001799
        String machineIds = params.getString("machineIds");
        //领取过期时间  2017-06-01 02:03:55
        String expireDate = params.getString("expireDate");
        //08:00:00_09:00:00-12:00:00_13:00:00-15:00:00_17:00:00
        String validTime = params.getString("validTime");
        //备注
        String blessDesc = params.getString("blessDesc");
        //分等级发福包，格式为：[{"key":10,"value":100},{"key":100,"value":10},{"key":1000,"value":1}]
        String gradeData = params.getString("gradeData");

        String[] machines = machineIds.split(",");
        for (String machine : machines) {
            String groupId = UUID.randomUUID().toString().replace("-", "");
            //向等级表中添加数据
            JSONObject jsonObject = addBlessEnvelopeGroupGrands(gradeData, groupId);
            int intNumber = jsonObject.getIntValue("intNumber");
            BigDecimal dMoney = jsonObject.getBigDecimal("dMoney");
            BlessEnvelopeGroup blessEnvelopeGroup = new BlessEnvelopeGroup();
            if (!StringUtil.isNullOrEmpty(adURL)) {
                blessEnvelopeGroup.setAdURL(adURL);
            }
            blessEnvelopeGroup.setExpiredtime(DateUtil.parseDateTime(expireDate));
            blessEnvelopeGroup.setId(groupId);
            blessEnvelopeGroup.setName(blessName);
            blessEnvelopeGroup.setCreatetime(DateTimeUtils.getCurrentDateTime());
            blessEnvelopeGroup.setFromuserid(userId);
            blessEnvelopeGroup.setFromusertype(Integer.parseInt(fromUserType));
            blessEnvelopeGroup.setIssinglecode(1);
            blessEnvelopeGroup.setRemark(blessDesc);
            blessEnvelopeGroup.setIspay(1);//是否已支付 0 未支付 1 已支付
            blessEnvelopeGroup.setIsbackpay(0);
            blessEnvelopeGroup.setAmount(dMoney);
            blessEnvelopeGroup.setBalance(dMoney);
            blessEnvelopeGroup.setGrands(2);//1 正常发福包  2 分等级发福包
            blessEnvelopeGroup.setNumber(intNumber);
            blessEnvelopeGroup.setAliveCount(intNumber);
            blessEnvelopeGroup.setSendtype(0);
            blessEnvelopeGroup.setType("2");
            blessEnvelopeGroup.setIsapplybackpay(0);
            blessEnvelopeGroupMapper.insertSelective(blessEnvelopeGroup);
            //向有效时间表中添加数据
            addBlessenvelopeVaild(validTime, groupId, 2);
            //向自助机和福包组对应的表中添加数据
            List<BlessEnvelopeGroupMachineR> blessEnvelopeGroupMachineRList = new ArrayList<>();
            Integer MachineId = Integer.parseInt(machine);
            BlessEnvelopeGroupMachineR blessEnvelopeGroupMachineR = new BlessEnvelopeGroupMachineR(groupId, MachineId);
            blessEnvelopeGroupMachineRList.add(blessEnvelopeGroupMachineR);
            blessEnvelopeMachineRMapper.addEnvelopeMachineList(blessEnvelopeGroupMachineRList);
        }
        log.info("进入创建等级福包(批量)添加完成！");
    }

    /**
     * <p>Title:      获取包含当前时间为有效期的福包组 </p>
     * <p>Description 有效时间段内，随机不同的福包组功能 </p>
     *
     * @Author zhangft
     * @CreateDate 2017/12/22 下午2:26
     */
    private BlessEnvelopeGroup getVaildGroup(List<BlessEnvelopeGroup> blessEnvelopeGroupList) {
        List<BlessEnvelopeGroup> validGroupList = new ArrayList<>();
        for (BlessEnvelopeGroup bless : blessEnvelopeGroupList) {
            List<BlessEnvelopeVaild> VaildTimes = blessEnvelopeVaildMapper.getByGroupId(bless.getId());
            if (VaildTimes.size() > 0) {
                for (BlessEnvelopeVaild vaild : VaildTimes) {
                    Date start, end;
                    try {
                        start = DateTimeUtils.getTime(vaild.getStartTime());
                        end = DateTimeUtils.getTime(vaild.getEndTime());
                        if (start.before(new Date()) && end.after(new Date())) {//当前时间在允许领取福包的时间段
                            //return bless;
                            validGroupList.add(bless);
                            break;
                        }
                    } catch (ParseException e) {
                        //可以继续处理下一个
                        log.info("日期转换异常");
                    }
                }
            }
        }

        if (validGroupList.size() == 0) {
            return null;
        }
        return validGroupList.get(0);
    }


    /**
     * <p>Title:      将随机码的固定几位用秘钥进行加密 </p>
     * <p>Description  </p>
     *
     * @Author zhangft
     * @CreateDate 2018/1/2 下午4:37
     */
    private String encodeRandStr(String randStr) {
        if (randStr == null) {
            throw new RuntimeException("randStr参数不能为空");
        }
        if (randStr.length() != 7) {
            throw new RuntimeException("randStr的长度必须为7位");
        }
        //加密 1，3，7的数字，秘钥为 gc123
        String str = randStr.substring(0, 1) + randStr.substring(2, 3) + randStr.substring(6, 7) + config.getRandNumSign();
        String md5Str = MD5.MD5Encode(str);
        return randStr + md5Str.substring(0, 2);
    }

    /**
     * <p>Title:      验证随机码本身的合法性 </p>
     * <p>Description  </p>
     *
     * @Author zhangft
     * @CreateDate 2018/1/2 下午4:37
     */
    private boolean checkRandStr(String randStr) {

        if (randStr == null) {
            return false;
        }
        if (randStr.length() != 9) {
            //throw new RuntimeException("randStr的长度必须为9位");
            return false;
        }
        String str = randStr.substring(0, 1) + randStr.substring(2, 3) + randStr.substring(6, 7) + config.getRandNumSign();
        String md5Str = MD5.MD5Encode(str);
        String checkStr = randStr.substring(7, 9);
        if (checkStr.equals(md5Str.substring(0, 2))) {
            return true;
        }
        return false;
    }

    /**
     * <p>Title:      获取领福包url </p>
     * <p>Description  </p>
     *
     * @Author zhangft
     * @CreateDate 2017/12/22 上午10:46
     */
    @Transactional
    public JSONObject getBlessEnvelopeURL(JSONObject params) {
        log.info("进入自助机获取二维码接口，参数为：" + params.toString());
        if (StringUtil.isNullOrEmpty(params.getString("machineId"))) {
            throw new BusinessException(ResultCodeEnum.ERR_41026, "自助机编码不能为空");
        }
        Integer machineId = Integer.parseInt(params.getString("machineId"));
        BlessEnvelopeGroup b = new BlessEnvelopeGroup();
        b.setMachineId(machineId);
        List<BlessEnvelopeGroup> groupList = blessEnvelopeGroupMapper.getAliveGroups(b);
        if (groupList.size() == 0) {
            throw new BusinessException(ResultCodeEnum.ERR_41027, "福包派发中,敬请期待");
        }
        BlessEnvelopeGroup group = groupList.get(0);  //先给个默认福包组
        BlessEnvelopeGroup vaildGroup = getVaildGroup(groupList);
        if (vaildGroup != null) {
            //优先使用当前时间段内的福包组
            group = vaildGroup;
        }

        Calendar calendar = Calendar.getInstance();
        Integer delayTime = config.getDelayTime();
        calendar.add(Calendar.MINUTE, delayTime);
        long expired = calendar.getTimeInMillis();

        JSONObject data = new JSONObject();
        if (!StringUtil.isNullOrEmpty(group.getAdURL())) {
            data.put("adURL", config.getPicture_url() + group.getAdURL());
        }
        String randStr = RandomUtil.randStr(7);
        randStr = encodeRandStr(randStr);
        String redirectURL = config.getBless_url() + "/q/m?" + randStr;
        data.put("url", redirectURL);

        String retStr = redisUtil.getSet(randStr, group.getId() + "#" + group.getType() + "#" + expired);
        while (!StringUtil.isNullOrEmpty(retStr)) {
            log.info("生成随机值=" + randStr);
            randStr = RandomUtil.randStr(7);
            randStr = encodeRandStr(randStr);
            retStr = redisUtil.getSet(randStr, group.getId() + "#" + group.getType() + "#" + expired);
        }
        return data;
    }

    private double randomAmount4Rand(int leftNum, double leftMoney, double maxAmount) {
        log.info("剩余个数{},总金额={},最大的随机金额={}", leftNum, leftMoney, maxAmount);
        DecimalFormat dcmFmt = new DecimalFormat("0.00");
        if (leftNum == 1) {
            log.info("最后一个福包，返回随机金额={}", leftMoney);
            return Double.valueOf(dcmFmt.format(leftMoney));
        }
        Random r = new Random();
        double min = 0.01;
        double max = (leftMoney / leftNum) * 2;
        double money = r.nextDouble() * max;
        money = money <= min ? 0.01 : money;
        log.info("剩余{}个福包，返回随机金额={}", leftNum, money);
        return Double.valueOf(dcmFmt.format(money));
    }

    /**
     * <p>Title:      判断用户是否领过福包 </p>
     * <p>Description  </p>
     *
     * @Author zhangft
     * @CreateDate 2017/12/28 下午2:22
     */
    private boolean isFirstGetBless(String userId) {
        BlessEnvelope blessEnvelope = new BlessEnvelope();
        blessEnvelope.setReceivememberid(userId);
        List<BlessEnvelope> blessEnvelopeList = blessEnvelopeMapper.queryByBlessEnvlope(blessEnvelope);
        if (blessEnvelopeList != null && blessEnvelopeList.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * <p>Title:      判断是否领过推广福包 </p>
     * <p>Description  </p>
     *
     * @Author zhangft
     * @CreateDate 2018/1/3 下午1:45
     */
    private boolean isGetSpreadBless(String userId, String groupId) {
        BlessEnvelope blessEnvelope = new BlessEnvelope();
        blessEnvelope.setReceivememberid(userId);
        blessEnvelope.setGroupid(groupId);
        List<BlessEnvelope> blessEnvelopeList = blessEnvelopeMapper.queryByBlessEnvlope(blessEnvelope);
        if (blessEnvelopeList != null && blessEnvelopeList.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * <p>Title:      领福包接口，包含各种类型的福包 </p>
     * <p>Description 推广福包的是固定的随机码，因为推广福包没有调用生成随机码的接口 </p>
     *
     * @Author zhangft
     * @CreateDate 2018/1/3 上午10:50
     */
    @Transactional
    public JSONObject getBlessEnvelope(JSONObject params) {
        log.info("进入用户获取福包接口，参数为：" + params.toString());
        String userId = params.getString("userId");    //是4A平台的guid
        String randNum = params.getString("randNum");  //福包组对应的随机码

        if (StringUtil.isNullOrEmpty(userId, randNum)) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVIDATE, "请检查参数【userId,randNum】是否为空");
        }

        String groupId = "";

        if ("9LGmJX97a".equals(randNum)) {
            groupId = config.getSpreadGroupId();
        }
        if (!"9LGmJX97a".equals(randNum)) {

            if (!checkRandStr(randNum)) {
                throw new BusinessException(ResultCodeEnum.ERR_41042, "福包二维码不合法");
            }

            String randStr = redisUtil.getValue(randNum);
            //随机码查询策略，首先查询缓存有没有，再次查询数据库(主要是推广码，没有存在redis中)
            if (StringUtil.isNullOrEmpty(randStr)) {
                throw new BusinessException(ResultCodeEnum.ERR_41030, "福包已过期");
            }


            String[] strArr = randStr.split("#");
            groupId = strArr[0];
        }

        AuthMember member = authMemberMapper.selectByPrimaryKey(userId);
        if (member == null) {
            throw new BusinessException(ResultCodeEnum.ERR_41031, "您还不是福包用户，请先注册");
        }

        String thisDay = new SimpleDateFormat("yyyyMMdd").format(new Date());
        BlessEnvelope condition = new BlessEnvelope();

        if (!config.getSuperAdmin().equals(userId)) {
            condition.setReceivememberid(userId);
            condition.setGroupid(groupId);
            condition.setThisday(thisDay);
            int count = blessEnvelopeMapper.checkReceivedNumThisDay(condition);

            if (count > 0 && "9LGmJX97a".equals(randNum)) {  //推广福包
                throw new BusinessException(ResultCodeEnum.ERR_41032, "福包已领取");
            }

            //测试，临时去掉
            if (count > 0) {
                throw new BusinessException(ResultCodeEnum.ERR_41032, "同一个福包每天只能领一次哦");
            }
        }

        double amount = 0;
        BlessEnvelopeGroup blessEnvelopeGroup;


        //if (distributeLockUtil.lock(groupId)) {

        //synchronized (groupId.intern()) {
        blessEnvelopeGroup = blessEnvelopeGroupMapper.selectByPrimaryKey(groupId);//获取福包组

        log.info("查询福包组数据=" + blessEnvelopeGroup.toString());
        if ("1".equals(blessEnvelopeGroup.getType())) {   //普通随机福包
            log.info("进入领取普通随机福包");

            Integer aliveCount = blessEnvelopeGroup.getAliveCount();
            BigDecimal balance = blessEnvelopeGroup.getBalance();//福包组剩余金额

            //随机福包领完的判断标准是，个数为0或者金额为0
            if (balance.doubleValue() <= 0 || aliveCount <= 0) {
                throw new BusinessException(ResultCodeEnum.ERR_41033, "来晚啦,该福包已被领完！");
            }

            //随机福包的个数有意义，可以确定福包金额的大小
            double randMax = 0;
            if (blessEnvelopeGroup.getRandMax() != null) {
                randMax = blessEnvelopeGroup.getRandMax().doubleValue();
            }
            amount = randomAmount4Rand(aliveCount, balance.doubleValue(), randMax); //获取随机福包金额

            log.info("普通随机福包金额=" + amount);
        } else if ("2".equals(blessEnvelopeGroup.getType())) {   //等级福包
            log.info("进入领取等级福包");

            List<BlessEnvelopeGroupGrands> grands = blessEnvelopeGroupGrandsMapper.selectByGroupId(groupId);//获取该福包组的每个等级对应金额以及
            //该等级所拥有的福报数量（数量不为0）
            if (grands.size() == 0 || grands.isEmpty()) {
                throw new BusinessException(ResultCodeEnum.ERR_41033, "来晚啦,该福包已被领完！");
            }

            Integer aliveCount = blessEnvelopeGroup.getAliveCount();
            BigDecimal balance = blessEnvelopeGroup.getBalance();//福包组剩余金额

            //随机福包领完的判断标准是，个数为0或者金额为0
            if (balance.doubleValue() <= 0 || aliveCount <= 0) {
                throw new BusinessException(ResultCodeEnum.ERR_41033, "来晚啦,该福包已被领完！");
            }

            int weightSum = 0;
            for (BlessEnvelopeGroupGrands b : grands) {
                weightSum += b.getCount();
            }

            Random random = new Random();

            Integer n = random.nextInt(weightSum); // n in [0, weightSum)
            Integer m = 0;

            BigDecimal univalence = new BigDecimal("0");
            int count = 0;

            BlessEnvelopeGroupGrands beg4Update = new BlessEnvelopeGroupGrands();
            for (BlessEnvelopeGroupGrands beg : grands) {
                if (m <= n && n < m + beg.getCount()) {

                    univalence = beg.getUnivalence();
                    count = beg.getCount();

                    beg4Update = beg;

                    break;
                }
                m += beg.getCount();
            }

            amount = univalence.doubleValue();//获取福包金额

            log.info("等级福包金额=" + amount);

            beg4Update.setCount(count - 1);//将对应等级福包数量减一
            blessEnvelopeGroupGrandsMapper.updateByPrimaryKeySelective(beg4Update);

        } else if ("3".equals(blessEnvelopeGroup.getType())) {
            log.info("进入领取推广福包");

            //首先判断是否领过推广福包，如果已经领取过，则提示已领取过，不能重复领取
            if (isGetSpreadBless(userId, groupId)) {
                throw new BusinessException(ResultCodeEnum.ERR_41043, "福包已领取");
            }

            BigDecimal balance = blessEnvelopeGroup.getBalance();//福包组剩余金额
            if (balance.doubleValue() <= 0) {
                throw new BusinessException(ResultCodeEnum.ERR_41033, "来晚啦,该福包已被领完！");
            }

            //判断新老用户，新老用户的判断依据是是否领过任意类型的福包
            boolean isFirstGetBlessUser = isFirstGetBless(userId);
            if (!isFirstGetBlessUser) {
                amount = fixBlessMoney;
            } else {
                //推广福包的金额要小于6元且小于等于当前剩余推广福包总金额
                amount = new Random().nextDouble() + new Random().nextInt(6); //获取6元以下随机金额

                DecimalFormat dcmFmt = new DecimalFormat("0.00");

                amount = Double.valueOf(dcmFmt.format(amount));

                //如果剩余金额不足6元，且随机金额大于剩余金额,则将剩余金额全部领完，防止出现负数
                if (balance.doubleValue() < 6 && amount > balance.doubleValue()) {
                    amount = balance.doubleValue();
                }
            }

            log.info("推广福包金额=" + amount);
        }

        return vaild(blessEnvelopeGroup, userId, amount, randNum);
        //}
        //}

        //throw new BusinessException(ResultCodeEnum.FAIL, "服务器繁忙");

    }

    private JSONObject vaild(BlessEnvelopeGroup blessEnvelopeGroup, String userId, double amount, String randNum) {
        JSONObject data = new JSONObject();
        List<BlessEnvelopeVaild> vaildTimes = blessEnvelopeVaildMapper.getByGroupId(blessEnvelopeGroup.getId());//获取每日可以领取该福包组的有效时间段集合
        boolean flag = false;
        //判断是否在有效时间段内
        if (vaildTimes.size() == 0) {
            throw new BusinessException(ResultCodeEnum.ERR_41034, "该福包未设置有效时间段！");
        }

        for (BlessEnvelopeVaild vaild : vaildTimes) {
            try {
                Date start = DateTimeUtils.getTime(vaild.getStartTime());
                Date end = DateTimeUtils.getTime(vaild.getEndTime());
                if (start.before(new Date()) && end.after(new Date())) {
                    flag = true;
                }
            } catch (Exception e) {
                log.info("日期格式转换错误");
                continue;
            }

            if (flag) {//当前时间在允许领取福包的时间段

                data.put("beName", blessEnvelopeGroup.getName());
                data.put("groupId", blessEnvelopeGroup.getId());
                //插入新的领取福包的记录
                BlessEnvelope blessEnvelope = new BlessEnvelope();
                String id = UUID.randomUUID().toString();//单个福包Id
                blessEnvelope.setId(id);
                blessEnvelope.setReceivememberid(userId);
                blessEnvelope.setReceivetime(DateTimeUtils.getCurrentDateTime());
                blessEnvelope.setIsopened(1);
                blessEnvelope.setOpentime(DateTimeUtils.getCurrentDateTime());
                blessEnvelope.setCreatetime(DateTimeUtils.getCurrentDateTime());
                blessEnvelope.setType(vaild.getType());
                blessEnvelope.setAmount(new BigDecimal(amount));
                blessEnvelope.setLotterytickettypename("");
                blessEnvelope.setReceiveexpiredtime(blessEnvelopeGroup.getExpiredtime());
                blessEnvelope.setRemark(blessEnvelopeGroup.getName());
                blessEnvelope.setFromuserid(blessEnvelopeGroup.getFromuserid());
                blessEnvelope.setFromusertype(blessEnvelopeGroup.getFromusertype());
                blessEnvelope.setGroupid(blessEnvelopeGroup.getId());
                blessEnvelope.setOriginalid(id);
                blessEnvelope.setIsused(1);
                blessEnvelope.setIstransfer(0);
                int count = blessEnvelopeMapper.insertSelective(blessEnvelope);
                if (count == 0) {
                    throw new BusinessException(ResultCodeEnum.ERR_41035, "福包信息插入失败");
                }
                //更改福包组剩余福包数量以及金额
                BlessEnvelopeGroup group = new BlessEnvelopeGroup();
                group.setBalance(blessEnvelopeGroup.getBalance().subtract(new BigDecimal(amount)));
                group.setAliveCount(blessEnvelopeGroup.getAliveCount() - 1);
                group.setId(blessEnvelopeGroup.getId());
                int updateCount = blessEnvelopeGroupMapper.updateByPrimaryKeySelective(group);
                if (updateCount == 0) {
                    throw new BusinessException(ResultCodeEnum.ERR_41036, "福包组信息更新失败");
                }
                //更改福包对应自助机的状态
                int alive = blessEnvelopeGroup.getAliveCount() - 1;
                if (alive == 0) {
                    //直接通过groupId批量更新机器状态
                    BlessEnvelopeGroupMachineR blessEnvelopeGroupMachineR = new BlessEnvelopeGroupMachineR();
                    blessEnvelopeGroupMachineR.setBlessEnvelopeGroupId(blessEnvelopeGroup.getId());
                    blessEnvelopeGroupMachineR.setStatus(0);
                    blessEnvelopeMachineRMapper.updateStatusByGroupId(blessEnvelopeGroupMachineR);

                }
                //将金额转入对应福包账户
                //Map<String, Object> param = new LinkedHashMap<>();
                JSONObject param = new JSONObject();
                param.put("amount", String.valueOf(amount));
                param.put("userId", userId);
                param.put("clientID", signClient.getClientId());
                param.put("nonce_str", UUID.randomUUID().toString());

                //String sign = Signature.getSign(param, signClient.getClientSecret());
                //param.put("sign", sign);
                //log.info("请求地址为={}", config.getPayment_url() + config.getAccountChargeURL());
                log.info("请求参数为={}", param);
                //JSONObject result = HttpUtils.sendPostTwo(config.getPayment_url() + config.getAccountChargeURL(), param);
                RWrapper<JSONObject> result = paymentFeignService.getPackage(param);
                log.info("payment返回结果={}", result);
                if (!"1000".equals(result.getCode())) {
                    throw new BusinessException(ResultCodeEnum.ERR_41037, result.getMsg());
                }
                break;
            }
        }
        if (!flag) {
            String vaileTime = getValidTimeStr(vaildTimes);
            throw new BusinessException(ResultCodeEnum.ERR_41041, vaileTime);
        }
        data.put("amount", amount);
        data.put("type", blessEnvelopeGroup.getType());
        data.put("userId", userId);

        try {
            //测试 临时去掉
            redisUtil.removeKey(randNum);
        } catch (Exception e) {
            log.error("删除randNum出现系统异常，异常信息={}", e.getMessage(), e);
        }
        return data;
    }

    /**
     * <p>Title:      解析随机码，跳转对应页面，支持福包app和微信以及第三方浏览器，该地址会重定向到真正的领福包地址 </p>
     * <p>Description 推广类型福包不经过此方法，所以在此不考虑推广福包 </p>
     *
     * @Author zhangft
     * @CreateDate 2017/12/22 上午10:52
     */
    @Transactional
    public void mappingURL(JSONObject requestParam, HttpServletResponse res) throws IOException {
        log.info("获取映射地址接口，参数为：" + requestParam.toString());
        String randNum = requestParam.getString("randNum");
        String userId = requestParam.getString("userId");

        if (StringUtil.isNullOrEmpty(randNum)) {
            throw new BusinessException(ResultCodeEnum.ERR_41028, "随机码不能为空");
        }

        if (!checkRandStr(randNum)) {
            //二维码不合法展示页面
            //throw new BusinessException(ResultCodeEnum.ERR_41042, "福包二维码不合法");
            log.info("进入错误页面,福包二维码不合法");
            res.sendRedirect(config.getErrorBlessPage() + "?errorCode=41042");
            return;
        }

        String randStr = redisUtil.getValue(randNum);

        //随机码查询策略，首先查询缓存有没有，再次查询数据库(主要是推广码，没有存在redis中)
        if (StringUtil.isNullOrEmpty(randStr)) {
            //福包过期展示页面
            //throw new BusinessException(ResultCodeEnum.ERR_41030, "福包已过期");
            log.info("进入错误页面,福包已过期");
            res.sendRedirect(config.getErrorBlessPage() + "?errorCode=41030");
            return;
        }

        String[] strArr = randStr.split("#");
        String groupId = strArr[0];
        BlessEnvelopeGroup blessEnvelopeGroup = blessEnvelopeGroupMapper.selectByPrimaryKey(groupId);
        String adURL = blessEnvelopeGroup.getAdURL();
        String url;

        if ("1".equals(requestParam.getString("source"))) { //微信扫自助机二维码
            //传递randNum到页面
            url = config.getGetEnvelopeByWechat() + "?blessURL=" + config.getBless_url() + "&randNum=" + randNum + "&adURL=" + adURL;//跳转到手机号领取福包页面
        } else if ("2".equals(requestParam.getString("source"))) {
            //传递randNum和userId到页面
            url = config.getGetEnvelopeByApp() + "?blessURL=" + config.getBless_url() + "&randNum=" + randNum + "&adURL=" + adURL + "&userId=" + userId;
        } else {
            //其他情况跳转至注册页面
            url = config.getGetEnvelopeByWechat() + "?blessURL=" + config.getBless_url() + "&randNum=" + randNum + "&adURL=" + adURL;//跳转到手机号领取福包页面
        }
        log.info("进入url={}", url);
        res.sendRedirect(url);
    }

    /**
     * 向有效时间表中添加数据
     *
     * @param validTime
     * @param groupId
     * @param type
     */
    public void addBlessenvelopeVaild(String validTime, String groupId, int type) {
        log.info("向有效时间表中添加数据，validTime={},groupId={},type={}", validTime, groupId, type);
        //将有效时间字符串转换为map
        Map<String, String> timeMap = getValidTimeMap(validTime);
        Set<Map.Entry<String, String>> dailyVaild = timeMap.entrySet();
        List<BlessEnvelopeVaild> blessEnvelopeVaildList = new ArrayList<>();
        for (Map.Entry<String, String> vaildTime : dailyVaild) {
            String id = UUID.randomUUID().toString().replace("-", "");
            BlessEnvelopeVaild blessEnvelopeVaild = new BlessEnvelopeVaild();
            blessEnvelopeVaild.setId(id);
            blessEnvelopeVaild.setGroupid(groupId);
            blessEnvelopeVaild.setStartTime(vaildTime.getKey());
            blessEnvelopeVaild.setEndTime(vaildTime.getValue());
            blessEnvelopeVaild.setType(type);
            blessEnvelopeVaildList.add(blessEnvelopeVaild);
        }
        blessEnvelopeVaildMapper.insertBlessEnvelopeVaildList(blessEnvelopeVaildList);
        log.info("向有效时间表中添加数据完成！");
    }

    /**
     * 向自助机和福包组对应的表中添加数据
     *
     * @param machineIds
     * @param groupId
     */
    public void addBlessenvelopeMachine(String machineIds, String groupId) {
        log.info("向自助机和福包组对应的表中添加数据，machineIds={},groupId={}", machineIds, groupId);
        String[] machines = machineIds.split(",");
        List<BlessEnvelopeGroupMachineR> blessEnvelopeGroupMachineRList = new ArrayList<>();
        for (String machine : machines) {
            Integer MachineId = Integer.parseInt(machine);
            BlessEnvelopeGroupMachineR blessEnvelopeGroupMachineR = new BlessEnvelopeGroupMachineR(groupId, MachineId);
            blessEnvelopeGroupMachineRList.add(blessEnvelopeGroupMachineR);
        }
        blessEnvelopeMachineRMapper.addEnvelopeMachineList(blessEnvelopeGroupMachineRList);
        log.info("向自助机和福包组对应的表中添加数据完成！");
    }

    /**
     * 向等级表中添加数据
     *
     * @param gradeData
     */
    public JSONObject addBlessEnvelopeGroupGrands(String gradeData, String groupId) {
        log.info("向等级表中添加数据gradeData={},groupId={}", gradeData, groupId);
        int intNumber = 0;
        BigDecimal dMoney = BigDecimal.ZERO;
        JSONArray jsonArray = JSONArray.parseArray(gradeData);
        List<BlessEnvelopeGroupGrands> grandList = new ArrayList<>();
        if (!jsonArray.isEmpty() && jsonArray.size() != 0) {
            JSONObject blessInfoDetail;
            int count;
            BigDecimal perAmount;
            for (int i = 0; i <= jsonArray.size() - 1; i++) {
                String id = UUID.randomUUID().toString().replace("-", "");
                blessInfoDetail = jsonArray.getJSONObject(i);
                count = blessInfoDetail.getInteger("key");
                perAmount = blessInfoDetail.getBigDecimal("value");
                dMoney = dMoney.add(perAmount.multiply(new BigDecimal(count)));
                intNumber += count;
                BlessEnvelopeGroupGrands grand = new BlessEnvelopeGroupGrands();
                grand.setId(id);
                grand.setCount(count);
                grand.setGroupid(groupId);
                grand.setUnivalence(perAmount);
                grand.setOriginalCount(count);
                grand.setOriginalAmount(perAmount);
                grand.setCreateTime(new Date());
                grandList.add(grand);
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("intNumber", intNumber);
        jsonObject.put("dMoney", dMoney);
        blessEnvelopeGroupGrandsMapper.insertGroupGrandList(grandList);
        log.info("向等级表中添加数据完成jsonObject={}", jsonObject);
        return jsonObject;
    }

}
