package kr.re.keti.sc.ingestinterface.common.datamapperhandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.util.ArrayList;

import static kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.AclRuleOperationType;

/**
 * Mybatis enum type handler class (AclRuleOperationType)
 */
public class AclRuleOperationTypeHandler extends BaseTypeHandler<ArrayList<AclRuleOperationType>> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, ArrayList<AclRuleOperationType> parameterList, JdbcType jdbcType) throws SQLException {
		if(parameterList == null || parameterList.size() == 0) {
			ps.setNull(i, Types.ARRAY);
			return;
		}
		StringBuilder str = new StringBuilder();
		str.append("{");
		for(AclRuleOperationType parameter : parameterList) {
			if(parameter == null) {
				continue;
			}
			str.append("\"").append(parameter.getCode()).append("\"").append(",");
		}
		str.deleteCharAt(str.length() - 1);
		str.append("}");
	    ps.setString(i, str.toString());
	}

	@Override
	public ArrayList<AclRuleOperationType> getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String str = rs.getString(columnName);
		if(str == null) return null;

		ArrayList<AclRuleOperationType> aclRuleOperationTypes = new ArrayList<AclRuleOperationType>();
		str = str.replace("{", "").replace("}", "").replace("\"", "");
		String[] strArray = str.split(",");
	    for(String s : strArray) {
	    	aclRuleOperationTypes.add(AclRuleOperationType.parseType(s));
	    }
	    return aclRuleOperationTypes;
	}

	@Override
	public ArrayList<AclRuleOperationType> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String str = rs.getString(columnIndex);
		if(str == null) return null;

		ArrayList<AclRuleOperationType> aclRuleOperationTypes = new ArrayList<AclRuleOperationType>();
		str = str.replace("{", "").replace("}", "").replace("\"", "");
		String[] strArray = str.split(",");
	    for(String s : strArray) {
	    	aclRuleOperationTypes.add(AclRuleOperationType.parseType(s));
	    }
	    return aclRuleOperationTypes;
	}

	@Override
	public ArrayList<AclRuleOperationType> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String str = cs.getString(columnIndex);
		if(str == null) return null;

		ArrayList<AclRuleOperationType> aclRuleOperationTypes = new ArrayList<AclRuleOperationType>();
		str = str.replace("{", "").replace("}", "").replace("\"", "");
		String[] strArray = str.split(",");
	    for(String s : strArray) {
	    	aclRuleOperationTypes.add(AclRuleOperationType.parseType(s));
	    }
	    return aclRuleOperationTypes;
	}

}
