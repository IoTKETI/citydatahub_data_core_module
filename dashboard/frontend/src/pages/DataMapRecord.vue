<template>
  <div class="content" style="background: #f7f7f8;">
    <div class="container-fluid">
      <div class="row">
        <div class="col-12">
          <div class="card">
            <div class="card-header">
              <h4 class="card-title">{{ $t('search.mapSearchHistorical') }}</h4>
              <div class="mt-4">
                <b-form inline>
                  <label class="mr-sm-2">{{ $t('search.dataModel') }}</label>
                  <b-input-group class="mb-2 mr-sm-2 mb-sm-0">
                    <b-form-select
                      id="input-1"
                      type="text"
                      required
                      v-model="selected"
                      :options="dataModels"
                      @change="onDataModelChange"
                    ></b-form-select>
                  </b-input-group>
                  <label class="mr-sm-2">{{ $t('search.entityID') }}</label>
                  <el-select v-model="selected2" filterable placeholder="Select" size="small">
                    <el-option
                      v-for="item in entityList"
                      :key="item.value"
                      :label="item.text"
                      :value="item.value"
                    >
                    </el-option>
                  </el-select>
                  <label class="mr-sm-2 ml-2">{{ $t('search.keywords') }}</label>
                  <b-form-input
                    id="inline-form-input-name"
                    class="mb-2 mr-sm-2 mb-sm-0"
                    :placeholder="$i18n.t('search.provideKeyword')"
                    v-model="searchValue"
                    :disabled="isDisabledSearch"
                  ></b-form-input>
                  <el-button size="small" type="info" @click="handleShowPopup" v-if="keywordIsEmpty && !isBtnLoading">{{ $t('search.options') }}</el-button>
                  <el-button size="small" type="primary" @click="getMapRecord" v-if="!isBtnLoading">{{ $t('comm.search') }}</el-button>
                  <el-button size="small" type="primary" :loading="true" v-if="isBtnLoading">{{ $t('search.searching') }}</el-button>
                </b-form>
              </div>
            </div>
            <div class="card-body">
              <gmap-map
                id="map"
                :center="center"
                :zoom="10"
                :options="options"
                ref="geoMap"
                :draggable="true"
                @dragend="updateCoordinates"
              >
                <gmap-marker
                  :key="index"
                  v-for="(m, index) in markers"
                  :position="m.position"
                  :clickable="true"
                  :draggable="false"
                  :z-index="index === 0 ? 1 : 0"
                  :icon="index === 0 ? { url: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png' } : null"
                  @click="onMarkerClick(m)"
                >
                  <gmap-info-window v-if="m.mapInfo.displayValue">
                    <label>{{ m.mapInfo.displayValue }}</label>
                  </gmap-info-window>
                </gmap-marker>
              </gmap-map>
            </div>
          </div>
        </div>
        <div class="col-6" v-if="isShowChart1">
          <div class="card">
            <div class="card-body">
              <line-chart :chart-data="datacollection[0]" :options="chartOptions"></line-chart>
            </div>
          </div>
        </div>
        <div class="col-6" v-if="isShowChart2">
          <div class="card">
            <div class="card-body">
              <line-chart :chart-data="datacollection[1]" :options="chartOptions"></line-chart>
            </div>
          </div>
        </div>
      </div>
    </div>
    <SearchConfiguration
      :visible="dialogVisible"
      @close-event="handleClose"
      @tab-click="tabClick"
      :activeName="activeName"
      :isChart="true"
    >
      <template v-slot:selectBox>
        <b-form-select
          id="input-1"
          type="text"
          required
          v-model="selected"
          :options="dataModels"
          disabled
        ></b-form-select>
      </template>
      <template v-slot:searchOption>
        <b-form inline class="col-12 mt-2">
          <label class="mr-sm-2">{{ $t('search.timeRelation') }}</label>
          <b-form-select
            type="text"
            required
            style="width: 10vw;"
            v-model="dateSelected"
            :options="dateOptions"
          ></b-form-select>
          <label class="mr-sm-2"></label>
          <el-date-picker
            v-show="dateSelected === 'between'"
            v-model="dateTime"
            type="datetimerange"
            size="small"
            start-placeholder="start at"
            end-placeholder="end at"
            :default-time="['00:00:00']">
          </el-date-picker>
          <el-date-picker
            v-show="dateSelected !== 'between'"
            v-model="dateTime"
            size="small"
            type="datetime"
            :placeholder="$i18n.t('message.selectDateTime')">
          </el-date-picker>
        </b-form>
      </template>
      <template v-slot:tree>
        <ElementTree
          :treeData="treeData"
          @on-tree-event="onTreeEvent"
          :checkList="dynamicQuery"
          nodeKey="query"
        >
        </ElementTree>
      </template>
      <template v-slot:addQuery>
        <b-form inline class="mb-4">
          <label class="mr-sm-2">{{ $t('comm.id') }}</label>
          <b-form-input
            id="inline-form-input-name"
            class="mb-2 mr-sm-2 mb-sm-0"
            v-model="searchId"
            disabled
          ></b-form-input>
          <div class="ml-1">
            <el-button size="small" type="info" @click="addDynamicSearch">{{ $t('comm.add') }}</el-button>
            <el-button size="small" type="primary" @click="handleDynamicSearchSave">{{ $t('comm.save') }}</el-button>
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
        <el-button class="ml-1" type="primary" @click="handleSave" size="small">{{ $t('comm.save') }}</el-button>
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
      <template v-slot:checks>
        <ElementTree
          nodeKey="charts"
          :treeData="treeData"
          :checkBox="true"
          :chartList="chartList"
          @on-chart-event="onChartEvent"
        >
        </ElementTree>
      </template>
    </SearchConfiguration>

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
 * History Map Page.
 * @components SearchConfiguration, ElementTree, DynamicSearch
 * JsonViewer, LineChart, GmapMap, GmapMarker, GmapCluster
 */
