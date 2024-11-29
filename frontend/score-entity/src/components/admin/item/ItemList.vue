<template>
  <div class="card">
    <Toast/>

    <Toolbar class="mb-6">
      <template #start>
        <Button label="Hinzufügen" icon="bx bx-plus" class="mr-2" @click="openNew"/>
        <Button label="Löschen" icon="pi pi-trash" severity="danger" outlined @click="confirmDeleteSelected"
                :disabled="!selectedItems || !selectedItems.length"/>
      </template>
    </Toolbar>

    <DataTable
        ref="dt"
        :filters="filters"
        v-model:selection="selectedItems"
        data-key="id"
        :value="items"
        :paginator="true"
        :rows="10"
        :rowsPerPageOptions="[10, 25]"
        scrollable
        scroll-height="flex"
    >
      <template #header>
        <div class="flex flex-wrap gap-2 items-center justify-between">
          <h4 class="m-0">Items Verwalten</h4>
          <IconField>
            <InputIcon>
              <i class="bx bx-search"/>
            </InputIcon>
            <InputText v-model="filters['global'].value" placeholder="Suchen..."/>
          </IconField>
        </div>
      </template>
      <Column selectionMode="multiple" style="width: 3rem"></Column>
      <Column header="Bild" style="width: 3rem">
        <template #body="{ data }">
          <div class="flex items-center gap-2">
            <img v-if="data.image?.imageUrl"
                 alt="Itembild"
                 :src="data.image?.imageUrl"
                 style="width: 32px"/>
            <img v-else
                 alt="Itembild"
                 class="rounded-full"
                 src="https://res.cloudinary.com/drcmgtifj/image/upload/c_thumb,w_200,g_face/v1732888382/vecteezy_hand-drawnman-avatar-profile-icon-for-social-networks__cvzd71.jpg"
                 style="width: 32px"/>
          </div>
        </template>
      </Column>
      <Column field="name" header="Name"></Column>
      <Column field="scoreValue" header="Wert Punktezahl"></Column>
      <Column :exportable="false" style="min-width: 12rem; text-align: right">
        <template #body="slotProps">
          <Button icon="bx bx-pencil" outlined rounded class="mr-2" @click="editItem(slotProps.data)"/>
          <Button icon="bx bx-trash" outlined rounded severity="danger" @click="confirmDeleteItem(slotProps.data)"/>
        </template>
      </Column>
    </DataTable>

    <Dialog v-model:visible="itemDialog" :style="{ width: '450px' }" header="Item Details" :modal="true">
      <div class="flex flex-col gap-6">
        <!-- Centered and Bigger Image -->
        <div class="flex justify-center">
          <img
              v-if="image.imageUrl"
              class="w-32 h-32 rounded-sm bg-gray-50 object-cover"
              :src="image.imageUrl"
              alt="Itembild"
          />
          <img v-else
               class="w-32 h-32 rounded-full bg-gray-50 object-cover"
               src="https://res.cloudinary.com/drcmgtifj/image/upload/c_thumb,w_200,g_face/v1732888382/vecteezy_hand-drawnman-avatar-profile-icon-for-social-networks__cvzd71.jpg"
               alt="Itembild"/>
        </div>

        <!-- File Upload First -->
        <div v-if="!image.imageUrl">
          <FileUpload
              mode="basic"
              accept="image/*"
              name="demo[]"
              :url="config.basePath + '/file'"
              :auto="true"
              chooseLabel="Auswählen"
              @upload="onUpload"
          />
        </div>
        <div class="flex justify-center" v-else>
          <Button icon="bx bx-trash" outlined rounded severity="danger"
                  @click="image = {imageUrl: '', publicId: ''}"/>
        </div>

        <!-- Input Field for Name -->
        <div>
          <label for="name" class="block font-bold mb-3">Name</label>
          <InputText
              id="name"
              v-model.trim="item.name"
              required="true"
              autofocus
              :invalid="submitted && !item.name"
              fluid
          />
          <small v-if="submitted && !item.name" class="text-red-500">Name muss gesetzt werden.</small>
        </div>

        <!-- Input Field for Name -->
        <div>
          <label for="scoreValue" class="block font-bold mb-3">Wert Punktezahl</label>
          <InputNumber
              id="scoreValue"
              v-model.number="item.scoreValue"
              required="true"
              autofocus
              :invalid="submitted && !item.scoreValue"
              fluid
          />
          <small v-if="submitted && !item.scoreValue" class="text-red-500">Wert muss gesetzt werden.</small>
        </div>
      </div>

      <!-- Footer Buttons -->
      <template #footer>
        <Button label="Abbrechen" icon="bx bx-x" text @click="hideDialog"/>
        <Button v-if="item.id == null" label="Speichern" icon="bx bx-save" @click="createItem"/>
        <Button v-else label="Speichern" icon="bx bx-save" @click="updateItem"/>
      </template>
    </Dialog>

    <Dialog v-model:visible="deleteItemsDialog" :style="{ width: '450px' }" header="Bestätigung" :modal="true">
      <div class="flex items-center gap-4">
        <i class="pi pi-exclamation-triangle !text-3xl"/>
        <span v-if="item">Willst du wirklich die selektierten Items löschen?<br> Es werden ggf. alle dazugehörigen Daten gelöscht.</span>
      </div>
      <template #footer>
        <Button label="Nein" icon="bx bx-x" text @click="deleteItemsDialog = false"/>
        <Button label="Ja" icon="bx bx-check" text @click="deleteSelectedItems"/>
      </template>
    </Dialog>

    <Dialog v-model:visible="deleteItemDialog" :style="{ width: '450px' }" header="Bestätigung" :modal="true">
      <div class="flex items-center gap-4">
        <i class="pi pi-exclamation-triangle !text-3xl"/>
        <span v-if="item"
        >Willst du wirklich <b>{{ item.name }}</b
        > löschen?<br> Es werden ggf. alle dazugehörigen Daten gelöscht.</span>
      </div>
      <template #footer>
        <Button label="Nein" icon="bx bx-x" text @click="deleteItemDialog = false"/>
        <Button label="Ja" icon="bx bx-check" @click="deleteItem"/>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {FilterMatchMode} from '@primevue/core/api';
