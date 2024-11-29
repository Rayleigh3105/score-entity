<template>
  <div class="card">
    <Toast/>

    <Toolbar class="mb-6">
      <template #start>
        <Button label="Hinzufügen" icon="bx bx-plus" class="mr-2" @click="openNew"/>
      </template>

    </Toolbar>

    <DataTable :value="groups" tableStyle="min-width: 50rem">
      <Column field="id" header="Id"></Column>
      <Column field="name" header="Name"></Column>
    </DataTable>

    <Dialog v-model:visible="groupDialog" :style="{ width: '450px' }" header="Neue Gruppe erstellen" :modal="true">
      <div class="flex flex-col gap-6">
        <div>
          <label for="name" class="block font-bold mb-3">Name</label>
          <InputText id="name" v-model.trim="group.name" required="true" autofocus :invalid="submitted && !group.name"
                     fluid/>
          <small v-if="submitted && !group.name" class="text-red-500">Name is required.</small>
        </div>
      </div>

      <template #footer>
        <Button label="Abbrechen" icon="bx bx-x" text @click="hideDialog"/>
        <Button label="Speichern" icon="bx bx-save" @click="saveGroup"/>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {Configuration, type Group, GroupResourceApi} from "../../../../api";

import Toast from 'primevue/toast';
import DataTable from 'primevue/datatable';
import Toolbar from 'primevue/toolbar';
import Button from 'primevue/button';
import Column from 'primevue/column';
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';
import {useToast} from "primevue";

const group = ref<Group>({});
const groupDialog = ref(false);
const submitted = ref(false);
const toast = useToast();
// Zustand der Komponente
const groups = ref([]);
const isLoading = ref(false);
const error = ref<string | null>(null);
const config = new Configuration({
  basePath: 'http://localhost:8080',
});

// API-Instanz erstellen
const api = new GroupResourceApi(config);

// Daten abrufen
const fetchGroups = async () => {
  isLoading.value = true;
  error.value = null;

  try {
    const response = await api.groupsGet(); // API-Aufruf
    groups.value = response.data;
  } catch (err) {
    error.value = 'Fehler beim Abrufen der Gruppen.';
    console.error(err);
  } finally {
    isLoading.value = false;
  }
};


const openNew = () => {
  group.value = {};
  submitted.value = false;
  groupDialog.value = true;
};

const hideDialog = () => {
  groupDialog.value = false;
  submitted.value = false;
};

const saveGroup = () => {
  submitted.value = true;
  toast.add({
    severity: 'success',
    summary: 'Erfolgreich',
    detail: 'Gruppe erfolgreich erstellt.',
    life: 3000
  });
  groupDialog.value = false;
  group.value = {};

};

onMounted(() => {
  fetchGroups();
});
</script>

<style scoped>
/* Optional: Stile für die Tabelle */
</style>