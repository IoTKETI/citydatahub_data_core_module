package kr.re.keti.sc.datacoreusertool.api.widgetdashboard.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetDashboardBaseVO;
import kr.re.keti.sc.datacoreusertool.api.widgetdashboard.vo.WidgetDashboardVO;

/**
 * WidgetDashboard DAO class
 * @FileName WidgetDashboardDAO.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Repository
public class WidgetDashboardDAO {
	@Autowired
	private SqlSessionTemplate sqlSession;

	public int createWidget(WidgetDashboardVO widgetDashboardVO) {
		return sqlSession.insert("datacoreusertool.widgetdashboard.createWidget", widgetDashboardVO);
	}

	public int updateWidget(WidgetDashboardVO widgetDashboardVO) {
		return sqlSession.update("datacoreusertool.widgetdashboard.updateWidget", widgetDashboardVO);
	}
	
	public int updateWidgetLayout(WidgetDashboardVO widgetDashboardVO) {
		return sqlSession.update("datacoreusertool.widgetdashboard.updateWidgetLayout", widgetDashboardVO);
	}

	public int deleteWidget(WidgetDashboardVO widgetDashboardVO) {
		return sqlSession.delete("datacoreusertool.widgetdashboard.deleteWidget", widgetDashboardVO);
	}

	public List<WidgetDashboardVO> getWidgets(WidgetDashboardVO widgetDashboardVO) {
		return sqlSession.selectList("datacoreusertool.widgetdashboard.selectAllWidget", widgetDashboardVO);
	}
	
	public WidgetDashboardVO getWidget(WidgetDashboardVO widgetDashboardVO) {
		return sqlSession.selectOne("datacoreusertool.widgetdashboard.selectWidget", widgetDashboardVO);
	}
	
	public int createDashboard(WidgetDashboardBaseVO widgetDashboardBaseVO) {
		return sqlSession.insert("datacoreusertool.widgetdashboard.createDashboard", widgetDashboardBaseVO);
	}
	
	public int updateDashboard(WidgetDashboardBaseVO widgetDashboardBaseVO) {
		return sqlSession.update("datacoreusertool.widgetdashboard.updateDashboard", widgetDashboardBaseVO);
	}
	
	public int deleteDashboard(WidgetDashboardBaseVO widgetDashboardBaseVO) {
		return sqlSession.delete("datacoreusertool.widgetdashboard.deleteDashboard", widgetDashboardBaseVO);
	}
	
	public List<WidgetDashboardBaseVO> getDashboards(String userId) {
		return sqlSession.selectList("datacoreusertool.widgetdashboard.selectDashboards", userId);
	}
}
