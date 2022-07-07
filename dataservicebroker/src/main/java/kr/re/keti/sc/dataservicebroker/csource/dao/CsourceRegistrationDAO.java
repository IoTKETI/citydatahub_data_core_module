package kr.re.keti.sc.dataservicebroker.csource.dao;

import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.util.QueryUtil;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import org.mybatis.spring.SqlSessionTemplate;
import org.postgis.LineString;
import org.postgis.MultiLineString;
import org.postgis.MultiPolygon;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.vo.DbConditionVO;
import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationBaseDaoVO;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationInfoDaoVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.Attribute;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.dataservicebroker.util.ConvertTimeParamUtil;
import lombok.extern.slf4j.Slf4j;

import static kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.*;

@Component
@Slf4j
public class CsourceRegistrationDAO implements CsourceRegistrationDAOInterface {

    private final SqlSessionTemplate sqlSession;
    private final SqlSessionTemplate retrieveSqlSession;
    private final DataModelManager dataModelManager;
    private final ObjectMapper objectMapper;

    @Value("${entity.retrieve.default.limit:1000}")
    private Integer defaultLimit;

    private String defaultLocationAttrName = "location";

    //Geo-query default EPSG 세팅
    @Value("${geometry.default.EPSG:4326}")
    private String defaultEPSG;

