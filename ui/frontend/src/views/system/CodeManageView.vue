<template>
  <div>
    <h3 class="content__title">{{ $t('code.title') }}</h3>
    <form>
      <fieldset>
        <section class="section">
          <div class="section__header">
            <h4 class="section__title">{{ $t('code.subTitle') }}</h4>
          </div>
          <div class="section__content">
            <table class="table--row">
              <caption>테이블 제목</caption>
              <colgroup>
                <col style="width:120px">
                <col style="width:auto">
              </colgroup>
              <tbody>
              <tr>
                <th rowspan="2">{{ $t('code.groups') }}</th>
                <td>
                  <input
                      class="input__text"
                      type="text"
                      name="codeGroupSearch"
                      v-model="codeGroupText"
                  />
                </td>
                <td>
                  <div class="button__group" style="float: left;">
                    <button
                        type="button"
                        class="button__outline w-68"
                        name="codeGroupSearch"
                        @click="onSearch"
                    >
                      {{ $t('comm.search') }}
                    </button>
                    <button
                        type="button"
                        class="button__outline w-68"
                        name="codeGroup"
                        @click="codeAddEvent"
                    >
                      {{ $t('comm.add') }}
                    </button>
                  </div>
                </td>
              </tr>
              <tr>
                <td colspan="2">
                  <AppTable
                      :meta-data="codeGroupFields"
                      :table-items="codeGroupList"
                      tableHeight="230px"
                      overflowY="auto"
                      @on-row-event="onCodeGroupTableEvent"
                  >
                    <template v-slot:pagination>
                      <AppPagination
                          :total-count="codeGroupTotal"
                          :pagination-value="5"
                          :items="codeGroupList"
                          @on-page-click="getCodeGroupList"
                      />
                    </template>
                  </AppTable>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </section>
        <section class="section">
          <div class="section__header">
            <h4 class="section__title">{{ $t('code.code') }}</h4>
          </div>
          <div class="section__content">
            <table class="table--row">
              <colgroup>
                <col style="width:120px">
                <col style="width:auto">
              </colgroup>
              <tbody>
              <tr>
                <th rowspan="2">{{ $t('code.codes') }}</th>
                <td>
                  <input
                      class="input__text"
                      type="text"
                      name="codeSearch"
                      v-model="codeText"
                  />
                </td>
                <td>
                  <div class="button__group" style="float: left;">
                    <button
                        type="button"
                        class="button__outline w-68"
                        name="codeSearch"
                        @click="onSearch"
                    >
                      {{ $t('comm.search') }}
                    </button>
                    <button
                        type="button"
                        class="button__outline w-68"
                        name="code"
                        @click="codeAddEvent"
                    >
                      {{ $t('comm.add') }}
                    </button>
                  </div>
                </td>
              </tr>
              <tr>
                <td colspan="2">
                  <AppTable
                      :meta-data="codeFields"
                      :table-items="codeList"
                      tableHeight="230px"
                      overflowY="auto"
                      @on-row-event="onCodeTableEvent"
                  >
                    <template v-slot:pagination>
                      <AppPagination
                          :total-count="codeTotal"
                          :pagination-value="5"
                          :items="codeList"
                          @on-page-click="getCodeList"
                      />
                    </template>
                  </AppTable>

                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </section>
      </fieldset>
    </form>
    <AppModal
        :is-show="isShow"
        @close-modal="onClose"
        @on-event-modal="onPopupEvent"
        :title="title"
        :button-name="$t('comm.save')"
        :is-del-btn="isDelBtn"
        :is-success-btn="true"
        :isCancelBtn="true"
    >
      <template v-slot:elements>
        <section class="section" v-if="popupName === 'codeGroup'">
          <div class="section__content">
            <table class="table--row">
              <colgroup>
                <col style="width:120px">
                <col style="width:auto">
                <col style="width:120px">
                <col style="width:auto">
              </colgroup>
              <tbody>
              <tr>
                <th class="icon__require">{{ $t('code.groupId') }}</th>
                <td>
                  <input
                      :class="codeGroupError['codeGroupId'] ? `input__text error__border` : `input__text`"
                      type="text"
                      name="codeGroupId"
                      v-model="formData['codeGroupId']"
                      :disabled="isMode === 'mod'"
                      @blur="onPopupFocusoutEvent"
                  />
                  <br>
                  <span v-show="codeGroupError['codeGroupId']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
                <th class="icon__require">{{ $t('code.groupName') }}</th>
                <td>
                  <input
                      :class="codeGroupError['codeGroupName'] ? `input__text error__border` : `input__text`"
                      type="text"
                      name="codeGroupName"
                      v-model="formData['codeGroupName']"
                      @blur="onPopupFocusoutEvent"
                  />
                  <br>
                  <span v-show="codeGroupError['codeGroupName']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
              </tr>
              <tr>
                <th class="icon__require">{{ $t('code.isActive') }}</th>
                <td>
                  <select
                      class="input__text"
                      :style="codeGroupError['enabled'] ? `border-color: #f56c6c;` : null"
                      name="enabled"
                      v-model="formData['enabled']"
                      @blur="onPopupFocusoutEvent"
                  >
                    <option
                        disabled
                        :value="null"
                    >
                      Please select one
                    </option>
                    <option value="true">{{ $t('comm.active') }}</option>
                    <option value="false">{{ $t('comm.inactive') }}</option>
                  </select>
                  <br>
                  <span v-show="codeGroupError['enabled']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
                <th></th>
                <td></td>
              </tr>
              <tr>
                <th>{{ $t('code.description') }}</th>
                <td colspan="3">
                  <input
                      class="input__text"
                      type="text"
                      name="description"
                      v-model="formData['description']"
                  />
                </td>
              </tr>
              <tr v-if="isMode === 'mod'">
                <th>{{ $t('comm.creator') }}</th>
                <td>
                  <input
                      class="input__text"
                      type="text"
                      name="creatorId"
                      v-model="formData['creatorId']"
                      disabled="true"
                  />
                </td>
                <th>{{ $t('comm.creationTime') }}</th>
                <td>
                  <input
                      class="input__text"
                      type="text"
                      name="createdAt"
                      v-model="formData['createdAt']"
                      disabled="true"
                  />
                </td>
              </tr>
              <tr v-if="isMode === 'mod'">
                <th>{{ $t('comm.modifier') }}</th>
                <td>
                  <input
                      class="input__text"
                      type="text"
                      name="modifierId"
                      v-model="formData['modifierId']"
                      disabled="true"
                  />
                </td>
                <th>{{ $t('comm.modifierTime') }}</th>
                <td>
                  <input
                      class="input__text"
                      type="text"
                      name="modifiedAt"
                      v-model="formData['modifiedAt']"
                      disabled="true"
                  />
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </section>
        <section class="section" v-else>
          <div class="section__content">
            <table class="table--row">
              <colgroup>
                <col style="width:120px">
                <col style="width:auto">
                <col style="width:120px">
                <col style="width:auto">
              </colgroup>
              <tbody>
              <tr>
                <th class="icon__require">{{ $t('code.groupId') }}</th>
                <td>
                  <input
                      :class="codeError['codeGroupId'] ? `input__text error__border` : `input__text`"
                      type="text"
                      name="codeGroupId"
                      v-model="formData['codeGroupId']"
                      :disabled="isMode === 'mod'"
                      @blur="onPopupFocusoutEvent"
                  />
                  <br>
                  <span v-show="codeError['codeGroupId']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
                <th class="icon__require">{{ $t('code.codeId') }}</th>
                <td>
                  <input
                      :class="codeError['codeId'] ? `input__text error__border` : `input__text`"
                      type="text"
                      name="codeId"
                      v-model="formData['codeId']"
                      :disabled="isMode === 'mod'"
                      @blur="onPopupFocusoutEvent"
                  />
                  <br>
                  <span v-show="codeError['codeId']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
              </tr>
              <tr>
                <th class="icon__require">{{ $t('code.codeName') }}</th>
                <td>
                  <input
                      :class="codeError['codeName'] ? `input__text error__border` : `input__text`"
                      type="text"
                      name="codeName"
                      v-model="formData['codeName']"
                      @blur="onPopupFocusoutEvent"
                  />
                  <br>
                  <span v-show="codeError['codeName']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
                <th class="icon__require">{{ $t('code.order') }}</th>
                <td>
                  <input
                      :class="codeError['sortOrder'] ? `input__text error__border` : `input__text`"
                      type="text"
                      name="sortOrder"
                      v-model="formData['sortOrder']"
                      @blur="onPopupFocusoutEvent"
                  />
                  <br>
                  <span v-show="codeError['sortOrder']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
              </tr>
              <tr>
                <th class="icon__require">{{ $t('code.isActive') }}</th>
                <td>
                  <select
                      class="input__text"
                      :style="codeError['enabled'] ? `border-color: #f56c6c;` : null"
                      name="enabled"
                      v-model="formData['enabled']"
                      @blur="onPopupFocusoutEvent"
                  >
                    <option
                        disabled
                        :value="null"
                    >
                      Please select one
                    </option>
                    <option value="true">{{ $t('comm.active') }}</option>
                    <option value="false">{{ $t('comm.inactive') }}</option>
                  </select>
                  <br>
                  <span v-show="codeError['enabled']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
                <th></th>
                <td></td>
              </tr>
              <tr>
                <th>{{ $t('code.description') }}</th>
                <td colspan="3">
                  <input
                      class="input__text"
                      type="text"
                      name="description"
                      v-model="formData['description']"
                  />
                </td>
              </tr>
              <tr v-if="isMode === 'mod'">
                <th>{{ $t('comm.creator') }}</th>
                <td>
                  <input
                      class="input__text"
                      type="text"
                      name="creatorId"
                      v-model="formData['creatorId']"
                      disabled="true"
                  />
                </td>
                <th>{{ $t('comm.creationTime') }}</th>
                <td>
                  <input
                      class="input__text"
                      type="text"
                      name="createdAt"
                      v-model="formData['createdAt']"
                      disabled="true"
                  />
                </td>
              </tr>
              <tr v-if="isMode === 'mod'">
                <th>{{ $t('comm.modifier') }}</th>
                <td>
                  <input
                      class="input__text"
                      type="text"
                      name="modifierId"
                      v-model="formData['modifierId']"
                      disabled="true"
                  />
                </td>
                <th>{{ $t('comm.modifierTime') }}</th>
                <td>
                  <input
                      class="input__text"
                      type="text"
                      name="modifiedAt"
                      v-model="formData['modifiedAt']"
                      disabled="true"
                  />
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </section>
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
 * Code Manage view page (container)
 */
