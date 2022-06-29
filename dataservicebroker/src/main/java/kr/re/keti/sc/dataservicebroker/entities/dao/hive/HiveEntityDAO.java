package kr.re.keti.sc.dataservicebroker.entities.dao.hive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.UncategorizedSQLException;

import kr.re.keti.sc.dataservicebroker.common.code.Constants;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.BigDataStorageType;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.ErrorCode;
import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode.Operation;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdBadRequestException;
import kr.re.keti.sc.dataservicebroker.common.exception.ngsild.NgsiLdResourceNotFoundException;
import kr.re.keti.sc.dataservicebroker.common.vo.CommonEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.common.vo.DbConditionVO;
import kr.re.keti.sc.dataservicebroker.common.vo.EntityAttrDaoVO;
import kr.re.keti.sc.dataservicebroker.common.vo.ProcessResultVO;
import kr.re.keti.sc.dataservicebroker.common.vo.QueryVO;
import kr.re.keti.sc.dataservicebroker.common.vo.entities.DynamicEntityDaoVO;
import kr.re.keti.sc.dataservicebroker.datamodel.DataModelManager;
import kr.re.keti.sc.dataservicebroker.datamodel.service.hive.HiveTableSVC;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.Attribute;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelDbColumnVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.DataModelStorageMetadataVO;
import kr.re.keti.sc.dataservicebroker.datamodel.vo.ObjectMember;
import kr.re.keti.sc.dataservicebroker.datasetflow.vo.DatasetFlowBaseVO;
import kr.re.keti.sc.dataservicebroker.entities.dao.EntityDAOInterface;
import kr.re.keti.sc.dataservicebroker.entities.sqlprovider.hive.HiveEntitySqlProvider;
import kr.re.keti.sc.dataservicebroker.util.QueryUtil;
import kr.re.keti.sc.dataservicebroker.util.StringUtil;
import kr.re.keti.sc.dataservicebroker.util.ValidateUtil;

/**
 * Entity 공통 DAO 클래스
 *
 * @param <DynamicEntityDaoVO>
 */
public class HiveEntityDAO implements EntityDAOInterface<DynamicEntityDaoVO> {

    @Autowired
    @Qualifier("hiveSqlSession")
    private SqlSessionTemplate sqlSession;

    @Autowired
	@Qualifier("hiveSqlSession")
    private SqlSessionTemplate batchSqlSession;

    @Autowired
    private HiveTableSVC hiveTableSVC;

    @Autowired
    private DataModelManager dataModelManager;

    @Value("${entity.history.retrive.full.yn:N}")
    public String retrieveFullHistoryYn;    //Entity 전체 이력 조회 여부
    
    @Value("${entity.retrieve.default.limit:1000}")
    private Integer defaultLimit;

    @Value("${entity.history.delete.yn}")
    public String deleteEntityHistoryYn; // Entity 삭제 시 이력까지 모두 삭제 여부

    @Value("${databroker.retryDelayMillisecond:1000}")
    private Integer retryDelayMillisecond;

    @Value("${databroker.retryMaximumThreadPool:10}")
    private Integer threadPool;

    public static String[] MANDATORY_ATTRIBUTE = new String[]{"id", "dataset_id", "created_at", "modified_at"};
    public static String[] MANDATORY_TEMPORAL_ATTRIBUTE = new String[]{"id", "dataset_id", "operation", "modified_at"};
    
    private ExecutorService service;
    
    private final Logger logger = LoggerFactory.getLogger(HiveEntityDAO.class);
    
    /**
     * 벌크 CREATE 처리
     *
     * @param createList CREATE 대상 VO 리스트
     * @return List<ProcessResultVO> 처리 결과 리스트
     */
    @Override
    public List<ProcessResultVO> bulkCreate(List<DynamicEntityDaoVO> createList) {
       List<ProcessResultVO> processResultVOList = new ArrayList<>(createList.size());

       for (DynamicEntityDaoVO entityDaoVO : createList) {
           List<String> tableColumns = hiveTableSVC.getTableScheme(entityDaoVO.getDbTableName());
           entityDaoVO.setTableColumns(tableColumns);

           // 결과 생성
           ProcessResultVO processResultVO = new ProcessResultVO();
           processResultVO.setProcessOperation(Operation.CREATE_ENTITY);
           processResultVO.setProcessResult(true);
           processResultVOList.add(processResultVO);
       }
       HiveEntitySqlProvider mapper = sqlSession.getMapper(HiveEntitySqlProvider.class);
       mapper.bulkCreate(createList);

       return processResultVOList;
    }

    /**
     * CREATE 처리
     *
     * @param entityDaoVO CREATE 대상 VO
     * @return ProcessResultVO 처리 결과VO
     */
    @Override
    public ProcessResultVO create(DynamicEntityDaoVO entityDaoVO) {
        HiveEntitySqlProvider mapper = sqlSession.getMapper(HiveEntitySqlProvider.class);
        List<String> tableColumns = hiveTableSVC.getTableScheme(entityDaoVO.getDbTableName());
        entityDaoVO.setTableColumns(tableColumns);
        mapper.create(entityDaoVO);

        // 결과 생성
        ProcessResultVO processResultVO = new ProcessResultVO();
        processResultVO.setProcessOperation(Operation.CREATE_ENTITY);
        processResultVO.setProcessResult(true);

        logger.debug("create. id=" + entityDaoVO.getId() + ", processResultVO=" + processResultVO);

        return processResultVO;
    }


    /**
     * 벌크 Update Entity Attributes 처리
     * @param updateAttrList Update Entity Attributes 대상 VO 리스트
     * @return List<ProcessResultVO> 처리 결과 리스트
     */
    @Override
//    @Transactional(value = "hiveDataSourceTransactionManager")
    public List<ProcessResultVO> bulkUpdateAttr(List<DynamicEntityDaoVO> updateAttrList) {

    	return null;
//        // 결과 리스트
//        List<ProcessResultVO> processResultVOList = new ArrayList<>(updateAttrList.size());
//
//        EntitySqlProvider2 batchMapper = batchSqlSession.getMapper(EntitySqlProvider2.class);
//
//        for (DynamicEntityDaoVO entityDaoVO : updateAttrList) {
//
//            logger.debug("bulkUpdateAttr. id=" + entityDaoVO.getId());
//
//            // Update Entity Attributes
//            int result = batchMapper.updateAttr(entityDaoVO);
//
//            // 결과 생성
//            ProcessResultVO processResultVO = new ProcessResultVO();
//            processResultVO.setProcessOperation(Operation.UPDATE_ENTITY_ATTRIBUTES);
//            if (result > 0) {
//                processResultVO.setProcessResult(true);
//            } else {
//                processResultVO.setProcessResult(false);
//                processResultVO.setException(new NgsiLdResourceNotFoundException(ErrorCode.INVALID_PARAMETER,
//                        "Not Exists Target Entity. id=" + entityDaoVO.getId()));
//                processResultVO.setErrorDescription("Update Entity Attributes fail. Not Exists Target Entity. id=" + entityDaoVO.getId());
//            }
//            processResultVOList.add(processResultVO);
//
//            logger.debug("bulkUpdateAttr. id=" + entityDaoVO.getId() + ", processResultVO=" + processResultVO);
//        }
//
//        return processResultVOList;
    }
    class RetryThread implements Runnable {

