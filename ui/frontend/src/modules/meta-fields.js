// META DATA TYPE
// disabled -> form use
// display_none -> table use

export const MetaFields = [{
  key: '', name: '', type: '', disable: false, unique: true,
  display_none: false, require: false, col: 0, choices: []
}];

export const MetaTestField = [
  {
    name: 'type', displayName: '데이터 모델 유형', type: 'text', require: false, isTable: false,
    nextFields: [{ name: 'version', displayName: '데이터 모델 버전', type: 'text', require: false, isTable: false }]
  },
  {
    name: 'test2',
    fields: [
      { name: 'namespace', displayName: 'Entity 유형 네임스페이스', type: 'text', require: false, isTable: false },
      { type: null }
    ]
  }
];

// 데이터 모델 메타정보
// 데이터 모델 스마트 검색
export const DATAMODEL_FORM_FIELDS = [
    [{ name: 'id', displayName: '데이터 모델 ID', type: 'text', require: false, isTable: false },
     { name: 'type', displayName: '데이터 모델 유형', type: 'text', require: false, isTable: false }],
    [{ name: 'name', displayName: '데이터 모델명', type: 'text', require: false, isTable: false },
      { type: null }]
];

// 데이터 모델 리스트 필드
export const DATAMODEL_TABLE_FIELDS = [
//  { name: 'number', displayName: '순번', require: false, col: 5 },
  { name: 'id', displayName: 'ID', require: false, col: 10 },
  { name: 'typeUri', displayName: 'TypeUri', require: false, col: 20 },
  { name: 'type', displayName: 'Type', require: false, col: 10 },
  { name: 'creatorId', displayName: '생성자', require: false, col: 10 },
  { name: 'createdAt', displayName: '생성시간', require: false, col: 15 }
];

// 데이터 모델 상세 화면 필드
export const DATAMODEL_MODIFY_FIELDS = [
  [
    { name: 'namespace', displayName: '데이터모델 Namespace', type: 'text', require: true, readOnly: false, col: 180 },
    {
      name: 'attributes', displayName: 'Entity의 Attribute 정보',
      type: 'tree', require: true, readOnly: false, colspan: 2, rowspan: 12,
      treeHeight: '475px', col: 180, slotName: 'treeButtons'
    }
  ],
  [{ name: 'type', displayName: '데이터모델 유형', type: 'text', require: true, readOnly: false }],
  [{ name: 'version', displayName: '데이터모델 버전', type: 'text', require: true, readOnly: false }],
  [{ name: 'context', displayName: '데이터모델 @Context정보', type: 'text', require: true, readOnly: false }],
  [{ name: 'creatorId', displayName: '생성자', type: 'text', require: false, readOnly: true }],
  [{ name: 'createdAt', displayName: '생성시간', type: 'text', require: false, readOnly: true }],
  [{ name: 'modifierId', displayName: '수정자', type: 'text', require: false, readOnly: true }],
  [{ name: 'modifiedAt', displayName: '수정시간', type: 'text', require: false, readOnly: true }],
  [{ name: 'description', displayName: 'Entity 설명', type: 'text', require: false, readOnly: false }],
  [
    {
      name: 'indexAttributeNames',
      displayName: '주요 검색 속성 아이디',
      type: 'userOption',
      require: false,
      readOnly: false,
      isTable: true,
      isAddButton: true,
      isDelButton: true,
      isInput: true,
      tableFields: [
        { displayName: '인덱스 속성명', require: false }
      ],
      tableHeight: '120px',
      overflowY: 'auto',
      slotName: 'indexAttributeButtons'
    }
  ]
];

