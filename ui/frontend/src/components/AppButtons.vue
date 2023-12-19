<template>
  <button
      v-show="!isHide"
      :class="buttonClass ? buttonClass : 'button__primary'"
      @click="onDelivery"
      type="button"
  >
    {{ buttonName }}
  </button>
</template>

<script>
/**
 * Common Button component
 */
export default {
  name: 'AppButtons',
  props: {
    isText: Boolean,
    buttonName: String,
    buttonClass: String,
    isHide: Boolean
  },
  methods: {
    onDelivery(event) {
      this.$emit('on-button-event', event);
    }
  }
}
</script>

<style scoped>

</style>