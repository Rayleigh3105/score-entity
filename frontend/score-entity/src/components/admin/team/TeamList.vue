<template>
  <div class="card">
    <Toast/>

    <Toolbar class="mb-6">
      <template #start>
        <Button label="Hinzufügen" icon="bx bx-plus" class="mr-2" @click="openNew"/>
        <Button label="Löschen" icon="pi pi-trash" severity="danger" outlined @click="confirmDeleteSelected"
                :disabled="!selectedGroups || !selectedGroups.length"/>
      </template>

    </Toolbar>

    <DataTable
        ref="dt"
        :filters="filters"
        v-model:selection="selectedGroups"
        data-key="id"
        :value="groups"
    >
      <template #header>
        <div class="flex flex-wrap gap-2 items-center justify-between">
          <h4 class="m-0">Gruppen Verwalten</h4>
          <IconField>
            <InputIcon>
              <i class="bx bx-search"/>
            </InputIcon>
            <InputText v-model="filters['global'].value" placeholder="Suchen..."/>
          </IconField>
        </div>
      </template>
      <Column selectionMode="multiple" style="width: 3rem" :exportable="false"></Column>
      <Column field="name" header="Name"></Column>
      <Column :exportable="false" style="min-width: 12rem; text-align: right">
        <template #body="slotProps">
          <Button icon="bx bx-pencil" outlined rounded class="mr-2" @click="editGroup(slotProps.data)"/>
          <Button icon="bx bx-trash" outlined rounded severity="danger" @click="confirmDeleteGroup(slotProps.data)"/>
        </template>
      </Column>
    </DataTable>

    <Dialog v-model:visible="groupDialog" :style="{ width: '450px' }" header="Gruppen Details" :modal="true">
      <div class="flex flex-col gap-6">
        <div>
          <label for="name" class="block font-bold mb-3">Name</label>
          <InputText id="name" v-model.trim="group.name" required="true" autofocus :invalid="submitted && !group.name"
                     fluid/>
          <small v-if="submitted && !group.name" class="text-red-500">Name is required.</small>
        </div>
        <div>
          <FileUpload mode="basic" accept="image/*" name="demo[]" :url="config.basePath + '/file'" :auto="true" chooseLabel="Auswählen"/>
        </div>
      </div>

      <template #footer>
        <Button label="Abbrechen" icon="bx bx-x" text @click="hideDialog"/>
        <Button label="Speichern" icon="bx bx-save" @click="createGroup"/>
      </template>
    </Dialog>

    <Dialog v-model:visible="deleteGroupsDialog" :style="{ width: '450px' }" header="Bestätigung" :modal="true">
      <div class="flex items-center gap-4">
        <i class="pi pi-exclamation-triangle !text-3xl"/>
        <span v-if="group">Willst du wirklich die selektierten Gruppen löschen?<br> Es werden ggf. alle dazugehörigen Daten gelöscht.</span>
      </div>
      <template #footer>
        <Button label="Nein" icon="bx bx-x" text @click="deleteGroupsDialog = false"/>
        <Button label="Ja" icon="bx bx-check" text @click="deleteSelectedGroups"/>
      </template>
    </Dialog>

    <Dialog v-model:visible="deleteGroupDialog" :style="{ width: '450px' }" header="Bestätigung" :modal="true">
      <div class="flex items-center gap-4">
        <i class="pi pi-exclamation-triangle !text-3xl"/>
        <span v-if="group"
        >Willst du wirklich <b>{{ group.name }}</b
        > löschen?<br> Es werden ggf. alle dazugehörigen Daten gelöscht.</span>
      </div>
      <template #footer>
        <Button label="Nein" icon="bx bx-x" text @click="deleteGroupDialog = false"/>
        <Button label="Ja" icon="bx bx-check" @click="deleteGroup"/>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {FilterMatchMode} from '@primevue/core/api';
import {Configuration, GroupResourceApi, FileResourceApi} from "@/api";
import type {Group} from "@/api";

import Toast from 'primevue/toast';
import DataTable from 'primevue/datatable';
import Toolbar from 'primevue/toolbar';
import Button from 'primevue/button';
import Column from 'primevue/column';
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import FileUpload from 'primevue/fileupload';
import {useToast} from "primevue";

