/***
 * searchData state manage
 * @type {{searchData: {}}}
 */

const state = {
  dataModelList: []
};

const getters = {
  getDataModelList(state) {
    return state.dataModelList;
  }
};

const actions = {
  dataModelList({ commit }) {
    commit('setDataModelList');
  }
};

const mutations = {
  setDataModelList(state, items) {
    state.dataModelList = items;
  }
};

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
};
