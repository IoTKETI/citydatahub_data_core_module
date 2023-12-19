package kr.re.keti.sc.ingestinterface.ingest.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.ErrorCode;
import kr.re.keti.sc.ingestinterface.common.code.IngestInterfaceCode.Operation;
import kr.re.keti.sc.ingestinterface.common.exception.BadRequestException;
import kr.re.keti.sc.ingestinterface.common.exception.BaseException;
import kr.re.keti.sc.ingestinterface.common.vo.CommonEntityFullVO;
import kr.re.keti.sc.ingestinterface.common.vo.EntityProcessVO;
import kr.re.keti.sc.ingestinterface.common.vo.ProcessResultVO;
import kr.re.keti.sc.ingestinterface.common.vo.RequestMessageVO;
import kr.re.keti.sc.ingestinterface.datamodel.DataModelManager;
import kr.re.keti.sc.ingestinterface.datamodel.vo.DataModelCacheVO;
import kr.re.keti.sc.ingestinterface.dataset.vo.DatasetBaseVO;
import kr.re.keti.sc.ingestinterface.util.ValidateUtil;
import org.springframework.beans.factory.annotation.Value;

/**
 * Ingest 공통 서비스 클래스
 * @param <T> Entity 별 FullVO
 */
public abstract class IngestProcessor<T extends CommonEntityFullVO> implements IngestProcessorInterface<T> {

    @Autowired
    protected DataModelManager dataModelManager;
    @Value("${entity.default.context-uri}")
    protected String defaultContextUri;

    /**
     * Entity 데이터 Operation 별 벌크 처리
     *
     * @param requestMessageVOList 요청수신메시지VO리스트
     * @return 처리내역 및 결과포함VO리스트
     */
    @Override
    public List<EntityProcessVO<T>> processBulk(List<RequestMessageVO> requestMessageVOList, IngestInterfaceCode.Operation operation) {

        // 1. 요청수신 VO -> 서비스 처리 VO 로 파싱
        List<EntityProcessVO<T>> processVOList = requestMessageVOToProcessVO(requestMessageVOList);

        // 2. 품질 검사 및 결과 세팅
        verification(processVOList, operation);

        return processVOList;
    }


    /**
     * 요청수신 VO를 실제 서비스 처리 VO로 파싱
     *
     * @param requestMessageVOList 요청 수신 VO
     * @return List OffStreetParkingProcessVO 서비스 처리 VO 리스트
     */
    @Override
    public List<EntityProcessVO<T>> requestMessageVOToProcessVO(List<RequestMessageVO> requestMessageVOList) {

        List<EntityProcessVO<T>> entityProcessVOList = new ArrayList<>();

        for (RequestMessageVO requestMessageVO : requestMessageVOList) {

            // 1. 서비스 로직 처리 과정 중 값을 담아 처리할 서비스 객체 생성
            EntityProcessVO<T> entityProcessVO = new EntityProcessVO<>();

            Operation opertaion = requestMessageVO.getOperation();
            String content = requestMessageVO.getContent();

            entityProcessVO.setContent(content);
            entityProcessVO.setOperation(opertaion);
            entityProcessVO.setOperationOptions(requestMessageVO.getOperationOptions());
            entityProcessVO.setDatasetId(requestMessageVO.getDatasetId());
            entityProcessVO.setIngestTime(requestMessageVO.getIngestTime());

            // 2. 수신 content -> entityFullVO 파싱
            T entityFullVO = null;
            DataModelCacheVO dataModelCacheVO = null;
            DatasetBaseVO datasetBaseVO = null;
            try {

                entityFullVO = this.deserializeContent(content);

                entityFullVO.setCreatedAt(requestMessageVO.getIngestTime());
                entityFullVO.setModifiedAt(requestMessageVO.getIngestTime());
                if (entityFullVO.getId() == null && requestMessageVO.getId() != null) {
                	entityFullVO.setId(requestMessageVO.getId());
                }

                if (ValidateUtil.isEmptyData(entityFullVO.getId())) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_NOT_EXIST_MANDATORY_PARAMETER,
                            "Not found entityId. operation=" + requestMessageVO.getOperation().getCode() + ", to=" + requestMessageVO.getTo() + ", contentType=" + requestMessageVO.getContentType());
                }

                datasetBaseVO = dataModelManager.getDatasetCache(requestMessageVO.getDatasetId());
                if (datasetBaseVO == null) {
                	throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER, "Not Found Dataset");
                }

                boolean isQualityCheckEnabled  = datasetBaseVO.getQualityCheckEnabled();

