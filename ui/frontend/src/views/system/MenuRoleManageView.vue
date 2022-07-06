<template>
  <div>
    <h3 class="content__title">{{ $t('role.title') }}</h3>
    <form>
      <fieldset>
        <section class="section">
<!--          <div class="section__header">-->
<!--            <h4 class="section__title">기본정보</h4>-->
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
                <th>{{ $t('role.menuAuth') }}</th>
                <td colspan="2">
                  <label>
                    <select
                        class="input__text"
                        v-model="selectedValue"
                        @change="onChange"
                        name="id"
                    >
                      <option
                          disabled
                          :value="null"
                      >
                        Please select one
                      </option>
                      <option
                          v-for="role in menuRoles"
                          :value="role.id"
                      >
                        {{ role.name }}
                      </option>
                    </select>
                  </label>
                </td>
                <td colspan="3">
                  <button
                      class="button__outline w-68"
                      type="button"
                      name="add"
                      @click="onShowPopup"
                  >
                    {{ $t('comm.add') }}
                  </button>
                  <button
                      class="button__outline w-68"
                      type="button"
                      name="mod"
                      @click="onShowPopup"
                  >
                    {{ $t('comm.getInfo') }}
                  </button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </section>
        <section class="section">
<!--          <div class="section__header">-->
<!--            <h4 class="section__title">부가정보</h4>-->
<!--          </div>-->
          <div class="section__content" style="text-align: center; padding: 10px;">
            <el-transfer
                filterable
                style="text-align: left; display: inline-block;"
                :titles="[$t('role.unAuthMenu'), $t('role.authMenu')]"
                v-model="value"
                :data="noRoleAssignMenu"
            >
            </el-transfer>
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
        </div>
      </fieldset>
    </form>
    <AppModal
        :is-show="isShow"
        @close-modal="onClose"
        @on-event-modal="onPopupEvent"
        :title="$t('role.popupTitle')"
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
                <col style="width:120px">
                <col style="width:auto">
                <col style="width:120px">
                <col style="width:auto">
              </colgroup>
              <tbody>
              <tr>
                <th class="icon__require">{{ $t('role.id') }}</th>
                <td>
                  <label>
                    <input
                        type="text"
                        :class="error['id'] ? `input__text error__border` : `input__text`"
                        v-model="formData['id']"
                        name="id"
                        @blur="onPopupFocusoutEvent"
                    />
                  </label>
                  <br>
                  <span v-show="error['id']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
                <th class="icon__require">{{ $t('role.name') }}</th>
                <td>
                  <label>
                    <input
                        type="text"
                        :class="error['name'] ? `input__text error__border` : `input__text`"
                        v-model="formData['name']"
                        name="name"
                        @blur="onPopupFocusoutEvent"
                    />
                  </label>
                  <br>
                  <span v-show="error['name']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
              </tr>
              <tr>
                <th class="icon__require">{{ $t('role.isActive') }}</th>
                <td>
                  <label>
                    <select
                        class="input__text"
                        :style="error['enabled'] ? `border-color: #f56c6c;` : null"
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
                      <option
                          v-for="code in commonCodeList"
                          :value="code.codeId"
                      >
                        {{ code.codeName }}
                      </option>
                    </select>
                  </label>
                  <br>
                  <span v-show="error['enabled']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
              </tr>
              <tr>
                <th>{{ $t('role.description') }}</th>
                <td colspan="3">
                  <label>
                    <input
                        type="text"
                        class="input__text"
                        v-model="formData['description']"
                        name="description"
                    />
                  </label>
                </td>
              </tr>
              <tr>
                <th>{{ $t('comm.creator') }}</th>
                <td>
                  <label>
                    <input
                        type="text"
                        class="input__text"
                        v-model="formData['creatorId']"
                        :disabled="true"
                        name="creatorId"
                    />
                  </label>
                </td>
                <th>{{ $t('comm.creationTime') }}</th>
                <td>
                  <label>
                    <input
                        type="text"
                        class="input__text"
                        v-model="formData['createdAt']"
                        :disabled="true"
                        name="createdAt"
                    />
                  </label>
                </td>
              </tr>
              <tr>
                <th>{{ $t('comm.modifier') }}</th>
                <td>
                  <label>
                    <input
                        type="text"
                        class="input__text"
                        v-model="formData['modifierId']"
                        :disabled="true"
                        name="modifierId"
                    />
                  </label>
                </td>
                <th>{{ $t('comm.modifierTime') }}</th>
                <td>
                  <label>
                    <input
                        type="text"
                        class="input__text"
                        v-model="formData['modifierAt']"
                        :disabled="true"
                        name="modifierAt"
                    />
                  </label>
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
 * Menu Role manage view page (container)
 */
