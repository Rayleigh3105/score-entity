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
        data-key="id"
        :value="items"
        selectionMode="single"
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
      <Column field="name" header="Name"></Column>
      <Column field="price" header="Preis">
        <template #body="{ data }">
          {{ data.price.toFixed(2).replace('.', ',') }} €
        </template>
      </Column>
      <Column field="quantity" header="Menge"></Column>
      <Column header="Bereich">
        <template #body="{ data }">
          <span v-if="data.areas && data.areas.length">
            {{
              areas.filter((a: Area) => data.areas.map((ar: Area) => ar.id).includes(a.id))
                  .map((a: Area) => a.name ?? '')
                  .join(', ')
            }}
          </span>
          <span v-else>–</span>
        </template>
      </Column>
      <Column header="Pfand">
        <template #body="{ data }">
          <span v-if="data.deposit">
            {{ (data.deposit.name && data.deposit.name.trim() !== '' ? data.deposit.name : (data.deposit.value !== undefined ? data.deposit.value.toFixed(2).replace('.', ',') + ' €' : '')) }}
          </span>
          <span v-else>–</span>
        </template>
      </Column>
      <Column :exportable="false" style="min-width: 12rem; text-align: right">
        <template #body="slotProps">
          <Button icon="bx bx-pencil" outlined rounded class="mr-2" @click="editItem(slotProps.data)"/>
          <Button icon="bx bx-trash" outlined rounded severity="danger" @click="confirmDeleteItem(slotProps.data)"/>
        </template>
      </Column>
    </DataTable>

    <Dialog v-model:visible="itemDialog" :style="{ width: '450px' }" header="Item Details" :modal="true">
      <div class="flex flex-col gap-6">
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

        <div>
          <label for="price" class="block font-bold mb-3">Preis</label>
          <InputNumber
              id="price"
              v-model.number="item.price"
              mode="decimal"
              :minFractionDigits="2"
              required="true"
              :invalid="submitted && (item.price === null || item.price === undefined)"
              fluid
          />
          <small v-if="submitted && (item.price === null || item.price === undefined)" class="text-red-500">Preis muss gesetzt werden.</small>
        </div>

        <div>
          <label for="quantity" class="block font-bold mb-3">Menge</label>
          <InputNumber
              id="quantity"
              v-model.number="item.quantity"
              required="true"
              :invalid="submitted && item.quantity == null"
              fluid
          />
          <small v-if="submitted && item.quantity == null" class="text-red-500">Menge muss gesetzt werden.</small>
        </div>

        <div>
          <label for="deposit" class="block font-bold mb-3">Pfand</label>
          <select
              id="deposit"
              v-model="item.deposit"
              class="w-full p-2 border rounded"
              :style="{ height: '2.5rem' }"
          >
            <option :value="undefined">Kein Pfand</option>
            <option v-for="d in deposits" :key="d.id ?? d.value" :value="d">
              {{ d.name && d.name.trim() !== '' ? d.name : (d.value !== undefined ? d.value.toFixed(2).replace('.', ',') + ' €' : '') }}
            </option>
          </select>
        </div>

        <div>
          <label for="areas" class="block font-bold mb-3">Bereiche</label>
          <MultiSelect
              id="areas"
              v-model="item.areas"
              :options="areas"
              optionLabel="name"
              placeholder="Bitte wählen"
              :class="{ 'p-invalid': submitted && (!item.areas || !item.areas.length) }"
              display="chip"
              filter
              fluid
          />
          <small v-if="submitted && (!item.areas || !item.areas.length)" class="text-red-500">Mindestens ein Bereich muss gesetzt werden.</small>
        </div>

        <div>
          <label for="color" class="block font-bold mb-3">Farbe</label>
          <ColorPicker
              id="color"
              v-model="item.color"
              inline
          />
        </div>

        <div>
          <label for="colorHex" class="block font-bold mb-3">Hex-Farbcode</label>
          <InputText
              id="colorHex"
              v-model="item.color"
              placeholder="#000000"
              fluid
          />
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
import type {Area, Deposit, Item, ItemUpdateRequest} from "@/api";
import {areaApi, depositApi, itemApi} from "@/router/api.custom";

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
import ColorPicker from 'primevue/colorpicker';
import MultiSelect from 'primevue/multiselect';
import {useToast} from "primevue";