                dataModelCacheVO = dataModelManager.getDataModelCacheByDatasetId(requestMessageVO.getDatasetId());
                if (isQualityCheckEnabled && dataModelCacheVO == null) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER, "Not Found DataModel");
                }
                
                if (isQualityCheckEnabled && entityFullVO.getType() == null) {
                    throw new BadRequestException(ErrorCode.VERIFICATION_INVALID_PARAMETER, "Not Found \"type\" Attribute in entities");
                }

                if(dataModelCacheVO != null) {
                	entityFullVO.setType(dataModelCacheVO.getType());
                }

                // @context가 존재하지 않을 경우 default context 사용
                setDefaultContextIfEmpty(entityFullVO);

                entityProcessVO.setEntityType(entityFullVO.getType());
                entityProcessVO.setEntityFullVO(entityFullVO);
                entityProcessVO.setDataModelCacheVO(dataModelCacheVO);
                entityProcessVO.setDatasetBaseVO(datasetBaseVO);
                entityProcessVOList.add(entityProcessVO);

            } catch (BaseException e) {
                ProcessResultVO processResultVO = new ProcessResultVO();
                processResultVO.setProcessResult(false);
                processResultVO.setException(e);
                processResultVO.setErrorDescription("Content Parsing Error. content=" + content);
                entityProcessVO.setProcessResultVO(processResultVO);
                entityProcessVO.setDataModelCacheVO(dataModelCacheVO);
                entityProcessVO.setDatasetBaseVO(datasetBaseVO);
                entityProcessVOList.add(entityProcessVO);
                continue;

            } catch (Exception e) {
                ProcessResultVO processResultVO = new ProcessResultVO();
                processResultVO.setProcessResult(false);
                processResultVO.setException(new BadRequestException(ErrorCode.REQUEST_MESSAGE_PARSING_ERROR, e));
                processResultVO.setErrorDescription("Content Parsing Error. content=" + content);
                entityProcessVO.setProcessResultVO(processResultVO);
                entityProcessVO.setDataModelCacheVO(dataModelCacheVO);
                entityProcessVO.setDatasetBaseVO(datasetBaseVO);
                entityProcessVOList.add(entityProcessVO);
                continue;
            }
        }

        return entityProcessVOList;
    }

    private void setDefaultContextIfEmpty(T entityFullVO) {
        if(ValidateUtil.isEmptyData(entityFullVO.getContext())) {
            entityFullVO.setContext(Arrays.asList(defaultContextUri));
        }
    }

    private void verification(List<EntityProcessVO<T>> entityProcessVOList, IngestInterfaceCode.Operation operation) {
        if (entityProcessVOList == null) return;

        for (int i = 0; i < entityProcessVOList.size(); i++) {

            EntityProcessVO<T> entityProcessVO = entityProcessVOList.get(i);

            try {

                // 파싱 실패건은 품질검사 제외
                if (entityProcessVO.getProcessResultVO() != null
                        && entityProcessVO.getProcessResultVO().isProcessResult() != null
                        && !entityProcessVO.getProcessResultVO().isProcessResult()) {
                    continue;
                }


                if(entityProcessVO.getDatasetBaseVO().getQualityCheckEnabled()){
                    this.verification(entityProcessVO.getEntityFullVO(), entityProcessVO.getDataModelCacheVO(), operation);

                }

                // 품질검증 성공
                ProcessResultVO processResultVO = new ProcessResultVO();
                processResultVO.setProcessResult(true);
                entityProcessVO.setProcessResultVO(processResultVO);

                // 품질검증 에러
            } catch (BaseException e) {
                ProcessResultVO processResultVO = new ProcessResultVO();
                processResultVO.setProcessResult(false);
                processResultVO.setException(e);
                processResultVO.setErrorDescription("Verification Error. content=" + entityProcessVO.getContent());
                entityProcessVO.setProcessResultVO(processResultVO);
                entityProcessVOList.set(i, entityProcessVO);
                continue;

                // 정의되지 않은 에러
            } catch (Exception e) {
                ProcessResultVO processResultVO = new ProcessResultVO();
                processResultVO.setProcessResult(false);
                processResultVO.setException(new BadRequestException(ErrorCode.VERIFICATION_UNKNOWN_ERROR, e));
                processResultVO.setErrorDescription("Verification Error. content=" + entityProcessVO.getContent());
                entityProcessVO.setProcessResultVO(processResultVO);
                entityProcessVOList.set(i, entityProcessVO);
                continue;
            }
        }
    }
}
