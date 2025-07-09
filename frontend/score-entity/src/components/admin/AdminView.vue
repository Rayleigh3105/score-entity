<script setup lang="ts">
import AdminSidebar from './AdminSidebar.vue';
</script>

<template>
  <div class="min-h-screen flex flex-col bg-gray-100">
    <!-- Horizontales Menü oberhalb -->
    <AdminSidebar/>

    <!-- Hauptinhalt -->
    <div class="flex-1 p-5">
      <router-view class="h-full" />
    </div>
  </div>
</template>

<style scoped>
/* Optional: Zusätzliche Stile für Übergänge */
</style>