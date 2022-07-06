<template>
  <div>
    <h3 class="content__title">{{ $t('dataset.datasetInfo') }}</h3>
    <SmartSearch
        :is-text="true"
        :button-name="$t('comm.detailSearch')"
        @smart-search="showSmartSearch"
    />
    <p class="text__total">{{ $t('comm.total') }} {{ totalCount }}</p>
    <AppTable
        :meta-data="tableFields"
        :table-items="datasetList"
        @on-row-event="onDetailView"
    >
      <template v-slot:pagination>
        <AppPagination
            :total-count="totalCount"
            :pagination-value="15"
            :items="datasetList"
            @on-page-click="getDatasetList"
        />
      </template>
      <template v-slot:buttons>
        <div class="button__group">
          <AppButtons
              :button-name="$t('comm.create')"
              @on-button-event="onCreate"
          />
        </div>
      </template>
    </AppTable>
    <AppModal
        :is-show="isShow"
        @close-modal="onClose"
        @on-event-modal="onSearch"
        :title="$t('dataset.datasetSearch')"
        :button-name="$t('comm.search')"
        :is-success-btn="true"
        :is-cancel-btn="true"
    >
      <template v-slot:elements>
        <AppForm
            :meta-data="formFields"
            :form-data="formData"
            @add-event="onDataTableAdd"
            @del-event="onDataTableDel"
        />
      </template>
    </AppModal>
    <AppModal
        :is-show="isAlertShow"
        @close-modal="onClose"
        modalSize="w-360"
        :content="modalText"
        :close-name="$t('comm.ok')"
        :isCancelBtn="true"
    />
  </div>
</template>

<script>
/**
 * Dataset list view page (container)
 */
import AppTable from '@/components/AppTable';
import AppPagination from '@/components/AppPagination';
import AppButtons from '@/components/AppButtons';
import AppModal from '@/components/AppModal';
import SmartSearch from '@/components/SmartSearch';
import AppForm from '@/components/AppForm';
import * as Fields from '@/modules/meta-fields';
import { APIHandler } from '@/modules/api-handler';
import { mapMutations, mapState } from 'vuex';
import { errorRender } from "@/modules/utils";

