<template>
  <section class="section">
    <div class="section__header">
      <h4 class="section__title">{{ $t('dataset.datasetFlow') }}</h4>
      <div class="button__group" style="margin: 0; padding-top: 5px;">
        <button
            v-if="isMode === 'mod'"
            class="button__secondary"
            type="button"
            @click="onDelete"
        >
          {{ $t('comm.delete') }}
        </button>
      </div>
    </div>
    <div class="section__content">
      <table class="table--row">
        <caption>테이블 제목</caption>
        <colgroup>
          <col style="width:120px">
          <col style="width:auto">
          <col style="width:120px">
          <col style="width:auto">
          <col style="width:120px">
          <col style="width:auto">
          <col style="width:120px">
          <col style="width:auto">
        </colgroup>
        <tbody>
        <tr>
          <th class="icon__require">{{ $t('dataset.datasetId') }}</th>
          <td>
            <label>
              <input
                  class="input__text"
                  type="text"
                  v-model="formData['datasetId']"
                  name="datasetId"
                  disabled
              />
            </label>
          </td>
          <th class="icon__require">{{ $t('dataset.isActive') }}</th>
          <td>
            <label>
              <select
                  v-model="formData['enabled']"
                  class="input__text"
                  :style="error['enabled'] ? `border-color: #f56c6c;` : null"
                  name="enabled"
                  @blur="onFocusoutEvent"
              >
                <option
                    disabled
                    :value="null"
                >
                  Please select one
                </option>
                <option
                    v-for="item in commonCodeList"
                    :value="item.codeId"
                >
                  {{ item.codeName }}
                </option>
              </select>
            </label>
            <br>
            <span v-show="error['enabled']" class="error__color">
              {{ $t('comm.required') }}
            </span>
          </td>
          <th class="icon__require">{{ $t('dataset.historicalData') }}</th>
          <td>
            <label>
              <select
                  class="input__text"
                  v-model="formData['historyStoreType']"
                  :style="error['historyStoreType'] ? `border-color: #f56c6c;` : null"
                  name="historyStoreType"
                  @blur="onFocusoutEvent"
              >
                <option
                    disabled
                    :value="null"
                >
                  Please select one
                </option>

                <option
                    v-for="item in storeTypeList"
                    :value="item.codeId"
                >
                  {{ item.codeName }}
                </option>
              </select>
            </label>
            <br>
            <span v-show="error['historyStoreType']" class="error__color">
              {{ $t('comm.required') }}
            </span>
          </td>
          <th rowspan="3" class="icon__require">{{ $t('dataset.provisionServer') }}</th>
          <td rowspan="3">
            <div class="button__group" style="margin: 0 0 5px;">
              <button
                  class="button__util button__util--add material-icons"
                  type="button"
                  name="targetTypesAdd"
                  @click="onShowPopup"
              >
                {{ $t('comm.add') }}
              </button>
            </div>
            <AppTable
                :meta-data="[]"
                :table-items="targetTypes"
                :class-name="error['targetTypes'] ? `error__border` : null"
                tableHeight="70px"
                overflowY="auto"
                @on-row-event="onTableRowEvent"
            />
            <span v-show="error['targetTypes']" class="error__color">
              {{ $t('comm.required') }}
            </span>
          </td>
        </tr>
        <tr>
          <th>{{ $t('comm.creator') }}</th>
          <td>
            <label>
              <input
                  class="input__text"
                  type="text"
                  v-model="formData['creatorId']"
                  name="creatorId"
                  disabled
              />
            </label>
          </td>
          <th>{{ $t('comm.creationTime') }}</th>
          <td>
            <label>
              <input
                  class="input__text"
                  type="text"
                  v-model="formData['createdAt']"
                  name="createdAt"
                  disabled
              />
            </label>
          </td>
          <th>{{ $t('dataset.description') }}</th>
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
        </tr>
        <tr>
          <th>{{ $t('comm.modifier') }}</th>
          <td>
            <label>
              <input
                  class="input__text"
                  type="text"
                  v-model="formData['modifierId']"
                  name="modifierId"
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
                  v-model="formData['modifiedAt']"
                  name="modifiedAt"
                  disabled
              />
            </label>
          </td>
          <th></th>
          <td></td>
        </tr>
        </tbody>
      </table>
    </div>
    <AppModal
        :is-show="isTargetTypesShow"
        @close-modal="onClose"
        @on-event-modal="onTargetTypeSave"
        :title="$t('dataset.provisionServer')"
        :button-name="$t('comm.save')"
        :is-del-btn="isDelBtn"
        :is-success-btn="true"
        :isCancelBtn="true"
    >
      <template v-slot:elements>
        <section class="section">
          <div class="section__content">
            <table class="table--row">
              <colgroup>
                <col style="width:200px">
                <col style="width:auto">
              </colgroup>
              <tbody>
              <tr>
                <th class="icon__require">{{ $t('dataset.provisionServer') }}</th>
                <td>
                  <label>
                    <select
                        class="input__text"
                        v-model="popupFormData['type']"
                        :style="error['type'] ? `border-color: #f56c6c;` : null"
                        name="type"
                        @blur="onFocusoutEvent"
                        @change="onChange2"
                        :disabled="isDisabled"
                    >
                      <option
                          disabled
                          :value="null"
                      >
                        Please select one
                      </option>
                      <option
                          v-for="item in serverTypeList"
                          :value="item.codeId"
                      >
                        {{ item.codeName }}
                      </option>
                    </select>
                  </label>
                  <br>
                  <span v-show="error['type']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
              </tr>
              <tr>
                <th>{{ $t('dataset.storageType') }}</th>
                <td>
                  <div class="button__group">
                    <label>
                      <select
                          class="input__text"
                          style="width: 60%;"
                          @change="onChange"
                          v-model="selected"
                          :disabled="isSaveTypeDisabled"
                          name="bigDataStorageTypes"
                      >
                        <option
                            disabled
                            :value="null"
                        >
                          Please select one
                        </option>
                        <option
                            v-for="item in storageTypeList"
                            :value="item.codeId"
                        >
                          {{ item.codeName }}
                        </option>
                      </select>
                    </label>
                    <button
                        class="button__util button__util--add material-icons"
                        type="button"
                        name="addButton"
                        @click="onTableAdd"
                    >
                      {{ $t('comm.add') }}
                    </button>
                    <button
                        class="button__util button__util--remove material-icons"
                        type="button"
                        name="deleteButton"
                        @click="onTableDel"
                    >
                      {{ $t('comm.delete') }}
                    </button>
                    <AppTable
                        :meta-data="[]"
                        :table-items="popupFormData.bigDataStorageTypes"
                        tableHeight="80px"
                        overflowY="auto"
                        @on-row-event="onPopupTableRowEvent"
                    />
                  </div>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </section>
      </template>
    </AppModal>
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
  </section>
