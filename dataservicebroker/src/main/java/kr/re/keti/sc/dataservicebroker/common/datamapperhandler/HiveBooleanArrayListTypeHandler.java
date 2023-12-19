package kr.re.keti.sc.dataservicebroker.common.datamapperhandler;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * Integer Array 형태의 DB 컬럼에 데이터 입력/조회를 위한 TypeHandler 클래스
 */
public class HiveBooleanArrayListTypeHandler extends BaseTypeHandler<ArrayList<Boolean>> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, ArrayList<Boolean> parameterList, JdbcType jdbcType) throws SQLException {
		StringBuilder str = new StringBuilder();
		boolean isNotNull = false;

		for (Boolean parameter : parameterList) {
			if (parameter != null) {
				isNotNull = true;
				str.append(parameter).append(",");
			}
		}

		if (isNotNull) str.deleteCharAt(str.length() - 1);
		ps.setString(i, str.toString());
	}

	@Override
	public ArrayList<Boolean> getNullableResult(ResultSet rs, String columnName) throws SQLException {
		Array array = rs.getArray(columnName);
		ArrayList<Boolean> items = new ArrayList<>();

        if (!rs.wasNull()) {

        	Boolean[] arrayItems = (Boolean[]) array.getArray();

            for (int i = 0; i < arrayItems.length; i++) {

                items.add(arrayItems[i]);

            }
            return items;
        } else {
            return null;
        }
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
