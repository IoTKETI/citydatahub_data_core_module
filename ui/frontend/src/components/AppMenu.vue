<template>
  <!-- aside -->
  <div class="aside">
    <div class="aside__header">
      <h1 class="aside__logo" style="margin-top: 0;">
        <a href="/"><span class="hidden">Smart City Hub</span></a>
      </h1>
<!--      <a href="/">-->
<!--        <img src="../assets/img/city_data_hub_logo_W.png" alt="" />-->
<!--      </a>-->
    </div>
    <div class="aside__content">
      <h2 class="hidden">주메뉴</h2>
      <div class="aside__user">
        <p class="aside__user-message">
          <strong class="aside__user-message--strong">{{ userInfo.name }}</strong> {{ $t('comm.welcome') }}
        </p>
      </div>
       <!-- nav -->
      <nav class="nav">
        <ul>
          <li
              v-for="menuItem in menus"
              class="nav__depth1 material-icons"
              @click="showMenuOneDept"
          >
            <button v-if="menuItem.children" @click="showMenuTowDept" class="nav__link nav__button" type="button">{{ menuItem.name }}</button>
            <router-link v-else class="nav__link" :to="menuItem.url" @click.native="goMenu">{{ menuItem.name }}</router-link>
            <ul v-if="menuItem.children" class="nav__depth2">
              <li v-for="subItem in menuItem.children" class="nav__item" @click="showMenuThreeDept">
                <router-link class="nav__link" :to="subItem.url">{{ subItem.name }}</router-link>
              </li>
            </ul>
          </li>
        </ul>
      </nav>
      <!-- //nav -->
    </div>
  </div>
  <!-- //aside -->
</template>

<script>
/**
 * Left Menu
 */
import { APIHandler } from '@/modules/api-handler';
import { traverse, errorRender } from '@/modules/utils';

export default {
  name: 'AppMenu',
  data() {
    return {
      userInfo: {},
      index: 0,
      menus: [],
      elements: ''
    }
  },
  methods: {
    showMenuOneDept(event) {
      $('.nav__depth1 ul').slideUp(300);
      $('.nav__depth1--active').removeClass('nav__depth1--active');
    },
    showMenuTowDept(event) {
      event.stopPropagation();
      let element = event.target.nextElementSibling;
      $('.nav__depth1 ul').slideUp(300);
      $('.nav__depth1--active').removeClass('nav__depth1--active');
      $(element).parent().addClass('nav__depth1--active');
      if ($(element).css('display') !== 'block') {
        $(element).slideToggle(300);
      } else {
        $(element).parent().removeClass('nav__depth1--active');
      }
    },
    showMenuThreeDept(event) {
      event.stopPropagation();
    },
    getUser() {
      this.$http.get(APIHandler.buildUrl(['user']))
          .then(response => {
            this.userInfo = response.data;
          });
    },
    defaultLocale() {
      let locale = localStorage.getItem('langCd');
      if (!locale) {
        locale = 'en';
      }
      return locale;
    },
    getMenuList() {
      const locale = this.defaultLocale();
      this.$http.get(APIHandler.buildUrl([`accessmenu`]))
          .then(response => {
            const items = response.data;
            let rootNodes = [];
            items.map((item, index) => {
              const key = `leftMenu.${item.id}`;
              item.name = this.$i18n.t(key);
              if (item.level === 1) {
                return rootNodes.push(item);
              }
              return traverse(rootNodes, item, index);
            });
            rootNodes.sort((a, b) => a.sortOrder - b.sortOrder);
            rootNodes.forEach((node) => {
              if (node["children"]) {
                node["children"] = node["children"].sort(
                  (a, b) => a.sortOrder - b.sortOrder
                );
              }
            });            
            this.menus = rootNodes;
          });
    },
    goMenu() {
      this.$router.go(this.$router.currentRoute);
    }
  },
  mounted() {
    this.getUser();
    this.getMenuList();
  }
};
</script>

<style scoped>
/* material icon css */
.nav__depth1:nth-of-type(1):before{ content:"dvr" }

img {
  height: 50px;
}
</style>