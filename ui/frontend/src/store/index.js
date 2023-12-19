/**
 * vuex instance
 * actions : actions
 * getters : state
 * modules : mutations
 */
import Vue from 'vue';
import Vuex from 'vuex';
import data from './modules/data';
import searchData from './modules/search-data';
import dataModels from './modules/data-model-list';
import dataSets from './modules/data-set-list';
import createLogger from 'vuex/dist/logger';

Vue.use(Vuex);

const debug = process.env.NODE_ENV !== 'production';

const store = new Vuex.Store({
  modules: {
    data,
    searchData,
    dataModels,
    dataSets
  },
  strict: false,
  plugins: debug ? [createLogger()] : []
});

export default store;
