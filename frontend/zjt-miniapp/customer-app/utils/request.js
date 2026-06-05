const { BASE_URL } = require('./env')
const { getToken, removeToken } = require('./auth')

function request(options) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const header = {
      'Content-Type': 'application/json',
      ...(options.header || {})
    }
    if (token) {
      header.Authorization = 'Bearer ' + token
    }
    const tenantId = wx.getStorageSync('tenantId')
    if (tenantId) {
      header['X-Tenant-Id'] = tenantId
    }

    wx.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      success(res) {
        if (res.statusCode === 200) {
          const data = res.data
          if (data.code === 0) {
            resolve(data)
          } else {
            if (data.code === 40101 || data.code === 40102 || data.code === 40103) {
              removeToken()
              wx.redirectTo({ url: '/pages/login/index' })
              reject(new Error('未授权'))
              return
            }
            wx.showToast({ title: data.message || '请求失败', icon: 'none' })
            reject(new Error(data.message || '请求失败'))
          }
        } else if (res.statusCode === 401) {
          removeToken()
          wx.redirectTo({ url: '/pages/login/index' })
          reject(new Error('未授权'))
        } else {
          wx.showToast({ title: '请求失败', icon: 'none' })
          reject(new Error('请求失败'))
        }
      },
      fail() {
        wx.showToast({ title: '网络连接失败', icon: 'none' })
        reject(new Error('网络连接失败'))
      }
    })
  })
}

function get(url, data) {
  return request({ url, method: 'GET', data })
}

function post(url, data) {
  return request({ url, method: 'POST', data })
}

function put(url, data) {
  return request({ url, method: 'PUT', data })
}

function del(url, data) {
  return request({ url, method: 'DELETE', data })
}

module.exports = { request, get, post, put, del }
