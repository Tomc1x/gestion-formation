# --- Stage 1 : build de l'application Angular ---
FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
# Note: package-lock.json est desynchronise de package.json (deps optionnelles
# manquantes), `npm ci` echoue en EUSAGE. `npm install` reste tolerant.
RUN npm install

COPY . .
RUN npm run build

# --- Stage 2 : nginx pour servir le build ---
FROM nginx:alpine AS runtime

COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