    public CsourceRegistrationDAO(
            SqlSessionTemplate sqlSession,
            SqlSessionTemplate retrieveSqlSession,
            DataModelManager dataModelManager,
            ObjectMapper objectMapper
    ) {
        this.sqlSession = sqlSession;
        this.retrieveSqlSession = retrieveSqlSession;
        this.dataModelManager = dataModelManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public Integer createCsourceRegistrationBase(CsourceRegistrationBaseDaoVO csourceRegistrationBaseDaoVO) {
        return sqlSession.update("dataservicebroker.csource.registration.createCsourceRegistrationBase", csourceRegistrationBaseDaoVO);
    }

    @Override
    public Integer createCsourceRegistrationInfo(List<CsourceRegistrationInfoDaoVO> csourceRegistrationInfoDaoVOs) {

        Map<String, Object> csourceRegistrationInfoMap = new HashMap<String, Object>();
        csourceRegistrationInfoMap.put("list", csourceRegistrationInfoDaoVOs);
        return sqlSession.update("dataservicebroker.csource.registration.createCsourceRegistrationInfo", csourceRegistrationInfoMap);
    }


    @Override
    public Integer createCsourceRegistrationEntity(List<CsourceRegistrationEntityDaoVO> csourceRegistrationEntityDaoVOs) {

        Map<String, Object> csourceRegistrationEntityMap = new HashMap<String, Object>();
        csourceRegistrationEntityMap.put("list", csourceRegistrationEntityDaoVOs);
        return sqlSession.update("dataservicebroker.csource.registration.createCsourceRegistrationEntity", csourceRegistrationEntityMap);
    }


    /**
     * contet source 개별 조회
     *
     * @param registrationId
     * @return
     * @throws JsonProcessingException
     */
    @Override
    public List<CsourceRegistrationBaseDaoVO> retrieveCsourceRegistration(String registrationId) {

        List<CsourceRegistrationBaseDaoVO> csourceRegistrationBaseDaoVOs = retrieveSqlSession.selectList("dataservicebroker.csource.registration.selectCsourceRegistrationByRegistrationId", registrationId);
        return csourceRegistrationBaseDaoVOs;
    }


    /**
     * contet source 전체 조회
     *
     * @param queryVO
     * @return
     * @throws JsonProcessingException
     */
    @Override
    public List<CsourceRegistrationBaseDaoVO> queryCsourceRegistration(QueryVO queryVO) {

        DbConditionVO dbConditionVO = setQueryCondition(queryVO);
        List<CsourceRegistrationBaseDaoVO> csourceRegistrationBaseDaoVOs = retrieveSqlSession.selectList("dataservicebroker.csource.registration.selectCsourceRegistration", dbConditionVO);
        return csourceRegistrationBaseDaoVOs;
    }


    /**
     * contet source 전체 조회 (Count)
     *
     * @param queryVO
     * @return
     * @throws JsonProcessingException
     */
    @Override
    public Integer queryCsourceRegistrationCount(QueryVO queryVO) {

        DbConditionVO dbConditionVO = setQueryCondition(queryVO);
        Integer totalCount = retrieveSqlSession.selectOne("dataservicebroker.csource.registration.selectCsourceRegistrationCount", dbConditionVO);
        return totalCount;
    }

    /**
     * contet source 전체 조회
     *
     * @param entityId
     * @return
     * @throws JsonProcessingException
     */
    @Override
    public List<CsourceRegistrationBaseDaoVO> queryCsourceRegistrationByEntityId(String entityId) {

        List<CsourceRegistrationBaseDaoVO> csourceRegistrationBaseDaoVOs = retrieveSqlSession.selectList("dataservicebroker.csource.registration.selectCsourceRegistrationByEntityId", entityId);
        return csourceRegistrationBaseDaoVOs;
    }

    @Override
    public Integer deleteCsourceRegistrationBase(String id) {
        return sqlSession.update("dataservicebroker.csource.registration.deleteCsourceRegistrationBase", id);
    }

    @Override
    public Integer deleteCsourceRegistrationInfo(String csourceRegistrationBaseId) {
        return sqlSession.update("dataservicebroker.csource.registration.deleteCsourceRegistrationInfo", csourceRegistrationBaseId);
    }

    @Override
    public Integer deleteCsourceRegistrationEntity(String csourceRegistrationBaseId) {
        return sqlSession.update("dataservicebroker.csource.registration.deleteCsourceRegistrationEntity", csourceRegistrationBaseId);
    }


    @Override
    public Integer updateCsourceRegistrationBase(CsourceRegistrationBaseDaoVO csourceRegistrationBaseDaoVO) {
        return sqlSession.update("dataservicebroker.csource.registration.updateCsourceRegistrationBase", csourceRegistrationBaseDaoVO);
    }
//
//    @Override
//    public Integer updateCsourceRegistrationInfo(List<CsourceRegistrationInfoDaoVO> csourceRegistrationInfoDaoVOs) {
//
//        Map<String, Object> csourceRegistrationInfoMap = new HashMap<String, Object>();
//        csourceRegistrationInfoMap.put("list", csourceRegistrationInfoDaoVOs);
//        return sqlSession.update("dataservicebroker.csource.registration.createCsourceRegistrationInfo", csourceRegistrationInfoMap);
//    }
//
//
//    @Override
//    public Integer updateCsourceRegistrationEntity(List<CsourceRegistrationEntityDaoVO> csourceRegistrationEntityDaoVOs) {
//
//        Map<String, Object> csourceRegistrationEntityMap = new HashMap<String, Object>();
//        csourceRegistrationEntityMap.put("list", csourceRegistrationEntityDaoVOs);
//        return sqlSession.update("dataservicebroker.csource.registration.createCsourceRegistrationEntity", csourceRegistrationEntityMap);
//    }


    /**
     * DB 조회 조건 세팅
     *
     * @param queryVO
     * @return
     */
    private DbConditionVO setQueryCondition(QueryVO queryVO) {

         /*

• A list (one or more) of Attribute names (optional).
• An id pattern as a regular expression (optional).
• An NGSI-LD temporal query (optional) as per clause 4.11.
• An NGSI-LD context source query (optional) as per clause 4.9.
• A limit to the number of Context Source Registrations to be retrieved. See clause 5.5.9. At least one of (a) list of Entity Types or (b) list of Attribute names shall be present.

         */

        DbConditionVO dbConditionVO = new DbConditionVO();

        // id  조건 넣기
        // • A list (one or more) of Entity identifiers (optional).
        if (!ValidateUtil.isEmptyData(queryVO.getId())) {
            List<String> searchIdList = Arrays.asList(queryVO.getId().split(","));
            dbConditionVO.setSearchIdList(searchIdList);
        }

        if (!ValidateUtil.isEmptyData(queryVO.getType())) {
            List<String> typeList = Arrays.asList(queryVO.getType().split(","));
            List<String> typeUriList = dataModelManager.convertAttrNameToFullUri(queryVO.getLinks(), typeList);
            dbConditionVO.setSearchTypeList(typeUriList);
        }

        //A reference to a JSON-LD @context (optional).
        if (queryVO.getContext() != null) {
            dbConditionVO.setContextList(queryVO.getContext());
        }

        //• A list (one or more) of Entity types of the matching entities (optional).

        //1. 조회 대상 컬럼 세팅
        //• A list (one or more) of Attribute names (optional).
        if(queryVO.getAttrs() != null) {
            List<String> attrsUriList = dataModelManager.convertAttrNameToFullUri(queryVO.getLinks(), queryVO.getAttrs());
            dbConditionVO.setWatchAttributeList(attrsUriList);
        }

        // id pattern 조건 넣기
        if (includeValidIdPattern(queryVO.getIdPattern())) {
            dbConditionVO.setIdPattern(queryVO.getIdPattern());
        }

        //2. geo-query param 처리
        // • An NGSI-LD geo-query (optional) as per clause 4.10.
        if (QueryUtil.validateGeoQuery(queryVO) && QueryUtil.includeGeoQuery(queryVO)) {
            dbConditionVO.setGeoCondition(QueryUtil.convertGeoQuery(generateGeoQuery(queryVO)));
        }

        if (queryVO.getLimit() == null) {
            dbConditionVO.setLimit(defaultLimit);
        } else {
            dbConditionVO.setLimit(queryVO.getLimit());
        }
        dbConditionVO.setOffset(queryVO.getOffset());

        //3. 상세쿼리(q-query) param 처리
        if (QueryUtil.includeQQuery(queryVO)) {
            dbConditionVO.setSearchQparamList(extractQparam(queryVO));
        }

        //4. timerel param 처리, 이력 데이터 조회시에만 적용
        if (queryVO.getTimerel() != null) {
            dbConditionVO.setTimerelCondition(convertCsourceRegistrationTimerel(queryVO));
        }
        //• An NGSI-LD query (optional) as per clause 4.9.

        return dbConditionVO;
    }

    private boolean includeValidIdPattern(String idPattern) {
        if (!ValidateUtil.isEmptyData(idPattern)) {
            try {
                Pattern.compile(idPattern);
                return true;
            } catch(PatternSyntaxException e) {
                log.warn("invalid RegEx expression. idPattern={}", idPattern);
                throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "invalid RegEx expression");
            }
        }
        return false;
    }

