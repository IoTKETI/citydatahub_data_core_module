<template>
  <form>
    <fieldset>
      <legend>필드셋 제목</legend>
      <!-- section-write -->
      <section class="section">
        <div class="section__header">
          <h4 class="section__title">{{ $t('dataModel.entityDetail') }}</h4>
        </div>
        <div class="section__content">
          <table class="table--row">
            <caption>테이블 제목</caption>
            <colgroup>
              <col style="width:120px">
              <col style="width:auto">
              <col style="width:120px">
              <col style="width:auto">
              <col style="width:130px">
              <col style="width:auto">
            </colgroup>
            <tbody>
              <tr>
                <th class="icon__require" rowspan="1">{{ $t('dataModel.context') }}</th>
                <td rowspan="1" colspan="3">
                  <div class="button__group" style="margin: 0 0 5px;">
                    <button class="button__util button__util--search material-icons" type="button"
                      @click="getContextSearch">
                      {{ $t('comm.ok') }}
                    </button>
                    <button class="button__util button__util--add material-icons" type="button" @click="onContextAdd">
                      {{ $t('comm.add') }}
                    </button>
                    <button class="button__util button__util--remove material-icons" type="button"
                      @click="onContextDel">
                      {{ $t('comm.delete') }}
                    </button>
                  </div>
                  <label>
                    <input class="input__text" type="text" v-model="addText" />
                  </label>
                  <AppTable :meta-data="[]" :table-items="formData['context']" tableHeight="130px" overflowY="auto"
                    @on-row-event="onTableRowEvent" :class-name="error['context'] ? `error__border` : null" />
                  <span v-show="error['context']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
                <th rowspan="7" class="icon__require">
                  {{ $t('dataModel.entityAttr') }}
                </th>
                <td colspan="2" rowspan="7">
                  <div class="button__group">
                    <button type="button" class="button__util button__util--add material-icons" name="add"
                      @click="onEntityEvent">
                      {{ $t('comm.add') }}
                    </button>
                    <button type="button" name="delete" class="button__util button__util--remove material-icons"
                      @click="onEntityEvent">
                      {{ $t('comm.delete') }}
                    </button>
                  </div>
                  <ElementTree :tree-data="treeData" nodeKey="attributes" tree-height="310px"
                    :className="error['attributes'] ? `error__border` : null" @on-tree-event="onTreeEvent">
                    <el-tooltip placement="left">
                      <div slot="content">
                        {{ $t('comm.required2') }}
                        * 속성 값 : Object, ArrayObject 일 경우, 필수입력 입니다.
                      </div>
                    </el-tooltip>
                  </ElementTree>
                  <span v-show="error['attributes']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
              </tr>
              <tr>
                <th class="icon__require">{{ $t('dataModel.dataModelId') }}</th>
                <td>
                  <label>
                    <input :class="error['id'] ? `input__text error__border` : `input__text`" type="text"
                      v-model="formData['id']" name="id" :disabled="isMode" @blur="onFocusoutEvent" />
                  </label>
                  <br>
                  <span v-show="error['id']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
                <th>{{ $t('dataModel.description') }}</th>
                <td>
                  <label>
                    <input class="input__text" type="text" name="description" v-model="formData['description']" />
                  </label>
                </td>
              </tr>
              <tr>
                <th class="icon__require">{{ $t('dataModel.dataModelType') }}</th>
                <td>
                  <label>
                    <el-select v-model="formData['type']" filterable size="mini"
                      @change="(value) => onChangeDataModel('type', value)" @blur="onFocusoutEvent"
                      placeholder="Please select one" :class="error['type'] ? `error__border` : ``" name="type"
                      style="width: 100%;">
                      <el-option size="mini" style="font-size: 12px;" v-for="item in dataModelTypes" :key="item"
                        :label="item" :value="item">
                      </el-option>
                    </el-select>
                  </label>
                  <br>
                  <span v-show="error['type']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
                <th>{{ $t('comm.creator') }}</th>
                <td>
                  <label>
                    <input class="input__text" type="text" v-model="formData['creatorId']" disabled />
                  </label>
                </td>
              </tr>
              <tr>
                <th class="icon__require">{{ $t('dataModel.typeUri') }}</th>
                <td>
                  <label>

                    <el-select v-model="formData['typeUri']" filterable size="mini"
                      @change="(value) => onChangeDataModel('typeUri', value)" @blur="onFocusoutEvent"
                      placeholder="Please select one" :class="error['typeUri'] ? `error__border` : ``" name="typeUri"
                      style="width: 100%;">
                      <el-option size="mini" style="font-size: 12px;" v-for="item in dataModelUris" :key="item"
                        :label="item" :value="item">
                      </el-option>
                    </el-select>
                  </label>
                  <br>
                  <span v-show="error['typeUri']" class="error__color">
                    {{ $t('comm.required') }}
                  </span>
                </td>
                <th>{{ $t('comm.creationTime') }}</th>
                <td>
                  <label>
                    <input class="input__text" type="text" v-model="formData['createdAt']" disabled />
                  </label>
                </td>
              </tr>
              <tr>
                <th>{{ $t('dataModel.dataModelName') }}</th>
                <td>
                  <label>
                    <input class="input__text" type="text" v-model="formData['name']" />
                  </label>
                </td>
                <th>{{ $t('comm.modifier') }}</th>
                <td>
                  <label>
                    <input class="input__text" type="text" v-model="formData['modifierId']" disabled />
                  </label>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </fieldset>
  </form>
