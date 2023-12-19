package kr.re.keti.sc.datacoreui.common.datamapperhandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import kr.re.keti.sc.datacoreui.util.DateUtil;

/**
 * TypeHandler class to input data to DB column in Date Array format
 * @FileName DateArrayListTypeHandler.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
public class DateArrayListTypeHandler extends BaseTypeHandler<ArrayList<Date>> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, ArrayList<Date> parameterList, JdbcType jdbcType) throws SQLException {
		StringBuilder str = new StringBuilder();
		str.append("{");
		for(Date parameter : parameterList) {
			str.append(DateUtil.dateToDbFormatString(parameter)).append(",");
		}
		str.deleteCharAt(str.length() - 1);
		str.append("}");
	    ps.setString(i, str.toString());
	}

	@Override
	public ArrayList<Date> getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return null;
	}

	@Override
	public ArrayList<Date> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public ArrayList<Date> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return null;
	}

}
