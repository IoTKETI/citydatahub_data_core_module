<template>
  <nav class="pagination">
    <ul class="pagination__list">
      <li class="pagination__item">
        <a class="pagination__link pagination__link--first" href="#none" @click="onFirstClick">
          <span class="hidden">처음으로</span>
        </a>
      </li>
      <li class="pagination__item">
        <a class="pagination__link pagination__link--prev" href="#none" @click="onPrevClick">
          <span class="hidden">이전</span>
        </a>
      </li>
      <li class="pagination__item" v-for="item in pageItems">
        <a
            :class="`pagination__link ${ item.active ? 'pagination__link--active' : null }`"
            href="#none" @click="() => onPageClick(item.page, item.value)"
        >
          {{ item.displayName }}
        </a>
      </li>
      <li class="pagination__item">
        <a class="pagination__link pagination__link--next" href="#none" @click="onNextClick">
          <span class="hidden">다음</span>
        </a>
      </li>
      <li class="pagination__item">
        <a class="pagination__link pagination__link--last" href="#none" @click="onLastClick">
          <span class="hidden">마지막으로</span></a>
      </li>
    </ul>
  </nav>
</template>

<script>
/**
 * Common Pagination component
 * @props totalCount, paginationValue, onClick(function), items(values)
 *
 */
  export default {
    name: 'AppPagination',
    props: {
      totalCount: Number,
      paginationValue: Number,
      onClick: Function,
      items: Array
    },
    data() {
      return {
        currentPage: 1,
        pageItems: null
      }
    },
    watch: {
      totalCount(val) {
        // props 의 변화 감시자
        // totalCount 가 변경되면 다시 로드한다.
        if (val === 0) {
          this.currentPage = 1;
        }
        this.makePageButton();
      }
    },
    methods: {
      onFirstClick() {
        this.currentPage = 1;
        const page = this.currentPage;
        const searchType = 'search'
        const limit = this.paginationValue;
        const offset = (this.currentPage - 1) * this.paginationValue;
        this.$emit('on-page-click', searchType, { page, limit, offset });
        this.makePageButton();
      },
      onLastClick() {
        const paginate = this.paginationValue;
        const lastPage = Math.ceil(this.totalCount / paginate);

        this.currentPage = lastPage;
        const page = this.currentPage;
        const searchType = 'search'
        const limit = this.paginationValue;
        const offset = (this.currentPage - 1) * this.paginationValue;
        this.$emit('on-page-click', searchType, { page, limit, offset });
        this.makePageButton();
      },
      onPrevClick() {
        if (this.currentPage > 1) {
          this.currentPage += -1;

          const page = this.currentPage;
          const searchType = 'search'
          const limit = this.paginationValue;
          const offset = (this.currentPage - 1) * this.paginationValue;
          this.$emit('on-page-click', searchType, { page, limit, offset });
          this.makePageButton();
        }
      },
      onNextClick() {
        const paginate = this.paginationValue;
        const lastPage = Math.ceil(this.totalCount / paginate);
        if (this.currentPage >= lastPage) {
          return;
        }
        this.currentPage += 1;

        const page = this.currentPage;
        const searchType = 'search'
        const limit = this.paginationValue;
        const offset = (this.currentPage - 1) * this.paginationValue;
        this.$emit('on-page-click', searchType, { page, limit, offset });
        this.makePageButton();
      },
      onPageClick(page, value) {
        const searchType = 'search'
        const limit = this.paginationValue;
        const offset = (page - 1) * this.paginationValue;
        this.currentPage = page;
        this.$emit('on-page-click', searchType, { page, limit, offset });
        this.makePageButton(page);
      },
      getDisabledState(isFront) {
        const paginate = this.paginationValue;
        const lastPage = Math.ceil(this.totalCount / paginate);
        if (isFront) {
          if (this.currentPage === 1) {
            return true;
          }
        } else if (!isFront) {
          if (this.currentPage === lastPage) {
            return true;
          }
        }
        return false;
      },
      makePageButton(page) {
        const element = [];
        if (page) {
          let activeItem = [ ...this.pageItems ];
          activeItem.map(item => {
            if (item.active) {
              item.active = false;
            }
            if (item.page === page) {
              item.active = true;
            }
          });
          return null;
        }
        const paginate = this.paginationValue;
        const lastPage = Math.ceil(this.totalCount / paginate);
        let startPage = 1;
        let endPage = lastPage;

        if (this.currentPage < 5) {
          if (lastPage > 5) {
            endPage = 5;
          }
        } else if (lastPage - this.currentPage >= 5) {
          startPage = this.currentPage;
          endPage = startPage + 4;
        } else {
          startPage = lastPage - 4;
        }
        for (let i = startPage; i <= endPage; i += 1) {
          element.push({
            element: 'li', key: `li-${i}`, active: this.currentPage === i ? true : false,
            displayName: i, page: i, value: 'search'
          });
          this.pageItems = element;
        }
      }
    },
    mounted() {
      this.makePageButton();
    }
  }
</script>

<style scoped>

</style>