const ENV = 'dev' // dev/test/prod

const BASE_URL_MAP = {
  dev: 'http://localhost:8080',
  test: 'https://test-api.zhongjitang.com',
  prod: 'https://api.zhongjitang.com'
}

module.exports = { ENV, BASE_URL: BASE_URL_MAP[ENV] }
