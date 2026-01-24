import Vue from 'vue'
import './plugins/axios'
import App from './App.vue'
import router from './router'
import store from './store'
import './plugins/element.js'

Vue.config.productionTip = false

console.error('===== 考务系统前端已启动 =====');
console.log('当前后端 API 地址:', '/api');

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