    private List<String> extractQparam(QueryVO queryVO) {
        // 1. q-query 파라미터에서 field name 만 추출
        List<String> qQueryPropertyNames = QueryUtil.extractQueryFieldNames(queryVO);

        // 2. q-query에서 추출한 파라미터가 full uri 형태가 아닐 경우 context 정보를 통해 full uri 정보로 변환
        return dataModelManager.convertAttrNameToFullUri(queryVO.getLinks(), qQueryPropertyNames);
    }


    /**
     * location 정보 -> postgis geometry Value(text)로 변환
     *
     * @param queryVO
     * @return
     */
    private QueryVO generateGeoQuery(QueryVO queryVO) {
        /*
            georel = nearRel / withinRel / containsRel / overlapsRel / intersectsRel / equalsRel / disjointRel
            nearRel = nearOp andOp distance equal PositiveNumber distance = "maxDistance" / "minDistance"
            nearOp = "near"
            withinRel = "within"
            containsRel = "contains"
            intersectsRel = "intersects"
            equalsRel = "equals"
            disjointRel = "disjoint"
            overlapsRel = "overlaps"
            ; near;max(min)Distance==x (in meters)
         */

        if (QueryUtil.includeGeoQuery(queryVO)) {

            try {
                String georelFullTxt = queryVO.getGeorel();
                String georelName = georelFullTxt.split(";")[0];

                GeometryType geometryType = GeometryType.parseType(georelName);
                if (geometryType == null) {
                    log.warn("invalid geo-query parameter");
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "invalid geo-query parameter");
                }

                queryVO.setGeorelType(geometryType);

                if (geometryType == GeometryType.NEAR_REL) {
                    String distanceText = georelFullTxt.split(";")[1];
                    String distanceColName = distanceText.split("==")[0];
                    int distance = Integer.parseInt(distanceText.split("==")[1]);

                    if (distanceColName.equals(GeometryType.MIN_DISTANCE.getCode())) {
                        queryVO.setMinDistance(distance);
                    } else if (distanceColName.equals(GeometryType.MAX_DISTANCE.getCode())) {
                        queryVO.setMaxDistance(distance);
                    } else {
                        throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "invalid geo-query parameter");
                    }
                }

            } catch (Exception e) {
                throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "invalid geo-query parameter", e);
            }

            CsourceGeoProperty searchGeoProperty = null;
            if (queryVO.getGeoproperty() != null) {
                searchGeoProperty = CsourceGeoProperty.parseType(queryVO.getGeoproperty());

                if(searchGeoProperty == null) {
                    throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "invalid geo-query parameter. geoproperty=" + queryVO.getGeoproperty());
                }

            } else {
                searchGeoProperty = CsourceGeoProperty.LOCATION;
            }

            int srid = Integer.parseInt(defaultEPSG);
            PGgeometry pGgeometry = makePostgisType(queryVO.getGeometry(), queryVO.getCoordinates(), srid);

            String pGgeometryValue = pGgeometry.getValue();

            queryVO.setGeometryValue(pGgeometryValue);
            queryVO.setLocationCol(searchGeoProperty.getColumnName());

            return queryVO;

        } else {
            log.warn("invalid geo-query parameter");
            throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "invalid geo-query parameter");
        }
    }

    /**
     * Geo-Query시, 기준이 되는 Geo Column명을 가져옴
     * <p>
     * 세팅 조건
     * 우선 순위 1. application-properties내 정보와 일치하는 컬럼이 있을 경우
     * 2. 1번의 케이스가 x 이고, type내 RootAttribute 중 GEO_PROPERTY 중 첫번째로 검색되는 컬럼
     *
     * @param dataModelCacheVO
     * @return
     */
    private String getDefaultLocationColName(DataModelCacheVO dataModelCacheVO) {

        String locationColName = null;

        Attribute locationAttr = dataModelCacheVO.getRootAttribute(defaultLocationAttrName);

        if (locationAttr != null) {
        	locationColName = locationAttr.getName() + Constants.GEO_PREFIX_4326;

        } else {
            for (Attribute rootAttribute : dataModelCacheVO.getDataModelVO().getAttributes()) {
                if (rootAttribute.getAttributeType() == AttributeType.GEO_PROPERTY) {
                    locationColName = rootAttribute.getName() + Constants.GEO_PREFIX_4326;
                    break;
                }
            }
        }
        return locationColName;
    }


    /**
     * geo-query 관련 정보를 postGiS type으로 변환 ( Point, Polygon, LineString, MultiLineString, MultiPolygon)
     *
     * @param geometry
     * @param coordinates
     * @param srid
     * @return
     */
    private PGgeometry makePostgisType(String geometry, String coordinates, int srid) {

        log.debug("geometry : '{}', coordinates : {}, srid : {}", geometry, coordinates, srid);

        try {
            String convertedCoordinates = convertCoordinatesToPGisStr(coordinates);

            PGgeometry pGgeometry = new PGgeometry();

            if (geometry.equalsIgnoreCase("Point")) {

//             {"type": "Point", "coordinates": [100.0, 0.0]}

                Point point = new Point(convertedCoordinates);
                point.setSrid(srid);
                pGgeometry.setGeometry(point);

            } else if (geometry.equalsIgnoreCase("Polygon")) {

            /*
            	"geometry": {
            		"type": "Polygon",
            		"coordinates": [
            			[
            				[100.0, 0.0],
            			 	[101.0, 0.0],
            			  	[101.0, 1.0],
            			   	[100.0, 1.0],
            			    [100.0, 0.0]
            			 ]
            		]
            	}
            */

                Polygon polygon = new Polygon(convertedCoordinates);
                polygon.setSrid(srid);
                pGgeometry.setGeometry(polygon);

            } else if (geometry.equalsIgnoreCase("LineString")) {

            /*
                "type": "Feature",
                "geometry": {
                   "type": "LineString",
                   "coordinates": [
                       [102.0, 0.0],
                       [103.0, 1.0],
                       [104.0, 0.0],
                       [105.0, 1.0]
                   ]
                }
            */

                LineString lineString = new LineString(convertedCoordinates);
                lineString.setSrid(srid);
                pGgeometry.setGeometry(lineString);


            } else if (geometry.equalsIgnoreCase("MultiLineString")) {

            /*
                {
                   "type": "MultiLineString",
                   "coordinates": [
                       [
                           [170.0, 45.0], [180.0, 45.0]
                       ], [
                           [-180.0, 45.0], [-170.0, 45.0]
                       ]
                   ]
                }
            */

                MultiLineString multiLineString = new MultiLineString(convertedCoordinates);
                multiLineString.setSrid(srid);
                pGgeometry.setGeometry(multiLineString);

            } else if (geometry.equalsIgnoreCase("MultiPolygon")) {

            /*
                {
                   "type": "MultiPolygon",
                   "coordinates": [
                       [
                           [
                               [180.0, 40.0], [180.0, 50.0], [170.0, 50.0],
                               [170.0, 40.0], [180.0, 40.0]
                           ]
                       ],
                       [
                           [
                               [-170.0, 40.0], [-170.0, 50.0], [-180.0, 50.0],
                               [-180.0, 40.0], [-170.0, 40.0]
                           ]
                       ]
                   ]
               }
            */

                MultiPolygon multiPolygon = new MultiPolygon(convertedCoordinates);
                multiPolygon.setSrid(srid);
                pGgeometry.setGeometry(multiPolygon);

            }

            log.debug("pGgeometry : '{}'", pGgeometry.toString());
            return pGgeometry;

        } catch (EmptyStackException et) {
            log.warn("invalid coordinates : " + et.getMessage());
            throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "invalid coordinates", et);
        } catch (SQLException se) {
            log.warn("invalid coordinates : " + se.getMessage());
            throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "invalid coordinates", se);
        } catch (Exception se) {
            log.warn("invalid coordinates : " + se.getMessage());
            throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "invalid coordinates", se);
        }
    }


    /**
     * coordinates (IETF RFC7946[8] -> postgis)
     *
     * @param coordinates
     * @return
     */
    private String convertCoordinatesToPGisStr(String coordinates) {

        /*
            coordinates = [[[180.0, 40.0], [180.0, 50.0]]]  , IETF RFC7946[8]
            coordinates = (((180.0 40.0]) (180.0, 50.0)))   , postgis
        */
        try {
            coordinates = coordinates.replace(" ", "");
            coordinates = coordinates.replace("[", "(");
            coordinates = coordinates.replace("]", ")");

            char[] arr = coordinates.toCharArray();
            char[] changeArr = new char[arr.length];

            for (int j = 0; j < arr.length; j++) {
                char comma = ',';
                char ch = arr[j];
                if (j > 1) {
                    if (ch == comma && !(arr[j - 1] == '(' || arr[j - 1] == ')')) {
                        changeArr[j] = ' ';
                    } else {
                        changeArr[j] = arr[j];
                    }
                } else {
                    changeArr[j] = arr[j];
                }
            }

            String changedCoordinates = new String(changeArr);
            log.debug("changed coordinates : '{}'", changedCoordinates);

            return changedCoordinates;

        } catch (Exception e) {
            throw new NgsiLdBadRequestException(ErrorCode.INVALID_PARAMETER, "invalid coordinates", e);
        }
    }


    /**
     * timerel 요청에 대해 query 생성
     *
     * @param queryVO
     * @return
     */
    public String convertCsourceRegistrationTimerel(QueryVO queryVO) {


        String timerel = queryVO.getTimerel();
        String time = null;
        if (queryVO.getTimeAt() != null) {
            time = ConvertTimeParamUtil.dateTimeToLocalDateTime(queryVO.getTimeAt());
        }

        String endTime = null;
        if (queryVO.getEndTimeAt() != null) {
            endTime = ConvertTimeParamUtil.dateTimeToLocalDateTime(queryVO.getEndTimeAt());
        }

        ConvertTimeParamUtil.checkTimeRelParams(timerel, time, endTime);

        String colName = timerelToColName(queryVO.getTimerel());


        if (timerel.equalsIgnoreCase(TemporalOperator.BETWEEN_REL.getCode())) {
            return makeCsourceRegistrationFragmentBetweenTimeQuery(timerel, colName, time, endTime);
        } else {
            return makeCsourceRegistrationFragmentTimeQuery(timerel, colName, time);
        }
    }


    /**
     * timerel (AFTER, BEFORE ) 조건일 경우, query 생성
     *
     * @param timerel
     * @param colName
     * @param timeStr
     * @return
     */
    private String makeCsourceRegistrationFragmentTimeQuery(String timerel, String colName, String timeStr) {

        StringBuilder timeQuery = new StringBuilder();
        if (timerel.equalsIgnoreCase(TemporalOperator.AFTER_REL.getCode())) {
            timeQuery.append(colName + "_START");
            timeQuery.append(" ");
            timeQuery.append(" > ");
            timeQuery.append("'" + timeStr + "'");
        } else if (timerel.equalsIgnoreCase(TemporalOperator.BEFORE_REL.getCode())) {
            timeQuery.append(colName + "_START");
            timeQuery.append(" ");
            timeQuery.append(" < ");
            timeQuery.append("'" + timeStr + "'");
        }

        log.debug("timeQuery : '{}'", timeQuery);

        return timeQuery.toString();
    }

    /**
     * timerel (Between) 조건일 경우, query 생성
     *
     * @param timerel
     * @param colName
     * @param timeStr
     * @param endTimeStr
     * @return
     */
    private String makeCsourceRegistrationFragmentBetweenTimeQuery(String timerel, String colName, String timeStr, String endTimeStr) {
        StringBuilder timeQuery = new StringBuilder();
        timeQuery.append("(");
        timeQuery.append(colName + "_START");
        timeQuery.append(" > ");
        timeQuery.append("'" + timeStr + "'");
        timeQuery.append(" AND ");

        timeQuery.append(colName + "_END");
        timeQuery.append(" < ");
        timeQuery.append("'" + endTimeStr + "'");
        timeQuery.append(")");


        return timeQuery.toString();

    }


    private String timerelToColName(String timerel) {

        if (timerel == null) {
            return "OBSERVATION_INTERVAL";
        }

        if (timerel.equalsIgnoreCase("managementInterval")) {
            return "MANAGEMENT_INTERVAL";
        } else {
            return "OBSERVATION_INTERVAL";
        }
    }


    private String locationToColName(String georel) {
        if (georel == null) {
            return "LOCATION";
        }

        if (georel.equalsIgnoreCase("observationSpace")) {
            return "OBSERVATION_SPACE";
        } else if (georel.equalsIgnoreCase("operationSpace")) {
            return "OPERATION_SPACE";
        } else {
            return "LOCATION";
        }
    }
}
