package kr.re.keti.sc.datacoreui.api.code.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.re.keti.sc.datacoreui.api.code.vo.CodeBaseVO;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeGroupBaseVO;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeGroupRequestVO;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeGroupVO;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeRequestVO;
import kr.re.keti.sc.datacoreui.api.code.vo.CodeVO;

/**
 * This is a UI code management DAO class.
 * @FileName CodeDAO.java
 * @Project citydatahub_datacore_ui
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 22.
 * @Author Elvin
 */
@Repository
public class CodeDAO {
	@Autowired
	private SqlSessionTemplate sqlSession;

	/**
	 * Create code group
	 * @param codeGroupBaseVO
	 * @return
	 */
	public int createCodeGroup(CodeGroupBaseVO codeGroupBaseVO) {
		return sqlSession.insert("datacoreui.code.createCodeGroup", codeGroupBaseVO);
	}
	
	/**
	 * Update code group
	 * @param codeGroupBaseVO
	 * @return
	 */
	public int updateCodeGroup(CodeGroupBaseVO codeGroupBaseVO) {
		return sqlSession.update("datacoreui.code.updateCodeGroup", codeGroupBaseVO);
	}
	
	/**
	 * Delete code group
	 * @param codeGroupId
	 * @return
	 */
	public int deleteCodeGroup(String codeGroupId) {
		return sqlSession.delete("datacoreui.code.deleteCodeGroup", codeGroupId);
	}
	
	/**
	 * Select code group
	 * @param codeGroupId
	 * @return
	 */
	public CodeGroupBaseVO getCodeGroup(String codeGroupId) {
		return sqlSession.selectOne("datacoreui.code.selectCodeGroup", codeGroupId);
	}
	
	/**
	 * Select multiple code group
	 * @param codeGroupRequestVO
	 * @return
	 */
	public List<CodeGroupVO> getCodeGroups(CodeGroupRequestVO codeGroupRequestVO) {
		return sqlSession.selectList("datacoreui.code.selectCodeGroups", codeGroupRequestVO);
	}
	
	/**
	 * Create code
	 * @param codeBaseVO
	 * @return
	 */
	public int createCode(CodeBaseVO codeBaseVO) {
		return sqlSession.insert("datacoreui.code.createCode", codeBaseVO);
	}
	
	/**
	 * Update code
	 * @param codeBaseVO
	 * @return
	 */
	public int updateCode(CodeBaseVO codeBaseVO) {
		return sqlSession.update("datacoreui.code.updateCode", codeBaseVO);
	}
	
	/**
	 * Delete code
	 * @param codeBaseVO
	 * @return
	 */
	public int deleteCode(CodeBaseVO codeBaseVO) {
		return sqlSession.delete("datacoreui.code.deleteCode", codeBaseVO);
	}
	
	/**
	 * Select code
	 * @param codeBaseVO
	 * @return
	 */
	public CodeBaseVO getCode(CodeBaseVO codeBaseVO) {
		return sqlSession.selectOne("datacoreui.code.selectCode", codeBaseVO);
	}
	
	/**
	 *  Select multiple code
	 * @param codeRequestVO
	 * @return
	 */
	public List<CodeVO> getCodes(CodeRequestVO codeRequestVO) {
		return sqlSession.selectList("datacoreui.code.selectCodes", codeRequestVO);
	}
}
