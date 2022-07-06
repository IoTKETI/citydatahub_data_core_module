<template>
  <form>
    <fieldset>
      <legend>필드셋 제목</legend>
      <!-- section-write -->
      <section class="section">
        <div class="section__header" v-show="title">
          <h4 class="section__title">{{ title }}</h4>
        </div>
        <div class="section__content">
          <table class="table--row">
            <tbody>
            <!-- TODO : 더 예쁜구조로 만들 수 있지 않을까 ... (고민중...) -->
              <tr v-for="( meta, index ) in metaData">
                <template v-for="( result ) in meta">
                  <th
                      :class="result.require ? `icon__require` : null"
                      :rowspan="result.rowspan"
                      :width="result.col"
                  >
                    {{ result.displayName }}
                  </th>
                  <td :colspan="result.colspan" :rowspan="result.rowspan">
                    <template v-if="result.type === `choice`">
                      <slot :name="result.slotName"></slot>
                      <select
                          v-if="!formData[result.name]"
                          class="input__text"
                          v-model="result.selectedValue"
                          :name="result.name"
                          :style="result.require && error[result.name] ? 'border-color: #f56c6c' : null"
                          @change="onChange($event)"
                          @blur="onFocusoutEvent"
                      >
                        <option
                            disabled
                            :value="null"
                        >
                          Please select one
                        </option>
                        <option
                            v-for="( choice, key ) in result.choices"
                            :key="`choice-${ key }`"
                            :value="choice.value"
                        >
                          {{ choice.displayName }}
                        </option>
                      </select>
                      <select
                          v-else
                          class="input__text"
                          v-model="formData[result.name]"
                          :name="result.name"
                          :style="result.require && error[result.name] ? 'border-color: #f56c6c' : null"
                          @blur="onFocusoutEvent"
                      >
                        <option
                            disabled
                            :value="null"
                        >
                          Please select one
                        </option>
                        <option
                            v-for="( choice, key ) in result.choices"
                            :key="`choice-${ key }`"
                            :value="choice.value"
                        >
                          {{ choice.displayName }}
                        </option>
                      </select>
                      <br>
                      <span v-show="result.require && error[result.name]" class="error__color">
                        필수 값 입니다.
                      </span>
                      <div v-if="result.isTable">
                        <AppTable
                            :meta-data="result.tableFields"
                            :table-items="result.tableList"
                            :tableHeight="result.tableHeight"
                            :overflowY="result.overflowY"
                        />
                      </div>
                    </template>
                    <template v-else-if="result.type === `datetime`">
                      <!-- TODO : 임시 화면 -->
                      <el-date-picker
                          v-model="formData[result.name]"
                          type="datetimerange"
                          :default-time="['00:00:00']"
                          size="mini"
                      >
                      </el-date-picker>
                    </template>
                    <template v-else-if="result.type === `tree`">
                      <slot :name="result.slotName"></slot>
                      <span v-show="result.require && error[result.name]" class="error__color">
                        필수 값 입니다.
                      </span>
                      <ElementTree
                          :tree-data="treeData"
                          :treeHeight="result.treeHeight"
                          :className="result.require && error[result.name] ? `error__border` : null"
                          @on-tree-event="onTreeEvent"
                      />
                    </template>
                    <template v-else-if="result.type === `userOption`">
                      <div class="button__group">
                        <select
                            v-if="result.isChoice"
                            class="input__text"
                            style="width: 30%;"
                            @change="onChange($event)"
                        >
                          <option
                              disabled
                              :value="null"
                          >
                            Please select one
                          </option>
                          <option
                              v-for="( choice, key ) in result.choices"
                              :value="choice.value"
                          >
                            {{ choice.displayName }}
                          </option>
                        </select>
                        <button
                            v-if="result.isAddButton"
                            class="button__util button__util--add material-icons"
                            type="button"
                            name="addButton"
                            @click="(event) => onDelivery(event, result)"
                        >
                          {{ $t('comm.add') }}
                        </button>
                        <button
                            v-if="result.isDelButton"
                            class="button__util button__util--remove material-icons"
                            type="button"
                            name="deleteButton"
                            @click="(event) => onDelivery(event, result)"
                        >
                          {{ $t('comm.delete') }}
                        </button>
                      </div>
                      <input
                          v-if="result.isInput"
                          class="input__text"
                          type="text"
                          :name="result.name"
                          :required="result.require"
                          :disabled="result.readOnly"
                          v-model="addTextData[result.name]"
                      />
                      <div v-if="result.isTable && result.isDelButton">
                        <AppTable
                            :meta-data="result.tableFields"
                            :table-items="formData[result.name]"
                            :tableHeight="result.tableHeight"
                            :overflowY="result.overflowY"
                            @on-row-event="onRowEvent"
                        />
                      </div>
                      <div v-if="result.isTable && !result.isDelButton">
                        <AppTable
                            :meta-data="result.tableFields"
                            :table-items="formData[result.name]"
                            :tableHeight="result.tableHeight"
                            :overflowY="result.overflowY"
                            @on-row-event="onRowEvent2"
                        />
                      </div>
                    </template>
                    <template v-else>
                      <slot :name="result.slotName"></slot>
                      <input
                          v-show="result.type !== null"
                          class="input__text"
                          :type="result.type"
                          :required="result.require"
                          :disabled="result.readOnly"
                          v-model="formData[result.name]"
                          :name="result.name"
                          :style="result.require && error[result.name] ? 'border-color: #f56c6c' : null"
                          @blur="onFocusoutEvent"
                      />
                      <br>
                      <span v-show="result.require && error[result.name]" class="error__color">
                        필수 값 입니다.
                      </span>
                    </template>
                  </td>
                </template>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
      <!-- //section-default -->
      <div class="button__group">
        <AppButtons
            v-for="button in formButtons"
            :button-name="button.name"
            :button-class="button.className"
            :key="button.id"
            :is-hide="button.isHide"
            @on-button-event="() => button.onButtonEvent()"
        />
      </div>
    </fieldset>
  </form>
