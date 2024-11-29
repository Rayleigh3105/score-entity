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
      <Dialog v-model:visible="scoreDialog" :style="{ width: '450px' }" header="Punkte" :modal="true">
        <div>
          <div class="button-grid">
            <Button
                v-for="number in numbers"
                :key="number"
                :label="number.toString()"
                class="p-button-rounded p-button-primary button-item"
                @click="doScore(number)"
            />
          </div>

          <!-- Klickbare Liste der Items -->
          <ul class="item-list">
            <li
                v-for="item in items"
                class="item-list-entry"
                @click="doScore(item.scoreValue ?? 0)"
            >
              <div class="item-header">
                <span class="item-name">{{ item.name }}</span>
                <span class="item-score">{{ item.scoreValue ?? 0 }}</span>
              </div>
            </li>
          </ul>

        </div>
        <!-- Footer Buttons -->
        <template #footer>
          <Button label="Abbrechen" icon="bx bx-x" text @click="hideDialog"/>
        </template>
      </Dialog>
    </template>

  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {FilterMatchMode} from '@primevue/core/api';
import {
  Configuration,
  type Group,
  GroupResourceApi, type Item,
  ItemResourceApi,
  ScoreResourceApi,
  type ScoreUpdateRequest
} from "@/api";

import Toast from 'primevue/toast';
import DataTable, {type DataTableRowSelectEvent} from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from 'primevue/inputtext';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import {useToast} from "primevue";
import Button from "primevue/button";
import Dialog from "primevue/dialog";

const toast = useToast();
const filters = ref({
  'global': {value: null, matchMode: FilterMatchMode.CONTAINS},
});

const initialScoreUpdate: ScoreUpdateRequest = {
  groupId: undefined,
  points: undefined,
}
const scoreDialog = ref(false);
const score = ref<ScoreUpdateRequest>(initialScoreUpdate);

const groups = ref<Group[]>([]);
const items = ref<Item[]>([]);
const isLoading = ref(false);
const error = ref<string | null>(null);
const config = new Configuration({
  basePath: 'http://localhost:8080',
});
const numbers = ref<number[]>(Array.from({length: 15}, (_, i) => i + 1));


// API-Instanz erstellen
const groupApi = new GroupResourceApi(config);
const scoreApi = new ScoreResourceApi(config);
const itemApi = new ItemResourceApi(config);

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

const fetchItems = async () => {
  try {
    const response = await itemApi.itemsGet();
    items.value = response.data;
  } catch (err) {
    error.value = 'Fehler beim Abrufen der Gruppen.';
    console.error(err);
  }
};

const doScore = async (numberToScore: number) => {
  try {
    score.value.points = numberToScore;
    await scoreApi.scoresPost(score.value);
    toast.add({severity: 'success', summary: 'Erfolg', detail: 'Punkte erfolgreich gespeichert.', life: 3000});
  } catch (err) {
    toast.add({severity: 'error', summary: 'Fehler', detail: 'Fehler beim Speichern der Punkte.', life: 3000});
    console.error(err);
  } finally {
    scoreDialog.value = false;
  }
};

const onRowSelect = (event: DataTableRowSelectEvent) => {
  scoreDialog.value = true;
  score.value.groupId = event.data.id;

  fetchItems()
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
  grid-template-columns: repeat(auto-fit, minmax(50px, 1fr));
  gap: 10px;
  justify-content: center;
  align-items: center;
}

.button-item {
  width: 50px;
  height: 50px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 14px;
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