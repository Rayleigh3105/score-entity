<script setup lang="ts">
import BasicSidebar from "@/components/basic/BasicSidebar.vue";
</script>

<template>
  <div class="min-h-screen flex flex-col bg-gray-100 overflow-hidden">
    <!-- Fixierte obere Leiste -->
    <BasicSidebar/>

    <!-- Hauptinhalt soll innerhalb des verfügbaren Bereichs scrollen -->
    <div class="flex-1 min-h-0 p-5 overflow-auto main-content">
      <router-view class="h-full" />
    </div>
  </div>
</template>

<style scoped>
.main-content {
  height: calc(100vh - 64px); /* Passe 64px ggf. auf die tatsächliche Höhe deiner Leiste an */
  overflow: auto;
}
</style>