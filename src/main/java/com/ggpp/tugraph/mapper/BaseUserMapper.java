package com.ggpp.tugraph.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ggpp.tugraph.domain.BaseUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BaseUserMapper extends BaseMapper<BaseUser> {
}
