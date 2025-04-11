import { createRouter, createWebHistory } from 'vue-router';
import Scoreboard from "@/components/Scoreboard.vue";
import AdminView from "@/components/admin/AdminView.vue";
import TeamList from "@/components/admin/team/TeamList.vue";
import Settings from "@/components/admin/settings/Settings.vue";
import ItemList from "@/components/admin/item/ItemList.vue";
import CentralView from "@/components/admin/central/CentralView.vue";

// Routes ohne RouteRecordRaw typisieren
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
        path: '/admin',
        name: 'Admin',
        component: AdminView,
        children: [
            {
                path: '/admin/scoreboard',
                name: 'Scoreboard',
                component: Scoreboard,
            },
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
                path: '/admin/item',
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