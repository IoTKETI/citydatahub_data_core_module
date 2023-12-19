<template>
  <ul class="nav nav-mobile-menu">
    <li class="nav-item">
      <a class="nav-link" href="#" @click="logout">
        <span class="no-icon">Log out</span>
      </a>
    </li>
  </ul>
</template>
<script>
  export default {
    name: 'mobile-menu',
    methods: {
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
    }
  }
</script>
<style>
</style>
