/***
 * searchData state manage
 * @type {{searchData: {}}}
 */

const state = {
  dataModelSearchData: {},
  dataSetInfoSearchData: {},
  dataSetFlowSearchData: {},
  verificationHistorySearchData: {},
  provisionSearchData: {},
  externalPlatformSearchData: {}
};

const getters = {
  getDataModel(state) {
    return state.dataModelSearchData;
  },
  getDataSetInfo(state) {
    return state.dataSetInfoSearchData;
  },
  getDataSetFlow(state) {
    return state.dataSetFlowSearchData;
  },
  getVerificationHistory(state) {
    return state.verificationHistorySearchData;
  },
  getProvision(state) {
    return state.provisionSearchData;
  },
  getExternalPlatform(state) {
    return state.externalPlatformSearchData;
  }
};

const actions = {
  dataModelSearchData({ commit }) {
    commit('setDataModelSearchData');
  },
  dataSetInfoSearchData({ commit }) {
    commit('setDataSetInfoSearchData');
  },
  dataSetFlowSearchData({ commit }) {
    commit('setDataSetFlowSearchData');
  },
  verificationHistorySearchData({ commit }) {
    commit('setVerificationHistorySearchData');
  },
  provisionSearchData({ commit }) {
    commit('setProvisionSearchData');
  },
  externalPlatformSearchData({ commit }) {
    commit('setExternalPlatformSearchData');
  }
};

const mutations = {
  setDataModelSearchData(state, items) {
    state.dataModelSearchData = items;
  },
  setDataSetInfoSearchData(state, items) {
    state.dataSetInfoSearchData = items;
  },
  setDataSetFlowSearchData(state, items) {
    state.dataSetFlowSearchData = items;
  },
  setVerificationHistorySearchData(state, items) {
    state.verificationHistorySearchData = items;
  },
  setProvisionSearchData(state, items) {
    state.provisionSearchData = items;
  },
  setExternalPlatformSearchData(state, items) {
    state.externalPlatformSearchData = items;
  }
};

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
};
