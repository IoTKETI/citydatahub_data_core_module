<template>
  <div>
    <h3 class="content__title">{{ $t('platform.detailTitle') }}</h3>
    <form>
      <fieldset>
        <legend>필드셋 제목</legend>
        <!-- section-write -->
        <section class="section">
<!--          <div class="section__header">-->
<!--            <h4 class="section__title">기본 정보</h4>-->
<!--          </div>-->
          <div class="section__content">
            <table class="table--row">
              <caption>테이블 제목</caption>
              <colgroup>
                <col style="width:150px">
                <col style="width:auto">
                <col style="width:150px">
                <col style="width:auto">
                <col style="width:150px">
                <col style="width:auto">
              </colgroup>
              <tbody>
                <tr>
                  <th class="icon__require">{{ $t('platform.id') }}</th>
                  <td>
                    <label>
                      <input
                          :class="error['id'] ? `input__text error__border` : `input__text`"
                          type="text"
                          v-model="formData['id']"
                          name="id"
                          @blur="onFocusoutEvent"
                          :disabled="isMode === 'mod'"
                      />
                    </label>
                    <br>
                    <span v-show="error['id']" class="error__color">
                      {{ $t('comm.required') }}
                    </span>
                  </td>
                  <th class="icon__require">{{ $t('platform.name') }}</th>
                  <td>
                    <label>
                      <input
                          v-model="formData['name']"
                          :class="error['name'] ? `input__text error__border` : `input__text`"
                          name="name"
                          type="text"
                          @blur="onFocusoutEvent"
                      />
                    </label>
                    <br>
                    <span v-show="error['name']" class="error__color">
                      {{ $t('comm.required') }}
                    </span>
                  </td>
                  <th>{{ $t('comm.creator') }}</th>
                  <td>
                    <label>
                      <input
                          class="input__text"
                          type="text"
                          name="creatorId"
                          v-model="formData['creatorId']"
                          disabled
                      />
                    </label>
                  </td>
                </tr>
                <tr>
                  <th>{{ $t('comm.creationTime') }}</th>
                  <td>
                    <label>
                      <input
                          class="input__text"
                          type="text"
                          name="createdAt"
                          v-model="formData['createdAt']"
                          disabled
                      />
                    </label>
                  </td>
                  <th>{{ $t('comm.modifier') }}</th>
                  <td>
                    <label>
                      <input
                          class="input__text"
                          type="text"
                          name="modifierId"
                          v-model="formData['modifierId']"
                          disabled
                      />
                    </label>
                  </td>
                  <th>{{ $t('comm.modifierTime') }}</th>
                  <td>
                    <label>
                      <input
                          class="input__text"
                          type="text"
                          name="modifiedAt"
                          v-model="formData['modifiedAt']"
                          disabled
                      />
                    </label>
                  </td>
                </tr>
                <tr>
                  <th class="icon__require">{{ $t('platform.allowedIp') }}</th>
                  <td>
                    <div class="button__group" style="margin: 0 0 5px;">
                      <input class="input__checkbox" id="receptionAll" type="checkbox" :checked="receptionChecked" @click="onAll">
                      <label class="label__checkbox" for="receptionAll">{{ $t('platform.selectAll') }}</label>
                      <button
                          class="button__util button__util--add material-icons"
                          type="button"
                          name="receptionIpsAdd"
                          @click="onTableAdd"
                      >
                        {{ $t('comm.add') }}
                      </button>
                      <button
                          class="button__util button__util--remove material-icons"
                          type="button"
                          name="receptionIpsDel"
                          @click="onTableDel"
                      >
                        {{ $t('comm.delete') }}
                      </button>
                    </div>
                    <label>
                      <input class="input__text" type="text" v-model="addText.receptionIps" :disabled="receptionChecked" />
                    </label>
                    <AppTable
                        :meta-data="[]"
                        :class-name="error['receptionIps'] ? `error__border` : null"
                        :table-items="formData['receptionIps']"
                        tableHeight="150px"
                        overflowY="auto"
                        @on-row-event="onTableRowEvent"
                    />
                    <span v-show="error['receptionIps']" class="error__color">
                      {{ $t('comm.required') }}
                    </span>
                  </td>

                  <th class="icon__require">{{ $t('platform.allowedDataset') }}</th>
                  <td>
                    <div class="button__group" style="margin: 0 0 5px;">
                      <input class="input__checkbox" id="receptionDatasetAll" type="checkbox" :checked="receptionDatasetChecked" @click="onAll">
                      <label class="label__checkbox" for="receptionDatasetAll">{{ $t('platform.selectAll') }}</label>
                      <button
                          class="button__util button__util--add material-icons"
                          type="button"
                          name="receptionDatasetIdsAdd"
                          @click="onShowDatasetPopup"
                      >
                        {{ $t('comm.search') }}
                      </button>
                      <button
                          class="button__util button__util--remove material-icons"
                          type="button"
                          name="receptionDatasetIdsDel"
                          @click="onTableDel"
                      >
                        {{ $t('comm.delete') }}
                      </button>
                    </div>
                    <AppTable
                        :meta-data="[]"
                        :class-name="error['receptionDatasetIds'] ? `error__border` : null"
                        :table-items="formData['receptionDatasetIds']"
                        tableHeight="185px"
                        overflowY="auto"
                        @on-row-event="onTableRowEvent"
                    />
                    <span v-show="error['receptionDatasetIds']" class="error__color">
                      {{ $t('comm.required') }}
                    </span>
                  </td>

                  <th class="icon__require">{{ $t('platform.allowedClient') }}</th>
                  <td>
                    <div class="button__group" style="margin: 0 0 5px;">
                      <input class="input__checkbox" id="receptionClientAll" type="checkbox" :checked="receptionClientChecked" @click="onAll">
                      <label class="label__checkbox" for="receptionClientAll">{{ $t('platform.selectAll') }}</label>
                      <button
                          class="button__util button__util--add material-icons"
                          type="button"
                          name="receptionClientIdsAdd"
                          @click="onTableAdd"
                      >
                        {{ $t('comm.add') }}
                      </button>
                      <button
                          class="button__util button__util--remove material-icons"
                          type="button"
                          name="receptionClientIdsDel"
                          @click="onTableDel"
                      >
                        {{ $t('comm.delete') }}
                      </button>
                    </div>
                    <label>
                      <input class="input__text" type="text" v-model="addText.receptionClientIds" :disabled="receptionClientChecked" />
                    </label>
                    <AppTable
                        :meta-data="[]"
                        :class-name="error['receptionClientIds'] ? `error__border` : null"
                        :table-items="formData['receptionClientIds']"
                        tableHeight="150px"
                        overflowY="auto"
                        @on-row-event="onTableRowEvent"
                    />
                    <span v-show="error['receptionClientIds']" class="error__color">
                      {{ $t('comm.required') }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
        <section class="section">
<!--          <div class="section__header">-->
<!--            <h4 class="section__title">부가 정보</h4>-->
<!--          </div>-->
          <div class="section__content">
            <table class="table--row">
              <caption>테이블 제목</caption>
              <colgroup>
                <col style="width:120px">
                <col style="width:auto">
                <col style="width:120px">
                <col style="width:auto">
              </colgroup>
              <tbody>
              <tr>
                <th>{{ $t('platform.description') }}</th>
                <td>
                  <label>
                    <input
                        class="input__text"
                        type="text"
                        v-model="formData['description']"
                        name="description"
                    />
                  </label>
                </td>
                <th>{{ $t('platform.instanceIdPrefix') }}</th>
                <td>
                  <label>
                    <input
                        class="input__text"
                        type="text"
                        v-model="formData['dataInstancePrefix']"
                        name="dataInstancePrefix"
                    />
                  </label>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </section>
        <div class="button__group">
          <button
              class="button__primary"
              type="button"
              @click="onSave"
          >
            {{ $t('comm.save') }}
          </button>
          <button
              v-if="isMode === 'mod'"
              class="button__secondary"
              type="button"
              @click="onDelete"
          >
            {{ $t('comm.delete') }}
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
        :is-show="isSaveShow"
        @close-modal="onClose"
        @on-event-modal="onConfirmSave"
        modalSize="w-360"
        :content="modalText"
        :button-name="$t('comm.ok')"
        :is-success-btn="true"
        :isCancelBtn="true"
    />
    <AppModal
        :is-show="isDelShow"
        @close-modal="onClose"
        @on-event-modal="onConfirmDel"
        modalSize="w-360"
        :content="modalText"
        :button-name="$t('comm.ok')"
        :is-success-btn="true"
        :isCancelBtn="true"
    />
    <AppModal
        :is-show="isAlertShow"
        @close-modal="onClose"
        modalSize="w-360"
        :content="modalText"
        :button-name="$t('comm.ok')"
        :isCancelBtn="true"
    />
    <AppModal
        :is-show="isShow"
        @close-modal="onClose"
        @on-event-modal="onDatasetSave"
        :title="$t('platform.datasetSearch')"
        :button-name="$t('comm.save')"
        :is-success-btn="true"
        :isCancelBtn="true"
    >
      <template v-slot:elements>
        <section class="section">
          <div class="section__content">
            <table class="table--row">
              <caption>테이블 제목</caption>
              <colgroup>
                <col style="width:150px">
                <col style="width:auto">
              </colgroup>
              <tbody>
              <tr>
                <th rowspan="4">{{ $t('platform.dataset') }}</th>
                <td colspan="3">
                  <label>
                    <input class="input__text" type="text" v-model="searchText" />
                  </label>
                </td>
                <td>
                  <button
                      class="button__outline w-68"
                      type="button"
                      @click="onDatasetSearch"
                  >
                    {{ $t('comm.search') }}
                  </button>
                </td>
              </tr>
              <tr>
                <td colspan="4">
                  <AppTable
                      :meta-data="[
                      { name: 'id', displayName: $t('platform.datasetId'), require: false, col: 15 },
                      { name: 'name', displayName: $t('platform.datasetName'), require: false, col: 15 }]"
                      :table-items="datasetList"
                      tableHeight="125px"
                      overflowY="auto"
                      @on-row-event="onDatasetRowEvent"
                  />
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </section>
      </template>
    </AppModal>
  </div>
