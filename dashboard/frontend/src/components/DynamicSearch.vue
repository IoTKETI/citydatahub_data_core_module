<template>
  <b-form inline class="col-12">
    <b-form-select :disabled="index === 0" v-model="formData['temp']" :options="condition" size="sm" class="mb-2 mr-sm-2 mb-sm-0 mt-2 col-md-3" style="width: 5vmax;"></b-form-select>
    <b-form-select v-model="formData['operator']" :options="operators" size="sm" class="mb-2 mr-sm-2 mb-sm-0 mt-2 col-md-3" style="width: 5vmax;"></b-form-select>
    <b-form-input
      v-if="setSearchType === 'Integer' || setSearchType === 'Double' || setSearchType === 'ArrayInteger' || setSearchType === 'ArrayDouble'"
      class="mb-2 mr-sm-2 mb-sm-0 mt-2 col-md-4"
      style="width: 8vmax;"
      v-model="formData['value']"
      type="number"
    ></b-form-input>
    <b-form-select
      v-else-if="setSearchType === 'Boolean' || setSearchType === 'ArrayBoolean'"
      v-model="formData['value']"
      :options="[{ value: null, text: 'Selected', disabled: true }, { value: true, text: true }, { value: false, text: false },]"
      size="sm"
      class="mb-2 mr-sm-2 mb-sm-0 mt-2 col-md-3"
      style="width: 5vmax;"
    ></b-form-select>
    <b-form-input
      v-else
      class="mb-2 mr-sm-2 mb-sm-0 mt-2 col-md-4"
      style="width: 8vmax;"
      v-model="formData['value']"
    ></b-form-input>
    <el-popover
      placement="top"
      width="200"
      v-model="visible"
    >
      <p style="font-size: 12px;">{{ $t('message.deleteCheck') }}</p>
      <div style="text-align: right; margin: 0">
        <el-button size="mini" type="" @click="visible = false">{{ $t('comm.cancel') }}</el-button>
        <el-button type="danger" size="mini" @click="remove(formData)">{{ $t('comm.delete') }}</el-button>
      </div>
      <i slot="reference" class="el-icon-error col-md-1" style="margin-top: 6px; cursor: pointer; font-size: 16px; padding-left: 2px;"></i>
    </el-popover>
  </b-form>
</template>

<script>
/**
 * Set detailed search conditions of attrs component
 *
 * @param formData
 * @param index
 */
export default {
    name: 'DynamicSearch',
    props: {
      formData: Object,
      index: Number,
    },
    watch: {
      formData(val) {
        // Set the operator according to the changed value type.
        this.setSearchType = val.valueType;
        if (val.valueType === 'Integer' || val.valueType === 'Double' || val.valueType === 'ArrayInteger' || val.valueType === 'ArrayDouble') {
          this.operators = [
            { value: null, text: 'Selected', disabled: true },
            { value: 'OPR_01', text: '=' },
            { value: 'OPR_02', text: '≠' },
            { value: 'OPR_03', text: '<' },
            { value: 'OPR_04', text: '≤' },
            { value: 'OPR_05', text: '>' },
            { value: 'OPR_06', text: '≥' },
          ];
        } else if (val.valueType === 'Boolean' || val.valueType === 'ArrayBoolean') {
          this.operators = [
            { value: null, text: 'Selected', disabled: true },
            { value: 'OPR_01', text: '=' },
            { value: 'OPR_02', text: '≠' },
          ];
        } else {
          this.operators = [
            { value: null, text: 'Selected', disabled: true },
            { value: 'OPR_01', text: '=' },
            { value: 'OPR_02', text: '≠' },
            { value: 'OPR_07', text: 'contains' },
          ];
        }
      }
    },
    data() {
      return {
        selected: null,
        visible: false,
        condition: [
          { value: null, text: 'Selected', disabled: true },
          { value: 'AND', text: 'AND' },
          { value: 'OR', text: 'OR' }
        ],
        operators: [
          { value: null, text: 'Selected', disabled: true },
          { value: 'OPR_01', text: '=' },
          { value: 'OPR_02', text: '≠' },
          { value: 'OPR_03', text: '<' },
          { value: 'OPR_04', text: '≤' },
          { value: 'OPR_05', text: '>' },
          { value: 'OPR_06', text: '≥' },
        ],
        setSearchType: null,
      }
    },
    methods: {
      // remove DynamicSearch, notify the above component.
      remove(data) {
        this.visible = false;
        this.$emit('remove', data.tempId, this.index);
      },
    },
    mounted() {
      // Set the operator according to the value type.
      const val = this.formData;
      this.setSearchType = val.valueType;
      if (val.valueType === 'Integer' || val.valueType === 'Double' || val.valueType === 'ArrayInteger' || val.valueType === 'ArrayDouble') {
        this.operators = [
          { value: null, text: 'Selected', disabled: true },
          { value: 'OPR_01', text: '=' },
          { value: 'OPR_02', text: '≠' },
          { value: 'OPR_03', text: '<' },
          { value: 'OPR_04', text: '≤' },
          { value: 'OPR_05', text: '>' },
          { value: 'OPR_06', text: '≥' },
        ];
      } else if (val.valueType === 'Boolean' || val.valueType === 'ArrayBoolean') {
        this.operators = [
          { value: null, text: 'Selected', disabled: true },
          { value: 'OPR_01', text: '=' },
          { value: 'OPR_02', text: '≠' },
        ];
      } else {
        this.operators = [
          { value: null, text: 'Selected', disabled: true },
          { value: 'OPR_01', text: '=' },
          { value: 'OPR_02', text: '≠' },
          { value: 'OPR_07', text: 'contains' },
        ];
      }
    }
  }
</script>

<style scoped>

</style>
