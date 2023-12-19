<template>
  <div>
    <div v-if="type === 'Image'" class="col-12 text-center card-image">
      <p class="card-category"><b>{{ this.options.title }}</b></p>
      <!--      <div class="card-image">-->
      <img :src="image.url" alt="image">
      <!--      </div>-->
    </div>
    <div v-else-if="type === 'custom_text'" class="col-12 card-custom" style="padding: 20px 15px;">
      <p class="card-category text-center "><b>{{ this.options.title }}</b></p>
      <p :style="customTextStyle">{{ customText.text }}</p>
    </div>
    <div v-else class="col-12 text-center">
      <div class="numbers">
        <div v-if="type === 'text'">
          <p class="card-category"><b>{{ this.options.title }}</b></p>
          <h4 class="card-title">{{ score }}</h4>
        </div>
        <div v-else>
          <p class="card-category"><b>{{ this.options.title }}</b></p>
          <el-switch
            v-model="value"
            active-color="#13ce66"
            inactive-color="#ff4949">
          </el-switch>
          <p class="card-title">{{ value }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * Dashboard Widget Text, Boolean Type
 * Components used to register dashboard widgets
 *
 * @component none
 * @props type, data, attrId, options
 * @state score, value, customText, image
 */
export default {
  name: 'CardChart',
  props: {
    type: String,
    data: Object,
    options: Object
  },
  computed: {
    customTextStyle () {
      return {
        'font-size': this.customText.fontSize
      }
    },
  },
  watch: {
    // Update when changing the setting data value in the widget.
    data(arrayData) {
      if (this.type === 'custom_text') {
        this.customText = this.data;
        const {extention1, extention2} = this.data;
        this.customText = {
          text: extention1,
          fontSize: extention2,
        }
      } else if (this.type === 'Image') {
        const {url, file} = this.data;
        this.image = {url, file};
      } else {
        // Data settings delivered from the web socket.
        if (this.type === 'text') {
          this.score = arrayData.result.data[0].chartValue;
        } else {
          this.value = arrayData.result.data[0].chartValue;
        }
      }
    }
  },
  data() {
    return {
      score: 0,
      value: false,
      customText: {
        text: null,
        fontSize: null,
      },
      image: {
        url: null,
        file: null,
      },
    }
  },
  mounted() {
    // Since it's not a websocket, set it up right away.
    if (this.type === 'custom_text') {
      this.customText = this.data;
      const {extention1, extention2} = this.data;
      this.customText = {
        text: extention1,
        fontSize: extention2,
      }
    } else if (this.type === 'Image') {
      const {url, file} = this.data;
      this.image = {url, file};
    }
  }
}
</script>

<style scoped>
.card-title {
  font-size: 50px;
}
.card-category {
  color: #666666;
  padding-top: 16px;
  font-size: 12px;
}
.card-custom {
  height: 100%;
  width: 100%;
  padding: 16px;
}
.card-custom p:not(.card-category) {
  position: absolute;
  top: 50%;
  width: calc(100% - 30px);
  overflow: hidden;
  transform: translateY(-50%);
  word-break: break-all;
  max-height: 100%;
}
.card-custom p.card-category {
  padding-top: 0;
  width: calc(100% - 32px);
  position: fixed;
}
.card-image img {
  width: 100%;
  height: 100%;
  object-fit: fill;
}
.card-image {
  padding: 5px !important;
  width: 100%;
  height: 100%;
}
.card-image img {
  width: 100%;
  height: 100%;
}
.card-image p.card-category {
  position: fixed;
  width: 100%;
}
</style>
