# Build multi-stage du client React — image finale nginx, légère et pauvre en CVE
FROM node:20-alpine AS build
WORKDIR /app
# La liaison du runner est lente (16-49 Ko/s) : pnpm abandonne vite par défaut.
# On relève fetch-timeout/retries et on réessaie install/build en cas d'erreur
# réseau transitoire (ECONNRESET/ETIMEDOUT) sans interrompre le job.
RUN corepack enable && pnpm config set fetch-timeout 900000 && pnpm config set fetch-retries 5
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile || (sleep 10 && pnpm install --frozen-lockfile)
COPY . .
RUN pnpm build || (sleep 10 && pnpm build)

FROM nginx:1.27-alpine
RUN apk add --no-cache curl
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
HEALTHCHECK --interval=15s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -fsS http://localhost/ || exit 1