        private CommonEntityDaoVO entityDaoVO;
        private HiveEntitySqlProvider mapper;
        private String executeType;
        private int count = 0;

        RetryThread (CommonEntityDaoVO entityDaoVO, HiveEntitySqlProvider mapper, String executeType) {
            this.entityDaoVO = entityDaoVO;
            this.mapper = mapper;
            this.executeType = executeType;
        }

        @Override
        public void run() {
            boolean needRetry = true;
            while (needRetry) {
                try {
                    Thread.sleep(retryDelayMillisecond);

                    logger.info("ConcurrentException happen Task Retrying..... Count : " + count++);
                    logger.info("Target Db :" + entityDaoVO.getDbTableName());

                    switch (executeType) {
                        case "UPDATE_ATTR": mapper.appendAttr(entityDaoVO); break;
                        case "PARTIAL_ATTR_UPDATE": mapper.partialAttrUpdate(entityDaoVO); break;
                        case "APPEND_ATTR": mapper.appendAttr(entityDaoVO); break;
                        case "APPEND_NO_OVERWRITE_ATTR": mapper.appendNoOverwriteAttr(entityDaoVO); break;
                        case "REPLACE_ATTR_HBASE": mapper.replaceAttrHBase(entityDaoVO); break;
                        case "REPLACE_ATTR": mapper.replaceAttr(entityDaoVO); break;
                        case "DELETE": mapper.delete(entityDaoVO); break;
                        case "DELETE_ATTR": mapper.deleteAttr(entityDaoVO); break;
                    }
    
                    needRetry = false;
                } catch (UncategorizedSQLException e) {
                    if (isConcurrentException(e)) {
                        continue;
                    } else {
                        logger.error("Concurrent error.", e);
                        needRetry = false;
                    }
                } catch (InterruptedException e) {
                    logger.error("Concurrent error.", e);
                    needRetry = false;
                }
            }      
        }
    }

    private int concurrentCheckAndExecuteThread(CommonEntityDaoVO entityDaoVO, HiveEntitySqlProvider mapper,
                                                String executeType, UncategorizedSQLException e) {
		if (isConcurrentException(e)) {
            logger.info("ConcurrentException happen Thread start...");

            if (service == null) {
                service = Executors.newFixedThreadPool(threadPool);
            }

            service.execute(new RetryThread(entityDaoVO, mapper, executeType));

            return 1;
		} else {
            logger.error("Concurrent error.", e);
            
            return 0;
        }
	}

    public static boolean isConcurrentException(UncategorizedSQLException e) {
        String exceptionMsg = e.getMessage(); 
        return exceptionMsg.contains("ConcurrentAppendException") || exceptionMsg.contains("ConcurrentDeleteDeleteException");
    }

    /**
     * Update Entity Attributes 처리
     * @param entityDaoVO Update Entity Attributes 대상 VO
     * @return ProcessResultVO 처리 결과VO
     */
    @Override
    public ProcessResultVO updateAttr(DynamicEntityDaoVO entityDaoVO) {

        logger.debug("Update Entity Attributes. DaoVO=" + entityDaoVO);

        HiveEntitySqlProvider mapper = sqlSession.getMapper(HiveEntitySqlProvider.class);
        List<String> tableColumns = hiveTableSVC.getTableScheme(entityDaoVO.getDbTableName());
        entityDaoVO.setTableColumns(tableColumns);
        int result;
        
        try {
            result = mapper.appendAttr(entityDaoVO);
        } catch (UncategorizedSQLException e) {
            result = concurrentCheckAndExecuteThread(entityDaoVO, mapper, "UPDATE_ATTR", e);
        }
        
        mapper.refreshTable(entityDaoVO);

        // 결과 생성
        ProcessResultVO processResultVO = new ProcessResultVO();
        processResultVO.setProcessOperation(Operation.UPDATE_ENTITY_ATTRIBUTES);
        if (result > 0) {
            processResultVO.setProcessResult(true);
        } else {
            processResultVO.setProcessResult(false);
            processResultVO.setException(new NgsiLdResourceNotFoundException(ErrorCode.INVALID_PARAMETER,
                    "Not Exists Target Entity. id=" + entityDaoVO.getId()));
            processResultVO.setErrorDescription("Update Entity Attributes fail. Not Exists Target Entity. id=" + entityDaoVO.getId());
        }

        logger.debug("Update Entity Attributes. id=" + entityDaoVO.getId() + ", processResultVO=" + processResultVO);

        return processResultVO;
    }

    /**
     * 벌크 Partial Attribute Update 처리
     * @param partialAttrUpdateList Partial Attribute Update 대상 VO 리스트
     * @return List<ProcessResultVO> 처리 결과 리스트
     */
    @Override
//    @Transactional(value = "hiveDataSourceTransactionManager")
    public List<ProcessResultVO> bulkPartialAttrUpdate(List<DynamicEntityDaoVO> partialAttrUpdateList) {

    	return null;
    	
//        // 결과 리스트
//        List<ProcessResultVO> processResultVOList = new ArrayList<>(partialAttrUpdateList.size());
//
//        EntitySqlProvider2 batchMapper = batchSqlSession.getMapper(EntitySqlProvider2.class);
//
//        for (DynamicEntityDaoVO entityDaoVO : partialAttrUpdateList) {
//
//            logger.debug("bulkPartialAttrUpdate. id=" + entityDaoVO.getId());
//
//            // Update Entity Attributes
//            List<String> tableColumns = hiveTableSVC.getTableScheme(entityDaoVO.getDbTableName());
//            entityDaoVO.setTableColumns(tableColumns);
//            int result = batchMapper.partialAttrUpdate(entityDaoVO);
//
//            // 결과 생성
//            ProcessResultVO processResultVO = new ProcessResultVO();
//            processResultVO.setProcessOperation(Operation.PARTIAL_ATTRIBUTE_UPDATE);
//            if (result > 0) {
//                processResultVO.setProcessResult(true);
//            } else {
//                processResultVO.setProcessResult(false);
//                processResultVO.setException(new NgsiLdResourceNotFoundException(ErrorCode.INVALID_PARAMETER,
//                        "Not Exists Target Entity. id=" + entityDaoVO.getId()));
//                processResultVO.setErrorDescription("Partial Attribute Update fail. Not Exists Target Entity. id=" + entityDaoVO.getId());
//            }
//            processResultVOList.add(processResultVO);
//
//            logger.debug("bulkPartialAttrUpdate. id=" + entityDaoVO.getId() + ", processResultVO=" + processResultVO);
//        }
//
//        return processResultVOList;
    }

