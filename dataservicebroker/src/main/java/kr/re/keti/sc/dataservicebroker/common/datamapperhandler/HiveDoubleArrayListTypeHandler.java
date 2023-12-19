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
 * Float Array 형태의 DB 컬럼에 데이터 입력/조회를 위한 TypeHandler 클래스
 */
public class HiveDoubleArrayListTypeHandler extends BaseTypeHandler<ArrayList<BigDecimal>> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, ArrayList<BigDecimal> parameterList, JdbcType jdbcType) throws SQLException {
	// 	StringBuilder str = new StringBuilder();
	// 	boolean isNotNull = false;
	// 	for (int idx = 0; idx<parameterList.size(); idx++) {
	// 		BigDecimal parameter = parameterList.get(idx);

	// 		if (parameter != null) {
	// 			isNotNull = true;
	// 			str.append(parameter.doubleValue()).append(",");
	// 		}
	// 	}

	// 	if (isNotNull) {
	// 		str.deleteCharAt(str.length() - 1);
	// 	}

	//     ps.setString(i, str.toString());
	// }
	StringBuilder str = new StringBuilder();
		for(int idx=0; idx<parameterList.size(); idx++) {
			str.append(parameterList.get(idx)).append(",");
		}
		str.deleteCharAt(str.length() - 1);
	    ps.setString(i, str.toString());
	}

	@Override
	public ArrayList<BigDecimal> getNullableResult(ResultSet rs, String columnName) throws SQLException {
		Array array = rs.getArray(columnName);
		ArrayList<BigDecimal> items = new ArrayList<>();

        if (!rs.wasNull()) {

        	BigDecimal[] arrayItems = (BigDecimal[]) array.getArray();

            for (int i = 0; i < arrayItems.length; i++) {

                items.add(arrayItems[i]);

            }
            return items;
        } else {
            return null;
        }
	}

	@Override
	public ArrayList<BigDecimal> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public ArrayList<BigDecimal> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return null;
	}
}
