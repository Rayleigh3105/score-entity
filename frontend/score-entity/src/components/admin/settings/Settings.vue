<script setup lang="ts">
import { Configuration, ScoreResourceApi } from "@/api";
import { ref } from 'vue';
import { useToast } from "primevue";
import Button from "primevue/button";
import Toast from "primevue/toast";
import Toolbar from "primevue/toolbar";
import Dialog from "primevue/dialog";

const toast = useToast();

const config = new Configuration({
  basePath: 'http://localhost:8080',
});

const resetScoresDialog = ref(false);

const confirmResetScores = () => {
  resetScoresDialog.value = true;
};

const resetScores = async () => {
  try {
    const scoreApi = new ScoreResourceApi(config);
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
</script>

<template>
  <div class="card">
    <Toast/>

    <Toolbar class="mb-6">
      <template #start>
        <Button label="Punkte zurücksetzen" icon="pi pi-refresh" severity="warning" outlined @click="confirmResetScores"/>
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