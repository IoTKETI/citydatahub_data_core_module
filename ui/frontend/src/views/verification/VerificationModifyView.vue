<template>
  <div>
    <h3 class="content__title">{{ $t('validation.detailTitle') }}</h3>
    <AppForm
        :meta-data="formFields"
        :form-data="formData"
    />
    <AppForm
        :meta-data="formAdditionFields"
        :form-data="formData"
        :form-buttons="formButtons"
    />
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
 * Verification History detail view page (container)
 */
  import AppForm from '@/components/AppForm';
  import AppTable from '@/components/AppTable';
  import SmartSearch from '@/components/SmartSearch';
  import AppModal from '@/components/AppModal';
  import AppPagination from '@/components/AppPagination';
  import AppButtons from '@/components/AppButtons';
  import * as Fields from '@/modules/meta-fields';
  import { APIHandler } from '@/modules/api-handler';
  import { errorRender } from "@/modules/utils";


  export default {
  name: 'DataSetFlowView',
  components: {
    AppForm,
    AppTable,
    AppPagination,
    AppModal,
    AppButtons,
    SmartSearch,
  },
  props: {
    objData: Object
  },
  data() {
    return {
      formFields: [
        [
          { name: 'seq', displayName: this.$i18n.t('validation.validationId'), type: 'text', require: false, isTable: false },
          { name: 'testTime', displayName: this.$i18n.t('validation.validationTime'), type: 'text', require: false, isTable: false },
          { name: 'datasetId', displayName: this.$i18n.t('validation.datasetId'), type: 'text', require: false, isTable: false }
        ],
        [{ name: 'dataModelVersion', displayName: this.$i18n.t('validation.dataModelId'), type: 'text', require: false, isTable: false },
          { name: 'dataModelType', displayName: this.$i18n.t('validation.dataModelType'), type: 'text', require: false, isTable: false },
          { name: 'entityId', displayName: this.$i18n.t('validation.entityId'), require: false, type: 'text', isTable: false }
        ],
        [
          {
            name: 'verified', displayName: this.$i18n.t('validation.verified'), require: false, type: 'choice', isTable: false,
            choices: [
              { value: true, displayName: this.$i18n.t('comm.valid') },
              { value: false, displayName: this.$i18n.t('comm.invalid') },
            ]
          },
          { type: null },
          { type: null }
        ]
      ],
      formAdditionFields: [],
      formData: { sourceDatasetIds:[], keywords: [] },
      formButtons: [
        { id: 'goBack', name: this.$i18n.t('comm.backToList'), className: 'button__primary', onButtonEvent: this.onGoBack, isHide: false }
      ],
      isAlertShow: false,
      modalText: null,
      selectedValue: null
    }
  },
  methods: {
    onGoBack() {
      this.formData = {};
      this.$router.push('verificationHistoryView');
    },
    getVerification(seq) {
      this.$http.get(APIHandler.buildUrl(['verificationHistory', seq]))
          .then(response => {
            this.formData = response.data;
            if (response.data.errorCode) {
              this.selectedValue = response.data.errorCode;
            }
      }).catch(error => {
        const result = errorRender(error.response.status, error.response.data);
        this.isAlertShow = result.isAlertShow;
        this.modalText = result.message + `(${ error.message })`;
      });
    },
    onClose() {
      this.isAlertShow = false;
    },
    getVerificationErrorCode() {
      this.$http.get(`/code?pageSize=999&currentPage=1&codeGroupId=DC018`)
          .then(response => {
            const { codeVOs } = response.data;
            codeVOs.map(item => item.codeName = this.$i18n.t(`codes.${item.codeId}`));

            let items = response.data.codeVOs;
            let result = [];
            items.map(item => {
              result.push({
                value: item.codeId,
                displayName: item.codeName
              });
            });
            this.formAdditionFields = [
              [{ name: 'errorCode', displayName: this.$i18n.t('validation.errorCode'), type: 'choice',
                choices: result, selectedValue: this.selectedValue,
                require: false, isTable: false, col: 170 }],
              [{ name: 'errorCause', displayName: this.$i18n.t('validation.errorMessage'), require: false, type: 'text', isTable: false, col: 170 }],
              [{ name: 'data', displayName: this.$i18n.t('validation.originalData'), require: false, type: 'text', isTable: false, col: 170 }]
            ];
          });
    }
  },
  mounted() {
    document.querySelectorAll('.breadcrumb__list')[0].innerText = this.$i18n.t('validation.title');

    const { mode, seq } = this.$route.query;
    if (mode === 'mod') {
      this.getVerification(seq);
    }
    this.getVerificationErrorCode();
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