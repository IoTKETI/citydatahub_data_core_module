package kr.re.keti.sc.dataservicebroker.common.datamapperhandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import kr.re.keti.sc.dataservicebroker.util.DateUtil;

/**
 * Date Array 형태의 DB 컬럼에 데이터를 입력하기 위한 TypeHandler 클래스
 */
public class BooleanArrayListTypeHandler extends BaseTypeHandler<ArrayList<Boolean>> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, ArrayList<Boolean> parameterList, JdbcType jdbcType) throws SQLException {
		StringBuilder str = new StringBuilder();
		str.append("{");
		for(Boolean parameter : parameterList) {
			str.append(Boolean.valueOf(parameter)).append(",");
		}
		str.deleteCharAt(str.length() - 1);
		str.append("}");
	    ps.setString(i, str.toString());
	}

	@Override
	public ArrayList<Boolean> getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return null;
	}

	@Override
	public ArrayList<Boolean> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public ArrayList<Boolean> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return null;
	}

}
