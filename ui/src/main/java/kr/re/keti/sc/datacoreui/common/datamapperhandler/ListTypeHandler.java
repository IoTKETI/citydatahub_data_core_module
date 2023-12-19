package kr.re.keti.sc.datacoreui.common.datamapperhandler;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * TypeHandler class for data input/inquiry in Array type DB column
 * @FileName ListTypeHandler.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
public class ListTypeHandler extends BaseTypeHandler<Object> {

	/**
	 * Set not null parameter
	 */
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Object o, JdbcType jdbcType) throws SQLException {
    }

    /**
     * Get nullable result
     */
    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {

        Array array = rs.getArray(columnName);
        List items = new ArrayList<>();

        if (!rs.wasNull()) {

            Object[] arrayItems = (Object[]) array.getArray();

            for (int i = 0; i < arrayItems.length; i++) {
                items.add(arrayItems[i]);
            }

            return items;
        } else {
            return null;
        }
    }

    /**
     * Get nullable result
     */
    @Override
    public Object getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return null;
    }

    /**
     * Get nullable result
     */
    @Override
    public Object getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return null;
    }
}