export default {
  name: 'DatasetView',
  components: {
    AppForm,
    AppModal,
    AppButtons,
    AppPagination,
    AppTable,
    SmartSearch
  },
  data() {
    return {
      isShow: false,
      isAlertShow: false,
      modalText: null,
      formFields: [
        [{ name: 'name', displayName: this.$i18n.t('dataset.name'), type: 'text', require: false },
          { name: 'updateInterval', displayName: this.$i18n.t('dataset.updateInterval'), type: 'text', require: false }],
        [{ name: 'category', displayName: this.$i18n.t('dataset.name'), type: 'text', require: false },
          { name: 'providerOrganization', displayName: this.$i18n.t('dataset.provider'), type: 'text', require: false }],
        [{ name: 'providerSystem', displayName: this.$i18n.t('dataset.providerSystem'), type: 'text', require: false },
          { name: 'isProcessed', displayName: this.$i18n.t('dataset.dataType'), type: 'text', require: false }],
        [{ name: 'ownership', displayName: this.$i18n.t('dataset.ownership'), type: 'text', require: false },
          { name: 'license', displayName: this.$i18n.t('dataset.license'), type: 'text', require: false }],
        [{ name: 'datasetExtension', displayName: this.$i18n.t('dataset.dataElements'), type: 'text', require: false },
          { name: 'targetRegions', displayName: this.$i18n.t('dataset.geographicScope'), type: 'text', require: false }],
        [{ name: 'qualityCheckEnabled', displayName: this.$i18n.t('dataset.qualityCheckEnabled'), type: 'choice',
          require: false,
          choices: [
            { value: true, displayName: this.$i18n.t('comm.yes') },
            { value: false, displayName: this.$i18n.t('comm.no') }
          ],
          selectedValue: null },
          { name: 'dataModelId', displayName: this.$i18n.t('dataModel.dataModelId'), type: 'text', require: false }],
        [
          {
            name: 'dataStoreUri', displayName: this.$i18n.t('dataset.dataStorage'), type: 'userOption',
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
              { displayName: this.$i18n.t('dataset.storageLocation'), require: false }
            ],
            tableHeight: '120px',
            overflowY: 'auto'
          }
        ],
      ],
      tableFields: [
        { name: 'id', displayName: this.$i18n.t('dataset.datasetId'), require: false, col: 15 },
        { name: 'name', displayName: this.$i18n.t('dataset.datasetName'), require: false, col: 15 },
        { name: 'updateInterval', displayName: this.$i18n.t('dataset.updateInterval'), require: false, col: 10 },
        { name: 'category', displayName: this.$i18n.t('dataset.category'), require: false, col: 10 },
        { name: 'providerSystem', displayName: this.$i18n.t('dataset.provider'), require: false, col: 15 },
        { name: 'qualityCheckEnabled', displayName: this.$i18n.t('dataset.qualityCheckEnabled'), require: false, col: 10 },
        { name: 'createdAt', displayName: this.$i18n.t('comm.createTime'), require: false, col: 15 }
      ],
      datasetList: [],
      formData: { dataStoreUri: [] },
      totalCount: 0,
      dataModelIds: []
    }
  },
  computed: {
    ...mapState('searchData', [
      'dataSetInfoSearchData'
    ])
  },
  methods: {
    ...mapMutations('searchData', [
      'setDataModelSearchData',
      'setDataSetInfoSearchData',
      'setDataSetFlowSearchData',
      'setVerificationHistorySearchData',
      'setProvisionSearchData'
    ]),
    ...mapMutations('dataSets', ['setDataSetList']),
    showSmartSearch() {
      this.isShow = true;
    },
    onClose() {
      this.isShow = false;
      this.isAlertShow = false;
      this.formData = { dataStoreUri: [] };
    },
    onCreate() {
      this.$router.push({
        name: 'DatasetModView',
        query: {
          mode: 'add'
        }
      });
    },
    onSearch() {
      this.isShow = false;
      this.setDataSetInfoSearchData(this.formData);
      this.getDatasetList('search');
    },
    onDataTableAdd(data) {
      const { value } = data;
      this.formData.dataStoreUri.push(value);
    },
    onDataTableDel(data) {
      const { value } = data;
      this.formData.dataStoreUri.some((item, index) => {
        if (item === value) {
          this.formData.dataStoreUri.splice(index, 1);
        }
      });
    },
    getDatasetList(searchType, pageObj) {
      let mergeObj = null;
      if (pageObj) {
        mergeObj = Object.assign(this.formData, pageObj);
      } else {
        this.formData.limit = 15;
        this.formData.offset = 0;
      }
      let queryStr = 'datasets?';
      queryStr += Object.entries(this.formData).map(e => e.join('=')).join('&');

      this.$http.get(APIHandler.buildUrl([queryStr]))
          .then(response => {
            const items = response.data.dataSetResponseVO;
            const totalCnt = response.data.totalCount;
            if (items && items !== '') {
              this.datasetList = items.map((item, index) => {
                return {
                  id: item.id,
                  name: item.name,
                  updateInterval: item.updateInterval,
                  category: item.category,
                  providerOrganization: item.providerOrganization,
                  qualityCheckEnabled: item.qualityCheckEnabled === true ? '예' : '아니오',
                  createdAt: item.createdAt
                }
              });
              this.totalCount = totalCnt;
              this.setDataSetList(items);
            } else {
              this.datasetList = [];
              this.totalCount = 0;
              this.setDataSetList([]);
            }
          }).catch(error => {
            const result = errorRender(error.response.status, error.response.data);
            this.isAlertShow = result.isAlertShow;
            this.modalText = result.message + `(${ error.message })`;
      });
    },
    onDetailView(item) {
      this.$router.push({
        name: 'DatasetModView',
        query: {
          id: item.id,
          mode: 'mod'
        }
      });
    }
  },
  mounted() {
    document.querySelectorAll('.breadcrumb__list')[0].innerText = this.$i18n.t('dataset.datasetManage');

    this.formData = this.dataSetInfoSearchData;
    this.setDataModelSearchData({});
    this.setDataSetFlowSearchData({});
    this.setVerificationHistorySearchData({});
    this.setProvisionSearchData({});
    this.getDatasetList();
  }
}
</script>

<style scoped>
.text__total {
  height: 25px;
  border-top: 0;
  line-height: 35px;
}
</style>