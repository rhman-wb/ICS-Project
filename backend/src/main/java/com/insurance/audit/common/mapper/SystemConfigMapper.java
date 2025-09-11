package com.insurance.audit.common.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SystemConfigMapper {

    @Select("SELECT config_value FROM system_configs WHERE config_key = #{key} AND is_deleted = 0 LIMIT 1")
    String getValue(@Param("key") String key);

    @Insert("INSERT INTO system_configs (id, config_key, config_value, config_type, description, is_system, created_by, created_at) " +
            "VALUES (#{id}, #{key}, #{value}, 'STRING', 'security bootstrap flag', 1, 'system', NOW()) " +
            "ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), updated_by='system', updated_at=NOW()")
    int upsertValue(@Param("key") String key, @Param("value") String value, @Param("id") String id);
}


