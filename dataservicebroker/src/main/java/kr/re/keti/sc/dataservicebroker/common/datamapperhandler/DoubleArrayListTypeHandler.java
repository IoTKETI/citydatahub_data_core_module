package kr.re.keti.sc.dataservicebroker.common.datamapperhandler;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * Double Array 형태의 DB 컬럼에 데이터 입력/조회를 위한 TypeHandler 클래스
 */
@Deprecated
public class DoubleArrayListTypeHandler extends BaseTypeHandler<ArrayList<Double>> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, ArrayList<Double> parameterList, JdbcType jdbcType) throws SQLException {
		StringBuilder str = new StringBuilder();
		str.append("{");
		for(int idx=0; idx<parameterList.size(); idx++) {
			str.append(parameterList.get(idx)).append(",");
		}
		str.deleteCharAt(str.length() - 1);
		str.append("}");
		ps.setString(i, str.toString());
	}

	@Override
	public ArrayList<Double> getNullableResult(ResultSet rs, String columnName) throws SQLException {
		Array array = rs.getArray(columnName);
		ArrayList<Double> items = new ArrayList<>();

		if (!rs.wasNull()) {

			BigDecimal[] arrayItems = (BigDecimal[]) array.getArray();

			for (int i = 0; i < arrayItems.length; i++) {

				items.add(arrayItems[i].doubleValue());

			}
			return items;
		} else {
			return null;
		}
	}

	@Override
	public ArrayList<Double> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public ArrayList<Double> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return null;
	}

}
