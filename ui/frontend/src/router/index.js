/**
 * Vue Router (Common)
 * Client Router
 */
import Vue from 'vue';
import Router from 'vue-router';

import DataModelView from '@/views/datamodel/DataModelView';
import DatasetView from '@/views/dataset/DatasetView';
import AccessControlView from '@/views/accessControl/AccessControl';
import VerificationHistoryView from '@/views/verification/VerificationHistoryView';
import VerificationModifyView from '@/views/verification/VerificationModifyView';
import UserManageView from '@/views/system/UserManageView';
import MenuManageView from '@/views/system/MenuManageView';
import MenuRoleManageView from '@/views/system/MenuRoleManageView';

import DataModelModView from '@/views/datamodel/DataModelModView';

import DatasetModView from '@/views/dataset/DatasetModView';
import AccessControlModView from '@/views/accessControl/AccessControlModView';
import ProvisionServerView from '@/views/provision/ProvisionServerView';
import ProvisionServerModView from '@/views/provision/ProvisionServerModView';
import ExternalPlatformView from '@/views/platform/ExternalPlatformView';
import ExternalPlatformModView from '@/views/platform/ExternalPlatformModView';
import CodeManageView from '@/views/system/CodeManageView';

Vue.use(Router);

console.log(this);

export default new Router({
  mode: 'history', // vue router default hash mode ( so... change history mode )
  routes: [
    {
      path: '/',
      name: 'Login',
      component: null,
      meta: {
        breadcrumb: ['Home']
      }
    },
    {
      path: '*',
      component: {
        template: '<script>alert("잘못된 호출입니다.");</script>'
      }
    },
    {
      path: '/dataModels',
      name: 'DataModelView',
      component: DataModelView,
      meta: {
        breadcrumb: ['데이터 모델 관리']
      }
    },
    {
      path: '/dataModelModView',
      name: 'DataModelModView',
      component: DataModelModView,
      meta: {
        breadcrumb: ['데이터 모델 관리', '데이터 모델 상세']
      }
    },
    {
      path: '/datasetView',
      name: 'DatasetView',
      component: DatasetView,
      meta: {
        breadcrumb: ['데이터 셋 관리', '데이터 셋 정보']
      }
    },
    {
      path: '/datasetModView',
      name: 'DatasetModView',
      component: DatasetModView,
      meta: {
        breadcrumb: ['데이터 셋 관리', '데이터 셋 정보 상세']
      }
    },
    {
      path: '/accessControl',
      name: 'AccessControl',
      component: AccessControlView,
      meta: {
        breadcrumb: ['데이터 접근 제어 관리', '데이터 접근제어 관리 정보']
      }
    },
    {
      path: '/accessControlMod',
      name: 'AccessControlMod',
      component: AccessControlModView,
      meta: {
        breadcrumb: ['데이터 접근 제어 관리', '데이터 접근제어 관리 정보 상세']
      }
    },
    {
      path: '/provisionServerView',
      name: 'ProvisionServerView',
      component: ProvisionServerView,
      meta: {
        breadcrumb: ['Provision 서버 관리']
      }
    },
    {
      path: '/provisionServerModView',
      name: 'ProvisionServerModView',
      component: ProvisionServerModView,
      meta: {
        breadcrumb: ['Provision 서버 관리', 'Provision 서버 상세']
      }
    },
    {
      path: '/externalPlatformView',
      name: 'ExternalPlatformView',
      component: ExternalPlatformView,
      meta: {
        breadcrumb: ['외부 플랫폼 인증 관리']
      }
    },
    {
      path: '/externalPlatformModView',
      name: 'ExternalPlatformModView',
      component: ExternalPlatformModView,
      meta: {
        breadcrumb: ['외부 플랫폼 인증 관리', '외부 플랫폼 인증 관리 상세']
      }
    },
    {
      path: '/verificationHistoryView',
      name: 'VerificationHistoryView',
      component: VerificationHistoryView,
      meta: {
        breadcrumb: ['품질 모니터링']
      }
    },
    {
      path: '/verificationModify',
      name: 'VerificationModifyView',
      component: VerificationModifyView,
      props: (route) => ({ query: route.query.objData }),
      meta: {
        breadcrumb: ['품질 모니터링', '품질 모니터링 상세']
      }
    },
    {
      path: '/userManagement',
      name: 'UserManageView',
      component: UserManageView,
      meta: {
        breadcrumb: ['시스템 관리', '사용자 관리']
      }
    },
    {
      path: '/menuManageView',
      name: 'MenuManageView',
      component: MenuManageView,
      meta: {
        breadcrumb: ['시스템 관리', '메뉴 관리']
      }
    },
    {
      path: '/menuRoleManageView',
      name: 'MenuRoleManageView',
      component: MenuRoleManageView,
      meta: {
        breadcrumb: ['시스템 관리', '권한 관리']
      }
    },
    {
      path: '/codeManageView',
      name: 'CodeManageView',
      component: CodeManageView,
      meta: {
        breadcrumb: ['시스템 관리', '코드 관리']
      }
    }
  ]
});
