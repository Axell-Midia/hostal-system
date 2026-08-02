# Etapa 1: Construcción (Build) con Node.js
FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci
COPY . .
RUN npm run build

# Etapa 2: Servidor Web Ligero con Nginx
FROM nginx:alpine
# Copiar el build compilado desde la etapa anterior
COPY --from=builder /app/dist /usr/share/nginx/html
# Reemplazar la configuración por defecto por la nuestra
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
