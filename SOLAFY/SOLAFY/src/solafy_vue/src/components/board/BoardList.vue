<template>
  <div>
    <!-- 게시판 list 시작 -->
    <q-markup-table>
      <!-- 헤더 부분 시작 -->
      <thead>
        <!-- 글쓰기버튼, 검색옵션, 검색어입력 부분 시작 -->
        <tr>
          <td colspan="5" style="width:100%">
            <!-- 검색 옵션 시작 -->
            <q-input
              bottom-slots
              v-model="keyword"
              label="검색어를 입력해주세요"
              counter
              maxlength="12"
              @keyup.enter="selectArticle"
            >
              <!-- 검색 옵션 끝 -->

              <!-- 글쓰기 버튼 시작 (검색 옵션의 앞에 달려서 v-slot:before) -->
              <template v-slot:before>
                <q-btn
                  color="secondary"
                  label="글쓰기"
                  @click="goToBoardWrite"
                />
                <q-select
                  style="margin-left:300px; width:120px"
                  filled
                  v-model="selection"
                  :options="boardSearchKeywords"
                  label="검색옵션"
                />
              </template>
              <!-- 글쓰기 버튼 끝 -->

              <!-- 검색어입력칸 시작(검색 옵션에 이어지므로 v-slot:append) -->
              <template v-slot:append>
                <q-icon
                  v-if="keyword !== ''"
                  name="close"
                  @click="keyword = ''"
                />
                <q-icon name="search" @click="selectArticle" />
              </template>
              <!-- 검색어 입력칸 끝 -->

              <!-- 검색 초기화 버튼 시작 -->
              <template v-slot:after>
                <q-btn
                  outline
                  color="black"
                  label="reset"
                  flat
                  @click="resetSearchKeyword"
                />
              </template>
              <!-- 검색 초기화 버튼 끝 -->
            </q-input>
            <!-- 검색옵션 끝 -->
          </td>
        </tr>
        <tr>
          <!-- vuex에 저장되어있는 boardColumns의 요소를 이용하여 컬럼명 표시
                 (boardColumns : FreeBoard.vue나 AnswerModify.vue에서 vuex에 저장한 값)      -->
          <th
            v-for="(column, index) in boardColumns"
            class="text-left"
            :key="index"
          >
            {{ column.label }}
          </th>
        </tr>
        <!-- 컬럼 표시 끝 -->
      </thead>
      <tbody>
        <!-- 공지사항 출력 시작(자유게시판 한정)
          타 게시판은 notices에 값이 들어있지 않으므로 패스, 언제든지 기능추가 가능-->
        <tr
          v-for="notice in notices"
          :key="notice.articleNo"
          @click="goToBoardDetail(notice.articleNo)"
          style="background-color:#DDD"
        >
          <td class="text-left">{{ notice.articleNo }}</td>
          <td class="text-left">{{ notice.nickname }}</td>
          <td class="text-left">
            <q-chip dense color="orange" text-color="white">
              공지
            </q-chip>
            {{ notice.title }}
          </td>
          <td class="text-left">{{ notice.regiTime }}</td>
          <td class="text-left">{{ notice.likeCount }}</td>
        </tr>
        <!-- 공지사항 출력 끝 -->

        <!-- TODO: 컬럼의 순서와 DTO의 순서가 맞아야한다!, v-for를 이용하여 출력하기때문이다
                      DTO의 필드 배치 순서에 의존적이라 수정할건지? -->

        <!-- 게시들을 출력
        각 tr(Row)를 클릭하면 goToBoardDetail함수가 실행 -->
        <tr
          v-for="article in articles"
          :key="article.articleNo"
          @click="goToBoardDetail(article.articleNo)"
        >
          <!-- 각 줄의 요소를 순차적으로 출력
          ex) 46	호랑돌이	(공지)보드 마무리입니다	2021-01-01 13:55:54	0 -->
          <td v-for="(name, value, index) in article" :key="index">
            {{ name }}
          </td>
          <!-- 각 줄의 요소 출력 끝 -->
        </tr>
        <!-- 게시글 출력 끝 -->
      </tbody>
    </q-markup-table>
  </div>
</template>
<script>
import Axios from "axios";

// vuex state에 저장된 값 불러오기
import { mapState } from "vuex";

