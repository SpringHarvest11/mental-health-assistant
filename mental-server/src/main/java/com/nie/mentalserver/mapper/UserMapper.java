package com.nie.mentalserver.mapper;

import com.nie.mentalpojo.dto.UserLoginDTO;
import com.nie.mentalpojo.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 根据用户名或邮箱进行查询
     * @param username
     * @return
     */
    @Select( " select * from user where username = #{username}")
    User getUserByUsername(String username);
    @Select(" select * from user where email = #{email}")
    User getUserByEmail(String email);

    /**
     * 新增用户
     * @param user
     */
    @Insert("insert into user(id,username,email,phone,password,nickname,avatar,gender,birthday,user_type,status,created_at,updated_at) " +
            "values(#{id},#{username},#{email},#{phone},#{password},#{nickname},#{avatar},#{gender},#{birthday},#{userType},#{status},#{createdAt},#{updatedAt})")
    void insert(User user);
}