// 데이터 모델 속성 기본정보
export const DATAMODEL_ENTITY_FIELDS = [
  [
    { name: 'name', displayName: '속성 아이디', type: 'text', require: true, readOnly: false },
    {
      name: 'objectMembers', displayName: 'Object 멤버',
      type: 'tree', require: false, readOnly: false, colspan: 2, rowspan: 12,
      treeHeight: '200px', slotName: 'treeButtons'
    }
  ],
  [{ name: 'attributeType', displayName: '속성 유형', type: 'choice',
    choices: [
      { value: 'Property', displayName: 'Property' },
      { value: 'GeoProperty', displayName: 'GeoProperty' },
      { value: 'Relationship', displayName: 'Relationship' }
    ],
    selectedValue: null,
    require: true, readOnly: false }],
    // 코드화 예정
  [{ name: 'valueType', displayName: '값 유형', type: 'text',
    choices: [],
    require: true, readOnly: false }]
];

// 데이터 모델 속성 부가정보
export const DATAMODEL_ENTITY_ADDITION_FIELDS = [
  [
    { name: 'hasObservedAt', displayName: 'observedAt 포함 여부', type: 'choice',
      choices: [
        { value: true, displayName: 'YES' },
        { value: false, displayName: 'NO' }
      ],
      selectedValue: false,
      require: false, readOnly: false },
    { name: 'description', displayName: '속성 설명', type: 'text', require: false, readOnly: false, colspan: 5 }
  ],
  [
    { name: 'isRequired', displayName: '필수 여부', type: 'choice',
      choices: [
        { value: true, displayName: 'YES' },
        { value: false, displayName: 'NO' }
      ],
      selectedValue: false,
      require: false, readOnly: false },
    {
      name: 'valueEnum', displayName: '속성 허용 값', type: 'userOption',
      require: false, readOnly: false, colspan: 5, rowspan: 4,
      isTable: true,
      isAddButton: true,
      isDelButton: true,
      isInput: true,
      tableFields: [
        { displayName: '속성 허용 값', require: false }
      ],
      tableHeight: '110px',
      overflowY: 'auto',
      slotName: 'additionButtons'
    }
  ],
  [{ name: 'accessMode', displayName: '접근 형태', type: 'text',
	  require: false, readOnly: false }],
  [{ name: 'maxLength', displayName: '값 최대길이', type: 'text', require: false, readOnly: false }],
  [{ name: 'minLength', displayName: '값 최소길이', type: 'text', require: false, readOnly: false }],
  [
    { name: 'greaterThan', displayName: '>', type: 'text', require: false, readOnly: false },
    { name: 'greaterThanOrEqualTo', displayName: '>=', type: 'text', require: false, readOnly: false },
    { name: 'lessThan', displayName: '<', type: 'text', require: false, readOnly: false },
    { name: 'lessThanOrEqualTo', displayName: '<=', type: 'text', require: false, readOnly: false }
  ],
];

// 데이터 모델 속성 object member 팝업 정보
export const DATAMODEL_ENTITY_OBJECT_FIELDS = [
  [
    { name: 'name', displayName: 'Object 멤버명', type: 'text', require: true, readOnly: false },
    {
      name: 'required', displayName: '필수 여부', type: 'choice', require: false, readOnly: false,
      choices: [
        { value: true, displayName: 'YES' },
        { value: false, displayName: 'NO' }
      ],
      selectedValue: false
    }
  ],
  [
    { name: 'maxLength', displayName: '값 최대길이', type: 'text', require: false, readOnly: false },
    { name: 'minLength', displayName: '값 최소길이', type: 'text', require: false, readOnly: false }
  ],
  [
    { name: 'greaterThan', displayName: '>', type: 'text', require: false, readOnly: false },
    { name: 'greaterThanOrEqualTo', displayName: '>=', type: 'text', require: false, readOnly: false }
  ],
  [
    { name: 'lessThan', displayName: '<', type: 'text', require: false, readOnly: false },
    { name: 'lessThanOrEqualTo', displayName: '<=', type: 'text', require: false, readOnly: false }
  ],
  [{ name: 'valueType', displayName: '값 유형', type: 'choice', 
    choices: [
        { value: 'String', displayName: 'String' },
        { value: 'Integer', displayName: 'Integer' },
        { value: 'Double', displayName: 'Double' },
        { value: 'Object', displayName: 'Object' },
        { value: 'Date', displayName: 'Date' },
        { value: 'ArrayString', displayName: 'ArrayString' },
        { value: 'ArrayInteger', displayName: 'ArrayInteger' },
        { value: 'ArrayDouble', displayName: 'ArrayDouble' }
      ],
    selectedValue: null,
    require: true, readOnly: false, colspan: 3 }],
  [{ name: 'description', displayName: 'ChildAttributes 설명', type: 'text', require: false, readOnly: false, colspan: 3 }],
  [{
    name: 'valueEnum', displayName: '허용값 Enum', type: 'userOption',
    require: false, readOnly: false, colspan: 3, rowspan: 4,
    isTable: true,
    isAddButton: true,
    isDelButton: true,
    isInput: true,
    tableFields: [
      { displayName: '허용값', require: false }
    ],
    tableHeight: '110px',
    overflowY: 'auto',
    slotName: 'valueEnumButtons'
  }]
];

