module.exports = {
  // 开发模式下配置
  devServer: {
    port: 8080,  // 本地开发端口
    open: true,  // 启动后自动打开浏览器
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:10086', // 本地后端 Spring Boot 地址
        changeOrigin: true,               // 必须，避免 Host 不匹配
        pathRewrite: { '^/api': '/api' }, // 保留 /api 前缀
        secure: false                     // 如果后端是 https，可改为 true
      }
    }
  }
};