import AppTable from '@/components/AppTable';
import AppModal from '@/components/AppModal';
import AppPagination from '@/components/AppPagination';
import { APIHandler } from '@/modules/api-handler';
import * as Fields from '@/modules/meta-fields';
import { errorRender } from '@/modules/utils';


export default {
  name: 'CodeManageView',
  components: {
    AppTable,
    AppModal,
    AppPagination
  },
  data() {
    return {
      codeGroupFields: [
        { name: 'codeGroupId', displayName: this.$i18n.t('code.groupId'), require: false, col: 10 },
        { name: 'codeGroupName', displayName: this.$i18n.t('code.groupName'), require: false, col: 10 },
        { name: 'enabled', displayName: this.$i18n.t('code.isActive'), require: false, col: 10 },
        { name: 'description', displayName: this.$i18n.t('code.description'), require: false, col: 20 }
      ],
      codeFields: [
        { name: 'codeGroupId', displayName: this.$i18n.t('code.groupId'), require: false, col: 10 },
        { name: 'codeId', displayName: this.$i18n.t('code.codeId'), require: false, col: 10 },
        { name: 'codeName', displayName: this.$i18n.t('code.codeName'), require: false, col: 10 },
        { name: 'enabled', displayName: this.$i18n.t('code.isActive'), require: false, col: 10 },
        { name: 'description', displayName: this.$i18n.t('code.description'), require: false, col: 20 }
      ],
      formData: { enabled: null },
      searchData: {},
      codeSearchData: {},
      isShow: false,
      title: null,
      isMode: 'add',
      popupName: 'codeGroup',
      isDelBtn: false,
      codeGroupText: null,
      codeText: null,
      codeGroupList: [],
      codeGroupTotal: 0,
      codeList: [],
      codeTotal: 0,
      isAlertShow: false,
      modalText: null,
      codeGroupError: { codeGroupId: false, codeGroupName: false, enabled: false },
      codeError: { codeGroupId: false, codeId: false, codeName: false, enabled: false, sortOrder: false }
    }
  },
  methods: {
    onSearch(event) {
      const { name } = event.target;
      if (name === 'codeGroupSearch') {
        this.getCodeGroupList();
      } else {
        this.getCodeList();
      }
    },
    getCodeGroupList(searchType, pageObj) {
      if (this.codeGroupText === '') {
        this.searchData = { pageSize: 5, currentPage: 1 };
      }
      if (this.codeGroupText) {
        this.searchData.searchValue = this.codeGroupText;
      }
      let mergeObj = null;
      if (pageObj) {
        pageObj.currentPage = pageObj.page;
        pageObj.pageSize = 5;
        mergeObj = Object.assign(this.searchData, pageObj);
      } else {
        this.searchData.pageSize = 5;
        this.searchData.currentPage = 1;
        this.codeGroupTotal = 0;
      }
      let queryStr = 'codegroup?';
      queryStr += Object.entries(this.searchData).map(e => e.join('=')).join('&');

      this.$http.get(APIHandler.buildUrl([queryStr]))
          .then(response => {
            const items = response.data.codeGroupVOs;
            const totalCnt = response.data.totalCount;
            if (items && items !== '') {
              let result = items.map(item => {
                return {
                  codeGroupId: item.codeGroupId,
                  codeGroupName: item.codeGroupName,
                  enabled: item.enabled ? this.$i18n.t('comm.active') : this.$i18n.t('comm.inactive'),
                  description: item.description
                }
              });
              this.codeGroupList = result;
              this.codeGroupTotal = totalCnt;
            } else {
              this.codeGroupList = [];
              this.codeGroupTotal = 0;
            }
          }).catch(error => {
            const result = errorRender(error.response.status, error.response.data);
            this.isAlertShow = result.isAlertShow;
            this.modalText = result.message + `(${ error.message })`;
      });
    },
    getCodeList(searchType, pageObj) {
      if (this.codeText === '') {
        this.codeSearchData = { pageSize: 5, currentPage: 1 };
      }
      if (this.codeText) {
        this.codeSearchData.searchValue = this.codeText;
      }
      let mergeObj = null;
      if (pageObj) {
        pageObj.currentPage = pageObj.page;
        pageObj.pageSize = 5;
        mergeObj = Object.assign(this.codeSearchData, pageObj);
      } else {
        this.codeSearchData.pageSize = 5;
        this.codeSearchData.currentPage = 1;
        this.codeTotal = 0;
      }
      let queryStr = 'code?';
      queryStr += Object.entries(this.codeSearchData).map(e => e.join('=')).join('&');

      this.$http.get(APIHandler.buildUrl([queryStr]))
          .then(response => {
            const items = response.data.codeVOs;
            const totalCnt = response.data.totalCount;
            if (items && items !== '') {
              let result = items.map(item => {
                return {
                  codeGroupId: item.codeGroupId,
                  codeId: item.codeId,
                  codeName: this.$i18n.t(`codes.${item.codeId}`),
                  enabled: item.enabled ? this.$i18n.t('comm.active') : this.$i18n.t('comm.inactive'),
                  description: item.description
                }
              });
              this.codeList = result;
              this.codeTotal = totalCnt;
            } else {
              this.codeList = [];
              this.codeTotal = 0;
            }
          }).catch(error => {
            const result = errorRender(error.response.status, error.response.data);
            this.isAlertShow = result.isAlertShow;
            this.modalText = result.message + `(${ error.message })`;
      });
    },
    codeAddEvent(event) {
      const { name } = event.target;
      if (name === 'codeGroup') {
        this.title = this.$i18n.t('code.groupCreate');
        this.popupName = 'codeGroup';
        this.isMode = 'add';
        this.isDelBtn = false;
      } else {
        this.title = this.$i18n.t('code.codeCreate');
        this.popupName = 'code';
        this.isMode = 'add';
        this.isDelBtn = false;
      }
      this.isShow = true;
    },
    onClose() {
      this.isShow = false;
      this.isAlertShow = false;
      this.formData = { enabled: null };
      this.codeGroupError = { codeGroupId: false, codeGroupName: false, enabled: false };
      this.codeError = { codeGroupId: false, codeId: false, codeName: false, enabled: false, sortOrder: false };
    },
    onCodeGroupTableEvent(item) {
      this.title = this.$i18n.t('code.groupDetail');
      this.popupName = 'codeGroup';
      this.isMode = 'mod';
      this.isDelBtn = true;
      this.isShow = true;
      this.$http.get(APIHandler.buildUrl(['codegroup', item.codeGroupId]))
          .then(response => {
            const resultCode = response.status;
            if (resultCode === 200 || 201 || 204) {
              this.formData = response.data;
            }
          });
    },
    onCodeTableEvent(item) {
      this.title = this.$i18n.t('code.codeDetail');
      this.popupName = 'code';
      this.isMode = 'mod';
      this.isDelBtn = true;
      this.isShow = true;
      this.$http.get(APIHandler.buildUrl(['code', item.codeGroupId, item.codeId]))
          .then(response => {
            const resultCode = response.status;
            if (resultCode === 200 || 201 || 204) {
              this.formData = response.data;
            }
          }).catch(error => {
            const result = errorRender(error.response.status, error.response.data);
            this.isAlertShow = result.isAlertShow;
            this.modalText = result.message + `(${ error.message })`;
      });
    },
    onPopupEvent(name) {
      if (name === 'deleteBtn') {
        this.onDelete();
        return null;
      }

      if (this.popupName === 'codeGroup') {
        Object.keys(this.codeGroupError).map(key => {
          if (!this.formData[key] || this.formData[key] === '' || this.formData[key].length === 0) {
            this.codeGroupError[key] = true;
          }
        });
        let checkResult = Object.keys(this.codeGroupError).some(key => {
          return !!this.codeGroupError[key];
        });
        if (checkResult) {
          return null;
        }
      } else {
        Object.keys(this.codeError).map(key => {
          if (!this.formData[key] || this.formData[key] === '' || this.formData[key].length === 0) {
            this.codeError[key] = true;
          }
        });
        let checkResult = Object.keys(this.codeError).some(key => {
          return this.codeError[key] ? true : false;
        });
        if (checkResult) {
          return null;
        }
      }

      if (this.popupName === 'codeGroup' && this.isMode === 'add') {
        this.$http.post(APIHandler.buildUrl(['codegroup']), this.formData)
            .then(response => {
              const resultCode = response.status;
              if (resultCode === 200 || 201 || 204) {
                this.getCodeGroupList();
              }
            }).catch(error => {
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      } else if (this.popupName === 'codeGroup' && this.isMode === 'mod') {
        this.$http.patch(
            APIHandler.buildUrl(['codegroup', this.formData.codeGroupId]),
            this.formData)
            .then(response => {
              const resultCode = response.status;
              if (resultCode === 200 || 201 || 204) {
                this.getCodeGroupList();
              }
            }).catch(error => {
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      } else if (this.popupName === 'code' && this.isMode === 'add') {
        this.$http.post(APIHandler.buildUrl(['code']), this.formData)
            .then(response => {
              const resultCode = response.status;
              if (resultCode === 200 || 201 || 204) {
                this.getCodeList();
              }
            }).catch(error => {
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      } else {
        this.$http.patch(APIHandler.buildUrl(['code', this.formData.codeGroupId, this.formData.codeId]), this.formData)
            .then(response => {
              const resultCode = response.status;
              if (resultCode === 200 || 201 || 204) {
                this.getCodeList();
              }
            }).catch(error => {
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      }
      this.isShow = false;
      this.formData = { enabled: null };
    },
    onDelete() {
      if (this.popupName === 'codeGroup') {
        this.$http.delete(APIHandler.buildUrl(['codegroup', this.formData.codeGroupId]))
            .then(response => {
              const resultCode = response.status;
              if (resultCode === 200 || 201 || 204) {
                this.getCodeGroupList();
              }
            }).catch(error => {
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      } else {
        this.$http.delete(APIHandler.buildUrl(['code', this.formData.codeGroupId, this.formData.codeId]))
            .then(response => {
              const resultCode = response.status;
              if (resultCode === 200 || 201 || 204) {
                this.getCodeList();
              }
            }).catch(error => {
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      }
      this.isShow = false;
      this.formData = { enabled: null };
    },
    onPopupFocusoutEvent(event) {
      const { name, value } = event.target;
      if (value !== '') {
        if (this.popupName === 'codeGroup') {
          this.codeGroupError[name] = false;
        } else {
          this.codeError[name] = false;
        }
      }
    }
  },
  mounted() {
    this.getCodeGroupList();
    this.getCodeList();

    document.querySelectorAll('.breadcrumb__list')[0].innerText = this.$i18n.t('menu.systemManage');
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