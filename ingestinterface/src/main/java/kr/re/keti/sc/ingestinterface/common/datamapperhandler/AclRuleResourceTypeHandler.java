package kr.re.keti.sc.ingestinterface.common.datamapperhandler;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mybatis enum type handler class (AclRuleResourceType)
 */
public class AclRuleResourceTypeHandler extends EnumTypeHandler<IngestInterfaceCode.AclRuleResourceType> {

	public AclRuleResourceTypeHandler(Class<IngestInterfaceCode.AclRuleResourceType> type) {
		super(type);
	}
	
	@Override
	public void setParameter(PreparedStatement ps, int i, IngestInterfaceCode.AclRuleResourceType parameter, JdbcType jdbcType) throws SQLException {
		if (parameter != null) {
			ps.setString(i, parameter.getCode());
		} 
	}
	
	@Override
	public IngestInterfaceCode.AclRuleResourceType getResult(ResultSet rs, String columnName) throws SQLException {
		String result = rs.getString(columnName);
		try {
			return IngestInterfaceCode.AclRuleResourceType.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public IngestInterfaceCode.AclRuleResourceType getResult(ResultSet rs, int columnIndex) throws SQLException {
		String result = rs.getString(columnIndex);
		try {
			return IngestInterfaceCode.AclRuleResourceType.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public IngestInterfaceCode.AclRuleResourceType getResult(CallableStatement cs, int columnIndex) throws SQLException {
		String result = cs.getString(columnIndex);
		try {
			return IngestInterfaceCode.AclRuleResourceType.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}
