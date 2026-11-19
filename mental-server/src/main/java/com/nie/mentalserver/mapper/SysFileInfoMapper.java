package com.nie.mentalserver.mapper;

import com.nie.mentalpojo.entity.SysFileInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface SysFileInfoMapper {
    @Insert("""
            insert into sys_file_info
                (original_name, file_path, file_size, file_type,
                 business_type, business_id, business_field, upload_user_id,
                 is_temp, status)
            values
                (#{originalName}, #{filePath}, #{fileSize}, #{fileType},
                 #{businessType}, #{businessId}, #{businessField}, #{uploadUserId},
                 #{isTemp}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysFileInfo info);



}
