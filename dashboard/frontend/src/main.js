/*!

 =========================================================
 * Vue Light Bootstrap Dashboard - v2.0.0 (Bootstrap 4)
 =========================================================

 * Product Page: http://www.creative-tim.com/product/light-bootstrap-dashboard
 * Copyright 2019 Creative Tim (http://www.creative-tim.com)
 * Licensed under MIT (https://github.com/creativetimofficial/light-bootstrap-dashboard/blob/master/LICENSE.md)

 =========================================================

 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 */
import Vue from 'vue'

import * as VueGoogleMaps from 'vue2-google-maps';

import ElementUI from 'element-ui'
import lang from 'element-ui/lib/locale/lang/en'
import locale from 'element-ui/lib/locale'
import VueRouter from 'vue-router'
import App from './App.vue'
import axios from 'axios'

// LightBootstrap plugin
import LightBootstrap from './light-bootstrap-main'

import { BootstrapVue, IconsPlugin } from 'bootstrap-vue'
// Import Bootstrap an BootstrapVue CSS files (order is important)
import 'element-ui/lib/theme-chalk/index.css'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'
import 'tui-pagination/dist/tui-pagination.css'

import 'vue-simple-context-menu/dist/vue-simple-context-menu.css'
import VueSimpleContextMenu from 'vue-simple-context-menu'

import routes from './routes/routes'
import './registerServiceWorker'

import TuiGrid from 'tui-grid'
import i18n from './moudules/i18n'

TuiGrid.setLanguage(i18n.locale, { display: { noData: i18n.t('message.noSearchResult') } });

Vue.component('vue-simple-context-menu', VueSimpleContextMenu);


axios.defaults.headers.common['Authorization'] = 'testCode'; // TODO going to fix it.
axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';
axios.defaults.headers.timeout = 60000;
Vue.prototype.$http = axios;

locale.use(lang);
Vue.use(ElementUI);
Vue.use(VueRouter);
Vue.use(LightBootstrap);
Vue.use(BootstrapVue);
Vue.use(IconsPlugin);
Vue.use(VueGoogleMaps);

// configure router
const router = new VueRouter({
  mode: 'hash',
  routes, // short for routes: routes
  linkActiveClass: 'nav-item active',
  scrollBehavior: (to) => {
    if (to.hash) {
      return {selector: to.hash}
    } else {
      return { x: 0, y: 0 }
    }
  }
});

/* eslint-disable no-new */
new Vue({
  el: '#app',
  render: h => h(App),
  router,
  i18n
});
