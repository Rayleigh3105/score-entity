<template>
  <div class="calculator">
    <!-- Toolbar oben: Bereichsauswahl & Rückgängig -->
    <div class="toolbar">
      <Dropdown
        v-model="selectedArea"
        :options="areaOptions"
        optionLabel="name"
        placeholder="Bereich wählen"
        class="area-select"
      />
      <Button
          icon="bx bx-trash"
          label="Alles löschen"
          @click="clearAll"
          class="undo-btn"
      />

    </div>

    <div class="panels">
      <!-- Linke Seite: Warenkorb / Zusammenfassung -->
      <div class="summary">
        <div class="cart-list-pos">
          <div v-for="entry in cart" :key="entry.id ?? entry.name" class="cart-row-pos">
            <span class="cart-quantity-pos">{{ entry.quantity }} x</span>
            <span class="cart-name-pos">
              {{ entry.name }}
              <span
                v-if="entry.deposit && entry.deposit.value"
                class="cart-pfand-label"
                title="Enthält Pfand"
              >
                + Pfand {{ formatMoney(entry.deposit.value * entry.quantity) }} €
              </span>
            </span>
            <span class="cart-price-pos">{{ formatMoney(entry.price * entry.quantity + (entry.deposit && entry.deposit.value ? entry.deposit.value * entry.quantity : 0)) }} €</span>
            <Button
              icon="bx bx-trash"
              class="cart-remove-btn"
              @click="removeFromCart(entry)"
              style="margin-left: 0.7rem; background: #fff; color: #222; border: 1px solid #ccc;"
              :pt="{ icon: { style: { fontSize: '1.1rem', color: '#222' } } }"
            />
          </div>
        </div>
        <div class="pfand-rabatt-row">
          <div class="return-pfand-block pfand-compact">
            <div class="pfand-list">
              <div
                  v-for="d in allDeposits"
                  :key="d.id ?? d.value"
                  class="pfand-list-row pfand-list-row-big"
              >
                <span class="pfand-list-label pfand-list-label-big">
                  {{ formatMoney(d.value ?? 0) }} €
                </span>
                <div class="pfand-counter-btns pfand-counter-btns-big">
                  <Button
                    icon="bx bx-minus"
                    size="large"
                    :disabled="getReturnedAmountRef(d.id ?? 0).value <= 0"
                    @click="getReturnedAmountRef(d.id ?? 0).value--"
                    class="pfand-btn pfand-btn-minus pfand-btn-big"
                  />
                  <span class="pfand-counter-value pfand-counter-value-big">{{ getReturnedAmountRef(d.id ?? 0).value }}</span>
                  <Button
                    icon="bx bx-plus"
                    size="large"
                    @click="getReturnedAmountRef(d.id ?? 0).value++"
                    class="pfand-btn pfand-btn-plus pfand-btn-big"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="rabatt-punkte-row">
          <Button
              class="discount-btn discount-btn-small"
              label="Rabatt"
              @click="showDiscount = true"
          />
          <Button
              class="discount-btn discount-btn-small"
              label="Punkteverteilung"
              @click="showCentralView = true"
          />
        </div>
        <Dialog v-model:visible="showDiscount" header="Rabatt auswählen" :modal="true" :style="{ width: '350px' }">
          <div class="discount-options">
            <Button
              v-for="step in 10"
              :key="step"
              class="discount-option-btn"
              :label="`${step * 5} %`"
              @click="selectDiscount(step * 5)"
              :outlined="discount !== step * 5"
              :severity="discount === step * 5 ? 'success' : 'secondary'"
              style="margin: 0.3rem 0.6rem 0.3rem 0;"
            />
            <Button
              label="Kein Rabatt"
              @click="selectDiscount(0)"
              :outlined="discount !== 0"
              :severity="discount === 0 ? 'success' : 'secondary'"
              style="margin-top: 1rem;"
            />
          </div>
        </Dialog>
        <div class="cart-total-big">
          <div class="cart-total-label">Rechnungsbetrag</div>
          <div class="cart-total-value">{{ formatMoney(total) }} EUR</div>
          <div v-if="discount" class="discount-info">
            -{{ discount }}% Rabatt angewendet
          </div>
          <div v-if="discount" class="discount-diff">
            Rabatt: {{ formatMoney(baseTotal - total) }} EUR
          </div>
          <div class="pfand-info">
            Enthaltenes Pfand: +{{ formatMoney(totalPfand) }} EUR<br>
            Davon abgegeben: -{{ formatMoney(totalPfandReturned) }} EUR
          </div>
        </div>
      </div>

      <!-- Rechte Seite: Artikel-Auswahl -->
      <div class="items">
        <div class="item-grid-colored">
          <div
            v-for="(group, color) in groupedAndSortedItems"
            :key="color"
            class="item-color-column"
          >
            <Button
              v-for="it in group"
              :key="it.id ?? it.name"
              class="item-card"
              @click="addToCart(it)"
              :style="{
                background: normalizeColor(it.color),
                borderColor: normalizeColor(it.color),
                color: getContrastColor(it.color)
              }"
            >
              <div class="item-info">
                <div class="item-name">{{ it.name }}</div>
                <div class="item-meta">
                  <span><b>{{ it.quantity }} x</b></span> /
                  <span>{{ formatMoney(it.price) }} €</span>
                </div>
              </div>
            </Button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <Dialog v-model:visible="showCentralView" header="Punkteverteilung" :modal="true" :style="{ height: '98vh', width: '98vw' }">
    <CentralView :suggested-points="suggestedPoints" @done="showCentralView = false" />
  </Dialog>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from 'vue';
