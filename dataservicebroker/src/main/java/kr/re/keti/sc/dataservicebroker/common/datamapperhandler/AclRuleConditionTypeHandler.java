package kr.re.keti.sc.dataservicebroker.common.datamapperhandler;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AclRuleConditionTypeHandler extends EnumTypeHandler<DataServiceBrokerCode.AclRuleCondition> {

	public AclRuleConditionTypeHandler(Class<DataServiceBrokerCode.AclRuleCondition> type) {
		super(type);
	}
	
	@Override
	public void setParameter(PreparedStatement ps, int i, DataServiceBrokerCode.AclRuleCondition parameter, JdbcType jdbcType) throws SQLException {
		if (parameter != null) {
			ps.setString(i, parameter.getCode());
		} 
	}
	
	@Override
	public DataServiceBrokerCode.AclRuleCondition getResult(ResultSet rs, String columnName) throws SQLException {
		String result = rs.getString(columnName);
		try {
			return DataServiceBrokerCode.AclRuleCondition.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public DataServiceBrokerCode.AclRuleCondition getResult(ResultSet rs, int columnIndex) throws SQLException {
		String result = rs.getString(columnIndex);
		try {
			return DataServiceBrokerCode.AclRuleCondition.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public DataServiceBrokerCode.AclRuleCondition getResult(CallableStatement cs, int columnIndex) throws SQLException {
		String result = cs.getString(columnIndex);
		try {
			return DataServiceBrokerCode.AclRuleCondition.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}