// 데이터 셋 메타정보
// 데이터 셋 검색필드
export const DATASET_SEARCH_FIELDS = [
  [{ name: 'name', displayName: '데이터셋이름', type: 'text', require: false },
    { name: 'updateInterval', displayName: '갱신주기', type: 'text', require: false }],
  [{ name: 'category', displayName: '분류체계', type: 'text', require: false },
    { name: 'providerOrganization', displayName: '제공기관', type: 'text', require: false }],
  [{ name: 'providerSystem', displayName: '제공시스템', type: 'text', require: false },
    { name: 'isProcessed', displayName: '데이터가공여부', type: 'text', require: false }],
  [{ name: 'ownership', displayName: '소유권', type: 'text', require: false },
    { name: 'license', displayName: '라이선스', type: 'text', require: false }],
  [{ name: 'datasetExtension', displayName: '제공항목', type: 'text', require: false },
    { name: 'targetRegions', displayName: '지역범위', type: 'text', require: false }],
  [{ name: 'qualityCheckEnabled', displayName: '품질검증여부', type: 'choice',
    require: false,
    choices: [
      { value: true, displayName: '예' },
      { value: false, displayName: '아니오' }
    ],
    selectedValue: null },
    { name: 'dataModelId', displayName: '데이터모델ID', type: 'text', require: false }],
  [
    {
      name: 'dataStoreUri', displayName: '데이터저장위치', type: 'userOption',
      require: false, readOnly: false, colspan: 3,
      isAddButton: true,
      isDelButton: true,
      isInput: false,
      isChoice: true,
      choices: [
        { value: 'Kafka Topic', displayName: 'Kafka Topic' },
        { value: 'Hive URL', displayName: 'Hive URL' },
        { value: 'Postgres URL', displayName: 'Postgres URL' },
        { value: 'Hbase URL', displayName: 'Hbase URL' },
      ],
      selectedValue: null,
      isTable: true,
      tableFields: [
        { displayName: '데이터저장위치', require: false }
      ],
      tableHeight: '120px',
      overflowY: 'auto'
    }
  ],
];

// 데이터 셋 정보 리스트 필드
export const DATASET_TABLE_FIELDS = [
//  { name: 'number', displayName: '순번', require: false, col: 5 },
  { name: 'id', displayName: '데이터셋아이디', require: false, col: 15 },
  { name: 'name', displayName: '데이터셋이름', require: false, col: 15 },
  { name: 'updateInterval', displayName: '갱신주기', require: false, col: 10 },
  { name: 'category', displayName: '분류체계', require: false, col: 10 },
  { name: 'providerSystem', displayName: '제공기관', require: false, col: 15 },
  { name: 'qualityCheckEnabled', displayName: '품질검증여부', require: false, col: 10 },
  { name: 'createdAt', displayName: '생성시간', require: false, col: 15 }
];

