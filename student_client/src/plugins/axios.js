"use strict";

import Vue from 'vue';
import axios from "axios";

// Full config:  https://github.com/axios/axios#request-config
// axios.defaults.baseURL = process.env.baseURL || process.env.apiUrl || '';
// axios.defaults.headers.common['Authorization'] = AUTH_TOKEN;
// axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';

let config = {
	baseURL: '/api'
  // baseURL: process.env.baseURL || process.env.apiUrl || ""
  // timeout: 60 * 1000, // Timeout
  // withCredentials: true, // Check cross-site Access-Control
};

const _axios = axios.create(config);

// 强制将 axios 挂载到 window，确保全局可用
window.axios = _axios;

_axios.interceptors.request.use(
  function(config) {
    console.log(`[Axios Request] ${config.method.toUpperCase()} ${config.url}`, config.params || config.data || '');
    // 从 sessionStorage 获取用户信息，设置到请求头
    const type = sessionStorage.getItem("type");
    const name = sessionStorage.getItem("name");
    
    // 根据 type 设置请求头，不再强制要求 name 必须是 "admin"
    if (type && name) {
      config.headers["Operator"] = encodeURIComponent(name);
      config.headers["UserType"] = type;
    }
    
    return config;
  },
  function(error) {
    console.error('[Axios Request Error]', error);
    return Promise.reject(error);
  }
);

// Add a response interceptor
_axios.interceptors.response.use(
  function(response) {
    console.log(`[Axios Response] ${response.config.url}`, response.data);
    return response;
  },
  function(error) {
    console.error(`[Axios Response Error] ${error.config ? error.config.url : 'Unknown URL'}`, error.response ? error.response.data : error.message);
    return Promise.reject(error);
  }
);

const Plugin = {
  install(Vue, options) {
    Vue.axios = _axios;
    window.axios = _axios;
    Object.defineProperties(Vue.prototype, {
      axios: {
        get() {
          return _axios;
        }
      },
      $axios: {
        get() {
          return _axios;
        }
      },
    });
  }
};

Vue.use(Plugin)

export default Plugin;
