<template>
  <nav class="bg-white shadow-md">
    <div class="max-w-screen-xl mx-auto px-4 flex items-center justify-between py-4">
      <!-- Navigationslinks -->
      <ul class="flex flex-col items-center md:flex-row md:items-center justify-center md:space-x-6 space-y-4 md:space-y-0 w-full">
        <li>
          <router-link
            to="/admin/central"
            class="flex items-center px-3 py-2 text-gray-500 hover:text-gray-800 transition"
          >
            <i class="bx bx-home mr-2 text-lg text-gray-400"></i>
            <span class="text-sm font-medium">Punkteverteilung</span>
          </router-link>
        </li>
        <li>
          <router-link
            to="/admin/teams"
            class="flex items-center px-3 py-2 text-gray-500 hover:text-gray-800 transition"
          >
            <i class="bx bx-group mr-2 text-lg text-gray-400"></i>
            <span class="text-sm font-medium">Teams/Gruppen</span>
          </router-link>
        </li>
        <li>
          <router-link
            to="/admin/settings"
            class="flex items-center px-3 py-2 text-gray-500 hover:text-gray-800 transition"
          >
            <i class="bx bx-cog mr-2 text-lg text-gray-400"></i>
            <span class="text-sm font-medium">Einstellungen</span>
          </router-link>
        </li>
      </ul>
    </div>
  </nav>
</template>