// 데이터 셋 정보 상세 필드
export const DATASET_FORM_FIELDS = [
  [
    { name: 'id', displayName: '데이터셋ID', type: 'text', require: true, isTable: false },
    { name: 'name', displayName: '데이터셋이름', type: 'text', require: true, isTable: false },
    { name: 'updateInterval', displayName: '갱신주기', type: 'text', require: true, isTable: false }
  ],
  [
    { name: 'category', displayName: '분류체계', type: 'text', require: true, isTable: false },
    { name: 'providerOrganization', displayName: '제공기관', type: 'text', require: true, isTable: false },
    { name: 'providerSystem', displayName: '제공시스템', type: 'text', require: true, isTable: false }
  ],
  [
    { name: 'isProcessed', displayName: '데이터가공여부', type: 'text', require: true, isTable: false },
    { name: 'ownership', displayName: '소유권', type: 'text', require: true, isTable: false },
    { name: 'license', displayName: '라이선스', type: 'text', require: true, isTable: false }
  ],
  [
    { name: 'datasetItems', displayName: '제공항목', type: 'text', require: true, isTable: false },
    { name: 'targetRegions', displayName: '지역범위', type: 'text', require: true, isTable: false },
    { name: 'qualityCheckEnabled', displayName: '품질검증여부', type: 'choice',
      choices: [
        { value: true, displayName: '예' },
        { value: false, displayName: '아니오' }
      ], require: true, isTable: false }
  ],
  [
    { name: 'creatorId', displayName: '생성자', type: 'text', require: false, isTable: false, readOnly: true },
    {
      name: 'dataStoreUri', displayName: '데이터저장위치',
      type: 'userOption', require: false, readOnly: true, colspan: 3, rowspan: 4,
      isButton: false,
      isTable: true,
      tableFields: [
        { displayName: '데이터 저장 위치', require: false }
      ],
      tableHeight: '120px',
      overflowY: 'auto'
    }
  ],
  [{ name: 'createdAt', displayName: '생성시간', type: 'text', require: false, isTable: false, readOnly: true }],
  [{ name: 'modifierId', displayName: '수정자', type: 'text', require: false, isTable: false, readOnly: true }],
  [{ name: 'modifiedAt', displayName: '수정시간', type: 'text', require: false, isTable: false, readOnly: true }]
];

// 데이터 셋 정보 부가 정보 필드
export const DATASET_FORM_ADDITION_FIELDS = [
  [
    { name: 'providingApiUri', displayName: '제공API주소', type: 'text', require: false, isTable: false },
    { name: 'restrictions', displayName: '제약사항', type: 'text', require: false, isTable: false },
    { name: 'datasetExtension', displayName: '확장자', type: 'text', require: false, isTable: false }
  ],
  [
    { name: 'dataIdentifierType', displayName: '식별자', type: 'text', require: false, isTable: false },
    { name: 'description', displayName: '데이터셋설명', type: 'text', require: false, isTable: false, colspan: 3 }
  ],
  [
    { name: 'dataModelType', displayName: '데이터모델유형', type: 'text', require: false, isTable: false },
    { name: 'sourceDatasetIds', displayName: '식별자유형', type: 'userOption', require: false, isTable: true, colspan: 1, rowspan: 3,
      isAddButton: true,
      isDelButton: true,
      isInput: true,
      tableFields: [
        { displayName: '식별자', require: false }
      ],
      tableHeight: '70px',
      overflowY: 'auto',
      slotName: 'dataStoreUriButtons'
    },
    { name: 'keyword', displayName: '키워드', type: 'userOption', require: false, isTable: true, colspan: 1, rowspan: 3,
      isAddButton: true,
      isDelButton: true,
      isInput: true,
      tableFields: [
        { displayName: '키워드', require: false }
      ],
      tableHeight: '70px',
      overflowY: 'auto',
      slotName: 'keywordsButtons'
    }
  ],
  [{ name: 'dataModelNamespace', displayName: '데이터모델네임스페이스', type: 'text', require: false, isTable: false }],
  [{ name: 'dataModelVersion', displayName: '데이터모델버전', type: 'text', require: false, isTable: false }]
];


