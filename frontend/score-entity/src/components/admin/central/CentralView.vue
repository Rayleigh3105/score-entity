<template>
  <div class="card">
    <Toast/>

    <DataTable
        ref="dt"
        :filters="filters"
        data-key="id"
        :value="groups"
        :paginator="true"
        :rows="10"
        :rowsPerPageOptions="[10, 25]"
        scrollable
        scroll-height="flex"
        selectionMode="single"
        :metaKeySelection="false"
        @rowSelect="onRowSelect"
    >
      <template #header>
        <div class="flex flex-wrap gap-2 items-center justify-between">
          <h4 class="m-0">Punkte Verwaltung</h4>
          <IconField>
            <InputIcon>
              <i class="bx bx-search"/>
            </InputIcon>
            <InputText v-model="filters['global'].value" placeholder="Suchen..."/>
          </IconField>
        </div>
      </template>
      <Column header="Bild" style="width: 3rem">
        <template #body="{ data }">
          <div class="flex items-center gap-2">
            <img v-if="data.image?.imageUrl"
                 alt="Gruppenbild"
                 :src="data.image?.imageUrl"
                 style="width: 32px"/>
            <img v-else
                 alt="Gruppenbild"
                 class="rounded-full"
                 src="https://res.cloudinary.com/drcmgtifj/image/upload/c_thumb,w_200,g_face/v1732888382/vecteezy_hand-drawnman-avatar-profile-icon-for-social-networks__cvzd71.jpg"
                 style="width: 32px"/>
          </div>
        </template>
      </Column>
      <Column field="name" header="Name"></Column>
    </DataTable>

    <template>
      <Dialog v-model:visible="scoreDialog" :style="{ width: '600px' }" header="Punkte" :modal="true">
        <div>
          <div style="text-align: center">
            <ToggleButton class="my-2 text-lg" v-model="checked" onLabel="Addieren" offLabel="Subtrahieren" on-icon="bx bx-plus" off-icon="bx bx-minus"/>
          </div>

          <div class="button-grid mt-6">
            <Button
                v-for="number in numbers"
                :key="number"
                :label="number.toString()"
                class="p-button-primary button-item"
                @click="doScore(number)"
            />
          </div>
          <div class="mt-4 text-center">
            <p class="text-sm mb-1">Oder eigene Punktzahl eingeben:</p>
            <InputNumber
                inputId="customPoints"
                class="w-full text-lg"
                :max="100000"
                v-model="score.points"
                mode="decimal"
            />

          </div>
        </div>
        <!-- Footer Buttons -->
        <template #footer>
          <Button
              label="Bestätigen"
              icon="pi pi-check"
              class="mt-2"
              @click="doScore(score.points ?? 0)"
          />
          <Button label="Abbrechen" icon="bx bx-x" text @click="hideDialog"/>
        </template>
      </Dialog>
    </template>

  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { FilterMatchMode } from '@primevue/core/api';
import {
  Configuration,
  type Group,
  GroupResourceApi, type Item,
  ItemResourceApi,
  ScoreResourceApi,
  type ScoreUpdateRequest
} from "@/api";

import Toast from 'primevue/toast';
import DataTable, { type DataTableRowSelectEvent } from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from 'primevue/inputtext';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import { useToast } from "primevue";
import Button from "primevue/button";
import Dialog from "primevue/dialog";
import ToggleButton from "primevue/togglebutton";
import InputNumber from 'primevue/inputnumber';
import { groupApi, scoreApi } from "@/router/api.custom";

const toast = useToast();
const filters = ref({
  'global': {value: null, matchMode: FilterMatchMode.CONTAINS},
});

const initialScoreUpdate: ScoreUpdateRequest = {
  groupId: undefined,
  points: undefined,
}
const checked = ref(true);
const scoreDialog = ref(false);
const score = ref<ScoreUpdateRequest>(initialScoreUpdate);

const groups = ref<Group[]>([]);
const isLoading = ref(false);
const error = ref<string | null>(null);
const numbers = ref<number[]>(Array.from({length: 20}, (_, i) => i + 1));


// Daten abrufen
const fetchGroups = async () => {
  isLoading.value = true;
  error.value = null;

  try {
    const response = await groupApi.groupsGet();
    groups.value = response.data;
  } catch (err) {
    error.value = 'Fehler beim Abrufen der Gruppen.';
    console.error(err);
  } finally {
    isLoading.value = false;
  }
};

const doScore = async (numberToScore: number) => {
  try {
    score.value.points = checked.value ? numberToScore : -numberToScore;
    await scoreApi.scoresPost(score.value);
    toast.add({severity: 'success', summary: 'Erfolg', detail: 'Punkte erfolgreich gespeichert.', life: 3000});
  } catch (err) {
    toast.add({severity: 'error', summary: 'Fehler', detail: 'Fehler beim Speichern der Punkte.', life: 3000});
    console.error(err);
  } finally {
    scoreDialog.value = false;
    score.value.points = undefined;
  }
};

const onRowSelect = (event: DataTableRowSelectEvent) => {
  scoreDialog.value = true;
  checked.value = true;
  score.value.groupId = event.data.id;
};

const onImageClick = (item: Item) => {
  doScore(item.scoreValue ?? 0);
};


const hideDialog = () => {
  scoreDialog.value = false;
};

onMounted(() => {
  fetchGroups();
});
</script>

<style scoped>
.button-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(60px, 1fr));
  gap: 14px;
  justify-content: center;
  align-items: center;
}

.button-item {
  width: 80px;
  height: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 22px;
}

/* Styling für die klickbare Liste */
.item-list {
  list-style: none;
  padding: 0;
  margin-top: 5%;
}

.item-list-entry {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  margin: 5px 0;
  border: 1px solid #ccc;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.item-list-entry:hover {
  background-color: #f0f0f0;
}

.item-header {
  display: flex;
  justify-content: space-between;
  width: 100%;
  font-weight: bold;
}
</style>