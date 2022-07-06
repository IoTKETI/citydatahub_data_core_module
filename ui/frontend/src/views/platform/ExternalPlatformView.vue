<template>
  <div>
    <h3 class="content__title">{{ $t('platform.title') }}</h3>
    <p class="text__total">{{ $t('comm.total') }} {{ totalCount }}</p>
    <AppTable
        :meta-data="tableFields"
        :table-items="externalPlatList"
        @on-row-event="onDetailView"
    >
      <template v-slot:pagination>
        <AppPagination
            :total-count="totalCount"
            :pagination-value="15"
            :items="externalPlatList"
            @on-page-click="getExternalPlatforms"
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
        title="상세 조건 검색"
        button-name="검색"
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
 * Platform list view page (container)
 */
import AppTable from '@/components/AppTable';
import AppPagination from '@/components/AppPagination';
import AppButtons from '@/components/AppButtons';
import AppModal from '@/components/AppModal';
import SmartSearch from '@/components/SmartSearch';
import AppForm from "@/components/AppForm";
import * as Fields from '@/modules/meta-fields';
import { APIHandler } from '@/modules/api-handler';
import { mapMutations, mapState } from 'vuex';
import {errorRender} from "@/modules/utils";


export default {
  name: 'ExternalPlatformView',
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
      formFields: [{ name: 'id', displayName: this.$i18n.t('platform.dataSourceId'), type: 'text', require: false }],
      tableFields: [
        { name: 'id', displayName: this.$i18n.t('platform.dataSourceId'), require: false, col: 10 },
        { name: 'name', displayName: this.$i18n.t('platform.dataSourceName'), require: false, col: 10 },
        { name: 'description', displayName: this.$i18n.t('platform.description'), require: false, col: 15 },
        { name: 'createdAt', displayName: this.$i18n.t('comm.creationTime'), require: false, col: 10 },
        { name: 'modifiedAt', displayName: this.$i18n.t('comm.modifierTime'), require: false, col: 10 }
      ],
      externalPlatList: [],
      formData: { dataStoreUri: [] },
      totalCount: 0
    }
  },
  computed: {
    ...mapState('searchData', [
      'externalPlatformSearchData'
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
      this.formData = {dataStoreUri: []};
    },
    onCreate() {
      this.$router.push({
        name: 'ExternalPlatformModView',
        query: {
          mode: 'add'
        }
      });
    },
    onSearch() {
      this.isShow = false;
      this.setExternalPlatformSearchData(this.formData);
      this.getExternalPlatforms('search');
    },
    getExternalPlatforms(searchType, pageObj) {
      let mergeObj = null;
      if (pageObj) {
        mergeObj = Object.assign(this.formData, pageObj);
      } else {
        this.formData.limit = 15;
        this.formData.offset = 0;
      }
      let queryStr = 'externalplatform/authentication/?';
      queryStr += Object.entries(this.formData).map(e => e.join('=')).join('&');

      this.$http.get(APIHandler.buildUrl([queryStr]))
          .then(response => {
            const items = response.data.externalPlatformAuthResponseVO;
            const totalCnt = response.data.totalCount;
            if (items && items !== '') {
              let result = items.map((item) => {
                return {
                  id: item.id,
                  name: item.name,
                  description: item.description,
                  modifiedAt: item.modifiedAt,
                  createdAt: item.createdAt
                }
              });
              this.externalPlatList = result;
              this.totalCount = totalCnt;
            } else {
              this.externalPlatList = [];
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
        name: 'ExternalPlatformModView',
        query: {
          id: item.id,
          mode: 'mod'
        }
      });
    }
  },
  mounted() {
    document.querySelectorAll('.breadcrumb__list')[0].innerText = this.$i18n.t('platform.title');
    this.formData = this.externalPlatformSearchData;
    this.setDataModelSearchData({});
    this.setDataSetFlowSearchData({});
    this.setVerificationHistorySearchData({});
    this.setProvisionSearchData({});
    this.getExternalPlatforms();
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