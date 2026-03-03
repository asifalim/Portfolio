FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
ENV NODE_OPTIONS=--openssl-legacy-provider
RUN npm run build -- --configuration production

# ─── Runtime Stage ─────────────────────────────────
FROM nginx:alpine
COPY --from=builder /app/dist/portfolio-frontend /usr/share/nginx/html
# If you have a custom nginx configuration, uncomment the next line
# COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
