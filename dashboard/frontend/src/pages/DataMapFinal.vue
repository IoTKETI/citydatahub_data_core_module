<template>
  <div class="content">
    <div class="container-fluid">
      <div class="row">
        <div class="col-12">
          <div class="card">
            <div class="card-header">
              <h4 class="card-title">{{ $t('search.mapSearchLatest') }}</h4>
              <!-- v2.0 dashboard add -->
              <div class="mt-12 mb-12 text-right">
                <el-select
                  v-model="latestValue"
                  filterable
                  :placeholder="$t('message.selectMap')"
                  size="small"
                  style="margin-right: 10px;"
                  @change="getLatest"
                >
                  <el-option
                    v-for="item in latestList"
                    :key="item.mapSearchConditionId"
                    :label="item.mapSearchConditionName"
                    :value="item.mapSearchConditionId">
                  </el-option>
                </el-select>
                <el-button type="primary" icon="el-icon-edit" size="small" :disabled="!latestValue" @click="createMap">{{ $t('search.newMap') }}</el-button>
                <el-button type="info" icon="el-icon-check" size="small" @click="saveMap">{{ $t('comm.save') }}</el-button>
                <el-button type="info" icon="el-icon-delete" size="small" :disabled="isRemoveBtn" @click="removeMap">{{ $t('comm.delete') }}</el-button>
              </div>
              <!-- v2.0 dashboard add -->
            </div>

            <div class="card-body">
              <div class="mt-4 mb-2">
                <div class="row">
                  <div class="col-8">
                    <el-tag
                      :key="tag"
                      v-for="(tag, index) in dynamicTags"
                      closable
                      class="mt-2"
                      :disable-transitions="false"
                      @click="handleInputConfirm(tag)"
                      @close="handleTagClose(tag)"
                      effect="dark"
                      :color="tagTypes[index]"
                      style="border: none;"
                    >
                      {{ tag }}
                    </el-tag>
                  </div>
                  <div class="col text-right">
                    <el-button size="small" class="button-new-tag mt-1" style="margin-right: 0;" @click="onShowPopup">+ Data Model(s)</el-button>
                    <el-button size="small" type="info" style="margin-right: 1px;" :disabled="this.dynamicTags.length == 0" @click="getEntitiesIsMap">{{ $t('comm.search') }}</el-button>
                  </div>
                </div>
              </div>

              <!-- v2.0 dashboard add -->
              <div class="row mb-2">
                <div class="col-xl-8">
                  <h4 style="margin: 0;" v-if="!isChangeName" @dblclick="changeName">{{latestName}}</h4>
                  <el-input
                    v-if="isChangeName"
                    type="text"
                    :placeholder="$t('message.enterTitle')"
                    v-model="latestName"
                    maxlength="32"
                    show-word-limit
                    size="small"
                    @blur="focusOut"
                  >
                  </el-input>
                </div>
              </div>
              <!-- v2.0 dashboard add -->

              <div class="row">
                <div class="col-xl-8">
                  <gmap-map
                    id="map"
                    :center="center"
                    :zoom="10"
                    :options="options"
                    ref="geoMap"
                    :draggable="true"
                    @dragend="updateCoordinates"
                  >
                    <gmap-cluster :gridSize="10">
                      <gmap-marker
                        v-for="(m, index) in markers"
                        :key="m.position.id"
                        :position="m.position"
                        :clickable="true"
                        :draggable="false"
                        :icon="m.icon"
                        :zIndex="m.zIndex"
                        :animation="m.animation"
                        @click="onMarkerClick(m)"
                      >
                        <gmap-info-window v-if="m.displayValue" :opened="isInfoWindow" :position="m.position">
                          <label>{{ m.displayValue }}</label>
                        </gmap-info-window>
                      </gmap-marker>
                    </gmap-cluster>
                  </gmap-map>
                </div>
                <div class="col-xl-4">
                  <div class="text-right mt-2 mb-2">
                    <el-button size="small" type="info" :disabled="this.subscribeList.length == 0" @click="goSubscriptions(true)">{{ $t('search.subscribe') }}</el-button>

                    <!-- Fetch Historical Data [기록 데이터 가져오기] -->
                    <el-button size="small" type="info" :disabled="!this.params.id" @click="goHistoryView">{{ $t('search.fetchHistoricalData') }}</el-button>
                  </div>
                  <strong style="font-size: 12px;">* {{ $t('message.checkSubscription') }}</strong>
                  <!-- 우측 하단의 grid, 구독 정보를 확인하세요, Entity ID, 엔티티 ID -->
                  <grid
                    :data="gridData"
                    :columns="[{ header: $i18n.t('search.entityID'), name: 'id', align: 'center' }]"
                    :rowHeaders="rowHeaders"
                    @check="onChecked"
                    @checkAll="onChecked"
                    @uncheck="onUnChecked"
                    @uncheckAll="onUnChecked"
                    :scrollX="false"
                    :bodyHeight="500"
                    @click="onGridClick"
                    ref="tuiGrid1"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <SearchConfiguration
      :searchValue="searchValue"
      :visible="dialogVisible"
      :activeName="searchValue ? 'second' : 'first'"
      @close-event="handleClose"
      @tab-click="tabClick"
    >
      <template v-slot:selectBox>
        <el-select
          v-model="selected"
          placeholder="Select"
          size="small"
          @change="onDataModelChange"
          :disabled="isSelectDisabled"
        >
          <el-option
            v-for="item in dataModels"
            :key="item.value"
            :label="item.text"
            :value="item.value"
            :disabled="item.disabled"
          >
          </el-option>
        </el-select>
      </template>
      <template v-slot:inputBox
        v-if="this.modelSelected">
        <label class="mr-sm-2 ml-4">{{ $t('search.keywords') }}</label>
        <b-form-input
          id="inline-form-input-name"
          class="mb-2 mr-sm-2 mb-sm-0"
          :placeholder="$i18n.t('search.provideKeyword')"
          v-model="validatedSearchValue"
        ></b-form-input>
      </template>
      <template v-slot:tree>
        <ElementTree :treeData="treeData" @on-tree-event="onTreeEvent" :checkList="dynamicQuery" nodeKey="query">
        </ElementTree>
      </template>
      <template v-slot:addQuery>
        <b-form inline class="mb-4">
          <label class="mr-sm-2">Attribute</label>
          <b-form-input
            id="inline-form-input-name"
            class="mb-2 mr-sm-2 mb-sm-0"
            v-model="searchId"
            disabled
          ></b-form-input>
          <div class="ml-1">
            <el-button size="small" type="info" @click="addDynamicSearch">{{ $t('comm.add') }}</el-button>
            <el-button
              size="small"
              type="primary"
              @click="handleDynamicSearchSave"
              v-show="addList.length && addList[addList.length-1].operator && addList[addList.length-1].value"
            >
              {{ $t('comm.save') }}
            </el-button>

          </div>
        </b-form>
        <DynamicSearch v-for="(map, index) in addList" :formData="map" :index="index" @remove="searchRemove" />
      </template>
      <template v-slot:buttonGroup>
        <el-popover
          placement="top"
          width="200"
          v-model="visible3">
          <p style="font-size: 12px;">
            {{ $t('message.resetCheck') }}
          </p>
          <div style="text-align: right; margin: 0">
            <el-button size="mini" type="" @click="visible3 = false">{{ $t('comm.cancel') }}</el-button>
            <el-button type="primary" size="mini" @click="initClose">{{ $t('comm.ok') }}</el-button>
          </div>
          <el-button slot="reference" class="mr-2" type="danger" size="small">{{ $t('comm.reset') }}</el-button>
        </el-popover>
        <!-- <el-button class="ml-1" type="primary" @click="handleSave" size="small">{{ $t('comm.save') }}</el-button> -->
        <el-button
          class="ml-1"
          type="primary"
          @click="handleSave"
          size="small"
          v-show="!keywordIsEmpty || isSelectDisabled"
        >
          {{ $t('comm.save') }}
        </el-button>

      </template>
      <template v-slot:radios>
        <ElementTree
          nodeKey="attrs"
          :treeData="treeData"
          :radioBox="true"
          :radioValue="attributeValue"
          @on-attr-event="onAttrEvent"
        >
        </ElementTree>
      </template>
    </SearchConfiguration>

    <!--dialogVisible2 마커를 눌렀을 때 나오는 다이얼로그, 상세정보-->
    <el-dialog
      title="Model Attribute"
      :visible.sync="dialogVisible2"
      width="30%"
      :before-close="handleClose">
      <div class="mb-3">
        <b-form inline>
          <label class="mr-sm-2">{{ $t('comm.id') }}</label>
          <b-form-input
            id="inline-form-input-name"
            class="col"
            v-model="detailId"
            disabled
          />
        </b-form>
      </div>
      <div class="card">
        <div class="card-body" style="height: 20vmax; overflow-y: auto;">
          <JsonViewer
            :value="detailData"
            :expand-depth=5
            copyable
            sort
          >
          </JsonViewer>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="dialogVisible2 = false" size="small">{{ $t('comm.ok') }}</el-button>
      </span>
    </el-dialog>

  </div>
