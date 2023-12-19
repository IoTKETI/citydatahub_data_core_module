package kr.re.keti.sc.ingestinterface.common.datamapperhandler;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mybatis enum type handler class (JsonldContextKind)
 */
public class JsonldContextKindTypeHandler extends EnumTypeHandler<IngestInterfaceCode.JsonldContextKind> {

	public JsonldContextKindTypeHandler(Class<IngestInterfaceCode.JsonldContextKind> type) {
		super(type);
	}
	
	@Override
	public void setParameter(PreparedStatement ps, int i, IngestInterfaceCode.JsonldContextKind parameter, JdbcType jdbcType) throws SQLException {
		if (parameter != null) {
			ps.setString(i, parameter.getCode());
		} 
	}
	
	@Override
	public IngestInterfaceCode.JsonldContextKind getResult(ResultSet rs, String columnName) throws SQLException {
		String result = rs.getString(columnName);
		try {
			return IngestInterfaceCode.JsonldContextKind.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public IngestInterfaceCode.JsonldContextKind getResult(ResultSet rs, int columnIndex) throws SQLException {
		String result = rs.getString(columnIndex);
		try {
			return IngestInterfaceCode.JsonldContextKind.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public IngestInterfaceCode.JsonldContextKind getResult(CallableStatement cs, int columnIndex) throws SQLException {
		String result = cs.getString(columnIndex);
		try {
			return IngestInterfaceCode.JsonldContextKind.parseType(result);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}
