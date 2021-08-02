package com.pde.SocketServer;

import com.pde.SocketServer.Dao.Bean.User;
import com.pde.SocketServer.Dao.UserDao;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 操作用户的类，dao层，提供静态方法实现对用户的增删改查，通过操作mybatis的对象实现。
 */
public class UserTools {

    private static final SqlSessionFactory sqlSessionFactory;
    private static SqlSession sqlSession = null;
    private static UserDao dao = null;

    // 初始化mybatis
    static {
        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);

        } catch (IOException e) {
            e.printStackTrace();
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        sqlSession = sqlSessionFactory.openSession(true);
        dao = sqlSession.getMapper(UserDao.class);
        File authorityData = new File("authority_data");
        if(!authorityData.exists()){
            authorityData.mkdir();
            dao.createTable();
        }
    }

    public static void OnClose() {
        if (sqlSession != null) {
            sqlSession.close();
        }
    }

    public static boolean checkUserAndPassword(String userName, String pwd) {
        User user = dao.selectByName(userName);
        return user != null && user.getPwd().equals(pwd);
    }

    public static User getUser(String userName) {
        return dao.selectByName(userName);
    }

    // 刷新用户，通过传入的用户的id查出数据库中的用户信息返回
    public static User refreshUser(User user) {
        if (user == null) return null;
        return dao.selectById(user.getId());
    }

    public static List<User> getAllUser() {
        return dao.selectAll();
    }

    // 更新同id用户
    public static void updateUser(User user) {
        dao.update(user);
    }

    public static boolean createUser(User user) {
        if (dao.selectByName(user.getName()) == null) {
            return dao.insert(user) > 0;
        }
        return false;
    }

    public static void deleteUser(User user) {
        dao.delete(user);
    }
}
