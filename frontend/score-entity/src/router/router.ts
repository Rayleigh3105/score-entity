import { createRouter, createWebHistory } from 'vue-router';
import Scoreboard from "@/components/Scoreboard.vue";
import AdminView from "@/components/admin/AdminView.vue";
import TeamList from "@/components/admin/team/TeamList.vue";
import Settings from "@/components/admin/settings/Settings.vue";
import ItemList from "@/components/admin/item/ItemList.vue";
import CentralView from "@/components/admin/central/CentralView.vue";
import BasicView from "@/components/basic/BasicView.vue";
import Calculator from "@/components/basic/Calculator.vue";

const routes = [
    {
        path: '/',
        name: 'Home',
        component: Scoreboard,
    },
    {
        path: '/scoreboard',
        name: 'Scoreboard',
        component: Scoreboard,
    },
    {
        path: '/calc',
        name: 'Calculator',
        component: Calculator,
    },

    {
        path: '/basic',
        name: 'Basis',
        component: BasicView,
        children: [
            {
                path: '/basic/central',
                name: 'Punkteverwaltung1',
                component: CentralView,
            },
            {
                path: '/basic/calculator',
                name: 'Rechner',
                component: Calculator,
            },
        ]
    },
    {
        path: '/admin',
        name: 'Admin',
        component: AdminView,
        children: [
            {
                path: '/admin/central',
                name: 'Punkteverwaltung',
                component: CentralView,
            },
            {
                path: '/admin/teams',
                name: 'Teams',
                component: TeamList,
            },
            {
                path: '/admin/settings',
                name: 'Einstellungen',
                component: Settings
            },
            {
                path: '/admin/items',
                name: 'Items',
                component: ItemList
            },
        ]
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;