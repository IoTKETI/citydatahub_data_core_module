import DashboardLayout from '../layout/DashboardLayout.vue';
import NotFound from '../pages/NotFoundPage.vue'
import Dashboard from '@/pages/Dashboard.vue';
import DataGrid from '@/pages/DataGrid.vue';
import DataMapFinal from '@/pages/DataMapFinal.vue';
import DataMapRecord from '@/pages/DataMapRecord.vue';

/**
 * Router settings.
 * - vue-router
 */

const routes = [
  {
    path: '/',
    component: DashboardLayout,
    redirect: '/mainDashboard'
  },
  {
    path: '/admin',
    component: DashboardLayout,
    redirect: '/mainDashboard',
    children: [
      {
        path: '/mainDashboard',
        name: 'Dashboard',
        component: Dashboard
      },
      {
        path: '/dataSearch',
        name: 'DataSearch',
        component: DataGrid
      },
      {
        path: '/mapSearchLatest',
        name: 'DataMapFinal',
        component: DataMapFinal
      },
      {
        path: '/mapSearchHistorical',
        name: 'DataMapRecord',
        component: DataMapRecord
      }
    ]
  },
  { path: '*', component: NotFound }
];

/**
 * Asynchronously load view (Webpack Lazy loading compatible)
 * The specified component must be inside the Views folder
 * @param  {string} name  the filename (basename) of the view to load.
function view(name) {
   var res= require('../components/Dashboard/Views/' + name + '.vue');
   return res;
};**/

export default routes
