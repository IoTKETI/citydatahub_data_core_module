package kr.re.keti.sc.datacoreusertool.api.map.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.re.keti.sc.datacoreusertool.api.map.vo.MapSearchConditionBaseIdResponseVO;
import kr.re.keti.sc.datacoreusertool.api.map.vo.MapSearchConditionBaseResponseVO;
import kr.re.keti.sc.datacoreusertool.api.map.vo.MapSearchConditionBaseVO;

/**
 * Map DAO class
 * @FileName MapDAO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Repository
public class MapDAO {
	@Autowired
	private SqlSessionTemplate sqlSession;

	public int createMapSearchCondition(MapSearchConditionBaseVO mapSearchBaseVO) {
		return sqlSession.insert("datacoreusertool.map.createMapSearchCondition", mapSearchBaseVO);
	}

	public int updateMapSearchCondition(MapSearchConditionBaseVO mapSearchBaseVO) {
		return sqlSession.update("datacoreusertool.map.updateMapSearchCondition", mapSearchBaseVO);
	}

	public int deleteMapSearchCondition(MapSearchConditionBaseVO mapSearchBaseVO) {
		return sqlSession.delete("datacoreusertool.map.deleteMapSearchCondition", mapSearchBaseVO);
	}
	
	public MapSearchConditionBaseResponseVO getMapSearchCondition(MapSearchConditionBaseVO mapSearchBaseVO) {
		return sqlSession.selectOne("datacoreusertool.map.selectMapSearchCondition", mapSearchBaseVO);
	}

	public List<MapSearchConditionBaseIdResponseVO> getMapSearchConditions(MapSearchConditionBaseVO mapSearchBaseVO) {
		return sqlSession.selectList("datacoreusertool.map.selectMapSearchConditions", mapSearchBaseVO);
	}

}
