<template>
  <div>
    <h3 class="content__title">{{ $t('dataset.datasetInformation') }}</h3>
    <div style="margin-top: 15px; text-align: right;" v-if="isMode === 'add'">
      <label>
        <el-select v-model="searchModelValue" filterable :placeholder="$t('dataset.datasetLoad')" size="mini">
          <el-option
              size="mini"
              style="font-size: 12px;"
              v-for="item in dataSetList"
              :key="item.id"
              :label="item.id"
              :value="item.id"
          >
          </el-option>
        </el-select>
        <button type="button" class="button__primary" @click="setDataSetId">
          {{ $t('comm.load') }}
        </button>
      </label>
    </div>
    <form>
      <fieldset>
        <DatasetInformationModView
            :call-save="datasetCount"
            :is-data-model-id="isDataModelId"
            :is-load-data="loadDataId"
        />
        <DatasetFlowModView
            v-if="isMode === 'mod'"
            :call-save="flowCount"
            @dataset-flow-data="getDatasetFlow"
        />
        <div class="button__group">
          <button
              v-if="isMode === 'mod'"
              class="button__primary"
              type="button"
              @click="onProvisioning('dataset')"
              style="width: 110px"
          >
            {{ $t('dataset.datasetProvisioning') }}
          </button>
          <button
              v-if="isFlowProvision"
              class="button__primary"
              type="button"
              @click="onProvisioning('flow')"
              style="width: 100px"
          >
            {{ $t('dataset.flowProvision') }}
          </button>
          <button
              class="button__primary"
              type="button"
              @click="onDatasetSave"
          >
            {{ $t('dataset.datasetSave') }}
          </button>
          <button
              v-if="isMode === 'mod'"
              class="button__primary"
              type="button"
              @click="onFlowSave"
          >
            {{ $t('dataset.flowSave') }}
          </button>
          <button
              class="button__primary"
              type="button"
              @click="onGoBack"
          >
            {{ $t('comm.backToList') }}
          </button>
        </div>
      </fieldset>
    </form>
    <AppModal
        :is-show="isAlertShow"
        @close-modal="onClose"
        modalSize="w-360"
        :content="modalText"
        close-name="확인"
        :isCancelBtn="true"
    />
    <Loading
        :opacity="0.3"
        color="#0996a5"
        background-color="#4B4B4B"
        :active.sync="isLoading"
        :can-cancel="true"
        :is-full-page="fullPage"
        :height="64"
        :width="64"
    >
    </Loading>
  </div>
</template>

<script>
/**
 * Dataset info, flow container
 */
  import AppTable from '@/components/AppTable';
  import AppModal from '@/components/AppModal';
  import DatasetInformationModView from '@/views/dataset/DatasetInformationView';
  import DatasetFlowModView from '@/views/dataset/DatasetFlowModView';

  import Loading from 'vue-loading-overlay';
  import 'vue-loading-overlay/dist/vue-loading.css';
  import { errorRender } from "@/modules/utils";
  import {mapState} from "vuex";


  export default {
    name: 'DatasetModView',
    components: {
      DatasetFlowModView,
      DatasetInformationModView,
      AppTable,
      AppModal,
      Loading
    },
    computed: {
      ...mapState('dataSets', [
        'dataSetList'
      ]),
    },
    data() {
      return {
        isLoading: false,
        fullPage: true,
        isSaveShow: false,
        isDelShow: false,
        isAlertShow: false,
        datasetForm: {},
        datasetFlowForm: {},
        error: {
          id: false, name: false, receptionIps: false, receptionDatasetIds: false, receptionClientIds: false
        },
        isMode: null,
        modalText: null,
        addText: { receptionIps: null, receptionDatasetIds: null, receptionClientIds: null },
        delText: null,
        datasetCount: 0,
        flowCount: 0,
        isFlowProvision: false,
        isDataModelId: false,
        searchModelValue: '',
        loadDataId: '',
      }
    },
    methods: {
      onGoBack() {
        this.$router.push({
          name: 'DatasetView'
        });
      },
      onClose() {
        this.isSaveShow = false;
        this.isAlertShow = false;
        this.isDelShow = false;
        this.isDelAttrShow = false;
      },
      getDatasetFlow(items) {
        if (items && Object.keys(items).length > 0) {
          this.isDataModelId = true;
        } else {
          this.isDataModelId = false;
        }
        this.isFlowProvision = items && Object.keys(items).length > 0;
      },
      onProvisioning(flag) {
        this.isLoading = true;
        const { id } = this.$route.query;
        if (flag === 'dataset') {
          this.$http.post(`/datasets/${id}/provision`, {})
              .then(response => {
                const resultCode = response.status;
                if (resultCode === 200 || 201 || 204) {
                  this.isLoading = false;
                  this.isAlertShow = true;
                  this.modalText = '요청 되었습니다.';
                }
              }).catch(error => {
                this.isLoading = false;
                const result = errorRender(error.response.status, error.response.data);
                this.isAlertShow = result.isAlertShow;
                this.modalText = result.message + `(${ error.message })`;
          });
        } else {
          this.$http.post(`/datasets/${id}/flow/provision`, {})
              .then(response => {
                const resultCode = response.status;
                if (resultCode === 200 || 201 || 204) {
                  this.isLoading = false;
                  this.isAlertShow = true;
                  this.modalText = '요청 되었습니다.';
                }
              }).catch(error => {
                this.isLoading = false;
                const result = errorRender(error.response.status, error.response.data);
                this.isAlertShow = result.isAlertShow;
                this.modalText = result.message + `(${ error.message })`;
          });
        }
      },
      onDatasetSave() {
        this.datasetCount++;
      },
      onFlowSave() {
        this.flowCount++;
      },
      setDataSetId() {
        this.loadDataId = this.searchModelValue;
      }
    },
    mounted() {
      const { mode, id } = this.$route.query;
      this.isMode = mode;
      this.getDatasetFlow();
    }
  }
</script>

<style scoped>

</style>