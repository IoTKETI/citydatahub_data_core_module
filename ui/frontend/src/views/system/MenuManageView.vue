<template>
  <div>
    <h3 class="content__title">{{ $t('menu.title') }}</h3>
    <form>
      <fieldset>
        <legend>필드셋 제목</legend>
        <!-- section-write -->
        <section class="section">
          <div class="section__header">
            <h4 class="section__title">{{ $t('menu.menuConf') }}</h4>
          </div>
          <div class="section__content">
            <ElementTree
                node-key="id"
                :node-name="true"
                :tree-data="treeData"
                treeHeight="350px"
                @on-tree-event="onTreeEvent"
            />
          </div>
        </section>
      </fieldset>
    </form>
    <form>
      <fieldset>
        <legend>필드셋 제목</legend>
        <!-- section-write -->
        <section class="section">
          <div class="section__header">
            <h4 class="section__title">{{ $t('menu.menuInfo') }}</h4>
          </div>
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
                <th class="icon__require">{{ $t('menu.id') }}</th>
                <td>
                  <label>
                    <input
                        type="text"
                        v-model="formData['id']"
                        :class="error['id'] ? `input__text error__border` : `input__text`"
                        :disabled="isDisabled"
                        @blur="onFocusoutEvent"
                        name="id"
                    />
                  </label>
                  <br>
                  <span v-show="error['id']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
                <th>URL</th>
                <td colspan="3">
                  <label>
                    <input type="text" v-model="formData['url']" class="input__text" name="url" />
                  </label>
                </td>
              </tr>
              <tr>
                <th class="icon__require">{{ $t('menu.name') }}</th>
                <td>
                  <label>
                    <input
                        type="text"
                        v-model="formData['name']"
                        :class="error['name'] ? `input__text error__border` : `input__text`"
                        name="name"
                        @blur="onFocusoutEvent"
                    />
                  </label>
                  <br>
                  <span v-show="error['name']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
                <th>{{ $t('menu.parentMenuId') }}</th>
                <td>
                  <label>
                    <input type="text" v-model="formData['upMenuId']" class="input__text" />
                  </label>
                </td>
                <th>{{ $t('menu.order') }}</th>
                <td>
                  <label>
                    <input type="text" v-model="formData['sortOrder']" class="input__text" />
                  </label>
                </td>
              </tr>
              <tr>
                <th class="icon__require">{{ $t('menu.level') }}</th>
                <td>
                  <label>
                    <select
                        v-model="formData['level']"
                        class="input__text"
                        :style="error['level'] ? `border-color: #f56c6c;` : null"
                        name="level"
                        @blur="onFocusoutEvent"
                    >
                      <option
                          disabled
                          :value="null"
                      >
                        Please select one
                      </option>
                      <option value="1">1</option>
                      <option value="2">2</option>
                    </select>
<!--                    <input-->
<!--                        type="text"-->
<!--                        v-model="formData['level']"-->
<!--                        :class="error['level'] ? `input__text error__border` : `input__text`"-->
<!--                        name="level"-->
<!--                        @blur="onFocusoutEvent"-->
<!--                    />-->
                  </label>
                  <br>
                  <span v-show="error['level']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
                <th class="icon__require">{{ $t('menu.isActive') }}</th>
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
                <th>{{ $t('comm.creator') }}</th>
                <td>
                  <label>
                    <input type="text" v-model="formData['creatorId']" class="input__text" disabled="true" />
                  </label>
                </td>
              </tr>
              <tr>
                <th>{{ $t('comm.creationDate') }}</th>
                <td>
                  <label>
                    <input type="text" v-model="formData['createdAt']" class="input__text" disabled="true" />
                  </label>
                </td>
                <th>{{ $t('comm.modifier') }}</th>
                <td>
                  <label>
                    <input type="text" v-model="formData['modifierId']" class="input__text" disabled="true" />
                  </label>
                </td>
                <th>{{ $t('comm.modifierDate') }}</th>
                <td>
                  <label>
                    <input type="text" v-model="formData['modifiedAt']" class="input__text" disabled="true" />
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
              class="button__primary"
              type="button"
              @click="initMenu"
          >
            {{ $t('comm.add') }}
          </button>
          <button
              class="button__secondary"
              type="button"
              @click="onDelete"
          >
            {{ $t('comm.delete') }}
          </button>
        </div>
      </fieldset>
    </form>
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
 * Menu manage view page (container)
 * @component element-ui
 */
