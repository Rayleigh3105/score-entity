<template>
  <div id="app" class="min-h-screen bg-white">
    <ul ref="groupList" role="list" class="divide-y divide-gray-100 w-full">
      <li
          v-for="(group, index) in groups"
          :key="group.groupId"
          class="flex justify-between items-center py-4 px-4"
          :data-id="group.groupId"
      >
        <!-- Nummerierung -->
        <div class="text-lg font-bold text-gray-900 w-12 text-left">
          {{ index + 1 }}
        </div>
        <!-- Bild und Name -->
        <div class="flex items-center flex-1 gap-x-4">
          <img
              class="w-12 h-12 rounded-full bg-gray-50"
              :src="group.imageUrl || placeholder"
              alt="Gruppenbild"
          />
          <div class="truncate">
            <p class="text-lg font-semibold text-gray-900 truncate">
              {{ group.name }}
            </p>
          </div>
        </div>
        <!-- Punkte -->
        <div class="text-lg font-semibold text-gray-900">
          {{ group.totalScore }}
        </div>
      </li>
    </ul>
  </div>
</template>

<script>
import gsap from "gsap";

export default {
  name: "Scoreboard",
  data() {
    return {
      groups: [], // Aktuelle Liste der Gruppen
      placeholder: "https://via.placeholder.com/50", // Standardbild
      eventSource: null, // SSE-Verbindung
    };
  },
  mounted() {
    this.initializeSSE();
  },
  beforeUnmount() {
    if (this.eventSource) {
      this.eventSource.close(); // SSE-Verbindung schließen
    }
  },
  methods: {
    initializeSSE() {
      // SSE-Verbindung herstellen
      this.eventSource = new EventSource("http://localhost:8080/scoreboard-stream");

      this.eventSource.onmessage = (event) => {
        try {
          const newGroups = JSON.parse(event.data); // Neue Daten von SSE
          this.animateChanges(newGroups);
        } catch (error) {
          console.error("Fehler beim Parsen der SSE-Daten:", error);
        }
      };

      this.eventSource.onerror = () => {
        console.error("SSE-Verbindung fehlgeschlagen");
        this.eventSource.close();
      };
    },
    animateChanges(newGroups) {
      const groupList = this.$refs.groupList;

      // Überprüfung, ob groupList verfügbar ist
      if (!groupList) {
        console.warn("groupList-Referenz ist nicht definiert.");
        return;
      }

      const oldPositions = new Map();

      // Alte Positionen speichern
      Array.from(groupList.children).forEach((node) => {
        const id = node.getAttribute("data-id");
        if (id) {
          oldPositions.set(id, node.getBoundingClientRect());
        }
      });

      const previousGroups = this.groups;
      this.groups = newGroups;

      // Nach DOM-Update animieren
      this.$nextTick(() => {
        const newPositions = new Map();
        Array.from(groupList.children).forEach((node) => {
          const id = node.getAttribute("data-id");
          if (id) {
            newPositions.set(id, node.getBoundingClientRect());
          }
        });

        // Vergleiche Gruppen und animiere Punktegewinne oder Positionswechsel
        newGroups.forEach((newGroup) => {
          const oldGroup = previousGroups.find(
              (g) => g.groupId === newGroup.groupId
          );
          const node = Array.from(groupList.children).find(
              (el) => el.getAttribute("data-id") === String(newGroup.groupId)
          );

          if (node) {
            // Punktegewinn-Animation
            if (oldGroup && oldGroup.totalScore < newGroup.totalScore) {
              gsap.fromTo(
                  node,
                  { scale: 1 }, // Ausgangszustand
                  {
                    scale: 1.2, // Vergrößerung
                    duration: 0.3,
                    ease: "power1.inOut",
                    yoyo: true, // Zurück zur normalen Größe
                    repeat: 1, // Einmal hin und zurück
                  }
              );
            }

            // Positionswechsel-Animation
            const oldPos = oldPositions.get(String(newGroup.groupId));
            const newPos = newPositions.get(String(newGroup.groupId));

            if (oldPos && newPos) {
              const deltaX = oldPos.left - newPos.left;
              const deltaY = oldPos.top - newPos.top;

              if (deltaX !== 0 || deltaY !== 0) {
                gsap.fromTo(
                    node,
                    {
                      x: deltaX,
                      y: deltaY,
                      zIndex: 10, // In den Vordergrund bringen
                    },
                    {
                      x: 0,
                      y: 0,
                      zIndex: 1,
                      duration: 1, // Dauer der Animation
                      ease: "power2.inOut", // Weiche Übergänge
                    }
                );
              }
            }
          }
        });
      });
    },
  },
};
</script>



<style scoped>

</style>
