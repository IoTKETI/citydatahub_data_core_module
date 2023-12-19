<template>
  <el-dialog
    :title="popupTitle"
    :visible.sync="visible"
    :before-close="handleClose"
    :close-on-click-modal="false"
    top="4vh"
  >
    <b-form class="col-12">
      <label v-if="!isDashboard" class="mr-sm-2">Data Model</label>
      <slot name="selectBox"></slot>
      <slot name="inputBox"></slot>
    </b-form>
    <div class="mt-3" v-if="visibleChartTree">
      <el-tabs v-if="activeName" v-model="actName" @tab-click="tabClick">
        <el-tab-pane :label="$t('widget.displayAttribute')" name="first">
          <div class="col-12 row mt-4" style="margin:0; padding: 0;">
            <div class="col-lg-6" style="padding-left: 0;">
              <div class="card">
                <div class="card-body" style="height: 15vmax; overflow-y: auto;">
                  <slot name="chartTree"></slot>
                </div>
              </div>
            </div>
            <div class="col-lg-6">
              <div class="card">
                <div class="card-body" style="height: 15vmax; overflow-y: auto;">
                  <slot name="addAttr"></slot>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane v-if="visibleSearchOption" :label="$t('widget.searchDetails')" name="second">
          <div class="col-12 row mt-4">
            <slot name="searchOption"></slot>
          </div>
          <div class="col-12 row" style="margin:0; padding: 0;">
            <div class="col-lg-6" style="padding-left: 0;">
              <div class="card">
                <div class="card-body" style="height: 15vmax; overflow-y: auto;">
                  <slot name="tree"></slot>
                </div>
              </div>
            </div>
            <div class="col-lg-6" style="padding-right: 0;">
              <div class="card">
                <div class="card-body" style="height: 15vmax; overflow-y: auto;">
                  <slot name="addQuery"></slot>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane v-if="isChart" label="Graph Setting" name="third">
          <strong style="font-size: 12px;">* Select maximum two attributes</strong>
          <div class="col-12 row mt-4" style="margin:0; padding: 0;">
            <div class="col-lg-12" style="padding-left: 0;">
              <div class="card">
                <div class="card-body" style="height: 15vmax; overflow-y: auto;">
                  <slot name="checks"></slot>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <div v-else class="col-12 row mt-4" style="margin:0; padding: 0;">
        <div class="col-lg-6" style="padding-left: 0;">
          <div class="card">
            <div class="card-body" style="height: 20vmax; overflow-y: auto;">
              <slot name="tree"></slot>
            </div>
          </div>
        </div>
        <div class="col-lg-6" style="padding-right: 0;">
          <div class="card">
            <div class="card-body" style="height: 20vmax; overflow-y: auto;">
              <slot name="addQuery"></slot>
            </div>
          </div>
        </div>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
        <slot name="buttonGroup"></slot>
      </span>
  </el-dialog>
</template>

<script>
/**
 * Widget Setting Popup Component
 * @component
 * - element-ui
 * @props props { ... }
 * @state data() { ... }
 */
export default {
  name: 'SearchConfiguration',
  props: {
    isDashboard: Boolean,
    popupTitle: String,
    visible: Boolean,
    activeName: String,
    isChart: Boolean,
    tabLabel: String,
    chartType: String,
    visibleChartTree: Boolean,
    visibleSearchOption: Boolean,
  },
  watch: {
    activeName(value) {
      this.actName = value;
    }
  },
  data() {
    return {
      actName: 'first',
    }
  },
  methods: {
    handleClose() {
      this.$emit('close-event', false);
    },
    tabClick(tab, event) {
      this.$emit('tab-click', tab, event, this.actName);
    }
  },
  mounted() {}
}
</script>

<style scoped>

</style>
