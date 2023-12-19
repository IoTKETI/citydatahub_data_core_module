// /**
//  * vuex 인스턴스 생성
//  * store.js 는 전역으로 사용할 vuex
//  * 각각 하위 모듈은 modules 에 구현
//  * actions : actions
//  * getters : state
//  * modules : mutations
//  */
// import Vue from 'vue';
// import Vuex from 'vuex';
// import axios from 'axios';
// import createLogger from 'vuex/dist/logger';
// import router from '../router/routes';
// import users from './modules/users';
//
// Vue.use(Vuex);
//
// const debug = process.env.NODE_ENV !== 'production';
//
// export default new Vuex.Store({
//   modules: {
//     users
//   },
//   strict: debug,
//   plugins: debug ? [createLogger()] : [],
//   state: {
//     isLogin: !!localStorage.getItem('access_token'),
//     sessionInfo: null,
//     userList: null
//   },
//   getters: {
//     getSession: (state) => {
//       return state.sessionInfo;
//     }
//   },
//   // 동기 처리 로직은 여기서
//   mutations: {
//     addSession(state, payload) {
//       state.sessionInfo = payload;
//     },
//     addUser(state, payload) {
//       state.userList = payload;
//     },
//     isLogined(state, payload) {
//       state.isLogin = payload;
//     }
//   },
//   // 전역 비동기 처리 로직은 여기서 작성한다. 지금은 Login 기능만.
//   actions: {
//     login({ dispatch }, parameter) {
//       let form = new FormData();
//       form.append('userId', parameter.userId);
//       form.append('userPassword', parameter.userPassword);
//
//       axios.post(`/api/login`, form)
//         .then(response => {
//           let token = response.data.token;
//           if (token) {
//             localStorage.setItem('access_token', token);
//             dispatch('getMemberInfo');
//           }
//         })
//         .catch(error => {
//           console.log(error);
//           alert('아이디 또는 비밀번호를 확인하세요.');
//         });
//     },
//     logout({ commit }, token) {
//       localStorage.removeItem('access_token');
//       commit('addSession', null);
//       commit('isLogined', false);
//       setTimeout(() => {
//         router.push({ name: 'Login' });
//       }, 1000);
//     },
//     getMemberInfo({ commit }) {
//       let token = localStorage.getItem('access_token');
//       if (token && token !== '') {
//         let config = {
//           headers: {
//             'access-token': token
//           }
//         };
//         axios.get('/api/users', config)
//           .then(response => {
//             let result = response.data;
//             commit('addSession', result);
//             commit('isLogined', true);
//             router.push({
//               name: 'Dashboard'
//             });
//           });
//       }
//     }
//   }
// });
