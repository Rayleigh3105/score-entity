<template>
  <div id="app" class="min-h-screen bg-white">
    <div v-if="introPhase" class="fixed inset-0 z-50 bg-black flex items-center justify-center">
    </div>
    <div v-if="showPokeball" class="pokeball-container">
      <div class="pokeball">
        <div class="top-half"></div>
        <div class="bottom-half"></div>
        <div class="button"></div>
      </div>
    </div>
    <div v-if="introText && !showPokeball"
         class="absolute top-1/3 left-1/2 transform -translate-x-1/2 bg-black bg-opacity-80 px-6 py-4 rounded-lg shadow-lg text-white text-4xl font-bold z-50 text-center font-mono leading-snug">
      <span v-html="introText"></span>
    </div>
    <div class="w-full flex flex-col items-center pt-4 px-4">
      <div class="flex flex-col md:flex-row justify-center gap-4 w-full max-w-6xl">
        <div v-if="globalCountdown"
             class="flex-1 bg-red-100 border-4 border-red-800 text-red-900 text-4xl font-bold font-mono px-8 py-4 rounded-lg shadow-lg text-center animate-pulse h-full flex items-center justify-center">
          ⏳ Ende in: {{ globalCountdown }}
        </div>
        <div v-if="price"
             class="flex-1 bg-yellow-50 border-4 border-yellow-300 text-yellow-800 px-8 py-4 rounded-lg shadow-lg text-lg leading-relaxed prose h-full overflow-auto">
          <div v-html="price"></div>
        </div>
      </div>
    </div>
    <div ref="groupList" class="w-full px-8 pt-6 flex flex-col gap-4">
      <div
          v-for="(group, index) in groups"
          :key="group.groupId"
          :data-id="group.groupId"
          class="flex items-center justify-between bg-gray-100 rounded-xl shadow-md p-4 border border-gray-300"
      >
        <!-- Platzierung -->
        <div class="text-2xl font-extrabold text-gray-700 w-12 text-center">
          {{ index + 1 }}.
        </div>

        <!-- Logo + Name -->
        <div class="flex items-center gap-4 flex-1">
          <img
              v-if="group.imageUrl "
              class="w-14 h-14 rounded-full object-cover border-2 border-white shadow"
              :src="group.imageUrl || placeholder"
              alt="Gruppenbild"
          />
          <div>
            <p class="text-xl font-bold text-gray-900 truncate">{{ group.name }}</p>
          </div>
        </div>

        <!-- Punkte -->
        <div class="text-3xl font-bold text-green-600">
          {{ group.totalScore }}
        </div>
      </div>
    </div>

    <!-- Pokémon Kampf Overlay -->
    <BattleOverlay
        v-if="showBattle"
        :challenger="battleChallenger"
        :defender="battleDefender"
        :defender-fainted="defenderFainted"
        :defender-hp="defenderHP"
        :defender-max-hp="defenderMaxHP"
        :defender-hp-override-percent="defenderHPOverridePercent"
        :challenger-hp="challengerHP"
        :challenger-max-hp="challengerMaxHP"
        :battle-text="battleText"
        :show-attack-effect="showAttackEffect"
        @battle-finished="onBattleFinished"
        ref="battleOverlay"
    />
  </div>
  <div v-if="showLeaderChange"
       class="fixed inset-0 z-50 bg-black text-white flex flex-col items-center justify-center font-mono text-5xl font-bold text-center leading-snug px-8">
    <div>Führungswechsel!</div>
    <div class="mt-4"><span class="text-yellow-300">{{ battleChallenger?.name }}</span> übernimmt die Spitze!</div>
  </div>
  <div v-if="rocketCountdown !== null" class="fixed inset-0 z-50 bg-black bg-opacity-75 flex items-center justify-center text-white text-9xl font-bold">
    {{ rocketCountdown }}
  </div>
  <div v-if="showRocketRace" class="fixed inset-0 z-50 bg-black bg-opacity-75 flex justify-around items-end overflow-hidden">
    <div
        v-for="(participant, index) in [battleChallenger, battleDefender]"
        :key="index"
        :class="[
        'flex flex-col items-center mb-10 rocket-wrapper',
        'rocket-launch-' + index,
        { 'winner': index === 0 }
      ]"
    >
      <img
          :src="participant?.imageUrl"
          alt="Teilnehmer"
          class="w-16 h-16 rounded-full border-2 border-white shadow mb-2 object-cover"
      />
      <div class="css-rocket">
        <div class="rocket-head"></div>
        <div class="rocket-body"></div>
        <div class="rocket-fins"></div>
        <div class="rocket-flame"></div>
      </div>
      <div class="mt-2 text-lg font-bold text-center text-white">{{ participant?.name }}</div>
      <div v-if="index === 0" class="mt-4 explosion"></div>
    </div>
  </div>