import { useRoute } from 'vue-router';

onMounted(() => {
  // Safari/iPad: Double-Tap Zoom verhindern
  let lastTouchEnd = 0;
  document.addEventListener('touchend', function(event) {
    const now = new Date().getTime();
    if (now - lastTouchEnd <= 50) {
      event.preventDefault();
    }
    lastTouchEnd = now;
  }, false);
});
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import type {Area, Deposit, Item} from '@/api';
import {areaApi, depositApi, itemApi} from '@/router/api.custom';
import CentralView from "@/components/admin/central/CentralView.vue";

// State
const items = ref<Item[]>([]);
const areas = ref<Area[]>([]);
const allDeposits = ref<Deposit[]>([]);
const selectedArea = ref<Area | null>(null);
const cart = ref<(Item & { quantity: number })[]>([]);
const history = ref<{ action: 'add' | 'remove'; item: Item }[]>([]);
const showDiscount = ref(false);
const discount = ref(0);
const returnedDeposits = ref<{ depositId: number, amount: number }[]>([]);
const showCentralView = ref(false);
// Bereich-Auswahl mit explizitem "Kein Bereich"
const areaOptions = computed(() => [
  { id: null, name: 'Kein Bereich' },
  ...areas.value
]);

// Daten laden
const route = useRoute();
onMounted(async () => {
  const [rit, rat, deps] = await Promise.all([
    itemApi.itemsGet().then(r => r.data),
    areaApi.areasGet().then(r => r.data),
    depositApi.depositsGet().then(r => r.data),
  ]);
  items.value = rit;
  areas.value = rat;
  allDeposits.value = deps;

  // Query-Parameter prüfen (areaId)
  const areaIdParam = route.query.areaId;
  if (areaIdParam !== undefined && areaIdParam !== null) {
    // areaId kann string oder array sein, wir nehmen nur den ersten Wert im Zweifel
    const areaIdStr = Array.isArray(areaIdParam) ? areaIdParam[0] : areaIdParam;
    // Versuche als Zahl zu interpretieren, falls möglich
    // @ts-ignore
    let areaId: number | string = areaIdStr;
    if (!isNaN(Number(areaIdStr))) {
      areaId = Number(areaIdStr);
    }
    // Suche Area mit passender ID (Typkonvertierung beachten)
    const found = areas.value.find(a => a.id == areaId);
    if (found) {
      selectedArea.value = found;
    }
  }
});

const suggestedPoints = computed(() =>
    cart.value.reduce((sum, item) => sum + (item.quantity ?? 0), 0)
);

// Gefilterte Artikel nach Bereich
const filteredItems = computed(() => {
  // Wenn kein Bereich gewählt oder "Kein Bereich" ausgewählt, zeige alle Items
  if (!selectedArea.value || selectedArea.value.id == null) return items.value;
  return items.value.filter(it =>
    it.areas.some(a => a.id === selectedArea.value!.id)
  );
});

