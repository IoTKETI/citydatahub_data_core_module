/***
 * searchData state manage
 * @type {{searchData: {}}}
 */

const state = {
  dataSetList: []
};

const getters = {
  getDataSetList(state) {
    return state.dataModelList;
  }
};

const actions = {
  dataSetList({ commit }) {
    commit('setDataSetList');
  }
};

const mutations = {
  setDataSetList(state, items) {
    state.dataSetList = items;
  }
};

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
};
