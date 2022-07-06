/**
 * Api function callback
 */
import axios from 'axios';
import qs from 'qs';

const request = (method, url, data) => {
  return axios({
    method,
    url,
    data,
  }).then(response => response.data)
    .catch(response => {
      const {status} = response;
      // TODO : Code define
      // alert(response);
      throw Error(response);
    });
};

const urlWithQuery = (url, params) => {
  const query = qs.stringify(params);
  return url + (query ? `?${query}` : '');
}

// DASHBOARD
export const dashboardApi = {
  fetch() {
    return request('get', '/dashboard');
  },
  create(data) {
    return request('post', '/dashboard', data);
  },
  modify(data) {
    return request('put', '/dashboard', data);
  },
  delete(id) {
    return request('delete', `/dashboard/${id}`);
  },
}

export const widgetApi = {
  fetch(dashboardId, widgetId) {
    let url = urlWithQuery('/widgets', {dashboardId});
    if (widgetId) {
      url = urlWithQuery('/widget', {dashboardId, widgetId});
    }
    return request('get', url);
  },
  fetchImageUrl(dashboardId, widgetId) {
    return urlWithQuery('widget/image', {dashboardId, widgetId});
  },
  fetchImage(dashboardId, widgetId) {
    return request('get', this.fetchImageUrl(dashboardId, widgetId));
  },
  create(data, file) {
    if (file) {
      let formData = new FormData();
      formData.append('file', file);
      formData.append('widgetDashboardUI', JSON.stringify(data));
      return request('post', '/widget/file', formData);
    }
    return request('post', '/widget', data);
  },
  modify(data) {
    return request('put', '/widget', data);
  },
  delete(dashboardId, widgetId) {
    const url = urlWithQuery('/widget', {dashboardId, widgetId});
    return request('delete', url);
  },
  update(data) {
    return request('put', '/widget/layout', data);
  }
};

// DATA MODEL
export const dataModelApi = {
  fetch() {
    return request('get', '/datamodelIds');
  },
  attributes({dataModelId, typeUri}) {
    const query = dataModelId ? `id=${dataModelId}` : `typeUri=${typeUri}`;
    return request('get', `/datamodels/attrstree?${query}`);
  }
};

// DATA MODEL typeuri
export const typeUriApi = {
  fetch() {
    return request('get', '/datamodel/typeuri');
  }
};

// Latest Map list
export const latestApi = {
  fetch(conditionType) {
    return request('get', `/map/ids?mapSearchConditionType=${conditionType}`);
  },
  detail(conditionId) {
    return request('get', `/map?mapSearchConditionId=${conditionId}`);
  },
  create(params) {
    console.log(params);
    return request('post','/map', params);
  },
  modify(params) {
    console.log(params);
    return request('put','/map', params);
  },
  delete(id) {
    return request('delete',`/map/${id}`);
  },
};