const deleteItemDialog = ref(false);
const deleteItemsDialog = ref(false);
const initialItem : Item = {
  id: null,
  name: '',
  price: 0,
  quantity: 0,
  color: '#ffffff',
  areas: [] as Area[],
  deposit: undefined,
}
const item = ref<Item>({...initialItem});
const itemDialog = ref(false);
const submitted = ref(false);
const toast = useToast();
const filters = ref({
  'global': {value: null, matchMode: FilterMatchMode.CONTAINS},
});
const selectedItems = ref<Item[]>([]);
const items = ref<Item[]>([]);
const areas = ref<Area[]>([]);
const deposits = ref<DepositWithName[]>([]);
const isLoading = ref(false);
const error = ref<string | null>(null);

export interface DepositWithName extends Deposit {
  name: string;
}
// Daten abrufen
const fetchItems = async () => {
  isLoading.value = true;
  error.value = null;

  try {
    const response = await itemApi.itemsGet();
    // Map each item to include areas list (no change needed)
    items.value = response.data.map(it => ({
      ...it,
      areas: it.areas || []
    }));
  } catch (err) {
    error.value = 'Fehler beim Abrufen der Items.';
    console.error(err);
  } finally {
    isLoading.value = false;
  }
};

const fetchAreas = async () => {
  try {
    const res = await areaApi.areasGet();
    areas.value = res.data;
  } catch (e) {
    console.error("Fehler beim Laden der Bereiche:", e);
  }
};

const fetchDeposits = async () => {
  try {
    const res = await depositApi.depositsGet();
    // Name ergänzen falls leer
    deposits.value = res.data.map((d: Deposit) => ({
      ...d,
      // @ts-ignore
      name: d.name && d.name.trim() !== ''
          // @ts-ignore
          ? d.name
          // @ts-ignore
          : `Pfand ${d.value.toFixed(2).replace('.', ',')} €`
    }));
  } catch (e) {
    console.error("Fehler beim Laden der Pfandarten:", e);
  }
};

onMounted(() => {
  fetchItems();
  fetchAreas();
  fetchDeposits();
});

const openNew = () => {
  item.value = {...initialItem};
  item.value.areas = [];
  item.value.deposit = undefined;
  submitted.value = false;
  itemDialog.value = true;
};

const hideDialog = () => {
  itemDialog.value = false;
  submitted.value = false;
};

const createItem = async () => {
  try {
    const createRequest: Omit<Item, 'id'> = {
      name: item.value.name,
      price: item.value.price,
      quantity: item.value.quantity,
      color: item.value.color,
      areas : item.value.areas,
      deposit: item.value.deposit
    };

    console.log("Erstelle Item mit Request:", createRequest);
    const response = await itemApi.itemsPost(createRequest);
    if (response.data.id && response.status === 200) {
      submitted.value = true;
      toast.add({severity: 'success', summary: 'Erfolgreich', detail: 'Item erstellt.', life: 3000});
      itemDialog.value = false;
      item.value = {...initialItem};
      await fetchItems();
    }
  } catch (e) {
    console.error(e);
    toast.add({severity: 'error', summary: 'Fehler', detail: 'Erstellung fehlgeschlagen.', life: 3000});
  }
};

const updateItem = async () => {
  try {
    const updateRequest: ItemUpdateRequest = {
      name: item.value.name,
      price: item.value.price,
      quantity: item.value.quantity,
      color: item.value.color,
      areaIds: item.value.areas.map(a => a.id).filter((id): id is number => id !== undefined),
      depositId: item.value.deposit?.id ?? undefined,
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

      // @ts-ignore
      const index = items.value.findIndex(val => val.id === response.data.id);
      if (index !== -1 && items.value[index]) {
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
  let mappedDeposit: Deposit | undefined = undefined;
  if (itm.deposit && itm.deposit.id) {
    mappedDeposit = deposits.value.find(d => d.id === itm.deposit?.id);
  }
  item.value = {...itm, deposit: mappedDeposit};
  itemDialog.value = true;
};
</script>

<style scoped>
.p-datatable .p-datatable-thead > tr > th,
.p-datatable .p-datatable-tbody > tr > td {
  white-space: nowrap;
}

.p-datatable .p-datatable-tbody > tr > td {
  padding: 0.75rem 1rem;
}
</style>