</template>

<script>
import gsap from "gsap";
import {settingsApi} from "@/router/api.custom";
import BattleOverlay from "@/components/BattleOverlay.vue";

export default {
  name: "Scoreboard",
  components: {
    BattleOverlay,
  },
  data() {
    return {
      groups: [], // Aktuelle Liste der Gruppen
      placeholder: "https://via.placeholder.com/50", // Standardbild
      eventSource: null, // SSE-Verbindung
      showBattle: false,
      showPokeball: false,
      battleChallenger: null,
      battleDefender: null,
      battlePhase: 0,
      battleText: "",
      challengerHP: 100, // Beispielwert
      challengerMaxHP: 100, // Beispielwert
      defenderHP: 100, // Beispielwert
      defenderMaxHP: 100, // Beispielwert
      introPhase: false, // Steuerung für die Intro-Phase
      introText: '', // Neue Datenvariable
      showAttackEffect: false, // Neue Datenvariable für Angriffsanimation
      defenderFainted: false, // Neue Datenvariable für besiegten Verteidiger
      defenderHPOverridePercent: null,
      showLeaderChange: false,
      showRocketRace: false,
      rocketCountdown: null,
      endTime: null,
      globalCountdown: null,
      price: "",
      frankenAttacks: [
        "Broudwoschdschlag",
        "Kellerdapp",
        "Gulblousn",
        "Schnuffzer",
        "Zammdrescher",
        "Bocksn",
        "Gleißhammer",
        "Gsichtsgrabscher",
      ],
      animationQueue: [],
      animationInProgress: false,
    };
  },
  mounted() {
    this.initializeSSE();
    this.loadSettings();
  },
  beforeUnmount() {
    if (this.eventSource) {
      this.eventSource.close(); // SSE-Verbindung schließen
    }
  },
  methods: {
    loadSettings() {
      settingsApi.settingsIdGet(1).then((response) => {
        if (response.status === 200) {
          this.endTime = response.data.endTime;
          this.price = response.data.price;
          this.startGlobalCountdown();
        }
      }).catch((error) => {
        console.error("Fehler beim Laden der Einstellungen:", error);
      });
    },
    initializeSSE() {
      // SSE-Verbindung herstellen
      this.eventSource = new EventSource("/scoreboard-stream");

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
        setTimeout(() => this.initializeSSE(), 5000);
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

        const previousFirst = previousGroups[0];
        const currentFirst = newGroups[0];

        // Prüfung auf Führungswechsel
        if (previousFirst && currentFirst && previousFirst.groupId !== currentFirst.groupId) {
          const challengerBefore = previousGroups.find(g => g.groupId === currentFirst.groupId);
          if (challengerBefore) {
            // Führungswechsel-Animation in die Queue einreihen
            this.queueLeaderChangeAnimation(currentFirst, previousFirst);
          }
        }

        // Vergleiche Gruppen und animiere Punktegewinne oder Positionswechsel
        newGroups.forEach((newGroup) => {
          const oldGroup = previousGroups.find(
              (g) => g.groupId === newGroup.groupId
          );
          const node = Array.from(groupList.children).find(
              (el) => el.getAttribute("data-id") === String(newGroup.groupId)
          );

          if (node) {
            // Punktegewinn-Animation mit visueller Rückmeldung
            if (oldGroup && oldGroup.totalScore !== newGroup.totalScore) {
              const isGain = newGroup.totalScore > oldGroup.totalScore;
              const highlightColor = isGain ? "#bbf7d0" : "#fecaca"; // tailwind: green-100 / red-100
              const originalBg = node.style.backgroundColor;

              gsap.fromTo(
                  node,
                  {
                    scale: 1,
                    backgroundColor: highlightColor
                  },
                  {
                    scale: 1.1,
                    backgroundColor: "",
                    duration: 0.4,
                    ease: "power1.inOut",
                    yoyo: true,
                    repeat: 1,
                    onComplete: () => {
                      node.style.backgroundColor = originalBg;
                    }
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
                      opacity: 0.4,
                      scale: 0.95,
                      zIndex: 10,
                    },
                    {
                      x: 0,
                      y: 0,
                      opacity: 1,
                      scale: 1,
                      zIndex: 1,
                      duration: 0.8,
                      ease: "power2.out"
                    }
                );
              }
            }
          }
        });
      });
    },
    // Neue Methode zum Einreihen von Führungswechsel-Animationen
    queueLeaderChangeAnimation(challenger, defender) {
      const animationTask = async () => {
        // Gemeinsame Daten setzen
        this.battleChallenger = challenger;
        this.battleDefender = defender;
        this.battlePhase = 0;

        this.introPhase = true;
        this.introText = `<span class="text-yellow-300">${challenger.name}</span> fordert <span class="text-red-400">${defender.name}</span> heraus!`;

        // 5 Sekunden warten für Intro-Text
        await this.sleep(5000);

        this.introText = '';

        // Zufällige Auswahl der Animation
        const animations = ["battle", "rocket"];
        let selected = animations[Math.floor(Math.random() * animations.length)];

        if (selected === "battle") {
          this.showPokeball = true;
          await this.sleep(2000);
          this.showPokeball = false;
          this.introPhase = false;
          this.showBattle = true;
          // Warten bis Battle beendet ist - wird durch onBattleFinished() fortgesetzt
        } else if (selected === "rocket") {
          // Rocket-Animation komplett abwickeln
          this.rocketCountdown = 3;
          await new Promise(resolve => {
            const countdownInterval = setInterval(() => {
              if (this.rocketCountdown > 1) {
                this.rocketCountdown--;
              } else {
                clearInterval(countdownInterval);
                this.rocketCountdown = null;
                resolve();
              }
            }, 1000);
          });

          this.showRocketRace = true;
          await this.sleep(5000);
          this.showRocketRace = false;
          this.introPhase = false;

          // Führungswechsel anzeigen
          this.showLeaderChange = true;
          await this.sleep(4000);
          this.showLeaderChange = false;

          // Animation beenden
          this.resetBattleState();
          this.finishAnimation();
        }
      };

      // In Queue einreihen oder direkt starten
      if (this.animationInProgress) {
        this.animationQueue.push(animationTask);
      } else {
        this.animationInProgress = true;
        animationTask();
      }
    },
    startGlobalCountdown() {
      if (!this.endTime) return;

      const update = () => {
        const now = new Date();
        const end = new Date(this.endTime);
        const diff = end.getTime() - now.getTime();
        if (diff <= 0) {
          this.globalCountdown = "0h 0m 0s";
          return;
        }
        const seconds = Math.floor(diff / 1000) % 60;
        const minutes = Math.floor(diff / 60000) % 60;
        const hours = Math.floor(diff / 3600000);
        this.globalCountdown = `${hours}h ${minutes}m ${seconds}s`;
      };

      update(); // call once immediately
      setInterval(update, 1000); // update every second
    },
    async onBattleFinished() {
      // Führungswechsel anzeigen
      this.showLeaderChange = true;
      await this.sleep(4000);
      this.showLeaderChange = false;

      // Battle-State zurücksetzen
      this.resetBattleState();

      // Animation als beendet markieren
      this.finishAnimation();
    },
    // Neue Hilfsmethode für Animation-Ende
    finishAnimation() {
      this.animationInProgress = false;
      this.runNextAnimation();
    },
    runNextAnimation() {
      if (this.animationInProgress) return;
      if (this.animationQueue.length === 0) return;

      const next = this.animationQueue.shift();
      this.animationInProgress = true;
      next();
    },
    resetBattleState() {
      this.showBattle = false;
      this.battleChallenger = null;
      this.battleDefender = null;
      this.battleText = '';
      this.battlePhase = 0;
      this.challengerHP = 100;
      this.challengerMaxHP = 100;
      this.defenderHP = 100;
      this.defenderMaxHP = 100;
      this.defenderFainted = false;
      this.defenderHPOverridePercent = null;
    },
    // Hilfsmethode für async sleep
    sleep(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
    },
  },
};
</script>

