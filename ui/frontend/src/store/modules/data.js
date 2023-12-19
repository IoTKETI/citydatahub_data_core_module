/***
 * Form data state manage
 * @type {{objectData: null}}
 */

const state = {
  objectData: { indexAttributeNames: [], attributes: [] },
  mode: null
};

const getters = {
  getData(state) {
    return state.objectData;
  },
  getMode(state) {
    return state.mode;
  }
};

const actions = {
  findByData({ commit }) {
    commit('setObjectData');
  },
  findByMode({ commit }) {
    commit('setMode');
  }
};

const mutations = {
  setObjectData(state, items) {
    state.objectData = items;
  },
  setMode(state, items) {
    state.mode = items;
  }
};

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
};