import type {Item, Image, ItemUpdateRequest} from "@/api";
import {Configuration, ItemResourceApi} from "@/api";

import Toast from 'primevue/toast';
import DataTable from 'primevue/datatable';
import Toolbar from 'primevue/toolbar';
import Button from 'primevue/button';
import Column from 'primevue/column';
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import FileUpload, {type FileUploadUploadEvent} from 'primevue/fileupload';
import {useToast} from "primevue";

const deleteItemDialog = ref(false);
const deleteItemsDialog = ref(false);
const initialItem : Item = {
  id: null,
  name: '',
  scoreValue: 1,
  image: undefined,
}
const item = ref<Item>(initialItem);
const itemDialog = ref(false);
const submitted = ref(false);
const toast = useToast();
const filters = ref({
  'global': {value: null, matchMode: FilterMatchMode.CONTAINS},
});
const selectedItems = ref<Item[]>([]);
const items = ref<Item[]>([]);
const isLoading = ref(false);
const error = ref<string | null>(null);
const config = new Configuration({
  basePath: 'http://localhost:8080',
});

// API-Instanz erstellen
const itemApi = new ItemResourceApi(config);

// Daten abrufen
const fetchItems = async () => {
  isLoading.value = true;
  error.value = null;

  try {
    const response = await itemApi.itemsGet();
    items.value = response.data;
  } catch (err) {
    error.value = 'Fehler beim Abrufen der Items.';
    console.error(err);
  } finally {
    isLoading.value = false;
  }
};

const openNew = () => {
  item.value = {...initialItem};
  image.value = {publicId: '', imageUrl: ''};
  submitted.value = false;
  itemDialog.value = true;
};

const hideDialog = () => {
  itemDialog.value = false;
  submitted.value = false;
};

