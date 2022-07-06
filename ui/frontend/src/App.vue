<template>
  <div id="app">
    <div class="wrap">
      <AppMenu />
      <hr>
      <AppHeader />
      <main class="main">
        <div class="content">
          <router-view />
          <AppFooter />
        </div>
      </main>
    </div>
  </div>
</template>

<style src="./assets/css/sub.css"></style>
<script>
/**
 * datacore-ui view page start.
 */
import AppMenu from '@/components/AppMenu';
import AppHeader from '@/components/AppHeader';
import AppFooter from '@/components/AppFooter';

export default {
  name: 'App',
  data() {
    return {}
  },
  components: {
    AppMenu,
    AppHeader,
    AppFooter
  },
  mounted() {
    console.log(this.$route);
  }
};
</script>

<style lang="scss" scoped>
</style>
