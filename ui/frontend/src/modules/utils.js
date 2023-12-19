/**
 * Common Utils
 * data format, error Handel etc...
 */
import { addDays, setHours, setMilliseconds, setMinutes, setSeconds, format } from 'date-fns';

export const getDefaultDateRange = () => {
  let today = setMilliseconds(setSeconds(setMinutes(setHours(new Date(), 0), 0), 0), 0);

  return {
      startDate: addDays(today, 0),
      endDate: addDays(today, +1)
  };
};

export const dateFormat = (date, str) => {
    return format(date, str);
};

export const errorRender = (code, data) => {
    let result = { isAlertShow: false, message: null };
    switch (code) {
        case 400 :
            result.isAlertShow = true;
            if (data !== null && data.title !== null && data.detail) {
            	result.message = `${data.title} [${data.detail}]`;
            } else {
            	result.message = '잘못 된 요청입니다.';
            }
            break;
        case 409 :
            result.isAlertShow = true;
            if (data !== null && data.title !== null && data.detail) {
            	result.message = `${data.title} [${data.detail}]`;
            } else {
            	result.message = '중복된 데이터가 있습니다.';
            }
            break;
        case 503 :
            result.isAlertShow = true;
            if (data !== null && data.title !== null && data.detail) {
                result.message = `${data.title} [${data.detail}]`;
            } else {
                result.message = '서비스를 사용할 수 없습니다. 관리자에게 문의해 주세요.';
            }
            break;
        default :
            result.isAlertShow = true;
            if (data !== null && data.title !== null && data.detail) {
        		result.message = `${data.title} [${data.detail}]`;
        	} else {
        		result.message = '관리자에게 문의해 주세요.';
        	}
            break;
    }
    return result;
};

// Recursive function to find tree data.
export let traverse = (nodes, item, index) => {
    if (nodes instanceof Array) {
        return nodes.some(function (node) {
            if (node.id === item.upMenuId) {
                node.children = node.children || [];
                return node.children.push(item);
            }
            return traverse(node.children, item, index);
        });
    }
};