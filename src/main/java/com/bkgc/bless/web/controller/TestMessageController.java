package com.bkgc.bless.web.controller;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import com.bkgc.bean.user.AuthMember;
import com.bkgc.bless.config.Config;
import com.bkgc.bless.mapper.AuthMemberMapper;
import com.bkgc.bless.mapper.AuthSpaceMasterMapper;
import com.bkgc.bless.mapper.PenaltyRecordMapper;
import com.bkgc.bless.model.vo.PenaltyRecordVo;
import com.bkgc.common.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yanqiang on 2017/11/30.
 */
@Controller
@Slf4j
public class TestMessageController {

    @Autowired
    private AuthMemberMapper authMemberMapper;


    @Autowired
    private AuthSpaceMasterMapper authSpaceMasterMapper;
    @Autowired
    private Producer producer;

    @Autowired
    private PenaltyRecordMapper penaltyRecordMapper;

    @RequestMapping(value = "/test/message/send")
    @ResponseBody
    public String testSend(HttpServletRequest request) {
        String userInfo = "{\"username\":\"phil\",\"guid\":\"76897654678987865\"}";
        Message message = new Message("dev_topic_bkgc_sync_data", "userInfo", "bless", userInfo.getBytes());
        producer.send(message);
        return "ok";
    }

    @RequestMapping(value = "/test/reg")
    @ResponseBody
    public String testReg(HttpServletRequest request) {

        List<AuthMember> all = authMemberMapper.getAll();
        int good = 0;
        for (AuthMember authMember : all) {
            if (!StringUtil.isNullOrEmpty(authMember.getIdcardnumber())) {
                boolean flag = check(authMember.getIdcardnumber());
                if (flag) {
                    good++;
                }
                System.out.println(authMember.getIdcardnumber() + ":" + flag);

            }
        }
        return good + "ok";
    }

    static String reg = "^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{2}$)";

    public static boolean check(String str) {
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    @Autowired
    private Config config;

    @RequestMapping("download.html")
    public String download() {
        log.info("download.html");
        return "redirect:http://oss.8fubao.com/apk/bv/html/tbvapp2.4.3.apk";
    }


    @RequestMapping("/test")
    public void test() {
        /*List<AuthMember> authMemberList = authMemberMapper.searchUser("135");
        log.info("authMemberList={}", authMemberList);*/

        //authSpaceMasterMapper.updateStatus("1d63949dfe02425cb98c74d94ba0a46e", 0);
        PageHelper.startPage(Integer.parseInt("1"), Integer.parseInt("2"));
        List<PenaltyRecordVo> penaltyRecordVos = penaltyRecordMapper.selectPage("", "", "188", 8);
        log.info("penaltyRecordVos={}", penaltyRecordVos);
    }

}
