package kr.re.keti.sc.datamanager.common.datamapperhandler;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode;
import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mybatis enum type handler class (AclRuleResourceType)
 */
public class AclRuleResourceTypeHandler extends EnumTypeHandler<DataManagerCode.AclRuleResourceType> {

	public AclRuleResourceTypeHandler(Class<DataManagerCode.AclRuleResourceType> type) {
		super(type);
	}
	
	@Override
	public void setParameter(PreparedStatement ps, int i, DataManagerCode.AclRuleResourceType parameter, JdbcType jdbcType) throws SQLException {
		if (parameter != null) {
			ps.setString(i, parameter.getCode());
		} 
	}
	
	@Override
	public DataManagerCode.AclRuleResourceType getResult(ResultSet rs, String columnName) throws SQLException {
		String result = rs.getString(columnName);
		try {
			return DataManagerCode.AclRuleResourceType.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public DataManagerCode.AclRuleResourceType getResult(ResultSet rs, int columnIndex) throws SQLException {
		String result = rs.getString(columnIndex);
		try {
			return DataManagerCode.AclRuleResourceType.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public DataManagerCode.AclRuleResourceType getResult(CallableStatement cs, int columnIndex) throws SQLException {
		String result = cs.getString(columnIndex);
		try {
			return DataManagerCode.AclRuleResourceType.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}
