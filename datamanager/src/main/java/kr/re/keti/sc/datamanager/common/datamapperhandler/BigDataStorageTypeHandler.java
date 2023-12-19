package kr.re.keti.sc.datamanager.common.datamapperhandler;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode.BigDataStorageType;

/**
 * Mybatis enum type handler class (BigDataStorageType)
 */
public class BigDataStorageTypeHandler extends BaseTypeHandler<ArrayList<BigDataStorageType>> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, ArrayList<BigDataStorageType> parameterList, JdbcType jdbcType) throws SQLException {
		if(parameterList == null || parameterList.size() == 0) {
			ps.setNull(i, Types.ARRAY);
			return;
		}
		StringBuilder str = new StringBuilder();
		str.append("{");
		for(BigDataStorageType parameter : parameterList) {
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
	public ArrayList<BigDataStorageType> getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String str = rs.getString(columnName);
		if(str == null) return null;

		ArrayList<BigDataStorageType> bigDataStorageTypes = new ArrayList<BigDataStorageType>();
		str = str.replace("{", "").replace("}", "");
		String[] strArray = str.split(",");
	    for(String s : strArray) {
	    	bigDataStorageTypes.add(BigDataStorageType.parseType(s));
	    }
	    return bigDataStorageTypes;
	}

	@Override
	public ArrayList<BigDataStorageType> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String str = rs.getString(columnIndex);
		if(str == null) return null;

		ArrayList<BigDataStorageType> bigDataStorageTypes = new ArrayList<BigDataStorageType>();
		str = str.replace("{", "").replace("}", "");
		String[] strArray = str.split(",");
	    for(String s : strArray) {
	    	bigDataStorageTypes.add(BigDataStorageType.parseType(s));
	    }
	    return bigDataStorageTypes;
	}

	@Override
	public ArrayList<BigDataStorageType> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String str = cs.getString(columnIndex);
		if(str == null) return null;

		ArrayList<BigDataStorageType> bigDataStorageTypes = new ArrayList<BigDataStorageType>();
		str = str.replace("{", "").replace("}", "");
		String[] strArray = str.split(",");
	    for(String s : strArray) {
	    	bigDataStorageTypes.add(BigDataStorageType.parseType(s));
	    }
	    return bigDataStorageTypes;
	}

}
