<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-90 p-4">
    <div
        class="relative w-full max-w-3xl aspect-[4/3] bg-white border-4 border-black overflow-hidden font-mono text-black">
      <div class="relative w-full h-full flex flex-col justify-between items-stretch">
        <!-- Gegner Sprite -->
        <img
            v-show="!defenderFainted"
            ref="defenderBox"
            :src="defender?.imageUrl"
            class="absolute top-8 right-8 w-28 h-28 md:w-36 md:h-36 object-contain pixelated z-10"
        />

        <!-- Gegner Lebensleiste -->
        <div class="absolute top-8 left-8 w-[40%] max-w-md border-4 border-black p-4 bg-white z-20">
          <div class="flex justify-between text-xl font-bold">
            <span>{{ defender?.name }}</span>
            <span>{{ defenderHp }}/{{ defenderMaxHp }} HP</span>
          </div>
          <div class="mt-2 text-base">HP</div>
          <div class="w-full h-4 bg-white border border-black">
            <div
                ref="defenderHpBar"
                class="h-full bg-green-700"
                :style="{ width: animatedHpPercent + '%' }"
            ></div>
          </div>
        </div>

        <!-- Herausforderer Sprite -->
        <img
            ref="challengerBox"
            :src="challenger?.imageUrl"
            class="absolute bottom-32 left-8 w-28 h-28 md:w-36 md:h-36 object-contain pixelated z-10"
        />

        <!-- Herausforderer Lebensleiste -->
        <div class="absolute bottom-32 right-8 w-[40%] max-w-md border-4 border-black p-4 bg-white z-20">
          <div class="flex justify-between text-xl font-bold">
            <span>{{ challenger?.name }}</span>
            <span>{{ challengerHp }}/{{ challengerMaxHp }} HP</span>
          </div>
          <div class="mt-2 text-base">HP</div>
          <div class="w-full h-4 bg-white border border-black">
            <div
                class="h-full bg-green-700"
                :style="{ width: (challengerHp / challengerMaxHp * 100) + '%' }"
            ></div>
          </div>
        </div>

        <!-- Angriffseffekt -->
        <div
            v-if="showAttackEffect"
            class="pointer-events-none flex items-center justify-end pr-12 md:pr-32 z-40"
            style="position: absolute; inset: 0;"
        >
          <div class="w-24 h-24 bg-white border-4 border-black rounded-full animate-tackle"></div>
        </div>

        <!-- Textbox -->
        <div
            class="absolute bottom-0 left-0 w-full border-t-4 border-black bg-white p-4 text-xl md:text-3xl font-semibold text-center z-30">
          {{ battleText }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import gsap from "gsap";

export default {
  name: "BattleOverlay",
  props: {
    challenger: Object,
    defender: Object,
    defenderFainted: Boolean,
    defenderHp: Number,
    defenderMaxHp: Number,
    defenderHpOverridePercent: Number,
    challengerHp: Number,
    challengerMaxHp: Number,
    battleText: String,
    showAttackEffect: Boolean,
  },
  emits: ["battle-finished"],
  data() {
    return {
      battlePhase: 0,
      frankenAttacks: ["Durstlöscher", "Pegelpeitsche", "Gsichtsgrabscher", "Bierbombe", "Schnapssturm", "Katerschlag", "Kotzgewitter"],
      localBattleText: "",
      animatedHpPercent: 100,
    };
  },
  mounted() {
    this.advanceBattle();
  },
  methods: {
    advanceBattle() {
      const attack = this.frankenAttacks[Math.floor(Math.random() * this.frankenAttacks.length)];
      const phases = [
        `${this.challenger?.name} betritt das Festgelände!`,
        `${this.challenger?.name} setzt ${attack} ein!`,
        `${this.defender?.name} ist kampfunfähig!`,
        `${this.challenger?.name} gewinnt den Kampf!`,
      ];
      this.localBattleText = phases[this.battlePhase];

      if (this.battlePhase === 0) {
        // Animations for challenger and defender entering
        gsap.fromTo(this.$refs.challengerBox, { x: "-100vw", opacity: 0 }, {
          x: 0,
          opacity: 1,
          duration: 1,
          ease: "power2.out"
        });
        gsap.fromTo(this.$refs.defenderBox, { x: "100vw", opacity: 0 }, {
          x: 0,
          opacity: 1,
          duration: 1,
          ease: "power2.out",
          onComplete: () => {
            setTimeout(() => this.advanceBattle(), 2000);
          }
        });
      }

      if (this.battlePhase === 1) {
        const attacker = this.$refs.challengerBox;
        const defender = this.$refs.defenderBox;

        gsap.to(attacker, {
          x: "-5vw",
          duration: 0.4,
          ease: "power2.in",
          onComplete: () => {
            gsap.to(attacker, {
              x: "0vw",
              duration: 0.6,
              ease: "power2.out",
            });

            setTimeout(() => {
              gsap.fromTo(
                  defender,
                  {opacity: 0},
                  {opacity: 1, duration: 0.15, repeat: 6, yoyo: true}
              );
            }, 500);

            setTimeout(() => this.advanceBattle(), 3000);
          },
        });
      } else if (this.battlePhase === 2) {
        this.animatedHpPercent = this.defenderHpOverridePercent ?? (this.defenderHp / this.defenderMaxHp * 100);
        const healthBar = this.$refs.defenderHpBar;
        gsap.to(this, {
          animatedHpPercent: 0,
          duration: 2,
          ease: "power2.out",
          onComplete: () => {
            gsap.to(this.$refs.defenderBox, {
              y: 100,
              opacity: 0,
              duration: 1.5,
              ease: "power2.in",
              onComplete: () => {
                setTimeout(() => this.advanceBattle(), 1000);
              },
            });
          },
        });
      } else if (this.battlePhase === 3) {
        gsap.to(this.$refs.challengerBox, {
          x: 0,
          duration: 1,
          onComplete: () => {
            this.$emit("battle-finished");
          },
        });
      }

      this.battlePhase++;
    },
  },
  computed: {
    battleText() {
      return this.localBattleText;
    },
  },
};
</script>

<style scoped>
.pixelated {
  image-rendering: pixelated;
}

.animate-tackle {
  animation: tackle 1.5s ease-out;
}

@keyframes tackle {
  0% {
    transform: translate(0, 0) scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(2);
    opacity: 0;
  }
}
</style>