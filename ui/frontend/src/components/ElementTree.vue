<template>
  <div
      :class="`tree-content ${ className }`"
      :style="{ height: treeHeight }"
  >
    <div class="custom-tree-container">
      <div class="block">
        <el-tree
            :data="treeData"
            :node-key="nodeKey"
            default-expand-all
            :expand-on-click-node="false"
            @node-click="handleNodeClick"
            empty-text="데이터가 없습니다."
        >
          <span class="custom-tree-node" slot-scope="{ node, data }">
            <span>{{ nodeName ? data.name : node.label }}</span>
          </span>
        </el-tree>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * Element Tree
 * @component element-ui(el-tree)
 * @type {number}
 */
let id = 1000;

export default {
  name: 'ElementTree',
  props: {
    treeData: Array,
    treeHeight: String,
    className: String,
    nodeKey: String,
    nodeName: Boolean
  },
  data() {
    return {}
  },
  methods: {
    handleNodeClick(data, node) {
      // node 를 클릭했을때의 정보를 가져온다.
      this.$emit('on-tree-event', data.id, data, node);
    },
    append(data) {
      const newChild = { id: id++, label: 'testtest', children: [] };
      if (!data.children) {
        this.$set(data, 'children', []);
      }
      data.children.push(newChild);
    },
    remove(node, data) {
      const parent = node.parent;
      const children = parent.data.children || parent.data;
      const index = children.findIndex(d => d.id === data.id);
      children.splice(index, 1);
    },
    renderContent(h, { node, data, store }) {
      return (
          `<span class="custom-tree-node">
            <span>{node.label}</span>
            <span>
                <el-button size="mini" type="text" on-click={ () => this.append(data) }>Append</el-button>
                <el-button size="mini" type="text" on-click={ () => this.remove(node, data) }>Delete</el-button>
            </span>
          </span>`
      );
    }
  },
  mounted() {
  }
}
</script>

<style scoped>
  .tree-content {
    border: 1px solid #dddddd;
    overflow-y: scroll;
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