</template>

<script>
/**
 * Data Model Basic view page (Container)
 */
import AppTable from '@/components/AppTable';
import ElementTree from '@/components/ElementTree';


export default {
  name: 'DataModelBasicView',
  components: {
    ElementTree,
    AppTable
  },
  props: {
    treeData: Array,
    formData: Object,
    error: Object,
    context: Object,
  },
  watch: {
    formData(val) {
      if (this.isMode) {
        // is modify mode
        this.getContextSearch();
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
      addText: null,
      delText: null,
      indexAddText: null,
      indexDelText: null,
      treeId: null,
      treeNode: null,
      treeInfo: null,
      isMode: false,
      dataModelData: null,
      dataModelTypes: [],
      dataModelUris: []
    }
  },
  methods: {
    getContextSearch() {
      this.$emit('on-context-data');
    },
    onChangeDataModel(name, value) {
      if (name === 'type') {
        this.formData['type'] = value;
        this.formData['typeUri'] = this.dataModelData[value];
      } else {
        Object.keys(this.dataModelData).some(key => {
          if (value === this.dataModelData[key]) {
            this.formData['type'] = key;
          }
        });
        this.formData['typeUri'] = value;
      }
    },
    onContextAdd() {
      if (this.addText) {
        if (!this.formData.context) {
          this.formData.context = [];
        }
        this.formData.context.push(this.addText);
        this.addText = null;
        this.error['context'] = false;
      }
    },
    onIndexAdd() {
      if (this.indexAddText) {
        if (!this.formData.indexAttributeNames) {
          this.formData.indexAttributeNames = [];
        }
        this.formData.indexAttributeNames.push(this.indexAddText);
        this.indexAddText = null;
      }
    },
    onContextDel() {
      if (this.delText) {
        let data = { ...this.formData };
        data.context.some((item, index) => {
          if (item === this.delText) {
            data.context.splice(index, 1);
          }
        });
        this.$emit('on-context-del', data);
      }
      this.delText = null;
    },
    onIndexDel() {
      if (this.indexDelText) {
        let data = { ...this.formData };
        data.indexAttributeNames.some((item, index) => {
          if (item === this.indexDelText) {
            data.indexAttributeNames.splice(index, 1);
          }
        });
        this.$emit('on-index-del', data);
      }
      this.indexDelText = null;
    },
    onTableRowEvent(value) {
      this.delText = value;
    },
    onIndexTableRowEvent(value) {
      this.indexDelText = value;
    },
    onEntityEvent(event, btnName) {
      this.error['attributes'] = false;
      if (event === null) {
        this.$emit('on-entity-add', this.treeId, this.treeInfo, this.treeNode, btnName);
      } else {
        const { name } = event.target;
        this.$emit('on-entity-add', this.treeId, this.treeInfo, this.treeNode, name);
      }
    },
    onTreeEvent(id, data, node) {
      this.treeId = id;
      this.treeInfo = data;
      this.treeNode = node;
      this.onEntityEvent(null, 'modify');
    },
    onFocusoutEvent(event) {
      const { name, value } = event.target;
      if (value !== '') {
        this.error[name] = false;
      }

      const { mode } = this.$route.query;
      if (name === 'type' || name === 'typeUri') {
        this.error['typeUri'] = false;
        this.error['type'] = false;
        document.querySelectorAll('.el-input__inner')[mode === 'mod' ? 0 : 1].style = '';
        document.querySelectorAll('.el-input__inner')[mode === 'mod' ? 1 : 2].style = '';
      }
    }
  },
  mounted() {
    const { mode } = this.$route.query;
    if (mode === 'mod') {
      this.isMode = true;
    }
  }
}
</script>

<style scoped>
.button__util--search:before {
  content: 'search'
}

.error__color {
  color: #f56c6c;
  font-size: 10px;
}

.error__border {
  border-color: #f56c6c;
}
</style>
