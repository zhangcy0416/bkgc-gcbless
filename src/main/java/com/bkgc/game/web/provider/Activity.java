package com.bkgc.game.web.provider;


import com.bkgc.bean.bless.BlessEnvelopeGroupMachineR;
import com.bkgc.bless.config.Config;
import com.bkgc.bless.mapper.AuthMemberMapper;
import com.bkgc.bless.mapper.BlessEnvelopeMachineRMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.List;

@RestController
@RequestMapping("/activity")
public class Activity {

    @Autowired
    private BlessEnvelopeMachineRMapper blessEnvelopeMachineRMapper;

    @Autowired
    private AuthMemberMapper authMemberMapper;

    @Autowired
    private Config config;

    private static Config staticConfig;

    private static BlessEnvelopeMachineRMapper staticBMRMapper;

    private static AuthMemberMapper staticAuthMemberMapper;

    private static Logger log = LoggerFactory.getLogger(Activity.class);

    @PostConstruct
    void initStaticBMRMapper() {
        staticBMRMapper = blessEnvelopeMachineRMapper;
        staticAuthMemberMapper = authMemberMapper;
        staticConfig = config;

    }

    /**
     * 发送兑奖的弹幕信息
     *
     * @param
     * @param
     * @return
     * @throws Exception
     */
/*    @RequestMapping(value = "/award")
    @ResponseBody
    public String award(@RequestBody AwardBriefInfo awardBriefInfo, HttpServletRequest request) throws Exception {
        if (awardBriefInfo != null) {
            AuthMember user = staticAuthMemberMapper.selectByguid(awardBriefInfo.getUserId());
            if (user != null) {
                JSONObject data = new JSONObject();
                data.put("type", "2");
                data.put("user", user.getPhone().substring(0, 3).concat("****").concat(user.getPhone().substring(7)));
                data.put("amount", awardBriefInfo.getAmount());
                if (user.getFacephotopath().trim().startsWith("/")) {
                    data.put("iconurl", config.getPicture_url().concat(user.getFacephotopath()));
                } else {
                    data.put("iconurl", user.getFacephotopath());
                }
                log.info("兑奖弹幕消息为:" + data.toJSONString() + ",开始发送...");
                HttpRequest.sendPost("http://123.57.229.241:40000/award/message/send", data.toJSONString());
                log.info("发送成功！");
            }
        }
        return "ok";
    }*/
    private static String extractMachineIds(List<BlessEnvelopeGroupMachineR> bmrList) {
        String machineIds = "";
        for (BlessEnvelopeGroupMachineR r : bmrList) {
            machineIds += String.valueOf(r.getMachineId()) + ",";
        }
        return machineIds.substring(0, machineIds.length() - 1);
    }

}
