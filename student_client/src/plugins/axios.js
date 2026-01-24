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

_axios.interceptors.request.use(
  function(config) {
    // 从 sessionStorage 获取用户信息，设置到请求头
    const type = sessionStorage.getItem("type");
    const name = sessionStorage.getItem("name");
    
    // 如果是 admin，设置 Operator 请求头
    if (type === "admin" && name === "admin") {
      config.headers["Operator"] = "admin";
      config.headers["UserType"] = "admin";
    } else if ((type === "teacher" || type === "dean") && name) {
      config.headers["Operator"] = name;
      config.headers["UserType"] = type;
    } else if (type === "student" && name) {
      config.headers["Operator"] = name;
      config.headers["UserType"] = "student";
    }
    
    return config;
  },
  function(error) {
    // Do something with request error
    return Promise.reject(error);
  }
);

// Add a response interceptor
_axios.interceptors.response.use(
  function(response) {
    // Do something with response data
    return response;
  },
  function(error) {
    // Do something with response error
    return Promise.reject(error);
  }
);

Plugin.install = function(Vue, options) {
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
};

Vue.use(Plugin)

export default Plugin;
