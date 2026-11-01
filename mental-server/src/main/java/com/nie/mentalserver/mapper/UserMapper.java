package com.nie.mentalserver.mapper;

import com.nie.mentalpojo.dto.UserLoginDTO;
import com.nie.mentalpojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 根据用户名或邮箱进行查询
     * @param userLoginDTO
     * @return
     */
    @Select( " select * from user where username = #{username}")
    User getUserByUsername(UserLoginDTO userLoginDTO);

    @Select(" select * from user where email = #{username}")
    User getUserByEmail(UserLoginDTO userLoginDTO);
}