</template>

<script>
/**
 * Common Form component
 * Deprecated
 */
import ElementTree from '@/components/ElementTree';
import AppTable from '@/components/AppTable';
import AppButtons from '@/components/AppButtons';


export default {
  name: 'AppForm',
  components: {
    AppTable,
    ElementTree,
    AppButtons
  },
  props: {
    metaData: Array,
    title: String,
    isButton: Boolean,
    treeData: Array,
    formData: Object,
    formButtons: Array,
    error: Object,
  },
  data() {
    return {
      objectData: this.formData,
      addText: null,
      delText: null,
      addTextData: {},
      delTextData: {}
    }
  },
  computed: {},
  methods: {
    onFocusoutEvent(event) {
      this.$emit('on-focusout-event', event);
    },
    onDelivery(event, data) {
      const targetName = event.target.name;
      if (data.isChoice && targetName === 'addButton') {
        this.addTextData.name = data.name;
        this.$emit('add-event', this.addTextData);
        return null;
      } else if (data.isChoice && targetName === 'deleteButton') {
        this.delTextData.name = data.name;
        this.delTextData.value = this.delText;
        this.$emit('del-event', this.delTextData);
        return null;
      }
      if (targetName === 'addButton') {
        this.addTextData.name = data.name;
        this.addTextData.value = this.addTextData[data.name];
        this.$emit('add-event', this.addTextData);
        this.addTextData = {};
      } else {
        this.delTextData.name = data.name;
        this.delTextData.value = this.delText;
        this.$emit('del-event', this.delTextData);
      }
    },
    onChange(event) {
      const { name, value } = event.target;
      console.log(value);
      this.objectData[name] = value;
      this.addTextData.value = event.target.value;
    },
    onTreeEvent(id, data, node) {
      this.$emit('on-tree-event', id, data, node);
    },
    onRowEvent(item) {
      this.delText = item;
    },
    onRowEvent2(item) {
      this.$emit('on-click-event', item);
    }
  },
  mounted() {
  }
}
</script>

<style scoped>
.button__group {
  margin: 10px 0 5px;
}
.error__color {
  color: #f56c6c; font-size: 10px;
}
.error__border {
  border-color: #f56c6c;
}
</style>