</template>

<script>
/**
 * Platform detail view page (container)
 */
  import ElementTree from '@/components/ElementTree';
  import AppTable from '@/components/AppTable';
  import AppModal from '@/components/AppModal';
  import {APIHandler} from '@/modules/api-handler';
  import {errorRender} from '@/modules/utils';

  export default {
  name: 'ExternalPlatformModView',
  components: {
    ElementTree,
    AppTable,
    AppModal
  },
  props: {},
  data() {
    return {
      isShow: false,
      isSaveShow: false,
      isDelShow: false,
      isAlertShow: false,
      formData: {},
      error: {
        id: false, name: false, receptionIps: false, receptionDatasetIds: false, receptionClientIds: false
      },
      isMode: null,
      modalText: null,
      addText: { receptionIps: null, receptionDatasetIds: null, receptionClientIds: null },
      delText: null,
      searchText: null,
      receptionChecked: false,
      receptionDatasetChecked: false,
      receptionClientChecked : false,
      datasetList: [],
    }
  },
  methods: {
    getExternalPlatform() {
      const { id, mode } = this.$route.query;
      this.$http.get(APIHandler.buildUrl(['externalplatform', 'authentication', id]))
          .then(response => {
            this.formData = response.data;
            if (this.formData.receptionIps[0] === '*') {
              this.receptionChecked = true;
            }
            if (this.formData.receptionDatasetIds[0] === '*') {
              this.receptionDatasetChecked = true;
            }
            if (this.formData.receptionClientIds[0] === '*') {
              this.receptionClientChecked = true;
            }
          }).catch(error => {
        const result = errorRender(error.response.status, error.response.data);
        this.isAlertShow = result.isAlertShow;
        this.modalText = result.message + `(${ error.message })`;
      });
    },
    onSave() {
      Object.keys(this.error).map(key => {
        if (!this.formData[key] || this.formData[key] === '' || this.formData[key].length === 0) {
          this.error[key] = true;
        }
        if (this.formData['receptionIps'] && this.formData['receptionIps'].length > 0) {
          this.error['receptionIps'] = false;
        }
        if (this.formData['receptionDatasetIds'] && this.formData['receptionDatasetIds'].length > 0) {
          this.error['receptionDatasetIds'] = false;
        }
        if (this.formData['receptionClientIds'] && this.formData['receptionClientIds'].length > 0) {
          this.error['receptionClientIds'] = false;
        }
      });
      let checkResult = Object.keys(this.error).some(key => {
        return !!this.error[key];
      });
      if (checkResult) {
        return null;
      }
      this.isSaveShow = true;
      this.modalText = this.$i18n.t('comm.saveCheck');
    },
    onDelete() {
      this.isDelShow = true;
      this.modalText = this.$i18n.t('comm.deleteCheck');
    },
    onGoBack() {
      this.$router.push({
        name: 'ExternalPlatformView'
      });
    },
    onClose() {
      this.isShow = false;
      this.isSaveShow = false;
      this.isDelShow = false;
      this.isAlertShow = false;
    },
    onConfirmSave() {
      const { id, mode } = this.$route.query;
      if (mode === 'add') {
        this.$http.post(APIHandler.buildUrl(['externalplatform', 'authentication']), this.formData)
            .then(response => {
              const resultCode = response.status;
              if (resultCode === 200 || 201 || 204) {
                this.onGoBack();
              }
            }).catch(error => {
          const result = errorRender(error.response.status, error.response.data);
          this.isAlertShow = result.isAlertShow;
          this.modalText = result.message + `(${ error.message })`;
        });
      } else {
        this.$http.patch(APIHandler.buildUrl(['externalplatform', 'authentication', id]), this.formData)
            .then(response => {
              const resultCode = response.status;
              if (resultCode === 200 || 201 || 204) {
                this.onGoBack();
              }
            }).catch(error => {
          const result = errorRender(error.response.status, error.response.data);
          this.isAlertShow = result.isAlertShow;
          this.modalText = result.message + `(${ error.message })`;
        });
      }
    },
    onConfirmDel() {
      const { id } = this.$route.query;
      this.$http.delete(APIHandler.buildUrl(['externalplatform', 'authentication', id]))
          .then(response => {
            const resultCode = response.status;
            if (resultCode === 200 || 201 || 204) {
              this.onGoBack();
            }
          }).catch(error => {
        const result = errorRender(error.response.status, error.response.data);
        this.isAlertShow = result.isAlertShow;
        this.modalText = result.message + `(${ error.message })`;
      });
    },
    onFocusoutEvent(event) {
      const { name, value } = event.target;
      if (value !== '') {
        this.error[name] = false;
      }
    },
    onTableRowEvent(value) {
      this.delText = value;
    },
    onTableAdd(event) {
      const { name } = event.target;
      if (name === 'receptionIpsAdd') {
        if (!this.addText.receptionIps || this.addText.receptionIps === '') {
          return null;
        }
        if (!this.formData.receptionIps) {
          this.formData.receptionIps = [];
        }
        this.formData.receptionIps.push(this.addText.receptionIps);
        this.addText.receptionIps = null;
      } else if (name === 'receptionDatasetIdsAdd') {
        if (!this.addText.receptionDatasetIds || this.addText.receptionDatasetIds === '') {
          return null;
        }
        if (!this.formData.receptionDatasetIds) {
          this.formData.receptionDatasetIds = [];
        }
        this.formData.receptionDatasetIds.push(this.addText.receptionDatasetIds);
        this.addText.receptionDatasetIds = null;
      } else {
        if (!this.addText.receptionClientIds || this.addText.receptionClientIds === '') {
          return null;
        }
        if (!this.formData.receptionClientIds) {
          this.formData.receptionClientIds = [];
        }
        this.formData.receptionClientIds.push(this.addText.receptionClientIds);
        this.addText.receptionClientIds = null;
      }
    },
    onTableDel(event) {
      const { name } = event.target;
      if (this.delText) {
        let data = { ...this.formData };
        if (name === 'receptionIpsDel') {
          if (this.receptionChecked) {
            return null;
          }
          data.receptionIps.some((item, index) => {
            if (item === this.delText) {
              data.receptionIps.splice(index, 1);
            }
          });
        } else if (name === 'receptionDatasetIdsDel') {
          if (this.receptionDatasetChecked) {
            return null;
          }
          data.receptionDatasetIds.some((item, index) => {
            if (item === this.delText) {
              data.receptionDatasetIds.splice(index, 1);
            }
          });
        } else {
          if (this.receptionClientChecked) {
            return null;
          }
          data.receptionClientIds.some((item, index) => {
            if (item === this.delText) {
              data.receptionClientIds.splice(index, 1);
            }
          });
        }
        this.formData = data;
      }
    },
    onAll(event) {
      const { id, checked } = event.target;

      if (id === 'receptionAll' && checked) {
        this.receptionChecked = true;
        this.formData.receptionIps = ['*'];
      } else if (id === 'receptionAll' && !checked) {
        this.receptionChecked = false;
        this.formData.receptionIps = [];
      }
      if (id === 'receptionDatasetAll' && checked) {
        this.receptionDatasetChecked = true;
        this.formData.receptionDatasetIds = ['*'];
      } else if (id === 'receptionDatasetAll' && !checked) {
        this.receptionDatasetChecked = false;
        this.formData.receptionDatasetIds = [];
      }
      if (id === 'receptionClientAll' && checked) {
        this.receptionClientChecked = true;
        this.formData.receptionClientIds = ['*'];
      } else if (id === 'receptionClientAll' && !checked) {
        this.receptionClientChecked = false;
        this.formData.receptionClientIds = [];
      }
    },
    onShowDatasetPopup() {
      this.isShow = true;
    },
    onDatasetSearch() {
      this.$http.get(`/datasets?searchValue=${ this.searchText }`)
          .then(response => {
            const items = response.data.dataSetResponseVO;
            this.datasetList = items.map(item => {
              return { id: item.id, name: item.name };
            });
          });
    },
    onDatasetRowEvent(item) {
      this.addText.receptionDatasetIds = item.id;
    },
    onDatasetSave() {
      if (!this.formData.receptionDatasetIds) {
        this.formData.receptionDatasetIds = [];
      }
      this.formData.receptionDatasetIds.push(this.addText.receptionDatasetIds);
      this.addText.receptionDatasetIds = null;
      this.searchText = null;
      this.isShow = false;
      this.datasetList = [];
    }
  },
  mounted() {
    const { mode } = this.$route.query;
    this.isMode = mode;
    if (mode !== 'add') {
      this.getExternalPlatform();
    }
  }
}
</script>

<style scoped>
.error__color {
  color: #f56c6c; font-size: 10px;
}
.error__border {
  border-color: #f56c6c;
}
</style>