import { RouteDefinition } from '@open-cells/core/types'; 

export const routes: RouteDefinition[] = [
  {
    path: '/',
    name: 'Home',
    component: 'home-page',
    action: async () => {
      await import('../pages/home/home-page.ts');
    },
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: 'dashboard-page',
    action: async () => {
      await import('../pages/dashboard/dashboard-page.ts');
    }
  },
  {
    path: '/create-ticket',
    name: 'Create Ticket',
    component: "create-ticket-page",
    action: async () => {
      await import('../pages/create-ticket/create-ticket.ts');
    }
  }
];