    /**
     * Partial Attribute Update 처리
     * @param entityDaoVO Update Entity Attributes 대상 VO
     * @return ProcessResultVO 처리 결과VO
     */
    @Override
    public ProcessResultVO partialAttrUpdate(DynamicEntityDaoVO entityDaoVO) {

        logger.debug("Partial Attribute Update. DaoVO=" + entityDaoVO);

        HiveEntitySqlProvider mapper = sqlSession.getMapper(HiveEntitySqlProvider.class);
        List<String> tableColumns = hiveTableSVC.getTableScheme(entityDaoVO.getDbTableName());
        entityDaoVO.setTableColumns(tableColumns);
        int result;

        try {
            result = mapper.partialAttrUpdate(entityDaoVO);
        } catch (UncategorizedSQLException e) {
            result = concurrentCheckAndExecuteThread(entityDaoVO, mapper, "PARTIAL_ATTR_UPDATE", e);
        }

        mapper.refreshTable(entityDaoVO);

        // 결과 생성
        ProcessResultVO processResultVO = new ProcessResultVO();
        processResultVO.setProcessOperation(Operation.PARTIAL_ATTRIBUTE_UPDATE);
        if (result > 0) {
            processResultVO.setProcessResult(true);
        } else {
            processResultVO.setProcessResult(false);
            processResultVO.setException(new NgsiLdResourceNotFoundException(ErrorCode.INVALID_PARAMETER,
                    "Not Exists Target Entity. id=" + entityDaoVO.getId()));
            processResultVO.setErrorDescription("Partial Attribute Update fail. Not Exists Target Entity. id=" + entityDaoVO.getId());
        }

        logger.debug("Partial Attribute Update. id=" + entityDaoVO.getId() + ", processResultVO=" + processResultVO);

        return processResultVO;
    }

    /**
     * 벌크 Append Entity Attributes 처리
     *
     * @param appendAttrList Append Entity Attributes 대상 VO 리스트
     * @return List<ProcessResultVO> 처리 결과 리스트
     */
    @Override
//    @Transactional(value = "hiveDataSourceTransactionManager")
    public List<ProcessResultVO> bulkAppendAttr(List<DynamicEntityDaoVO> appendAttrList) {

    	return null;

//        // 결과 리스트
//        List<ProcessResultVO> processResultVOList = new ArrayList<>(appendAttrList.size());
//
//        EntitySqlProvider2 batchMapper = batchSqlSession.getMapper(EntitySqlProvider2.class);
//
//        for (DynamicEntityDaoVO entityDaoVO : appendAttrList) {
//
//            logger.debug("bulkAppendAttr. id=" + entityDaoVO.getId());
//
//            // Append Entity Attributes
//            List<String> tableColumns = hiveTableSVC.getTableScheme(entityDaoVO.getDbTableName());
//            entityDaoVO.setTableColumns(tableColumns);
//            int result = batchMapper.appendAttr(entityDaoVO);
//
//            // 결과 생성
//            ProcessResultVO processResultVO = new ProcessResultVO();
//            processResultVO.setProcessOperation(Operation.APPEND_ENTITY_ATTRIBUTES);
//            if (result > 0) {
//                processResultVO.setProcessResult(true);
//            } else {
//                processResultVO.setProcessResult(false);
//                processResultVO.setException(new NgsiLdResourceNotFoundException(ErrorCode.INVALID_PARAMETER,
//                        "Not Exists Target Entity. id=" + entityDaoVO.getId()));
//                processResultVO.setErrorDescription("Append Entity Attributes fail. Not Exists Target Entity. id=" + entityDaoVO.getId());
//            }
//            processResultVOList.add(processResultVO);
//
//            logger.debug("bulkAppendAttr. id=" + entityDaoVO.getId() + ", processResultVO=" + processResultVO);
//        }
//
//        return processResultVOList;
    }

    /**
     * Append Entity Attributes 처리
     *
     * @param entityDaoVO Append Entity Attributes 대상 VO
     * @return ProcessResultVO 처리 결과VO
     */
    @Override
    public ProcessResultVO appendAttr(DynamicEntityDaoVO entityDaoVO) {

        logger.debug("appendAttr. DaoVO=" + entityDaoVO);

        HiveEntitySqlProvider mapper = sqlSession.getMapper(HiveEntitySqlProvider.class);
        List<String> tableColumns = hiveTableSVC.getTableScheme(entityDaoVO.getDbTableName());
        entityDaoVO.setTableColumns(tableColumns);
        int result;

        try {
            result = mapper.appendAttr(entityDaoVO);
        } catch (UncategorizedSQLException e) {
            result = concurrentCheckAndExecuteThread(entityDaoVO, mapper, "APPEND_ATTR", e);
        }
        mapper.refreshTable(entityDaoVO);

        // 결과 생성
        ProcessResultVO processResultVO = new ProcessResultVO();
        processResultVO.setProcessOperation(Operation.APPEND_ENTITY_ATTRIBUTES);
        if (result > 0) {
            processResultVO.setProcessResult(true);
        } else {
            processResultVO.setProcessResult(false);
            processResultVO.setException(new NgsiLdResourceNotFoundException(ErrorCode.INVALID_PARAMETER,
                    "Not Exists Target Entity. id=" + entityDaoVO.getId()));
            processResultVO.setErrorDescription("Append Entity Attributes fail. Not Exists Target Entity. id=" + entityDaoVO.getId());
        }

        logger.debug("appendAttr. id=" + entityDaoVO.getId() + ", processResultVO=" + processResultVO);

        return processResultVO;
    }



    /**
     * 벌크 Append Entity Attributes 처리
     *
     * @param appendAttrList Append Entity Attributes 대상 VO 리스트
     * @return List<ProcessResultVO> 처리 결과 리스트
     */
    @Override
//    @Transactional(value = "hiveDataSourceTransactionManager")
    public List<ProcessResultVO> bulkAppendNoOverwriteAttr(List<DynamicEntityDaoVO> appendAttrList) {

    	return null;

//        // 결과 리스트
//        List<ProcessResultVO> processResultVOList = new ArrayList<>(appendAttrList.size());
//
//        EntitySqlProvider2 batchMapper = batchSqlSession.getMapper(EntitySqlProvider2.class);
//
//        for (DynamicEntityDaoVO entityDaoVO : appendAttrList) {
//
//            logger.debug("bulkAppendNoOverwriteAttr. id=" + entityDaoVO.getId());
//
//            // Append Entity Attributes
//            List<String> tableColumns = hiveTableSVC.getTableScheme(entityDaoVO.getDbTableName());
//            entityDaoVO.setTableColumns(tableColumns);
//            int result = batchMapper.appendNoOverwriteAttr(entityDaoVO);
//
//            // 결과 생성
//            ProcessResultVO processResultVO = new ProcessResultVO();
//            processResultVO.setProcessOperation(Operation.APPEND_ENTITY_ATTRIBUTES);
//            if (result > 0) {
//                processResultVO.setProcessResult(true);
//            } else {
//                processResultVO.setProcessResult(false);
//                processResultVO.setException(new NgsiLdResourceNotFoundException(ErrorCode.INVALID_PARAMETER,
//                        "Not Exists Target Entity. id=" + entityDaoVO.getId()));
//                processResultVO.setErrorDescription("Append Entity Attributes (noOverwrite) fail. Not Exists Target Entity. id=" + entityDaoVO.getId());
//            }
//            processResultVOList.add(processResultVO);
//
//            logger.debug("bulkAppendAttr. (noOverwrite) id=" + entityDaoVO.getId() + ", processResultVO=" + processResultVO);
//        }
//
//        return processResultVOList;
    }


