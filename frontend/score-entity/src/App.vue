<template>
  <div id="body">
    <router-view/>
  </div>
</template>

<script>
import Scoreboard from "./components/Scoreboard.vue";

export default {
  components: {
    Scoreboard,
  },
};
</script>

<style scoped>


</style>