import ElementTree from '@/components/ElementTree';
import AppModal from '@/components/AppModal';
import { APIHandler } from '@/modules/api-handler';
import { traverse, errorRender } from '@/modules/utils';

export default {
  name: 'MenuManageView',
  components: {
    ElementTree,
    AppModal
  },
  data() {
    return {
      treeData: null,
      formData: { enabled: null, level: null },
      isDisabled: false,
      selectedId: null,
      commonCodeList: [],
      error: { id: false, name: false, enabled: false, level: false },
      isAlertShow: false,
      modalText: null
    }
  },
  methods: {
    getMenuList() {

      this.$http.get(APIHandler.buildUrl(['menu']))
          .then(response => {
            const items = response.data;
            if (items !== null && items !== '') {
              let rootNodes = [];
              items.map((item, index) => {
                const key = `leftMenu.${item.id}`;
                item.name = this.$i18n.t(key);
                if (item.level === 1) {
                  return rootNodes.push(item);
                }
                return traverse(rootNodes, item, index);
              });
              rootNodes.sort((a, b) => a.sortOrder - b.sortOrder);
              rootNodes.forEach(node => {
                if(node['children']) {
                  node['children'] = node['children'].sort(
                    (a, b) => a.sortOrder - b.sortOrder
                  );
                }
              });
              this.treeData = rootNodes;
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
    initMenu() {
      const level = this.formData.level;
      if (level === 2 || level > 2) {
        this.isAlertShow = true;
        this.modalText = this.$i18n.t('comm.menuCheckMessage');
        return null;
      }
      const id = this.formData.id;
      this.formData = { enabled: null, upMenuId: id, level: null };
      this.isDisabled = false;
      this.selectedId = null;
    },
    onClose() {
      this.isAlertShow = false;
    },
    onTreeEvent(item) {
      this.error = { id: false, name: false, enabled: false, level: false };
      this.selectedId = item;
      this.$http.get(APIHandler.buildUrl(['menu', item]))
          .then(response => {
            this.formData = response.data;
            this.isDisabled = true;
          }).catch((error) => {
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
    onSave() {
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
      if (this.selectedId) {
        this.$http.patch(APIHandler.buildUrl(['menu', this.selectedId]), this.formData)
            .then(response => {
              const resultCode = response.status;
              if (resultCode === 200 || 201 || 204) {
                this.formData = { enabled: null, level: null };
                this.isDisabled = false;
                this.getMenuList();
              }
            }).catch((error) => {
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      } else {
        this.$http.post(APIHandler.buildUrl(['menu']), this.formData)
            .then(response => {
              const resultCode = response.status;
              if (resultCode === 200 || 201 || 204) {
                this.formData = {};
                this.isDisabled = false;
                this.getMenuList();
              }
            }).catch((error) => {
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      }
    },
    onDelete() {
      if (this.selectedId) {
        this.$http.delete(APIHandler.buildUrl(['menu', this.selectedId]))
            .then(response => {
              const resultCode = response.status;
              if (resultCode === 200 || 201 || 204) {
                this.formData = {};
                this.isDisabled = false;
                this.getMenuList();
              }
            }).catch((error) => {
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      }
    }
  },
  mounted() {
    document.querySelectorAll('.breadcrumb__list')[0].innerText = this.$i18n.t('menu.systemManage');

    this.getMenuList();
    this.getCommonCodeList();
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