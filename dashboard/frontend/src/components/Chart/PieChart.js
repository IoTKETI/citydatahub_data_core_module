import { Pie, mixins } from 'vue-chartjs';
const { reactiveProp } = mixins;

/**
 * Dashboard Widget Line Type
 * Components used to register dashboard widgets
 *
 * @component
 * - vue-chartjs, chart.js
 * @props options, chartData
 * @state none
 */
export default {
  extends: Pie,
  mixins: [reactiveProp],// for autoUpdate
  props: ['chartData', 'options'],
  mounted () {
    this.renderChart(this.chartData, this.options);
  }
}
