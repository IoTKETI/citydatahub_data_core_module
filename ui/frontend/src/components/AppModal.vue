<template>
  <div v-if="isShow" class="modal" style="display:block">
    <div class="modal__wrap">
      <div :class="`modal__content ${ modalSize }`">
        <div
            v-if="title"
            class="modal__header"
        >
          <h4 class="modal__title">{{ title }}</h4>
          <button
              class="modal__button--close button__modal--close"
              type="button"
              @click="onClose"
          >
            <span class="hidden">모달 닫기</span>
          </button>
        </div>
        <div class="modal__body">
          <p style="font-size: 12px;">
            {{ content }}
          </p>
          <slot name="elements"></slot>
        </div>
        <div class="modal__footer">
          <button
              v-if="isSuccessBtn"
              class="button__primary"
              type="button"
              name="successBtn"
              @click="onDelivery"
          >
            {{ buttonName }}
          </button>
          <button
              v-if="isDelBtn"
              class="button__primary"
              type="button"
              name="deleteBtn"
              @click="onDelivery"
          >
            {{ $t('comm.delete') }}
          </button>
          <button
              v-for="item in buttonGroup"
              v-if="!item.isHide"
              :class="item.className"
              :name="item.id"
              type="button"
              @click="onDelivery"
          >
            {{ item.name }}
          </button>
          <button
              v-if="isCancelBtn"
              @click="onClose"
              class="button__secondary button__modal--close"
              type="button"
          >
            {{ closeName ? closeName: $t('comm.cancel') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AppModal',
  props: {
    isShow: Boolean,
    content: String,
    title: String,
    buttonGroup: Array,
    buttonName: String,
    closeName: String,
    // w-1000 (modal max width)
    modalSize: String,
    isSuccessBtn: Boolean,
    isCancelBtn: Boolean,
    isDelBtn: Boolean
  },
  methods: {
    onClose() {
      this.$emit('close-modal');
    },
    onDelivery(event) {
      this.$emit('on-event-modal', event.target.name);
    }
  }
}
</script>

<style scoped>
.w-360 {
  width: 360px;
}
.modal__body {
  padding: 1rem;
}
.modal__footer {
  padding: 1rem;
}
</style>