const groupedAndSortedItems = computed(() => {
  // Nach Farbe gruppieren
  const grouped: Record<string, Item[]> = {};
  for (const it of filteredItems.value) {
    const color = normalizeColor(it.color);
    if (!grouped[color]) {
      grouped[color] = [];
    }
    grouped[color].push(it);
  }
  // Innerhalb jeder Farbe nach Menge sortieren
  for (const color in grouped) {
    grouped[color] = grouped[color].slice().sort((a, b) => (a.quantity ?? 0) - (b.quantity ?? 0));
  }
  // Spalten sortieren: zuerst Farben mit mehr als einem Item, dann die anderen
  const multiItemColors: string[] = [];
  const singleItemColors: string[] = [];
  for (const color in grouped) {
    if (grouped[color].length > 1) {
      multiItemColors.push(color);
    } else {
      singleItemColors.push(color);
    }
  }
  // Reihenfolge: Multi > Single, dann bleibt die Reihenfolge der Farben wie sie im Objekt vorkommen
  const sorted: Record<string, Item[]> = {};
  multiItemColors.concat(singleItemColors).forEach(color => {
    sorted[color] = grouped[color];
  });
  return sorted;
});

// Pfandarten im Warenkorb mit maximaler Anzahl
const cartDeposits = computed(() => {
  const res: { deposit: any, max: number }[] = [];
  cart.value.forEach(item => {
    if (item.deposit && item.deposit.id) {
      // @ts-ignore
      const idx = res.findIndex(r => r.deposit.id === item.deposit.id);
      if (idx > -1) {
        res[idx].max += item.quantity;
      } else {
        res.push({ deposit: item.deposit, max: item.quantity });
      }
    }
  });
  return res;
});

// Helfer für v-model an InputNumber Pfand-Rückgabe
function getReturnedAmountRef(depositId: number) {
  let entry = returnedDeposits.value.find(r => r.depositId === depositId);
  if (!entry) {
    entry = { depositId, amount: 0 };
    returnedDeposits.value.push(entry);
  }
  return {
    get value() { return entry!.amount; },
    set value(val: number) { entry!.amount = val; }
  }
}

// Summe abgegebenes Pfand
const totalPfandReturned = computed(() =>
  returnedDeposits.value.reduce((sum, entry) => {
    const dep = allDeposits.value.find(cd => cd.id === entry.depositId);
    // @ts-ignore
    return dep ? sum + entry.amount * dep.value : sum;
  }, 0)
);

// Warenkorb-Logik
function addToCart(it: Item) {
  const idx = cart.value.findIndex(c => c.id === it.id);
  // Wenn das Item eine quantity > 0 hat (z.B. 50 Stück), diese übernehmen, sonst 1
  const qty = it.quantity && it.quantity > 0 ? it.quantity : 1;
  if (idx >= 0) {
    cart.value[idx].quantity += qty;
  } else {
    cart.value.push({ ...it, quantity: qty });
  }
  history.value.push({ action: 'add', item: { ...it, quantity: qty } });
}

function removeFromCart(entry: Item & { quantity: number }) {
  const idx = cart.value.findIndex(c => c.id === entry.id);
  if (idx >= 0) {
    if (cart.value[idx].quantity > 1) {
      cart.value[idx].quantity--;
    } else {
      cart.value.splice(idx, 1);
    }
    history.value.push({ action: 'remove', item: entry });
  }
}


// Total berechnen
const baseTotal = computed(() =>
  cart.value.reduce((sum, c) => {
    const pfand = c.deposit && c.deposit.value ? c.deposit.value : 0;
    return sum + (c.price + pfand) * c.quantity;
  }, 0) - totalPfandReturned.value
);
const total = computed(() =>
  Math.round(baseTotal.value * (1 - discount.value / 100) * 100) / 100
);
const totalPfand = computed(() =>
  cart.value.reduce((sum, c) => sum + (c.deposit && c.deposit.value ? c.deposit.value * c.quantity : 0), 0)
);

function selectDiscount(val: number) {
  discount.value = val;
  showDiscount.value = false;
}

// Helfer
function formatMoney(v: number): string {
  return v.toFixed(2).replace('.', ',');
}
function normalizeColor(c: string): string {
  return c.startsWith('#') ? c : '#' + c;
}
function colorCell(row: any) {
  const bg = normalizeColor(row.color);
  return `<div class="color-box" style="background:${bg}"></div>`;
}
function getContrastColor(hex: string): string {
  let c = normalizeColor(hex).replace('#','');
  if (c.length === 3) c = c[0]+c[0]+c[1]+c[1]+c[2]+c[2];
  const r = parseInt(c.substr(0,2),16);
  const g = parseInt(c.substr(2,2),16);
  const b = parseInt(c.substr(4,2),16);
  return ((r*0.299 + g*0.587 + b*0.114) > 186) ? "#222" : "#fff";
}

