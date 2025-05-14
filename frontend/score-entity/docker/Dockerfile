# 📦 1. Build Stage mit Node (ohne Alpine!)
FROM node:18 AS build-stage

# Arbeitsverzeichnis setzen
WORKDIR /app

# Nur package.json und lockfile kopieren
COPY package*.json ./

# Optional-Dependencies verhindern (nur nötig, wenn es weiterhin Probleme gibt)
# ENV NPM_CONFIG_OPTIONAL=false

# Abhängigkeiten installieren
RUN npm install

# Restliche Dateien kopieren
COPY . .

# Vite-Build durchführen
RUN npm run build

# 🧊 2. Production Stage mit NGINX (bleibt auf Alpine, da hier kein npm mehr gebraucht wird)
FROM nginx:alpine AS production-stage

# Build-Output kopieren
COPY --from=build-stage /app/dist /usr/share/nginx/html

# nginx.conf kopieren (falls vorhanden)
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]