import { CHART_COLORS, namedColor, color, transparentize } from '@/components/Chart/Utils';
import moment from 'moment';


/**
 * Dataset by type of dashboard widget
 */

// Chart default options
export const chartOptions = (option) => {
  return {
    responsive: true,
    maintainAspectRatio: false,
    title: {
      display: true,
      text: option.title
    }
  };
};

// Bar Chart options
export const barChartOptions = (option) => {
  let ticks = {};
  if (option.yaxisRange) {
    ticks = {
      beginAtZero: true,
      min: parseInt(option.yaxisRange.split('-')[0]),
      max: parseInt(option.yaxisRange.split('-')[1]),
    };
  } else {
    ticks = {
      beginAtZero: true
    };
  }
  return {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      yAxes: [{
        ticks: ticks,
        scaleLabel: {
          display: !!option.chartYName,
          labelString: option.chartYName
        }
      }],
      xAxes: [{
        scaleLabel: {
          display: !!option.chartXName,
          labelString: option.chartXName
        }
      }],
    },
    title: {
      display: true,
      text: option.title
    }
  };
};

// Line Chart options
export const lineChartOptions = (option) => {
  let ticks = {};
  if (option.yaxisRange) {
    ticks = {
      beginAtZero: true,
      min: parseInt(option.yaxisRange.split('-')[0]),
      max: parseInt(option.yaxisRange.split('-')[1]),
    };
  } else {
    ticks = {
      beginAtZero: true
    };
  }
  return {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      yAxes: [{
        ticks: ticks,
        scaleLabel: {
          display: !!option.chartYName,
          labelString: option.chartYName
        }
      }],
      xAxes: [{
        scaleLabel: {
          display: !!option.chartXName,
          labelString: option.chartXName,
        }
      }],
    },
    title: {
      display: true,
      text: option.title
    }
  };
};

// Histogram String Chart options
export const histogramStrChartOptions = (option) => {
  const result = barChartOptions(option);
  result.legend = {display: false};
  return result;
};

// Histogram Number Chart options
export const histogramNumberChartOptions = (option, chartUnit, maxX) => {
  return {
    responsive: true,
    maintainAspectRatio: false,
    legend: {
      display: false,
    },
    tooltips: {
      callbacks: {
        title: function(tooltipItem, data) {
          const label = parseInt(tooltipItem[0]['xLabel']);
          return `${label}-${(label + parseInt(chartUnit))}` ;
        },
      }
    },
    scales: {
      xAxes: [{
        display: false,
        ticks: {
          autoSkip: true,
          max: maxX - chartUnit, // 최종 값 - 기준 값 (ex- 0~100 / 10 단위면 90까지)
        },
      },
        {
          offset: false,
          display: true,
          ticks: {
            autoSkip: true,
            max: maxX, // 최종 값 - 기준 값 (ex- 0~100)
          }
        }
      ],
      yAxes: [{
        ticks: {
          beginAtZero: true
        }
      }]
    },
    title: {
      display: true,
      text: option.title
    }
  };
};

// Donut, Pie Chart dateset data binding
export const setDonutChart = (donutData) => {
  const rendomIndex = Math.floor(Math.random() * 13);
  let labels = [];
  let datasets = [];
  let data = [];
  let bgColor = [];

  // Setting the data according to the received array value.
  donutData.data.map((item, index) => {
    labels.push(item.id);
    data.push(item.chartValue);
    bgColor.push(color(rendomIndex + index));
  });
  datasets.push({
    label: 'donut',
    data: data,
    backgroundColor: bgColor,
    hoverOffset: 4
  });
  return { labels: labels, datasets: datasets };
};

// Bar Chart dateset data binding
export const setBarChartLast = (barData) => {
  const randomIndex = Math.floor(Math.random() * 13);
  let labels = [];
  let datasets = [];
  let data = [];
  let bgColor = [];
  let borderColor = [];

  // Setting the data according to the received array value.
  let chartLabels = barData.entityIds;
  const hasLegendvalues = !!barData.legendvalues;
  barData.data.map((item, index) => {
    labels.push(hasLegendvalues ? barData.legendvalues[index] : item.id);
    data.push(item.chartValue);
    bgColor.push(transparentize(color(randomIndex + index), 0.5));
    borderColor.push(color(randomIndex + index));
  });
  datasets.push({
    label: barData.attributeId,
    data: data,
    borderColor: borderColor,
    backgroundColor: bgColor,
    borderWidth: 1
  });

  return { labels: labels, datasets: datasets };
};

// Bar History Chart dateset data binding
export const setBarChartHistory = (barData) => {
  const rendomIndex = Math.floor(Math.random() * 13);
  let labels = new Set();
  let datasets = [];

  //If you have two entity IDs, you need to create two data sets.
  // barchart single id
  let chartLabels = barData.entityIds;
  const hasLegendvalues = !!barData.legendvalues;
  chartLabels.forEach((entityId, index) =>
      datasets.push({
        label: hasLegendvalues ? barData.legendvalues[index] : entityId,
        entityId,
        data: [],
        borderColor: color(rendomIndex + index),
        backgroundColor: transparentize(color(rendomIndex + index), 0.5),
        borderWidth: 1
      }));

  // Find and map the entity ID corresponding to the created data set.
  // Data extract
  barData.data.forEach((item, index) => {
    datasets.map(d => item.id === d.entityId && d.data.push(item.chartValue));
    labels.add(moment(item.observedAt).format('YYYY-MM-DD HH:mm:ss'));
  });
  return { labels: Array.from(labels), datasets };
};