export default {
  name: "boardlist",
  data() {
    return {
      // 검색 옵션 값 저장 변수
      selection: "제목",

      // 검색 키워드 저장 변수
      keyword: null,

      // 받아온 데이터나 공지글 저장 변수
      articles: [],
      notices: [],

      // error나 loading 상태 저장 변수
      errored: null,
      loading: null
    };
  },
  methods: {
    /**
     * @Method설명 : 전체 게시글 목록을 가져오는 메서드
     * @변경이력 :
     */
    selectAllArticles: function() {
      /**
       * ${this.boardType} 와 같이 현재 보는 게시판의 형식을 vuex에 기억하여서
       * api를 부를때 변화에 대응할 수 있게 한다.
       */
      Axios.get(`/${this.boardType}/selectAllArticles`)
        .then(response => {
          this.articles = response.data;

          /**
           * 전체 DTO(ex- FreeBoardDto)에서 value값이 null이거나 null값에 준하는 컬럼이 있다면
           * 현재 불러온 객체에서 해당 컬럼을 지워준다
           *
           * TODO: likeCount는 데이터로 0의 값을 가질 수 있어서 예외로 둠
           */
          for (var i = 0; i < this.articles.length; i++) {
            for (var propName in this.articles[i]) {
              if (
                this.articles[i][propName] === null ||
                this.articles[i][propName] === undefined ||
                this.articles[i][propName] === false ||
                (propName !== "likeCount" &&
                  this.articles[i][propName] === 0) ||
                propName === "uid"
              ) {
                delete this.articles[i][propName];
              }
            }
          }
          //빈 Column지우기 끝

          if (this.boardType === "free") this.selectAllNotices();
        })
        .catch(() => {
          this.errored = true;
        })
        .finally(() => (this.loading = false));
    },

    /**
     * @Method설명 : 전체 공지사항 게시글 가져오기
     * @변경이력 :
     */
    selectAllNotices: function() {
      Axios.get(`/${this.boardType}/selectAllNotices`)
        .then(response => {
          this.notices = response.data;
        })
        .catch(() => {
          this.errored = true;
        })
        .finally(() => (this.loading = false));
    },

    /**
     * @Method설명 : 검색 조건, 키워드에 따른 검색
     * @변경이력 :
     */
    selectArticle: function() {
      // 검색 옵션이 제목일때
      if (this.selection === "제목") {
        Axios.get(`/${this.boardType}/selectArticleByTitle/${this.keyword}`)
          .then(response => {
            this.articles = response.data;

            // (중복) 빈값들어있는 컬럼 지우기
            for (var i = 0; i < this.articles.length; i++) {
              for (var propName in this.articles[i]) {
                if (
                  this.articles[i][propName] === null ||
                  this.articles[i][propName] === undefined ||
                  this.articles[i][propName] === false ||
                  (propName !== "likeCount" &&
                    this.articles[i][propName] === 0) ||
                  propName === "uid"
                ) {
                  delete this.articles[i][propName];
                }
              }
            }
            // (중복) 빈값들어있는 컬럽 지우기 완료
          })
          .catch(() => {
            this.errored = true;
          })
          .finally(() => (this.loading = false));

        // 이하 동일
      } else if (this.selection === "작성자") {
        Axios.get(`/${this.boardType}/selectArticleByNickname/${this.keyword}`)
          .then(response => {
            this.articles = response.data;
            for (var i = 0; i < this.articles.length; i++) {
              for (var propName in this.articles[i]) {
                if (
                  this.articles[i][propName] === null ||
                  this.articles[i][propName] === undefined ||
                  this.articles[i][propName] === false ||
                  (propName !== "likeCount" &&
                    this.articles[i][propName] === 0) ||
                  propName === "uid"
                ) {
                  delete this.articles[i][propName];
                }
              }
            }
          })
          .catch(() => {
            this.errored = true;
          })
          .finally(() => (this.loading = false));
        // 문제번호가 검색옵션에 있는 게시판 한정으로 문제번호 검색기능
      } else if (this.selection === "문제번호") {
        Axios.get(`/${this.boardType}/selectArticleByProblemNo/${this.keyword}`)
          .then(response => {
            this.articles = response.data;
            for (var i = 0; i < this.articles.length; i++) {
              for (var propName in this.articles[i]) {
                console.log(propName);
                if (
                  this.articles[i][propName] === null ||
                  this.articles[i][propName] === undefined ||
                  this.articles[i][propName] === false ||
                  (propName !== "likeCount" &&
                    this.articles[i][propName] === 0) ||
                  propName === "uid"
                ) {
                  delete this.articles[i][propName];
                }
              }
            }
          })
          .catch(() => {
            this.errored = true;
          })
          .finally(() => (this.loading = false));
      }
    },

    /**
     * @Method설명 : 글쓰기 페이지로 이동하는 메소드
     * @변경이력 :
     */
    goToBoardWrite: function() {
      // (해당 보드)-board-wirte의 이름을 가진 라우트로 이동한다
      this.$router.push({
        name: `${this.boardType}-board-write`
      });
    },

    /**
     * @Method설명 : 검색 조건, 키워드에 따른 검색
     * @변경이력 :
     */
    goToBoardDetail: function(articleNo) {
      this.$router.push({
        name: `${this.boardType}-board-detail`,
        params: { articleNo: articleNo }
      });
    },

    /**
     * @Method설명 : 검색조건 초기화 메소드
     * @변경이력 :
     */
    resetSearchKeyword: function() {
      this.keyword = null;
      this.selectAllArticles();
    }
  },

  // mapState : vuex의 state(변수)를 가져다 쓰고 싶을 때
  computed: {
    ...mapState(["boardColumns", "boardSearchKeywords", "boardType"])
  },
  created() {
    this.selectAllArticles();
  }
};
</script>
