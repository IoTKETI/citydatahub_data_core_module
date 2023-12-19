package kr.re.keti.sc.dataservicebroker.csource.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.AttributeValueType;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdResourceNotFoundException;
import kr.re.keti.sc.dataservicebroker.common.vo.GeoPropertyVO;
import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import kr.re.keti.sc.dataservicebroker.common.vo.TimeIntervalVO;
import kr.re.keti.sc.dataservicebroker.csource.dao.CsourceRegistrationDAO;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationBaseDaoVO;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationInfoDaoVO;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationVO;
import kr.re.keti.sc.dataservicebroker.csource.vo.CsourceRegistrationVO.EntityInfo;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.postgis.PGgeometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class CsourceRegistrationSVC {
	
    @Autowired
    private CsourceRegistrationDAO csourceRegistrationDAO;
    @Autowired
    private DataModelManager dataModelManager;
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 구독 입력값 validation 체크
     *
     * @param csourceRegistrationVO
     * @param isCreateMode
     */
    private void processInputValidationCheck(CsourceRegistrationVO csourceRegistrationVO, boolean isCreateMode) {


        if (isCreateMode) {
            if (csourceRegistrationVO.getType() == null) {
                throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include type");
            }

            if (csourceRegistrationVO.getInformation() == null) {
                throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include information");
            }

            if (csourceRegistrationVO.getEndpoint() == null) {
                throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include Endpoint");
            }

            // If the data types and restrictions expressed by clause 5.2.9 are not met by the Context Source Registration, then an error of type BadRequestData shall be raised.
            if (!csourceRegistrationVO.getType().equalsIgnoreCase(DataServiceBrokerCode.JsonLdType.CSOURCE_REGISTRATION.getCode())) {
                throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include valid type");
            }


        } else {
            if (csourceRegistrationVO.getType() != null && !csourceRegistrationVO.getType().equalsIgnoreCase(DataServiceBrokerCode.JsonLdType.CSOURCE_REGISTRATION.getCode())) {
                throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include valid type");
            }

            //Any attempt to remove (by setting them to null in the Fragment) mandatory properties of a Context Source
            //Registration (clause 5.2.9) shall result in an error of type BadRequestData.
            //필수 조건 널 요청 시, 에러처리
            // type, informationn, endpoint
        }
        //공통
        if (csourceRegistrationVO.getId() == null) {
            //ID가 없는 경우
            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "should include id");
        }

        if (csourceRegistrationVO.getExpiresAt() != null) {
            Date now = new Date();

            if ((csourceRegistrationVO.getExpiresAt().getTime() < now.getTime()) || (csourceRegistrationVO.getExpiresAt().getTime() < now.getTime())) {
                //• If expires is a date and time in the past, an error of type BadRequestData shall be raised.
                throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "expires has passed.");
            }
        }

        // information 및 location 필드 유효성 체크
        validateParameterByContext(csourceRegistrationVO);

    }

    /**
     * csourceRegistration 요청
     *
     * @param csourceRegistrationVO
     * @return
     * @throws Exception
     */
    @Transactional(value = "dataSourceTransactionManager")
    public Integer createCsourceRegistration(CsourceRegistrationVO csourceRegistrationVO) throws Exception {

        // 1. csourceRegistrations id가 없는 경우 생성
        if (ValidateUtil.isEmptyData(csourceRegistrationVO.getId())) {
            csourceRegistrationVO.setId(makeRandomCsourceRegistrationId());
        }

        // 2. 파라미터 유효성 체크
        processInputValidationCheck(csourceRegistrationVO, true);

        // 3. csource 정보 생성
        CsourceRegistrationBaseDaoVO csourceRegistrationBaseDaoVO = csourceRegistrationVoToDaoVO(csourceRegistrationVO);
        List<CsourceRegistrationInfoDaoVO> csourceRegistrationInfoDaoVOs = csourceRegistrationBaseDaoVO.getInformation();

        List<CsourceRegistrationEntityDaoVO> allEntities = new ArrayList<>();
        for (CsourceRegistrationInfoDaoVO csourceRegistrationInfoDaoVO : csourceRegistrationInfoDaoVOs) {
            List<CsourceRegistrationEntityDaoVO> entities = csourceRegistrationInfoDaoVO.getEntities();
            if(entities != null) {
                allEntities.addAll(entities);
            }
        }

        // Base 등록
        Integer resultCreateCsourceRegistrationBase = csourceRegistrationDAO.createCsourceRegistrationBase(csourceRegistrationBaseDaoVO);

        // Info 등록
        Integer resultCreateCsourceRegistrationInfo = csourceRegistrationDAO.createCsourceRegistrationInfo(csourceRegistrationInfoDaoVOs);

        if(!ValidateUtil.isEmptyData(allEntities)) {
            // Entity 등록
            Integer resultCreateCsourceRegistrationEntity = csourceRegistrationDAO.createCsourceRegistrationEntity(allEntities);
        }

        return resultCreateCsourceRegistrationBase;
    }


    /**
     * csource Update 요청
     *
     * @param csourceRegistrationVO
     * @return
     * @throws Exception
     */
    @Transactional(value = "dataSourceTransactionManager")
    public void updateCsourceRegistration(CsourceRegistrationVO csourceRegistrationVO) throws Exception {
        // 체크
        processInputValidationCheck(csourceRegistrationVO, false);

        CsourceRegistrationBaseDaoVO csourceRegistrationBaseDaoVO = csourceRegistrationVoToDaoVO(csourceRegistrationVO);

        if (csourceRegistrationVO.getInformation() != null) {
            List<CsourceRegistrationEntityDaoVO> allEntities = new ArrayList<>();

            List<CsourceRegistrationInfoDaoVO> csourceRegistrationInfoDaoVOs = csourceRegistrationBaseDaoVO.getInformation();
            for (CsourceRegistrationInfoDaoVO csourceRegistrationInfoDaoVO : csourceRegistrationInfoDaoVOs) {
                List<CsourceRegistrationEntityDaoVO> entities = csourceRegistrationInfoDaoVO.getEntities();
                allEntities.addAll(entities);
            }

            String csourceRegistrationBaseId = csourceRegistrationVO.getId();

            // Entity 삭제
            Integer resultDeleteCsourceRegistrationEntity = csourceRegistrationDAO.deleteCsourceRegistrationEntity(csourceRegistrationBaseId);
            // Info 삭제
            Integer resultDeleteCsourceRegistrationInfo = csourceRegistrationDAO.deleteCsourceRegistrationInfo(csourceRegistrationBaseId);

            // Info 등록
            Integer resultCreateCsourceRegistrationInfo = csourceRegistrationDAO.createCsourceRegistrationInfo(csourceRegistrationInfoDaoVOs);
            if(!ValidateUtil.isEmptyData(allEntities)) {
                // Entity 등록
                Integer resultCreateCsourceRegistrationEntity = csourceRegistrationDAO.createCsourceRegistrationEntity(allEntities);
            }
        }

        // Base 업데이트
        Integer resultCreateCsourceRegistrationBase = csourceRegistrationDAO.updateCsourceRegistrationBase(csourceRegistrationBaseDaoVO);
    }

    /**
     * 삭제
     *
     * @param csourceRegistrationBaseId
     * @throws Exception
     */
    @Transactional(value = "dataSourceTransactionManager")
    public void deleteCsourceRegistration(String csourceRegistrationBaseId) throws Exception {

        //entity 삭제
        Integer resultDeleteCsourceRegistrationEntity = csourceRegistrationDAO.deleteCsourceRegistrationEntity(csourceRegistrationBaseId);
        //info 삭제
        Integer resultDeleteCsourceRegistrationInfo = csourceRegistrationDAO.deleteCsourceRegistrationInfo(csourceRegistrationBaseId);
        //base 등록
        Integer resultDeleteCsourceRegistrationBase = csourceRegistrationDAO.deleteCsourceRegistrationBase(csourceRegistrationBaseId);
    }


    /**
     * 개별 조회
     *
     * @param registrationId
     * @return
     * @throws JsonProcessingException
     */
    public CsourceRegistrationVO retrieveCsourceRegistration(String registrationId) throws JsonProcessingException {

        List<CsourceRegistrationBaseDaoVO> csourceRegistrationBaseDaoVOs = csourceRegistrationDAO.retrieveCsourceRegistration(registrationId);

        if (csourceRegistrationBaseDaoVOs == null || csourceRegistrationBaseDaoVOs.size() == 0) {
            // 2. 조회된 CsourceRegistration 없을 경우, ResourceNotFound 처리
            throw new NgsiLdResourceNotFoundException(DataServiceBrokerCode.ErrorCode.NOT_EXIST_ID, "There is no an existing registration which id");
        }

        return csourceRegistrationsDaoToVo(csourceRegistrationBaseDaoVOs.get(0));

    }

    /**
     * 전체 조회
     *
     * @param queryVO
     * @return
     * @throws JsonProcessingException
     */
    public List<CsourceRegistrationVO> queryCsourceRegistrations(QueryVO queryVO) {

        List<CsourceRegistrationBaseDaoVO> csourceRegistrationBaseDaoVOs = csourceRegistrationDAO.queryCsourceRegistration(queryVO);

        List<CsourceRegistrationVO> csourceRegistrationVOS = new ArrayList<>();

        for (CsourceRegistrationBaseDaoVO csourceRegistrationBaseDaoVO : csourceRegistrationBaseDaoVOs) {
            csourceRegistrationVOS.add(csourceRegistrationsDaoToVo(csourceRegistrationBaseDaoVO));
        }
        return csourceRegistrationVOS;

    }


    /**
     * 전체 조회 (Count)
     *
     * @param queryVO
     * @return
     * @throws JsonProcessingException
     */
    public Integer queryCsourceRegistrationsCount(QueryVO queryVO) throws JsonProcessingException {

        Integer totalCount = csourceRegistrationDAO.queryCsourceRegistrationCount(queryVO);
        return totalCount;

    }

    /**
     * 개별 조회
     *
     * @param entityId
     * @return
     * @throws JsonProcessingException
     */
    public List<CsourceRegistrationVO> queryCsourceRegistrationsByEntityId(String entityId) {

        List<CsourceRegistrationBaseDaoVO> csourceRegistrationBaseDaoVOs = csourceRegistrationDAO.queryCsourceRegistrationByEntityId(entityId);

        List<CsourceRegistrationVO> csourceRegistrationVOS = new ArrayList<>();

        for (CsourceRegistrationBaseDaoVO csourceRegistrationBaseDaoVO : csourceRegistrationBaseDaoVOs) {
            csourceRegistrationVOS.add(csourceRegistrationsDaoToVo(csourceRegistrationBaseDaoVO));
        }
        return csourceRegistrationVOS;

    }


    /**
     * API VO to DAO VO
     *
     * @param csourceRegistrationVO
     * @return CsourceRegistrationBaseDaoVO
     * @throws JsonProcessingException
     */
    private CsourceRegistrationBaseDaoVO csourceRegistrationVoToDaoVO(CsourceRegistrationVO csourceRegistrationVO) throws JsonProcessingException {

        CsourceRegistrationBaseDaoVO csourceRegistrationBaseDaoVO = new CsourceRegistrationBaseDaoVO();
        csourceRegistrationBaseDaoVO.setId(csourceRegistrationVO.getId());
        csourceRegistrationBaseDaoVO.setContext(csourceRegistrationVO.getContext());
        csourceRegistrationBaseDaoVO.setName(csourceRegistrationVO.getName());
        csourceRegistrationBaseDaoVO.setDescription(csourceRegistrationVO.getDescription());
        csourceRegistrationBaseDaoVO.setExpires(csourceRegistrationVO.getExpiresAt());
        csourceRegistrationBaseDaoVO.setEndpoint(csourceRegistrationVO.getEndpoint());
        
        if(csourceRegistrationVO.getSupportedAggregationMethod() != null && csourceRegistrationVO.getSupportedAggregationMethod().size() > 0) {
        	csourceRegistrationBaseDaoVO.setSupportedAggregationMethod(csourceRegistrationVO.getSupportedAggregationMethod());
        }
        
        if(csourceRegistrationVO.getScope() instanceof String) {
        	csourceRegistrationBaseDaoVO.setScope(new ArrayList<>(Arrays.asList((String)csourceRegistrationVO.getScope())));
        	csourceRegistrationBaseDaoVO.setScopeDataType(AttributeValueType.STRING);
        } else if(csourceRegistrationVO.getScope() instanceof List) {
        	csourceRegistrationBaseDaoVO.setScope((List<String>)csourceRegistrationVO.getScope());
        	csourceRegistrationBaseDaoVO.setScopeDataType(AttributeValueType.ARRAY_STRING);
        }

        List<CsourceRegistrationVO.Information> informations = csourceRegistrationVO.getInformation();
        for (CsourceRegistrationVO.Information information : informations) {
        	
        	if(csourceRegistrationBaseDaoVO.getInformation() == null) {
        		csourceRegistrationBaseDaoVO.setInformation(new ArrayList<>());
        	}
        	
        	CsourceRegistrationInfoDaoVO csourceRegistrationInfoDaoVO = new CsourceRegistrationInfoDaoVO();
            csourceRegistrationInfoDaoVO.setCsourceRegistrationBaseId(csourceRegistrationVO.getId());
            String registrationInfoId = UUID.randomUUID().toString().replace("-", "");
            csourceRegistrationInfoDaoVO.setCsourceRegistrationInfoId(registrationInfoId);

            if(information.getPropertyNames() != null && information.getPropertyNames().size() > 0) {
            	csourceRegistrationInfoDaoVO.setProperties(information.getPropertyNames());
            }

            if(information.getRelationshipNames() != null && information.getRelationshipNames().size() > 0) {
            	csourceRegistrationInfoDaoVO.setRelationships(information.getRelationshipNames());
            }

            List<CsourceRegistrationVO.EntityInfo> entities = information.getEntities();
            if(entities != null && entities.size() > 0) {
            	csourceRegistrationInfoDaoVO.setEntities(new ArrayList<>());
            	for (CsourceRegistrationVO.EntityInfo entityInfo : entities) {
                	CsourceRegistrationEntityDaoVO csourceRegistrationEntityDaoVO = new CsourceRegistrationEntityDaoVO();
                    csourceRegistrationEntityDaoVO.setCsourceRegistrationBaseId(csourceRegistrationVO.getId());
                    csourceRegistrationEntityDaoVO.setCsourceRegistrationInfoId(registrationInfoId);
                    csourceRegistrationEntityDaoVO.setEntityId(entityInfo.getId());
                    csourceRegistrationEntityDaoVO.setEntityIdPattern(entityInfo.getIdPattern());
                    csourceRegistrationEntityDaoVO.setEntityType(entityInfo.getType());
                    csourceRegistrationInfoDaoVO.getEntities().add(csourceRegistrationEntityDaoVO);
                    
                }
            }
            csourceRegistrationBaseDaoVO.getInformation().add(csourceRegistrationInfoDaoVO);
        }
        
        
        if (csourceRegistrationVO.getLocation() != null) {
            String location = objectMapper.writeValueAsString(csourceRegistrationVO.getLocation().getValue());
            csourceRegistrationBaseDaoVO.setLocation(location);
        }

        if (csourceRegistrationVO.getObservationSpace() != null) {

            String observationSpace = objectMapper.writeValueAsString(csourceRegistrationVO.getObservationSpace().getValue());
            csourceRegistrationBaseDaoVO.setObservationSpace(observationSpace);
        }

        if (csourceRegistrationVO.getOperationSpace() != null) {
            String operationSpace = objectMapper.writeValueAsString(csourceRegistrationVO.getOperationSpace().getValue());
            csourceRegistrationBaseDaoVO.setOperationSpace(operationSpace);
        }

        if (csourceRegistrationVO.getManagementInterval() != null) {
            csourceRegistrationBaseDaoVO.setManagementIntervalStart(csourceRegistrationVO.getManagementInterval().getStartAt());
            csourceRegistrationBaseDaoVO.setManagementIntervalEnd(csourceRegistrationVO.getManagementInterval().getEndAt());
        }

        if (csourceRegistrationVO.getObservationInterval() != null) {
            csourceRegistrationBaseDaoVO.setObservationIntervalStart(csourceRegistrationVO.getObservationInterval().getStartAt());
            csourceRegistrationBaseDaoVO.setObservationIntervalEnd(csourceRegistrationVO.getObservationInterval().getEndAt());
        }

        return csourceRegistrationBaseDaoVO;
    }


    /**
     * DAO => VO
     *
     * @param csourceRegistrationBaseDaoVO
     * @return
     */
    private CsourceRegistrationVO csourceRegistrationsDaoToVo(CsourceRegistrationBaseDaoVO csourceRegistrationBaseDaoVO) {

        CsourceRegistrationVO csourceRegistrationVO = new CsourceRegistrationVO();
        csourceRegistrationVO.setContext(csourceRegistrationBaseDaoVO.getContext());
        csourceRegistrationVO.setId(csourceRegistrationBaseDaoVO.getId());
        csourceRegistrationVO.setName(csourceRegistrationBaseDaoVO.getName());
        csourceRegistrationVO.setDescription(csourceRegistrationBaseDaoVO.getDescription());
        csourceRegistrationVO.setExpiresAt(csourceRegistrationBaseDaoVO.getExpires());
        csourceRegistrationVO.setEndpoint(csourceRegistrationBaseDaoVO.getEndpoint());
        csourceRegistrationVO.setSupportedAggregationMethod(csourceRegistrationBaseDaoVO.getSupportedAggregationMethod());

        if(csourceRegistrationBaseDaoVO.getScope() != null && csourceRegistrationBaseDaoVO.getScope().size() > 0) {
        	if(csourceRegistrationBaseDaoVO.getScopeDataType() == AttributeValueType.ARRAY_STRING) {
        		csourceRegistrationVO.setScope(csourceRegistrationBaseDaoVO.getScope());
        	} else {
        		csourceRegistrationVO.setScope(csourceRegistrationBaseDaoVO.getScope().get(0));
        	}
        }

        if(csourceRegistrationBaseDaoVO.getInformation() != null) {
        	csourceRegistrationVO.setInformation(new ArrayList<>());
        	for(CsourceRegistrationInfoDaoVO csourceRegistrationInfoDaoVO : csourceRegistrationBaseDaoVO.getInformation()) {
        		
        		CsourceRegistrationVO.Information information = new CsourceRegistrationVO.Information();
        		information.setPropertyNames(csourceRegistrationInfoDaoVO.getProperties());
        		information.setRelationshipNames(csourceRegistrationInfoDaoVO.getRelationships());
        		
        		if(csourceRegistrationInfoDaoVO.getEntities() != null) {
        			information.setEntities(new ArrayList<>());
        			for(CsourceRegistrationEntityDaoVO csourceRegistrationEntityDaoVO : csourceRegistrationInfoDaoVO.getEntities()) {
        				EntityInfo entityInfo = new EntityInfo();
        				entityInfo.setId(csourceRegistrationEntityDaoVO.getEntityId());
        				entityInfo.setIdPattern(csourceRegistrationEntityDaoVO.getEntityIdPattern());
        				entityInfo.setType(csourceRegistrationEntityDaoVO.getEntityType());
        				information.getEntities().add(entityInfo);
        			}
        		}
        		
        		csourceRegistrationVO.getInformation().add(information);
        	}
        }
        
        if(csourceRegistrationBaseDaoVO.getManagementIntervalStart() != null || csourceRegistrationBaseDaoVO.getManagementIntervalEnd() != null) {
        	TimeIntervalVO managementInterval = new TimeIntervalVO(csourceRegistrationBaseDaoVO.getManagementIntervalStart(), csourceRegistrationBaseDaoVO.getManagementIntervalEnd());
            csourceRegistrationVO.setManagementInterval(managementInterval);
        }
        
        if(csourceRegistrationBaseDaoVO.getObservationIntervalStart() != null || csourceRegistrationBaseDaoVO.getObservationIntervalEnd() != null) {
        	TimeIntervalVO observationInterval = new TimeIntervalVO(csourceRegistrationBaseDaoVO.getObservationIntervalStart(), csourceRegistrationBaseDaoVO.getObservationIntervalEnd());
            csourceRegistrationVO.setObservationInterval(observationInterval);
        }

        if (csourceRegistrationBaseDaoVO.getLocation() != null) {
            PGgeometry pGgeometry = (PGgeometry) csourceRegistrationBaseDaoVO.getLocation();
            if(pGgeometry != null) {
            	GeoPropertyVO geoPropertyVO = new GeoPropertyVO();
                geoPropertyVO.setValue(pGgeometry.getGeometry());
                csourceRegistrationVO.setLocation(geoPropertyVO);
            }
        }

        if (csourceRegistrationBaseDaoVO.getOperationSpace() != null) {
            PGgeometry pGgeometry = (PGgeometry) csourceRegistrationBaseDaoVO.getOperationSpace();
            if(pGgeometry != null) {
            	GeoPropertyVO geoPropertyVO = new GeoPropertyVO();
                geoPropertyVO.setValue(pGgeometry.getGeometry());
                csourceRegistrationVO.setOperationSpace(geoPropertyVO);
            }
        }

        if (csourceRegistrationBaseDaoVO.getObservationSpace() != null) {
            PGgeometry pGgeometry = (PGgeometry) csourceRegistrationBaseDaoVO.getObservationSpace();
            if(pGgeometry != null) {
            	GeoPropertyVO geoPropertyVO = new GeoPropertyVO();
                geoPropertyVO.setValue(pGgeometry.getGeometry());
                csourceRegistrationVO.setObservationSpace(geoPropertyVO);
            }
        }

        return csourceRegistrationVO;
    }


    /**
     * 5.2.9-1
     * context registration 시, id 없을 경우, 자동 생성
     * rovided, it will be assigned durinAt creation time, If it is not provided,
     * it will be assigned during registration process and returned to client.
     * It cannot be later modified in update operations
     *
     * @return
     */
    private String makeRandomCsourceRegistrationId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String id = Constants.PREFIX_CSOURCE_REGISTRATION_ID + uuid.substring(0, 10);

        return id;
    }


    private void validateParameterByContext(CsourceRegistrationVO csourceRegistrationVO) {
        Map<String, String> contextMap = null;
        if(!ValidateUtil.isEmptyData(csourceRegistrationVO.getContext())) {
            contextMap = dataModelManager.contextToFlatMap(csourceRegistrationVO.getContext());
        }

        if(!ValidateUtil.isEmptyData(csourceRegistrationVO.getInformation())) {
            for(CsourceRegistrationVO.Information information : csourceRegistrationVO.getInformation()) {
                // validate entity type
                if(!ValidateUtil.isEmptyData(information.getEntities())) {
                    for(CsourceRegistrationVO.EntityInfo entityInfo : information.getEntities()) {
                        if(!ValidateUtil.isEmptyData(entityInfo.getType())) {
                            // entityType이 full uri 인 경우 유효성 검증 skip
                            if(entityInfo.getType().startsWith("http")) {
                                continue;
                            }
                            // entityType이 short name 인 경우 context 정보에 존재하는 지 유효성 검증
                            if(contextMap == null || !contextMap.containsKey(entityInfo.getType())) {
                                throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                                        "Invalid Parameter. Not exists entityType in context. entityType=" + entityInfo.getType());
                            }
                        }
                    }
                }
                // validate propertyName
                if(!ValidateUtil.isEmptyData(information.getPropertyNames())) {
                    for(String propertyName : information.getPropertyNames()) {
                        // propertyName이 full uri 인 경우 유효성 검증 skip
                        if(propertyName.startsWith("http")) {
                            continue;
                        }
                        // propertyName이 short name 인 경우 context 정보에 존재하는 지 유효성 검증
                        if(contextMap == null || !contextMap.containsKey(propertyName)) {
                            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                                    "Invalid Parameter. Not exists propertyName in context. propertyName=" + propertyName);
                        }
                    }
                }
                // validate relationshipName
                if(!ValidateUtil.isEmptyData(information.getRelationshipNames())) {
                    for(String relationshipName : information.getRelationshipNames()) {
                        // relationshipName이 full uri 인 경우 유효성 검증 skip
                        if(relationshipName.startsWith("http")) {
                            continue;
                        }
                        // relationshipName이 short name 인 경우 context 정보에 존재하는 지 유효성 검증
                        if(contextMap == null || !contextMap.containsKey(relationshipName)) {
                            throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER,
                                    "Invalid Parameter. Not exists relationshipName in context. relationshipName=" + relationshipName);
                        }
                    }
                }
            }
        }
    }
}