const deleteGroupDialog = ref(false);
const deleteGroupsDialog = ref(false);
const initialGroup = {
  id: null,
  name: '',
  imageUrl: undefined,
  totalScore: undefined,
}
const group = ref<Group>(initialGroup);
const groupDialog = ref(false);
const submitted = ref(false);
const toast = useToast();
const filters = ref({
  'global': {value: null, matchMode: FilterMatchMode.CONTAINS},
});
const selectedGroups = ref<Group[]>([]);
const groups = ref<Group[]>([]);
const isLoading = ref(false);
const error = ref<string | null>(null);
const config = new Configuration({
  basePath: 'http://localhost:8080',
});

// API-Instanz erstellen
const groupApi = new GroupResourceApi(config);
const fileApi = new FileResourceApi(config);

// Daten abrufen
const fetchGroups = async () => {
  isLoading.value = true;
  error.value = null;

  try {
    const response = await groupApi.groupsGet(); // API-Aufruf
    groups.value = response.data;
  } catch (err) {
    error.value = 'Fehler beim Abrufen der Gruppen.';
    console.error(err);
  } finally {
    isLoading.value = false;
  }
};


const openNew = () => {
  group.value = {...initialGroup};
  submitted.value = false;
  groupDialog.value = true;
};

const hideDialog = () => {
  groupDialog.value = false;
  submitted.value = false;
};

const createGroup = async () => {
  try {
    const response = await groupApi.groupsPost(group.value);

    if (response.data.id && response.status === 200) {
      submitted.value = true;
      toast.add({
        severity: 'success',
        summary: 'Erfolgreich',
        detail: 'Gruppe erfolgreich erstellt.',
        life: 3000
      });
      groupDialog.value = false;
      group.value = {...initialGroup};

      groups.value.push(response.data);
    }
  } catch (error) {
    console.error('Fehler beim Speichern der Gruppe:', error);
    toast.add({
      severity: 'error',
      summary: 'Fehler',
      detail: 'Gruppe konnte nicht gespeichert werden.',
      life: 3000,
    });
  }


};

const confirmDeleteSelected = () => {
  deleteGroupsDialog.value = true;
};

const confirmDeleteGroup = (grp: Group) => {
  group.value = {...grp};
  deleteGroupDialog.value = true;
};

const deleteSelectedGroups = () => {
  try {
    selectedGroups.value.forEach(async (groupToDelete: Group) => {
      const response = await groupApi.groupsIdDelete(groupToDelete.id as number);
      if (response.status === 200) {
        groups.value = groups.value.filter(val => val.id !== groupToDelete.id);
      }
    });
    groups.value = groups.value.filter(val => !selectedGroups.value.includes(val));
    deleteGroupsDialog.value = false;
    selectedGroups.value = [];
    toast.add({severity: 'success', summary: 'Erfolgreich', detail: 'Gruppen gelöscht', life: 3000});
  } catch (error) {
    console.error('Fehler beim Löschen der Gruppen:', error);
    toast.add({
      severity: 'error',
      summary: 'Fehler',
      detail: 'Gruppen konnten nicht gelöscht werden.',
      life: 3000,
    });
  }

};

const deleteGroup = async () => {
  try {
    const response = await groupApi.groupsIdDelete(group.value.id as number);

    if (response.status === 200 || response.status === 204) {
      groups.value = groups.value.filter(val => val.id !== group.value.id);
      deleteGroupDialog.value = false;
      group.value = {...initialGroup};
      toast.add({severity: 'success', summary: 'Erfolgreich', detail: 'Gruppe gelöscht', life: 3000});
    }

  } catch (error) {
    console.error('Fehler beim Löschen der Gruppe:', error);
    toast.add({
      severity: 'error',
      summary: 'Fehler',
      detail: 'Gruppe konnte nicht gelöscht werden.',
      life: 3000,
    });
  }
}

const editGroup = (grp: Group) => {
  group.value = {...grp};
  groupDialog.value = true;
};


onMounted(() => {
  fetchGroups();
});
</script>

<style scoped>
</style>