<template>
  <WidgetPopup
    isDashboard
    :popup-title="$t('widget.widgetOptions')"
    visible
    :activeName="activeName"
    :chartType="formData['chartType']"
    :visibleChartTree="visibleChartTree"
    :visibleSearchOption="visibleSearchOption"
    @close-event="onClose"
    @tab-click="tabClick"
  >
    <template v-slot:selectBox>
      <form>
        <div class="row">
          <div class="col-md-4">
            <div class="form-group">
              <!--차트 유형-->
              <label class="control-label">{{ $t('widget.widgetType') }}</label>
              <el-select
                class="mr-sm-2"
                v-model="formData['chartType']"
                :placeholder="$t('comm.select')"
                size="small"
                style="width: 100%;"
                @change="onChartTypeChange"
                :disabled="isModify"
              >
                <el-option
                  v-for="item in chartTypes"
                  :key="item.value"
                  :label="item.text"
                  :value="item.value"
                  :disabled="item.disabled"
                >
                </el-option>
              </el-select>
            </div>
          </div>
          <div class="col-md-4" v-if="display['chartType']">
            <div class="form-group">
              <!-- 데이터 모델 ID -->
              <label class="control-label">{{ $t('widget.dataModelID') }}</label>
              <el-select
                class="mr-sm-2"
                v-model="dataModel"
                :placeholder="$t('message.selectOptionEntity')"
                size="small"
                style="width: 100%;"
                @change="onDataModelChange"
                :disabled="dataModelDisabled['dataModelId']"
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
            </div>
          </div>
          <div class="col-md-4" v-if="display['typeUri']">
            <div class="form-group">
              <!-- Type URI-->
              <label class="control-label">{{ $t('widget.typeUri') }}</label>
              <el-select
                class="mr-sm-2"
                v-model="typeUri"
                placeholder="Select"
                size="small"
                style="width: 100%;"
                @change="onTypeUriChange"
                :disabled="dataModelDisabled['typeUri']"
              >
                <el-option
                  v-for="item in typeUris"
                  :key="item.value"
                  :label="item.text"
                  :value="item.value"
                  :disabled="item.disabled"
                >
                </el-option>
              </el-select>
            </div>
          </div>
          <div
            class="col-md-4"
            v-if="display['entityId']"
          >
            <div class="form-group">
              <!-- Entity ID -->
              <label class="control-label">{{ $t('widget.entityInstanceID') }}</label>
              <!-- :multiple="isEntityIdMultiple && (['bar'].indexOf(formData['chartType']) > -1)"-->
              <!-- :multiple="isEntityIdMultiple && !(['text'].indexOf(formData['chartType']) > -1)" -->
              <el-select
                class="mr-sm-2"
                v-model="entityId"
                :multiple="isEntityIdMultiple"
                :collapse-tags="isEntityIdMultiple"
                size="small"
                style="width: 100%;"
                :placeholder="$t('message.selectOptionEntity')"
                @change="onSelectedEntity"
                :disabled="isEntityIdDisabled"
              >
                <el-option
                  v-for="item in entityIds"
                  :key="item.value"
                  :label="item.text"
                  :value="item.value"
                  :disabled="item.disabled"
                >
                </el-option>
              </el-select>
            </div>
          </div>
          <div
            class="col-md-4"
            v-if="display['displayData']"
          >
            <div class="form-group">
              <!--표시 데이터(일부 차트 타입에서만 표시됨)-->
              <label class="control-label">{{ $t('widget.displayData') }}</label>
              <el-select
                class="mr-sm-2"
                v-model="formData['dataType']"
                :placeholder="$t('comm.select')"
                size="small"
                style="width: 100%;"
                @change="onDataTypeChange"
              >
                <el-option
                  v-for="item in dataTypes"
                  :key="item.value"
                  :label="item.text"
                  :value="item.value"
                  :disabled="item.disabled"
                >
                </el-option>
              </el-select>
            </div>
          </div>
          <div
            class="col-md-4"
            v-if="display['chartTitle']"
          >
            <div class="form-group">
              <!--타이틀-->
              <label class="control-label">{{ $t('widget.widgetTitle') }}</label>
              <b-form-input
                id="inline-form-input-name"
                class="mr-sm-2"
                v-model="formData['chartTitle']"
                placeholder="최대 32자"
                maxlength="32"
              />
            </div>
          </div>
          <div
            class="col-md-4"
            v-if="display['yaxisRange']"
          >
            <div class="form-group">
              <!--Y축 범위 (일부 차트에서만 보임)-->
              <label class="control-label">{{ $t('widget.rangeOfYaxis') }}</label>
              <b-form-input
                id="inline-form-input-name"
                class="mr-sm-2"
                v-model="formData['yaxisRange']"
                :placeholder="$t('widget.minMaxAverage')"
              />
            </div>
          </div>
          <div
            class="col-md-4"
            v-if="display['timerel']"
          >
            <div class="form-group">
              <!--시간 조건 (일부 차트에서만 보임)-->
              <label class="control-label">{{ $t('search.timeRelation') }}</label>
              <el-select
                v-model="timerel"
                :placeholder="$t('comm.select')"
                size="small"
                style="width: 100%;"
                @change="ontimerelChange"
              >
                <el-option
                  v-for="item in dateOptions"
                  :key="item.value"
                  :label="item.text"
                  :value="item.value"
                  :disabled="item.disabled"
                >
                </el-option>
              </el-select>
            </div>
          </div>
          <div
            class="col-md-4"
            v-if="display['time']"
          >
            <div class="form-group">
              <!--일시 (일부 차트에서만 보임)-->
              <label class="control-label">{{ $t('widget.time') }}</label>
              <el-date-picker
                v-if="timerel !== 'between'"
                v-model="time"
                type="datetime"
                :placeholder="$t('message.selectDateTime')"
                size="small"
                style="width: 100%;"
                @change="onTimeChange"
              />
              <el-date-picker
                v-else
                v-model="times"
                type="datetimerange"
                size="small"
                :start-placeholder="$t('search.startAt')"
                :end-placeholder="$t('search.endAt')"
                style="width: 100%;"
                :default-time="['00:00:00']"
                @change="onTimeChange"
              />
            </div>
          </div>

          <div
            class="col-md-4"
            v-if="display['realtimeUpdateEnabled']"
          >
            <div class="form-group">
              <!--실시간 갱신 (일부 차트에서만 보임)-->
              <label class="control-label">{{ $t('widget.autoRefresh') }}
              </label>
              <el-select
                v-model="formData['realtimeUpdateEnabled']"
                :placeholder="$t('comm.select')"
                size="small"
                style="width: 100%;"
                :disabled="isRealtimeDisabled"
              >
                <el-option
                  v-for="(item, i) in realTimeOptions"
                  :key="i"
                  :label="item.text"
                  :value="item.value"
                  :disabled="item.disabled"
                >
                </el-option>
              </el-select>
            </div>
          </div>
          <div
            class="col-md-4"
            v-if="display['chartYName']"
          >
            <div class="form-group">
              <!--Y축명 (일부 차트에서만 보임)-->
              <label class="control-label">{{ $t('widget.labelOfYaxis') }}
              </label>
              <input type="text" class="form-control" v-model="formData['chartYName']" placeholder="최대 20자" maxlength="20"/>
            </div>
          </div>
          <div
            class="col-md-4"
            v-if="display['chartXName']"
          >
            <div class="form-group">
              <!--X축명 (일부 차트에서만 보임)-->
              <label class="control-label">{{ $t('widget.labelOfXaxis') }}</label>
              <input type="text" class="form-control" v-model="formData['chartXName']" placeholder="최대 20자" maxlength="20"/>
            </div>
          </div>
          <div
            class="col-md-4"
            v-if="display['updateInterval']"
          >
            <div class="form-group">
              <!--갱신주기 (일부 차트에서만 보임)-->
              <label class="control-label">{{ $t('widget.refreshInterval') }}</label>
              <input
                type="number"
                min="0"
                max="2147483646"
                class="form-control"
                :placeholder="$t('message.ZeroEqualRefresh')"
                v-model="formData['updateInterval']"
              />
            </div>
          </div>
          <div
            class="col-md-4"
            v-if="display['custom_text']"
          >
            <div class="form-group">
              <!--노출 텍스트 (custom Text 차트 타입에서만 보임)-->
              <label class="control-label">{{ $t('widget.textToDisplay') }}</label>
              <input
                type="text"
                class="form-control"
                :placeholder="$t('message.maxTextLength')"
                v-model="formData['extention1']"
                maxlength="100"
              />
            </div>
          </div>
          <div
            class="col-md-4"
            v-if="display['custom_text']"
          >
            <div class="form-group">
              <!--폰트 사이즈 (Custom Text 차트 타입에서만 보임)-->
              <label class="control-label">{{ $t('widget.fontSize') }}</label>
              <el-select
                v-model="formData['extention2']"
                :placeholder="$t('comm.select')"
                size="small"
                style="width: 100%;"
              >
                <el-option
                  v-for="item in textSizes"
                  :key="item.value"
                  :label="item.text"
                  :value="item.value"
                  :disabled="item.disabled"
                >
                </el-option>
              </el-select>
            </div>
          </div>
          <div
            class="col-md-8"
            v-if="display['image']"
          >
            <div class="form-group">
              <!--이미지 파일 (Image 차트 타입에서만 보임)-->
              <label class="control-label">{{ $t('widget.imageFile') }}</label>
              <div>
                <el-upload
                  ref="upload"
                  action="" :auto-upload="false"
                  :file-list="imageFiles"
                  accept="image/jpeg,image/jpg,image/png"
                  :limit="1" :on-exceed="handleExceed"
                  :on-change="handleImageChange">
                  <el-button size="small" type="primary">{{ $t('widget.selectFile') }}</el-button>
                  <div slot="tip" class="el-upload__tip">{{ $t('message.fileMaxDescription') }}</div>
                </el-upload>
              </div>
            </div>
          </div>
          <div
            class="col-md-4"
            v-if="display['latestMap']"
          >
            <div class="form-group">
              <!--사용자 지도 (Latest Map 차트 타입에서만 보임)-->
              <label class="control-label">{{ $t('widget.userMapLatestData') }}</label>
              <div>
                <el-select
                  v-model="formData['mapSearchConditionId']"
                  filterable
                  :placeholder="$t('message.selectMap')"
                  size="small"
                  style="margin-right: 10px;"
                  @change="getLatest"
                >
                  <el-option
                    v-for="item in latestMaps"
                    :key="item.mapSearchConditionId"
                    :label="item.mapSearchConditionName"
                    :value="item.mapSearchConditionId"
                  >
                  </el-option>
                </el-select>
              </div>
            </div>
          </div>
        </div>
      </form>
    </template>
    <template v-slot:chartTree>
      <ElementTree
        :treeData="treeData"
        @on-tree-event="onChartClick"
        nodeKey="chart"
        :checkList="{}"
        :option="visibleTreeOption"
        :optionFiltering="formData.chartType === 'scatter'"
        @popover-show="chartOptRadio = null"
      >
        <!-- Line / Bar 차트 타입일 때 차트 값에서 필드 선택 시 뜨는 팝업-->
        <template v-slot:popover-content="{show, node}">
          <el-radio-group v-model="chartOptRadio" @change="() => onChartOptChange(show, node)">
            <el-radio :label="1" v-if="formData['chartType'] === 'scatter'">{{ $t('widget.XaxisSetting') }}</el-radio>
            <el-radio :label="2" v-if="formData['chartType'] === 'scatter'">{{ $t('widget.YaxisSetting') }}</el-radio>
            <el-radio :label="3" v-if="isLineOrBarChartType">{{ $t('widget.selectedAttribute') }} {{ $t('comm.setting') }}</el-radio>
            <el-radio :label="4" v-if="isLineOrBarChartType">{{ legendText }} {{ $t('comm.setting') }}</el-radio>
          </el-radio-group>
        </template>
      </ElementTree>
    </template>
    <template v-slot:addAttr>
      <div v-if="!display['chartAttributeXY']" class="mt-2">
        <label class="control-label mb-2">{{ $t('widget.selectedAttribute') }}</label>
        <input
          type="text"
          class="form-control"
          v-model="selectedAttrsText"
          disabled
        />
      </div>
      <div v-if="display['legendDisplay']" class="mt-2">
        <label class="control-label mb-2">{{ legendText }}</label>
        <div>
          <input
            type="text"
            class="form-control legend-display mr-2"
            v-model="legendDisplay"
            disabled
          />
          <el-button size="small" type="primary" @click="legendDisplay = 'ID'">{{ $t('comm.reset') }}</el-button>
        </div>
      </div>
      <div v-if="display['chartUnit']">
        <label class="control-label mb-2">{{ $t('widget.chartUnit') }}
        </label>
        <input
          type="number"
          class="form-control"
          v-model="chartUnit"
          :disabled="isChartAttributeString"
        />
      </div>
      <div v-if="display['chartAttributeXY']" class="mt-2">
        <label class="control-label mb-2">{{ $t('widget.valueOfXaxis') }}</label>
        <input
          type="text"
          class="form-control legend-display ml-2"
          v-model="attrsXText"
          disabled
        />
      </div>
      <div v-if="display['chartAttributeXY']" class="mt-2">
        <label class="control-label mb-2">{{ $t('widget.valueOfYaxis') }}</label>
        <input
          type="text"
          class="form-control legend-display ml-2"
          v-model="attrsYText"
          disabled
        />
      </div>
    </template>
    <template v-slot:tree>
      <ElementTree
        :treeData="treeData"
        @on-tree-event="onTreeEvent"
        nodeKey="query"
        :checkList="dynamicQuery"
      >
      </ElementTree>
    </template>
    <template v-slot:addQuery>
      <b-form inline class="mb-4">
        <label class="mr-sm-2">ID</label>
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
      <DynamicSearch v-for="(map, index) in addList" :key="index" :formData="map" :index="index" @remove="searchRemove"/>
    </template>
    <template v-slot:buttonGroup>
      <el-popover
        v-if="isModify"
        placement="top"
        width="200"
        v-model="messageVisible">
        <p style="font-size: 12px;">
          {{ $t('message.deleteCheck') }}
        </p>
        <div style="text-align: right; margin: 0">
          <el-button size="mini" type="" @click="messageVisible = false">{{ $t('comm.cancel') }}</el-button>
          <el-button type="primary" size="mini" @click="handleRemove">{{ $t('comm.ok') }}</el-button>
        </div>
        <el-button slot="reference" class="mr-2" type="danger" size="small">{{ $t('widget.deleteWidget') }}</el-button>
      </el-popover>
      <!-- <el-button class="ml-1" type="primary" @click="widgetSave" size="small" :disabled="entityIds.length < 1" >{{ $t('comm.save') }}</el-button> -->
      <el-button class="ml-1" type="primary" @click="widgetSave" size="small">{{ $t('comm.save') }}</el-button>
    </template>
  </WidgetPopup>