    /**
     * Append Entity (noOverwrite) Attributes 처리
     *
     * @param entityDaoVO Append Entity (noOverwrite) Attributes 대상 VO
     * @return ProcessResultVO 처리 결과VO
     */
    @Override
    public ProcessResultVO appendNoOverwriteAttr(DynamicEntityDaoVO entityDaoVO) {

        logger.debug("appendNoOverwriteAttr. DaoVO=" + entityDaoVO);

        HiveEntitySqlProvider mapper = sqlSession.getMapper(HiveEntitySqlProvider.class);
        List<String> tableColumns = hiveTableSVC.getTableScheme(entityDaoVO.getDbTableName());
        entityDaoVO.setTableColumns(tableColumns);
        int result;

        try {
            result = mapper.appendNoOverwriteAttr(entityDaoVO);
        } catch (UncategorizedSQLException e) {
            result = concurrentCheckAndExecuteThread(entityDaoVO, mapper, "APPEND_NO_OVERWRITE_ATTR", e);
        }
        mapper.refreshTable(entityDaoVO);

        // 결과 생성
        ProcessResultVO processResultVO = new ProcessResultVO();
        processResultVO.setProcessOperation(Operation.APPEND_ENTITY_ATTRIBUTES);
        if (result > 0) {
            processResultVO.setProcessResult(true);
        } else {
            processResultVO.setProcessResult(false);
            processResultVO.setException(new NgsiLdResourceNotFoundException(ErrorCode.INVALID_PARAMETER,
                    "Not Exists Target Entity. id=" + entityDaoVO.getId()));
            processResultVO.setErrorDescription("Append Entity (noOverwrite) Attributes fail. Not Exists Target Entity. id=" + entityDaoVO.getId());
        }

        logger.debug("appendAttr. (noOverwrite) id=" + entityDaoVO.getId() + ", processResultVO=" + processResultVO);

        return processResultVO;
    }

    /**
     * 벌크 Replace Entity Attributes 처리
     *
     * @param daoVOList Replace Entity Attributes 대상 VO 리스트
     * @return List<ProcessResultVO> 처리 결과 리스트
     */
    @Override
//    @Transactional(value = "hiveDataSourceTransactionManager")
    public List<ProcessResultVO> bulkReplaceEntity(List<DynamicEntityDaoVO> daoVOList) {

    	return null;

//        // 결과 리스트
//        List<ProcessResultVO> processResultVOList = new ArrayList<>(daoVOList.size());
//
//        EntitySqlProvider2 batchMapper = batchSqlSession.getMapper(EntitySqlProvider2.class);
//
//        for (DynamicEntityDaoVO entityDaoVO : daoVOList) {
//
//            logger.debug("Replace Entity Attributes. id=" + entityDaoVO.getId());
//
//            // Replace Entity Attributes
//            List<String> tableColumns = hiveTableSVC.getTableScheme(entityDaoVO.getDbTableName());
//            int result;
//
//            if (isUsingHBase(entityDaoVO.getDatasetId())) {
//            	entityDaoVO.setTableColumns(tableColumns);
//                result = batchMapper.replaceAttrHBase(entityDaoVO);
//            } else {
//                result = batchMapper.replaceAttr(entityDaoVO);
//            }
//
//            // 결과 생성
//            ProcessResultVO processResultVO = new ProcessResultVO();
//            processResultVO.setProcessOperation(Operation.REPLACE_ENTITY_ATTRIBUTES);
//            if (result > 0) {
//                processResultVO.setProcessResult(true);
//            } else {
//                processResultVO.setProcessResult(false);
//                processResultVO.setException(new NgsiLdResourceNotFoundException(ErrorCode.INVALID_PARAMETER,
//                        "Not Exists Target Entity. id=" + entityDaoVO.getId()));
//                processResultVO.setErrorDescription("Replace Entity Attributes fail. Not Exists Target Entity. id=" + entityDaoVO.getId());
//            }
//            processResultVOList.add(processResultVO);
//
//            logger.debug("Replace Entity Attributes. id=" + entityDaoVO.getId() + ", processResultVO=" + processResultVO);
//        }
//
//        return processResultVOList;
    }

    /**
     * Replace Entity Attributes 처리
     *
     * @param entityDaoVO Replace Entity Attributes 대상 VO
     * @return ProcessResultVO 처리 결과VO
     */
    @Override
    public ProcessResultVO replaceEntity(DynamicEntityDaoVO entityDaoVO) {

        logger.debug("Replace Entity Attributes. DaoVO=" + entityDaoVO);

        HiveEntitySqlProvider mapper = sqlSession.getMapper(HiveEntitySqlProvider.class);
        List<String> tableColumns = hiveTableSVC.getTableScheme(entityDaoVO.getDbTableName());

        int result = 0;

        try {
            if (isUsingHBase(entityDaoVO.getDatasetId())) {
                entityDaoVO.setTableColumns(tableColumns);
                result = mapper.replaceAttrHBase(entityDaoVO);
            } else {
                result = mapper.replaceAttr(entityDaoVO);
            }
        } catch (UncategorizedSQLException e) {
            String executeType = isUsingHBase(entityDaoVO.getDatasetId()) ? "REPLACE_ATTR_HBASE" : "REPLACE_ATTR";

            result = concurrentCheckAndExecuteThread(entityDaoVO, mapper, executeType, e);
        }
        mapper.refreshTable(entityDaoVO);
        // 결과 생성
        ProcessResultVO processResultVO = new ProcessResultVO();
        processResultVO.setProcessOperation(Operation.REPLACE_ENTITY_ATTRIBUTES);
        if (result > 0) {
            processResultVO.setProcessResult(true);
        } else {
            processResultVO.setProcessResult(false);
            processResultVO.setException(new NgsiLdResourceNotFoundException(ErrorCode.INVALID_PARAMETER,
                    "Not Exists Target Entity. id=" + entityDaoVO.getId()));
            processResultVO.setErrorDescription("Replace Entity Attributes fail. Not Exists Target Entity. id=" + entityDaoVO.getId());
        }

        logger.debug("Replace Entity Attributes. id=" + entityDaoVO.getId() + ", processResultVO=" + processResultVO);

        return processResultVO;
    }


