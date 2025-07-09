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
        <div class="score-dialog-content">
          <!-- ToggleButton -->
          <div class="score-toggle">
            <ToggleButton class="my-2" v-model="checked" onLabel="Addieren" offLabel="Subtrahieren" on-icon="bx bx-plus" off-icon="bx bx-minus"/>
          </div>

          <!-- Vorgeschlagene Punkte -->
          <div v-if="suggestedPointsValue" class="suggested-points-block">
            <div class="suggested-points-label">Vorgeschlagene Punkte:</div>
            <div class="suggested-points-value">{{ suggestedPointsValue }}</div>
            <Button
              label="Vorgeschlagene Punkte übernehmen"
              class="suggested-points-btn"
              @click="doScore(suggestedPointsValue)"
            />
          </div>

          <!-- Zahlenauswahl -->
          <div class="button-grid mt-6">
            <Button
                v-for="number in numbers"
                :key="number"
                :label="number.toString()"
                class="p-button-primary button-item"
                @click="doScore(number)"
            />
          </div>

          <!-- Eigene Eingabe -->
          <div class="mt-4 text-center">
            <div class="custom-label">Oder eigene Punktzahl eingeben:</div>
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
              class="footer-btn"
              @click="doScore(score.points ?? 0)"
          />
          <Button label="Abbrechen" icon="bx bx-x" text @click="hideDialog"/>
        </template>
      </Dialog>
    </template>

  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {FilterMatchMode} from '@primevue/core/api';
import {type Group, type ScoreUpdateRequest} from "@/api";

import Toast from 'primevue/toast';
import DataTable, {type DataTableRowSelectEvent} from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from 'primevue/inputtext';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import {useToast} from "primevue";
import Button from "primevue/button";
import Dialog from "primevue/dialog";
import ToggleButton from "primevue/togglebutton";
import InputNumber from 'primevue/inputnumber';
import {groupApi, scoreApi} from "@/router/api.custom";

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


const props = defineProps<{
  suggestedPoints?: number
}>();

const emit = defineEmits(['done']);

function finish() {
  emit('done');
}


const suggestedPointsValue = ref(props.suggestedPoints ?? null);

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
    finish();
  } catch (err) {
    toast.add({severity: 'error', summary: 'Fehler', detail: 'Fehler beim Speichern der Punkte.', life: 3000});
    console.error(err);
  } finally {
    scoreDialog.value = false;
    score.value.points = undefined;
    filters.value['global'].value = null;
  }
};

const onRowSelect = (event: DataTableRowSelectEvent) => {
  scoreDialog.value = true;
  checked.value = true;
  score.value.groupId = event.data.id;
};

const hideDialog = () => {
  scoreDialog.value = false;
};

onMounted(() => {
  fetchGroups();
});
</script>

<style scoped>
.score-dialog-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 0 0 0;
}

.score-toggle {
  margin-bottom: 16px;
  display: flex;
  justify-content: center;
}

.suggested-points-block {
  width: 100%;
  background: #eafff2;
  border: 1.5px dashed #7ac99b;
  border-radius: 10px;
  padding: 18px 10px 12px 10px;
  margin-bottom: 20px;
  text-align: center;
  box-shadow: 0 2px 8px #e8fbe6;
}

.suggested-points-label {
  font-weight: bold;
  color: #21613d;
  font-size: 1.1em;
  letter-spacing: .03em;
}
.suggested-points-value {
  color: #00995b;
  font-weight: bold;
  font-size: 2.4em;
  margin: 0.1em 0 0.5em 0;
}
.suggested-points-btn {
  width: 90%;
  margin: 0 auto;
  margin-bottom: 5px;
  font-weight: 600;
  font-size: 1.04em;
  background: #51c38b !important;
  border: none;
}

.button-grid {
  display: grid;
  grid-template-columns: repeat(5, 56px);
  gap: 12px;
  justify-content: center;
  margin: 0 auto;
}

.button-item {
  width: 56px;
  height: 56px;
  border-radius: 10px !important;
  font-size: 20px;
  font-weight: bold;
  background: #47c69a !important;
  color: #fff;
  border: none;
  box-shadow: 0 1px 3px #eaf8f1;
  transition: background 0.12s;
}
.button-item:hover {
  background: #32b97f !important;
}

.custom-label {
  font-size: 1em;
  margin-bottom: 8px;
  color: #666;
  font-weight: 500;
}

.footer-btn {
  width: 130px;
  font-size: 1.08em;
  margin-right: 10px;
  font-weight: 600;
}
</style>