import { createRouter, createWebHistory } from 'vue-router';
import Scoreboard from "@/components/Scoreboard.vue";

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
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;