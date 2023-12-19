<template>
  <div style="padding: 20px;">
    <div style="text-align: center; font-weight: bold;">
      <label style="font-size: 12px;">
        {{chartTitle}}
      </label>
    </div>
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
    <div class="col-xl-4" style="display: none;">
      <div class="text-right mt-2 mb-2">
        <el-button size="small" type="primary" @click="goSubscriptions(true)">{{ $t('search.subscribe') }}</el-button>
        <el-button size="small" type="primary" :disabled="isHistoryBtn" @click="goHistoryView">{{ $t('search.fetchHistoricalData') }}</el-button>
      </div>
      <strong style="font-size: 12px;">* {{ $t('message.checkSubscription') }}</strong>
      <grid
        :data="gridData"
        :columns="[{ header: 'Entity ID', name: 'id', align: 'center' }]"
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
    <el-dialog
      title="Model Attribute"
      :visible.sync="dialogVisible2"
      width="80%"
      :before-close="handleClose"
    >
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
        <div class="card-body" style="height: 10vmax; overflow-y: auto;">
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
 * Dashboard Widget Latest Map Type Page
 * Components used to register dashboard widgets
 * @component
 * - Grid,
 * - SearchConfiguration,
 * - ElementTree,
 * - DynamicSearch,
 * - JsonViewer,
 * - GmapCluster
 * @props latestId, chartTitle, elementId
 * @state data () { ... }
 */
import { latestApi } from '@/moudules/apis';
import {gmapApi as google, loadGmapApi} from 'vue2-google-maps';
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
import axios from "axios";
import i18n from '@/moudules/i18n';

TuiGrid.setLanguage(i18n.locale);
TuiGrid.applyTheme('striped');

