package com.bkgc.bless.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.bless.*;
import com.bkgc.bean.order.OrderDailyStatistics;
import com.bkgc.bean.order.OrderSerachParam;
import com.bkgc.bless.config.Config;
import com.bkgc.bless.config.SignClient;
import com.bkgc.bless.mapper.*;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Transactional(propagation = Propagation.REQUIRED)
@Service
public class BlessEnvelopeService {

    @Autowired
    private BlessEnvelopeGroupMapper blessEnvelopeGroupMapper;

    @Autowired
    private BlessEnvelopeMapper blessEnvelopeMapper;


    @Autowired
    private BlessEnvelopeRMachineMapper blessEnvelopeRMachineMapper;

    @Autowired
    private BlessEnvelopeVaildMapper blessEnvelopeVaildMapper;

    @Autowired
    private BlessEnvelopeMachineRMapper blessEnvelopeMachineRMapper;


    @Autowired
    private RandomMappingGroupMapper randomMappingGroupMapper;


    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private Config config;

    @Resource
    private SignClient signClient;


    /**
     * 获取每个人的福包记录
     *
     * @param params
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public JSONObject getMyReceivedBlessEnvelopeList(JSONObject params) {
        String userId = params.getString("userId");
        if (StringUtil.isNullOrEmpty(userId)) {
            throw new BusinessException(ResultCodeEnum.ERR_41058, "用户编号不能为空");
        }

        String pageSize = params.getString("pageSize");
        if (StringUtil.isNullOrEmpty(pageSize)) {
            throw new BusinessException(ResultCodeEnum.ERR_41077, "每页记录数量不能为空");
        }
        int intPageSize = Integer.parseInt(pageSize);

        String pageIndex = params.getString("pageIndex");
        if (StringUtil.isNullOrEmpty(pageIndex)) {
            throw new BusinessException(ResultCodeEnum.ERR_41078, "页码不能为空");
        }
        int intPageIndex = Integer.parseInt(pageIndex);

        JSONObject data = new JSONObject();

        BlessEnvelope condition = new BlessEnvelope();
        condition.setReceivememberid(userId);
        condition.setPageStart(intPageIndex * intPageSize);
        condition.setPageSize(intPageSize);
        condition.setIsopened(1);
        List<ReceivedBlessEnvelope> blessEnvelopes = blessEnvelopeMapper.getReceivedBes(condition);
        JSONArray list = new JSONArray();
        if (blessEnvelopes != null && blessEnvelopes.size() != 0) {
            for (ReceivedBlessEnvelope envelope : blessEnvelopes) {
                JSONObject d = new JSONObject();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String ReceiveTime = sdf.format(envelope.getReceivetime());
                d.put("Id", envelope.getId());
                d.put("Type", envelope.getType());
                d.put("Amount", envelope.getAmount() + "");
                d.put("Remark", envelope.getRemark());
                d.put("ReceiveTime", ReceiveTime);
                d.put("IsOpened", envelope.getIsopened());
                d.put("FromName", envelope.getRemark());
                list.add(d);

            }
        }
        data.put("List", list);
        //数量
        int count = blessEnvelopeMapper.countBlessEnvelope(condition);
        data.put("Count", count);
        return ResultUtil.buildSuccessResult(1000, data);
    }


    public JSONObject getReceivedBlessEnvelopeList(JSONObject params) {
        String userId = params.getString("UserId");
        if (StringUtil.isNullOrEmpty(userId)) {
            throw new BusinessException(ResultCodeEnum.ERR_41058, "用户编号不能为空");
        }

        String pageSize = params.getString("PageSize");
        if (StringUtil.isNullOrEmpty(pageSize)) {
            throw new BusinessException(ResultCodeEnum.ERR_41077, "每页记录数量不能为空");
        }
        int intPageSize = Integer.parseInt(pageSize);

        String pageIndex = params.getString("PageIndex");
        if (StringUtil.isNullOrEmpty(pageIndex)) {
            throw new BusinessException(ResultCodeEnum.ERR_41078, "页码不能为空");
        }
        int intPageIndex = Integer.parseInt(pageIndex);
        JSONObject data = new JSONObject();
        int indexStart = intPageIndex * intPageSize;
        BlessEnvelope condition = new BlessEnvelope();
        condition.setReceivememberid(userId);
        condition.setPageStart(indexStart);
        condition.setPageSize(intPageSize);
        condition.setIsopened(1);
        List<ReceivedBlessEnvelope> blessEnvelopes = blessEnvelopeMapper.getReceivedBes(condition);
        JSONArray list = new JSONArray();
        if (blessEnvelopes != null && blessEnvelopes.size() != 0) {
            for (ReceivedBlessEnvelope envelope : blessEnvelopes) {
                JSONObject d = new JSONObject();
                d.put("Id", envelope.getId());
                d.put("Type", envelope.getType());
                d.put("Amount", envelope.getAmount());
                d.put("Remark", envelope.getRemark());
                d.put("ReceiveTime", DateTimeUtils.getTimestamp(envelope.getReceivetime()));
                d.put("IsOpened", envelope.getIsopened());
                d.put("FromName", envelope.getFromName());
                list.add(d);
            }
        }
        data.put("List", list);
        //数量
        int count = blessEnvelopeMapper.countBlessEnvelope(condition);
        data.put("Count", count);
        return ResultUtil.buildSuccessResult(1000, data);
    }


    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public JSONObject getBlessEnvelopeGroupId(JSONObject params) {
        Integer machineId;
        if (StringUtil.isNullOrEmpty(params.getString("machineId"))) {
            throw new BusinessException(ResultCodeEnum.ERR_41079, "自主机编码不能为空");
        } else {
            machineId = Integer.parseInt(params.getString("machineId"));
        }
        List<BlessEnvelopeRMachine> blessEnvelopeRMachines = blessEnvelopeRMachineMapper.getByMachineId(machineId);
        BlessEnvelopeRMachine group = null;
        if (blessEnvelopeRMachines != null && blessEnvelopeRMachines.size() != 0) {
            Random rand = new Random();
            int randIndex = rand.nextInt(blessEnvelopeRMachines.size());
            group = blessEnvelopeRMachines.get(randIndex);
        }
        JSONObject data;
        if (group != null) {
            data = new JSONObject();
            data.put("groupId", group.getBlessenvelopegroupId());
            data.put("msg", "获取福包Id成功");
        } else {
            throw new BusinessException(ResultCodeEnum.ERR_41080, "未成功获取福包,请重试");
        }
        return ResultUtil.buildSuccessResult(1000, data);
    }


    /**
     * 获取20个自助机的福包数量
     *
     * @param params
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public JSONObject getDefault20MachinesByCurrentLoaction(JSONObject params) {
        JSONObject data = new JSONObject();
        log.info("进入自助机获取福包接口，参数为：" + params);
        String machineIds = params.getString("machineIds");//自助机组：8001763-8001788-8001766

        String[] machines = machineIds.split("-");
        Map map = new HashMap<String, String>();
        for (String machineId : machines) {
            int count = blessEnvelopeGroupMapper.getCountByMachineId(machineId);
            map.put(machineId, count);
        }
        data.put("result", map);
        log.info("返回结果为：" + data);
        return ResultUtil.buildSuccessResult(1000, data);
    }


    /**
     * 根据指定条件获取福包组信息
     *
     * @param params
     * @return
     * @throws ParseException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public JSONObject getBlessEnvelopeList(JSONObject params) throws ParseException {
        JSONObject resultData = new JSONObject();
        String storeId = params.getString("storeId");
        String adUserId = params.getString("adUserId");
        String machId = params.getString("machineId");

        String startTime = params.getString("startTime");
        if (StringUtil.isNullOrEmpty(startTime)) {
            return ResultUtil.buildFailResult("参数startTime不能为空");
        }
        String endTime = params.getString("endTime");
        if (StringUtil.isNullOrEmpty(endTime)) {
            return ResultUtil.buildFailResult("参数endTime不能为空");
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date start = sdf.parse(startTime);
        Date end = sdf.parse(endTime);
        Calendar canlender = Calendar.getInstance();
        canlender.setTime(end);
        int day = canlender.get(Calendar.DATE);
        canlender.set(Calendar.DATE, day + 1);
        Date time = canlender.getTime();

        BlessEnvelope blessEnvlope = new BlessEnvelope();
        blessEnvlope.setCreatetime(start);
        blessEnvlope.setEndTime(time);


        BlessEnvelopeGroup b = new BlessEnvelopeGroup();

        List<String> list = new ArrayList<String>();//存放商家所拥有的自助机编码

        if (!StringUtil.isNullOrEmpty(adUserId)) {
            b.setFromuserid(adUserId);
        }
        if (!StringUtil.isNullOrEmpty(machId)) {
            b.setMachineId(Integer.parseInt(machId));
        }

        if (!StringUtil.isNullOrEmpty(storeId)) {
            Map<String, Object> machineParam = new LinkedHashMap<String, Object>();
            machineParam.put("storeId", storeId);
            /*JSONObject result = sendPost(config.getMachine_url() + config.getGetMachineIdURL(), machineParam);
            if ("1000".equals(result.getString("code"))) {
                String data = result.getString("data");
                String[] machineIds = data.split(",");
                for (String machineId : machineIds) {
                    list.add(machineId);
                }
            } else {
                return ResultUtil.buildFailResult(Integer.parseInt(result.getString("code")), result.getString("msg") + result.getString("data"));
            }*/
        }


        List<Map> groupList = new ArrayList<Map>();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (list.size() != 0) {
            for (String machineId : list) {
                b.setMachineId(Integer.parseInt(machineId));
                List<BlessEnvelopeGroup> group = blessEnvelopeGroupMapper.getGroup(b);
                for (BlessEnvelopeGroup bel : group) {
                    Map<String, Object> groupInfo = new HashMap<String, Object>();
                    Integer isOpend = bel.getNumber() - bel.getAliveCount();
                    String groupCreateTime = format.format(bel.getCreatetime());
                    groupInfo.put("groupId", bel.getId());
                    groupInfo.put("groupCreateTime", groupCreateTime);
                    groupInfo.put("isOpend", isOpend.toString());
                    groupInfo.put("noOpend", bel.getAliveCount().toString());


                    blessEnvlope.setGroupid(bel.getId());
                    List<BlessEnvelope> byGroupId = blessEnvelopeMapper.queryByBlessEnvlope(blessEnvlope);
                    Map[] map = new Map[byGroupId.size()];
                    for (int i = 0; i < byGroupId.size(); i++) {


                        String dateTime = format.format(byGroupId.get(i).getCreatetime());

                        Map<String, String> blessMap = new HashMap<String, String>();
                        blessMap.put("envlopeId", byGroupId.get(i).getId());
                        blessMap.put("envelopeCreateTime", dateTime);
                        blessMap.put("formUserId", byGroupId.get(i).getFromuserid());
                        blessMap.put("remark", byGroupId.get(i).getRemark());
                        map[i] = blessMap;
                    }
                    groupInfo.put("envelopes", map);
                    groupList.add(groupInfo);
                }
            }
        } else {
            List<BlessEnvelopeGroup> group = blessEnvelopeGroupMapper.getGroup(b);
            for (BlessEnvelopeGroup bel : group) {
                Map<String, Object> groupInfo = new HashMap<String, Object>();
                Integer isOpend = bel.getNumber() - bel.getAliveCount();
                String groupCreateTime = format.format(bel.getCreatetime());
                groupInfo.put("groupId", bel.getId());
                groupInfo.put("isOpend", isOpend.toString());
                groupInfo.put("groupCreateTime", groupCreateTime);
                groupInfo.put("noOpend", bel.getAliveCount().toString());

                blessEnvlope.setGroupid(bel.getId());
                List<BlessEnvelope> byGroupId = blessEnvelopeMapper.queryByBlessEnvlope(blessEnvlope);
                Map[] map = new Map[byGroupId.size()];
                for (int i = 0; i < byGroupId.size(); i++) {
                    String dateTime = format.format(byGroupId.get(i).getCreatetime());
                    Map<String, String> blessMap = new HashMap<String, String>();
                    blessMap.put("envlopeId", byGroupId.get(i).getId());
                    blessMap.put("envelopeCreateTime", dateTime);
                    blessMap.put("formUserId", byGroupId.get(i).getFromuserid());
                    blessMap.put("remark", byGroupId.get(i).getRemark());
                    map[i] = blessMap;
                }
                groupInfo.put("envelopes", map);
                groupList.add(groupInfo);
            }
        }
        resultData.put("groupData", groupList);
        return ResultUtil.buildSuccessResult(1000, resultData);
    }


    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public JSONObject getBlessEnvelopeGroupList(JSONObject params) throws ParseException {
        JSONObject resultData = new JSONObject();
        log.info("根据指定条件获取福包组信息接口，参数为：" + params);
        String storeId = params.getString("storeId");
        //String dateOfType = params.getString("dateOfType");	//1：昨天:2：最近一周:3：最近一个月，默认：所有时间
        String adUserId = params.getString("adUserId");
        String machId = params.getString("machineId");


        String startTime = params.getString("startTime");
        if (StringUtil.isNullOrEmpty(startTime)) {
            return ResultUtil.buildFailResult("参数startTime不能为空");
        }
        String endTime = params.getString("endTime");
        if (StringUtil.isNullOrEmpty(endTime)) {
            return ResultUtil.buildFailResult("参数endTime不能为空");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd ");
        Date start = sdf.parse(startTime);
        Date end = sdf.parse(endTime);
        Calendar canlender = Calendar.getInstance();
        canlender.setTime(end);
        int day = canlender.get(Calendar.DATE);
        canlender.set(Calendar.DATE, day + 1);
        Date time = canlender.getTime();


        BlessEnvelopeGroup b = new BlessEnvelopeGroup();
        b.setCreatetime(start);
        b.setEndTime(time);


        List<String> list = new ArrayList<String>();//存放商家所拥有的自助机编码

        if (!StringUtil.isNullOrEmpty(adUserId)) {
            b.setFromuserid(adUserId);
        }
        if (!StringUtil.isNullOrEmpty(machId)) {
            b.setMachineId(Integer.parseInt(machId));
        }

        if (!StringUtil.isNullOrEmpty(storeId)) {
            Map<String, Object> machineParam = new LinkedHashMap<String, Object>();
            machineParam.put("storeId", storeId);
            /*JSONObject result = sendPost(config.getMachine_url() + config.getGetMachineIdURL(), machineParam);
            if ("1000".equals(result.getString("code"))) {
                String data = result.getString("data");
                String[] machineIds = data.split(",");
                for (String machineId : machineIds) {
                    list.add(machineId);
                }
            } else {
                return ResultUtil.buildFailResult(Integer.parseInt(result.getString("code")), result.getString("msg") + result.getString("data"));
            }*/
        }

        List<Map> groupList = new ArrayList<Map>();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (list.size() != 0) {
            for (String machineId : list) {
                b.setMachineId(Integer.parseInt(machineId));
                List<BlessEnvelopeGroup> group = blessEnvelopeGroupMapper.getGroup(b);
                for (BlessEnvelopeGroup bel : group) {
                    Map<String, Object> groupInfo = new HashMap<String, Object>();
                    Integer isOpend = bel.getNumber() - bel.getAliveCount();
                    String dateTime = format.format(bel.getCreatetime());
                    groupInfo.put("groupId", bel.getId());
                    groupInfo.put("isOpend", isOpend.toString());
                    groupInfo.put("noOpend", bel.getAliveCount().toString());
                    groupInfo.put("groupCreateTime", dateTime);
                    groupList.add(groupInfo);
                }
            }
        } else {
            List<BlessEnvelopeGroup> group = blessEnvelopeGroupMapper.getGroup(b);
            for (BlessEnvelopeGroup bel : group) {
                Map<String, Object> groupInfo = new HashMap<String, Object>();
                String dateTime = format.format(bel.getCreatetime());
                Integer isOpend = bel.getNumber() - bel.getAliveCount();
                groupInfo.put("groupId", bel.getId());
                groupInfo.put("isOpend", isOpend.toString());
                groupInfo.put("noOpend", bel.getAliveCount().toString());
                groupInfo.put("groupCreateTime", dateTime);
                groupList.add(groupInfo);
            }
        }

        resultData.put("groupData", groupList);
        return ResultUtil.buildSuccessResult(1000, resultData);
    }


//    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
//    public void mappingURL(JSONObject requestParam, HttpServletResponse res) throws IOException {
//        log.info("获取映射地址接口，参数为：" + requestParam);
//        String randomNumber = requestParam.getString("randomNumber");
//        String expired = requestParam.getString("expired");
//        String userId = requestParam.getString("userId");
//        String accessToken = requestParam.getString("accessToken");
//        if (StringUtil.isNullOrEmpty(randomNumber)) {
//            throw new BusinessException(ResultCodeEnum.ERR_41087, "随机码不能为空");
//        }
//
//        RandomMappingGroup selectByRandomNumber = randomMappingGroupMapper.selectByRandomNumber(randomNumber);
//        String groupId = selectByRandomNumber.getGroupId();
//        BlessEnvelopeGroup b = blessEnvelopeGroupMapper.selectByPrimaryKey(groupId);
//        String adURL = b.getAdURL();
//        String groupURL = selectByRandomNumber.getGroupURL();
//        String url = "";
//        if (StringUtil.isNullOrEmpty(userId)) {//第三方app领取福包
//            url = config.getBlessEnvelopeByWechat() + "?expired=" + expired + "&blessURL=" + config.getBless_url() + "&GroupId=" + groupId;//跳转到手机号领取福包页面
//        } else if (!StringUtil.isNullOrEmpty(userId) && !StringUtil.isNullOrEmpty(adURL)) {
//            url = config.getBlessEnvelopeByApp() + "?expired=" + expired + "&UserId=" + userId + "&blessURL=" + config.getBless_url() + "&adURL=" + adURL + "&GroupId=" + groupId;
//        } else {
//            url = config.getBlessEnvelopeByApp() + "?expired=" + expired + "&UserId=" + userId + "&blessURL=" + config.getBless_url() + "&GroupId=" + groupId;
//        }
//        res.sendRedirect(url);
//    }

    /**
     * 获取终端机发放福报的数据
     *
     * @param requestParam
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public JSONObject getGroupByMachineId(JSONObject requestParam) {
        JSONObject data = new JSONObject();
        String machineid = requestParam.getString("machineId");
        List<BlessEnvelopeRMachine> list = blessEnvelopeRMachineMapper.getByMachineId(Integer.parseInt(machineid));
        List<BlessEnvelopeGroup> groups = new ArrayList<>();
        if (list.size() != 0) {
            for (BlessEnvelopeRMachine bm : list) {
                String groupId = bm.getBlessenvelopegroupId();
                BlessEnvelopeGroup b = blessEnvelopeGroupMapper.selectByPrimaryKey(groupId);
                if (b != null) {
                    groups.add(b);
                }
            }
        }
        data.put("machineId", machineid);
        data.put("groups", groups);

        return ResultUtil.buildSuccessResult(1000, data);
    }

    /**
     * 获取个人发放福报数据
     *
     * @param requestParam
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public JSONObject getGroupByUserId(JSONObject requestParam) {
        // TODO Auto-generated method stub
        log.info("进入获取个人发福包组接口，参数为：" + requestParam);
        JSONObject data = new JSONObject();
        String userId = requestParam.getString("userId");
        BlessEnvelopeGroup b = new BlessEnvelopeGroup();
        b.setFromuserid(userId);
        List<BlessEnvelopeGroup> groups = blessEnvelopeGroupMapper.getGroup(b);
        if (groups.size() != 0) {
            data.put("groups", groups);
        }
        data.put("userId", userId);
        return ResultUtil.buildSuccessResult(1000, data);
    }


    public JSONObject getAdurlByMachineId(JSONObject params) {
        log.info("自助机获取广告地址：参数为：" + params);
        JSONObject data = new JSONObject();
        String machineId = params.getString("machineId");
        List<BlessEnvelopeRMachine> list = blessEnvelopeRMachineMapper.getByMachineId(Integer.parseInt(machineId));
        List<String> urls = new ArrayList<>();
        //Map<String,String> urls = new HashMap<>();
        if (list.size() != 0) {
            for (BlessEnvelopeRMachine bm : list) {
                String groupId = bm.getBlessenvelopegroupId();
                BlessEnvelopeGroup b = blessEnvelopeGroupMapper.selectByPrimaryKey(groupId);
                if (b != null && b.getAliveCount() > 0) {
                    //urls.put(b.getId(), b.getAdURL());
                    if (!StringUtil.isNullOrEmpty(b.getAdURL())) {

                        //urls.add(config.getBless_url()+"/auth/getImage?p="+b.getAdURL());
                        urls.add(config.getPicture_url() + b.getAdURL());
                    }
                }
            }
        }
        data.put("machineId", machineId);
        data.put("urls", urls);
        //data.put("groupId-adurl", urls);
        return ResultUtil.buildSuccessResult(1000, data);
    }


    public JSONObject getDailyStatisticByMachine(JSONObject params) throws ParseException {
        log.info("根据自助机获取每日领取福包数量，参数为：" + params);
        String machineId = params.getString("machineId");
        if (StringUtil.isNullOrEmpty(machineId)) {
            throw new BusinessException(ResultCodeEnum.ERR_41081, "请填写自助机编号");
        }
        String startDate = params.getString("startDate");
        if (StringUtil.isNullOrEmpty(startDate)) {
            throw new BusinessException(ResultCodeEnum.ERR_41082, "参数startDate 不能为空");
        }
        String endDate = params.getString("endDate");
        if (StringUtil.isNullOrEmpty(endDate)) {
            throw new BusinessException(ResultCodeEnum.ERR_41083, "参数 endDate 不能为空");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date end = sdf.parse(endDate);
        Date start = sdf.parse(startDate);
        Calendar canlender = Calendar.getInstance();
        canlender.setTime(end);
        int day = canlender.get(Calendar.DATE);
        canlender.set(Calendar.DATE, day + 1);
        Date time = canlender.getTime();
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        OrderSerachParam param = new OrderSerachParam();
        param.setStartDate(start);
        param.setEndDate(time);
        param.setStartDateStr(sdf2.format(start));
        param.setEndDateStr(sdf2.format(time));
        param.setMachineId(Integer.parseInt(machineId));

        List<OrderDailyStatistics> list = blessEnvelopeMapper.getDailyStatisticsByMachines(param);
        return ResultUtil.buildSuccessResult(1000, list);
    }


    /**
     * 指定时间内用户个人发福包组的个人和金额
     *
     * @param requestParam
     * @return
     */
    public JSONObject getGroupSumAndCountByUserId(JSONObject requestParam) {
        JSONObject data = new JSONObject();
        String userId = requestParam.getString("userId");
        if (StringUtil.isNullOrEmpty(userId)) {
            throw new BusinessException(ResultCodeEnum.ERR_41084, "userId 不能为空");
        }


        BlessEnvelopeGroup b = new BlessEnvelopeGroup();
/*			b.setCreatetime(start);
            b.setEndTime(time);*/
        b.setFromuserid(userId);
        List<BlessEnvelopeGroup> groups = blessEnvelopeGroupMapper.getGroup(b);
        int count = 0;
        double totalAmount = 0;
        if (groups.size() > 0) {
            for (BlessEnvelopeGroup bless : groups) {
                double price = bless.getAmount().doubleValue();
                totalAmount += price;
                Integer number = bless.getNumber();
                count += number;
            }
        }
        data.put("count", count);
        data.put("totalAmount", totalAmount);
        return ResultUtil.buildSuccessResult(1000, data);
    }


    public JSONObject getGroupSumAndCountByChannelId(JSONObject params) {
        JSONObject data = new JSONObject();
        log.info("根据指定条件获取福包组信息接口，参数为：" + params);
        String channelId = params.getString("channelId");


        BlessEnvelopeGroup b = new BlessEnvelopeGroup();
        List<Integer> idList = new ArrayList<Integer>();//存放商家所拥有的自助机编码
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("channelId", channelId);
        map.put("clientID", signClient.getClientId());
        map.put("nonce_str", UUID.randomUUID().toString().replace("-", ""));
        String sign = Signature.getSign(map, signClient.getClientSecret());
        map.put("sign", sign);
        if (idList.size() != 0) {
            List<String> GroupIdList = new ArrayList<String>();//存放福包组Id集合
            for (Integer machineId : idList) {
                List<BlessEnvelopeGroupMachineR> machineIds = blessEnvelopeMachineRMapper.queryByMachineId(machineId);
                for (BlessEnvelopeGroupMachineR bm : machineIds) {
                    if (!StringUtil.isNullOrEmpty(bm.getBlessEnvelopeGroupId())) {
                        GroupIdList.add(bm.getBlessEnvelopeGroupId());
                    }
                }
            }
            HashSet h = new HashSet(GroupIdList);
            GroupIdList.clear();
            GroupIdList.addAll(h);

            if (GroupIdList.size() > 0) {

                b.setIdList(GroupIdList);
                OrderDailyStatistics o = blessEnvelopeGroupMapper.queryByGroupIds(b);
                data.put("count", o.getProductNum());
                data.put("totalAmount", o.getTotalAmount());
            } else {
                return ResultUtil.buildFailResult("无查询结果，原因可能为：渠道码为 【" + channelId + "】 没有发福包");
            }
        } else {
            return ResultUtil.buildFailResult("无查询结果，原因可能为：渠道码为 【" + channelId + "】 无绑定自助机");
        }
        return ResultUtil.buildSuccessResult(1000, data);
    }


    public JSONObject getBlessReciverTop(JSONObject params) {
        JSONObject data = new JSONObject();
        Integer number = params.getString("number") == null ? 20 : Integer
                .parseInt(params.getString("number"));
        List<BlessReciver> blessReciverTop = blessEnvelopeMapper.blessReciverTop(number);
        data.put("top", blessReciverTop);
        return ResultUtil.buildSuccessResult(1000, data);
    }


    public JSONObject getBlessSumAndCountByChannelId(JSONObject params) {
        JSONObject data = new JSONObject();
        log.info("根据指定条件获取福包组信息接口，参数为：" + params);
        String channelId = params.getString("channelId");
        List<Integer> idList = new ArrayList<Integer>();//存放商家所拥有的自助机编码

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("channelId", channelId);
        map.put("clientID", signClient.getClientId());
        map.put("nonce_str", UUID.randomUUID().toString().replace("-", ""));
        String sign = Signature.getSign(map, signClient.getClientSecret());
        map.put("sign", sign);
/*        JSONObject result = sendPost(config.getChannel_url() + config.getQueryMachineIdURL(), map);
        int result_code = result.getIntValue("code");
        if (result_code == 1) {
            JSONArray array = result.getJSONArray("data");
            if (array != null && array.size() != 0) {
                Object[] idArray = array.toArray();
                Arrays.asList(idArray).forEach(machineId -> {
                    idList.add(Integer.parseInt(machineId.toString()));
                });

            }
        }*/

        List<String> GroupIdList = new ArrayList<String>();//存放福包组Id集合
        for (Integer machineId : idList) {
            List<BlessEnvelopeGroupMachineR> machineIds = blessEnvelopeMachineRMapper.queryByMachineId(machineId);
            for (BlessEnvelopeGroupMachineR b : machineIds) {
                if (!StringUtil.isNullOrEmpty(b.getBlessEnvelopeGroupId())) {

                    GroupIdList.add(b.getBlessEnvelopeGroupId());
                }
            }
        }
        HashSet h = new HashSet(GroupIdList);
        GroupIdList.clear();
        GroupIdList.addAll(h);
        if (GroupIdList.size() != 0) {

            BlessEnvelope b = new BlessEnvelope();
            b.setIdList(GroupIdList);
            List<OrderDailyStatistics> queryByGroupIds = blessEnvelopeMapper.queryByGroupIds(b);
            data.put("count", queryByGroupIds.get(0).getProductNum());
            data.put("totalAmount", queryByGroupIds.get(0).getTotalAmount());
            return ResultUtil.buildSuccessResult(1000, data);
        } else {
            return ResultUtil.buildFailResult("无查询结果，原因可能为：渠道码为 【" + channelId + "】 没有发放福包");
        }
    }


    public JSONObject getGroups(JSONObject params) {
        JSONObject data = new JSONObject();
        String channelId = params.getString("channelId");
        String mcId = params.getString("machineId");

        String startTime = params.getString("startTime");
        if (StringUtil.isNullOrEmpty(startTime)) {
            return ResultUtil.buildFailResult("参数startTime不能为空");
        }
        String endTime = params.getString("endTime");
        if (StringUtil.isNullOrEmpty(endTime)) {
            return ResultUtil.buildFailResult("参数endTime不能为空");
        }

        List<String> idList = new ArrayList<String>();//存放商家所拥有的自助机编码
        if (!StringUtil.isNullOrEmpty(channelId)) {//通过渠道商获取对应自助机发福包组的所有信息

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("channelId", channelId);
            map.put("clientID", signClient.getClientId());
            map.put("nonce_str", UUID.randomUUID().toString().replace("-", ""));
            String sign = Signature.getSign(map, signClient.getClientSecret());
            map.put("sign", sign);
            /*JSONObject result = sendPost(config.getChannel_url() + config.getQueryMachineIdURL(), map);
            int result_code = result.getIntValue("code");
            if (result_code == 1) {
                JSONArray array = result.getJSONArray("data");
                if (array != null && array.size() != 0) {
                    Object[] idArray = array.toArray();
                    Arrays.asList(idArray).forEach(machineId -> {
                        idList.add(machineId.toString());
                    });

                }
            }*/
        } else {
            idList.add(mcId);
        }

        Map<Integer, List> maps = new HashMap<>();
        BlessEnvelopeGroup b = new BlessEnvelopeGroup();
        b.setIdList(idList);
        List<BlessEnvelopeGroup> groups = blessEnvelopeGroupMapper.getGroup(b);
        List<String> GroupIdList = new ArrayList<String>();//存放福包组Id集合
        if (groups.size() != 0) {
            for (BlessEnvelopeGroup blessGroup : groups) {
                GroupIdList.add(blessGroup.getId());
            }
        }
        if (GroupIdList.size() > 0) {
            OrderSerachParam o = new OrderSerachParam();
            o.setStartDateStr(startTime);
            o.setEndDateStr(endTime);
            o.setGroupIds(GroupIdList);
            List<OrderDailyStatistics> list = blessEnvelopeGroupMapper.getDailyStatisticsByGroupIds(o);
            data.put("Info", list);
        } else {
            return ResultUtil.buildFailResult("无查询结果，原因可能为：渠道码为 【" + channelId + "】 没有发放福包");
        }

/*		for (Integer machineId : idList) {

			List<BlessEnvelopeGroupMachineR> machineIds = blessEnvelopeMachineRMapper.queryByMachineId(machineId);
			for (BlessEnvelopeGroupMachineR b : machineIds) {
				if(!StringUtil.isNullOrEmpty(b.getBlessEnvelopeGroupId())){
					GroupIdList.add(b.getBlessEnvelopeGroupId());
				}
			}
			if(GroupIdList.size() > 0){
				OrderSerachParam o = new OrderSerachParam();
				o.setStartDateStr(startTime);
				o.setEndDateStr(endTime);
				o.setGroupIds(GroupIdList);
				List<OrderDailyStatistics> list = blessEnvelopeGroupMapper.getDailyStatisticsByGroupIds(o);
				maps.put(machineId,list);
			}
		}*/


        return ResultUtil.buildSuccessResult(1000, data);
    }

    public String GetBlessGroup(JSONObject param) throws ParseException {

        JSONObject data = new JSONObject();

        String name = param.getString("name");//发包人姓名
        String phone = param.getString("phone");//发包人手机号
        String isGrand = param.getString("isGrand");//是否随机
        String startTime = param.getString("startTime");//起始时间
        String endTime = param.getString("endTime");//结束时间
        String size = param.getString("size");//每页显示数量
        String page = param.getString("page");//页码


//		if(com.bkgc.bless.utils.StringUtil.isNullOrEmpty(startTime,endTime,size,page)){
//			return ResultUtil.buildFailResult("请检查参数[startTime，endTime，size，page]");
//		}

        if (StringUtil.isNullOrEmpty(size, page)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "请检查参数[size，page]");
        }
        BlessEnvelopeGroup b = new BlessEnvelopeGroup();
        if (!StringUtil.isNullOrEmpty(startTime, endTime)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);
            Calendar canlender = Calendar.getInstance();
            canlender.setTime(end);
            int day = canlender.get(Calendar.DATE);
            canlender.set(Calendar.DATE, day + 1);
            Date time = canlender.getTime();
            b.setCreatetime(start);
            b.setEndTime(time);
        }

        if (!StringUtil.isNullOrEmpty(phone)) {
            b.setPhone(phone);
        }
        if (!StringUtil.isNullOrEmpty(name)) {
            b.setUserName(name);
        }
        if (!StringUtil.isNullOrEmpty(isGrand)) {
            b.setGrands(Integer.parseInt(isGrand));
        }

        b.setPageSize(Integer.parseInt(size));
        b.setPageStart((Integer.parseInt(page) - 1) * Integer.parseInt(size));
        int count = blessEnvelopeGroupMapper.getBlessGroupCount(b);

        List<BlessEnvelopeGroup> list = blessEnvelopeGroupMapper.getBlessGroup(b);
        for (BlessEnvelopeGroup bep : list) {

            switch (bep.getType()) {
                case "1":
                    bep.setBlessDescribe("随机福包");
                    break;
                case "2":
                    bep.setBlessDescribe("等级福包");
                    break;
                case "3":
                    bep.setBlessDescribe("推广福包");
                    break;
                case "4":
                    bep.setBlessDescribe("线下单码包");
                    break;
                case "5":
                    bep.setBlessDescribe("线下多码包");
                    break;
                case "6":
                    bep.setBlessDescribe("公益福包");
                    break;
                default:
                    bep.setBlessDescribe("未知福包");
                    break;
            }

//			bep.setBlessDescribe(bep.getGrands()==1?"随机福包":"等级福包");
        }

        data.put("info", list);
        data.put("count", count);
        return WrapperUtil.ok(data).toString();

    }

    public String GetBlessInfo(JSONObject params) {

        JSONObject data = new JSONObject();

        String id = params.getString("id");
        if (StringUtil.isNullOrEmpty(id)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "请检查参数[id]");
        }
        BlessInfo info = blessEnvelopeGroupMapper.getBlessGroupInfo(id);
        List<BlessEnvelopeGroupMachineR> machines = blessEnvelopeMachineRMapper.queryByBlessEnvelopeGroupId(id);
        List<String> timeByGroupId = blessEnvelopeVaildMapper.getTimeByGroupId(id);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("receiveCount", info.getReceiveCount());
        resultMap.put("receiveMoney", info.getReceiveMoney());
        resultMap.put("aliveCount", info.getAliveCount());
        resultMap.put("balance", info.getBalance());
        resultMap.put("machineIds", machines);
        resultMap.put("adURL", info.getAdURL());
        resultMap.put("timeTactics", timeByGroupId);
        resultMap.put("expiredTime", info.getExpiredTime());


        return WrapperUtil.ok(resultMap).toString();
    }

    public JSONObject GetGroupByMcId(JSONObject params) {
        JSONObject data = new JSONObject();
        String mcId = params.getString("machineId");
        OrderSerachParam param = new OrderSerachParam();
        if (StringUtil.isNullOrEmpty(mcId)) {
            param.setMachineId(Integer.parseInt(mcId));
        }
        List<OrderDailyStatistics> list = blessEnvelopeGroupMapper.getGroupStatistics(param);
        data.put("list", list);
        return ResultUtil.buildSuccessResult(1000, data);
    }

    public RWrapper getGroupStatistics(OrderSerachParam param) {

        List<OrderDailyStatistics> groupStatistics = blessEnvelopeGroupMapper.getGroupStatistics(param);

        return WrapperUtil.ok(groupStatistics);
    }

    public RWrapper getStatisticsByMachineIds(List<Integer> list) {
        JSONObject data = new JSONObject();

        BlessGroupParam bp = blessEnvelopeGroupMapper.getStatisticsByMachineIds(list);
        log.info("查询={}", bp);
        if (bp != null) {
            data.put("aliveCount", bp.getAliveCount());
            data.put("receiveCount", bp.getReceiveCount());
            data.put("remainBalance", bp.getRemainBalance());
            return WrapperUtil.ok(data);
        }
        return WrapperUtil.error(ResultCodeEnum.ERR_54004);
//	throw new BusinessException(ResultCodeEnum.ERR_54004, "未查询到该用户所发放的福包");

    }

    public String GetBlessList(JSONObject params) throws ParseException {
        String id = params.getString("id");
        String size = params.getString("size");//每页显示数量
        String page = params.getString("page");//页码
        if (StringUtil.isNullOrEmpty(id, size, page)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "请检查参数[id,size,page]");
        }
        BlessEnvelope b = new BlessEnvelope();
        b.setGroupid(id);

        String name = params.getString("name");//领福包人
        if (!StringUtil.isNullOrEmpty(name)) {
            b.setName(name);
        }
        String phone = params.getString("phone");//领福包人手机号
        if (!StringUtil.isNullOrEmpty(phone)) {
            b.setPhone(phone);
        }
        String startTime = params.getString("startTime");//起始时间
        String endTime = params.getString("endTime");//结束时间
        if (!StringUtil.isNullOrEmpty(startTime, endTime)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);
            Calendar canlender = Calendar.getInstance();
            canlender.setTime(end);
            int day = canlender.get(Calendar.DATE);
            canlender.set(Calendar.DATE, day + 1);
            Date time = canlender.getTime();
            b.setCreatetime(start);
            b.setEndTime(time);
        }
        b.setPageSize(Integer.parseInt(size));
        b.setPageStart((Integer.parseInt(page) - 1) * Integer.parseInt(size));
        List<ReceiveBlessInfo> blessInfoByGroupId = blessEnvelopeMapper.getBlessInfoByGroupId(b);
        int count = blessEnvelopeMapper.getBlessCountByGroupId(b);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("count", count);
        resultMap.put("list", blessInfoByGroupId);
        return WrapperUtil.ok(resultMap).toString();
    }

    public String ExpiredBlessGroup(JSONObject params) throws ParseException {


        JSONObject data = new JSONObject();

        String name = params.getString("name");//发包人姓名
        String phone = params.getString("phone");//发包人手机号
        String status = params.getString("status");//福包状态
        String startTime = params.getString("startTime");//起始时间
        String endTime = params.getString("endTime");//结束时间
        String size = params.getString("size");//每页显示数量
        String page = params.getString("page");//页码


//		if(com.bkgc.bless.utils.StringUtil.isNullOrEmpty(startTime,endTime,size,page)){
//			return ResultUtil.buildFailResult("请检查参数[startTime，endTime，size，page]");
//		}

        if (StringUtil.isNullOrEmpty(size, page)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "请检查参数[size，page]");
        }
        BlessEnvelopeGroup b = new BlessEnvelopeGroup();
        if (!StringUtil.isNullOrEmpty(startTime, endTime)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);
            Calendar canlender = Calendar.getInstance();
            canlender.setTime(end);
            int day = canlender.get(Calendar.DATE);
            canlender.set(Calendar.DATE, day + 1);
            Date time = canlender.getTime();
            b.setCreatetime(start);
            b.setEndTime(time);
        }

        if (!StringUtil.isNullOrEmpty(phone)) {
            b.setPhone(phone);
        }
        if (!StringUtil.isNullOrEmpty(name)) {
            b.setUserName(name);
        }
        b.setExpiredtime(new Date());

        b.setPageSize(Integer.parseInt(size));
        b.setPageStart((Integer.parseInt(page) - 1) * Integer.parseInt(size));
        int count = blessEnvelopeGroupMapper.getBlessGroupCount(b);

        List<BlessEnvelopeGroup> list = blessEnvelopeGroupMapper.getBlessGroup(b);

        data.put("info", list);
        data.put("count", count);
        return WrapperUtil.ok(data).toString();


    }

    public String getAdBlessGroup(JSONObject param) throws ParseException {
        JSONObject data = new JSONObject();

        String name = param.getString("name");//发包人姓名
        String phone = param.getString("phone");//发包人手机号
        String startTime = param.getString("startTime");//起始时间
        String endTime = param.getString("endTime");//结束时间
        String size = param.getString("size");//每页显示数量
        String page = param.getString("page");//页码


//		if(com.bkgc.bless.utils.StringUtil.isNullOrEmpty(startTime,endTime,size,page)){
//			return ResultUtil.buildFailResult("请检查参数[startTime，endTime，size，page]");
//		}

        if (StringUtil.isNullOrEmpty(size, page)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "请检查参数[size，page]");
        }
        BlessEnvelopeGroup b = new BlessEnvelopeGroup();
        if (!StringUtil.isNullOrEmpty(startTime, endTime)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);
            Calendar canlender = Calendar.getInstance();
            canlender.setTime(end);
            int day = canlender.get(Calendar.DATE);
            canlender.set(Calendar.DATE, day + 1);
            Date time = canlender.getTime();
            b.setCreatetime(start);
            b.setEndTime(time);
        }

        if (!StringUtil.isNullOrEmpty(phone)) {
            b.setPhone(phone);
        }
        if (!StringUtil.isNullOrEmpty(name)) {
            b.setUserName(name);
        }
        b.setIsAd("true");
        b.setPageSize(Integer.parseInt(size));
        b.setPageStart((Integer.parseInt(page) - 1) * Integer.parseInt(size));
        int count = blessEnvelopeGroupMapper.getBlessGroupCount(b);

        List<BlessEnvelopeGroup> list = blessEnvelopeGroupMapper.getBlessGroup(b);

        data.put("info", list);
        data.put("count", count);
        return WrapperUtil.ok(data).toString();
    }

    public String ExpiredAdBlessGroup(JSONObject params) throws ParseException {
        JSONObject data = new JSONObject();

        String name = params.getString("name");//发包人姓名
        String phone = params.getString("phone");//发包人手机号
        String startTime = params.getString("startTime");//起始时间
        String endTime = params.getString("endTime");//结束时间
        String size = params.getString("size");//每页显示数量
        String page = params.getString("page");//页码


        if (StringUtil.isNullOrEmpty(size, page)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "请检查参数[size，page]");
        }
        BlessEnvelopeGroup b = new BlessEnvelopeGroup();
        if (!StringUtil.isNullOrEmpty(startTime, endTime)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);
            Calendar canlender = Calendar.getInstance();
            canlender.setTime(end);
            int day = canlender.get(Calendar.DATE);
            canlender.set(Calendar.DATE, day + 1);
            Date time = canlender.getTime();
            b.setCreatetime(start);
            b.setEndTime(time);
        }

        if (!StringUtil.isNullOrEmpty(phone)) {
            b.setPhone(phone);
        }
        if (!StringUtil.isNullOrEmpty(name)) {
            b.setUserName(name);
        }
        b.setExpiredtime(new Date());
        b.setIsAd("true");

        b.setPageSize(Integer.parseInt(size));
        b.setPageStart((Integer.parseInt(page) - 1) * Integer.parseInt(size));
        int count = blessEnvelopeGroupMapper.getBlessGroupCount(b);

        List<BlessEnvelopeGroup> list = blessEnvelopeGroupMapper.getBlessGroup(b);

        data.put("info", list);
        data.put("count", count);
        return WrapperUtil.ok(data).toString();
    }

    public List<BlessEnvelopeGroup> getTop10() {
        return blessEnvelopeGroupMapper.getTop10();
    }
    /**
     * @Description:    地区领取福包排名
     * @Author:         WeiWei
     * @CreateDate:     2019/1/7 15:23
     * @UpdateUser:     WeiWei
     * @UpdateDate:     2019/1/7 15:23
     * @UpdateRemark:
     * @Version:        1.0
     */
    public JSONArray getBlessenvelopeByArea() {
        List<BlessEnvelopeGroupMachineR> orderMoneyResList = blessEnvelopeGroupMapper.getBlessenvelopeByArea();
        JSONArray proviceArray = new JSONArray();
        for (BlessEnvelopeGroupMachineR proviceRes : orderMoneyResList) {
            JSONObject orderData = new JSONObject();
            orderData.put("areaName",proviceRes.getAreaName());
            proviceArray.add(orderData);
        }
        return proviceArray;
    }
}
