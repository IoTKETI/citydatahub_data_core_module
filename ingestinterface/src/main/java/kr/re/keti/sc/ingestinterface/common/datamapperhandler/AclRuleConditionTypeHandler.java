package kr.re.keti.sc.ingestinterface.common.datamapperhandler;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mybatis enum type handler class (AclRuleCondition)
 */
public class AclRuleConditionTypeHandler extends EnumTypeHandler<IngestInterfaceCode.AclRuleCondition> {

	public AclRuleConditionTypeHandler(Class<IngestInterfaceCode.AclRuleCondition> type) {
		super(type);
	}
	
	@Override
	public void setParameter(PreparedStatement ps, int i, IngestInterfaceCode.AclRuleCondition parameter, JdbcType jdbcType) throws SQLException {
		if (parameter != null) {
			ps.setString(i, parameter.getCode());
		} 
	}
	
	@Override
	public IngestInterfaceCode.AclRuleCondition getResult(ResultSet rs, String columnName) throws SQLException {
		String result = rs.getString(columnName);
		try {
			return IngestInterfaceCode.AclRuleCondition.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public IngestInterfaceCode.AclRuleCondition getResult(ResultSet rs, int columnIndex) throws SQLException {
		String result = rs.getString(columnIndex);
		try {
			return IngestInterfaceCode.AclRuleCondition.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public IngestInterfaceCode.AclRuleCondition getResult(CallableStatement cs, int columnIndex) throws SQLException {
		String result = cs.getString(columnIndex);
		try {
			return IngestInterfaceCode.AclRuleCondition.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}