</template>

<script>
/**
 * Dataset Flow detail view page (container)
 */
import AppTable from '@/components/AppTable';
import { APIHandler } from '@/modules/api-handler';
import AppModal from '@/components/AppModal';
import {errorRender} from "@/modules/utils";


export default {
  name: 'DatasetFlowModView',
  components: {
    AppTable,
    AppModal
  },
  props: {
    callSave: Number
  },
  watch: {
    callSave(val) {
      this.onSave();
    }
  },
  data() {
    return {
      isSaveShow: false,
      isDelShow: false,
      isAlertShow: false,
      isTargetTypesShow: false,
      isDelBtn: false,
      isDisabled: false,
      isSaveTypeDisabled: false,
      formData: { enabled: null, historyStoreType: null },
      popupFormData: { type: null },
      error: {
        enabled: false, historyStoreType: false, targetTypes: false, type: false
      },
      isMode: null,
      modalText: null,
      closeName: this.$i18n.t('comm.cancel'),
      isSuccessBtn: true,
      addText: { receptionIps: null, receptionDatasetIds: null, receptionClientIds: null },
      delText: null,
      selected: null,
      targetTypes: [],
      commonCodeList: [],
      storeTypeList: [],
      serverTypeList: [],
      storageTypeList: []
    }
  },
  methods: {
    onSave() {
      Object.keys(this.error).map(key => {
        if (!this.formData[key] || this.formData[key] === '' || this.formData[key].length === 0) {
          if (key !== 'type') {
            this.error[key] = true;
          }
        }
      });
      let checkResult = Object.keys(this.error).some(key => {
        return !!this.error[key];
      });
      if (checkResult) {
        return null;
      }
      this.isSaveShow = true;
      this.isSuccessBtn = true;
      this.closeName = this.$i18n.t('comm.cancel');
      this.modalText = this.$i18n.t('comm.saveCheck');
    },
    onDelete() {
      this.isDelShow = true;
      this.modalText = this.$i18n.t('comm.deleteCheck');
    },
    onConfirmSave() {
      const { id } = this.$route.query;
      if (this.isMode === 'mod') {
        this.$http.put(APIHandler.buildUrl(['datasets', id, 'flow']), this.formData)
            .then(response => {
              const resultCode = response.status;
              if (resultCode === 200 || 201 || 204) {
                this.$router.push('datasetView');
              }
            }).catch(error => {
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      } else {
        this.$http.post(APIHandler.buildUrl(['datasets', id, 'flow']), this.formData)
            .then(response => {
              const resultCode = response.status;
              if (resultCode === 200 || 201 || 204) {
                this.$router.push('datasetView');
              }
            }).catch(error => {
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      }
    },
    onConfirmDel() {
      const { datasetId } = this.formData;
      this.$http.delete(APIHandler.buildUrl(['datasets', datasetId, 'flow']))
          .then(response => {
            const resultCode = response.status;
            if (resultCode === 200 || 201 || 204) {
              this.$router.push('datasetView');
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
    onShowPopup() {
      this.isDisabled = false;
      this.isDelBtn = false;
      this.isTargetTypesShow = true;
      let data = { type: null };
      this.popupFormData = data;
    },
    onClose() {
      this.isSaveShow = false;
      this.isAlertShow = false;
      this.isDelShow = false;
      this.isTargetTypesShow = false;
    },
    onTableAdd(event) {
      if (!this.selected || this.selected === '') {
        return null;
      }
      const { name } = event.target;
      let data = { ...this.popupFormData };
      if (name === 'addButton') {
        if (!data.bigDataStorageTypes) {
          data.bigDataStorageTypes = [];
        }

        let checked = data.bigDataStorageTypes.some(item => {
          return item === this.addText.bigDataStorageTypes;
        });

        if (checked) {
          return null;
        }

        data.bigDataStorageTypes.push(this.addText.bigDataStorageTypes);
        this.addText.bigDataStorageTypes = null;
        this.selected = null;
        this.popupFormData = data;
      }
    },
    onTableDel(event) {
      const { name } = event.target;
      if (this.delText) {
        let data = { ...this.popupFormData };
        if (name === 'deleteButton') {
          data.bigDataStorageTypes.some((item, index) => {
            if (item === this.delText) {
              data.bigDataStorageTypes.splice(index, 1);
            }
          });
        }

        let result = this.formData.targetTypes.map(item => {
          if (item.type === this.popupFormData.type) {
            return item = data;
          }
        });
        this.formData.targetTypes = result;
        this.delText = null;
      }
    },
    onTableRowEvent(value) {
      const resultValue = value.split('(')[0];
      this.isDisabled = true;
      this.isDelBtn = true;
      this.isTargetTypesShow = true;
      const data = this.formData.targetTypes;
      data.map(item => {
        if (item.type === resultValue) {
          this.popupFormData = item;
        }
      });
      this.delText = resultValue;
    },
    onPopupTableRowEvent(value) {
      this.delText = value;
    },
    onTargetTypeSave(name) {
      if (name === 'deleteBtn') {
        let form = this.formData;
        form.targetTypes.some((item, index) => {
          if (item.type === this.delText) {
            form.targetTypes.splice(index, 1);
          }
        });
        this.formData = form;
        let type = form.targetTypes.map(data => {
          return data.type;
        });
        this.targetTypes = type;

        this.popupFormData = { type: null };
        this.isTargetTypesShow = false;
        return null;
      }
      if (!this.popupFormData['type'] || this.popupFormData['type'] === '' || this.popupFormData['type'].length === 0) {
        this.error['type'] = true;
      }
      let checkResult = this.error['type'];
      if (checkResult) {
        return null;
      }
      if (!this.formData.targetTypes) {
        this.formData.targetTypes = [];
      }

      if (!this.isDisabled) {
        let checked = this.formData.targetTypes.some(item => {
          return item.type === this.popupFormData.type;
        });

        if (checked) {
          alert(this.$i18n.t('comm.provisionDuplicateMessage'));
          return null;
        }
        this.formData.targetTypes.push(this.popupFormData);

        if (this.popupFormData.type === 'bigDataStorageHandler' && this.popupFormData.bigDataStorageTypes) {
          let text = '(';
          this.popupFormData.bigDataStorageTypes.map((item, index) => {
            text += `${ item },`;
          });
          this.targetTypes.push(this.popupFormData.type + text.slice(0, -1) + ')');
        } else {
          this.targetTypes.push(this.popupFormData.type);
        }
        this.isTargetTypesShow = false;
      } else {
        const { type } = this.popupFormData;
        let data = this.formData.targetTypes.map(item => {
          if (item.type === type) {
            return item = this.popupFormData;
          }
        });
        this.formData.targetTypes = data;
        this.isTargetTypesShow = false;
      }
      this.popupFormData = { type: null };
      this.error['targetTypes'] = false;
      this.error['type'] = false;
    },
    onChange(event) {
      this.selected = event.target.value;
      this.addText.bigDataStorageTypes = event.target.value;
    },
    onChange2(event) {
      const { name, value } = event.target;
      this.popupFormData.bigDataStorageTypes = [];
    },
    getDatasetFlow(datasetId) {
      this.$http.get(APIHandler.buildUrl(['datasets', datasetId, 'flow']))
          .then(response => {
            const item = response.data;
            this.$emit('dataset-flow-data', item);
            if (Object.keys(item).length > 0) {
              let type = [];
              item.targetTypes.map(data => {
                if (data.bigDataStorageTypes) {
                  type.push(`${data.type}(${data.bigDataStorageTypes})`);
                } else {
                  type.push(data.type);
                }
              });
              this.targetTypes = type;
              this.formData = item;
              return null;
            }
            this.isMode = 'add';
          }).catch(error => {
            const result = errorRender(error.response.status, error.response.data);
            this.isAlertShow = result.isAlertShow;
            this.modalText = result.message + `(${ error.message })`;
      });
    },
    getCommonCodeList() {
      this.$http.get(`/code?pageSize=999&currentPage=1&codeGroupId=DC101`)
          .then(response => {
            const { codeVOs } = response.data;
            codeVOs.map(item => {
              item.codeName = this.$i18n.t(`codes.${item.codeId}`);
              this.commonCodeList.push(item);
            });
          });
    },
    getStoreTypeList() {
      this.$http.get(`/code?pageSize=999&currentPage=1&codeGroupId=DC013`)
          .then(response => {
            const { codeVOs } = response.data;
            codeVOs.map(item => {
              item.codeName = this.$i18n.t(`codes.${item.codeId}`);
              this.storeTypeList.push(item);
            });
          });
    },
    getServerType() {
      this.$http.get(`/code?pageSize=999&currentPage=1&codeGroupId=DC014`)
          .then(response => {
            const { codeVOs } = response.data;
            codeVOs.map(item => {
              item.codeName = this.$i18n.t(`codes.${item.codeId}`);
              this.serverTypeList.push(item);
            });
          });
    },
    getStorageType() {
      this.$http.get(`/code?pageSize=999&currentPage=1&codeGroupId=DC015`)
          .then(response => {
            const { codeVOs } = response.data;
            codeVOs.map(item => {
              item.codeName = this.$i18n.t(`codes.${item.codeId}`);
              this.storageTypeList.push(item);
            });
          });
    }
  },
  mounted() {
    const { mode, id } = this.$route.query;
    this.isMode = mode;
    if (id) {
      this.getDatasetFlow(id);
    }
    this.getCommonCodeList();
    this.getStoreTypeList();
    this.getServerType();
    this.getStorageType();
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