// 데이터 셋 플로우 메타정보
// 데이터 셋 플로우 스마트 검색
export const DATASET_FLOW_SEARCH_FIELDS = [
  [{ name: 'datasetId', displayName: '데이터셋ID', type: 'text', require: false, isTable: false },
    { name: 'targetTypes', displayName: 'Provision서버유형', type: 'choice',
      choices: [
        { value: 'dataServiceBroker', displayName: 'DataService Broker' },
        { value: 'bigDataStorageHandler', displayName: 'BigData Storage Handler' }
      ],
      require: false, isTable: false }],
  [{ name: 'historyStoreType', displayName: '엔티티이력저장유형', type: 'choice',
    choices: [
      { value: 'partial', displayName: 'partial' },
      { value: 'full', displayName: 'full' },
      { value: 'all', displayName: 'all' }
    ],
    require: false, isTable: false },
    { name: 'enabled', displayName: '사용여부', type: 'choice',
      choices: [
        { value: true, displayName: '예' },
        { value: false, displayName: '아니오' }
      ],
      require: false, isTable: false }
  ],
  [
    { name: 'bigDataStorageTypes', displayName: '저장소유형', type: 'userOption',
      require: false, isTable: true, readOnly: false, rowspan: 2, colspan: 3,
      isAddButton: true,
      isDelButton: true,
      isInput: false,
      isChoice: true,
      choices: [
        { value: 'hive', displayName: 'hive' },
        { value: 'hbase', displayName: 'hbase' }
      ],
      tableFields: [
        { displayName: 'BigData저장소유형', require: false }
      ],
      tableHeight: '120px',
      overflowY: 'auto',
      col: 180
    }
  ]
];

// 데이터 셋 플로우 리스트 필드
export const DATASET_FLOW_TABLE_FIELDS = [
//  { name: 'number', displayName: '순번', require: false, col: 5 },
  { name: 'datasetId', displayName: '데이터셋ID', require: false, col: 15 },
  { name: 'enabled', displayName: '사용여부', require: false, col: 10 },
  { name: 'modifiedAt', displayName: '수정시간', require: false, col: 15 },
  { name: 'createdAt', displayName: '생성시간', require: false, col: 15 }
];

// 데이터 셋 플로우 상세 필드
export const DATASET_FLOW_DETAIL_FIELDS = [
  [
    { name: 'datasetId', displayName: '데이터셋ID', type: 'text', require: false, isTable: false },
    { name: 'enabled', displayName: '사용여부', type: 'choice', choices: [
        { value: true, displayName: '예' },
        { value: false, displayName: '아니오' }
      ], require: false, isTable: false },
    { name: 'historyStoreType', displayName: '엔티티이력저장유형', type: 'choice', choices: [
        { value: 'partial', displayName: 'partial' },
        { value: 'full', displayName: 'full' },
        { value: 'all', displayName: 'all' }
      ], require: false, isTable: false }
  ],
  [
	{ name: 'creatorId', displayName: '생성자', type: 'text', require: false, isTable: false, readOnly: true },
    { name: 'targetTypes', displayName: 'Provision서버유형', type: 'userOption', colspan: 3, rowspan: 5,
      require: false,
      isAddButton: true,
      isDelButton: false,
      isInput: false,
      isTable: true,
      tableFields: [
        { displayName: 'Provision 서버유형', require: false }
      ],
      tableHeight: '120px',
      overflowY: 'auto'
    }
  ],
  [{ name: 'createdAt', displayName: '생성시간', type: 'text', require: false, isTable: false, readOnly: true }],
  [{ name: 'modifierId', displayName: '수정자', type: 'text', require: false, isTable: false, readOnly: true }],
  [{ name: 'modifiedAt', displayName: '수정시간', type: 'text', require: false, isTable: false, readOnly: true }]
];

// 데이터 셋 플로우 상세 부가정보
export const DATASET_FLOW_DETAIL_ADDITION_FIELDS = [
  [{ name: 'description', displayName: '데이터셋흐름설명', type: 'text', require: false, isTable: false, readOnly: false, col: 170 }]
];

