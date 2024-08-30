package com.Dao_;
import com.Utils.JDBCUtilsDruid;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BasicDao<E> {
    private QueryRunner queryRunner=new QueryRunner();

    /**
     *
     * @param sql
     * @param cls 得到类doMain反射机制
     * @param parameter
     * @return
     */
    //查询多行记录
    public List<E> QueryMulti(String sql,Class<E> cls,Object...parameter) throws SQLException {
        Connection connection= JDBCUtilsDruid.getConnect();

        try {
            return queryRunner.query(connection,sql,new BeanListHandler<E>(cls),parameter);
        } catch (SQLException throwables) {
            new RuntimeException(throwables);
        }
        finally {
            JDBCUtilsDruid.closeConnect(null,null,connection);
        }
        return null;
    }
    //查询单行记录
    public E QuerySignal(String sql,Class<E> cls,Object...parameter) throws SQLException {
        Connection connection= JDBCUtilsDruid.getConnect();
        try {
            return queryRunner.query(connection,sql,new BeanHandler<>(cls),parameter);
        } catch (SQLException throwables) {
            new RuntimeException(throwables);
        }
        finally {
            JDBCUtilsDruid.closeConnect(null,null,connection);
        }
        return null;
    }
    //查询单行单列记录
    public Object QueryOne(String sql,Object...parameter) throws SQLException {
        Connection connection= JDBCUtilsDruid.getConnect();
        try {
            return queryRunner.query(connection,sql,new ScalarHandler<E>(),parameter);
        } catch (SQLException throwables) {
            new RuntimeException(throwables);
        }
        finally {
            JDBCUtilsDruid.closeConnect(null,null,connection);
        }
        return null;
    }

    //增删该通用方法
    public int QueryDML(String sql,Object...parameters) throws SQLException {
        Connection connection= JDBCUtilsDruid.getConnect();
        try {
            return queryRunner.update(connection,sql,parameters);
        } catch (SQLException throwables) {
            new RuntimeException(throwables);
        }
        finally {
            JDBCUtilsDruid.closeConnect(null,null,connection);
        }
        return 0;
    }
}