function clearAll() {
  cart.value = [];
  returnedDeposits.value = [];
  history.value = [];
  discount.value = 0;
}

// --- Animation-Randomizer: Nie zweimal dieselbe Animation hintereinander ---
// Diese Logik kann für Animationswechsel genutzt werden, z.B. im Overlay oder in der Hauptkomponente.
// Sie stellt sicher, dass nie zweimal in Folge dieselbe Animation ausgewählt wird.
const availableAnimations = ['battle', 'moorhuhn', 'rakete']; // ggf. ergänzen
const lastAnimation = ref<string|null>(null);

function getRandomAnimation() {
  const filtered = availableAnimations.filter(a => a !== lastAnimation.value);
  const next = filtered[Math.floor(Math.random() * filtered.length)];
  lastAnimation.value = next;
  return next;
}

// Beispiel: So wechselst du die Animation, ohne dass die gleiche doppelt kommt:
// const currentAnimation = ref(getRandomAnimation());
// function switchAnimation() {
//   currentAnimation.value = getRandomAnimation();
// }
</script>

<style scoped>
.calculator {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 100px); /* Höhe des Containers */
  overflow: hidden;
}
.toolbar {
  display: flex;
  gap: 1rem;
  padding: 0.5rem;
}
.panels {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.summary,
.items {
  padding: 0.6rem 0.5rem;
}
.summary {
  flex: 1;
  border-right: 1px solid #ccc;
  background: #fff;
  min-width: 220px;
  max-width: 410px;
  box-shadow: 2px 0 8px rgba(0,0,0,0.04);
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  /* Remove scroll from summary, let only cart-list-pos scroll */
  overflow: visible;
}
.cart-list-pos {
  background: #fff;
  border-radius: 7px;
  overflow-y: auto;
  margin-bottom: 0.6rem;
  box-shadow: 0 2px 6px rgba(0,0,0,0.04);
  flex-grow: 1;
  min-height: 0;
  font-size: 0.90em !important;
  /* Height: fill all space except for pfand, rabatt, total */
  max-height: calc(100vh - 56px - 0.6rem - 0.6rem - 1.1rem - 1.2rem - 4.4rem - 2.2rem); /* toolbar + paddings + pfand + rabatt + total + margin fudge */
}
@media (min-width: 768px) and (max-width: 1100px) {
  .cart-list-pos {
    font-size: 0.91em;
  }
}
.cart-row-pos {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e3e3e3;
  font-size: 0.81em !important;
  font-weight: 500;
  padding: 0.32rem 0.4rem;
  min-height: 1.25rem;
}
.cart-row-pos:last-child {
  border-bottom: none;
}
.cart-quantity-pos {
  font-weight: bold;
  width: 2.1rem;
  display: inline-block;
  font-size: 0.96em;
}
.cart-name-pos {
  flex: 1;
  margin-left: 0.3rem;
  font-weight: bold;
  font-size: 0.95em;
}
.cart-price-pos {
  width: 3.4rem;
  text-align: right;
  font-weight: normal;
  font-size: 0.92em;
}
.cart-remove-btn {
  margin-left: 0.3rem;
  padding: 0.22rem 0.22rem !important;
  border-radius: 6px !important;
  min-width: 2.6rem;
  min-height: 2.6rem;
  font-size: 1.24em;
}
.cart-total-big {
  text-align: center;
  background: #f4f4f4;
  padding: 0.65rem 0 0.55rem 0;
  border-radius: 7px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
  position: sticky;
  bottom: 0.6rem;
  left: 0;
  width: 100%;
  z-index: 2;
}
.cart-total-label {
  font-size: 0.93rem;
  color: #333;
  letter-spacing: 0.02em;
  margin-bottom: 0.15rem;
}
.cart-total-value {
  font-size: 1.7rem;
  font-weight: bold;
  letter-spacing: 0.01em;
}
.items {
  flex: 2;
  background: #fafbfc;
  min-width: 0;
  overflow-y: auto;
  padding: 0.7rem 0.5rem;
}
.item-grid {
  /* entfernt, damit kein doppeltes Grid entsteht */
}
.item-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  white-space: nowrap;
  min-width: 150px;
  max-width: 210px;
  width: 100%;
  height: 120px;
  font-size: 0.98rem;
  border: 2px solid transparent;
  border-radius: 8px;
  transition: border 0.2s, box-shadow 0.2s;
}
.item-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  white-space: nowrap;
}
.item-meta {
  white-space: nowrap;
}
.color-box {
  width: 1.5rem;
  height: 1rem;
  border: 1px solid #000;
  display: inline-block;
}
@media (max-width: 768px) {
  .panels {
    flex-direction: column;
  }
  .summary {
    border-right: none;
    border-bottom: 1px solid #ccc;
    min-width: unset;
    max-width: unset;
    padding: 0.4rem 0.2rem;
  }
  .items {
    padding: 0.4rem 0.2rem;
  }
}
.discount-btn {
  width: 50%;
  margin-bottom: 0.7rem;
  background: #f8f8f8;
  color: #444;
  border: 1.2px solid #bbb;
  font-weight: bold;
  letter-spacing: 0.02em;
  font-size: 0.97em;
  padding: 0.3rem 0.1rem;
}
.discount-options {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
}
.discount-option-btn {
  min-width: 60px;
  margin-bottom: 0.4rem;
  font-size: 0.96em;
  padding: 0.18rem 0.3rem;
}
.discount-info {
  margin-top: 0.2rem;
  font-size: 0.97rem;
  color: #388e3c;
  font-weight: 500;
}
.pfand-info {
  margin-top: 0.1rem;
  font-size: 0.92rem;
  color: #666;
}
.discount-diff {
  margin-top: 0.08rem;
  font-size: 0.96rem;
  color: #2e7d32;
  font-weight: 500;
}
.cart-pfand-label {
  display: inline-block;
  margin-left: 0.35rem;
  color: #276b28;
  background: #eafbe8;
  border-radius: 5px;
  font-size: 0.93em;
  padding: 0 0.28em;
  font-weight: 400;
}
.return-pfand-block {
  background: #f9f9f9;
  padding: 0.4rem 0.55rem 0.5rem 0.55rem;
  border-radius: 6px;
  margin-bottom: 0.7rem;
  margin-top: 0.5rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.03);
  max-width: 98%;
}
.pfand-compact {
  background: #f7f7f7;
  padding: 0.5rem;
  padding-top: 0;
  padding-bottom: 0;
  border-radius: 7px;
  margin-bottom: 0.7rem;
  margin-top: 0.5rem;
  border: 1px solid #e7e7e7;
  max-width: 99%;
}
.pfand-section-label {
  font-size: 0.93em !important;
  margin-bottom: 0.1rem !important;
  color: #333;
}
/* --- Kompakte Pfandliste --- */
.pfand-list {
  display: flex;
  flex-direction: column;
  gap: 0.04rem;
  margin-top: 0.03rem;
  font-size: 0.85em;
}
.pfand-list-row {
  display: flex;
  align-items: center;
  gap: 2rem;
  padding: 0.04rem 0;
  min-height: 1.2rem;
  font-size: 0.90em;
  line-height: 1.08;
}
.pfand-list-label {
  display: inline-block;
  width: 3.5rem;
  min-width: 2.2rem;
  font-size: 0.87rem;
  color: #444;
  background: #ececec;
  border-radius: 2px;
  text-align: center;
  padding: 0.03rem 0.07rem;
  border: 1px solid #e2e2e2;
  margin-right: 0.03rem;
  line-height: 1.08;
}
/* Sticky header row for pfand list */
.pfand-sticky-row {
  position: sticky;
  top: 0;
  background: #f7f7f7;
  z-index: 1;
  font-weight: bold;
  font-size: 0.91em;
  border-bottom: 1px solid #e2e2e2;
  min-height: 1.2rem;
}
.pfand-list-label-header {
  background: transparent;
  border: none;
  color: #888;
  font-size: 0.81em;
  padding: 0 0.14rem;
  margin-right: 0.04rem;
}
.pfand-list-label-header-anzahl {
  width: 2.9rem;
  min-width: 2.2rem;
  text-align: left;
}


