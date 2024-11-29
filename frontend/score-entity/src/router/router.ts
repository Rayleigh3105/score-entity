import {createRouter, createWebHistory} from 'vue-router';
import Scoreboard from "@/components/Scoreboard.vue";
import AdminView from "@/components/admin/AdminView.vue";
import TeamList from "@/components/admin/team/TeamList.vue";
import EventList from "@/components/admin/event/EventList.vue";
import Settings from "@/components/admin/settings/Settings.vue";
import EntityList from "@/components/admin/entity/EntityList.vue";

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
                path: '/admin/teams',
                name: 'Teams',
                component: TeamList,
            },
            {
                path: '/admin/event',
                name: 'Events',
                component: EventList
            },
            {
                path: '/admin/settings',
                name: 'Einstellungen',
                component: Settings
            },
            {
                path: '/admin/entity',
                name: 'Entitäten',
                component: EntityList
            },
        ]
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;