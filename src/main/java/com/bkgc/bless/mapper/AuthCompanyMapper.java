package com.bkgc.bless.mapper;

import java.util.List;

import com.bkgc.bean.SearchBean;
import org.apache.ibatis.annotations.Param;

import com.bkgc.bean.bless.Company;
import com.bkgc.bean.user.AuthCompany;

public interface AuthCompanyMapper {
    int deleteByPrimaryKey(@Param("id")String id);

    int insert(AuthCompany record);

    int insertSelective(AuthCompany record);

    AuthCompany selectByPrimaryKey(@Param("id")String id);

    int updateByPrimaryKeySelective(AuthCompany record);

    int updateByPrimaryKey(AuthCompany record);

	List<Company> getAll();

    List<AuthCompany> getComPageAll(SearchBean pageBean);

    int getComAllCount(SearchBean searchBean);

    Integer getCompaniesCount(SearchBean searchBean);

    List<AuthCompany> getSearched(SearchBean searchBean);
}