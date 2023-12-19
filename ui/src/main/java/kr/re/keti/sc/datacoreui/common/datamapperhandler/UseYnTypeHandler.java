package kr.re.keti.sc.datacoreui.common.datamapperhandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.JdbcType;

import kr.re.keti.sc.datacoreui.common.code.DataCoreUiCode.UseYn;

/**
 * TypeHandler class for data input/inquiry in UseYn type DB column
 * @FileName UseYnTypeHandler.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
public class UseYnTypeHandler extends EnumTypeHandler<UseYn> {

	/**
	 * UseYnTypeHandler constructor
	 * @param type
	 */
	public UseYnTypeHandler(Class<UseYn> type) {
		super(type);
	}
	
	/**
	 * Set UseYnTypeHandler parameter
	 */
	@Override
	public void setParameter(PreparedStatement ps, int i, UseYn parameter, JdbcType jdbcType) throws SQLException {
		if (parameter != null) {
			ps.setString(i, parameter.getCode());
		} 
	}
	
	/**
	 * Get UseYnTypeHandler result
	 */
	@Override
	public UseYn getResult(ResultSet rs, String columnName) throws SQLException {
		String result = rs.getString(columnName);
		try {
			return UseYn.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	/**
	 * Get UseYnTypeHandler result
	 */
	@Override
	public UseYn getResult(ResultSet rs, int columnIndex) throws SQLException {
		String result = rs.getString(columnIndex);
		try {
			return UseYn.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	/**
	 * Get UseYnTypeHandler result
	 */
	@Override
	public UseYn getResult(CallableStatement cs, int columnIndex) throws SQLException {
		String result = cs.getString(columnIndex);
		try {
			return UseYn.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}
