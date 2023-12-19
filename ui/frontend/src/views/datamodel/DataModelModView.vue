<template>
  <div>
    <h3 class="content__title">{{ $t('dataModel.dataModelInfo') }}</h3>
    <div style="margin-top: 15px; text-align: right;" v-if="isMode === 'add'">
      <label>
        <el-select v-model="searchModelValue" filterable size="mini" id="choiceModel">
          <el-option
              size="mini"
              style="font-size: 12px;"
              v-for="item in dataModelList"
              :key="item.id"
              :label="item.id"
              :value="item.id"
          >
          </el-option>
        </el-select>
        <button type="button" class="button__primary" @click="getDataModel('load')">
          {{ $t('comm.load') }}
        </button>
      </label>
    </div>
    <DataModelBasicView
        :tree-data="basicTreeData"
        :form-data="formData"
        @on-entity-add="onEntityEvent"
        @on-entity-view="onEntityEvent"
        @on-context-del="onContextDel"
        @on-index-del="onIndexDel"
        @on-context-data="getContextSearch"
        :error="basicErrors"
        :context="contextData"
    />
    <DataModelProView
        :tree-data="proTreeData"
        :form-data="proFormData"
        :is-disabled="isDisabled"
        @on-entity-add="onEntityAttr"
        @on-value-enum-del="onValueEnumDel"
        @on-object-member-event="onObjectMemberEvent"
        :error="proErrors"
        :context="contextData"
    />
    <div class="button__group">
      <button
          v-if="isMode === 'mod'"
          class="button__primary"
          type="button"
          @click="onProvisioning"
      >
        {{ $t('dataModel.provisioning') }}
      </button>
      <button
          class="button__primary"
          type="button"
          @click="onSave"
      >
        {{ $t('dataModel.modelSave') }}
      </button>
      <button
          v-if="isMode === 'mod'"
          class="button__secondary"
          type="button"
          @click="onDelete"
      >
        {{ $t('dataModel.modelDelete') }}
      </button>
      <button
          class="button__primary"
          type="button"
          @click="onGoBack"
      >
        {{ $t('comm.backToList') }}
      </button>
    </div>

    <AppModal
        :is-show="isSaveShow"
        @close-modal="onClose"
        @on-event-modal="onConfirmSave"
        modalSize="w-360"
        :content="modalText"
        button-name="확인"
        :is-success-btn="true"
        :isCancelBtn="true"
    />
    <AppModal
        :is-show="isDelShow"
        @close-modal="onClose"
        @on-event-modal="onConfirmDel"
        modalSize="w-360"
        :content="modalText"
        button-name="확인"
        :is-success-btn="true"
        :isCancelBtn="true"
    />
    <AppModal
        :is-show="isDelAttrShow"
        @close-modal="onClose"
        @on-event-modal="onAttrDel"
        modalSize="w-360"
        :content="modalText"
        button-name="확인"
        :is-success-btn="true"
        :isCancelBtn="true"
    />
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
 * Data Model view page (Container)
 */
  import ElementTree from '@/components/ElementTree';
  import AppTable from '@/components/AppTable';
  import AppModal from '@/components/AppModal';
  import DataModelBasicView from '@/views/datamodel/DataModelBasicView';
  import DataModelProView from '@/views/datamodel/DataModelProView';
  import { APIHandler } from '@/modules/api-handler';
  import treeSearch from 'tree-search';
  import { errorRender } from "@/modules/utils";

  import Loading from 'vue-loading-overlay';
  import 'vue-loading-overlay/dist/vue-loading.css';
  import {mapState} from "vuex";


  export default {
    name: 'DataModelModView',
    components: {
      DataModelProView,
      DataModelBasicView,
      ElementTree,
      AppTable,
      AppModal,
      Loading
    },
    computed: {
      ...mapState('dataModels', [
        'dataModelList'
      ]),
    },
    data() {
      return {
        isLoading: false,
        fullPage: true,
        basicTreeData: [{ id: 1, label: 'ROOT', children: [] }],
        proTreeData: [{ id: 1, label: 'ROOT', children: [] }],
        formData: { type: null, typeUri: null },
        proFormData: { hasObservedAt: false, isRequired: false, hasUnitCode: false, type: null, attributeUri: null, valueType: null },
        changeProFormData: {},
        attributes: [],
        isSaveShow: false,
        isDelShow: false,
        isAlertShow: false,
        isDelAttrShow: false,
        isDisabled: true,
        modalText: null,
        treeId: null,
        treeNode: null,
        btnType: null,
        isMode: null,
        basicErrors: {
          context: false, attributes: false, id: false, type: false, typeUri: false
        },
        proErrors: {
          name: false, attributeType: false, attributeUri: false, valueType: false
        },
        contextData: null,
        searchModelValue: ''
      };
    },
    methods: {
      getContextSearch() {
        this.isLoading = true;
        this.$http.post('/datamodels/context', this.formData.context)
          .then(response => {
            this.isLoading = false;
            this.contextData = response.data;
          }).catch(error => {
            this.isLoading = false;
            const result = errorRender(error.response.status, error.response.data);
            this.isAlertShow = result.isAlertShow;
            this.modalText = result.message + `(${ error.message })`;
        });
      },
      onProvisioning() {
        this.isLoading = true;
        const { id } = this.$route.query;
        this.$http.post(`/datamodels/${ id }/provision`, {})
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
      },
      onSave() {
        Object.keys(this.basicErrors).map(key => {
          if (!this.formData[key] || this.formData[key] === '' || this.formData[key].length === 0) {
            this.basicErrors[key] = true;
          }
        });
        let checkResult = Object.keys(this.basicErrors).some(key => {
          return !!this.basicErrors[key];
        });

        if (this.basicErrors.type) {
          document.querySelectorAll('.el-input__inner')[1].style = 'border-color: #f56c6c';
        }

        if (this.basicErrors.typeUri) {
          document.querySelectorAll('.el-input__inner')[2].style = 'border-color: #f56c6c';
        }

        if (checkResult) {
          return null;
        }
        this.isSaveShow = true;
        this.modalText = '저장하시겠습니까?';
      },
      onDelete() {
        this.isDelShow = true;
        this.modalText = '삭제하시겠습니까?';
      },
      onGoBack() {
        this.$router.push({
          name: 'DataModelView'
        });
      },
      onClose() {
        this.isSaveShow = false;
        this.isAlertShow = false;
        this.isDelShow = false;
        this.isDelAttrShow = false;
      },
      onEntityEvent(treeId, treeInfo, treeNode, btnType) {
        this.treeId = treeId;
        this.treeNode = treeNode;
        const { mode } = this.$route.query;

        if (treeId === null) {
          return null;
        }
        if (btnType === 'add') {
          // level check add 2021-01-18
          if (this.treeNode.level > 2) {
            this.isAlertShow = true;
            this.modalText = 'ROOT 를 기준으로 2레벨까지 입력 가능합니다.';
            return null;
          }

          // create
          this.isDisabled = false;
          this.btnType = btnType;
          this.proFormData = {};
          this.proTreeData = [{ id: 1, label: 'ROOT', children: [] }];
        } else if (btnType === 'modify' && treeId !== 1) {
          // modify
          this.isDisabled = false;
          this.btnType = btnType;
          if (mode === 'add') {
            // 데이터를 찾아서 넣어주기
            const resultObject = this.formData.attributes;
            const resultFind = treeSearch('childAttributes');
            this.proFormData = resultFind(resultObject, 'name', this.treeId);
            return null;
          }
          this.getDataModelEntity(treeId);
        } else if (btnType === 'delete' && treeId !== 1) {
          // delete
          this.isDelAttrShow = true;
          this.modalText = '삭제하시겠습니까?';
          this.btnType = btnType;
        }
      },
      onAttrDel() {
        this.proFormData = {};
        this.isDisabled = true;
        this.isDelAttrShow = false;
        this.proTreeData = [{ id: 1, label: 'ROOT', children: [] }];

        const { mode } = this.$route.query;
        if (mode === 'mod') {
          this.onEntityDel();
          return null;
        }

        // form delete
        if (this.treeNode.parent.data.id === 1) {
          const index = this.formData.attributes.findIndex(d => d.name === this.treeId);
          this.formData.attributes.splice(index, 1);
        } else {
          const parentId = this.treeNode.parent.data.id;
          const parentFind = treeSearch('attributes');
          const parentFindData = parentFind(this.formData.attributes, 'name', parentId);
          const children = parentFindData.childAttributes || parentFindData;
          const index = children.findIndex(d => d.name === this.treeId);
          children.splice(index, 1);
        }
        // tree delete
        const parent = this.treeNode.parent;
        const children = parent.data.children || parent.data;
        const index2 = children.findIndex(d => d.id === this.treeId);
        children.splice(index2, 1);
      },
      onEntityDel() {
        // delete -> api put change.
        const { namespace, type, version } = this.$route.query;
        if (this.treeNode.parent.data.id === 1) {
          const index = this.formData.attributes.findIndex(d => d.name === this.treeId);
          this.formData.attributes.splice(index, 1);
        } else {
          const parentId = this.treeNode.parent.data.id;
          const parentFind = treeSearch('attributes');
          const parentFindData = parentFind(this.formData.attributes, 'name', parentId);
          const children = parentFindData.childAttributes || parentFindData;
          const index = children.findIndex(d => d.name === this.treeId);
          children.splice(index, 1);
        }
        this.$http.put(APIHandler.buildUrl([`datamodels`]), this.formData)
            .then(response => {
              const resultCode = response.status;
              if (resultCode === 200 || 201 || 204) {
                this.getDataModel();
              }
            }).catch(error => {
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      },
      onEntityAttr(data) {
        const { mode } = this.$route.query;
        Object.keys(this.proErrors).map(key => {
          if (!data[key] || data[key] === '' || data[key].length === 0) {
            this.proErrors[key] = true;
          }
        });
        let checkResult = Object.keys(this.proErrors).some(key => {
          return !!this.proErrors[key];
        });

        if (this.proErrors.name) {
          document.querySelectorAll('.el-input__inner')[mode === 'mod' ? 2 : 3].style = 'border-color: #f56c6c';
        }

        if (this.proErrors.attributeUri) {
          document.querySelectorAll('.el-input__inner')[mode === 'mod' ? 3 : 4].style = 'border-color: #f56c6c';
        }

        if (checkResult) {
          return null;
        }
        if (this.treeNode.level > 3) {
          alert('ROOT 를 기준으로 2레벨까지 입력 가능합니다.');
          return null;
        }
        if (data.accessMode === "") {
          data.accessMode = null;
        }
        this.changeProFormData = data;


        this.isLoading = true;
        // new create attr -> call api
        if (this.btnType === 'add') {
          if (this.treeId === 1) {
            this.basicTreeData[0].children.push({
              id: data.name,
              label: `${ data.name } (${ data.attributeType })`,
              children: []
            });
            if (!this.formData.attributes) {
              this.formData.attributes = [];
            }
            this.formData.attributes.push(data);

            this.treeId = null;
            this.treeNode = null;
            this.btnType = null;
            this.proFormData = {};
            this.proTreeData = [{ id: 1, label: 'ROOT', children: [] }];
            this.isDisabled = true;
            this.isLoading = false;
          } else {
            const entityAttr = this.basicTreeData[0].children;
            const find = treeSearch('children');
            const findData = find(entityAttr, 'id', this.treeId);
            if (findData.children === null){
              findData.children = [];
            }
            findData.children.push({
              id: data.name,
              label: `${ data.name } (${ data.attributeType })`,
              children: []
            });

            const resultObject = this.formData.attributes;
            const resultFind = treeSearch('childAttributes');
            const resultFindData = resultFind(resultObject, 'name', this.treeId);
            if (!resultFindData.childAttributes){
              resultFindData.childAttributes = [];
            }
            resultFindData.childAttributes.push(data);
            this.isLoading = false;
          }

          if (mode === 'mod') {
            this.$http.put(APIHandler.buildUrl(['datamodels']), this.formData)
                .then(response => {
                  const resultCode = response.status;
                  if (resultCode === 200 || 201 || 204) {
                    this.getDataModel();
                  }
                  this.isLoading = false;
                }).catch(error => {
                  const result = errorRender(error.response.status, error.response.data);
                  this.isAlertShow = result.isAlertShow;
                  this.modalText = result.message + `(${ error.message })`;
                  this.isLoading = false;
            });
          }
        } else {
          if (mode === 'mod') {
            const attrName = this.treeNode.parent.data.id === 1 ? this.treeId : this.treeNode.parent.data.id;
            let params = null;
            // Depth only supports level 2.
            if (this.treeNode.level === 3) {
              const resultObject = this.formData.attributes;
              const resultFind = treeSearch('attributes');
              let resultFindData = resultFind(resultObject, 'name', attrName);
              let resultChildren = [];
              resultFindData.childAttributes.map(item => {
                if (item.name === data.name) {
                  resultChildren.push(data);
                } else {
                  resultChildren.push(item);
                }
              });
              resultFindData.childAttributes = resultChildren;
              params = resultFindData;
            } else {
              const resultObject = this.formData.attributes;
              const resultFind = treeSearch('attributes');
              const resultFindData = resultFind(resultObject, 'name', this.treeId);
              const formChildren = resultObject || resultFindData;
              const index = formChildren.findIndex(d => d.name === this.treeId);
              formChildren.splice(index, 1);
              this.formData.attributes.push(data);
            }

            this.$http.put(APIHandler.buildUrl(['datamodels']), this.formData)
                .then(response => {
                  const resultCode = response.status;
                  if (resultCode === 200 || 201 || 204) {
                    this.getDataModel();
                  }
                  this.isLoading = false;
                }).catch(error => {
                  debugger;
                  const result = errorRender(error.response.status, error.response.data);
                  this.isAlertShow = result.isAlertShow;
                  this.modalText = result.message + `(${ error.message })`;
                  this.isLoading = false;
            });
          } else {
            this.treeId = null;
            this.treeNode = null;
            this.btnType = null;
            this.proFormData = {};
            this.proTreeData = [{ id: 1, label: 'ROOT', children: [] }];
            this.isDisabled = true;
          }
          this.isLoading = false;
        }
      },
      onValueEnumDel(data) {
        this.proFormData = data;
      },
      onContextDel(data) {
        this.formData = data;
      },
      onIndexDel(data) {
        this.formData = data;
      },
      onObjectMemberEvent(treeId, treeInfo, treeNode, btnType) {},
      onConfirmSave() {
        this.isSaveShow = false;
        this.isLoading = true;
        // data modal create, modify
        const { namespace, type, version, mode } = this.$route.query;
        if (mode === 'add') {
          this.$http.post(APIHandler.buildUrl(['datamodels']), this.formData)
              .then(response => {
                const resultCode = response.status;
                if (resultCode === 200 || 201 || 204) {
                  this.$router.push('dataModels');
                }
                this.isLoading = false;
              }).catch(error => {
                this.isLoading = false;
                const result = errorRender(error.response.status, error.response.data);
                this.isAlertShow = result.isAlertShow;
                this.modalText = result.message + `(${ error.message })`;
          });
          return null;
        }
        this.$http.put(APIHandler.buildUrl(['datamodels']), this.formData)
            .then(response => {
              const resultCode = response.status;
              if (resultCode === 200 || 201 || 204) {
                this.$router.push('dataModels');
              }
              this.isLoading = false;
            }).catch(error => {
              this.isLoading = false;
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      },
      onConfirmDel() {
        this.isDelAttrShow = false;
        this.isLoading = true;
        const { id } = this.formData;
        this.$http.delete(APIHandler.buildUrl([`datamodels?id=${ id }`]))
            .then(response => {
              if (response.status === 200 || 201 || 204) {
                this.$router.push('dataModels');
              }
              this.isLoading = false;
            }).catch(error => {
              this.isLoading = false;
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      },
      getDataModel(type) {
        const { id } = this.$route.query;

        this.$http.get(APIHandler
            .buildUrl([`datamodels?id=${ type && type === 'load' ? this.searchModelValue : id }`]))
            .then(response => {
              const items = response.data;
              this.formData = items;
              this.attributes = items.attributes;
              this.basicTreeData[0].children = items.treeStructure;
              this.proFormData = {};

              // context info load.
              if (type && type === 'load') {
                this.formData.id = '';
                this.formData.createdAt = '';
                this.formData.modifiedAt = '';
                this.getContextSearch();
              }
            }).catch(error => {
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      },
      getDataModelEntity(attrName) {
        const { id } = this.$route.query;
        this.$http.get(`datamodels/attr?id=${ id }&attrName=${ attrName }`)
            .then(response => {
              const items = response.data;
              console.log(items);
              if (this.treeNode.level < 3) {
                const resultObject = items.attributes;
                const resultFind = treeSearch('childAttributes');
                this.proFormData = resultFind(resultObject, 'name',this.treeId);
              } else {
                const resultObject = items.attributes;
                const resultFind = treeSearch('childAttributes');
                const parentId = this.treeNode.parent.data.id === 1 ? attrName : this.treeNode.parent.data.id;
                const resultFindData = resultFind(resultObject, 'name', parentId);
                const formChildren = resultFindData.childAttributes || resultFindData;

                let result = null;
                formChildren.some(d => {
                  if (d.name === attrName) {
                    result = d;
                  }
                });
                this.proFormData = result;
              }
              this.proTreeData[0].children = items.treeStructure;
            }).catch(error => {
              const result = errorRender(error.response.status, error.response.data);
              this.isAlertShow = result.isAlertShow;
              this.modalText = result.message + `(${ error.message })`;
        });
      }
    },
    mounted() {
      const { mode } = this.$route.query;
      this.isMode = mode;
      if (mode !== 'add') {
        this.getDataModel();
      }
    }
  }
</script>

<style scoped>
  .button__util--search:before {
    content:'search'
  }
  #choiceModel {
    background-color: #ffffff !important;
  }
</style>