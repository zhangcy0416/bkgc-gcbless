package com.bkgc.bless.service.impl;

import com.bkgc.bean.SearchBean;
import com.bkgc.bean.account.AuthAccount;
import com.bkgc.bean.user.AuthCompany;
import com.bkgc.bean.user.AuthMember;
import com.bkgc.bless.consumer.PaymentFeignService;
import com.bkgc.bless.mapper.AuthCompanyMapper;
import com.bkgc.bless.mapper.AuthMemberMapper;
import com.bkgc.bless.model.vo.Account;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.utils.RWrapper;
import com.bkgc.common.utils.WrapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by yanqiang on 2017/11/13.
 */
@Service
public class AccountService {

    @Autowired
    private AuthMemberMapper authMemberMapper;

    @Autowired
    private AuthCompanyMapper authCompanyMapper;

    @Autowired
    protected PaymentFeignService paymentFeignService;

    public RWrapper<Map<String, Object>> getMemberAccountList(SearchBean search) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        List<AuthMember> members = authMemberMapper.getSearched(search);
        StringBuilder ids = new StringBuilder("");
        if (!members.isEmpty()) {
            for (AuthMember member : members) {
                ids.append(member.getId()).append(",");
            }
        } else {
            resultMap.put("count", getMembersCount(search));
            resultMap.put("list", null);
            return WrapperUtil.ok(resultMap);
        }
        String idsStr = "";
        if (ids.length() != 0) {
            idsStr = ids.toString();
            idsStr = idsStr.substring(0, idsStr.length() - 1);
        }
        Map<String, String> param = new HashMap<>();
        param.put("userIds", idsStr);
        RWrapper<Map<String, AuthAccount>> authAccounts = paymentFeignService.getAccountList(param);
        //RWrapper<Map<String, Object>> authAccounts = restTemplate.postForObject("http://" + serviceNameConfig.getPayment() + "/mchaccount/list", param, RWrapper.class);
        List<Account> accounts;
        if ("1000".equals(authAccounts.getCode())) {
            //Map<String, Object> authAccountMap = authAccounts.getData();
            Map<String, AuthAccount> authAccountMap = authAccounts.getData();
            accounts = new ArrayList<>();
            Account account;
            for (AuthMember member : members) {
                //LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) authAccountMap.get(member.getId());
                AuthAccount authAccount = authAccountMap.get(member.getId());
                //if (map != null && map.size() > 0) {
                account = new Account();
                account.setName(member.getName());
                account.setNickname(member.getNickname());
                account.setPhone(member.getPhone());
                //account.setBlessamount(new BigDecimal(String.valueOf(map.get("blessamount"))));
                //account.setAccountbalance(new BigDecimal(String.valueOf(map.get("accountbalance"))));
                account.setBlessamount(authAccount.getBlessamount());
                account.setAccountbalance(authAccount.getAccountbalance());

                account.setCreateTime(member.getCreatetime());
                account.setCreateTimeStr(member.getCreatetimeStr());
                account.setUserid(member.getId());
                //account.setId(map.get("id").toString());
                account.setId(authAccount.getId());
                accounts.add(account);
                //}
            }
        } else {
            throw new BusinessException("加载数据有误,请重试");
        }

        if (!accounts.isEmpty()) {
            resultMap.put("count", getMembersCount(search));
            resultMap.put("list", accounts);
            return WrapperUtil.ok(resultMap);
        } else {
            throw new BusinessException("加载数据有误,请重试");
        }

    }

    private Integer getMembersCount(SearchBean searchBean) {
        Integer count = this.authMemberMapper.getMembersCount(searchBean);
        if (count != null) {
            return count;
        } else {
            return 0;
        }
    }

    private Integer getCompaniesCount(SearchBean searchBean) {
        Integer count = this.authCompanyMapper.getCompaniesCount(searchBean);
        if (count != null) {
            return count;
        } else {
            return 0;
        }
    }

    public RWrapper<Map<String, Object>> getCompaniesAccount(SearchBean search) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        List<AuthCompany> members = authCompanyMapper.getSearched(search);
        StringBuilder ids = new StringBuilder("");
        if (!members.isEmpty()) {
            for (AuthCompany member : members) {
                ids.append(member.getId()).append(",");
            }
        } else {
            resultMap.put("count", getCompaniesCount(search));
            resultMap.put("list", null);
            return WrapperUtil.ok(resultMap);
        }
        String idsStr = "";
        if (ids.length() != 0) {
            idsStr = ids.toString();
            idsStr = idsStr.substring(0, idsStr.length() - 1);
        }
        Map<String, String> param = new HashMap<>();
        param.put("userIds", idsStr);
        RWrapper<Map<String, AuthAccount>> authAccounts = paymentFeignService.getAccountList(param);
        //RWrapper<Map<String, Object>> authAccounts = restTemplate.postForObject("http://" + serviceNameConfig.getPayment() + "/mchaccount/list", param, RWrapper.class);
        List<Account> accounts = null;
        if ("1000".equals(authAccounts.getCode())) {
            Map<String, AuthAccount> authAccountMap = authAccounts.getData();
            //Map<String, Object> authAccountMap = authAccounts.getData();
            accounts = new ArrayList<>();
            Account account = null;
            for (AuthCompany member : members) {
                //LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) authAccountMap.get(member.getId());
                AuthAccount authAccount = authAccountMap.get(member.getId());
                account = new Account();
                account.setName(member.getName());
                account.setPhone(member.getPhone());
                account.setAddress(member.getAddress());
                //account.setBlessamount(new BigDecimal(String.valueOf(map.get("blessamount"))));
                //account.setAccountbalance(new BigDecimal(String.valueOf(map.get("accountbalance"))));
                account.setBlessamount(authAccount.getBlessamount());
                account.setAccountbalance(authAccount.getAccountbalance());

                account.setCreateTime(member.getCreatetime());
                account.setUserid(member.getId());
                //account.setId(map.get("id").toString());
                account.setId(authAccount.getId());

                accounts.add(account);
            }
        } else {
            throw new BusinessException("加载数据有误,请重试");
        }

        if (!accounts.isEmpty()) {
            resultMap.put("count", getCompaniesCount(search));
            resultMap.put("list", accounts);
            return WrapperUtil.ok(resultMap);
        } else {
            throw new BusinessException("加载数据有误,请重试");
        }

    }


}
