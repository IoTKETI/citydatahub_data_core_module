<template>
  <div class="wrapper">
    <side-bar>
      <mobile-menu slot="content"></mobile-menu>
      <sidebar-link to="/mainDashboard">
        <i class="nc-icon nc-chart-pie-35"></i>
        <p>{{ $t('dashboard.dashboard') }}</p>
      </sidebar-link>
      <sidebar-link to="/dataSearch">
        <i class="nc-icon nc-notes"></i>
        <p>{{ $t('search.dataSearch') }}</p>
      </sidebar-link>
      <sidebar-link to="/mapSearchLatest">
        <i class="nc-icon nc-pin-3"></i>
        <p>{{ $t('search.mapSearchLatest') }}</p>
      </sidebar-link>
      <sidebar-link to="/mapSearchHistorical">
        <i class="nc-icon nc-pin-3"></i>
        <p>{{ $t('search.mapSearchHistorical') }}</p>
      </sidebar-link>
    </side-bar>
    <div class="main-panel">
      <top-navbar></top-navbar>
      <dashboard-content @click="toggleSidebar">
      </dashboard-content>
    </div>
  </div>
</template>
<style lang="scss">
.sidebar .sidebar-wrapper, body > .navbar-collapse .sidebar-wrapper {
  width: 270px !important;
}
</style>
<script>
/**
 * App Layout
 * @components TopNavbar, ContentFooter, DashboardContent, MobileMenu
 */
import TopNavbar from './TopNavbar.vue'
import ContentFooter from './ContentFooter.vue'
import DashboardContent from './Content.vue'
import MobileMenu from './MobileMenu.vue'
export default {
  components: {
    TopNavbar,
    ContentFooter,
    DashboardContent,
    MobileMenu
  },
  methods: {
    toggleSidebar () {
      if (this.$sidebar.showSidebar) {
        this.$sidebar.displaySidebar(false)
      }
    }
  }
}

</script>