/**
 * Data (datasets, label) setting for line widget type
 *
 * @param lineData
 * @returns {{datasets: *[], labels: *[]}}
 */
export const setLineChart = (lineData) => {
  const rendomIndex = Math.floor(Math.random() * 13);
  let labels = new Set();
  let datasets = [];

  // If you have two entity IDs, you need to create two data sets.
  let chartLabels = lineData.entityIds;
  const hasLegendvalues = !!lineData.legendvalues;
  chartLabels.forEach((entityId, index) =>
      datasets.push({
        label: hasLegendvalues ? lineData.legendvalues[index] : entityId,
        entityId,
        data: [],
        fill: false,
        borderColor: color(rendomIndex + index), tension: 0.1
      }));

  // Find and map the entity ID corresponding to the created data set.
  // Data extract
  lineData.data.forEach((item, index) => {
    datasets.map(d => item.id === d.entityId && d.data.push(item.chartValue));
    labels.add(moment(item.observedAt).format('YYYY-MM-DD HH:mm:ss'));
  });

  return { labels: Array.from(labels), datasets };};


/**
 * Data (datasets, label) setting for scatter widget type
 *
 * @param setScatterLastChart
 * @returns {{datasets: *[], labels: *[]}}
 */
export const setScatterLastChart = (scatterData) => {
  const randomIndex = Math.floor(Math.random() * 13);
  let labels = [];
  let datasets = [];

  // If you have two entity IDs, you need to create two data sets.
  let chartLabels = scatterData.entityIds;
  const hasLegendvalues = !!scatterData.legendvalues;
  chartLabels.forEach((entityId, index) =>
    datasets.push({
      label: hasLegendvalues ? scatterData.legendvalues[index] : entityId,
      entityId,
      data: [],
      fill: false,
      borderColor: color(randomIndex + index),
      backgroundColor: color(randomIndex + index),
    }));

  // Find and map the entity ID corresponding to the created data set.
  // Data extract
  scatterData.data.forEach((item, index) => {
    datasets[index].data.push(item);
    labels.push(moment(item.observedAt).format('YYYY-MM-DD HH:mm:ss'));
  });

  return { labels: labels, datasets: datasets };
};

/**
 * Data (datasets, label) setting for scatter widget type
 *
 * @param setScatterHistoryChart
 * @returns {{datasets: *[], labels: *[]}}
 */
export const setScatterHistoryChart = (scatterData) => {
  const randomIndex = Math.floor(Math.random() * 13);
  let labels = [];
  let datasets = [];

  // If you have two entity IDs, you need to create two data sets.
  let chartLabels = scatterData.entityIds;
  const hasLegendvalues = !!scatterData.legendvalues;
  chartLabels.forEach((entityId, index) =>
    datasets.push({
      label: hasLegendvalues ? scatterData.legendvalues[index] : entityId,
      entityId,
      data: [],
      fill: false,
      borderColor: color(randomIndex + index),
      backgroundColor: color(randomIndex + index),
    }));

  datasets[0].data = scatterData.data;

  return { labels: labels, datasets: datasets };
};

// Histogram Last Chart dateset data binding
export const setHistogramNumberChart = (histogramData, chartUnit) => {
  let randomIndex = Math.floor(Math.random() * 13);
  let labels = [0]; // 항상 0으로 시작
  let datasets = [];
  let data = [];
  let bgColor = [];
  let borderColor = [];

  const half = chartUnit / 2;
  // Setting the data according to the received array value.
  histogramData.data.forEach((item, index) => {
    labels.push(item.x + half);
    data.push(item.y);
    randomIndex = Math.floor(Math.random() * 13);
    bgColor.push(transparentize(color(randomIndex + index), 0.5));
    borderColor.push(color(randomIndex + index));
  });
  datasets.push({
    label: histogramData.attributeId,
    data: data,
    borderColor: borderColor,
    backgroundColor: bgColor,
    borderWidth: 1,
    barPercentage: 1, // 공백 없게
    categoryPercentage: 1, // 공백 없게
  });
  return { labels: labels, datasets: datasets };
};

// Histogram Last Chart dateset data binding
export const setHistogramStrChart = (histogramData) => {
  const randomIndex = Math.floor(Math.random() * 13);
  let labels = [];
  let datasets = [];
  let data = [];
  let bgColor = [];
  let borderColor = [];

  // Setting the data according to the received array value.
  histogramData.data.map((item, index) => {
    labels.push(item.x);
    data.push(item.y);
    bgColor.push(transparentize(color(randomIndex + index), 0.5));
    borderColor.push(color(randomIndex + index));
  });
  datasets.push({
    label: histogramData.attributeId,
    data: data,
    borderColor: borderColor,
    backgroundColor: bgColor,
    borderWidth: 1
  });
  return { labels: labels, datasets: datasets };
};
