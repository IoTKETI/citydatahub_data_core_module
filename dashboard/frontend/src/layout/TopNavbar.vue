<template>
  <nav class="navbar navbar-expand-lg">
    <div class="container-fluid">
      <a class="navbar-brand" href="#">&nbsp;</a>
      <button type="button"
              class="navbar-toggler navbar-toggler-right"
              :class="{toggled: $sidebar.showSidebar}"
              aria-controls="navigation-index"
              aria-expanded="false"
              aria-label="Toggle navigation"
              @click="toggleSidebar">
        <span class="navbar-toggler-bar burger-lines"></span>
        <span class="navbar-toggler-bar burger-lines"></span>
        <span class="navbar-toggler-bar burger-lines"></span>
      </button>
      <div class="collapse navbar-collapse justify-content-end">
<!--        <ul class="nav navbar-nav mr-auto">-->
<!--          <li class="nav-item">-->
<!--            <a class="nav-link" href="#" data-toggle="dropdown">-->
<!--              <i class="nc-icon nc-palette"></i>-->
<!--            </a>-->
<!--          </li>-->
<!--          <base-dropdown tag="li">-->
<!--            <template slot="title">-->
<!--              <i class="nc-icon nc-planet"></i>-->
<!--              <b class="caret"></b>-->
<!--              <span class="notification">5</span>-->
<!--            </template>-->
<!--            <a class="dropdown-item" href="#">Notification 1</a>-->
<!--            <a class="dropdown-item" href="#">Notification 2</a>-->
<!--            <a class="dropdown-item" href="#">Notification 3</a>-->
<!--            <a class="dropdown-item" href="#">Notification 4</a>-->
<!--            <a class="dropdown-item" href="#">Another notification</a>-->
<!--          </base-dropdown>-->
<!--          <li class="nav-item">-->
<!--            <a href="#" class="nav-link">-->
<!--              <i class="nc-icon nc-zoom-split"></i>-->
<!--              <span class="d-lg-block">&nbsp;Search</span>-->
<!--            </a>-->
<!--          </li>-->
<!--        </ul>-->
        <ul class="navbar-nav ml-auto">
          <i class="nc-icon nc-circle-09"></i>
          <base-dropdown :title="`${userId}`">
            <a class="dropdown-item" href="#" @click="logout">LOGOUT</a>
          </base-dropdown>
          <li class="nav-item">
            <a href="#" class="nav-link" style="visibility: hidden;">
            </a>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>
<script>
  export default {
    computed: {
      routeName () {
        const {name} = this.$route
        return this.capitalizeFirstLetter(name)
      }
    },
    data () {
      return {
        activeNotifications: false,
        userId: null,
      }
    },
    methods: {
      capitalizeFirstLetter (string) {
        return string.charAt(0).toUpperCase() + string.slice(1)
      },
      toggleNotificationDropDown () {
        this.activeNotifications = !this.activeNotifications
      },
      closeDropDown () {
        this.activeNotifications = false
      },
      toggleSidebar () {
        this.$sidebar.displaySidebar(!this.$sidebar.showSidebar)
      },
      hideSidebar () {
        this.$sidebar.displaySidebar(false)
      },
      getUserId() {
        this.$http.get('/userId')
          .then(response => {
            this.userId = response.data;
          });
      },
      logout() {
        this.$http.get('/logout')
          .then(response => {
            const status = response.status;
            if (status === 200) {
              console.log('success Logout.');
              location.replace('/');
            }
          });
      }
    },
    mounted() {
      this.getUserId();
    }
  }

</script>
<style>

</style>
