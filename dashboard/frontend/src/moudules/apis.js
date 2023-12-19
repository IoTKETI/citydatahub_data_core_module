/**
 * Api function callback
 */
import axios from 'axios';
import qs from 'qs';

const request = async (method, url, data) => {
  try {
    const response = await axios({
      method,
      url,
      data,
    });

    // console.info("HTTP: " + response.status);
    if (response.status >= 200 && response.status <= 299) {
      return response.data;
    } else {
      switch (response.status) {
        case 400:
          throw new Error("잘못된 요청입니다. (Bad Request)");
        case 401:
          throw new Error("인증이 필요합니다. (Unauthorized)");
        case 403:
          throw new Error("접근이 금지되었습니다. (Forbidden)");
        case 404:
          throw new Error("요청한 리소스를 찾을 수 없습니다. (Not Found)");
        case 500:
          throw new Error("서버 내부 오류가 발생했습니다. (Internal Server Error)");
        default:
          throw new Error("HTTP 요청 실패: " + response.status + " (Unknown Error)");
      }
    }
  } catch (error) {
    console.error("HTTP 요청 실패: " + error.message + " (Request Failed)");
    alert("HTTP 요청 실패: " + error.message + " (Request Failed)");
    throw error;
  }
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
  query({dataModelId, dataModelType}) {
    const query = dataModelId ? `id=${dataModelId}` : `type=${dataModelType}`;
    return request('get', `/datamodels?${query}`);
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
