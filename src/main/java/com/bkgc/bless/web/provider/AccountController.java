package com.bkgc.bless.web.provider;

import com.bkgc.bean.SearchBean;
import com.bkgc.bless.service.impl.AccountService;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.RWrapper;
import com.bkgc.common.utils.StringUtil;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by yanqiang on 2017/11/13.
 */
@RequestMapping(value = "/account")
@RestController
public class AccountController {

    @Autowired
    private AccountService accountService;


    @RequestMapping(value = "/members")
    @ResponseBody
    public RWrapper<Map<String, Object>> getMemberAccount(@RequestBody Map<String, String> map) {
        SearchBean search = assembleParms(map);
        return accountService.getMemberAccountList(search);
    }


    @RequestMapping(value = "/companies")
    @ResponseBody
    public RWrapper<Map<String, Object>> getCompaniesAccount(@RequestBody Map<String, String> map) {
        SearchBean search = assembleParms(map);
        return accountService.getCompaniesAccount(search);
    }

    private SearchBean assembleParms(Map<String, String> map) {
        SearchBean searchBean = new SearchBean();
        String pageNum = map.get("pageNum");
        if (StringUtil.isNullOrEmpty(pageNum)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "参数pageNum不存在");
        }
        searchBean.setPageNum(Integer.parseInt(pageNum));

        String pageSize = map.get("pageSize");
        if (StringUtil.isNullOrEmpty(pageSize)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "参数pageSize不存在");
        }
        searchBean.setPageSize(Integer.parseInt(pageSize));

        String moduleId = map.get("moduleId");
        if (!StringUtil.isNullOrEmpty(moduleId)) {
            searchBean.setModuleId(moduleId);
        }
        String name = map.get("name");

        if (!StringUtil.isNullOrEmpty(name)) {
            searchBean.setName(name);
        }
        String phone = map.get("phone");
        if (!StringUtil.isNullOrEmpty(phone)) {
            searchBean.setPhone(phone);
        }
        String startDateTime = map.get("startDateTime");
        if (!StringUtil.isNullOrEmpty(startDateTime)) {
            searchBean.setStartDateTime(startDateTime);
        }

        String endDateTime = map.get("endDateTime");
        if (!StringUtil.isNullOrEmpty(endDateTime)) {
            searchBean.setEndDateTime(endDateTime);
        }
        return searchBean;

    }

}
