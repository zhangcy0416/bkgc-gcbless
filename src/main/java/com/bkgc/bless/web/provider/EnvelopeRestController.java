package com.bkgc.bless.web.provider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bkgc.bless.service.impl.BlessEnvelopeService;
import com.bkgc.bless.service.impl.EnvelopeService;
import com.bkgc.common.utils.RWrapper;
import com.bkgc.common.utils.WrapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * <p>Title:      EnvelopeController </p>
 * <p>Description  </p>
 * <p>Company:    北控国彩 </p>
 *
 * @Author zhangft
 * @CreateDate 2017/12/21 上午10:34
 */
@RestController
@RequestMapping(value = "/envelope")
public class EnvelopeRestController {


    @Autowired
    private EnvelopeService envelopeService;

    @Autowired
    private BlessEnvelopeService blessEnvelopeService;

    private static Logger log = LoggerFactory.getLogger(EnvelopeRestController.class);


    /**
     * 创建福包组
     *
     * @param requestParam
     * @return
     */
    @RequestMapping(value = "/createBlessEnvelope")
    @ResponseBody
    public RWrapper createBlessEnvelope(@RequestBody JSONObject requestParam) {
        log.info("创建福包组参数={}", requestParam);
        JSONObject retJson = envelopeService.createBlessEnvelope(requestParam);
        log.info("创建福包组返回={}", retJson.toString());
        return WrapperUtil.ok(retJson);
    }

    /**
     * 自助机获取领福包二维码
     *
     * @param paramJson
     * @return
     */
    @RequestMapping(value = "/getBlessEnvelopeURL")
    @ResponseBody
    public RWrapper getBlessEnvelopeURL(@RequestBody JSONObject paramJson) {
        log.info("自助机获取福包二维码参数=" + paramJson.toString());
        JSONObject retJson = envelopeService.getBlessEnvelopeURL(paramJson);
        log.info("自助机获取福包二维码返回=" + retJson.toString());
        return WrapperUtil.ok(retJson);
    }

    /**
     * 获取20个自助机的福包数量
     *
     * @param jsonObject
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    @RequestMapping(value = "/getDefault20MachinesByCurrentLoaction")
    @ResponseBody
    public RWrapper<JSONObject> getDefault20MachinesByCurrentLoaction(@RequestBody JSONObject jsonObject) throws IllegalArgumentException, IllegalAccessException {
        log.info("获取20个自助机的福包数量入参{}", jsonObject);
        JSONObject responseData = blessEnvelopeService.getDefault20MachinesByCurrentLoaction(jsonObject);
        log.info("获取20个自助机的福包数量出参{}", responseData);
        return WrapperUtil.ok(responseData.getJSONObject("data"));
    }
    /**
     * @Description:    地区领取福包排名
     * @Author:         WeiWei
     * @CreateDate:     2019/1/7 15:17
     * @UpdateUser:     WeiWei
     * @UpdateDate:     2019/1/7 15:17
     * @UpdateRemark:
     * @Version:        1.0
     */
    @ResponseBody
    @RequestMapping("/getBlessenvelopeByArea")
    public RWrapper<JSONArray> getBlessenvelopeByArea() {
        log.info("地区领取福包排名");
        JSONArray data = blessEnvelopeService.getBlessenvelopeByArea();
        log.info("地区领取福包排名，返回数据data={}", data);
        return WrapperUtil.ok(data);
    }

}
