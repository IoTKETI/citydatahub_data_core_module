package kr.re.keti.sc.dataservicebroker.common.datamapperhandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.JdbcType;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.HistoryStoreType;

public class HistoryStoreTypeHandler extends EnumTypeHandler<HistoryStoreType> {

	public HistoryStoreTypeHandler(Class<HistoryStoreType> type) {
		super(type);
	}
	
	@Override
	public void setParameter(PreparedStatement ps, int i, HistoryStoreType parameter, JdbcType jdbcType) throws SQLException {
		if (parameter != null) {
			ps.setString(i, parameter.getCode());
		} 
	}
	
	@Override
	public HistoryStoreType getResult(ResultSet rs, String columnName) throws SQLException {
		String result = rs.getString(columnName);
		try {
			return HistoryStoreType.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public HistoryStoreType getResult(ResultSet rs, int columnIndex) throws SQLException {
		String result = rs.getString(columnIndex);
		try {
			return HistoryStoreType.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public HistoryStoreType getResult(CallableStatement cs, int columnIndex) throws SQLException {
		String result = cs.getString(columnIndex);
		try {
			return HistoryStoreType.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}
