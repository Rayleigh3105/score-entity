<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useToast } from "primevue";
import Button from "primevue/button";
import Toast from "primevue/toast";
import Toolbar from "primevue/toolbar";
import Dialog from "primevue/dialog";
import Datepicker from 'primevue/datepicker';
import Textarea from 'primevue/textarea';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import InputNumber from 'primevue/inputnumber';
import InputText from 'primevue/inputtext';
import Divider from 'primevue/divider';
import { scoreApi, settingsApi, areaApi, depositApi } from "@/router/api.custom";
import type { Area, Deposit } from "@/api";

const toast = useToast();
const resetScoresDialog = ref(false);
const endTime = ref<Date | null>(null);
const price = ref<string>("");

const areas = ref<Area[]>([]);
const areaDialog = ref(false);
const area = ref<Area>({ id: null, name: '' });
const submittedArea = ref(false);

const deleteAreaDialog = ref(false);

const confirmDeleteArea = (a: Area) => {
  area.value = { ...a };
  deleteAreaDialog.value = true;
};

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

const fetchAreas = async () => {
  try {
    const res = await areaApi.areasGet();
    areas.value = res.data;
  } catch (e) {
    console.error("Fehler beim Laden der Bereiche:", e);
  }
};
onMounted(fetchAreas);

const openNewArea = () => {
  area.value = { id: null, name: '' };
  submittedArea.value = false;
  areaDialog.value = true;
};

const editArea = (a: Area) => {
  area.value = { ...a };
  submittedArea.value = false;
  areaDialog.value = true;
};

const saveArea = async () => {
  submittedArea.value = true;
  if (!area.value.name) return;

  try {
    if (area.value.id) {
      await areaApi.areasIdPut(area.value.id, area.value);
    } else {
      await areaApi.areasPost(area.value);
    }
    toast.add({ severity: 'success', summary: 'Erfolg', detail: 'Bereich gespeichert.', life: 3000 });
    areaDialog.value = false;
    await fetchAreas();
  } catch (e) {
    console.error("Fehler beim Speichern der Bereiche:", e);
    toast.add({ severity: 'error', summary: 'Fehler', detail: 'Speichern fehlgeschlagen.', life: 3000 });
  }
};

const deleteArea = async (a: Area) => {
  try {
    await areaApi.areasIdDelete(a.id as number);
    toast.add({ severity: 'success', summary: 'Erfolg', detail: 'Bereich gelöscht.', life: 3000 });
    deleteAreaDialog.value = false;
    await fetchAreas();
  } catch (e) {
    console.error("Fehler beim Löschen der Bereiche:", e);
    toast.add({ severity: 'error', summary: 'Fehler', detail: 'Löschen fehlgeschlagen.', life: 3000 });
  }
};

const confirmResetScores = () => {
  resetScoresDialog.value = true;
};

const pad = (num: number): string => num.toString().padStart(2, '0');

const toLocalISOString = (date: Date) =>
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;

const deposits = ref<Deposit[]>([]);
const depositDialog = ref(false);
const deposit = ref<Deposit>({ id: null, value: 0 });
const submittedDeposit = ref(false);
const deleteDepositDialog = ref(false);

const fetchDeposits = async () => {
  try {
    const res = await depositApi.depositsGet();
    deposits.value = res.data;
  } catch (e) {
    console.error("Fehler beim Laden der Pfandarten:", e);
  }
};
onMounted(fetchDeposits);

const openNewDeposit = () => {
  deposit.value = { id: null, value: 0 };
  submittedDeposit.value = false;
  depositDialog.value = true;
};

const editDeposit = (d: Deposit) => {
  deposit.value = { ...d };
  submittedDeposit.value = false;
  depositDialog.value = true;
};

const saveDeposit = async () => {
  submittedDeposit.value = true;

  try {
    if (deposit.value.id) {
      await depositApi.depositsIdPut(deposit.value.id, deposit.value);
    } else {
      await depositApi.depositsPost(deposit.value);
    }
    toast.add({ severity: 'success', summary: 'Erfolg', detail: 'Pfand gespeichert.', life: 3000 });
    depositDialog.value = false;
    await fetchDeposits();
  } catch (e) {
    console.error("Fehler beim Speichern des Pfands:", e);
    toast.add({ severity: 'error', summary: 'Fehler', detail: 'Speichern fehlgeschlagen.', life: 3000 });
  }
};

const confirmDeleteDeposit = (d: Deposit) => {
  deposit.value = { ...d };
  deleteDepositDialog.value = true;
};