</template>

<script>
/**
 * Dashboard Add/Edit Widget Popup UI
 * Components used to register or modify dashboard widgets
 *
 * @component WidgetPopup, DynamicSearch, ElementTree
 * - element-ui
 * @props props { ... }
 * @state data() { ... }
 */
import WidgetPopup from '@/components/WidgetPopup';
import ElementTree from '@/components/ElementTree';
import DynamicSearch from '@/components/DynamicSearch';
import {typeUriApi, dataModelApi, widgetApi, latestApi} from '@/moudules/apis';

import {
  chartOptions,
  barChartOptions,
  lineChartOptions
} from '@/components/Chart/Dataset';

export default {
  name: "AddWidgetPopup",
  components: {
    WidgetPopup,
    DynamicSearch,
    ElementTree
  },
  props: {
    layout: Array,
    dashboardId: String,
    editItem: Object,
  },
  computed: {
    index() {
      return this.layout.length;
    },
    visibleChartTree() {
      const chartType = this.formData['chartType'];
      return chartType && !(chartType === 'custom_text' || chartType === 'Image' || chartType === 'map_latest');
    },
    visibleSearchOption() {
      const {chartType, dataType} = this.formData;
      return ['pie', 'donut'].indexOf(chartType) >= 0 || (chartType === 'histogram' && dataType === 'last') || (chartType === 'scatter' && dataType === 'history');
    },
    isChartAttributeString() {
      const chartAttribute = this.validation.chartAttribute;
      if (!chartAttribute) return false;
      return !(chartAttribute === 'Double' || chartAttribute === 'Integer');
    },
    isEntityIdMultiple() {
      // const {chartType} = this.formData;  // 차트 타입 할당
      // if (chartType) return false;
      if (this.isLineOrBarChartType) return true;
      return this.formData['dataType'] === 'last';
    },
    isEntityIdDisabled() {
      const {chartType, dataType} = this.formData;
      return chartType === 'histogram' && dataType === 'last';
    },
    isLineOrBarChartType() {
      const {chartType} = this.formData;
      return ['line', 'bar'].indexOf(chartType) >= 0
    },
    visibleTreeOption() {
      return this.isLineOrBarChartType || this.formData.chartType === 'scatter';
    },
    treeOptionLegendText() {
      const {chartType} = this.formData;
      return ['line', 'scatter'].indexOf(chartType) >= 0 ? this.$i18n.t('widget.legendDisplay') : this.$i18n.t('widget.XaxisDisplay');
    },
    legendText() {
      const {chartType, dataType} = this.formData;
      return chartType === 'bar' && dataType === 'last' ? this.$i18n.t('widget.XaxisDisplay') : this.$i18n.t('widget.legendDisplay');
    },
    selectedAttrsText() {
      if (this.formData['chartType'] === 'scatter') {
        let text = null;
        if (this.attrsXText.length > 0) text = this.attrsXText;
        if (this.attrsYText.length > 0) text = `${text}, ${this.attrsYText}`;
        return text || '';
      }
      return this.formData['chartAttribute'];
    },
    attrsXText() {
      return this.attrs.x && Object.hasOwn(this.attrs.x, 'fullId') ? this.attrs.x.fullId : '';
    },
    attrsYText() {
      return this.attrs.y && Object.hasOwn(this.attrs.y, 'fullId') ? this.attrs.y.fullId : '';
    }
  },
  data() {
    return {
      valueData: [],
      isEditImage: false,
      messageVisible: false,
      time: null,
      times: [],
      timerel: null,
      entityIds: [], // DummyData
      entityId: null,
      typeUri: null,
      typeUris: [],
      dynamicQuery: {},
      dataModels: [],
      addList: [],
      searchId: null,
      activeName: 'first',
      dataModel: null,
      isRealtimeDisabled: false,
      isModify: false,
      treeData: [],
      imageFiles: [],
      imageFile: null,
      latestMaps: [],
      latestValue: null,
      chartUnit: null,
      chartOptRadio: null,
      legendDisplay: 'ID', // default ID
      attrs: {
        x: null, y: null,
      },
      display: {
        chartType: false, chartTitle: false, chartXName: false, chartYName: false, updateInterval: false,
        realtimeUpdateEnabled: false, timerel: false, entityId: false, displayData: false, time: false,
        yaxisRange: false, custom_text: false, image: false, latestMap: false, chartAttributeXY: false,
      },
      dataTypes: [
        {value: null, text: this.$i18n.t('message.selectOption'), disabled: true},
        {value: 'history', text: this.$i18n.t('widget.displayHistorical'), disabled: false},
        {value: 'last', text: this.$i18n.t('widget.displayLatest'), disabled: false}
      ],
      dataModelDisabled: {dataModelId: false, typeUri: false},
      formData: {
        dashboardId: null,
        widgetId: null, chartType: null, chartOrder: null, chartSize: null, dataType: null, chartTitle: null,
        updateInterval: null, realtimeUpdateEnabled: false, yaxisRange: null,
        chartAttribute: null, chartXName: null, chartYName: null, realTimeOption: null, q: null,
        extention1: null, extention2: null, image: null
      },
      validation: {
        chartAttribute: null,
      },
      chartTypes: [
        {value: null, text: this.$i18n.t('message.selectOption'), disabled: true},
        {value: 'donut', text: 'Donut', disabled: false},
        {value: 'bar', text: 'Bar', disabled: false},
        {value: 'pie', text: 'Pie', disabled: false},
        {value: 'line', text: 'Line', disabled: false},
        {value: 'text', text: 'Text', disabled: false},
        {value: 'boolean', text: 'Boolean', disabled: false},
        {value: 'custom_text', text: 'Text(Custom)', disabled: false},
        {value: 'Image', text: 'Image', disabled: false},
        {value: 'map_latest', text: 'Latest Map', disabled: false},
        {value: 'histogram', text: 'Histogram', disabled: false},
        {value: 'scatter', text: 'Scatter', disabled: false},
      ],
      dateOptions: [
        {value: null, text: this.$i18n.t('message.selectOption'), disabled: true},
        {value: 'before', text: this.$i18n.t('search.before')},
        {value: 'after', text: this.$i18n.t('search.after')},
        {value: 'between', text: this.$i18n.t('search.between')}
      ],
      realTimeOptions: [
        {value: null, text: this.$i18n.t('message.selectOption'), disabled: true},
        {value: true, text: this.$i18n.t('comm.yes')},
        {value: false, text: this.$i18n.t('comm.no')},
      ],
      textSizes: [
        {value: null, text: this.$i18n.t('message.selectOption'), disabled: true},
        {value: '10px', text: '10px'},
        {value: '20px', text: '20px'},
        {value: '30px', text: '30px'},
        {value: '40px', text: '40px'},
      ]
    }
  },
  mounted() {
    this.isEditImage = false;
    // The status should be set to true/false values only.
    this.isModify = this.editItem && Object.keys(this.editItem).length > 0;
    // Get widget information.
    if (this.isModify) {
      this.dataModelDisabled = {dataModelId: true, typeUri: true};
      const {widgetId} = this.editItem;
      this.getWidgetInfo(widgetId);
    }

    this.formData['dashboardId'] = this.dashboardId;
    this.getDataModelList();
    this.getTypUriList();
  },
  methods: {
    searchRemove(id, index) {
      const tempTree = [...this.treeData];
      this.addList.splice(index, 1);
      if (Object.keys(this.dynamicQuery).length > 0) {
        delete this.dynamicQuery[id];
      }
      this.treeData = tempTree;
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
    handleExceed() {
      this.$message.warning(this.$i18n.t('message.fileExist'));
    },
    handleImageChange(file, fileList) {
      const isLt10M = file.size / 1024 / 1024 < 10;

      if (!isLt10M) {
        this.$message.error('message.fileMaxDescription');
        this.imageFiles = [];
        this.imageFile = null;
      } else this.imageFile = file.raw;
    },
    handleDynamicSearchSave() {
      const tempTree = [...this.treeData];
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
    tabClick(tab, event, active) {
      this.activeName = active;
    },
  async  getEntityList(dataModelId, typeUri) {
      try {
        const { status, data: items } = await this.$http.post('/entityIds', {dataModelId, typeUri});

        if (status === 204) {
          this.resetData();
          return;
        }

        this.processEntityIds(items);
        const hasEntityIds = this.entityIds.length > 0;

        if (hasEntityIds) {
          const data = await dataModelApi.attributes({dataModelId, typeUri});
          this.treeData = this.processTreeData(data);

          let chartAttribute = this.formData['chartAttribute'];

          const deepFind = (obj, key, value) => {
            if (!obj || typeof obj !== 'object') return null;
            if (obj[key] === value) return obj;

            for (let i in obj) {
              if (obj.hasOwnProperty(i)){
                let found = deepFind(obj[i], key, value);
                if (found) return found;
              }
            }

            return null;
          }

          let matchedObject = this.valueData.filter(value => typeof value === 'object').map(value => deepFind(value, 'fullId', chartAttribute)).filter(Boolean)[0];

          if (matchedObject) {
            // Matched object를 이용해 formData와 validation 속성을 업데이트
            this.formData.chartAttribute = matchedObject.fullId;
            this.validation.chartAttribute = matchedObject.valueType;
            this.chartUnit = null;
          }
        }
      } catch (error) {
        console.error('Error in getEntityList:', error);
      }
    },
    resetData() {
      this.entityIds = [];
      this.treeData = [];
    },
    processEntityIds(items) {
      const result = items.map(item => ({value: item, text: item, disabled: false}));
      result.unshift({value: '', text: 'all', disabled: false});
      this.entityIds = result;

      const {dataType, chartType} = this.formData;
      this.entityIds.at(0).disabled = dataType === 'history' && ['scatter', 'histogram'].includes(chartType);
    },
    setSearchableToFalse(valueData) {
      // 각 속성의 type값 추출
      // console.log(valueData);
      this.valueData.push(valueData);

      const chartTypeRestrictions = {
        donut: ['Integer', 'Double'],
        bar: ['Integer', 'Double'],
        pie: ['Integer', 'Double'],
        line: ['Integer', 'Double'],
        boolean: ['Boolean'],
        histogram: ['Integer'],
        scatter: ['Integer', 'Double']
      };
      const allowedTypes = chartTypeRestrictions[this.formData.chartType];

      if (typeof valueData === 'object' && valueData !== null) {
        if ('valueType' in valueData && allowedTypes && !allowedTypes.includes(valueData.valueType)) {
          valueData.searchable = false;
        }
        for (let key in valueData) {
          if (typeof valueData[key] === 'object' && valueData[key] !== null) {
            this.setSearchableToFalse(valueData[key]);
          }
        }
      }
    },
    processTreeData(data) {
      data.forEach(this.setSearchableToFalse);
      return data;
    },
    getAttributed() {
      dataModelApi.attributes(value)
        .then(data => {
          this.treeData = data;
        });
    },
    getDataModelList() {
      dataModelApi.fetch()
        .then(data => {
          let result = [{value: null, text: this.$i18n.t('message.selectOption'), disabled: true}];
          data.map(item => {
            return result.push({value: item, text: item, disabled: false});
          });
          this.dataModels = result;
        });
    },
    getTypUriList() {
      typeUriApi.fetch()
        .then(data => {
          let result = [{value: null, text: this.$i18n.t('message.selectOption'), disabled: true}];
          data.map(item => {
            return result.push({value: item, text: item, disabled: false});
          });
          this.typeUris = result;
        });
    },
    // 2.0 dashboard latest map list
    getLatestList() {
      latestApi.fetch('latest')
        .then(data => {
          this.latestMaps = data;
        });
    },
    // 2.0 dashboard Call additional registered late map details
    getLatest(value) {
      latestApi.detail(value)
        .then(data => {
          this.formData.mapSearchConditionId = value;
        });
    },
    imageToFile(bstr, name) {
      let n = bstr.length;
      let u8arr = new Uint8Array(n);

      while (n--) {
        u8arr[n] = bstr.charCodeAt(n);
      }

      let file = new File([u8arr], name, {type: 'mime'});
      this.imageFile = file;
      this.imageFiles = [file];
    },
    getWidgetInfo(widgetId) {
      this.dynamicQuery = {};

      widgetApi.fetch(this.dashboardId, widgetId)
        .then(data => {
          const {chartType, entityRetrieveVO, file, extention1, extention2, dataType, chartAttribute} = data;
          this.formData = data;
          // console.error("getWidgetInfo "+this.formData.entityRetrieveVO.type);
          if (this.formData['chartAttribute']) {
            this.valueData.push(this.formData['chartAttribute']);
          }

          if (chartType === 'histogram') {
            this.chartUnit = extention1;
          }

          if (chartType === 'Image') {
            this.imageFile = new Blob([file]);
            this.imageToFile(file, extention1);
          }

          if (entityRetrieveVO) {
            this.dataModel = entityRetrieveVO.dataModelId;
            this.typeUri = entityRetrieveVO.typeUri;
            this.timerel = entityRetrieveVO.timerel;

            if (entityRetrieveVO.timerel === 'between') {
              // date format
              // new Date('2022-03-04T15:00:00.000Z')
              this.times = [new Date(entityRetrieveVO.time), new Date(entityRetrieveVO.endtime)];
            } else {
              this.time = entityRetrieveVO.time ? new Date(entityRetrieveVO.time) : null;
            }

            if (['line', 'bar', 'donut', 'pie', 'text'].indexOf(chartType) > -1) {
              this.entityId = entityRetrieveVO.id.split(',');
            } else if (['scatter'].indexOf(chartType) > -1) {
              this.entityId = dataType === 'history' ? entityRetrieveVO.id : entityRetrieveVO.id.split(',');
            } else {
              this.entityId = entityRetrieveVO.id; // histogram...
            }

            if (this.dataModel) this.onDataModelChange(this.dataModel);
            if (this.typeUri) this.onTypeUriChange(this.typeUri);

            if (entityRetrieveVO.attrs.length > 1 && chartAttribute) {
              const ids = chartAttribute.split(', ');
              this.attrs.x = {
                fullId: entityRetrieveVO.attrs[0],
                id: ids[0],
                valueType: 'NUMBER'
              };
              this.attrs.y = {
                fullId: entityRetrieveVO.attrs[1],
                id: ids[1],
                valueType: 'NUMBER'
              };
            }

            if (extention1 === 'legend') this.legendDisplay = extention2;

            // Changing the data type to set detailed search conditions.
            if (entityRetrieveVO.q) {
              const query = [...entityRetrieveVO.q];
              query.map((item, i) => {
                const key = item.attr;
                if (item.condition === ';') {
                  item.temp = 'AND';
                } else if (i === 0) {
                  item.temp = null; // first Option
                } else {
                  item.temp = 'OR';
                }
                if (this.dynamicQuery[key]) {
                  this.dynamicQuery[key].push(item);
                } else {
                  this.dynamicQuery[key] = [item];
                }
              });
            }
          }

          this.onChartTypeChange(chartType, 1);
        });
    },
    onTreeEvent(data, node) {
      this.searchId = data.id;
      this.treeId = data.fullId;
      this.treeRow = data;
      this.treeNode = node;
      this.addList = [];
      Object.keys(this.dynamicQuery).some(key => {
        if (key === data.fullId) {
          this.dynamicQuery[key].map(_ => _.valueType = data.valueType);
          this.addList = this.dynamicQuery[key];
        }
      });
    },
    onDataTypeChange(value) {
      this.entityId = null;
      const {dataType, chartType} = this.formData;
      if (this.entityIds.length > 0) {
        this.entityIds.at(0).disabled = dataType === 'history' && (['scatter', 'histogram'].indexOf(chartType) >= 0);
      }
      if (chartType === 'histogram') {
        this.chartUnit = null;
        this.formData['chartAttribute'] = null;
        const displayOption = {
          chartType: true,
          typeUri: true,
          entityId: true,
          displayData: true,
          chartTitle: true,
          chartUnit: true,
        };
        if (value === 'history') {
          displayOption.time = true;
          displayOption.timerel = true;
          this.dynamicQuery = {};
        }
        this.initDisplay(displayOption);
      }
    },
    async onTypeUriChange(value) {
      this.attrs = {x: null, y: null};
      if (!this.dataModelDisabled['typeUri']) {
        this.entityId = null
      }
      await this.getEntityList(null, value);

      this.dataModelDisabled = {dataModelId: true, typeUri: false};
    },
    ontimerelChange(value) {
      this.time = null;
      this.isRealtimeDisabled = value !== 'after';
    },
    onTimeChange(value) {
      console.log(value);
    },
    async onDataModelChange(value) {
      this.attrs = {x: null, y: null};
      if (!this.dataModelDisabled['dataModelId']) {
        this.entityId = null
      }
      await this.getEntityList(value, null);

      this.dataModelDisabled = {dataModelId: this.isModify, typeUri: true};
    },
    onSelectedEntity(array) {
      if (this.isEntityIdMultiple) {
        if (array.indexOf('') >= 0) {
          this.entityId = this.entityIds.filter(_ => _.value !== '').map(item => item.value);
        } else {
          this.entityId = array;
        }
      }
    },
    // Line이랑 Bar 제외 나머지에서 차트 값 선택 시
    onChartClick(data, node) {
      if (!this.visibleTreeOption) {
        const {fullId, valueType} = data;
        this.formData.chartAttribute = fullId;
        this.validation.chartAttribute = valueType;
        this.chartUnit = null;
      }
    },
    // Line이랑 Bar에서 차트 값 선택 시
    onChartOptChange(show, node) {
      if (show && node) {
        switch (this.chartOptRadio) {
          case 1: this.attrs.x = node.data; break;
          case 2: this.attrs.y = node.data; break;
          case 3: this.formData['chartAttribute'] = node.data.fullId;
                  this.validation.chartAttribute = node.data.valueType;
                  break;
          case 4: this.legendDisplay = node.data.fullId; break;
        }
      }
    },
    onChartTypeChange(value, type) {
      // Form information exposed according to chart type.
      this.display['chartType'] = !(value === 'custom_text' || value === 'Image' || value === 'latestMap');

      // Data to be reset.
      if (!type) {
        this.formData['chartAttribute'] = null;
        this.validation['chartAttribute'] = null;
        this.dataModel = null;
        this.entityId = null;
        this.typeUri = null;
        // this.typeUris = [];
        this.dataModelDisabled = {dataModelId: false, typeUri: false};
        this.treeData = [];
        this.attrs = {x: null, y: null};
      }

      // Display setting according to chart type.
      switch (value) {
        case 'donut' :
          this.formData.dataType = 'last';
          this.initDisplay({
            chartType: true,
            chartTitle: true,
            updateInterval: true,
            entityId: true,
            typeUri: true,
          });
          break;
        case 'bar' :
          this.formData.dataType = 'last';
          this.initDisplay({
            chartType: true,
            chartTitle: true,
            chartXName: true,
            chartYName: true,
            displayData: true,
            realtimeUpdateEnabled: true,
            timerel: true,
            entityId: true,
            time: true,
            yaxisRange: true,
            typeUri: true,
            legendDisplay: true,
          });
          break;
        case 'pie' :
          this.formData.dataType = 'last';
          this.initDisplay({
            chartType: true,
            chartTitle: true,
            updateInterval: true,
            entityId: true,
            typeUri: true,
          });
          break;
        case 'line' :
          this.formData.dataType = 'history';
          this.initDisplay({
            chartType: true,
            chartTitle: true,
            chartXName: true,
            chartYName: true,
            displayData: true,
            realtimeUpdateEnabled: true,
            timerel: true,
            entityId: true,
            time: true,
            yaxisRange: true,
            typeUri: true,
            legendDisplay: true,
          });
          break;
        case 'text' :
          this.initDisplay({
            chartType: true,
            chartTitle: true,
            realtimeUpdateEnabled: true,
            entityId: true,
          });
          break;
        case 'boolean' :
          this.initDisplay({
            chartType: true,
            chartTitle: true,
            realtimeUpdateEnabled: true,
            entityId: true,
          });
          break;
        case 'custom_text' :
          this.initDisplay({
            custom_text: true,
            chartTitle: true,
          });
          break;
        case 'Image' :
          this.initDisplay({
            image: true,
            chartTitle: true,
          });
          break;
        case 'map_latest' :
          this.initDisplay({
            latestMap: true,
            chartTitle: true
          });
          // Call the list of stored latest maps
          this.getLatestList();
          break;
        case 'histogram' :
          this.formData.dataType = this.formData.dataType || 'history';
          this.initDisplay({
            chartType: true,
            typeUri: true,
            timerel: true,
            entityId: true,
            displayData: true,
            chartTitle: true,
            time: true,
            chartUnit: true,
          });
          break;
        case 'scatter' :
          this.formData.dataType = this.formData.dataType || 'history';
          this.initDisplay({
            chartType: true,
            typeUri: true,
            entityId: true,
            displayData: true,
            chartTitle: true,
            chartAttributeXY: true,
            timerel: true,
            time: true,
          });
          break;
      }
    },
    handleRemove() {
      const {widgetId} = this.editItem;
      const chartType = this.formData['chartType'];
      this.messageVisible = false;

      // delete logic
      widgetApi.delete(this.dashboardId, widgetId)
        .then(() => {
          const {widgetId, userId} = this.formData;
          if (!this.isEditImage) {
            this.$emit('remove', {chartType, widgetId, userId});
            this.onClose();
          }
        });
    },
    async editImageWidget() {
      await this.handleRemove();
      this.isModify = false;
      this.isEditImage = true;
      this.widgetSave(true);
    },
    widgetSave() {
      // console.error(this.formData.entityRetrieveVO);
      const query = [];
      const chartType = this.formData['chartType'];

      // start exceptions

      // 1. Data value selection limitation based on chart type
      const chartTypeRestrictions = {
        donut: ['Integer', 'Double'],
        bar: ['Integer', 'Double'],
        pie: ['Integer', 'Double'],
        line: ['Integer', 'Double'],
        boolean: ['Boolean'],
        histogram: ['Integer'],
        scatter: ['Integer', 'Double']
      };

      const allowedTypes = chartTypeRestrictions[chartType];

      if (!this.formData['chartAttribute']){
        if (allowedTypes && !allowedTypes.includes(this.validation.chartAttribute)) {
          const allowedTypesMessage = allowedTypes.join(', ');
          this.$alert(`${this.$i18n.t('message.notSupportType')} ${chartType}: ${allowedTypesMessage}]`);
          return;
        }
      }


      // 2. Required verification of entity ID.
      if (!this.isEntityIdDisabled && this.display['entityId'] && this.entityId === null) {
        this.$alert(this.$i18n.t('message.selectDataEntityIds'));
        return;
      }

      // 3. Required verification of X, Y axis at Scatter
      if (chartType === 'scatter' && !(this.attrs.x && this.attrs.y)) {
        this.$alert(this.$i18n.t('message.selectXandYaxis'));
        return;
      }

      // 3-1. Number value only can be selected for scatter widgets.
      const isNumberType = (data) => (['Integer', 'Double'].indexOf(data.valueType) < 0);
      if (chartType === 'scatter' && (isNumberType(this.attrs.x) || isNumberType(this.attrs.y))) {
        this.$alert(this.$i18n.t('message.onlySupportNumberType'));
        return;
      }

      // 4. Required verification of chartAttribute
      if (this.display['chartType'] && chartType !== 'scatter' && !this.formData.chartAttribute) {
        this.$alert(this.$i18n.t('message.selectChartAttribute'));
        return;
      }

      // 5. Required verification of imageFile
      if (!this.isEditImage && this.isModify && chartType === 'Image') {
        this.editImageWidget();
        return;
      }

      // 6. Required verification of time for Line, Bar Widgets
      if (this.isLineOrBarChartType && (this.entityId || []).length > 1 && !(this.time && this.timerel)) {
        this.$alert(this.$i18n.t('message.selectTimeAndRel'));
        return;
      }

      if (chartType === 'pie' || chartType === 'donut' || chartType === 'histogram') {
        Object.keys(this.dynamicQuery).map(key => {
          this.dynamicQuery[key].forEach(item => {
            query.push(item);
          });
        });
      }

      if (this.display['chartType']) {
        const entityId = this.entityId ? this.entityId.toString() : this.entityId;

        this.formData.entityRetrieveVO = {
          dataModelId: this.dataModel,
          typeUri: this.typeUri,
          attrs: chartType === 'scatter' ? [this.attrs.x.fullId, this.attrs.y.fullId] : [this.formData['chartAttribute']],
          id: entityId,
          timerel: this.timerel,
          time: this.timerel === 'between' ? this.times.at(0) : this.time,
          endtime: this.timerel === 'between' ? this.times.at(1) : null,
          timeproperty: this.time && this.timerel === 'after' ? 'modifiedAt' : null,
          q: query
        };
      } else {
        this.formData.entityRetrieveVO = null;
      }

      let chartTypesNotToVerify = ['Image', 'text', 'custom_text', 'boolean', 'map_latest', 'histogram'];

      if (!chartTypesNotToVerify.includes(chartType)) {
        // Code for limiting the number of entities
        if (this.entityId.length > 0) {
          const entityIdCount = this.entityId.join().split(',').length;

          if (this.entityId.length > 1000) {
            this.$alert(this.$i18n.t('message.MaximumLengthOfEntities'));
            return;
          }
          // For 'Text' type, don't join and calculate count.
          if (chartType !== 'text' && entityIdCount > 10) {
            this.$alert(this.$i18n.t('message.MaximumNumberOfEntities'));
            return;
          }
        }
      }

      // text 위젯의 경우 1개의 엔티티만 허용
      if (chartType === 'text') {
        if (JSON.stringify(this.entityId).length < 1000 && JSON.stringify(this.entityId).indexOf(',') !== -1) {
          this.$alert(this.$i18n.t('message.OnlyOneEntityForTextType'));
          return;
        }
      }

      // api updateInterval default 0 setting
      // (Error occurs when requesting deletion of widget websocket.)
      if (!this.formData.updateInterval) {
        this.formData.updateInterval = 0;
      }

      if (chartType === 'custom_text') {
        this.formData.dataType = null;
        this.formData.chartAttribute = 'custom_text';
      } else if (chartType === 'Image') {
        this.formData.chartAttribute = 'Image';
        this.formData.extention1 = this.imageFile.name;
      } else if (chartType === 'map_latest') {
        this.formData.chartAttribute = 'map_latest';
      } else if (chartType === 'histogram') {
        this.formData.extention1 = this.chartUnit;
        this.formData.extention2 = this.validation.chartAttribute;
      } else if (chartType === 'scatter') {
        this.formData.chartAttribute = `${this.attrs.x.id}, ${this.attrs.y.id}`;
      }

      if (this.visibleTreeOption) {
        if (this.legendDisplay !== 'ID') { // not default Legend
          this.formData.extention1 = 'legend';
          this.formData.extention2 = this.legendDisplay;
        } else {
          this.formData.extention1 = null;
          this.formData.extention2 = null;
        }
      }

      // Save the chart size location.
      if (this.isModify || this.isEditImage) {
        const {x, y, w, h, i} = this.editItem;
        this.formData.chartSize = JSON.stringify({
          x, y, w, h, i
        });
      } else {
        this.formData.chartSize = JSON.stringify({
          x: (this.layout.length * 2) % (this.colNum || 12),
          y: this.layout.length + (this.colNum || 12),
          w: 2,
          h: 4,
          i: this.index
        });
        this.formData.chartOrder = this.index;
      }

      if (chartType === 'line') { // line is only history
        this.formData.dataType = 'history';
      }

      // add modify logic
      if (this.isModify) {
        widgetApi.modify(this.formData)
          .then((data) => {
            const {
              chartSize,
              chartType,
              chartTitle,
              chartXName,
              chartYName,
              yaxisRange,
              userId,
              widgetId,
              mapSearchConditionId
            } = data;
            const {x, y, w, h, i} = JSON.parse(chartSize);
            const item = {
              userId, widgetId,
              x, y, w, h, i,
              chartType,
              title: chartTitle,
              chartXName, chartYName, yaxisRange,
            }
            if (item.chartType === 'bar') {
              item.options = barChartOptions(item);
            } else if (this.formData.chartType === 'line') {
              item.options = lineChartOptions(item);
            } else if (this.formData.chartType === 'custom_text') {
              item.data = {
                extention1: this.formData.extention1,
                extention2: this.formData.extention2
              };
            } else if (this.formData.chartType === 'map_latest') {
              item.mapSearchConditionId = mapSearchConditionId;
            } else if (chartType === 'histogram') {
              item.chartUnit = this.formData.extention1;
              item.valueType = this.formData.extention2;
            } else {
              item.options = chartOptions(item);
            }
            this.$emit('edit', item);
            this.onClose();
          });
        return null;
      }

      if (['line', 'bar', 'custom_text', 'histogram', 'scatter'].indexOf(chartType) < 0) {
        this.formData.dataType = 'last';
      }

      // add register logic
      const file = this.imageFile;
      widgetApi.create(this.formData, file)
        .then(data => {
          const chartSize = JSON.parse(this.formData.chartSize);
          const {widgetId, userId} = data;
          const {chartType, chartTitle, chartXName, chartYName, yaxisRange, mapSearchConditionId} = this.formData;
          let item = {
            widgetId, userId,
            x: chartSize.x,
            y: chartSize.y,
            w: 2, h: 4, i: this.index,
            chartType: chartType,
            title: chartTitle,
            chartXName,
            chartYName,
            yaxisRange,
            data: null,
            options: null,
            mapSearchConditionId
          }
          if (this.isEditImage) {
            const {x, y, w, h, i} = this.editItem;
            item = {
              ...item, x, y, w, h, i
            }
          }

          if (chartType === 'bar') {
            item.options = barChartOptions(item);
          } else if (chartType === 'line') {
            item.options = lineChartOptions(item);
          } else if (chartType === 'custom_text') {
            item.data = {
              extention1: this.formData.extention1,
              extention2: this.formData.extention2
            };
          } else if (chartType === 'Image') {
            item.data = {
              file: URL.createObjectURL(file)
            }
          } else if (chartType === 'histogram') {
            item.chartUnit = this.formData.extention1;
            item.valueType = this.formData.extention2;
          } else {
            item.options = chartOptions(item);
          }

          if (this.isEditImage && chartType === 'Image') {
            this.$emit('edit', item);
          } else {
            this.$emit('add', item);
          }

          // save and close
          this.onClose();
        });
    },
    async onClose() {
      this.valueData = [];
      this.isEditImage = false;
      this.dataModel = null;
      this.treeData = [];
      this.chartTitle = '';
      this.typeUri = null;
      this.timerel = null;
      this.time = null;
      this.activeName = 'first';
      this.entityId = null;
      this.entityIds = [];
      this.typeUris = [];
      this.dataModels = [];
      this.imageFiles = [];
      this.imageFile = null;
      this.chartUnit = null;
      this.attrs = {x: null, y: null};

      this.formData = {
        dashboardId: null,
        widgetId: null, chartType: null, chartOrder: null, chartSize: null, dataType: null, chartTitle: null,
        updateInterval: null, realtimeUpdateEnabled: false, chartAttribute: null, yaxisRange: null,
        chartXName: null, chartYName: null, realTimeOption: null, q: null,
        extention1: null, extention2: null, image: null, data: null, entityRetrieveVO: null,
      };
      this.validation = {
        chartAttribute: null,
      };
      this.dataModelDisabled = {dataModelId: false, typeUri: false};
      // Initialize when the widget window is closed.
      await this.$nextTick();
      this.initDisplay();
      this.$emit('close');
    },
    initDisplay(obj = {}) {
      // only update param obj
      this.display = {
        chartType: false,
        chartTitle: false, chartXName: false, chartYName: false, updateInterval: false,
        realtimeUpdateEnabled: false, timerel: false, entityId: false, time: false, displayData: false,
        yaxisRange: false, custom_text: false, image: false, latestMap: false,
        chartUnit: false, legendDisplay: false,
        ...obj,
      };
    },
  }
}
</script>

<style>
.el-upload__tip {
  font-size: 12px;
  color: red;
}

.el-upload-list {
  position: absolute;
  left: 20%;
  top: 22px;
}

.legend-display {
  width: 75%;
  display: inline-block;
}
</style>