import { dataModelApi } from '@/moudules/apis';
import {gmapApi as google, loadGmapApi} from 'vue2-google-maps';
import GmapMap from 'vue2-google-maps/src/components/map';
import GmapCluster from 'vue2-google-maps/src/components/cluster';
import GmapMarker from 'vue2-google-maps/src/components/marker';
import SearchConfiguration from '../components/SearchConfiguration';
import ElementTree from '../components/ElementTree';
import DynamicSearch from '../components/DynamicSearch';
import JsonViewer from 'vue-json-viewer';
import LineChart from '../components/LineChart.js';
import axios from "axios";

export default {
  name: 'DataMapRecord',
  components: {
    SearchConfiguration,
    ElementTree,
    DynamicSearch,
    JsonViewer,
    LineChart,
    GmapMap,
    GmapMarker,
    GmapCluster
  },
  computed: {
    google,
    keywordIsEmpty() {
      return !this.searchValue || this.searchValue === '';
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
  data () {
    return {
      datacollection: [],
      googleMap: null,
      lastShape: null,
      shiftDraw: true,
      radio: 1,
      activeName: 'first',
      dialogVisible: false,
      dialogVisible2: false,
      center: {
        lat: 37.56377293986715,
        lng: 126.99055872213141
      },
      markers: [],
      bounds: null,
      // bounds: { north: 37.59612684714326, south: 37.53569179089619, east: 127.03979547119144, west: 126.9398748168945 },
      options: {},
      dateSelected: null,
      dateOptions: [
        { value: null, text: this.$i18n.t('message.selectOption'), disabled: true },
        { value: 'before', text: this.$i18n.t('search.before') },
        { value: 'after', text: this.$i18n.t('search.after') },
        { value: 'between', text: this.$i18n.t('search.between') },
      ],
      selected: null,
      dataModels: [],
      selected2: null,
      entityList: [{ value: null, text: this.$i18n.t('message.selectOption'), disabled: true }],
      isBtnLoading: false,
      searchData: null,
      detailData: {},
      detailId: null,
      dynamicQuery: {},
      searchId: null,
      entityId: null,
      addList: [],
      treeData: [],
      treeId: null,
      treeRow: null,
      treeNode: null,
      visible3: false,
      attributeValue: '',
      displayAttribute: {},
      searchValue: null,
      isDisabledSearch: false,
      dateTime: '',
      coordinates: [],
      flightPath: null,
      chartList: {},
      isChart: false,
      isShowChart1: false,
      isShowChart2: false,
      chartData: {},
      chartOptions:{
        responsive: true,
        title: {
          text: 'Chart.js Time Scale'
        },
        onClick: (point, event) => {
          if (event.length <= 0) return;
          const index = event[0]['_index'];
          const cData = this.chartData[this.selected2][index];
          let items = null;
          this.markers.some(item => {
            if (item.mapInfo.index === cData.index) {
              items = item;
            }
          });
          this.$refs.geoMap.$mapPromise.then((map) => {
            const bounds = new window.google.maps.LatLngBounds();
            let loc = null;
            loc = new window.google.maps.LatLng(
              items.position.lat,
              items.position.lng,
            );
            bounds.extend(loc);

            map.fitBounds(bounds);
            map.panToBounds(bounds);
            const zoom = map.getZoom();
            map.setZoom(zoom > 12 ? 12 : zoom);
          });
        },
        scales: {
          yAxes: [{
            ticks: { beginAtZero: true },
            gridLines: { display: true },
          }],
          xAxes: [{
            distribution: 'series',
            gridLines: { display: false }
          }]
        },
        legend: { display: true },
        maintainAspectRatio: false
      },
      intervalId: null,
    }
  },
  methods: {
    handleClose(tag) {
      this.dialogVisible = false;
      this.dialogVisible2 = false;
    },
    onShowPopup() {
      this.dialogVisible = true;
    },
    handleInputConfirm(event) {
      this.dialogVisible = true;
    },
    goHistoryView() {
      this.$router.push({ path: '/data-map-record' })
    },
    updateCoordinates(location) {

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
      this.activeName = activeName;
    },
    onMarkerClick(map) {
      const REMOVE_KEYS = ['index', 'uniqueKey', 'rowKey', 'rowSpanMap', 'sortKey', 'uniqueKey', '_attributes', '_disabledPriority', '_relationListItemMap'];
      const resultMapList = [];
      let resultMapData = {};
      Object.keys(map.mapInfo).filter(key => REMOVE_KEYS.indexOf(key) > -1 || resultMapList.push({ [key]: map.mapInfo[key] }));
      resultMapList.forEach(item => Object.assign(resultMapData, item));

      this.detailData = resultMapData;
      this.detailId = map.mapInfo.id;
      this.dialogVisible2 = true;
    },
    handleShowPopup() {
      if (!this.selected) {
        this.$alert(this.$i18n.t('message.selectDataModels'), '', {
          confirmButtonText: 'OK'
        });
        return null;
      }
      this.getDataModels();
      this.dialogVisible = true;
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
      this.treeData = tempTree;
    },
    searchRemove(id, index) {
      const tempTree = [ ...this.treeData ];
      this.addList.splice(index, 1);
      if (Object.keys(this.dynamicQuery).length > 0) {
        delete this.dynamicQuery[id];
      }
      this.treeData = tempTree;
    },
    initClose() {
      this.addList = [];
      this.dynamicQuery = {};
      this.visible3 = false;
      this.isDisabledSearch = false;
      this.dateSelected = null;
      this.dateTime = '';
      this.displayAttribute[this.attributeValue] = null;
      this.attributeValue = null;
    },
    handleSave() {
      // Save exposure attributes.
      this.displayAttribute[this.selected] = this.attributeValue;

      // this.treeId = null;
      this.searchId = null;
      this.addList = [];
      this.treeData = null;
      this.treeRow = null;
      this.treeNode = null;
      this.dialogVisible = false;
      this.isDisabledSearch = true;
      this.attributeValue = null;
    },
    onAttrEvent(displayAttr) {
      this.attributeValue = displayAttr;
    },
    onChartEvent(data) {
      this.chartList = data;
    },
    onDataModelChange() {
      // Data model has been changed to reset.
      this.searchValue = null;
      this.entityId = null;
      this.treeId = null;
      this.dateSelected = null;
      this.dateTime = '';
      this.dynamicQuery = {};
      this.displayAttribute = {};
      this.attributeValue = '';
      this.activeName = 'first';
      this.isDisabledSearch = false;
      this.chartList = {};
      this.selected2 = null;

      this.getEntityList();
    },
    fillData () {
      const attrs = [];
      Object.keys(this.chartList).map(key => {
        attrs.push(key);
      });

      const colorScheme = [
        "#25CCF7","#FD7272","#54a0ff","#00d2d3",
        "#1abc9c","#2ecc71","#3498db","#9b59b6","#34495e",
        "#16a085","#27ae60","#2980b9","#8e44ad","#2c3e50",
        "#f1c40f","#e67e22","#e74c3c","#ecf0f1","#95a5a6",
        "#f39c12","#d35400","#c0392b","#bdc3c7","#7f8c8d",
        "#55efc4","#81ecec","#74b9ff","#a29bfe","#dfe6e9",
        "#00b894","#00cec9","#0984e3","#6c5ce7","#ffeaa7",
        "#fab1a0","#ff7675","#fd79a8","#fdcb6e","#e17055",
        "#d63031","#feca57","#5f27cd","#54a0ff","#01a3a4"
      ];

      let labels = [];
      let dataset = [];
      Object.keys(this.chartData).map((key, index) => {
        let values = [];
        this.chartData[key].forEach(item => {
          values.push(item[attrs[0]].value);
          labels.push(item[attrs[0]].observedAt);
        });
        dataset.push({
          label: key,
          backgroundColor: colorScheme[index],
          borderColor: colorScheme[index],
          pointBorderColor: colorScheme[index],
          pointBackgroundColor: colorScheme[index],
          data: values,
          fill: false,
        });
      });

      let labels2 = [];
      let dataset2 = [];
      if (attrs.length > 1) {
        Object.keys(this.chartData).map((key, index) => {
          let values = [];
          this.chartData[key].forEach(item => {
            values.push(item[attrs[1]].value);
            labels2.push(item[attrs[1]].observedAt);
          });
          dataset2.push({
            label: key,
            backgroundColor: colorScheme[index],
            borderColor: colorScheme[index],
            pointBorderColor: colorScheme[index],
            pointBackgroundColor: colorScheme[index],
            data: values,
            fill: false,
          });
        });
      }

      this.datacollection = [
        {
          labels: labels,
          datasets: dataset
        },
        {
          labels: labels2,
          datasets: dataset2
        }
      ];

    },
    chartOverEvent(event) {
      console.log(event);
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
        confirm: () => this.$alert(this.$i18n.t('message.changeSearchScope'), '', {
          confirmButtonText: 'OK',
          // cancelButtonText: '취소',
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
      });
      this.shiftDraw = false;
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
    getEntityList() {
      this.$http.post('/entities', { dataModelId: this.selected })
        .then(response => {
          const status = response.status;
          if (status === 204) {
            return null;
          }
          const items = response.data;

          let result = [{ value: null, text: this.$i18n.t('message.selectOption'), disabled: true }];
          items.commonEntityVOs.map(item => {
            return result.push({ value: item.id, text: item.id, disabled: false });
          });
          this.entityList = result;
        });
    },
    getMapRecord() {
      if (!this.selected) {
        this.$alert(this.$i18n.t('message.selectDataModels'), '', {
          confirmButtonText: 'OK'
        });
        return null;
      }
      if (!this.selected2) {
        this.$alert(this.$i18n.t('message.selectDataEntityIds'), '', {
          confirmButtonText: 'OK'
        });
        return null;
      }

      // init chart
      this.isShowChart1 = false;
      this.isShowChart2 = false;
      this.datacollection = [];

      this.isBtnLoading = true;
      let params = {};

      const query = [];
      Object.keys(this.dynamicQuery).map(key => {
        this.dynamicQuery[key].forEach(item => {
          query.push(item);
        });
      });

      if (this.coordinates.length > 0) {
        params = {
          dataModelId: this.selected,
          id: this.selected2,
          searchValue: this.searchValue,
          q: query,
          coordinates: JSON.stringify([this.coordinates]),
          displayAttribute: this.displayAttribute[this.selected] ? this.displayAttribute[this.selected] : null,
          timerel: this.dateSelected,
          time: this.dateSelected === 'between' ? this.dateTime[0] : this.dateTime,
          endtime: this.dateSelected === 'between' ? this.dateTime[1] : null,
          timeproperty: this.dateSelected === 'after' ? 'modifiedAt' : null,
        };
      } else {
        params = {
          dataModelId: this.selected,
          id: this.selected2,
          searchValue: this.searchValue,
          q: query,
          displayAttribute: this.displayAttribute[this.selected] ? this.displayAttribute[this.selected] : null,
          timerel: this.dateSelected,
          time: this.dateSelected === 'between' ? this.dateTime[0] : this.dateTime,
          endtime: this.dateSelected === 'between' ? this.dateTime[1] : null,
          timeproperty: this.dateSelected === 'after' ? 'modifiedAt' : null,
        };
      }
      // 데이터 모델 조회 => 순서 상 맨 처음으로 attributeType이 GeoProperty을 갖는 것을 대표 값으로 선택
      dataModelApi.query({dataModelId: params.dataModelId , dataModelType: null}).
      then(response => {
        // 주어진 배열에서 첫번째로 attributeType이 GeoProperty인 속성을 찾는 함수
        function setRepresentativeGeoProperty(attributes) {
          return attributes.find(attribute => attribute.attributeType === 'GeoProperty');
        }

        const geoPropertyAttribute = setRepresentativeGeoProperty(response.attributes);
        const representativeGeoProperty = geoPropertyAttribute ? geoPropertyAttribute.name : undefined;


        this.$http.post('/temporal/entities', params)
          .then(response => {
            const status = response.status;
            // TODO : The alignment specification has been changed, so the front needs to reverse processing and change later.

            if (status === 204) {
              this.$alert(this.$i18n.t('message.noSearchResult'));
              this.markers = [];
              this.flightPath = null;
              this.isBtnLoading = false;
              this.animateCircle(null);
              return null;
            }

            const items = response.data.commonEntityVOs.reverse();

              if (this.flightPath) {
                this.flightPath.setMap(null);
                this.flightPath = null;
              }
            // init marker info
            this.markers = [];
            const markerPath = [];

            items.map(item => {
              const locationKey = item[representativeGeoProperty];
              if(locationKey && Array.isArray(locationKey.value.coordinates)){
                this.markers.push({
                  position: {
                    lat: locationKey.value.coordinates[1],
                    lng: locationKey.value.coordinates[0],
                  },
                  mapInfo: item,
                  displayValue: item.displayValue,
                });
                markerPath.push({
                  lat: locationKey.value.coordinates[1],
                  lng: locationKey.value.coordinates[0],
                  time: locationKey.observedAt,
                });

              }
              else {
                // console.error('Invalid locationKey or coordinates');
              }
            });

            if (this.markers.length === 0) {
              this.$alert(this.$i18n.t('message.noSearchResult'));
              this.flightPath = null;
              this.isBtnLoading = false;
              return null;
            }

            const lineSymbol = {
              path: window.google.maps.SymbolPath.FORWARD_CLOSED_ARROW,
              strokeColor: '#6991fd',
              scale: 4
            };

            //markerPath배열을 observedAt 기준으로 정렬
            markerPath.sort(function(a, b) {
              return Date.parse(a.time) - Date.parse(b.time);
            });

            this.$refs.geoMap.$mapPromise.then((map) => {
              // Draw the path of the marker.
              this.flightPath = new window.google.maps.Polyline({
                path: markerPath,
                strokeColor: '#FF0000',
                strokeOpacity: 0.8,
                strokeWeight: 2,
                icons: [
                  {
                    icon: lineSymbol,
                    offset: '100%',
                  }
                ]
              });
              this.animateCircle(this.flightPath);

              // auto focus, auto zoom in out
              const bounds = new window.google.maps.LatLngBounds();
              let loc = null;
              this.markers.map(item => {
                // I'll use a separate one.
                loc = new window.google.maps.LatLng(item.position.lat, item.position.lng);
                bounds.extend(loc);
              });
              map.fitBounds(bounds);
              map.panToBounds(bounds);
              const zoom = map.getZoom();
              map.setZoom(zoom > 12 ? 12 : zoom);
              setTimeout(() => {
                this.flightPath.setMap(map);
                if (Object.keys(this.chartList).length > 0) {
                  this.getChartData();
                }
              }, 1000);
              this.isBtnLoading = false;
            });
          });
      });
    },
    animateCircle(flightPath) {
      if (this.intervalId || flightPath === null) {
        clearInterval(this.intervalId);
        if (flightPath === null) {
          return null;
        }
      }
      let count = 0;
      this.intervalId = setInterval(() => {
        // flight 마커 이동속도
        count = (count + 1) % 200;
        const icons = flightPath.get('icons');
        icons[0].offset = count / 2 + '%';
        flightPath.set('icons', icons);
      }, 100);
    },
    getChartData() {
      const attrs = [];
      Object.keys(this.chartList).map(key => {
        attrs.push(key);
      });
      if (attrs.length === 1) {
        this.isShowChart1 = true;
      } else if (attrs.length > 1) {
        this.isShowChart1 = true;
        this.isShowChart2 = true;
      }
      const params = {
        dataModelId: this.selected,
        id: this.selected2,
        attrs: attrs
      };
      this.$http.post('/temporal/entities', params)
        .then(response => {
          const status = response.status;
          if (status === 204) {
            this.$alert(this.$i18n.t('message.noSearchResult'));
            return null;
          }

          const items = response.data.commonEntityVOs;

          // console.log(items);

          let entityId = null;
          let tempData = {};
          items.map(item => {
            entityId = item.id;
            if (entityId === item.id) {
              tempData[item.id] = tempData[item.id] || [];
              tempData[item.id].push(item);
            }
          });

          // It has to be displayed in chronological order, so flip the arrangement.
          // tempData[entityId].reverse();

          this.chartData = tempData;
          this.fillData();
        });
    },
    getDataModels() {
      this.$http.get(`/datamodels/attrstree?id=${ this.selected }`)
        .then(response => {
          const status = response.status;
          if (status === 204) {
            return null;
          }
          this.treeData = response.data;
        });
    }
  },
  mounted() {
    const { id, type } = this.$route.query;
    if (type) {
      dataModelApi.query({dataModelId: null, dataModelType: type}).then((dataModel) => {
        this.selected = dataModel.id;
        this.selected2 = id;
        this.getMapRecord();
        this.getEntityList();
      }).catch(err => console.error('mounted(param) error', err));
    }

    this.getDataModelList();

    setTimeout(() => {
      this.loadControls();
    }, 1000);
  }
}
</script>
<style>
#map {
  min-height: calc(100vh - 323px);
}
select {
  border: 1px solid #E3E3E3;
  color: #9A9A9A;
}
/*.vue-map-container,*/
/*.vue-map-container .vue-map {*/
/*  width: 100%;*/
/*  height: 700px;*/
/*}*/
</style>
