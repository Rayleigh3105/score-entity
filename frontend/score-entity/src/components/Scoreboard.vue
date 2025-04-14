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
    <div v-if="showBattle" class="fixed inset-0 z-50 font-mono bg-white text-black">
      <div class="absolute inset-0 border-4 border-black bg-white">
        <!-- Gegner Status -->
        <div class="absolute top-[4vh] left-[3vw] w-[50vw] h-[9vh] border-4 border-black p-4">
          <div class="flex justify-between text-xl font-bold">
            <span>{{ battleDefender?.name }}</span>
            <span>{{ defenderHP }}/{{ defenderMaxHP }} HP</span>
          </div>
          <div class="mt-2 text-base">HP</div>
          <div class="w-full h-4 bg-white border border-black">
            <div ref="defenderHpBar" class="h-full bg-green-700"
                 :style="{ width: (defenderHPOverridePercent !== null ? defenderHPOverridePercent : defenderHPPercent) + '%' }"></div>
          </div>
        </div>

        <!-- Gegner Sprite -->
        <div class="absolute top-[8vh] right-[10vw] w-[24vw] h-[24vw]">
          <img v-show="!defenderFainted" ref="defenderBox" :src="battleDefender?.imageUrl" class="w-full h-full object-contain pixelated"/>
        </div>

        <!-- Eigenes Pokémon Sprite -->
        <div class="absolute bottom-[12vh] left-[10vw] w-[24vw] h-[24vw]">
          <img ref="challengerBox" :src="battleChallenger?.imageUrl" class="w-full h-full object-contain pixelated"/>
        </div>

        <!-- Eigen Status -->
        <div class="absolute bottom-[18vh] right-[3vw] w-[50vw] h-[9vh] border-4 border-black p-4">
          <div class="flex justify-between text-xl font-bold">
            <span>{{ battleChallenger?.name }}</span>
            <span>{{ challengerHP }}/{{ challengerMaxHP }} HP</span>
          </div>
          <div class="mt-2 text-base">HP</div>
          <div class="w-full h-4 bg-white border border-black">
            <div class="h-full bg-green-700" :style="{ width: challengerHPPercent + '%' }"></div>
          </div>
        </div>

        <!-- Angriffsanimation -->
        <div v-if="showAttackEffect" class="absolute inset-0 flex items-center justify-end pr-[12vw] z-40 pointer-events-none">
          <div class="w-24 h-24 bg-white border-4 border-black rounded-full animate-tackle"></div>
        </div>

        <!-- Textbox -->
        <div class="absolute bottom-20 left-0 w-full border-t-4 border-black bg-white p-4 text-4xl font-semibold">
          {{ battleText }}
        </div>
      </div>
    </div>
  </div>
  <div v-if="showLeaderChange" class="fixed inset-0 z-50 bg-black text-white flex flex-col items-center justify-center font-mono text-5xl font-bold text-center leading-snug px-8">
    <div>Führungswechsel!</div>
    <div class="mt-4"><span class="text-yellow-300">{{ battleChallenger?.name }}</span> übernimmt die Spitze!</div>
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
    };
  },
  computed: {
    challengerHPPercent() {
      return (this.challengerHP / this.challengerMaxHP) * 100;
    },
    defenderHPPercent() {
      return (this.defenderHP / this.defenderMaxHP) * 100;
    }
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

        const previousFirst = previousGroups[0];
        const currentFirst = newGroups[0];

        if (previousFirst && currentFirst && previousFirst.groupId !== currentFirst.groupId) {
          const challengerBefore = previousGroups.find(g => g.groupId === currentFirst.groupId);
          if (challengerBefore) {
            this.introPhase = true;
            this.introText = `<span class="text-yellow-300">${currentFirst.name}</span> fordert <span class="text-red-400">${previousFirst.name}</span> heraus!`;

            setTimeout(() => {
              this.introText = '';
              this.showPokeball = true;

              setTimeout(() => {
                this.showPokeball = false;
                this.introPhase = false;
                this.battleChallenger = currentFirst;
                this.battleDefender = previousFirst;
                this.battlePhase = 0;
                this.showBattle = true;

                this.$nextTick(() => {
                  // Setze Anfangszustand der Sprites
                  gsap.set(this.$refs.challengerBox, {x: "-100vw", opacity: 0});
                  gsap.set(this.$refs.defenderBox, {x: "100vw", opacity: 0});

                  // Animation starten nach kurzer Verzögerung
                  gsap.to(this.$refs.challengerBox, {x: 0, opacity: 1, duration: 1});
                  gsap.to(this.$refs.defenderBox, {
                    x: 0, opacity: 1, duration: 1, onComplete: () => {
                      this.battleText = `${this.battleChallenger?.name} betritt das Festgelände!`;
                      setTimeout(() => {
                        this.advanceBattle();
                      }, 3000); // Schnellere Einleitung der Tackle-Phase
                    }
                  });
                });
              }, 2000);
            }, 5000);
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
    advanceBattle() {
      const phases = [
        `${this.battleChallenger?.name} setzt Schelle ein!`,
        `${this.battleDefender?.name} ist kampfunfähig!`,
        `${this.battleChallenger?.name} gewinnt den Kampf!`,
      ];

      this.battleText = phases[this.battlePhase];
      if (this.battlePhase === 0) {
        setTimeout(() => this.advanceBattle(), 4000);
      }

      if (this.battlePhase === 1) {
        const attack = this.frankenAttacks[Math.floor(Math.random() * this.frankenAttacks.length)];
        this.battleText = `${this.battleChallenger?.name} setzt ${attack} ein!`;

        gsap.to(this.$refs.challengerBox, {
          x: "-5vw",
          duration: 0.4,
          ease: "power2.in",
          onComplete: () => {
            gsap.to(this.$refs.challengerBox, {
              x: "0vw",
              duration: 0.6,
              ease: "power2.out"
            });

            setTimeout(() => {
              gsap.fromTo(
                  this.$refs.defenderBox,
                  {opacity: 0},
                  {
                    opacity: 1,
                    duration: 0.15,
                    repeat: 6,
                    yoyo: true
                  }
              );
            }, 500); // Flackerverzögerung nach dem Stoß

            setTimeout(() => this.advanceBattle(), 3000); // Zeit nach Tackle bis Phase 2
          }
        });
      } else if (this.battlePhase === 2) {
        const healthBar = this.$refs.defenderHpBar;
        if (healthBar) {
          gsap.to(this.$data, {
            defenderHPOverridePercent: 0,
            duration: 2,
            ease: "power2.out",
            onUpdate: () => {
              if (this.defenderHPOverridePercent < 50 && healthBar) {
                healthBar.style.backgroundColor = "#FFA500"; // orange ab 50%
              }
              if (this.defenderHPOverridePercent < 25 && healthBar) {
                healthBar.style.backgroundColor = "#8B0000"; // rot ab 25%
              }
            },
            onComplete: () => {
              gsap.to(this.$refs.defenderBox, {
                y: 100,
                opacity: 0,
                duration: 1.5,
                ease: "power2.in",
                onComplete: () => {
                  this.defenderFainted = true;
                  setTimeout(() => this.advanceBattle(), 1000);
                }
              });
            }
          });
        }
      } else if (this.battlePhase === 3) {
        gsap.to(this.$refs.challengerBox, {
          x: 0,
          duration: 1,
          onComplete: () => {
            this.showBattle = false;
            this.showLeaderChange = true;
            setTimeout(() => {
              this.showLeaderChange = false;
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
            }, 4000);
          }
        });
      }

      this.battlePhase++;
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

.animate-fade-in {
  animation: fade-in 0.5s ease-out;
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

.animate-tackle {
  animation: tackle 1.5s ease-out; /* Verlangsame die Tackle-Animation */
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

.pixel-mask {
  width: 100vw;
  height: 100vh;
  background: black;
  clip-path: circle(150% at 50% 50%);
  animation: pixelCircleClose 1s ease-in-out forwards;
  image-rendering: pixelated;
}

.pixelated {
  image-rendering: pixelated;
}
</style>
