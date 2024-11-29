<template>
  <div class="min-h-screen flex flex-row bg-gray-100">
    <!-- Seitenleiste -->
    <Sidebar />

    <!-- Hauptinhalt -->
    <div class="flex-1 p-6">
      <router-view />
    </div>
  </div>
</template>
<script setup lang="ts">
import Sidebar from "@/components/admin/Sidebar.vue";
</script>