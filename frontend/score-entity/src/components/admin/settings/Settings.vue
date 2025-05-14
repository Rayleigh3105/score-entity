<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useToast } from "primevue";
import Button from "primevue/button";
import Toast from "primevue/toast";
import Toolbar from "primevue/toolbar";
import Dialog from "primevue/dialog";
import Datepicker from 'primevue/datepicker';
import Textarea from 'primevue/textarea';
import { scoreApi, settingsApi } from "@/router/api.custom";

const toast = useToast();
const resetScoresDialog = ref(false);
const endTime = ref<Date | null>(null);
const price = ref<string>("");

const confirmResetScores = () => {
  resetScoresDialog.value = true;
};

const pad = (num: number): string => num.toString().padStart(2, '0');

const toLocalISOString = (date: Date) =>
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;


const saveSettings = async () => {
  if (!endTime.value) return;

  try {
    await settingsApi.settingsPut({ endTime: toLocalISOString(endTime.value), price: price.value, id: 1 });
    toast.add({
      severity: 'success',
      summary: 'Gespeichert',
      detail: 'Einstellungen gespeichert.',
      life: 3000,
    });
  } catch (error) {
    console.error("Fehler beim Speichern der Einstellung:", error);
    toast.add({
      severity: 'error',
      summary: 'Fehler',
      detail: 'Einstellungen konnten nicht gespeichert werden.',
      life: 3000,
    });
  }
};

const resetScores = async () => {
  try {
    const response = await scoreApi.scoresGet();
    if (response.status === 200) {
      toast.add({
        severity: 'success',
        summary: 'Erfolgreich',
        detail: 'Alle Punkte wurden zurückgesetzt.',
        life: 3000,
      });
      resetScoresDialog.value = false;
    }
  } catch (error) {
    console.error('Fehler beim Zurücksetzen der Punkte:', error);
    toast.add({
      severity: 'error',
      summary: 'Fehler',
      detail: 'Punkte konnten nicht zurückgesetzt werden.',
      life: 3000,
    });
  }
};

onMounted(async () => {
  try {
    const result = await settingsApi.settingsIdGet(1);
    endTime.value = new Date(result.data.endTime);
    price.value = result.data.price || "";
  } catch (error) {
    console.error("Fehler beim Laden der Einstellungen:", error);
  }
});
</script>

<template>
  <div class="card">
    <Toast/>

    <Toolbar class="mb-6">
      <template #start>
        <div class="flex flex-col gap-4 w-full">
          <div class="flex items-end gap-4">
            <div>
              <label for="endTime" class="block mb-1 font-medium">Endzeitpunkt</label>
              <Datepicker v-model="endTime" showTime hourFormat="24" id="endTime" :showIcon="true" date-format="dd.mm.yy"/>
            </div>
          </div>
          <div class="mt-2">
            <label for="price" class="block mb-1 font-medium">Preis (HTML-Markdown)</label>
            <Textarea id="price" v-model="price" autoResize rows="4" class="w-full" />
            <Button label="Speichern" icon="pi pi-save" severity="success" outlined class="self-end" @click="saveSettings"/>
          </div>

          <div>
            <Button label="Punkte zurücksetzen" icon="pi pi-refresh" severity="warning" outlined @click="confirmResetScores"/>
          </div>
        </div>
      </template>

    </Toolbar>

    <!-- Dialog hier korrekt außerhalb -->
    <Dialog v-model:visible="resetScoresDialog" :style="{ width: '450px' }" header="Bestätigung" :modal="true">
      <div class="flex items-center gap-4">
        <i class="pi pi-exclamation-triangle !text-3xl"/>
        <span>Möchtest du wirklich <b>alle Punkte</b> für alle Mitspieler zurücksetzen?</span>
      </div>
      <template #footer>
        <Button label="Nein" icon="bx bx-x" text @click="resetScoresDialog = false"/>
        <Button label="Ja" icon="bx bx-check" severity="warning" text @click="resetScores"/>
      </template>
    </Dialog>

  </div>

</template>

<style scoped>

</style>