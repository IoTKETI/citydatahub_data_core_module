<template>
  <div>
    <form>
      <fieldset>
        <legend>필드셋 제목</legend>
        <!-- section-write -->
        <section class="section">
          <div class="section__header">
            <h4 class="section__title">{{ $t('dataModel.attributeDetails') }}</h4>
            <div class="button__group" style="margin: 0; padding-top: 5px;">
              <button
                  :class="`button__primary ${ isDisabled ? 'btn-disabled' : null }`"
                  type="button"
                  @click="onEntityEvent"
                  :disabled="isDisabled"
              >
                {{ $t('comm.save') }}
              </button>
            </div>
          </div>
          <div class="section__content">
            <table class="table--row">
              <caption>테이블 제목</caption>
              <colgroup>
                <col style="width:130px">
                <col style="width:auto">
                <col style="width:100px">
                <col style="width:auto">
                <col style="width:100px">
                <col style="width:auto">
                <col style="width:120px">
                <col style="width:auto">
              </colgroup>
              <tbody>
              <tr>
                <th class="icon__require">{{ $t('dataModel.attribute') }}</th>
                <td>
                  <label>
                    <el-select
                        v-model="formData['name']"
                        filterable size="mini"
                        @change="(value) => onChangeDataModel('name', value)"
                        @blur="onFocusoutEvent"
                        placeholder="Please select one"
                        :class="error['name'] ? `error__border` : ``"
                        name="name"
                        style="width: 100%;"
                    >
                      <el-option
                          size="mini"
                          style="font-size: 12px;"
                          v-for="item in dataModelTypes"
                          :key="item"
                          :label="item"
                          :value="item"
                      >
                      </el-option>
                    </el-select>
                  </label>
                  <br>
                  <span v-show="error['name']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
                <th>{{ $t('dataModel.accessMode') }}</th>
                <td>
                  <label>
                    <select
                        v-model="formData['accessMode']"
                        :class="isDisabled ? `input__text input__disabled` : `input__text`"
                        :disabled="isDisabled"
                        name="accessMode"
                    >
                      <option
                          disabled
                          :value="null"
                      >
                        Please select one
                      </option>
                      <option
                          v-for="item in accessModeList"
                          :value="item.codeId"
                      >
                        {{ item.codeName }}
                      </option>
                    </select>
                  </label>