<style scoped>
@keyframes fade-in {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.animate-pulse {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes tackle {
  0% {
    transform: translate(0, 0) scale(1);
    opacity: 1;
  }
  10% {
    transform: translate(-20px, -10px) scale(1.1);
  }
  20% {
    transform: translate(20px, 10px) scale(1);
  }
  30% {
    transform: translate(-15px, -5px) scale(1.1);
  }
  40% {
    transform: translate(15px, 5px) scale(1);
  }
  50% {
    transform: translate(-10px, 0px) scale(1.1);
  }
  60% {
    transform: translate(10px, 0px) scale(1);
  }
  70% {
    transform: translate(0px, 0px) scale(1.2);
    opacity: 1;
  }
  80% {
    transform: scale(1.5);
    opacity: 0.8;
  }
  100% {
    transform: scale(2);
    opacity: 0;
  }
}

.pokeball-container {
  position: absolute;
  z-index: 100;
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: black;
}

.pokeball {
  width: 100vw;
  height: 100vw;
  position: relative;
  animation: pop 1s ease-in-out forwards;
}

.top-half, .bottom-half {
  position: absolute;
  width: 100%;
  height: 50%;
  left: 0;
}

.top-half {
  top: 0;
  background: red;
  border-top-left-radius: 50vw;
  border-top-right-radius: 50vw;
}

.bottom-half {
  bottom: 0;
  background: white;
  border-bottom-left-radius: 50vw;
  border-bottom-right-radius: 50vw;
  border-top: 1vw solid black;
}

.button {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 10vw;
  height: 10vw;
  background: white;
  border: 1vw solid black;
  border-radius: 50%;
  z-index: 2;
}

@keyframes pop {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

/* Rocket Launch Animations */
.rocket-launch-0 {
  animation: rocket-launch-0 6s ease-in-out forwards;
}

.rocket-launch-1 {
  animation: rocket-launch-1 8s ease-in-out forwards;
}

@keyframes rocket-launch-0 {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(-120vh);
  }
}

@keyframes rocket-launch-1 {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(-120vh);
  }
}

.rocket-wrapper {
  animation-fill-mode: forwards;
  animation-timing-function: ease-in-out;
}

.explosion {
  width: 60px;
  height: 60px;
  background: radial-gradient(circle, red, orange, yellow);
  border-radius: 50%;
  animation: boom 0.8s ease-out;
  margin-top: 10px;
}

@keyframes boom {
  0% {
    transform: scale(0.5);
    opacity: 0.5;
  }
  100% {
    transform: scale(2.5);
    opacity: 0;
  }
}

.winner {
  z-index: 10;
  filter: drop-shadow(0 0 20px yellow);
}

.css-rocket {
  position: relative;
  width: 24px;
  height: 60px;
  background: linear-gradient(to bottom, #ccc 30%, #f00 30% 70%, #ccc 70%);
  border-radius: 12px;
  margin: 0 auto;
}

.rocket-head {
  position: absolute;
  top: -12px;
  left: 0;
  width: 24px;
  height: 24px;
  background: white;
  border-radius: 50% 50% 0 0;
  border: 2px solid black;
}

.rocket-body {
  position: absolute;
  top: 12px;
  left: 4px;
  width: 16px;
  height: 30px;
  background: white;
  border: 2px solid black;
}

.rocket-fins {
  position: absolute;
  bottom: 8px;
  left: -6px;
  width: 36px;
  height: 12px;
  background: red;
  border-radius: 4px;
  z-index: -1;
}

.rocket-flame {
  position: absolute;
  bottom: -12px;
  left: 6px;
  width: 12px;
  height: 20px;
  background: radial-gradient(ellipse at center, #ffd700 0%, #ff4500 80%);
  border-radius: 50%;
  animation: flicker 0.3s infinite;
}

@keyframes flicker {
  0% {
    opacity: 1;
    transform: scaleY(1) scaleX(1);
  }
  30% {
    opacity: 0.9;
    transform: scaleY(1.1) scaleX(0.95);
  }
  50% {
    opacity: 0.8;
    transform: scaleY(1.2) scaleX(1.1);
  }
  70% {
    opacity: 1;
    transform: scaleY(1) scaleX(1);
  }
  100% {
    opacity: 0.9;
    transform: scaleY(1.05) scaleX(1);
  }
}
</style>