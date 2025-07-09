import {AreaResourceApi, Configuration, DepositResourceApi, GroupResourceApi, ItemResourceApi, ScoreResourceApi, SettingsResourceApi} from "@/api";

export const backendUrl = `/api`;

const config = new Configuration({
    basePath: backendUrl,
});

export const settingsApi = new SettingsResourceApi(config);
export const groupApi = new GroupResourceApi(config);
export const scoreApi = new ScoreResourceApi(config);
export const itemApi = new ItemResourceApi(config);
export const areaApi = new AreaResourceApi(config);
export const depositApi = new DepositResourceApi(config);