export default {
  name: 'DashboardLatestMap',
  props: {
    latestId: String,
    chartTitle: String,
    elementId: String
  },
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
    google
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
      isDisabledSearch: true,
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
      isRemoveBtn: true
    }
  },
  methods: {
    // map title input focus out event
    focusOut() {
      if (!this.latestName) {
        this.latestName = this.$i18n.t('message.enterTitle');
      }
    },
    // websocket connection handler
    socketConnect() {
      if (!this.websocket) {
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
          console.log('websocket close');
        };
      }
    },
    // websocket open event function
    onOpen() {
      const entityIds = [];
      this.subscribeList.map(item => {
        entityIds.push(item.id);
      });
      const data = { subscriptionId: this.subscriptionId, entityIds: entityIds };
      this.websocket.send(JSON.stringify(data));
    },
    // websocket message event function
    // from the server
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

      this.markers = []; // init map marker
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
    // websocket disconnect event function
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
    // get the latest list
    getLatestList() {
      latestApi.fetch('latest')
        .then(data => {
          this.latestList = data;
        });
    },
    // get the latest detail
    getLatest(value) {
      this.subscriptionId = null;
      this.isRemoveBtn = false;
      this.searchCondition = [];
      this.subscriptionCondition = [];
      this.dynamicTags = [];
      this.displayAttribute = {};
      this.subscribeList = [];

      this.initMap();
      this.markers = [];
      this.disconnect();
      this.gridData = [];
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

          const newHPx = document.querySelector(`#${this.elementId}`).parentNode.clientHeight;
          this.$el.querySelector(`#${this.elementId} .vue-map`).style.height = `${newHPx - 70}px`;
          // 세팅 후 검색
          this.getEntitiesIsMap();
        });
    },
    // latest add button click event
    createMap() {
      this.isRemoveBtn = true;
      this.latestName = this.$i18n.t('message.enterTitle');
      this.latestValue = null;
      this.searchCondition = [];
      this.subscriptionCondition = [];
      this.dynamicTags = [];
      this.displayAttribute = {};
      this.subscribeList = [];

      this.initMap();     // 맵 초기화
      this.markers = [];  // 마커 초기화
      this.disconnect();  // 웹소켓 중지
      this.gridData = []; // 엔티티 아이디 목록 초기화
      this.$refs.tuiGrid1.invoke('resetData', this.gridData);
    },
    // Search for search conditions
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
    // latest save(create, modify)
    saveMap() {
      this.$confirm(this.$i18n.t('message.saveCheck'), '', {
        confirmButtonText: 'OK',
        cancelButtonText: 'Cancel',
        type: 'info'
      }).then(() => {
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
    // latest delete event
    removeMap() {
      this.$confirm(this.$i18n.t('message.deleteCheck'), '', {
        confirmButtonText: 'OK',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        latestApi.delete(this.latestValue)
          .then(data => {
            // 목록 호출 뒤 항목 모두 초기화
            this.getLatestList();
            this.createMap();
          });
      }).catch(() => {});
    },
    // dialog popup close event
    handleClose() {
      this.dialogVisible = false;
      this.dialogVisible2 = false;
      this.isSelectDisabled = false;
      this.searchId = null;
      this.addList = [];
      this.selected = null;
      this.treeData = null;
      this.treeRow = null;
      this.treeNode = null;
    },
    // dialog popup save event
    handleSave() {
      // Selected data cannot be added
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

      // 검색어 저장
      this.searchList[this.selected] = this.searchValue;
      // 노출 속성 저장
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
    // grid click event
    onGridClick(event) {
      if (event.targetType !== 'cell') {
        return null;
      }
      // zoom 재설정
      const items = this.$refs.tuiGrid1.invoke('getRow', event.rowKey);
      const locationKey = items['geoproperty_ui'];
      this.$refs.geoMap.$mapPromise.then((map) => {
        const bounds = new window.google.maps.LatLngBounds();
        let loc = null;
        loc = new window.google.maps.LatLng(
          items[locationKey].value.coordinates[1],
          items[locationKey].value.coordinates[0],
        );
        bounds.extend(loc);

        map.fitBounds(bounds);
        map.panToBounds(bounds);
        const zoom = map.getZoom();
        map.setZoom(zoom > 18 ? 18 : zoom);
      });
      this.params = { id: items.id, type: items.type };
    },
    goHistoryView() {
      if (!this.params.id) {
        this.$alert(this.$i18n.t('message.clickItem', [this.$i18n.t('search.entityID')]));
        return null;
      }
      this.$router.push({ path: '/map-search-historical', query: this.params });
    },
    updateCoordinates(location) {
      console.log(location);
    },
    onMarkerClick(map) {
      const REMOVE_KEYS = ['uniqueKey', 'rowKey', 'rowSpanMap', 'sortKey', 'uniqueKey', '_attributes', '_disabledPriority', '_relationListItemMap'];
      const resultMapList = [];
      let resultMapData = {};
      Object.keys(map.mapInfo).filter(key => REMOVE_KEYS.indexOf(key) > -1 || resultMapList.push({ [key]: map.mapInfo[key] }));
      resultMapList.forEach(item => Object.assign(resultMapData, item));

      this.detailData = resultMapData;
      this.detailId = map.mapInfo.id;
      this.dialogVisible2 = true;
      setTimeout(() => {
        document.querySelector('.v-modal').style.display = 'none';
      }, 10);
    },
    onChecked(event) {
      // 모두 체크
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
        confirm: () => this.$alert(this.$i18n.t('message.changeSearchScope'), '', {
          confirmButtonText: this.$i18n.t('comm.ok'),
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
        that.lastShape = e.overlay;
        that.lastShape.type = e.type;
        const shape = that.lastShape.getBounds();
        const ne = shape.getNorthEast();
        const sw = shape.getSouthWest();
        that.coordinates = [[ne.lng(), sw.lat()], [sw.lng(), sw.lat()], [sw.lng(), ne.lat()], [ne.lng(), ne.lat()], [ne.lng(), sw.lat()]];
      });
      this.shiftDraw = false;
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
    getEntitiesIsMap() {
      if (this.dynamicTags.length === 0) {
        return null;
      }

      // 새로 검색일 경우 구독 해제 처리
      if (this.subscriptionId && this.subscribeList.length > 0) {
        this.subscribeList = [];
        this.deleteSubscriptions();
      }

      this.isInfoWindow = false;

      const typeParams = this.setTypeParams('search');
      this.$http.post('/entities/multimodel', typeParams)
        .then(response => {
          const items = response.data;
          const status = response.status;
          if (status === 204) {
            this.$alert(this.$i18n.t('message.noSearchResult'));
            return null;
          }

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

          // 마커 정보 초기화
          this.markers = [];
          const data = [];
          // 마커 정보 세팅
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
              const locationKey = resultItem['geoproperty_ui'];
              this.markers.push({
                position: {
                  lat: resultItem[locationKey].value.coordinates[1],
                  lng: resultItem[locationKey].value.coordinates[0],
                },
                mapInfo: resultItem,
                displayValue: resultItem.displayValue,
                icon: resultItem.icon
              });
              // 원천 데이터에서 아이콘 제거
              delete resultItem.icon;
              delete resultItem.displayValue;
              data.push(resultItem);
            });
          });

          this.$refs.geoMap.$mapPromise.then((map) => {
            // auto focus, auto zoom in out
            const bounds = new window.google.maps.LatLngBounds();
            let loc = null;
            this.markers.map(item => {
              loc = new window.google.maps.LatLng(item.position.lat, item.position.lng);
              bounds.extend(loc);
            });
            map.fitBounds(bounds);
            map.panToBounds(bounds);
            const zoom = map.getZoom();
            map.setZoom(zoom > 12 ? 12 : zoom);
          });
          this.gridData = data;
          this.$refs.tuiGrid1.invoke('resetData', this.gridData);


          // 저장된 정보의 rowKey 를 찾아서 체크박스를 세팅 해줌
          const gridList = [ ...this.$refs.tuiGrid1.invoke('getData') ];
          const subscription = this.subscriptionCondition;
          subscription.forEach(s => {
            gridList.filter(k => k.id === s.id)
              .find(r => this.$refs.tuiGrid1.invoke('check', r.rowKey));
          });

          // 저장되어 있는 경우 자동으로 구독을 설정함
          if (subscription.length > 0) {
            this.goSubscriptions(false);
          }

          setTimeout(() => {
            this.isHistoryBtn = false;
            this.isInfoWindow = true;
          }, 1000);
        });
    }
  },
  mounted() {
    this.getLatest(this.latestId);
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

.vue-map-container,
.vue-map-container .vue-map {
  width: 100%;
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
