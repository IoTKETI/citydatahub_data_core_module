package kr.re.keti.sc.dataservicebroker.common.datamapperhandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.JdbcType;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeValueType;

public class AttributeValueTypeHandler extends EnumTypeHandler<AttributeValueType> {

	public AttributeValueTypeHandler(Class<AttributeValueType> type) {
		super(type);
	}
	
	@Override
	public void setParameter(PreparedStatement ps, int i, AttributeValueType parameter, JdbcType jdbcType) throws SQLException {
		if (parameter != null) {
			ps.setString(i, parameter.getCode());
		} 
	}
	
	@Override
	public AttributeValueType getResult(ResultSet rs, String columnName) throws SQLException {
		String result = rs.getString(columnName);
		try {
			return AttributeValueType.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public AttributeValueType getResult(ResultSet rs, int columnIndex) throws SQLException {
		String result = rs.getString(columnIndex);
		try {
			return AttributeValueType.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public AttributeValueType getResult(CallableStatement cs, int columnIndex) throws SQLException {
		String result = cs.getString(columnIndex);
		try {
			return AttributeValueType.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}
