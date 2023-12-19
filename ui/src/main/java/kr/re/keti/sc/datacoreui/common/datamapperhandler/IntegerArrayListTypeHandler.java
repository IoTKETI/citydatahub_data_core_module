package kr.re.keti.sc.datacoreui.common.datamapperhandler;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * TypeHandler class for data input/inquiry in Integer Array type DB column
 * @FileName IntegerArrayListTypeHandler.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
public class IntegerArrayListTypeHandler extends BaseTypeHandler<ArrayList<Integer>> {

	/**
	 * Set not null parameter
	 */
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, ArrayList<Integer> parameterList, JdbcType jdbcType) throws SQLException {
		StringBuilder str = new StringBuilder();
		str.append("{");
		for(int idx=0; idx<parameterList.size(); idx++) {
			str.append(parameterList.get(idx)).append(",");
		}
		str.deleteCharAt(str.length() - 1);
		str.append("}");
	    ps.setString(i, str.toString());
	}

	/**
	 * Get nullable result
	 */
	@Override
	public ArrayList<Integer> getNullableResult(ResultSet rs, String columnName) throws SQLException {
		Array array = rs.getArray(columnName);
		ArrayList<Integer> items = new ArrayList<>();

        if (!rs.wasNull()) {

        	BigDecimal[] arrayItems = (BigDecimal[]) array.getArray();

            for (int i = 0; i < arrayItems.length; i++) {

                items.add(arrayItems[i].intValue());

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
	public ArrayList<Integer> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * Get nullable result
	 */
	@Override
	public ArrayList<Integer> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return null;
	}

}
