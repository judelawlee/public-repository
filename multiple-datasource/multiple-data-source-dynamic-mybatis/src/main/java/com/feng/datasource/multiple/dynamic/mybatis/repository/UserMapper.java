package com.feng.datasource.multiple.dynamic.mybatis.repository;


import com.feng.datasource.multiple.dynamic.mybatis.annotation.DataSource;
import com.feng.datasource.multiple.dynamic.mybatis.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Auther: yukong
 * @Date: 2018/8/13 19:47
 * @Description: UserMapper接口
 */
@Repository
//@DataSource("slave1")
public interface UserMapper {

    /**
     * 新增用户
     * @param user
     * @return
     */
    @DataSource  //默认数据源
    int save(User user);

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    @DataSource("slave1")  //默认数据源
    int update(User user);

    /**
     * 根据id删除
     * @param id
     * @return
     */
    @DataSource  //默认数据源
    int deleteById(Long id);

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @DataSource("slave1")  //slave1
    User selectById(Long id);

    /**
     * 查询所有用户信息
     * @return
     */
    List<User> selectAll();
}
