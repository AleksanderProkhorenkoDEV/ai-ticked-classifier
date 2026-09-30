import { RouteDefinition } from '@open-cells/core/types'; 

export const routes: RouteDefinition[] = [
  {
    path: '/',
    name: 'home',
    component: 'home-page',
    action: async () => {
      await import('../pages/home/home-page.ts');
    },
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: 'dashboard-page',
    action: async () => {
      await import('../pages/dashboard/dashboard-page.ts');
    }
  }
];