@media (min-width: 768px) and (max-width: 1100px) {
  .calculator {
    height: 100vh;
  }
  .toolbar {
    padding: 1rem 1.2rem;
    gap: 1.1rem;
  }
  .panels {
    flex-direction: row;
    height: calc(100vh - 60px);
    overflow: hidden;
  }
  .summary {
    flex: 1.2;
    min-width: 260px;
    max-width: 390px;
    padding: 0.7rem 0.7rem;
    border-right: 1px solid #ccc;
    box-shadow: 3px 0 10px rgba(0,0,0,0.05);
    font-size: 0.95em;
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 0;
  }
  .items {
    flex: 2.5;
    padding: 0.7rem 0.7rem;
    overflow-y: auto;
    background: #fafbfc;
    font-size: 1.01em;
  }
  .cart-list-pos {
    margin-bottom: 0.7rem;
    flex-grow: 1;
    min-height: 0;
    overflow-y: auto;
    font-size: 0.77em !important;
    max-height: calc(100vh - 60px - 0.7rem - 0.7rem - 0.7rem - 3.5rem - 2.6rem);
  }
  .cart-row-pos {
    padding: 0.41rem 0.5rem;
    font-size: 0.86em !important;
    min-height: 1.2rem;
  }
  .cart-quantity-pos {
    width: 1.7rem;
    font-size: 0.93em;
  }
  .cart-name-pos {
    font-size: 0.93em;
  }
  .cart-price-pos {
    width: 3.1rem;
    font-size: 0.91em;
  }
  /* Delete-Button (Papierkorb) bleibt immer groß, keine kleinere Größe mehr */
  .cart-total-big {
    padding: 0.8rem 0 0.7rem 0;
    font-size: 0.97em;
    position: sticky;
    bottom: 0.7rem;
    left: 0;
    width: 100%;
    z-index: 2;
  }
  .cart-total-label {
    font-size: 0.97em;
  }
  .cart-total-value {
    font-size: 1.45em;
  }
}
@media (max-width: 768px) {
  .cart-list-pos {
    max-height: calc(50vh - 5.5rem);
  }
}

