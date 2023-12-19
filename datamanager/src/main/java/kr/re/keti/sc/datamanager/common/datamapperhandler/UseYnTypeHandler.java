package kr.re.keti.sc.datamanager.common.datamapperhandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.JdbcType;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode.UseYn;

/**
 * Mybatis enum type handler class (UseYn)
 */
public class UseYnTypeHandler extends EnumTypeHandler<UseYn> {

	public UseYnTypeHandler(Class<UseYn> type) {
		super(type);
	}
	
	@Override
	public void setParameter(PreparedStatement ps, int i, UseYn parameter, JdbcType jdbcType) throws SQLException {
		if (parameter != null) {
			ps.setString(i, parameter.getCode());
		} 
	}
	
	@Override
	public UseYn getResult(ResultSet rs, String columnName) throws SQLException {
		String result = rs.getString(columnName);
		try {
			return UseYn.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public UseYn getResult(ResultSet rs, int columnIndex) throws SQLException {
		String result = rs.getString(columnIndex);
		try {
			return UseYn.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
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