<!--                  <input class="input__text" type="text" name="accessMode" v-model="formData['accessMode']" :disabled="isDisabled" />-->
                </td>
                <th>{{ $t('dataModel.description') }}</th>
                <td>
                  <label>
                    <input class="input__text" type="text" name="description" v-model="formData['description']" :disabled="isDisabled" />
                  </label>
                </td>
                <th rowspan="7">
                  <el-tooltip placement="left">
                    <div slot="content">
                      {{ $t('comm.required2') }}
                    </div>
                    <span style="background: #ffffff;border: 1px solid #2b2b2b;border-radius: 3px;padding-left: 5px;padding-right: 2px;cursor: help;">
                      ?
                    </span>
                  </el-tooltip>
                  &nbsp
                  {{ $t('dataModel.objectDetails') }}
                </th>
                <td colspan="2" rowspan="7">
                  <div class="button__group">
                    <button
                        class="button__util button__util--add material-icons"
                        type="button"
                        name="add"
                        @click="onObjectMemberEvent"
                        :disabled="isDisabled"
                    >
                      {{ $t('comm.add') }}
                    </button>
                    <button
                        class="button__util button__util--remove material-icons"
                        type="button"
                        name="delete"
                        @click="onObjectMemberEvent"
                        :disabled="isDisabled"
                    >
                      {{ $t('comm.delete') }}
                    </button>
                    <button
                        class="button__util button__util--search material-icons"
                        type="button"
                        name="modify"
                        @click="onObjectMemberEvent"
                        :disabled="isDisabled"
                    >
                      {{ $t('comm.getInfo') }}
                    </button>
                  </div>
                  <ElementTree
                      :tree-data="treeData"
                      node-key="objectMem"
                      tree-height="220px"
                      @on-tree-event="onTreeEvent"
                  />
                </td>
              </tr>
              <tr>
                <th class="icon__require">{{ $t('dataModel.attributeUri') }}</th>
                <td>
                  <label>
                    <el-select
                        v-model="formData['attributeUri']"
                        filterable size="mini"
                        @change="(value) => onChangeDataModel('attributeUri', value)"
                        @blur="onFocusoutEvent"
                        placeholder="Please select one"
                        :class="error['attributeUri'] ? `error__border` : ``"
                        name="attributeUri"
                        style="width: 100%;"
                    >
                      <el-option
                          size="mini"
                          style="font-size: 12px;"
                          v-for="item in dataModelUris"
                          :key="item"
                          :label="item"
                          :value="item"
                      >
                      </el-option>
                    </el-select>
                  </label>
                  <br>
                  <span v-show="error['attributeUri']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
                <th>{{ $t('dataModel.maxLength') }}</th>
                <td>
                  <label>
                    <input class="input__text" type="text" name="maxLength" v-model="formData['maxLength']" :disabled="isDisabled" />
                  </label>
                </td>
                <th rowspan="6">{{ $t('dataModel.enumerations') }}</th>
                <td rowspan="6">
                  <div class="button__group" style="margin: 0 0 5px;">
                    <button
                        class="button__util button__util--add material-icons"
                        type="button"
                        :disabled="isDisabled"
                        @click="onValueEnumAdd"
                    >
                      {{ $t('comm.add') }}
                    </button>
                    <button
                        class="button__util button__util--remove material-icons"
                        type="button"
                        :disabled="isDisabled"
                        @click="onValueEnumDel"
                    >
                      {{ $t('comm.delete') }}
                    </button>
                  </div>
                  <label>
                    <input class="input__text" type="text" :disabled="isDisabled" v-model="addText" />
                  </label>
                  <AppTable
                      :meta-data="[]"
                      :table-items="formData['valueEnum']"
                      tableHeight="160px"
                      overflowY="auto"
                      @on-row-event="onTableRowEvent"
                  />
                </td>
              </tr>
              <tr>
                <th class="icon__require">{{ $t('dataModel.attributeType') }}</th>
                <td>
                  <label>
                    <select
                        v-model="formData['attributeType']"
                        :class="isDisabled ? `input__text input__disabled` : `input__text`"
                        :style="error['attributeType'] ? `border-color: #f56c6c;` : null"
                        :disabled="isDisabled"
                        name="attributeType"
                        @change="onChange"
                        @blur="onFocusoutEvent"
                    >
                      <option
                          disabled
                          :value="null"
                      >
                        Please select one
                      </option>
                      <option
                          v-for="item in attrTypeList"
                          :value="item.codeId"
                      >
                        {{ item.codeName }}
                      </option>
                    </select>
                  </label>
                  <br>
                  <span v-show="error['attributeType']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
                <th>{{ $t('dataModel.minLength') }}</th>
                <td>
                  <label>
                    <input class="input__text" type="text" name="minLength" v-model="formData['minLength']" :disabled="isDisabled" />
                  </label>
                </td>
              </tr>
              <tr>
                <th class="icon__require">{{ $t('dataModel.dataType') }}</th>
                <td>
                  <label>
                    <select
                        v-model="formData['valueType']" :disabled="isDisabled"
                        :class="isDisabled ? `input__text input__disabled` : `input__text`"
                        :style="error['valueType'] ? `border-color: #f56c6c;` : null"
                        name="valueType"
                        @change="onChange2"
                        @blur="onFocusoutEvent"
                    >
                      <option
                          disabled
                          :value="null"
                      >
                        Please select one
                      </option>
                      <option v-for="item in optionList" :value="item.codeId">
                        {{ item.codeName }}
                      </option>
                    </select>
                  </label>
                  <br>
                  <span v-show="error['valueType']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
                <th rowspan="2">> OR >=</th>
                <td rowspan="2">
                  <label>
                    <input class="input__text" type="text" placeholder=">" name="greaterThan" v-model="formData['greaterThan']" style="margin-bottom: 4px;" :disabled="isDisabled"/>
                  </label>
                  <br>
                  <label>
                    <input class="input__text" type="text" placeholder=">=" name="greaterThanOrEqualTo" v-model="formData['greaterThanOrEqualTo']" :disabled="isDisabled" />
                  </label>
                </td>
              </tr>
              <tr>
                <th>
                  <label :title="$t('dataModel.observedAtIn')">
                    {{ $t('dataModel.observedAtIn') }}
                  </label>
                </th>
                <td>
                  <label>
                    <select
                        :class="isDisabled ? `input__text input__disabled` : `input__text`"
                        v-model="formData['hasObservedAt']"
                        :disabled="isDisabled"
                        name="hasObservedAt"
                    >
                      <option
                          v-for="item in commonCodeList"
                          :value="item.codeId"
                      >
                        {{ item.codeName }}
                      </option>
                    </select>
                  </label>
                </td>

              </tr>
              <tr>
                <th>
                  <label :title="$t('dataModel.unitCodeIn')">
                    {{ $t('dataModel.unitCodeIn') }}
                  </label>
                </th>
                <td>
                  <label>
                    <select
                        :class="isDisabled ? `input__text input__disabled` : `input__text`"
                        v-model="formData['hasUnitCode']"
                        :disabled="isDisabled"
                        name="hasUnitCode"
                    >
                      <option
                          v-for="item in commonCodeList"
                          :value="item.codeId"
                      >
                        {{ item.codeName }}
                      </option>
                    </select>
                  </label>
                </td>
                <th rowspan="2">< OR <=</th>
                <td rowspan="2">
                  <label>
                    <input class="input__text" type="text" placeholder="<" name="lessThan" v-model="formData['lessThan']" style="margin-bottom: 4px;" :disabled="isDisabled" />
                  </label>
                  <br>
                  <label>
                    <input class="input__text" type="text" placeholder="<=" name="lessThanOrEqualTo" v-model="formData['lessThanOrEqualTo']" :disabled="isDisabled" />
                  </label>
                </td>
              </tr>
              <tr>
                <th>{{ $t('dataModel.mandatory') }}</th>
                <td>
                  <label>
                    <select
                        :class="isDisabled ? `input__text input__disabled` : `input__text`"
                        v-model="formData['isRequired']"
                        :disabled="isDisabled"
                        name="isRequired"
                    >
                      <option
                          v-for="item in commonCodeList"
                          :value="item.codeId"
                      >
                        {{ item.codeName }}
                      </option>
                    </select>
                  </label>
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
        @on-event-modal="onPopupSave"
        :title="$t('dataModel.objectMemTitle')"
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
                  <th class="icon__require">{{ $t('dataModel.objectMemName') }}</th>
                  <td>
                    <label>
                      <input
                          :class="popupErrors['name'] ? `input__text error__border` : `input__text`"
                          name="name"
                          type="text"
                          v-model="popupFormData['name']"
                          :disabled="isDisabled"
                          @blur="onPopupFocusoutEvent"
                      />
                    </label>
                    <br>
                    <span v-show="popupErrors['name']" class="error__color">
                      {{ $t('comm.required') }}
                    </span>
                  </td>
                  <th>
                    {{ $t('comm.mandatory') }}
                  </th>
                  <td>
                    <label>
                      <select
                          :class="isDisabled ? `input__text input__disabled` : `input__text`"
                          v-model="popupFormData['isRequired']"
                          name="isRequired"
                      >
                        <option value="true">{{ $t('comm.yes') }}</option>
                        <option value="false">{{ $t('comm.no') }}</option>
                      </select>
                    </label>
                  </td>
                </tr>
                <tr>
                  <th>{{ $t('comm.maxLength') }}</th>
                  <td>
                    <label>
                      <input class="input__text" type="text" name="maxLength" v-model="popupFormData['maxLength']" :disabled="isDisabled" />
                    </label>
                  </td>
                  <th>{{ $t('comm.minLength') }}</th>
                  <td>
                    <label>
                      <input class="input__text" type="text" name="minLength" v-model="popupFormData['minLength']" :disabled="isDisabled" />
                    </label>
                  </td>
                </tr>
                <tr>
                  <th>> OR >=</th>
                  <td>
                    <label>
                      <input class="input__text" type="text" placeholder=">" style="margin-bottom: 4px;" v-model="popupFormData['greaterThan']" :disabled="isDisabled"/>
                    </label>
                    <br>
                    <label>
                      <input class="input__text" type="text" placeholder=">=" v-model="popupFormData['greaterThanOrEqualTo']" :disabled="isDisabled" />
                    </label>
                  </td>
                  <th>< OR <=</th>
                  <td>
                    <label>
                      <input class="input__text" type="text" placeholder="<" v-model="popupFormData['lessThan']" style="margin-bottom: 4px;" :disabled="isDisabled" />
                    </label>
                    <br>
                    <label>
                      <input class="input__text" type="text" placeholder="<=" v-model="popupFormData['lessThanOrEqualTo']" :disabled="isDisabled" />
                    </label>
                  </td>
                </tr>
                <tr>
                  <th class="icon__require">{{ $t('dataModel.objectDataType') }}</th>
                  <td colspan="3">
                    <label>
                      <select
                          v-model="popupFormData['valueType']"
                          :class="isDisabled ? `input__text input__disabled` : `input__text`"
                          :style="popupErrors['valueType'] ? `border-color: #f56c6c;` : null"
                          :disabled="isDisabled"
                          name="valueType"
                          @blur="onPopupFocusoutEvent"
                      >
                        <option
                            disabled
                            :value="null"
                        >
                          Please select one
                        </option>
                        <option v-for="item in objectValueType" :value="item.codeId">
                          {{ item.codeName }}
                        </option>
                      </select>
                    </label>
                    <br>
                    <span v-show="popupErrors['valueType']" class="error__color">
                      {{ $t('comm.required') }}
                    </span>
                  </td>
                </tr>
                <tr>
                  <th>{{ $t('dataModel.description') }}</th>
                  <td colspan="3">
                    <label>
                      <input class="input__text" type="text" v-model="popupFormData['description']" :disabled="isDisabled" />
                    </label>
                  </td>
                </tr>
                <tr>
                  <th rowspan="4">{{ $t('dataModel.enumerations') }} Enum</th>
                  <td rowspan="4" colspan="3">
                    <div class="button__group" style="margin: 0 0 5px;">
                      <button
                          class="button__util button__util--add material-icons"
                          type="button"
                          :disabled="isDisabled"
                          @click="onPopupValueEnumAdd"
                      >
                        {{ $t('comm.add') }}
                      </button>
                      <button
                          class="button__util button__util--remove material-icons"
                          type="button"
                          :disabled="isDisabled"
                          @click="onPopupValueEnumDel"
                      >
                        {{ $t('comm.delete') }}
                      </button>
                    </div>
                    <label>
                      <input class="input__text" type="text" :disabled="isDisabled" v-model="addText" />
                    </label>
                    <AppTable
                        :meta-data="[]"
                        :table-items="popupFormData['valueEnum']"
                        tableHeight="125px"
                        overflowY="auto"
                        @on-row-event="onPopupTableRowEvent"
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
        @close-modal="onAlertClose"
        modalSize="w-360"
        :content="modalText"
        :close-name="$t('comm.ok')"
        :isCancelBtn="true"
    />
  </div>
