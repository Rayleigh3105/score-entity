<template>
  <div class="min-h-screen flex flex-row bg-gray-100 relative">
    <!-- Kebab-Menü (nur sichtbar auf kleinen Bildschirmen) -->
    <div class="absolute top-4 left-4 lg:hidden">
      <button
          @click="toggleMenu"
          class="p-2 rounded-full bg-white shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      >
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6.75 12a.75.75 0 100 1.5h10.5a.75.75 0 000-1.5H6.75zm0-4.5a.75.75.75 0 100 1.5h10.5a.75.75 0 000-1.5H6.75zm0 9a.75.75.75 0 100 1.5h10.5a.75.75 0 000-1.5H6.75z" />
        </svg>
      </button>
    </div>

    <!-- Seitenleiste -->
    <Sidebar v-if="menuOpen || isLargeScreen" class="lg:flex" />

    <!-- Hauptinhalt -->
    <div class="flex-1 p-5" :class="!isLargeScreen ? 'mt-14' : ''" style="height: calc(100vh - 90px);">
      <router-view class="h-full" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';
import Sidebar from '@/components/admin/Sidebar.vue';

// Zustand für das Menü
const menuOpen = ref(false);

// Erkennen der Bildschirmgröße
const isLargeScreen = ref(false);

// Überwachung der Bildschirmgröße
const updateScreenSize = () => {
  isLargeScreen.value = window.innerWidth >= 1024; // 1024px ist der Breakpoint für 'lg'
};

// Event-Listener für Resize
onMounted(() => {
  updateScreenSize();
  window.addEventListener('resize', updateScreenSize);
});

onUnmounted(() => {
  window.removeEventListener('resize', updateScreenSize);
});

// Menü-Umschaltung
const toggleMenu = () => {
  menuOpen.value = !menuOpen.value;
};
</script>

<style scoped>
/* Optional: Zusätzliche Stile für Übergänge */
</style>