const deleteDeposit = async (d: Deposit) => {
  try {
    await depositApi.depositsIdDelete(d.id as number);
    toast.add({ severity: 'success', summary: 'Erfolg', detail: 'Pfand gelöscht.', life: 3000 });
    deleteDepositDialog.value = false;
    await fetchDeposits();
  } catch (e) {
    console.error("Fehler beim Löschen des Pfands:", e);
    toast.add({ severity: 'error', summary: 'Fehler', detail: 'Löschen fehlgeschlagen.', life: 3000 });
  }
};
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

    <Divider class="my-6" />
    <h2 class="text-xl font-bold mb-4">Bereiche</h2>
    <Button label="Bereich hinzufügen" icon="pi pi-plus" class="mb-4 mr-2" @click="openNewArea" />

    <DataTable :value="areas" dataKey="id" responsiveLayout="scroll">
      <Column field="id" header="ID" style="width: 4rem" />
      <Column field="name" header="Name" />
      <Column header="Aktionen" style="width: 8rem">
        <template #body="{ data }">
          <Button icon="bx bx-pencil" class="mr-2" text @click="editArea(data)" />
          <Button icon="bx bx-trash" severity="danger" text @click="confirmDeleteArea(data)" />
        </template>
      </Column>
    </DataTable>

    <Dialog v-model:visible="areaDialog" header="Bereich" modal :style="{ width: '400px' }">
      <div class="field">
        <label for="areaName" class="block font-bold mb-2">Name</label>
        <InputText
          id="areaName"
          v-model.trim="area.name"
          :class="{ 'p-invalid': submittedArea && !area.name }"
          autofocus
        />
        <small v-if="submittedArea && !area.name" class="p-error">Name ist erforderlich.</small>
      </div>
      <template #footer>
        <Button label="Abbrechen" icon="pi pi-times" text @click="areaDialog = false" />
        <Button label="Speichern" icon="pi pi-check" text @click="saveArea" />
      </template>
    </Dialog>
    <Dialog v-model:visible="deleteAreaDialog" header="Bestätigung" modal :style="{ width: '450px' }">
      <div class="flex items-center gap-4">
        <i class="pi pi-exclamation-triangle !text-3xl"/>
        <span>Willst du wirklich den Bereich <b>{{ area.name }}</b> löschen?</span>
      </div>
      <template #footer>
        <Button label="Nein" icon="pi pi-times" text @click="deleteAreaDialog = false"/>
        <Button label="Ja" icon="pi pi-check" severity="danger" text @click="deleteArea(area)" />
      </template>
    </Dialog>

    <Divider class="my-6" />
    <h2 class="text-xl font-bold mb-4">Pfand</h2>
    <Button label="Pfand hinzufügen" icon="pi pi-plus" class="mb-4 mr-2" @click="openNewDeposit" />

    <DataTable :value="deposits" dataKey="id" responsiveLayout="scroll">
      <Column field="id" header="ID" style="width: 4rem" />
      <Column field="value" header="Wert (€)">
        <template #body="{ data }">
          {{ data.value.toFixed(2).replace('.', ',') }}
        </template>
      </Column>
      <Column header="Aktionen" style="width: 8rem">
        <template #body="{ data }">
          <Button icon="bx bx-pencil" class="mr-2" text @click="editDeposit(data)" />
          <Button icon="bx bx-trash" text severity="danger" @click="confirmDeleteDeposit(data)" />
        </template>
      </Column>
    </DataTable>

    <Dialog v-model:visible="depositDialog" header="Pfand" modal :style="{ width: '400px' }">
      <div class="field">
        <label for="depositValue" class="block font-bold mb-2">Wert (€)</label>
        <InputNumber
            id="depositValue"
            v-model.number="deposit.value"
            :class="{ 'p-invalid': submittedDeposit && !deposit.value }"
            mode="decimal"
            :minFractionDigits="2"
            placeholder="0,00"
        />
        <small v-if="submittedDeposit && !deposit.value" class="p-error">Wert ist erforderlich.</small>
      </div>
      <template #footer>
        <Button label="Abbrechen" icon="pi pi-times" text @click="depositDialog = false" />
        <Button label="Speichern" icon="pi pi-check" text @click="saveDeposit" />
      </template>
    </Dialog>
    <Dialog v-model:visible="deleteDepositDialog" header="Bestätigung" modal :style="{ width: '450px' }">
      <div class="flex items-center gap-4">
        <i class="pi pi-exclamation-triangle !text-3xl"/>
        <span>Willst du wirklich das Pfand <b>{{ deposit.value }}</b> löschen?</span>
      </div>
      <template #footer>
        <Button label="Nein" icon="pi pi-times" text @click="deleteDepositDialog = false"/>
        <Button label="Ja" icon="pi pi-check" severity="danger" text @click="deleteDeposit(deposit)" />
      </template>
    </Dialog>
  </div>

</template>

<style scoped>

</style>