.pfand-counter-btns {
  display: flex;
  align-items: center;
  gap: 0.2rem;
  min-width: 70px;
  justify-content: flex-start;
}
.pfand-counter-value {
  width: 2.2rem;
  display: inline-block;
  text-align: center;
  font-size: 1.03em;
  font-weight: 500;
}
.pfand-btn {
  min-width: 2.6rem;
  min-height: 2.6rem;
  padding: 0.22rem 0.22rem;
  border-radius: 9px;
  font-size: 1.34em;
}

.pfand-rabatt-row {
  display: flex;
  flex-direction: row;
  gap: 0.7rem;
  align-items: flex-start;
  margin-bottom: 0rem;
}
.pfand-rabatt-row .return-pfand-block {
  flex: 2 1 0;
  min-width: 0;
}
.pfand-rabatt-row .discount-btn {
  flex: 1 1 0;
  min-width: 120px;
  height: 100%;
  align-self: stretch;
  margin-top: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1em;
}
@media (max-width: 800px) {
  .pfand-rabatt-row {
    flex-direction: column;
    gap: 0.4rem;
  }
  .pfand-rabatt-row .discount-btn {
    margin-top: 0.2rem;
    max-width: 300px;
    width: 100%;
    height: auto;
  }
}

.pfand-list-row-big {
  min-height: 3.5rem;
  padding: 0.7rem 0;
  gap: 2.6rem;
}

.pfand-counter-btns-big {
  gap: 0.3rem;
}

.pfand-btn-big {
  min-width: 3.5rem;
  min-height: 3.5rem;
  font-size: 2.2em;
  border-radius: 13px;
  padding: 0.2rem 0.2rem;
}
.pfand-counter-value-big {
  font-size: 2em;
  min-width: 2.5rem;
}

.pfand-list-label-big {
  font-size: 1.25rem;
  min-width: 4.6rem;
  padding: 0.15rem 0.2rem;
}

.rabatt-punkte-row {
  display: flex;
  flex-direction: row;
  gap: 0.7rem;
  margin-bottom: 0.3rem;
  margin-top: 0rem;
  width: 100%;
  justify-content: stretch;
}
.rabatt-punkte-row .discount-btn {
  flex: 1 1 0;
  min-width: 100px;
  height: 100%;
  align-self: stretch;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.item-grid-colored {
  display: flex;
  flex-wrap: wrap;
  gap: 2.4rem;
  align-items: flex-start;
  justify-content: flex-start;
  width: 100%;
  overflow-x: visible;
  padding-bottom: 0.3rem;
}
.item-color-column {
  display: flex;
  flex-direction: column;
  gap: 1.1rem;
  min-width: 220px;
  max-width: 220px;
  width: 220px;
  box-sizing: border-box;
  margin-bottom: 2rem;
}
.item-card {
  width: 100%;
}
@media (max-width: 900px) {
  .item-color-column {
    min-width: 90vw;
    max-width: 100vw;
    width: 100%;
  }
  .item-grid-colored {
    gap: 1.1rem;
  }
}

</style>