    /**
     * FULL UPSERT 벌크 처리
     * - Replace Entity Attributes 벌크 처리 후 result 가 0 인 항목들만 Create 수행
     *
     * @param fullUpsertList FULL UPSERT 대상 VO 리스트
     * @return List<ProcessResultVO> 처리결과 리스트
     */
    @Override
//    @Transactional(value = "hiveDataSourceTransactionManager")
    public List<ProcessResultVO> bulkFullUpsert(List<DynamicEntityDaoVO> fullUpsertList) {

    	return null;
    	
//        List<ProcessResultVO> processResultVOList = new ArrayList<>(fullUpsertList.size());
//
//        // DB 벌크처리 속도 최적화를 위해 update 전체 요청 처리 후 create 처리
//        List<Integer> updateResult = new ArrayList<>(fullUpsertList.size());
//
//        EntitySqlProvider2 batchMapper = sqlSession.getMapper(EntitySqlProvider2.class);
//
//        // Replace Entity Attributes
//        for (DynamicEntityDaoVO entityDaoVO : fullUpsertList) {
//
//            logger.debug("bulkFullUpsert. id=" + entityDaoVO.getId());
//            List<String> tableColumns = hiveTableSVC.getTableScheme(entityDaoVO.getDbTableName());
//            entityDaoVO.setTableColumns(tableColumns);
//            int result;
//
//            if (isUsingHBase(entityDaoVO.getDatasetId())) {
//                result = batchMapper.replaceAttrHBase(entityDaoVO);
//            } else {
//                result = batchMapper.replaceAttr(entityDaoVO);
//            }
//
//            updateResult.add(result);
//        }
//
//        for (int i = 0; i < fullUpsertList.size(); i++) {
//            ProcessResultVO processResultVO = new ProcessResultVO();
//            processResultVO.setProcessOperation(Operation.REPLACE_ENTITY_ATTRIBUTES);
//            int result = updateResult.get(i);
//            if (result == 0) {
//                // CREATE
//                processResultVO.setProcessOperation(Operation.CREATE_ENTITY);
//                List<String> tableColumns = hiveTableSVC.getTableScheme(fullUpsertList.get(i).getDbTableName());
//                fullUpsertList.get(i).setTableColumns(tableColumns);
//                batchMapper.create(fullUpsertList.get(i));
//            }
//
//            processResultVO.setProcessResult(true);
//            processResultVOList.add(processResultVO);
//
//            logger.debug("bulkFullUpsert. id=" + fullUpsertList.get(i).getId() + ", processResultVO=" + processResultVO);
//        }
//        return processResultVOList;
    }

    /**
     * FULL UPSERT 처리
     * - Replace Entity Attributes 결과가 0 인 경우 Create 처리
     *
     * @param entityDaoVO FULL UPSERT 대상 VO
     * @return ProcessResultVO 처리결과VO
     */
    @Override
    public ProcessResultVO fullUpsert(DynamicEntityDaoVO entityDaoVO) {
        ProcessResultVO processResultVO = new ProcessResultVO();
        processResultVO.setProcessOperation(Operation.REPLACE_ENTITY_ATTRIBUTES);

        logger.info("fullUpsert. DaoVO=" + entityDaoVO);

        // Replace Entity Attributes
        HiveEntitySqlProvider mapper = sqlSession.getMapper(HiveEntitySqlProvider.class);
        List<String> tableColumns = hiveTableSVC.getTableScheme(entityDaoVO.getDbTableName());
        entityDaoVO.setTableColumns(tableColumns);

        try {
//            if (isUsingHBase(entityDaoVO.getDatasetId())) {
//                logger.debug("Using HBase An existing row exists. Update Process Execute...");
//
//                mapper.replaceAttrHBase(entityDaoVO);
//            } else {
//                logger.debug("Using Hive An existing row exists. Update Process Execute...");
//
//                mapper.replaceAttr(entityDaoVO);
//            }
            mapper.replaceAttr(entityDaoVO);
        } catch (UncategorizedSQLException e) {
            String executeType = isUsingHBase(entityDaoVO.getDatasetId()) ? "REPLACE_ATTR_HBASE" : "REPLACE_ATTR";

            concurrentCheckAndExecuteThread(entityDaoVO, mapper, executeType, e);
        }
        mapper.refreshTable(entityDaoVO);

        processResultVO.setProcessResult(true);

        logger.debug("fullUpsert. id=" + entityDaoVO.getId() + ", processResultVO=" + processResultVO);

        return processResultVO;
    }

    private boolean isUsingHBase(String datasetId) {
        DatasetFlowBaseVO datasetFlowBaseVO = dataModelManager.getDatasetFlowCache(datasetId);
        List<BigDataStorageType> dataStoreTypes = datasetFlowBaseVO.getBigDataStorageTypes();

        if (dataStoreTypes != null && !dataStoreTypes.isEmpty()) {
            return dataStoreTypes.contains(BigDataStorageType.HBASE);
        }

        return false;
    }


    /**
     * 벌크 PARTIAL UPSERT 처리
     *
     * @param partialUpsertList PARTIAL UPSERT 대상 VO 리스트
     * @return List<ProcessResultVO> 처리 결과 리스트
     */
    @Override
//    @Transactional(value = "hiveDataSourceTransactionManager")
    public List<ProcessResultVO> bulkPartialUpsert(List<DynamicEntityDaoVO> partialUpsertList) {

    	return null;
    	
//        List<ProcessResultVO> processResultVOList = new ArrayList<>(partialUpsertList.size());
//
//        // DB 벌크처리 속도 최적화를 위해 update 요청 처리 후 create 처리
//        List<Integer> updateResult = new ArrayList<>(partialUpsertList.size());
//
//        EntitySqlProvider2 batchMapper = batchSqlSession.getMapper(EntitySqlProvider2.class);
//
//        // Append Entity Attributes
//        for (DynamicEntityDaoVO entityDaoVO : partialUpsertList) {
//
//            logger.debug("bulkPartialUpsert. id=" + entityDaoVO.getId());
//
//            List<String> tableColumns = hiveTableSVC.getTableScheme(entityDaoVO.getDbTableName());
//            entityDaoVO.setTableColumns(tableColumns);
//            int result = batchMapper.appendAttr(entityDaoVO);
//            updateResult.add(result);
//        }
//
//        for (int i = 0; i < partialUpsertList.size(); i++) {
//
//            ProcessResultVO processResultVO = new ProcessResultVO();
//            processResultVO.setProcessOperation(Operation.APPEND_ENTITY_ATTRIBUTES);
//            int result = updateResult.get(i);
//            if (result == 0) {
//                // CREATE
//                processResultVO.setProcessOperation(Operation.CREATE_ENTITY);
//                List<String> tableColumns = hiveTableSVC.getTableScheme(partialUpsertList.get(i).getDbTableName());
//                partialUpsertList.get(i).setTableColumns(tableColumns);
//                batchMapper.create(partialUpsertList.get(i));
//            }
//
//            processResultVO.setProcessResult(true);
//            processResultVOList.add(processResultVO);
//
//            logger.debug("bulkPartialUpsert. id=" + partialUpsertList.get(i).getId() + ", processResultVO=" + processResultVO);
//        }
//
//        return processResultVOList;
    }

