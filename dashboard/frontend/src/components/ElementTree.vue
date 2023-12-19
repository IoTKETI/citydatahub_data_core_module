<template>
  <div class="custom-tree-container">
    <div class="block">
      <el-tree
        :data="treeData"
        :node-key="nodeKey"
        default-expand-all
        :expand-on-click-node="false"
        @node-click="handleNodeClick"
        empty-text="Please select a model."
        :props="defaultProps"
      >
      <label
        class="custom-tree-node"
        slot-scope="{ node, data }"
        :name="node.label"
        style="margin-bottom: 0;cursor: pointer;"
      >
        <span :style="{ color: data.searchable ? null : '#d8d9d9'  }">{{ node.label }}</span>
        <i
          v-if="!radioBox && !checkBox && checkList[data.fullId] && [data.fullId].length > 0"
          class="el-icon-success"
          style="color: #67C23A;"
        ></i>
        <el-radio
          v-if="radioBox"
          v-model="radio"
          :label="data.fullId"
          @change="onChange(node, data)"
          style="margin-bottom: 0;"
        >&nbsp;</el-radio>
        <el-checkbox
          v-if="checkBox && data.graphable"
          v-model="checkedValue[data.fullId]"
          @change="onCheckChange($event, node, data)"
          style="margin-bottom: 0;color: #67C23A;"
        >&nbsp;</el-checkbox>
        <span v-if="visibleOption(data)">
          <el-popover
            placement="top"
            width="240"
            v-model="popover.visible[node.id]"
            @show="() => popoverShow(node.id)"
          >
            <slot name="popover-content" :node="node" :show="popover.visible[node.id]"/>
            <!-- 범례 선택 버튼 -->
            <el-button v-if="data.searchable"
              @click.stop
              slot="reference"
              type="text"
              size="mini">
            {{ $t('comm.option') }}
          </el-button>
          </el-popover>
        </span>
      </label>
      </el-tree>
    </div>
    <div style="display: none;">{{ normalizedCharacter }}</div>
  </div>
</template>

<script>
/**
 * Element Tree
 * @component
 * - element-ui
 * @props props { ... }
 * @state data() { ... }
 */
  let id = 1000;

  export default {
    name: 'ElementTree',
    props: {
      treeData: Array, // set init treeData
      // treeHeight: String,
      // className: String,
      // nodeName: Boolean,
      nodeKey: String, // set node key
      checkList: Object, // use checkIcon for checkList
      checkBox: Boolean, // use checkbox(default false)
      radioBox: Boolean, //  use radiobox(default false)
      radioValue: String, // for init selected radio
      chartList: Object, // for init checked List
      option: Boolean, //  use option(default false)
      optionFiltering: Boolean, // use option Filtering (default false)
    },
    computed: {
      normalizedCharacter () {
        this.radio = this.radioValue;
        return this.radioValue;
      }
    },
    watch: {
      chartList(value) {
        this.checkedValue = value;
      },
    },
    data() {
      return {
        checkedValue: {}, // checked list (checkbox)
        defaultProps: {
          children: 'child',
          label: 'label'
        },
        radio: '', // selected radio item (radiobox)
        popover: {
          visible: {}, // node popover list
          activeId: null,
        },
      }
    },
    methods: {
      append(data) {
        const newChild = { id: id++, label: '', children: [] };
        if (!data.children) {
          this.$set(data, 'children', []);
        }
        data.children.unshift(newChild);
      },
      remove(node, data) {
        // remove this component
        const parent = node.parent;
        const children = parent.data.children || parent.data;
        const index = children.findIndex(d => d.id === data.id);
        children.splice(index, 1);
      },
      onChange(node, data) {
        // Forward the selected radio item to the parent component.
        this.$emit('on-attr-event', data.fullId);
      },
      onCheckChange(event, node, data) {
        if (!event) {
          // Synchronize data when unchecking.
          delete this.checkedValue[data.fullId];
        }
        // If it exceeds two, uncheck node
        if (Object.keys(this.checkedValue).length > 2) {
          this.checkedValue[data.fullId] = false;
          setTimeout(() => {
            delete this.checkedValue[data.fullId];
          }, 1000);
          return null;
        }
        // Forward the checked item to the parent component.
        this.$emit('on-chart-event', this.checkedValue);
      },
      handleNodeClick(data, node) {
        if (!data.searchable) {
          return null;
        }
        // Forward the clicked item to the parent component.
        this.$emit('on-tree-event', data, node);
      },
      renderContent(h, { node, data }) {
        return (
            <span class="custom-tree-node">
            <span>{node.label}</span>
            <span>
              <el-button size="mini" type="text" on-click={ () => this.append(data) }>Append</el-button>
              <el-button size="mini" type="text" on-click={ () => this.remove(node, data) }>Delete</el-button>
            </span>
          </span>);
      },
      popoverShow(nodeId) {
        if (this.popover.activeId) {
          this.popover.visible[this.popover.activeId] = undefined;
        }
        this.popover.activeId = nodeId;
        this.$emit('popover-show');
      },
      visibleOption(data) {
        return this.option && (!this.optionFiltering || (this.optionFiltering && data.searchable))
      }
    },
    mounted() {}
  }
</script>

<style>
  .btn {
    line-height: 0.5;
  }
  .custom-select {
    font-size: 12px;
    height: 25px;
    width: 50px;
  }
  select {
    border: 1px solid #E3E3E3;
    color: #9A9A9A;
  }
  button {
    font-weight: bold;
  }
  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 12px;
    padding-right: 8px;
  }
</style>
