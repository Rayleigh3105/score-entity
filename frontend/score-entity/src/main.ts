import './assets/main.css'

import {createApp} from 'vue'
import {createPinia} from 'pinia'

import router from './router/router'
import "./index.css";
import PrimeVue from 'primevue/config';
import {ToastService} from "primevue";
import App from "@/App.vue";

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(PrimeVue, {
    theme: 'none'
});
app.use(ToastService);


app.mount('#app')
