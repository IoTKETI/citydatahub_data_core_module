<template>
  <div>
    <DashboardLatestMap
      :id="chartKey"
      :key="mapSearchConditionId"
      :latestId="mapSearchConditionId"
      :chartTitle="chartTitle"
      :elementId="chartKey"
    />
  </div>
</template>

<script>
/**
 * Dashboard Widget Latest Map Type
 * Components used to register dashboard widgets
 *
 * @component
 * - vue-chartjs, chart.js
 * @props mapSearchConditionId, chartTitle, chartKey
 * @state loaded
 */
import DashboardLatestMap from "@/pages/DashboardLatestMap";
export default {
  name: 'LatestMapChart',
  components: {DashboardLatestMap},
  props: {
    mapSearchConditionId: String,
    chartTitle: String,
    chartKey: String
  },
  data() {
    return {
      loaded: false
    }
  },
  mounted() {
    this.loaded = true;
  }
}
</script>

<style scoped>
.card-title {
  font-size: 50px;
}
.card-category {
  color: #666666;
  padding-top: 16px;
  font-size: 12px;
}
.card-custom {
  height: 100%;
  width: 100%;
}
.card-custom p {
  position: absolute;
  top: 50%;
  width: calc(100% - 30px);
  overflow: hidden;
  transform: translateY(-50%);
  word-break: break-all;
  max-height: 100%;
}
.card-custom img {
  width: 100%;
  height: 100%;
  object-fit: fill;
}
</style>
