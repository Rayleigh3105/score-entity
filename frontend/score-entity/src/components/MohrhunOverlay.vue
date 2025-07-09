<template>
  <div class="moorhuhn-overlay">
    <div class="background"></div>
    <img
        :src="challenger?.imageUrl"
        class="chicken"
        :style="challengerStyle"
        alt="Kontrahent"
    />
    <img
        :src="defender?.imageUrl"
        class="chicken"
        :style="defenderStyle"
        :class="{ hit: defenderHit }"
        alt="Verteidiger"
    />
    <div v-if="showScore" class="moorhuhn-score" :style="scoreStyle">+25</div>
    <div
        v-if="showCrosshair"
        class="crosshair"
        :style="crosshairStyle"
    ></div>
    <div v-for="p in particles" :key="p.id"
         class="particle"
         :style="{
           top: p.top,
           left: p.left,
           background: p.color,
           transform: `rotate(${p.angle}deg) translate(0, 0)`,
           animation: `explode 0.8s ease-out forwards ${Math.random() * 0.1}s`
         }"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';

const props = defineProps(['challenger', 'defender']);
const emit = defineEmits(['moorhuhn-finished']);

// Positionen und Bewegung
const challengerStyle = ref({ top: '30%', left: '20%' });
const defenderStyle = ref({ top: '60%', left: '65%' });

const showCrosshair = ref(false);
const crosshairStyle = ref({ top: '60%', left: '65%' });
const defenderHit = ref(false);

const showScore = ref(false);
const scoreStyle = ref({ top: '0%', left: '0%' });

const particles = ref<Array<{id: number, top: string, left: string, angle: number, color: string}>>([]);
let particleId = 0;

function createParticles(style: { top: string, left: string }) {
  particles.value = [];
  const rectTop = parseFloat(style.top);
  const rectLeft = parseFloat(style.left);
  for (let i = 0; i < 18; i++) {
    const angle = (360 / 18) * i + Math.random() * 10;
    const color = i % 2 === 0 ? '#ff2d2d' : '#ffd700';
    particles.value.push({
      id: particleId++,
      top: rectTop + 4 + Math.random() * 2 + '%',
      left: rectLeft + 4 + Math.random() * 2 + '%',
      angle,
      color
    });
  }
}

function randomPos() {
  return {
    top: `${40 + Math.random() * 15}%`,
    left: `${30 + Math.random() * 30}%`
  };
}

function randomNonOverlappingPos(otherStyle: { top: string, left: string }) {
  const minDistancePercent = 8; // ca. 160px in Prozent
  let attempts = 0;
  let pos = randomPos();
  const otherTop = parseFloat(otherStyle.top);
  const otherLeft = parseFloat(otherStyle.left);

  while (attempts < 20) {
    const top = parseFloat(pos.top);
    const left = parseFloat(pos.left);
    const distTop = Math.abs(top - otherTop);
    const distLeft = Math.abs(left - otherLeft);
    if (distTop >= minDistancePercent || distLeft >= minDistancePercent) {
      return pos;
    }
    pos = randomPos();
    attempts++;
  }
  return pos;
}

let moveInterval: any = null;

onMounted(async () => {
  // Startpositionen setzen mit Abstand
  challengerStyle.value = randomNonOverlappingPos({ top: '0%', left: '0%' });
  defenderStyle.value = randomNonOverlappingPos(challengerStyle.value);

  // Bewegung simulieren
  moveInterval = setInterval(() => {
    challengerStyle.value = randomNonOverlappingPos(defenderStyle.value);
    defenderStyle.value = randomNonOverlappingPos(challengerStyle.value);
  }, 1700);

  // Nach 5 Sekunden: Ziel auf Verteidiger
  setTimeout(() => {
    clearInterval(moveInterval);
    showCrosshair.value = true;
    crosshairStyle.value = { ...defenderStyle.value };

    // Schuss-Animation nach kurzer Pause
    setTimeout(() => {
      defenderHit.value = true;
      createParticles(defenderStyle.value);

      // Score anzeigen
      showScore.value = true;
      // Score mittig über Verteidiger positionieren
      const topNum = parseFloat(defenderStyle.value.top);
      const leftNum = parseFloat(defenderStyle.value.left);
      scoreStyle.value = {
        top: `${topNum - 10}%`,
        left: `${leftNum + 7}%`
      };

      setTimeout(() => {
        showScore.value = false;
      }, 1200);

      setTimeout(() => {
        emit('moorhuhn-finished');
        particles.value = [];
      }, 2000);
    }, 1000);
  }, 5000);
});
</script>

<style scoped>
.moorhuhn-overlay {
  position: fixed;
  z-index: 9999;
  inset: 0;
  background: url('/moorhuhn.png') center center / cover no-repeat;
  overflow: hidden;
}
.chicken {
  position: absolute;
  width: 140px;
  height: 140px;
  transition: top 0.7s cubic-bezier(.8,.2,.2,1), left 0.7s cubic-bezier(.8,.2,.2,1);
  z-index: 2;
  border-radius: 50%;
  box-shadow: 0 4px 18px #1a774c44;
  border: 2.5px solid #fff;
}
.chicken.hit {
  animation: hit-blink 0.9s 1;
  filter: drop-shadow(0 0 40px #e53935) blur(2px) brightness(1.5);
  opacity: 0.07;
  z-index: 10;
}
@keyframes hit-blink {
  0% { filter: none; }
  30% { filter: drop-shadow(0 0 30px #fff959) brightness(2.5); }
  60% { filter: drop-shadow(0 0 65px #e53935) brightness(2.5); }
  90% { filter: drop-shadow(0 0 45px #fff) brightness(2.5); }
  100% { opacity: 0.09; }
}
.crosshair {
  position: absolute;
  width: 140px;
  height: 140px;
  z-index: 99;
  background: none;
  pointer-events: none;
  border-radius: 50%;
  border: 3px solid #f00a;
  box-shadow: 0 0 14px #d44a;
}
.crosshair::before,
.crosshair::after {
  content: "";
  position: absolute;
  background: #f00;
  border-radius: 1px;
}
.crosshair::before {
  left: 69px;
  top: 0;
  width: 2px;
  height: 140px;
}
.crosshair::after {
  top: 69px;
  left: 0;
  width: 140px;
  height: 2px;
}

.particle {
  position: absolute;
  width: 13px;
  height: 13px;
  border-radius: 100%;
  pointer-events: none;
  z-index: 150;
  opacity: 0.87;
}

@keyframes explode {
  0% {
    opacity: 1;
    transform: rotate(var(--angle)) scale(1) translate(0, 0);
  }
  85% {
    opacity: 1;
  }
  100% {
    opacity: 0;
    transform: rotate(var(--angle)) scale(1.5) translate(80px, 0);
  }
}

.moorhuhn-score {
  position: absolute;
  font-size: 54px;
  color: #ffe200;
  -webkit-text-stroke: 3px #522;
  font-weight: bold;
  z-index: 200;
  pointer-events: none;
  animation: score-anim 1.2s ease forwards;
}

@keyframes score-anim {
  0% {
    opacity: 0;
    transform: translateY(20px) scale(0.8);
  }
  30% {
    opacity: 1;
    transform: translateY(0) scale(1.1);
  }
  70% {
    opacity: 1;
    transform: translateY(-10px) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateY(-20px) scale(0.8);
  }
}
</style>