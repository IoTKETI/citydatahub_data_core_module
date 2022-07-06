<template>
  <div>
    <header class="header">
      <div class="header__util">
        <button class="button__nav--toggle" type="button" @click="showNav">
          <i class="nav__icon"><span class="hidden">주메뉴 확장/축소 하기</span></i>
        </button>
        <el-dropdown size="small" trigger="click">
        <span class="header__user el-dropdown-link" style="cursor: pointer;">
          {{ `${userInfo['name']}(${userInfo['userId']})` }}
          <i class="el-icon-arrow-down el-icon--right" />
        </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item icon="el-icon-user-solid">
              <a href="#" @click="onShowPopup">{{ $t('comm.userInfo') }}</a>
            </el-dropdown-item>
            <el-dropdown-item icon="el-icon-user-solid">
              <a href="#" @click="logout">{{ $t('comm.logout') }}</a>
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
      <div class="breadcrumb">
        <a class="breadcrumb__list--home" href="#none">
          <span class="hidden">Home</span>
        </a><span class="breadcrumb__list">{{ $route.meta.breadcrumb[0] }}</span>
<!--        <span class="breadcrumb__list&#45;&#45;current">{{ $route.meta.breadcrumb[1] }}</span>-->
      </div>
    </header>
    <AppModal
        :is-show="isShow"
        @close-modal="onClose"
        :title="$t('comm.userPopupTitle')"
        modalSize="w-360"
        :close-name="$t('comm.ok')"
        :isCancelBtn="true"
    >
      <template v-slot:elements>
        <section class="section">
          <div class="section__content">
            <table class="table--row">
              <tbody>
              <tr>
                <th>{{ $t('comm.userId') }}</th>
                <td>{{ userInfo['userId'] }}</td>
              </tr>
              <tr>
                <th>{{ $t('comm.name') }}</th>
                <td>{{ userInfo['name'] }}</td>
              </tr>
              <tr>
                <th>{{ $t('comm.contact') }}</th>
                <td>{{ userInfo['phone'] }}</td>
              </tr>
              </tbody>
            </table>
          </div>
        </section>
      </template>
    </AppModal>
  </div>
</template>

<script>
/**
 * Header Menu
 * Login, Logout
 * User Information
 */
import { APIHandler } from '@/modules/api-handler';
import AppModal from '@/components/AppModal';

export default {
  name: 'AppHeader',
  components: {
    AppModal
  },
  props: {
    title: Object
  },
  data() {
    return {
      userInfo: {},
      isShow: false
    };
  },
  methods: {
    showNav() {
      if ($('.wrap').hasClass('wrap--wide')) {
        $('.wrap').removeClass('wrap--wide');
        $('.button__nav--toggle').attr('title','메뉴 접기');
        $('.nav__depth1').off('mouseenter').off('mouseleave');
      } else {
        $('.wrap').addClass('wrap--wide');
        $('.button__nav--toggle').attr('title','메뉴 펼치기');
        $('.nav__depth1').removeClass('nav__depth1--active');
       $('.nav__depth2').hide();
      }
    },
    getUser() {
      this.$http.get(APIHandler.buildUrl(['user']))
          .then(response => {
            this.userInfo = response.data;
          });
    },
    onShowPopup() {
      this.isShow = true;
    },
    onClose() {
      this.isShow = false;
    },
    logout() {
      this.$http.get(APIHandler.buildUrl(['logout']))
          .then(response => {
            const resultCode = response.status;
            if (resultCode === 200 || 201 || 204) {
              location.replace('/');
            }
          });
    }
  },
  mounted() {
    this.getUser();
  }
};
</script>

<style scoped>

</style>