const createItem = async () => {
  try {
    if (image.value.id) {
      item.value.image = image.value
    } else {
      item.value.image = undefined;
    }
    const response = await itemApi.itemsPost(item.value);

    if (response.data.id && response.status === 200) {
      submitted.value = true;
      toast.add({
        severity: 'success',
        summary: 'Erfolgreich',
        detail: 'Item erfolgreich erstellt.',
        life: 3000
      });
      itemDialog.value = false;
      item.value = {...initialItem};

      items.value.push(response.data);
    }
  } catch (error) {
    console.error('Fehler beim Speichern des Items:', error);
    toast.add({
      severity: 'error',
      summary: 'Fehler',
      detail: 'Item konnte nicht gespeichert werden.',
      life: 3000,
    });
  }
};

const updateItem = async () => {
  try {
    const updateRequest: ItemUpdateRequest = {
      name: item.value.name,
      image: image.value,
      scoreValue: item.value.scoreValue,
    };

    const response = await itemApi.itemsIdPut(item.value.id as number, updateRequest);

    if (response.status === 200) {
      submitted.value = true;
      toast.add({
        severity: 'success',
        summary: 'Erfolgreich',
        detail: 'Item erfolgreich aktualisiert.',
        life: 3000
      });
      itemDialog.value = false;
      item.value = {...initialItem};

      const index = items.value.findIndex(val => val.id === response.data.id);
      if (index != -1 && items.value[index]) {
        items.value[index] = response.data as unknown as Item;
      }

    }
  } catch (error) {
    console.error('Fehler beim Speichern des Items:', error);
    toast.add({
      severity: 'error',
      summary: 'Fehler',
      detail: 'Item konnte nicht gespeichert werden.',
      life: 3000,
    });
  }
};

const confirmDeleteSelected = () => {
  deleteItemsDialog.value = true;
};

const confirmDeleteItem = (itm: Item) => {
  item.value = {...itm};
  deleteItemDialog.value = true;
};

const deleteSelectedItems = () => {
  try {
    selectedItems.value.forEach(async (itemToDelete: Item) => {
      const response = await itemApi.itemsIdDelete(itemToDelete.id as number);
      if (response.status === 200) {
        items.value = items.value.filter(val => val.id !== itemToDelete.id);
      }
    });
    items.value = items.value.filter(val => !selectedItems.value.includes(val));
    deleteItemsDialog.value = false;
    selectedItems.value = [];
    toast.add({severity: 'success', summary: 'Erfolgreich', detail: 'Items gelöscht', life: 3000});
  } catch (error) {
    console.error('Fehler beim Löschen der Items:', error);
    toast.add({
      severity: 'error',
      summary: 'Fehler',
      detail: 'Items konnten nicht gelöscht werden.',
      life: 3000,
    });
  }

};

const deleteItem = async () => {
  try {
    const response = await itemApi.itemsIdDelete(item.value.id as number);

    if (response.status === 200 || response.status === 204) {
      items.value = items.value.filter(val => val.id !== item.value.id);
      deleteItemDialog.value = false;
      item.value = {...initialItem};
      toast.add({severity: 'success', summary: 'Erfolgreich', detail: 'Item gelöscht', life: 3000});
    }

  } catch (error) {
    console.error('Fehler beim Löschen des Items:', error);
    toast.add({
      severity: 'error',
      summary: 'Fehler',
      detail: 'Item konnte nicht gelöscht werden.',
      life: 3000,
    });
  }
}

const editItem = (itm: Item) => {
  item.value = {...itm};
  image.value = itm.image ?? {publicId: '', imageUrl: ''};
  itemDialog.value = true;
};

const image = ref<Image>({
  publicId: '',
  imageUrl: '',
});

const onUpload = (event: FileUploadUploadEvent) => {
  const response = JSON.parse(event.xhr.response)
  image.value.imageUrl = response.imageUrl; // Update the reactive property
  image.value = response
  console.log('Image uploaded:', image.value);
}

onMounted(() => {
  fetchItems();
});
</script>

<style scoped>
</style>