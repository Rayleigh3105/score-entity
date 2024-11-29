<template>
  <div class="flex flex-col w-56 bg-white rounded-r-3xl overflow-hidden">
    <div class="flex items-center justify-center h-20 shadow-md">
      <h1 class="text-3xl text-green-500">Admin</h1>
    </div>
    <ul class="flex flex-col py-4">
      <li>
        <router-link
            to="/admin/teams"
            class="flex flex-row items-center h-12 transform hover:translate-x-2 transition-transform ease-in duration-200 text-gray-500 hover:text-gray-800"
        >
          <span class="inline-flex items-center justify-center h-12 w-12 text-lg text-gray-400"><i
              class="bx bx-group"></i></span>
          <span class="text-sm font-medium">Teams</span>
        </router-link>
      </li>
      <li>
        <router-link
            to="/admin/entity"
            class="flex flex-row items-center h-12 transform hover:translate-x-2 transition-transform ease-in duration-200 text-gray-500 hover:text-gray-800"
        >
          <span class="inline-flex items-center justify-center h-12 w-12 text-lg text-gray-400"><i
              class="bx bx-grid"></i></span>
          <span class="text-sm font-medium">Entitäten</span>
        </router-link>
      </li>
      <li>
        <router-link
            to="/admin/event"
            class="flex flex-row items-center h-12 transform hover:translate-x-2 transition-transform ease-in duration-200 text-gray-500 hover:text-gray-800"
        >
          <span class="inline-flex items-center justify-center h-12 w-12 text-lg text-gray-400"><i
              class="bx bx-calendar-event"></i></span>
          <span class="text-sm font-medium">Events</span>
        </router-link>
      </li>
      <li>
        <router-link
            to="/admin/settings"
            class="flex flex-row items-center h-12 transform hover:translate-x-2 transition-transform ease-in duration-200 text-gray-500 hover:text-gray-800"
        >
          <span class="inline-flex items-center justify-center h-12 w-12 text-lg text-gray-400"><i
              class="bx bx-cog"></i></span>
          <span class="text-sm font-medium">Einstellungen</span>
        </router-link>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
</script>