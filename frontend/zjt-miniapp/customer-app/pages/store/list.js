const storeApi = require('../../api/store');

Page({
  data: {
    stores: [],
    loading: false,
    page: 1,
    pageSize: 10,
    hasMore: true,
    keyword: ''
  },

  onLoad() {
    this.loadStoreList();
  },

  onPullDownRefresh() {
    this.setData({
      stores: [],
      page: 1,
      hasMore: true,
      keyword: ''
    });
    this.loadStoreList().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadStoreList();
    }
  },

  loadStoreList() {
    if (this.data.loading) return Promise.resolve();

    this.setData({ loading: true });

    const params = {
      page: this.data.page,
      pageSize: this.data.pageSize
    };
    if (this.data.keyword) {
      params.keyword = this.data.keyword;
    }

    return storeApi.getStoreList(params).then(res => {
      const list = res.data.list || res.data || [];
      const newStores = this.data.stores.concat(list);
      this.setData({
        stores: newStores,
        page: this.data.page + 1,
        hasMore: list.length >= this.data.pageSize,
        loading: false
      });
    }).catch(() => {
      this.setData({ loading: false });
    });
  },

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value });
  },

  onSearch() {
    this.setData({
      stores: [],
      page: 1,
      hasMore: true
    });
    this.loadStoreList();
  },

  onStoreTap(e) {
    const storeId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/technician/list?storeId=${storeId}`
    });
  }
});
