package kr.re.keti.sc.dataservicebroker.common.datamapperhandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import kr.re.keti.sc.dataservicebroker.util.DateUtil;

/**
 * Date Array 형태의 DB 컬럼에 데이터를 입력하기 위한 TypeHandler 클래스
 */
public class HiveDateArrayListTypeHandler extends BaseTypeHandler<ArrayList<Date>> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, ArrayList<Date> parameterList, JdbcType jdbcType) throws SQLException {
		StringBuilder str = new StringBuilder();
		boolean isNotNull = false;

		for (Date parameter : parameterList) {
			if (parameter != null) {
				isNotNull = true;
				str.append(DateUtil.dateToDbFormatString(parameter)).append(",");
			}
		}

		if (isNotNull) str.deleteCharAt(str.length() - 1);
	    ps.setString(i, str.toString());
	}

	@Override
	public ArrayList<Date> getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return null;
	}

	@Override
	public ArrayList<Date> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public ArrayList<Date> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return null;
	}

}
