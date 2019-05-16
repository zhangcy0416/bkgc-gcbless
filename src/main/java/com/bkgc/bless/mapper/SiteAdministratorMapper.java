package com.bkgc.bless.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.bkgc.bean.bless.SiteAdministrator;

public interface SiteAdministratorMapper
{
	public SiteAdministrator get(String id);
	public List<SiteAdministrator> getAll(@Param("whereExp")String whereExp,@Param("sort")String sort , @Param("direction")String direction);
    public List<SiteAdministrator> getByPage(@Param("offset")int offset , @Param("size")int size ,@Param("whereExp")String whereExp, @Param("sort")String sort , @Param("direction")String direction);
    public int getCount(@Param("whereExp")String whereExp);
	
	public void add(SiteAdministrator entity);
	public void update(SiteAdministrator entity);
    public void delete(String id);
    
}