</template>
<script>

/**
 * Latest Map Page.
 * @components Grid, SearchConfiguration, ElementTree, DynamicSearch
 * JsonViewer, GmapMap, GmapMarker, GmapCluster
 */
import { latestApi, dataModelApi } from '@/moudules/apis';
import { loadGmapApi, gmapApi as google } from 'vue2-google-maps';
import GmapMap from 'vue2-google-maps/src/components/map';
import GmapCluster from 'vue2-google-maps/src/components/cluster';
import GmapMarker from 'vue2-google-maps/src/components/marker';
import 'tui-grid/dist/tui-grid.css';
import { Grid } from '@toast-ui/vue-grid';
import SearchConfiguration from '../components/SearchConfiguration';
import TuiGrid from 'tui-grid';
import ElementTree from '../components/ElementTree';
import DynamicSearch from '../components/DynamicSearch';
import JsonViewer from 'vue-json-viewer';

import axios from 'axios';

TuiGrid.setLanguage('ko');
TuiGrid.applyTheme('striped');

export default {
  name: 'DataMapFinal',
  components: {
    Grid,
    SearchConfiguration,
    ElementTree,
    DynamicSearch,
    JsonViewer,
    GmapMap,
    GmapMarker,
    GmapCluster
  },
  computed: {
    google,
    keywordIsEmpty() {
      return !this.searchValue || this.searchValue === '';
    },

    validatedSearchValue: {
      get() {
        return this.searchValue;
      },
      set(newValue) {
        // 공백만 있는 문자열을 제거
        newValue = newValue.trim();

        // 문자열과 숫자만 가능하도록 정규식을 이용하여 필터링
        newValue = newValue.replace(/[^a-z0-9]/gi, '');

        this.searchValue = newValue;
      }
    }


  },
  beforeMount () {
    axios.get('/getapikey')
      .then(response => {
        loadGmapApi({
          key: response.data,
          libraries: 'drawing'
        })
      });
  },
  // watch: {
  //   google(api) {
  //     this.googleMap = api;
  //   }
  // },
  data () {
    return {
      gridSize: 0,
      googleMap: null,
      lastShape: null,
      shiftDraw: true,
      radio: 1,
      dynamicTags: [],
      dialogVisible: false,
      dialogVisible2: false,
      inputValue: '',
      rowHeaders: [{ type: 'checkbox' }],
      edited: null,
      bounds: null,
      gridData: [],
      center: {
        lat: 37.56377293986715,
        lng: 126.99055872213141
      },
      markers: [],
      options: {
        minZoom: 6
      },
      selected: null,
      dataModels: [],
      addList: [],
      searchData: null,
      searchId: null,
      dynamicQuery: {},
      visible3: false,
      treeData: [],
      treeId: null,
      treeRow: null,
      treeNode: null,
      modelSelected: false,
      searchChecked: false,
      detailData: {},
      detailId: null,
      selectedData: {},
      isSelectDisabled: false,
      searchValue: null,
      searchList: {},
      boundsChecked: false,
      subscribeList: [],
      attributeValue: '',
      displayAttribute: {},
      coordinates: [],
      subscriptionId: null,
      params: {},
      websocket: null,
      entityId: null,
      tagTypes: ['#6991fd', '#f79903', '#e661ac', '#8e67fd', '#47e64d'],
      infoWindowPosition: null,
      infoWindowLabel: null,
      isInfoWindow: false,
      isHistoryBtn: true,
      timeout: null,

      // v2.0 adds
      latestList: [],
      latestValue: null,
      latestName: this.$i18n.t('message.enterTitle'),
      searchCondition: [],
      subscriptionCondition: [],
      isChangeName: false,
      isRemoveBtn: true,
    }
  },
  methods: {
    handleInputChange(value) {
      this.showSaveButton = value;
    },
    focusOut() {
      this.isChangeName = false;
      if (!this.latestName) {
        this.latestName = this.$i18n.t('message.enterTitle')
      }
    },
    changeName() {
      this.isChangeName = true;
    },
    // websocket connection
    socketConnect() {
      if (!this.websocket) {
        // TODO process.env - subtract it as an environmental variable.
        // const serverURL = 'ws://localhost:38081/events'
        const serverURL = `ws://${ window.location.host }/events`;
        this.websocket = new WebSocket(serverURL);

        this.websocket.onopen = (event) => {
          // console.log(event);
          this.onOpen();
        };

        this.websocket.onmessage = (event) => {
          this.onMessage(event);
        };

        this.websocket.onclose = (event) => {
          // console.log(event);
          // console.log('websocket close');
        };
      }
    },
    onOpen() {
      const entityIds = [];
      this.subscribeList.map(item => {
        entityIds.push(item.id);
      });
      const data = { subscriptionId: this.subscriptionId, entityIds: entityIds };
      this.websocket.send(JSON.stringify(data));
    },
    onMessage(event) {
      if (this.timeout) {
        clearTimeout(this.timeout);
        this.timeout = null;
      }
      const response = JSON.parse(event.data);
      this.entityId = response.id;

      const markers = [];
      this.markers.map(item => {
        const icon = this.entityId === item.mapInfo.id ? 'http://maps.google.com/mapfiles/ms/icons/red-dot.png' : item.icon;
        const zIndex = this.entityId === item.mapInfo.id ? 1 : 0;
        const animation = this.entityId === item.mapInfo.id || item.animation > 0 ? window.google.maps.Animation.BOUNCE : null;

        markers.push({
          position: item.position,
          icon: icon,
          zIndex: zIndex,
          mapInfo: item.mapInfo,
          displayValue: item.displayValue,
          animation: animation
        });
      });

      this.markers = []; // Initialize the marker to refresh it.
      markers.map(item => {
        this.markers.push({
          position: item.position,
          icon: item.icon,
          zIndex: item.zIndex,
          mapInfo: item.mapInfo,
          displayValue: item.displayValue,
          animation: item.animation
        });
      });
      this.timeout = setTimeout(() => {
        const temp = this.markers;
        temp.map((item) => {
          item.icon = 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png';
          item.animation = null;
        });
        this.markers = [];
        this.markers = temp;
      }, 9000);
    },
    disconnect() {
      if (this.websocket){
        this.websocket.close();
        this.websocket = null;
      }
    },
    // init google map
    initMap() {
      this.$refs.geoMap.$mapPromise.then((map) => {
        map.panTo({lat: 37.56377293986715, lng: 126.99055872213141});
        map.setZoom(10);
      });
    },
    // Call the list of registered latests.
    getLatestList() {
      latestApi.fetch('latest')
        .then(data => {
          this.latestList = data;
        });
    },
    // Call the registered latest details.
    getLatest(value) {
      this.subscriptionId = null;
      this.isRemoveBtn = false;
      this.isChangeName = false;
      this.searchCondition = [];
      this.subscriptionCondition = [];
      this.dynamicTags = [];
      this.displayAttribute = {};
      this.subscribeList = [];

      this.initMap();     // init map
      this.markers = [];  // init marker
      this.disconnect();  // stop websocket
      this.gridData = []; // init entity id list
      this.$refs.tuiGrid1.invoke('resetData', this.gridData);

      latestApi.detail(value)
        .then(data => {
          this.latestValue = value;
          this.latestName = data.mapSearchConditionName;
          this.searchCondition = JSON.parse(data.searchCondition);
          this.subscriptionCondition = JSON.parse(data.subscriptionCondition);

          this.searchCondition.map(item => {
            this.dynamicTags.push(item.dataModelId);
            this.displayAttribute[item.dataModelId] = item.displayAttribute;
          });
          // setting and search
          this.getEntitiesIsMap();
        });
    },
    // Latest add button click event.
    // Initialization item, stop websocket.
    createMap() {
      this.isRemoveBtn = true;
      this.latestName = this.$i18n.t('message.enterTitle');
      this.latestValue = null;
      this.searchCondition = [];
      this.subscriptionCondition = [];
      this.dynamicTags = [];
      this.displayAttribute = {};
      this.subscribeList = [];

      this.initMap();     // init map
      this.markers = [];  // init marker
      this.disconnect();  // stop websocket
      this.gridData = []; // init entity id list
      this.$refs.tuiGrid1.invoke('resetData', this.gridData);
    },

    setTypeParams(type) {
      const typeParams = [];
      this.dynamicTags.forEach(value => {
        let query = [];
        if (this.selectedData[value]) {
          Object.keys(this.selectedData[value]).map(index => {
            this.selectedData[value][index].map(item => {
              query.push(item);
            });
          });
        }
        if (this.coordinates.length > 0) {
          typeParams.push({
            dataModelId: value,
            q: query,
            searchValue: this.searchList[value],
            coordinates: JSON.stringify([this.coordinates]),
            displayAttribute: this.displayAttribute[value],
          });
        } else {
          typeParams.push({
            dataModelId: value,
            q: query,
            searchValue: this.searchList[value],
            displayAttribute: this.displayAttribute[value],
          });
        }
      });

      if (type === 'saveMap') {
        this.searchCondition = typeParams;
        this.subscriptionCondition = this.subscribeList.map(item => {
          return { id: item.id, type: item.type };
        });
      }
      return typeParams;
    },
    // latest add(register/modify)
    saveMap() {
      this.$confirm(this.$i18n.t('message.saveCheck'), '', {
        confirmButtonText: 'OK',
        cancelButtonText: 'Cancel',
        type: 'info'
      }).then(() => {
        // Extract search condition information to save.
        this.setTypeParams('saveMap');
        const params = {
          mapSearchConditionType: 'latest',
          mapSearchConditionName: this.latestName,
          searchCondition: JSON.stringify(this.searchCondition),
          subscriptionCondition: JSON.stringify(this.subscriptionCondition)
        };

        const method = this.latestValue ? 'modify' : 'create';
        if (method === 'modify') {
          params.mapSearchConditionId = this.latestValue;
        }
        latestApi[method](params)
          .then(data => {
            this.getLatestList();
            this.getLatest(data.mapSearchConditionId);
          });
      }).catch(() => {});
    },
    // delete latest
    removeMap() {
      this.$confirm(this.$i18n.t('message.deleteCheck'), '', {
        confirmButtonText: 'OK',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        latestApi.delete(this.latestValue)
          .then(data => {
            // Initialize all items after calling the list.
            this.getLatestList();
            this.createMap();
          });
      }).catch(() => {});
    },
    handleTagClose(tag) {
      this.dataModels.forEach(item => {
        if (item.value === tag) {
          this.modelSelected = false;
          return item.disabled = false;
        }
      });

      this.dynamicTags.splice(this.dynamicTags.indexOf(tag), 1);

    },
    handleClose() {
      this.modelSelected = false;
      this.dialogVisible = false;
      this.dialogVisible2 = false;
      this.isSelectDisabled = false;
      this.searchId = null;
      this.addList = [];
      this.selected = null;
      this.treeData = null;
      this.treeRow = null;
      this.treeNode = null;
      this.attributeValue = null;
    },
    handleSave() {
      // Selected data cannot be added.
      const result = this.dataModels.filter(s => this.dynamicTags.indexOf(s.value) > -1);
      result.forEach(s => s.disabled = true);

      const checked = this.dynamicTags.some(item => {
        if (item === this.selected) {
          return item;
        }
      });
      if (!checked) {
        if (this.dynamicTags.length > 4) {
          this.$alert(this.$i18n.t('message.enterMaxNum', [5]));
          return null;
        }
        this.dynamicTags.push(this.selected);
      }

      // Save the search word.
      this.searchList[this.selected] = this.searchValue;
      // Save exposure attributes.
      this.displayAttribute[this.selected] = this.attributeValue;

      this.treeId = null;
      this.searchId = null;
      this.addList = [];
      this.selected = null;
      this.searchValue = null;
      this.attributeValue = null;
      this.treeData = null;
      this.treeRow = null;
      this.treeNode = null;
      this.dialogVisible = false;
      this.isSelectDisabled = false;
    },
    initClose() {
      this.addList = [];
      this.dynamicQuery = {};
      this.visible3 = false;
      this.displayAttribute[this.attributeValue] = null;
      this.modelSelected = false;
      this.attributeValue = null;
    },
    searchRemove(id, index) {
      const tempTree = [ ...this.treeData ];
      this.addList.splice(index, 1);
      if (Object.keys(this.dynamicQuery).length > 0) {
        delete this.dynamicQuery[id];
      }
      this.treeData = tempTree;
    },
    // Attribute 추가 및 저장
    handleDynamicSearchSave() {
      const tempTree = [ ...this.treeData ];
      this.treeData = [];
      this.addList.map(item => {
        if (item.temp === 'AND') {
          item.condition = ';';
        } else {
          item.condition = '|';
        }
        this.dynamicQuery[this.treeId] = {};
      });
      this.addList.map(item => {
        Object.keys(this.dynamicQuery).map(key => {
          if (item.tempId === key) {
            this.dynamicQuery[key] = this.addList;
          }
        });
      });
      // Set it up to find information when you modify it.
      this.selectedData[this.selected] = this.dynamicQuery;
      this.treeData = tempTree;
      this.isSelectDisabled = true;
    },
    onTreeEvent(data, node) {
      if (this.searchChecked) {
        this.$alert(this.$i18n.t('message.notSupportDetailSearch'));
        return null;
      }
      this.searchId = data.id;
      this.treeId = data.fullId;
      this.treeRow = data;
      this.treeNode = node;

      this.addList = [];
      Object.keys(this.dynamicQuery).some(key => {
        if (key === data.fullId) {
          this.addList = this.dynamicQuery[key]
        }
      });
    },
    onAttrEvent(displayAttr) {
      this.attributeValue = displayAttr;
    },
    onShowPopup() {
      // Selected data cannot be added.
      const result = this.dataModels.filter(s => this.dynamicTags.indexOf(s.value) > -1);
      result.forEach(s => s.disabled = true);

      this.selected = null;
      this.searchValue = null;
      this.dialogVisible = true;
      this.addList = [];
      this.dynamicQuery = {};
      this.searchChecked = false;
      this.modelSelected = false;
    },
    handleInputConfirm(val) {
      this.onDataModelChange(val);
      if (this.selectedData[val]) {
        this.dynamicQuery = this.selectedData[val];
      }
      this.attributeValue = this.displayAttribute[val];
      this.searchValue = this.searchList[val];

      this.selected = val;
      this.dialogVisible = true;
      this.isSelectDisabled = true;
    },
    onGridClick(event) {
      // console.log("GRID_CLICKED");
      // grid 클릭
      if (event.targetType !== 'cell') {
        return null;
      }

      // 'search'에 해당하는 typeParams를 설정
      const typeParams = this.setTypeParams('search');

      // GeoProperty를 찾아주는 비동기 함수
      async function findGeoProperty() {
        // 데이터 모델 조회
        const response = await dataModelApi.query({ dataModelId: typeParams[0].dataModelId, dataModelType: null });

        // 응답에서 attributeType이 'GeoProperty'인 속성 찾기
        const geoPropertyAttribute = response.attributes.find(attribute => attribute.attributeType === 'GeoProperty');

        // GeoProperty 속성이 존재하면 해당 속성의 이름 반환, 그렇지 않으면 undefined 반환
        return geoPropertyAttribute ? geoPropertyAttribute.name : undefined;
      }

      findGeoProperty().then(representativeGeoProperty => {
        // 이 지점에서 representativeGeoProperty는 비동기 호출이 완료된 후의 값을 가짐

        // 특정 row에 대한 정보를 가져옴
        const items = this.$refs.tuiGrid1.invoke('getRow', event.rowKey);

        // 대표 GeoProperty 위치 정보를 가져옴
        const locationKey = items[representativeGeoProperty];

        // 위치 정보가 존재하고, 그 값이 좌표 배열이라면
        if (locationKey && Array.isArray(locationKey.value.coordinates)) {
          const [lng, lat] = locationKey.value.coordinates; // 순서를 주의. 일반적으로 GeoJSON은 [경도, 위도] 순서를 사용

          // 해당 위치를 중심으로 지도 이동
          this.$refs.geoMap.$mapPromise.then((map) => {
            const bounds = new window.google.maps.LatLngBounds();
            const loc = new window.google.maps.LatLng(lat, lng);

            // 지도를 특정 위치로 이동시키고 확대
            bounds.extend(loc);
            map.fitBounds(bounds);
            map.panToBounds(bounds);

            // 현재 줌 레벨을 가져와서 최대 줌 레벨(현재 18)을 넘지 않도록 설정
            const zoom = map.getZoom();
            map.setZoom(zoom > 18 ? 18 : zoom);
          });

          // 파라미터 업데이트
          this.params = { id: items.id, type: items.type };
        }
      });

    },
    goHistoryView() {
      // console.log(this.params);
      if (!this.params.id) {
        // this.$alert(this.$i18n.t('message.clickItem', [this.$i18n.t('search.entityID')]));
        return null;
      }
      this.$router.push({ path: '/mapSearchHistorical', query: this.params});
    },
    updateCoordinates(location) {
      // console.log(location);
    },
    updateEdited(event) {
      const ne = event.getNorthEast();
      const sw = event.getSouthWest();
      this.bounds = {
        north: ne.lat(),
        east: ne.lng(),
        south: sw.lat(),
        west: sw.lng()
      };
    },
    tabClick(tab, event, activeName) {
      // console.log(activeName);
    },
    // 마커 클릭 시
    onMarkerClick(map) {
      // console.log("MarkerClicked");
      const REMOVE_KEYS = ['uniqueKey', 'rowKey', 'rowSpanMap', 'sortKey', 'uniqueKey', '_attributes', '_disabledPriority', '_relationListItemMap'];
      const resultMapList = [];
      let resultMapData = {};
      Object.keys(map.mapInfo).filter(key => REMOVE_KEYS.indexOf(key) > -1 || resultMapList.push({ [key]: map.mapInfo[key] }));
      resultMapList.forEach(item => Object.assign(resultMapData, item));

      this.detailData = resultMapData;
      this.detailId = map.mapInfo.id;
      this.dialogVisible2 = true;
    },
    onMarkerOver(event) {
      this.infoWindowPosition = {
        lat: event.getCenter().lat(),
        lng: event.getCenter().lng(),
      };
      let labels = null;
      this.markers.map(item => {
        labels += item.displayValue + ', ';
      });
      this.infoWindowLabel = labels.slice(0, -2);
      this.isInfoWindow = true;
    },
    onMarkerOut(event) {
      this.infoWindowPosition = null;
      this.infoWindowLabel = null;
      this.isInfoWindow = false;
    },
    // 모델 선택 시
    onDataModelChange(value) {
      dataModelApi.attributes({dataModelId: value, typeUri: null}).
        then(data =>
          this.treeData = data,
          this.modelSelected = true)
          .catch((err) => null);
    },
    onChecked(event) {
      // all check
      if (event.rowKey >= 0) {
        this.subscribeList.push(this.$refs.tuiGrid1.invoke('getRow', event.rowKey));
        if (this.subscriptionId) {
          const isAlert = this.subscribeList.length > 0;
          if (this.subscribeList)
            this.deleteSubscriptions(isAlert);
        }
      } else {
        this.subscribeList = this.$refs.tuiGrid1.invoke('getData');
        const isAlert = this.subscribeList.length > 0;
        if (this.subscriptionId) {
          this.deleteSubscriptions(isAlert);
        }
      }
    },
    onUnChecked(event) {
      if (event.rowKey >= 0) {
        const data = this.$refs.tuiGrid1.invoke('getRow', event.rowKey);
        this.subscribeList.forEach((item, index) => {
          if (data.id === item.id) {
            this.subscribeList.splice(index, 1);
          }
        });
        this.deleteSubscriptions(true);
      } else {
        this.subscribeList = [];
        if (this.subscriptionId) {
          this.deleteSubscriptions(true);
        }
      }
    },
    loadControls() {
      const drawingManager = new window.google.maps.drawing.DrawingManager({
        // drawingMode: window.google.maps.drawing.OverlayType.MARKER,
        drawingMode: null,
        drawingControl: true,
        rectangleOptions: {
          fillOpacity: 0,
          strokeColor: '#F56C6C',
        },
        drawingControlOptions: {
          position: window.google.maps.ControlPosition.TOP_CENTER,
          drawingModes: [
            window.google.maps.drawing.OverlayType.RECTANGLE
          ]
        },
        confirm: () => this.$alert(this.$i18n.t('message.changeSearchArea'), '', {
          confirmButtonText: 'OK',
          type: 'info'
        }).then(() => {
        }).catch((event) => {
          if (event === 'cancel') {
            return true;
          }
        })
      });

      drawingManager.setMap(this.$refs.geoMap.$mapObject);

      const that = this;
      window.google.maps.event.addListener(drawingManager, 'overlaycomplete', e => {
        if (that.lastShape !== null) {
          that.lastShape.setMap(null);
        }
        // this.confirm();
        // if (that.lastShape !== undefined) {
        //   that.lastShape.setMap(null);
        // }
        // if (this.shiftDraw === false) {
        //   drawingManager.setDrawingMode(null);
        // }
        that.lastShape = e.overlay;
        that.lastShape.type = e.type;
        const shape = that.lastShape.getBounds();
        const ne = shape.getNorthEast();
        const sw = shape.getSouthWest();
        that.coordinates = [[ne.lng(), sw.lat()], [sw.lng(), sw.lat()], [sw.lng(), ne.lat()], [ne.lng(), ne.lat()], [ne.lng(), sw.lat()]];
        // [south,east],[south,west],[north,west],[north,east],[south,east]
      });
      this.shiftDraw = false;
    },
    addDynamicSearch() {
      if (!this.searchId) {
        return null;
      }
      if (this.addList.length > 9) {
        this.$alert(this.$i18n.t('message.enterMaxNum', [10]), '', {
          confirmButtonText: 'OK'
        });
        return null;
      }
      this.addList.push({
        attr: this.treeRow.fullId,
        tempId: this.treeId,
        fullId: this.treeRow.fullId,
        valueType: this.treeRow.valueType,
        condition: null,
        temp: null,
        operator: null,
        value: null
      });
    },
    goSubscriptions(isAlert) {
      this.$http.post('/subscriptions', this.subscribeList)
        .then(response => {
          const items = response.data;
          const status = response.status;
          if (status === 201) {
            this.subscriptionId = items.id;
            if (isAlert) {
              this.$alert(this.$i18n.t('message.successSubscribe'));
            }
            // websocket connect
            this.socketConnect();
          }
        });
    },
    deleteSubscriptions(isAlert) {
      // console.log(this.subscriptionId);
      this.$http.delete(`/subscriptions/${ this.subscriptionId }`)
        .then(response => {
          const status = response.status;
          if (status === 204) {
            this.disconnect();
            this.subscriptionId = null;
            if (isAlert) {
              this.$alert(this.$i18n.t('message.clearSubscribe'));
            }
            if (this.subscribeList.length > 0) {
              this.goSubscriptions(false);
            }
          }
        });
    },
    getDataModelList() {
      this.$http.get('/datamodelIds')
        .then((response) => {
          const items = response.data;
          let result = [{ value: null, text: this.$i18n.t('message.selectOption'), disabled: true }];
          items.map(item => {
            return result.push({ value: item, text: item, disabled: false });
          });
          this.dataModels = result;
        });
    },
    getEntitiesIsMap() {
      if (this.dynamicTags.length === 0) {
        return null;
      }

      // If it's a new search, process unsubscribe.
      if (this.subscriptionId && this.subscribeList.length > 0) {
        this.subscribeList = [];
        this.deleteSubscriptions();
      }

      this.isInfoWindow = false;

      // 마커 찍는 부분
      const typeParams = this.setTypeParams('search');

      const url = [
        'http://maps.google.com/mapfiles/ms/icons/blue-dot.png',
        'http://maps.google.com/mapfiles/ms/icons/orange-dot.png',
        'http://maps.google.com/mapfiles/ms/icons/pink-dot.png',
        'http://maps.google.com/mapfiles/ms/icons/purple-dot.png',
        'http://maps.google.com/mapfiles/ms/icons/green-dot.png',
      ];

      const iconData = [];
      const tags = this.dynamicTags;
      tags.map((item, index) => {
        iconData.push({
          type: item,
          icon: url[index],
        });
      });

      // 데이터 모델 조회 => 순서 상 맨 처음으로 attributeType이 GeoProperty을 갖는 것을 대표 값으로 선택
      dataModelApi.query({dataModelId: typeParams[0].dataModelId , dataModelType: null}).
      then(response => {
        // 주어진 배열에서 첫번째로 attributeType이 GeoProperty인 속성을 찾는 함수
        function setRepresentativeGeoProperty(attributes) {
          return attributes.find(attribute => attribute.attributeType === 'GeoProperty');
        }

        const geoPropertyAttribute = setRepresentativeGeoProperty(response.attributes);
        const representativeGeoProperty = geoPropertyAttribute ? geoPropertyAttribute.name : undefined;

        this.$http.post('/entities/multimodel', typeParams)
        .then(response => {
          const items = response.data;
          const status = response.status;
          if (status === 204) {
            this.$alert(this.$i18n.t('message.noSearchResult'));
            return null;
          }

          // init marker info
          this.markers = [];
          const data = [];
          // marker info setting
          items.map(item => {
            item.commonEntityVOs.map(resultItem => {
              iconData.map((item2, index) => {
                if (item2.type === resultItem.type) {
                  resultItem['icon'] = item2.icon
                }
              });
            });
          });

          items.map(item => {
            item.commonEntityVOs.map(resultItem => {
              // console.error(resultItem);
              const locationKey = resultItem[representativeGeoProperty];
              // 확인: resultItem[locationKey]이 정의되어 있는지, 그리고 그 값이 좌표를 나타내는 배열인지
              if(locationKey && Array.isArray(locationKey.value.coordinates)){
                this.markers.push({
                  position: {
                    lat: locationKey.value.coordinates[1],
                    lng: locationKey.value.coordinates[0],
                  },
                  mapInfo: resultItem,
                  displayValue: resultItem.displayValue,
                  icon: resultItem.icon
                });
                // console.error(this.markers);
                // Remove icons from source data.
                delete resultItem.icon;
                delete resultItem.displayValue;
                data.push(resultItem);
              }
              else {
                // console.error('Invalid locationKey or coordinates');
              }
            });

            this.$refs.geoMap.$mapPromise.then((map) => {
              // auto focus, auto zoom in out
              const bounds = new window.google.maps.LatLngBounds();
              let loc = null;
              this.markers.map(item => {
                // console.log(item.position.lat, item.position.lng);
                loc = new window.google.maps.LatLng(item.position.lat, item.position.lng);
                bounds.extend(loc);
              });
              map.fitBounds(bounds);
              map.panToBounds(bounds);
              const zoom = map.getZoom();
              map.setZoom(zoom > 12 ? 12 : zoom);

              // const zoom = this.$refs.geoMap.$mapObject.getZoom();
              // this.$refs.geoMap.$mapObject.setZoom(zoom > 12 ? 12 : zoom);
            });

          });
          this.gridData = data;
          this.$refs.tuiGrid1.invoke('resetData', this.gridData);


          // Find the rowKey of the stored information and set up the check box.
          const gridList = [ ...this.$refs.tuiGrid1.invoke('getData') ];
          const subscription = this.subscriptionCondition;
          subscription.forEach(s => {
            gridList.filter(k => k.id === s.id)
              .find(r => this.$refs.tuiGrid1.invoke('check', r.rowKey));
          });

          // If it's saved, I'll automatically set up a subscription.
          if (subscription.length > 0) {
            this.goSubscriptions(false);
          }

          setTimeout(() => {
            this.isHistoryBtn = false;
            this.isInfoWindow = true;
          }, 1000);
        });
      });
    }
  },
  mounted() {
    this.getLatestList();

    this.getDataModelList();
    setTimeout(() => {
      this.loadControls();
    }, 1000);
  }
}
</script>

<style>
.card-title {
  font-size: 16px;
  font-weight: bold !important;
}
#map {
  min-height: calc(100vh - 323px);
}
.el-tag {
  margin-right: 10px;
  cursor: pointer;
}
.button-new-tag {
  margin-right: 10px;
  height: 32px;
  line-height: 30px;
  padding-top: 0;
  padding-bottom: 0;
}
.input-new-tag {
  width: 90px;
  margin-right: 10px;
  vertical-align: bottom;
}
.custom-select {
  font-size: 12px;
  height: 32px;
}
.form-control {
  font-size: 12px;
  height: 32px;
}
select {
  border: 1px solid #E3E3E3;
  color: #9A9A9A;
}
ul {
  list-style:none;
}
button.gm-ui-hover-effect {
  display: none !important;
}
.gm-style-iw + button {
  display: none;
}
table > tbody > tr {
  cursor: pointer;
}
</style>