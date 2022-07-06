/**
 * Vue config
 */
import Vue from 'vue';
import ElementUI from 'element-ui';
import lang from 'element-ui/lib/locale/lang/en'
import locale from 'element-ui/lib/locale'
import App from './App.vue';
import router from './router/index';
import axios from 'axios';
import store from './store';

import i18n from './modules/i18n'

// eslint-disable-next-line import/no-webpack-loader-syntax
import 'expose-loader?exposes[]=$&exposes[]=jQuery!jquery';
import '@/assets/js/common-ui';
import 'element-ui/lib/theme-chalk/index.css';

import 'es6-promise/auto';

// locale.use(lang);
Vue.use(ElementUI);
Vue.config.productionTip = false;

axios.defaults.headers.common['Authorization'] = 'testCode'; // 수정예정
axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';
axios.defaults.headers.timeout = 60000;
Vue.prototype.$http = axios;

locale.use(lang);

new Vue({
  router,
  store,
  render: h => h(App),
  components: { App },
  i18n
}).$mount('#app');
