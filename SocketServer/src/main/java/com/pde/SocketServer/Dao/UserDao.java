package com.pde.SocketServer.Dao;

import com.pde.SocketServer.Dao.Bean.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户权限表的操作接口
 */
public interface UserDao {
    @Update("create table user\n" +
            "(\n" +
            "\tid INTEGER not null\n" +
            "\t\tconstraint user_pk\n" +
            "\t\t\tprimary key autoincrement,\n" +
            "\tname CHAR(50) not null unique,\n" +
            "\tpwd CHAR(50) not null,\n" +
            "\ta_browse tinyint not null,\n" +
            "\ta_upload tinyint not null,\n" +
            "\ta_download tinyint not null,\n" +
            "\ta_refactor tinyint not null\n" +
            ");\n")
    void createTable();

    @Update("DROP TABLE IF EXISTS user;")
    void dropTable();

    @Select("SELECT * FROM user;")
    List<User> selectAll();

    @Select("SELECT * FROM user WHERE name = #{name};")
    User selectByName(String name);

    @Select("SELECT * FROM user WHERE id = #{id};")
    User selectById(int id);

    @Delete("DELETE FROM user WHERE id = #{id};")
    Integer delete(User person);

    @Update("UPDATE user set name = #{name},pwd = #{pwd}," +
     "a_browse = #{a_browse},a_upload = #{a_upload},a_download = #{a_download},a_refactor = #{a_refactor} WHERE id = #{id};")
    Integer update(User person);

    @Insert("INSERT INTO `user` (name,pwd,a_browse,a_upload,a_download,a_refactor) VALUES (#{name},#{pwd},#{a_browse},#{a_upload},#{a_download},#{a_refactor})")
    Integer insert(User person);

}