import AppModal from '@/components/AppModal';
import { APIHandler } from '@/modules/api-handler';
import { errorRender } from '@/modules/utils';

export default {
  name: 'MenuRoleManageView',
  components: {
    AppModal
  },
  data() {
    return {
      formData: { enabled: null },
      menuRoles: [],
      noRoleAssignMenu: [],
      value: [],
      commonCodeList: [],
      isMode: null,
      isPopupMode: null,
      selectedValue: null,
      isShow: false,
      isDelBtn: false,
      error: { id: false, name: false, enabled: false },
      isAlertShow: false,
      modalText: null
    }
  },
  methods: {
    getMenuList() {
      this.$http.get(APIHandler.buildUrl(['menu']))
          .then(response => {
            let items = response.data;
            items.map(item => {
              item.key = item.id;
              item.label = item.name;
            });
            this.noRoleAssignMenu = items;
      }).catch((error) => {
        const result = errorRender(error.response.status, error.response.data);
        this.isAlertShow = result.isAlertShow;
        this.modalText = result.message + `(${ error.message })`;
      });
    },
    getMenuRoleList() {
      this.$http.get(APIHandler.buildUrl(['menurole']))
          .then(response => {
            this.menuRoles = response.data;
          }).catch((error) => {
            const result = errorRender(error.response.status, error.response.data);
            this.isAlertShow = result.isAlertShow;
            this.modalText = result.message + `(${ error.message })`;
      });
    },
    getMenuRoleRelation(roleId) {
      this.getMenuList();
      this.$http.get(APIHandler.buildUrl(['menurolrelation', roleId]))
          .then(response => {
            let items = response.data;
            let assessRoles = [];
            if (items && items !== '') {
              items.roleAssignMenu.map(item => {
                assessRoles.push(item.id);
              });
              this.value = assessRoles;
              this.isMode = 'mod';
            } else {
              this.isMode = 'add';
            }
          }).catch((error) => {
            const result = errorRender(error.response.status, error.response.data);
            this.isAlertShow = result.isAlertShow;
            this.modalText = result.message + `(${ error.message })`;
      });
    },
    getCommonCodeList() {
      this.$http.get(`/code?pageSize=999&currentPage=1&codeGroupId=DC101`)
          .then(response => {
            this.commonCodeList = response.data.codeVOs;
          }).catch((error) => {
            const result = errorRender(error.response.status, error.response.data);
            this.isAlertShow = result.isAlertShow;
            this.modalText = result.message + `(${ error.message })`;
      });
    },
    onShowPopup(event) {
      const { name } = event.target;
      if (name === 'mod') {
        if (this.selectedValue === null) {
          return null;
        }
        this.$http.get(APIHandler.buildUrl(['menurole', this.selectedValue]))
            .then(response => {
              this.formData = response.data;
            }).catch((error) => {
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
        this.isDelBtn = true;
      } else {
        this.selectedValue = null;
        this.formData = { enabled: null };
        this.isDelBtn = false;
        this.value = [];
        this.noRoleAssignMenu = [];
      }
      this.isShow = true;
    },
    onPopupEvent(name) {
      Object.keys(this.error).map(key => {
        if (!this.formData[key] || this.formData[key] === '' || this.formData[key].length === 0) {
          this.error[key] = true;
        }
      });
      let checkResult = Object.keys(this.error).some(key => {
        return !!this.error[key];
      });
      if (checkResult) {
        return null;
      }
      if (name === 'successBtn') {
        if (!this.selectedValue) {
          this.$http.post(APIHandler.buildUrl(['menurole']), this.formData)
              .then(response => {
                const resultCode = response.status;
                if (resultCode === 200 || 201 || 204) {
                  this.selectedValue = null;
                  this.isMode = null;
                  this.noRoleAssignMenu = [];
                  this.value = [];
                  this.formData = { enabled: null };
                  this.getMenuRoleList();
                }
              }).catch((error) => {
                const result = errorRender(error.response.status, error.response.data);
                this.isAlertShow = result.isAlertShow;
                this.modalText = result.message + `(${ error.message })`;
          });
        } else {
          this.$http.patch(APIHandler.buildUrl(['menurole', this.selectedValue]), this.formData)
              .then(response => {
                const resultCode = response.status;
                if (resultCode === 200 || 201 || 204) {
                  this.selectedValue = null;
                  this.isMode = null;
                  this.noRoleAssignMenu = [];
                  this.value = [];
                  this.formData = { enabled: null };
                  this.getMenuRoleList();
                }
              }).catch((error) => {
                const result = errorRender(error.response.status, error.response.data);
                this.isAlertShow = result.isAlertShow;
                this.modalText = result.message + `(${ error.message })`;
          });
        }
      } else {
        this.$http.delete(APIHandler.buildUrl(['menurole', this.selectedValue]))
            .then(response => {
              const resultCode = response.status;
              if (resultCode === 200 || 201 || 204) {
                this.selectedValue = null;
                this.isMode = null;
                this.noRoleAssignMenu = [];
                this.value = [];
                this.formData = { enabled: null };
                this.getMenuRoleList();
              }
            }).catch((error) => {
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      }
      this.isShow = false;
    },
    onChange(event) {
      const { name, value } = event.target;
      this.selectedValue = value;
      this.noRoleAssignMenu = [];
      this.value = [];
      this.getMenuRoleRelation(value);
    },
    onClose() {
      this.isShow = false;
      this.isAlertShow = false;
    },
    onSave() {
      if (this.isMode === 'add') {
        this.$http.post(APIHandler.buildUrl(['menurolrelation', this.selectedValue]), this.value)
            .then(response => {
              const resultCode = response.status;
              if (resultCode === 200 || 201 || 204) {
                this.selectedValue = null;
                this.isMode = null;
                this.noRoleAssignMenu = [];
                this.value = [];
                this.formData = { enabled: null };
                this.getMenuRoleList();
              }
            }).catch((error) => {
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      } else {
        this.$http.patch(APIHandler.buildUrl(['menurolrelation', this.selectedValue]), this.value)
            .then(response => {
              const resultCode = response.status;
              if (resultCode === 200 || 201 || 204) {
                this.selectedValue = null;
                this.isMode = null;
                this.noRoleAssignMenu = [];
                this.value = [];
                this.formData = { enabled: null };
                this.getMenuRoleList();
              }
            }).catch((error) => {
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      }
    },
    onPopupFocusoutEvent(event) {
      const { name, value } = event.target;
      if (value !== '') {
        this.error[name] = false;
      }
    }
  },
  mounted() {
    this.getMenuRoleList();
    this.getCommonCodeList();
    document.getElementsByClassName('el-transfer-panel')[0].style.width = '350px';
    document.getElementsByClassName('el-transfer-panel')[1].style.width = '350px';
    document.getElementsByClassName('el-transfer__buttons')[0].children[0].style.marginBottom = 0;

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