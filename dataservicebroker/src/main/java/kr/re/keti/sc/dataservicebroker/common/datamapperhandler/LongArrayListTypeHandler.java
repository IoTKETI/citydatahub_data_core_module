package kr.re.keti.sc.dataservicebroker.common.datamapperhandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;

/**
 * Long Array 형태의 DB 컬럼에 데이터 입력/조회를 위한 TypeHandler 클래스
 */
public class LongArrayListTypeHandler extends BaseTypeHandler<ArrayList<Long>> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, ArrayList<Long> parameterList, JdbcType jdbcType) throws SQLException {
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
	public ArrayList<Long> getNullableResult(ResultSet rs, String columnName) throws SQLException {
		Array array = rs.getArray(columnName);
		ArrayList<Long> items = new ArrayList<>();

        if (!rs.wasNull()) {

        	BigDecimal[] arrayItems = (BigDecimal[]) array.getArray();

            for (int i = 0; i < arrayItems.length; i++) {

                items.add(arrayItems[i].longValue());

            }
            return items;
        } else {
            return null;
        }
	}

	@Override
	public ArrayList<Long> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public ArrayList<Long> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return null;
	}

}