    /**
     * PARTIAL UPSERT 처리
     *
     * @param entityDaoVO PARTIAL UPSERT 대상 VO
     * @return ProcessResultVO 처리 결과VO
     */
    @Override
    public ProcessResultVO partialUpsert(DynamicEntityDaoVO entityDaoVO) {
        ProcessResultVO processResultVO = new ProcessResultVO();
        processResultVO.setProcessOperation(Operation.APPEND_ENTITY_ATTRIBUTES);

        logger.debug("partialUpsert. DaoVO=" + entityDaoVO);

        HiveEntitySqlProvider mapper = sqlSession.getMapper(HiveEntitySqlProvider.class);
        List<String> tableColumns = hiveTableSVC.getTableScheme(entityDaoVO.getDbTableName());
        entityDaoVO.setTableColumns(tableColumns);
        int result;

        try {
            result = mapper.appendAttr(entityDaoVO);
        } catch (UncategorizedSQLException e) {
            result = concurrentCheckAndExecuteThread(entityDaoVO, mapper, "APPEND_ATTR", e);
        }

        if (result < 1) {
            processResultVO.setProcessOperation(Operation.CREATE_ENTITY);
            mapper.create(entityDaoVO);
            mapper.refreshTable(entityDaoVO);
        }

        processResultVO.setProcessResult(true);

        logger.debug("partialUpsert. id=" + entityDaoVO.getId() + ", processResultVO=" + processResultVO);

        return processResultVO;
    }


    /**
     * 벌크 DELETE 처리
     *
     * @param deleteList DELETE 대상 VO 리스트
     * @return List<ProcessResultVO> 처리 결과 리스트
     */
    @Override
//    @Transactional(value = "hiveDataSourceTransactionManager")
    public List<ProcessResultVO> bulkDelete(List<DynamicEntityDaoVO> deleteList) {
    	
    	return null;
//        // 결과 리스트
//        List<ProcessResultVO> processResultVOList = new ArrayList<>(deleteList.size());
//
//        for (DynamicEntityDaoVO entityDaoVO : deleteList) {
//
//            logger.debug("bulkDelete. id=" + entityDaoVO.getId());
//
//            // DELETE
//            EntitySqlProvider2 batchMapper = batchSqlSession.getMapper(EntitySqlProvider2.class);
//            int result = batchMapper.delete(entityDaoVO);
//
//            if (result > 0) {
//                if (DataServiceBrokerCode.UseYn.YES.getCode().equals(deleteEntityHistoryYn)) {
//                    batchMapper.deleteHist(entityDaoVO);
//                }
//
//                if (DataServiceBrokerCode.UseYn.YES.getCode().equals(deleteEntityHistoryYn)) {
//                    batchMapper.deleteFullHist(entityDaoVO);
//                }
//            }
//
//            // 결과 생성
//            ProcessResultVO processResultVO = new ProcessResultVO();
//            processResultVO.setProcessOperation(Operation.DELETE_ENTITY);
//            if (result > 0) {
//                processResultVO.setProcessResult(true);
//            } else {
//                processResultVO.setProcessResult(false);
//                processResultVO.setException(new NgsiLdResourceNotFoundException(ErrorCode.INVALID_PARAMETER,
//                        "Not Exists Target Entity. id=" + entityDaoVO.getId()));
//                processResultVO.setErrorDescription("DELETE fail. Not Exists Target Entity. id=" + entityDaoVO.getId());
//            }
//            processResultVOList.add(processResultVO);
//
//            logger.debug("bulkDelete. id=" + entityDaoVO.getId() + ", processResultVO=" + processResultVO);
//        }
//
//        return processResultVOList;
    }

    /**
     * DELETE 처리
     *
     * @param entityDaoVO DELETE 대상 VO
     * @return ProcessResultVO 처리 결과VO
     */
    @Override
//    @Transactional(value = "hiveDataSourceTransactionManager")
    public ProcessResultVO delete(DynamicEntityDaoVO entityDaoVO) {

        logger.debug("delete. id=" + entityDaoVO.getId());

        HiveEntitySqlProvider mapper = sqlSession.getMapper(HiveEntitySqlProvider.class);
        int result;

        try {
            result = mapper.delete(entityDaoVO);
        } catch (UncategorizedSQLException e) {
            result = concurrentCheckAndExecuteThread(entityDaoVO, mapper, "DELETE", e);
        }

        if (result > 0) {
            HiveEntitySqlProvider batchMapper = batchSqlSession.getMapper(HiveEntitySqlProvider.class);
            if (DataServiceBrokerCode.UseYn.YES.getCode().equals(deleteEntityHistoryYn)) {
                batchMapper.deleteHist(entityDaoVO);
                mapper.refreshTable(entityDaoVO);
            }

            if (DataServiceBrokerCode.UseYn.YES.getCode().equals(deleteEntityHistoryYn)) {
                batchMapper.deleteFullHist(entityDaoVO);
                mapper.refreshTable(entityDaoVO);
            }
        }

        // 결과 생성
        ProcessResultVO processResultVO = new ProcessResultVO();
        processResultVO.setProcessOperation(Operation.DELETE_ENTITY);
        if (result > 0) {
            processResultVO.setProcessResult(true);
        } else {
            processResultVO.setProcessResult(false);
            processResultVO.setException(new NgsiLdResourceNotFoundException(ErrorCode.INVALID_PARAMETER,
                    "Not Exists Target Entity. id=" + entityDaoVO.getId()));
            processResultVO.setErrorDescription("DELETE fail. Not Exists Target Entity. id=" + entityDaoVO.getId());
        }

        logger.debug("delete. id=" + entityDaoVO.getId() + ", processResultVO=" + processResultVO);

        return processResultVO;
    }

    @Override
	public ProcessResultVO deleteAttr(DynamicEntityDaoVO entityDaoVO) {
		logger.debug("deleteAttribute. entityId=" + entityDaoVO.getId() + ", attrId=" + entityDaoVO.getAttrId());

		HiveEntitySqlProvider mapper = sqlSession.getMapper(HiveEntitySqlProvider.class);
        int result;
        try {
            result = mapper.deleteAttr(entityDaoVO);
        } catch (UncategorizedSQLException e) {
            result = concurrentCheckAndExecuteThread(entityDaoVO, mapper, "DELETE_ATTR", e);
        }

        mapper.refreshTable(entityDaoVO);

		// 결과 생성
		ProcessResultVO processResultVO = new ProcessResultVO();
		processResultVO.setProcessOperation(Operation.DELETE_ENTITY_ATTRIBUTES);
		if(result > 0) {
			processResultVO.setProcessResult(true);
		} else {
			processResultVO.setProcessResult(false);
			processResultVO.setException(new NgsiLdResourceNotFoundException(ErrorCode.NOT_EXIST_ENTITY,
					"Not Exists Target Entity. entityId=" + entityDaoVO.getId()));
			processResultVO.setErrorDescription("DELETE_ATTRIBUTE fail. Not Exists Target Entity. entityId=" + entityDaoVO.getId());
		}

		logger.debug("deleteAttribute. entityId=" + entityDaoVO.getId() + ", attrId=" + entityDaoVO.getAttrId() + ", processResultVO=" + processResultVO);

		return processResultVO;
	}

