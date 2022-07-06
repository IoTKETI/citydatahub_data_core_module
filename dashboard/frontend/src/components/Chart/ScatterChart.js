import { Scatter, mixins } from 'vue-chartjs';
const { reactiveProp } = mixins;

/**
 * Dashboard Widget Bar Type
 * Components used to register dashboard widgets
 *
 * @component
 * - vue-chartjs, chart.js
 * @props options, chartData
 * @state none
 */
export default {
  extends: Scatter,
  mixins: [reactiveProp], // for autoUpdate
  props: {
    options: {
      type: Object,
      default: function () {
        return {
          maintainAspectRatio: false,
        }
      }
    },
    chartData: {
      type: Object
    }
  },
  watch: {
    options () {
      this.renderChart(this.chartData, this.options);
    }
  },
  mounted () {
    this.renderChart(this.chartData, this.options);
  }
}
