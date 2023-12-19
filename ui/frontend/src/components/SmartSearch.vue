<template>
  <div>
    <div class="button__group">
<!--      <el-tooltip placement="left">-->
<!--        <div slot="content">-->
<!--          * 데이터셋 아이디 변경 시 목록을 검색합니다.-->
<!--        </div>-->
<!--        <select-->
<!--            v-show="isSelectBox"-->
<!--            class="input__text" style="width: 15%; font-size: 12px;"-->
<!--            @change="onChange($event)"-->
<!--        >-->
<!--          <option-->
<!--              v-for="( choice, key ) in choices"-->
<!--              :value="choice.id"-->
<!--          >-->
<!--            {{ choice.name }}-->
<!--          </option>-->
<!--        </select>-->
<!--      </el-tooltip>-->
<!--      <el-tooltip placement="left">-->
<!--        <div slot="content">-->
<!--          * 스마트 검색 : 입력 값을 모든 필드를 대상으로 검색합니다.-->
<!--          <br/><br/>-->
<!--          * 상세 검색 : 상세 조건 설정 값으로 검색 합니다.-->
<!--        </div>-->
<!--        <input-->
<!--            id="search"-->
<!--            class="input__text"-->
<!--            style="width: 20%; font-size: 12px;"-->
<!--            type="text"-->
<!--            placeholder="스마트 검색"-->
<!--        />-->
<!--      </el-tooltip>-->
      <button
          @click="onDelivery"
          :class="buttonClass ? buttonClass : 'button__primary'"
          type="button"
      >
        {{ buttonName }}
      </button>
    </div>
  </div>
</template>

<script>
/**
 * Common search component
 */
export default {
  name: 'AppButtons',
  props: {
    buttonName: String,
    buttonClass: String,
    isSelectBox: Boolean,
    choices: Array
  },
  data() {
    return {}
  },
  methods: {
    onDelivery() {
      this.$emit('smart-search');
    },
    onChange(event) {
      const value = event.target.value;
      this.$emit('on-change', value);
    }
  }
}
</script>

<style scoped>
input#search {
  background-image: url(../assets/img/search-black-18dp.svg);
  background-repeat: no-repeat;
  text-indent: 35px;
  background-position: 5px center;
  box-sizing: border-box;
}
/*input#search:focus{*/
/*  background-image: none;*/
/*  text-indent: 5px;*/
/*}*/
</style>