    @Override
    public ArrayList<Integer> bulkCreateHist(List<DynamicEntityDaoVO> histList) {

        ArrayList<Integer> resultList = new ArrayList<>(histList.size());
        for (DynamicEntityDaoVO entityDaoVO : histList) {
            HiveEntitySqlProvider batchMapper = batchSqlSession.getMapper(HiveEntitySqlProvider.class);
            List<String> tableColumns = hiveTableSVC.getTableScheme(entityDaoVO.getDbTableName());
            entityDaoVO.setTableColumns(tableColumns);
            int result = batchMapper.createHist(entityDaoVO);
            batchMapper.refreshTable(entityDaoVO);
            resultList.add(result);
        }
        return resultList;
    }

    @Override
    public ArrayList<Integer> bulkCreateFullHist(List<DynamicEntityDaoVO> histList) {
        ArrayList<Integer> resultList = new ArrayList<>(histList.size());
        for (DynamicEntityDaoVO entityDaoVO : histList) {
            HiveEntitySqlProvider batchMapper = batchSqlSession.getMapper(HiveEntitySqlProvider.class);
            List<String> tableColumns = hiveTableSVC.getTableScheme(entityDaoVO.getDbTableName());
            entityDaoVO.setTableColumns(tableColumns);
            int result = batchMapper.createFullHist(entityDaoVO);
            batchMapper.refreshTable(entityDaoVO);
            resultList.add(result);
        }
        return resultList;
    }

    @Override
    public ProcessResultVO deleteAttribute(EntityAttrDaoVO entityAttrDaoVO) {
        logger.debug("deleteAttribute. entityId=" + entityAttrDaoVO.getId() + ", attrId=" + entityAttrDaoVO.getAttrId());

        HiveEntitySqlProvider mapper = sqlSession.getMapper(HiveEntitySqlProvider.class);
        int result = mapper.deleteAttr(entityAttrDaoVO);
        mapper.refreshTable(entityAttrDaoVO);

        // 결과 생성
        ProcessResultVO processResultVO = new ProcessResultVO();
        processResultVO.setProcessOperation(Operation.APPEND_ENTITY_ATTRIBUTES);
        if (result > 0) {
            processResultVO.setProcessResult(true);
        } else {
            processResultVO.setProcessResult(false);
            processResultVO.setException(new NgsiLdResourceNotFoundException(ErrorCode.INVALID_PARAMETER,
                    "Not Exists Target Entity. entityId=" + entityAttrDaoVO.getId()));
            processResultVO.setErrorDescription("DELETE_ATTRIBUTE fail. Not Exists Target Entity. entityId=" + entityAttrDaoVO.getId());
        }

        logger.debug("deleteAttribute. entityId=" + entityAttrDaoVO.getId() + ", attrId=" + entityAttrDaoVO.getAttrId() + ", processResultVO=" + processResultVO);

        return processResultVO;
    }

    @Override
    public DynamicEntityDaoVO selectById(QueryVO queryVO, Boolean useForCreateOperation) {

        DbConditionVO dbConditionVO = setQueryCondition(queryVO, false);

        HiveEntitySqlProvider mapper = sqlSession.getMapper(HiveEntitySqlProvider.class);

        //ID 조건 추가
        dbConditionVO.setId(queryVO.getId());
        DynamicEntityDaoVO entityDaoVO = mapper.selectOne(dbConditionVO);
        return entityDaoVO;
    }

    @Override
    public List<DynamicEntityDaoVO> selectAll(QueryVO queryVO) {
    	DbConditionVO dbConditionVO = setQueryCondition(queryVO, false);

        HiveEntitySqlProvider mapper = sqlSession.getMapper(HiveEntitySqlProvider.class);
        List<DynamicEntityDaoVO> entityDaoVOs = (List<DynamicEntityDaoVO>) mapper.selectList(dbConditionVO);

        return entityDaoVOs;
    }

    @Override
    public List<DynamicEntityDaoVO> selectHistById(QueryVO queryVO) {
    	DbConditionVO dbConditionVO = setQueryCondition(queryVO, true);
        dbConditionVO.setId(queryVO.getId());

        HiveEntitySqlProvider mapper = sqlSession.getMapper(HiveEntitySqlProvider.class);
        List<DynamicEntityDaoVO> entityDaoVOs = (List<DynamicEntityDaoVO>) mapper.selectHistList(dbConditionVO);


        return entityDaoVOs;
    }

    @Override
    public List<DynamicEntityDaoVO> selectAllHist(QueryVO queryVO) {
    	DbConditionVO dbConditionVO = setQueryCondition(queryVO, true);


    	HiveEntitySqlProvider mapper = sqlSession.getMapper(HiveEntitySqlProvider.class);
        List<DynamicEntityDaoVO> entityDaoVOs = (List<DynamicEntityDaoVO>) mapper.selectHistList(dbConditionVO);

        return entityDaoVOs;
    }
    
    @Override
	public Integer selectCount(QueryVO queryVO) {
    	throw new UnsupportedOperationException("HiveDAO not supported 'selectCount'");
	}

	@Override
	public Integer selectHistCount(QueryVO queryVO) {
		throw new UnsupportedOperationException("HiveDAO not supported 'selectHistCount'");
	}
    
    /**
     * DB 조회 조건 세팅
     *
     * @param queryVO
     * @param isTemproal
     * @return
     */
    private DbConditionVO setQueryCondition(QueryVO queryVO, Boolean isTemproal) {

        DataModelCacheVO dataModelCacheVO = queryVO.getDataModelCacheVO();
        if(dataModelCacheVO == null) {
        	if(!ValidateUtil.isEmptyData(queryVO.getDatasetId())) {
        		dataModelCacheVO = dataModelManager.getDataModelCacheByDatasetId(queryVO.getDatasetId());
        	} else if(!ValidateUtil.isEmptyData(queryVO.getType())) {
        		// retrieve 시 queryVO의 type에 데이터모델 namespace.type:version (데이터모델캐쉬 key) 형태로 요청수신함
        		dataModelCacheVO = dataModelManager.getDataModelVOCacheByType(queryVO.getType());
        	}
        	queryVO.setDataModelCacheVO(dataModelCacheVO);
        }

        DbConditionVO dbConditionVO = new DbConditionVO();

        dbConditionVO.setTableName(dataModelCacheVO.getDataModelStorageMetadataVO().getHiveTableName());
        dbConditionVO.setHistTableName(getHistoryTableName(dataModelCacheVO.getDataModelStorageMetadataVO().getHiveTableName()));
        if(queryVO.getLimit() == null) {
        	dbConditionVO.setLimit(defaultLimit);
        } else {
        	dbConditionVO.setLimit(queryVO.getLimit());
        }
        dbConditionVO.setOffset(queryVO.getOffset());


        // id List 조건 넣기
        if (queryVO.getSearchIdList() != null) {

            dbConditionVO.setSearchIdList(queryVO.getSearchIdList());
        }
        // id pattern 조건 넣기
        if (queryVO.getIdPattern() != null) {

            dbConditionVO.setIdPattern(queryVO.getIdPattern());
        }
        //1. 조회 대상 컬럼 세팅
        String selectCondition = this.getSelectCondition(queryVO, isTemproal);
        dbConditionVO.setSelectCondition(selectCondition);

        //2. geo-query param 처리
//        String geoCondition = null;
//        if (queryVO.getGeorel() != null) {
//            dbConditionVO.setGeoCondition(this.convertGeoQuery(generateGeoQuery(dataModelCacheVO, queryVO)));
//        }

        //3. 상세쿼리(q-query) param 처리
        if (queryVO.getQ() != null) {
            queryVO = QueryUtil.generateQuery(dataModelCacheVO, queryVO);
            dbConditionVO.setQueryCondition(queryVO.getQuery());
        }


        //4. 시간 조건(time rel) param 처리
        if (isTemproal == true) {

//            //4. timerel param 처리, 이력 데이터 조회시에만 적용
//            if (queryVO.getTimerel() != null) {
//                queryVO = convertTimerel(queryVO);
//                dbConditionVO.setQueryCondition(queryVO.getTimeQuery());
//            }
        }

        return dbConditionVO;
    }
    
