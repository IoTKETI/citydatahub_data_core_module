<template>
  <div style="margin-top: 10px;">
    <section :class="className ? `section ${ className }` : `section`">
      <div class="section__content" :style="{ height: tableHeight, overflowY: overflowY }">
        <table class="table--column">
          <caption></caption>
          <colgroup>
            <col v-for="meta in metaData" v-if="!meta.displayNone" :width="meta.col">
          </colgroup>
          <thead>
            <tr>
              <th
                v-for="(meta, index) in metaData"
                :class="overflowY ? 'slim' : null"
                :key="`th-${ index }`"
              >
                {{ meta.displayName }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr
                v-for="(item, index) in tableItems"
                :key="`tr-${ index }`"
                @click="onDelivery($event, item)"
            >
              <td
                  v-if="typeof item === 'string'"
                  :title="item"
              >
                {{ item }}
              </td>
              <td
                  v-else
                  v-for="(value, key) in item"
                  :key="`td-${ key }`"
                  :class="overflowY ? 'slim' : null"
                  :title="value"
              >
                {{ value }}
              </td>
            </tr>
            <tr v-if="!tableItems || tableItems.length === 0">
              <td :colspan="metaData.length">
                {{ $t('comm.noData') }}
              </td>
            </tr>
          </tbody>
        </table>
        <slot v-if="tableItems && tableItems.length > 0" name="pagination"></slot>
      </div>
    </section>
    <slot name="buttons"></slot>
  </div>
</template>

<script>
/**
 * Common Table component
 * @props props { ... }
 */
  export default {
    name: 'AppTable',
    props: {
      metaData: Array,
      tableTitle: String,
      tableItems: Array,
      tableHeight: String,
      overflowY: String,
      className: String
    },
    methods: {
      onDelivery(event, item) {
        event.stopPropagation();
        this.$emit('on-row-event', item);
      }
    },
    mounted() {
    }
  }
</script>

<style scoped>
.slim {
  padding: 3px 10px !important;
}
.table--column tr:hover {
  background-color: #fcfcfc;
  cursor: pointer;
}
.error__border {
  border-color: #f56c6c;
}
.active {
  background-color: #fcfcfc;
}
</style>