</template>

<script>
/**
 * Data Model Pro view page (container)
 */
import AppTable from '@/components/AppTable';
import ElementTree from '@/components/ElementTree';
import AppModal from '@/components/AppModal';
import _ from 'lodash';
import treeSearch from 'tree-search';


export default {
  name: 'DataModelProView',
  components: {
    ElementTree,
    AppTable,
    AppModal
  },
  props: {
    formData: Object,
    treeData: Array,
    isDisabled: Boolean,
    error: Object,
    context: Object
  },
  watch: {
    formData() {
      // props change watcher
      // formData event listener
      const value = this.formData.attributeType;
      if (value === 'Property') {
        this.getCodeList('DC003');
      } else if (value === 'GeoProperty') {
        this.getCodeList('DC004');
      } else {
        this.getCodeList('DC005');
      }

      if (!this.formData['isRequired']) {
        this.formData['isRequired'] = false;
      }
      if (!this.formData['hasObservedAt']) {
        this.formData['hasObservedAt'] = false;
      }
      if (!this.formData['hasUnitCode']) {
        this.formData['hasUnitCode'] = false;
      }
    },
    context(contextData) {
      this.dataModelData = contextData;
      this.dataModelTypes = Object.keys(contextData).map(key => key);
      this.dataModelUris = Object.keys(contextData).map(key => contextData[key]);
    }
  },
  data() {
    return {
      optionList: [],
      attrTypeList: [],
      accessModeList: [],
      commonCodeList:[],
      addText: null,
      delText: null,
      treeId: null,
      treeNode: null,
      treeInfo: null,
      isShow: false,
      isAlertShow: false,
      modalText: null,
      popupFormData: {},
      popupErrors: {
        name: false, valueType: false
      },
      change2Value: null,
      objectValueType: [],
      dataModelData: null,
      dataModelTypes: [],
      dataModelUris: []
    }
  },
  methods: {
    onChangeDataModel(name, value) {
      if (name === 'name') {
        this.formData['name'] = value;
        this.formData['attributeUri'] = this.dataModelData[value];
      } else {
        Object.keys(this.dataModelData).some(key => {
          if (value === this.dataModelData[key]) {
            this.formData['name'] = key;
          }
        });
        this.formData['attributeUri'] = value;
      }
    },
    onValueEnumAdd() {
      if (this.addText) {
        if (!this.formData.valueEnum) {
          this.formData.valueEnum = [];
        }
        this.formData.valueEnum.push(this.addText);
        this.addText = null;
      }
    },
    onPopupValueEnumAdd() {
      if (this.addText) {
        if (!this.popupFormData.valueEnum) {
          this.popupFormData.valueEnum = [];
        }
        this.popupFormData.valueEnum.push(this.addText);
        this.addText = null;
      }
    },
    onValueEnumDel() {
      if (this.delText) {
        let data = { ...this.formData };
        data.valueEnum.some((item, index) => {
          if (item === this.delText) {
            data.valueEnum.splice(index, 1);
          }
        });
        this.$emit('on-value-enum-del', data);
      }
      this.delText = null;
    },
    onPopupValueEnumDel() {
      if (this.delText) {
        let data = { ...this.popupFormData };
        data.valueEnum.some((item, index) => {
          if (item === this.delText) {
            data.valueEnum.splice(index, 1);
          }
        });
      }
      this.delText = null;
    },
    onTableRowEvent(value) {
      this.delText = value;
    },
    onPopupTableRowEvent(value) {
      this.delText = value;
    },
    onEntityEvent() {
      this.popupFormData = {};
      this.$emit('on-entity-add', this.formData);
    },
    onChange2(event) {
      const { name, value } = event.target;
      this.change2Value = value;
      this.getPopupCodeList(value);
    },
    onChange(event) {
      const { name, value } = event.target;
      if (value === 'Property') {
        this.getCodeList('DC003');
      } else if (value === 'GeoProperty') {
        this.getCodeList('DC004');
      } else {
        this.getCodeList('DC005');
      }
    },
    onObjectMemberEvent(event) {
      // ArrayObject
      let valueType = this.formData['valueType'];
      this.getPopupCodeList(valueType);
      if (this.treeId === null) {
        return null;
      }
      if (event.target.name === 'add') {
        if (this.treeNode.level > 2) {
          this.isAlertShow = true;
          this.modalText = 'ROOT 를 기준으로 2레벨까지 입력 가능합니다.';
          return null;
        }
        this.popupFormData = {};
      }

      if (this.treeId !== 1 && event.target.name === 'modify') {
        this.backupData = _.cloneDeep(this.formData);
        const objMembers = this.formData.objectMembers;
        const find = treeSearch('objectMembers');

        if (this.treeNode.level < 3) {
          let tempData = find(objMembers, 'name', this.treeId);
          if (tempData.valueEnum) {
            let result = tempData.valueEnum.map(item => {
              return item;
            });
            tempData.valueEnum.length = 0;
            tempData.valueEnum = result;
          }
          this.popupFormData = tempData;
        } else {
          const parentId = this.treeNode.parent.data.id === 1 ? attrName : this.treeNode.parent.data.id;
          const resultFindData = find(objMembers, 'name', parentId);
          const formChildren = resultFindData.objectMembers || resultFindData;

          let result = null;
          formChildren.some(d => {
            if (d.name === this.treeId) {
              result = d;
            }
          });
          this.popupFormData = result;
        }
      }
      if (!this.popupFormData.isRequired) {
        this.popupFormData.isRequired = false;
      }
      const { name } = event.target;
      if (this.treeId === 1 && name === 'modify') {
        return null;
      }
      if (name === 'delete') {
        // tree delete
        const parent = this.treeNode.parent;
        const children = parent.data.children || parent.data;
        const index = children.findIndex(d => d.id === this.treeId);
        children.splice(index, 1);

        // formData delete
        const resultObject = [this.formData];
        const resultFind = treeSearch('objectMembers');
        const parentId = this.treeNode.parent.data.id === 1 ? this.formData.name : this.treeNode.parent.data.id;
        const resultFindData = resultFind(resultObject, 'name', parentId);
        const formChildren = resultFindData.objectMembers || resultFindData;
        const index2 = formChildren.findIndex(d => d.name === this.treeId);
        formChildren.splice(index2, 1);

        // init tree data
        this.treeId = null;
        this.treeInfo = null;
        this.treeNode = null;
        return null;
      }
      this.isShow = true;
      this.$emit('on-object-member-event', this.treeId, this.treeInfo, this.treeNode, name);
    },
    onTreeEvent(id, data, node) {
      this.treeId = id;
      this.treeInfo = data;
      this.treeNode = node;
    },
    onClose() {
      this.isShow = false;
      this.treeId = null;
    },
    onAlertClose() {
      this.isAlertShow = false;
    },
    onPopupSave() {
      Object.keys(this.popupErrors).map(key => {
        if (!this.popupFormData[key] || this.popupFormData[key] === '' || this.popupFormData[key].length === 0) {
          this.popupErrors[key] = true;
        }
      });
      let checkResult = Object.keys(this.popupErrors).some(key => {
        return !!this.popupErrors[key];
      });
      if (checkResult) {
        return null;
      }
      if (this.treeId === 1) {
        if (this.treeData[0].children === null) {
          this.treeData[0].children = [];
        }
        this.treeData[0].children.push({
          id: this.popupFormData.name,
          label: `${ this.popupFormData.name } (${ this.popupFormData.valueType })`,
          children: []
        });
        if (!this.formData.objectMembers) {
          this.formData.objectMembers = [];
        }
        this.formData.objectMembers.push(this.popupFormData);
      } else {
        const objMembers = this.treeData[0].children;
        const find = treeSearch('children');
        const findData = find(objMembers, 'id', this.treeId);
        if (findData.children === null){
          findData.children = [];
        }
        findData.children.push({
          id: this.popupFormData.name,
          label: `${ this.popupFormData.name } (${ this.popupFormData.valueType })`,
          children: []
        });
        const resultObject = this.formData.objectMembers;
        const resultFind = treeSearch('objectMembers');
        const resultFindData = resultFind(resultObject, 'name', this.treeId);
        if (!resultFindData.objectMembers){
          resultFindData.objectMembers = [];
        }
        resultFindData.objectMembers.push(this.popupFormData);
      }
      this.popupFormData = {};
      this.isShow = false;
    },
    onFocusoutEvent(event) {
      const { name, value } = event.target;
      if (value !== '') {
        this.error[name] = false;
      }

      const { mode } = this.$route.query;
      if (name === 'name' || name === 'attributeUri') {
        this.error['name'] = false;
        this.error['attributeUri'] = false;
        document.querySelectorAll('.el-input__inner')[mode === 'mod' ? 2 : 3].style = '';
        document.querySelectorAll('.el-input__inner')[mode === 'mod' ? 3 : 4].style = '';
      }
    },
    onPopupFocusoutEvent(event) {
      const { name, value } = event.target;
      if (value !== '') {
        this.popupErrors[name] = false;
      }
    },
    getCodeList(attrType) {
      if (attrType) {
        this.$http.get(`/code?pageSize=999&currentPage=1&codeGroupId=${ attrType }`)
            .then(response => {
              const { codeVOs } = response.data;
              this.optionList.length = 0;
              codeVOs.map(item => {
                item.codeName = this.$i18n.t(`codes.${item.codeId}`);                
                this.optionList.push(item);
              });
            });
      } else {
        this.$http.get('/code?pageSize=999&currentPage=1&codeGroupId=DC001')
            .then(response => {
              const { codeVOs } = response.data;
              this.attrTypeList.length = 0;
              codeVOs.map(item => {
                item.codeName = this.$i18n.t(`codes.${item.codeId}`);
                this.attrTypeList.push(item);
              });
            });
      }
    },
    getPopupCodeList(value) {
      // ArrayObject
      let codeGroupId = 'DC006';
      if (value === 'ArrayObject') {
        codeGroupId = 'DC019';
      }
      this.$http.get(`/code?pageSize=999&currentPage=1&codeGroupId=${codeGroupId}`)
          .then(response => {
            const { codeVOs } = response.data;
            this.objectValueType.length = 0;
            codeVOs.map(item => {
              item.codeName = this.$i18n.t(`codes.${item.codeId}`);
              this.objectValueType.push(item);
            });
          });
    },
    getAccessModeList() {
      this.$http.get(`/code?pageSize=999&currentPage=1&codeGroupId=DC002`)
          .then(response => {
            const { codeVOs } = response.data;
            this.accessModeList.length = 0;
            codeVOs.map(item => {
              item.codeName = this.$i18n.t(`codes.${item.codeId}`);              
              this.accessModeList.push(item);
            });
          });
    },
    getCommonCodeList() {
      this.$http.get(`/code?pageSize=999&currentPage=1&codeGroupId=DC101`)
          .then(response => {
            const { codeVOs } = response.data;
            this.commonCodeList.length = 0;
            codeVOs.map(item => {
              item.codeName = this.$i18n.t(`codes.${item.codeId}`);              
              this.commonCodeList.push(item);
            });
          });
    }
  },
  mounted() {
    this.getCodeList();
    this.getPopupCodeList();
    this.getAccessModeList();
    this.getCommonCodeList();
  }
}
</script>

<style scoped>
.button__util--search:before {
  content:'search'
}
.btn-disabled {
  background: #dddddd;
  cursor: no-drop;
}
.input__disabled {
  background: #f5f5f5;
  border-color: #f5f5f5;
}
.error__color {
  color: #f56c6c; font-size: 10px;
}
.error__border {
  border-color: #f56c6c;
}
</style>