    /**
     * 프로퍼티 조건에 따란 이력 테이블 명 리턴 (full이력 또는 partial 이력 테이블)
     *
     * @param dbTableName
     * @return
     */
    private String getHistoryTableName(String dbTableName) {

        StringBuilder tableNameBuilder = new StringBuilder();

        if (DataServiceBrokerCode.UseYn.YES.getCode().equals(retrieveFullHistoryYn)) {
            tableNameBuilder.append(dbTableName).append("fullhist");
        } else {
            tableNameBuilder.append(dbTableName).append("partialhist");
        }
        return tableNameBuilder.toString();

    }
    
    
    /**
     * select 조건 생성
     * @param queryVO
     * @param isTemproal 이력 데이터 조회 유무
     * @return
     */
    private String getSelectCondition(QueryVO queryVO, Boolean isTemproal) {

    	DataModelCacheVO dataModelCacheVO = queryVO.getDataModelCacheVO();
    	DataModelStorageMetadataVO storageMetadataVO = dataModelCacheVO.getDataModelStorageMetadataVO();
    	Map<String, DataModelDbColumnVO> dbColumnInfoVOMap = storageMetadataVO.getDbColumnInfoVOMap();

    	String selectCondition;
        List<String> targetColumnList = new ArrayList<>();

        // 1-1. attr 조건이 있을 경우
        if (queryVO.getAttrs() != null) {
            for (String attr : queryVO.getAttrs()) {

                Attribute rootAttribute = dataModelCacheVO.getRootAttribute(attr);

                if (rootAttribute == null) {
                    //존재하지 않는 attrs 요청이 왔을 때, 예외 처리
                    throw new NgsiLdBadRequestException(DataServiceBrokerCode.ErrorCode.INVALID_PARAMETER, "attrs ERROR. attr=" + queryVO.getAttrs(), null);
                }
                List<ObjectMember> objectMembers = rootAttribute.getObjectMembers();

                //2-1. childAttributes 조건 처리, ex) "address" : { "value : { "addressCountry" : "xx" } }
                if (objectMembers != null) {
                    for (ObjectMember objectMember : objectMembers) {
                        targetColumnList.add(StringUtil.arrayStrToDbStyle(Arrays.asList(rootAttribute.getName(), objectMember.getName())));
                    }
                }

                //2-2. hasAttributes 조건 처리, ex) "inAccident" : { { "providedBy" : "xx" } }
                List<Attribute> hasAttributes = rootAttribute.getChildAttributes();
                if (hasAttributes != null) {

                    for (Attribute hasAttribute : hasAttributes) {

                        DataServiceBrokerCode.AttributeType hasAttributeType = hasAttribute.getAttributeType();
                        if (hasAttributeType == DataServiceBrokerCode.AttributeType.GEO_PROPERTY) {

                            targetColumnList.add(StringUtil.arrayStrToDbStyle(Arrays.asList(rootAttribute.getName(), hasAttribute.getName())) + Constants.GEO_PREFIX_4326);
//                            targetColumnList.add(StringUtil.arrayStrToDbStyle(Arrays.asList(rootAttribute.getName(), hasAttribute.getName())) + Constants.GEO_PREFIX_3857);
                        } else {
                            targetColumnList.add(StringUtil.arrayStrToDbStyle(Arrays.asList(rootAttribute.getName(), hasAttribute.getName())));

                        }
                    }

                    DataModelDbColumnVO dbColumnInfoVO = dbColumnInfoVOMap.get(rootAttribute.getName());
                    if(dbColumnInfoVO != null) {
                    	targetColumnList.add(dbColumnInfoVO.getColumnName());
                    }
                }

                //2-3. observedAt 조건 처리
                if (rootAttribute.getHasObservedAt() != null && rootAttribute.getHasObservedAt() == true) {
                    targetColumnList.add(StringUtil.arrayStrToDbStyle(Arrays.asList(rootAttribute.getName(), DataServiceBrokerCode.PropertyKey.OBSERVED_AT.getCode())));
                }

                //2-4. 1-level처리 ex) "inAccident" : 11 }
                if (rootAttribute.getChildAttributes() == null && rootAttribute.getChildAttributes() == null) {
                    DataModelDbColumnVO dbColumnInfoVO;

                    if (rootAttribute != null && rootAttribute.getValueType() == DataServiceBrokerCode.AttributeValueType.GEO_JSON) {
                        dbColumnInfoVO = dbColumnInfoVOMap.get(rootAttribute.getName() + Constants.GEO_PREFIX_4326);
                    } else {
                        dbColumnInfoVO = dbColumnInfoVOMap.get(rootAttribute.getName());
                    }

                    if (dbColumnInfoVO != null) {
                        targetColumnList.add(dbColumnInfoVO.getColumnName());
                    }
                }

            }


        } else {
            // 1-2. attr 조건이 없을 경우
            for (Map.Entry<String, DataModelDbColumnVO> entry : storageMetadataVO.getDbColumnInfoVOMap().entrySet()) {

            	DataModelDbColumnVO dbColumnInfoVO = entry.getValue();

                String columnName = dbColumnInfoVO.getColumnName();

                if (!columnName.equalsIgnoreCase("")) {
                    //TODO : 개발 테스트용 값 처리 부분
                    if (!columnName.startsWith("컬럼")) {
                        targetColumnList.add(columnName);
                    }
                }

            }
        }

        if (isTemproal) {
            targetColumnList.addAll(Arrays.asList(MANDATORY_TEMPORAL_ATTRIBUTE));

        } else {
            targetColumnList.addAll(Arrays.asList(MANDATORY_ATTRIBUTE));
        }
        selectCondition = String.join(",", targetColumnList);

        return selectCondition;

    }

}