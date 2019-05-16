package com.bkgc.bless.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.bkgc.bean.bless.Company;

public interface CompanyMapper
{
	public Company get(String id);
	public List<Company> getAll(@Param("whereExp")String whereExp,@Param("sort")String sort , @Param("direction")String direction);
    public List<Company> getByPage(@Param("offset")int offset , @Param("size")int size ,@Param("whereExp")String whereExp, @Param("sort")String sort , @Param("direction")String direction);
    public int getCount(@Param("whereExp")String whereExp);
	
	public void add(Company entity);
	public void update(Company entity);
    public void delete(String id);
    
}