// 데이터 셋 플로우 상세 팝업 정보
export const DATASET_FLOW_POPUP_FIELDS = [
  [{ name: 'type', displayName: 'Provision서버유형', type: 'choice',
    choices: [
      { value: 'dataServiceBroker', displayName: 'dataServiceBroker' },
      { value: 'bigDataStorageHandler', displayName: 'bigDataStorageHandler' }
    ],
    require: false, isTable: false, readOnly: false, col: 180 }],
  [
    { name: 'bigDataStorageTypes', displayName: '저장소유형', type: 'userOption',
      require: false, isTable: true, readOnly: false, rowspan: 5,
      isAddButton: true,
      isDelButton: true,
      isInput: false,
      isChoice: true,
      choices: [
        { value: 'hive', displayName: 'hive' },
        { value: 'hbase', displayName: 'hbase' },
        { value: 'hive_hbase', displayName: 'hive_hbase' }
      ],
      tableFields: [
        { displayName: 'BigData저장소유형', require: false }
      ],
      tableHeight: '120px',
      overflowY: 'auto',
      col: 180
    }
  ]
];

// 품질 검증 이력
// 품질 검증 이력 스마트 검색 필드
export const VERIFICATION_HISTORY_SEARCH_FIELDS = [
  [{ name: 'datasetId', displayName: '데이터셋아이디', type: 'text', require: false, isTable: false },
    { name: 'dataModelType', displayName: '데이터모델ID', type: 'text', require: false, isTable: false }],
  [{ name: 'dataModelType', displayName: '데이터모델유형', type: 'text', require: false, isTable: false },
    { name: 'entityId', displayName: '엔티티아이디', type: 'text', require: false, isTable: false }],
  [
    { name: 'verified', displayName: '품질정상여부', type: 'choice',
	  choices: [
		  { value: true, displayName: '정상' },
	      { value: false, displayName: '비정상' }
	  ],
	  require: false, isTable: false }
  ],
    
  [{ name: 'datetime', displayName: '품질검사시간', type: 'datetime', require: false, isTable: false, colspan: 3 }]
];

// 품질 검증 이력 목록 필드
export const VERIFICATION_HISTORY_TABLE_FIELDS = [
//  { name: 'number', displayName: '순번', require: false, col: 5 },
  { name: 'seq', displayName: '품질검증이력일련번호', require: false, col: 10 },
  { name: 'datasetId', displayName: '데이터셋ID', require: false, col: 10 },
  { name: 'dataModelVersion', displayName: '데이터모델ID', require: false, col: 10 },
  { name: 'dataModelType', displayName: '데이터모델유형', require: false, col: 10 },
  { name: 'entityId', displayName: '엔티티아이디', require: false, col: 10 },
  { name: 'verified', displayName: '정상여부', require: false, col: 10 },
  { name: 'testTime', displayName: '품질검사시간', require: false, col: 15 }
];

// 품질 검증 이력 상세 필드
// 기본정보
export const VERIFICATION_HISTORY_FORM_FIELDS = [
  [
    { name: 'seq', displayName: '이력식별일련번호', type: 'text', require: false, isTable: false },
    { name: 'testTime', displayName: '품질검사시간', type: 'text', require: false, isTable: false },
    { name: 'datasetId', displayName: '데이터셋아이디', type: 'text', require: false, isTable: false }
  ],
  [{ name: 'dataModelVersion', displayName: '데이터모델ID', type: 'text', require: false, isTable: false },
    { name: 'dataModelType', displayName: '데이터모델유형', type: 'text', require: false, isTable: false },
    { name: 'entityId', displayName: '엔티티아이디', require: false, type: 'text', isTable: false }
  ],
  [
	{
	  name: 'verified', displayName: '품질정상여부', require: false, type: 'choice', isTable: false,
    choices: [
      { value: true, displayName: '정상' },
      { value: false, displayName: '비정상' },
    ]
  },
    { type: null },
    { type: null }
  ]
];

// 부가정보

