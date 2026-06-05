const TOKEN_KEY = 'zjt_technician_token'

function getToken() {
  return wx.getStorageSync(TOKEN_KEY) || ''
}

function setToken(token) {
  wx.setStorageSync(TOKEN_KEY, token)
}

function removeToken() {
  wx.removeStorageSync(TOKEN_KEY)
}

function checkLogin() {
  return !!getToken()
}

module.exports = { getToken, setToken, removeToken, checkLogin }
