package com.bkgc.bless.web.provider;

import com.bkgc.bean.SearchBean;
import com.bkgc.bean.user.AuthCompany;
import com.bkgc.bean.user.AuthMember;
import com.bkgc.bless.service.impl.AuthService;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.RWrapper;
import com.bkgc.common.utils.StringUtil;
import com.bkgc.common.utils.WrapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.*;

/**
 * Created by gmg on on 2017-10-20 16:45.
 */
@RequestMapping(value = "/authMemCom")
@RestController
public class AuthMemComController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    AuthService authService;

    /**
     * 获取福包用户列表
     * @param searchBean
     * @return
     */
    @RequestMapping(value = "/getAuthMemberList")
    public RWrapper<Map<String,Object>> getAuthMemberList(@RequestBody SearchBean searchBean){
        String pageNum = String.valueOf(searchBean.getPageNum());
        String pageSize = String.valueOf(searchBean.getPageSize());
        log.info("pageNum为:"+pageNum+",pageSize"+pageSize);
        if(StringUtil.isNullOrEmpty(pageNum,pageSize)){
            throw new BusinessException(ResultCodeEnum.ERR_41001);
        }
        if(StringUtil.isNullOrEmpty(searchBean.getName())){
            searchBean.setName(null);
        }
        if(StringUtil.isNullOrEmpty(searchBean.getPhone())){
            searchBean.setPhone(null);
        }
        if(StringUtil.isNullOrEmpty(searchBean.getStartDateTime())){
            searchBean.setStartDateTime(null);
        }
        if(StringUtil.isNullOrEmpty(searchBean.getEndDateTime())){
            searchBean.setEndDateTime   (null);
        }
            int count = authService.getAuthMemberAllCount(searchBean);
            List<AuthMember> queryList = authService.getAuthMemberList(searchBean);
            Map<String,Object> map=new HashMap<String,Object>();
            map.put("count", count);
            map.put("data", queryList);
            log.info("返回的条数为:"+count);
            return WrapperUtil.ok(map);

    }

    /**
     * 获取企业用户列表
     * @param searchBean
     * @return
     */
    @RequestMapping(value = "/getAuthComList")
    public RWrapper<Map<String,Object>> getAuthComList(@RequestBody SearchBean searchBean){
        String pageNum = String.valueOf(searchBean.getPageNum());
        String pageSize = String.valueOf(searchBean.getPageSize());
        log.info("pageNum为:"+pageNum+",pageSize"+pageSize);
        if(StringUtil.isNullOrEmpty(pageNum,pageSize)){
           throw  new BusinessException(ResultCodeEnum.ERR_41001);
        }
        if(StringUtil.isNullOrEmpty(searchBean.getName())){
            searchBean.setName(null);
        }
        if(StringUtil.isNullOrEmpty(searchBean.getPhone())){
            searchBean.setPhone(null);
        }
        if(StringUtil.isNullOrEmpty(searchBean.getStartDateTime())){
            searchBean.setStartDateTime(null);
        }
        if(StringUtil.isNullOrEmpty(searchBean.getEndDateTime())){
            searchBean.setEndDateTime(null);
        }
        int count = authService.getAuthCompanyAllCount(searchBean);
        List<AuthCompany> queryList = authService.getAuthCompanyList(searchBean);
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("count", count);
        map.put("data", queryList);
        log.info("返回的条数为:"+count);
        return WrapperUtil.ok(map);

    }
}