export const VERIFICATION_HISTORY_ADDITION_FORM_FIELDS = [
  [{ name: 'errorCode', displayName: '품질검사오류코드', type: 'choice',
    choices: [
      { value: '000', displayName: '000' },
      { value: '001', displayName: '001' },
      { value: '002', displayName: '002' },
      { value: '003', displayName: '003' },
      { value: '004', displayName: '004' },
      { value: '005', displayName: '005' }
    ],
    require: false, isTable: false, col: 170 }],
  [{ name: 'errorCause', displayName: '상세오류메시지', require: false, type: 'text', isTable: false, col: 170 }],
  [{ name: 'data', displayName: '실패원본데이터', require: false, type: 'text', isTable: false, col: 170 }]
];

// 프로비전 서버 관리
export const PROVISION_TABLE_FIELDS = [
  { name: 'id', displayName: 'Provision 서버아이디', require: false, col: 15 },
  { name: 'type', displayName: 'Provision 서버유형', require: false, col: 15 },
  { name: 'provisionUri', displayName: 'URI', require: false, col: 20 },
  { name: 'provisionProtocol', displayName: '프로토콜', require: false, col: 10 },
  { name: 'enabled', displayName: '사용여부', require: false, col: 10 },
  { name: 'modifiedAt', displayName: '수정시간', require: false, col: 15 },
  { name: 'createdAt', displayName: '생성시간', require: false, col: 15 }
];

export const PROVISION_SEARCH_FIELDS = [
  [{ name: 'type', displayName: '서버유형', type: 'choice',
    choices: [
      { value: 'dataServiceBroker', displayName: 'dataServiceBroker' },
      { value: 'bigDataStorageHandler', displayName: 'bigDataStorageHandler' },
      { value: 'ingestInterface', displayName: 'ingestInterface' }
    ],
    selectedValue: null, require: false, isTable: false },
    { name: 'provisionProtocol', displayName: '프로토콜유형', type: 'choice',
      choices: [
        { value: 'http', displayName: 'http' },
        { value: 'kafka', displayName: 'kafka' }
      ],
      selectedValue: null, require: false, isTable: false }],
  [{ name: 'enabled', displayName: '사용여부', type: 'choice',
    choices: [
      { value: true, displayName: '사용' },
      { value: false, displayName: '미사용' }
    ],
    selectedValue: null, require: false, isTable: false }]
];


// 외부 플랫폼 인증 리스트
export const EXTERNAL_PLATFORM_TABLE_FIELDS = [
  { name: 'id', displayName: '외부 연동 플랫폼 ID', require: false, col: 10 },
  { name: 'name', displayName: '외부 연동 플랫폼 명', require: false, col: 10 },
  { name: 'description', displayName: '외부 연동 플랫폼 설명', require: false, col: 15 },
  { name: 'modifiedAt', displayName: '수정시간', require: false, col: 10 },
  { name: 'createdAt', displayName: '생성시간', require: false, col: 10 }
];

export const EXTERNAL_PLATFORM_SEARCH_FIELDS = [
  [{ name: 'id', displayName: '외부 연동 플랫폼 ID', type: 'text', require: false }]
];

// 코드 그룹 리스트
export const CODE_GROUP_TABLE_FIELDS = [
  { name: 'codeGroupId', displayName: '코드그룹 아이디', require: false, col: 10 },
  { name: 'codeGroupName', displayName: '코드그룹 명', require: false, col: 10 },
  { name: 'enabled', displayName: '사용여부', require: false, col: 10 },
  { name: 'description', displayName: '코드그룹 설명', require: false, col: 20 }
];

export const CODE_TABLE_FIELDS = [
  { name: 'codeGroupId', displayName: '코드그룹 아이디', require: false, col: 10 },
  { name: 'codeId', displayName: '코드 아이디', require: false, col: 10 },
  { name: 'codeName', displayName: '코드명', require: false, col: 10 },
  { name: 'enabled', displayName: '사용여부', require: false, col: 10 },
  { name: 'description', displayName: '코드설명', require: false, col: 20 }
];

