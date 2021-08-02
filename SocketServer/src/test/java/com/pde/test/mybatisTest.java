package com.pde.test;

import com.pde.SocketServer.Dao.Bean.User;
import com.pde.SocketServer.Dao.UserDao;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class mybatisTest {
    SqlSessionFactory sqlSessionFactory = null;
    SqlSession sqlSession = null;

    @Before
    public void before() throws IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

    }

    @After
    public void after() {
        if (sqlSession != null) {
            sqlSession.close();
        }
    }
    @Test
    public void Test1() {
        sqlSession = sqlSessionFactory.openSession(true);
        UserDao dao = sqlSession.getMapper(UserDao.class);
//        dao.dropTable();
        dao.createTable();
        User user = new User("nnnnnn","eeeee");
        user.setA_browse(true);
        dao.insert(user);

        User user1 = dao.selectByName("nnnnnn");
        Integer delete = dao.delete(user1);
        dao.insert(user);
        user.setA_browse(true);
        dao.update(user);
        List<User> users = dao.selectAll();
        users.forEach(System.out::println);
    }
}
