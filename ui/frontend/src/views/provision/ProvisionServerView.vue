<template>
  <div>
    <h3 class="content__title">{{ $t('provision.title') }}</h3>
    <SmartSearch
        :is-text="true"
        :button-name="$t('comm.detailSearch')"
        @smart-search="showSmartSearch"
    />
    <p class="text__total">{{ $t('comm.total') }} {{ totalCount }}</p>
    <AppTable
        :meta-data="tableFields"
        :table-items="provisionList"
        @on-row-event="onDetailView"
    >
      <template v-slot:pagination>
        <AppPagination
            :total-count="totalCount"
            :pagination-value="15"
            :items="provisionList"
            @on-page-click="getProvisionList"
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
        :title="$t('provision.popupTitle')"
        :button-name="$t('comm.search')"
        :is-success-btn="true"
        :is-cancel-btn="true"
    >
      <template v-slot:elements>
        <AppForm
            :meta-data="formFields"
            :form-data="formData"
        />
      </template>
    </AppModal>
    <AppModal
        :is-show="isAlertShow"
        @close-modal="onClose"
        modalSize="w-360"
        :content="modalText"
        close-name="확인"
        :isCancelBtn="true"
    />
  </div>
</template>

<script>
/**
 * Provision Server list view page (container)
 */
import AppForm from '@/components/AppForm';
import AppModal from '@/components/AppModal';
import AppButtons from '@/components/AppButtons';
import AppPagination from '@/components/AppPagination';
import AppTable from '@/components/AppTable';
import SmartSearch from '@/components/SmartSearch';
import * as Fields from '@/modules/meta-fields';
import {mapMutations, mapState} from "vuex";
import {APIHandler} from "@/modules/api-handler";
import {errorRender} from "@/modules/utils";

export default {
  name: 'ProvisionServerView',
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
        [{ name: 'type', displayName: this.$i18n.t('provision.serverType'), type: 'choice',
          choices: [
            { value: 'dataServiceBroker', displayName: 'Data Service Broker' },
            { value: 'bigDataStorageHandler', displayName: 'Bigdata Storage Handler' },
            { value: 'ingestInterface', displayName: 'Ingest Interface' }
          ],
          selectedValue: null, require: false, isTable: false },
          { name: 'provisionProtocol', displayName: this.$i18n.t('provision.protocol'), type: 'choice',
            choices: [
              { value: 'http', displayName: 'Http' },
              { value: 'kafka', displayName: 'Kafka' }
            ],
            selectedValue: null, require: false, isTable: false }],
        [{ name: 'enabled', displayName: this.$i18n.t('provision.isActive'), type: 'choice',
          choices: [
            { value: true, displayName: this.$i18n.t('comm.active') },
            { value: false, displayName: this.$i18n.t('comm.inactive') }
          ],
          selectedValue: null, require: false, isTable: false }]
      ],
      tableFields: [
        { name: 'id', displayName: this.$i18n.t('provision.serverId'), require: false, col: 15 },
        { name: 'type', displayName: this.$i18n.t('provision.serverType'), require: false, col: 15 },
        { name: 'provisionUri', displayName: this.$i18n.t('provision.uri'), require: false, col: 20 },
        { name: 'provisionProtocol', displayName: this.$i18n.t('provision.protocol'), require: false, col: 10 },
        { name: 'enabled', displayName: this.$i18n.t('provision.isActive'), require: false, col: 10 },
        { name: 'createdAt', displayName: this.$i18n.t('comm.creationTime'), require: false, col: 15 },
        { name: 'modifiedAt', displayName: this.$i18n.t('comm.modifierTime'), require: false, col: 15 }
      ],
      provisionList: [],
      formData: {},
      totalCount: 0
    }
  },
  computed: {
    ...mapState('searchData', [
      'provisionSearchData'
    ])
  },
  methods: {
    ...mapMutations('searchData', [
      'setDataModelSearchData',
      'setDataSetInfoSearchData',
      'setDataSetFlowSearchData',
      'setVerificationHistorySearchData',
      'setProvisionSearchData',
      'setExternalPlatformSearchData'
    ]),
    showSmartSearch() {
      this.isShow = true;
    },
    onClose() {
      this.isShow = false;
      this.isAlertShow = false;
      this.formData = {};
    },
    onCreate() {
      this.$router.push({
        name: 'ProvisionServerModView',
        query: {
          mode: 'add'
        }
      });
    },
    onSearch() {
      this.isShow = false;
      this.setProvisionSearchData(this.formData);
      this.getProvisionList('search');
    },
    getProvisionList(searchType, pageObj) {
      let mergeObj = null;
      if (pageObj) {
        mergeObj = Object.assign(this.formData, pageObj);
      } else {
        this.formData.limit = 15;
        this.formData.offset = 0;
      }
      let queryStr = 'provision/servers?';
      queryStr += Object.entries(this.formData).map(e => e.join('=')).join('&');

      this.$http.get(APIHandler.buildUrl([queryStr]))
          .then(response => {
            const items = response.data.provisionServerResponseVO;
            const totalCnt = response.data.totalCount;
            if (items && items !== '') {
              let result = items.map((item, index) => {
                return {
                  id: item.id,
                  type: item.type,
                  provisionUri: item.provisionUri,
                  provisionProtocol: item.provisionProtocol,
                  enabled: item.enabled === true ? this.$i18n.t('codes.true') : this.$i18n.t('codes.false'),
                  modifiedAt: item.modifiedAt,
                  createdAt: item.createdAt
                }
              });
              this.provisionList = result;
              this.totalCount = totalCnt;
            } else {
              this.provisionList = [];
              this.totalCount = 0;
            }
          }).catch(error => {
            const result = errorRender(error.response.status, error.response.data);
            this.isAlertShow = result.isAlertShow;
            this.modalText = result.message + `(${ error.message })`;
      });
    },
    onDetailView(item) {
      this.$router.push({
        name: 'ProvisionServerModView',
        query: {
          id: item.id,
          mode: 'mod'
        }
      });
    }
  },
  mounted() {
    document.querySelectorAll('.breadcrumb__list')[0].innerText = this.$i18n.t('provision.title');
    this.formData = this.provisionSearchData;
    this.setDataModelSearchData({});
    this.setDataSetFlowSearchData({});
    this.setVerificationHistorySearchData({});
    this.setProvisionSearchData({});
    this.setExternalPlatformSearchData({});
    this.getProvisionList();
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