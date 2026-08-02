# Guía de Despliegue — Sistema Hostal

Guía completa para desplegar el Sistema Hostal en producción.
Incluye dos opciones de proveedor: **Hostinger KVM 2** y **Hetzner**.

---

## Tabla de Contenidos

1. [Resumen de Arquitectura](#1-resumen-de-arquitectura)
2. [Comparación de Proveedores](#2-comparación-de-proveedores)
3. [Opción A: Hostinger KVM 2](#3-opción-a-hostinger-kvm-2)
4. [Opción B: Hetzner Cloud](#4-opción-b-hetzner-cloud)
5. [Configuración Común (ambos proveedores)](#5-configuración-común-ambos-proveedores)
6. [Despliegue del Backend](#6-despliegue-del-backend)
7. [Despliegue del Frontend](#7-despliegue-del-frontend)
8. [Dominio y SSL](#8-dominio-y-ssl)
9. [Backups Automatizados](#9-backups-automatizados)
10. [Seguridad](#10-seguridad)
11. [Mantenimiento](#11-mantenimiento)

---

## 1. Resumen de Arquitectura

```
┌─────────────────────────────────────────────────┐
│                  VPS (Ubuntu 22.04)              │
│                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────────┐  │
│  │  Nginx   │  │ Backend  │  │  PostgreSQL  │  │
│  │  :443    │→ │ :8080    │→ │  :5432       │  │
│  │  :80     │  │ Spring   │  │  15-alpine   │  │
│  │  SSL     │  │ Boot     │  │              │  │
│  └────┬─────┘  └──────────┘  └──────────────┘  │
│       │                                          │
│  ┌────┴─────┐                                    │
│  │ Frontend │                                    │
│  │ React    │                                    │
│  │ (static) │                                    │
│  └──────────┘                                    │
└─────────────────────────────────────────────────┘
         ↑
    https://tudominio.com
```

**Stack de producción:**
- Frontend: React (archivos estáticos servidos por Nginx)
- Backend: Spring Boot 3.3.5 (Java 17)
- Base de datos: PostgreSQL 15
- Proxy inverso: Nginx + Let's Encrypt (SSL)
- Orquestación: Docker Compose

---

## 2. Comparación de Proveedores

| Característica | Hostinger KVM 2 | Hetzner CX22 |
|----------------|-----------------|--------------|
| **Precio** | $9/mes | ~$5/mes (€4.49) |
| **CPU** | 2 vCPU | 2 vCPU |
| **RAM** | 8 GB | 2 GB |
| **Disco** | 100 GB NVMe | 40 GB SSD |
| **Ancho de banda** | 8 TB | 20 TB |
| **Ubicación** | Varias (América disponible) | Alemania/Finlandia |
| **Soporte** | 24/7 chat (español) | Tickets (inglés/alemán) |
| **Panel de control** | Panel Hostinger | Hetzner Cloud Console |
| **Snapshots** | No | Sí (hasta 10) |
| **API** | No | Sí (CLI `hcloud`) |
| **IPv6** | Sí | Sí |
| **Backups automáticos** | Pago extra | Pago extra (~$2/mes) |

### ¿Cuál elegir?

| Situación | Proveedor recomendado |
|-----------|----------------------|
| Quieres más RAM por el precio | **Hostinger KVM 2** (8GB por $9) |
| Quieres la opción más barata | **Hetzner CX22** (~$5) |
| Necesitas soporte en español | **Hostinger** |
| Quieres snapshots/grupos de seguridad | **Hetzner** |
| Tu audiencia está en América | **Hostinger** (mejor latencia) |
| Quieres CLI para automatizar | **Hetzner** (`hcloud`) |

---

## 3. Opción A: Hostinger KVM 2

### ¿Por qué Hostinger KVM 2?

**Ventajas:**
- 8GB de RAM por $9/mes es excelente relación precio/calidad para Java
- Soporte en español 24/7 por chat
- Panel de control amigable para principiantes
- Servidores en múltiples ubicaciones (América disponible)
- NVMe SSD (más rápido que SSD normal)

**Desventajas:**
- No tiene snapshots (copias de estado del servidor)
- Sin API/CLI para automatización
- Menos herramientas avanzadas que Hetzner
- Backups automáticos son de pago extra

### Paso 1: Crear la cuenta y el VPS

1. Ir a https://www.hostinger.com/vps-hosting
2. Seleccionar el plan **KVM 2** ($9/mes)
3. Durante el registro:
   - Elegir **Ubuntu 22.04 64-bit** como sistema operativo
   - Elegir ubicación lo más cercana a los usuarios (América si está disponible)
4. Completar el pago
5. Recibirás un email con:
   - IP del servidor
   - Contraseña root
   - Puerto SSH (generalmente 22)

### Paso 2: Primer acceso al servidor

```bash
# Desde tu computadora (Linux/Mac/Windows con WSL)
ssh root@IP_DE_TU_SERVIDOR

# Si usas Windows sin WSL, puedes usar PuTTY:
# - Host Name: IP_DE_TU_SERVIDOR
# - Port: 22
# - Login: root
# - Password: la que recibiste por email
```

### Paso 3: Actualizar el sistema

```bash
apt update && apt upgrade -y
```

### Paso 4: Crear usuario no-root (opcional pero recomendado)

```bash
# Crear usuario
adduser deploy
usermod -aG sudo deploy

# Copiar SSH keys (desde tu computadora local)
ssh-copy-id deploy@IP_DE_TU_SERVIDOR

# Deshabilitar login root por contraseña
sed -i 's/PermitRootLogin yes/PermitRootLogin prohibit-password/' /etc/ssh/sshd_config
systemctl restart sshd
```

### Paso 5: Instalar Docker

```bash
# Instalar Docker
curl -fsSL https://get.docker.com | sh

# Agregar usuario al grupo docker
usermod -aG docker deploy

# Instalar Docker Compose
apt install docker-compose -y

# Verificar instalación
docker --version
docker-compose --version
```

### Continuar en la sección 5: [Configuración Común](#5-configuración-común-ambos-proveedores)

---

## 4. Opción B: Hetzner Cloud

### ¿Por qué Hetzner?

**Ventajas:**
- El VPS más barato del mercado (~$5/mes para CX22)
- Panel de control moderno y funcional
- API/CLI potente (`hcloud`) para automatizar todo
- Snapshots gratuitos (hasta 10)
- Red de alta calidad (20TB de ancho de banda)
- Soporte técnico rápido

**Desventajas:**
- RAM limitada en el plan básico (2GB para CX22)
- Soporte solo en inglés/alemán
- Servidores en Europa (mayor latencia si tus usuarios están en América)
- Sin datacenter en América (el más cercano es US East en Ashburn, VA)

### Paso 1: Crear la cuenta y el VPS

1. Ir a https://www.hetzner.com/cloud
2. Crear una cuenta
3. En el panel de Hetzner Cloud:
   - Click "New Project" → nombre: `hostal`
   - Dentro del proyecto: "Create Server"
   - **Image:** Ubuntu 22.04
   - **Type:** CX22 (2 vCPU, 2GB RAM, ~$5/mes)
   - **Location:** Ashburn, VA (más cercano a América) o el que prefieras
   - **SSH Keys:** Agregar tu SSH key pública
   - Click "Create & Buy"

### Paso 2: Obtener tu SSH key (si no tienes)

```bash
# Linux/Mac
ssh-keygen -t ed25519 -C "tu@email.com"
cat ~/.ssh/id_ed25519.pub

# Copiar el resultado y pegarlo en Hetzner Cloud Console
# → SSH Keys → Add SSH Key
```

### Paso 3: Primer acceso

```bash
ssh root@IP_DE_TU_SERVIDOR
```

### Paso 4: Actualizar el sistema

```bash
apt update && apt upgrade -y
```

### Paso 5: Crear usuario no-root

```bash
adduser deploy
usermod -aG sudo deploy

# Copiar SSH keys
ssh-copy-id deploy@IP_DE_TU_SERVIDOR

# Deshabilitar login root
sed -i 's/PermitRootLogin yes/PermitRootLogin prohibit-password/' /etc/ssh/sshd_config
systemctl restart sshd
```

### Paso 6: Instalar Docker

```bash
curl -fsSL https://get.docker.com | sh
usermod -aG docker deploy
apt install docker-compose -y

docker --version
docker-compose --version
```

### Paso 7 (Opcional): Usar la CLI de Hetzner

```bash
# Instalar hcloud CLI
curl -fsSL https://get.hetzner.cloud/install.sh | bash

# Configurar token (lo encuentras en Hetzner Console → Security → API Tokens)
export HCLOUD_TOKEN="tu-token-aqui"

# Útiles comandos
hcloud server list                    # Ver servidores
hcloud server create-snapshot hostal  # Crear snapshot antes de cambios
hcloud server poweroff hostal         # Apagar servidor
hcloud server delete hostal           # Eliminar servidor
```

### Continuar en la sección 5: [Configuración Común](#5-configuración-común-ambos-proveedores)

---

## 5. Configuración Común (Ambos Proveedores)

Desde aquí los pasos son idénticos sin importar qué proveedor elegiste.

### Paso 1: Clonar el repositorio

```bash
cd /home/deploy
git clone https://github.com/TU_USUARIO/hostal-system.git
cd hostal-system
```

### Paso 2: Crear archivo .env

```bash
cp .env.example .env
nano .env
```

Configurar con valores reales de producción:

```bash
# Base de Datos
DB_HOST=postgres
DB_PORT=5432
DB_NAME=hostal_db
DB_USER=hostal_user
DB_PASSWORD=<CONTRASEÑA-FUERTE-ALEATORIA-32-CARACTERES>

# JWT (generar con: openssl rand -base64 32)
JWT_SECRET=<SECRETO-JWT-GENERADO-ALEATORIAMENTE>

# Spring
SPRING_PROFILES_ACTIVE=prod

# CORS (tu dominio)
CORS_ORIGINS=https://tudominio.com

# Frontend
VITE_API_URL=https://tudominio.com
```

**Generar contraseñas seguras:**

```bash
# Para DB_PASSWORD
openssl rand -base64 32

# Para JWT_SECRET
openssl rand -base64 32
```

### Paso 3: Crear docker-compose de producción

Reemplazar el `docker-compose.yml` actual con la versión completa:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: hostal-postgres
    restart: always
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER} -d ${DB_NAME}"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build: ./backend
    container_name: hostal-backend
    restart: always
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: ${DB_NAME}
      DB_USER: ${DB_USER}
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE}
      CORS_ORIGINS: ${CORS_ORIGINS}
      PORT: 8080

  nginx:
    image: nginx:alpine
    container_name: hostal-nginx
    restart: always
    ports:
      - "80:80"
      - "443:443"
    depends_on:
      - backend
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/conf.d:/etc/nginx/conf.d:ro
      - ./nginx/ssl:/etc/letsencrypt:ro
      - ./frontend/dist:/usr/share/nginx/html:ro

volumes:
  postgres_data:
```

### Paso 4: Crear configuración de Nginx

```bash
mkdir -p nginx/conf.d nginx/ssl
```

Crear `nginx/nginx.conf`:

```bash
cat > nginx/nginx.conf << 'EOF'
events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    # Compresión
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml;

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;

    # Upstream del backend
    upstream backend {
        server backend:8080;
    }

    include /etc/nginx/conf.d/*.conf;
}
EOF
```

Crear `nginx/conf.d/default.conf`:

```bash
cat > nginx/conf.d/default.conf << 'EOF'
# HTTP → redirigir a HTTPS
server {
    listen 80;
    server_name tudominio.com www.tudominio.com;
    return 301 https://$host$request_uri;
}

# HTTPS
server {
    listen 443 ssl http2;
    server_name tudominio.com www.tudominio.com;

    # SSL (se configura después con Certbot)
    ssl_certificate     /etc/letsencrypt/live/tudominio.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/tudominio.com/privkey.pem;

    # Headers de seguridad
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # Frontend (React SPA)
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;

        # Cache de assets estáticos
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }

    # API → Backend
    location /api/ {
        limit_req zone=api burst=20 nodelay;
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Health check (solo desde localhost)
    location /actuator/ {
        allow 127.0.0.1;
        allow 10.0.0.0/8;
        deny all;
        proxy_pass http://backend;
    }

    # Swagger (solo desde localhost)
    location /swagger-ui.html {
        allow 127.0.0.1;
        allow 10.0.0.0/8;
        deny all;
        proxy_pass http://backend;
    }

    # API docs
    location /api-docs {
        allow 127.0.0.1;
        allow 10.0.0.0/8;
        deny all;
        proxy_pass http://backend;
    }
}
EOF
```

### Paso 5: Compilar el frontend para producción

```bash
cd frontend

# Instalar dependencias
npm install

# Configurar URL del API para producción
echo "VITE_API_URL=https://tudominio.com" > .env

# Compilar
npm run build

cd ..
```

### Paso 6: Levantar los servicios (sin SSL temporalmente)

```bash
# Crear un nginx.conf temporal sin SSL para poder obtener el certificado
cat > nginx/conf.d/default-temp.conf << 'EOF'
server {
    listen 80;
    server_name tudominio.com www.tudominio.com;

    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
EOF

# Levantar todo
docker-compose up -d --build
```

---

## 6. Despliegue del Backend

El backend se construye automáticamente con `docker-compose up -d --build`.

### Verificar que el backend funciona

```bash
# Ver logs
docker logs hostal-backend

# Probar el health check
curl http://localhost:8080/actuator/health

# Probar login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"centro","password":"centro123"}'
```

### Troubleshooting del backend

```bash
# Si el backend no arranca, revisar logs detallados
docker logs hostal-backend --tail 50

# Si hay error de conexión a BD
docker exec hostal-postgres pg_isready -U hostal_user -d hostal_db

# Reiniciar solo el backend
docker restart hostal-backend
```

---

## 7. Despliegue del Frontend

El frontend ya está compilado y servido por Nginx. Solo verificar:

```bash
# Verificar que Nginx está corriendo
docker logs hostal-nginx

# Probar que el frontend responde
curl -I http://localhost
```

---

## 8. Dominio y SSL

### Paso 1: Configurar DNS

En tu proveedor de dominios (Namecheap, Cloudflare, etc.), crear los registros:

```
Tipo    Nombre    Valor
A       @         IP_DE_TU_VPS
A       www       IP_DE_TU_VPS
```

Esperar 5-10 minutos a que se propague.

### Paso 2: Verificar propagación DNS

```bash
# Desde tu computadora
nslookup tudominio.com
# Debe retornar la IP de tu VPS
```

### Paso 3: Obtener certificado SSL con Let's Encrypt

```bash
# Instalar Certbot
apt install certbot -y

# Detener Nginx temporalmente
docker stop hostal-nginx

# Obtener certificado (puerto 80 debe estar libre)
certbot certonly --standalone \
  -d tudominio.com \
  -d www.tudominio.com \
  --email tu@email.com \
  --agree-tos \
  --non-interactive

# Copiar certificados al directorio de Nginx
cp -r /etc/letsencrypt/live/tudominio.com nginx/ssl/

# Eliminar config temporal
rm nginx/conf.d/default-temp.conf

# Reiniciar Nginx con SSL
docker start hostal-nginx
```

### Paso 4: Auto-renovación del certificado

```bash
# Crear cron job para renovación automática
cat > /etc/cron.d/certbot-renew << 'EOF'
0 3 * * * root certbot renew --quiet --deploy-hook "docker restart hostal-nginx" >> /var/log/certbot-renew.log 2>&1
EOF
```

### Paso 5: Verificar HTTPS

```bash
# Probar que HTTPS funciona
curl -I https://tudominio.com

# Verificar el certificado
openssl s_client -connect tudominio.com:443 -servername tudominio.com
```

---

## 9. Backups Automatizados

### Crear script de backup

```bash
mkdir -p /home/deploy/backups

cat > /home/deploy/backup.sh << 'SCRIPT'
#!/bin/bash
FECHA=$(date +%Y-%m-%d_%H%M%S)
BACKUP_DIR="/home/deploy/backups"
DB_CONTAINER="hostal-postgres"

# Backup de la base de datos
docker exec $DB_CONTAINER pg_dump -U hostal_user hostal_db | \
  gzip > "$BACKUP_DIR/hostal-db-$FECHA.sql.gz"

# Backup del código
tar -czf "$BACKUP_DIR/hostal-code-$FECHA.tar.gz" \
  --exclude='node_modules' \
  --exclude='target' \
  --exclude='.git' \
  -C /home/deploy hostal-system

# Mantener solo los últimos 30 backups
find $BACKUP_DIR -name "hostal-db-*.gz" -mtime +30 -delete
find $BACKUP_DIR -name "hostal-code-*.tar.gz" -mtime +30 -delete

echo "[$(date)] Backup completado: hostal-db-$FECHA.sql.gz"
SCRIPT

chmod +x /home/deploy/backup.sh
```

### Programar backup diario

```bash
# Ejecutar backup todos los días a las 2:00 AM
crontab -e
# Agregar:
0 2 * * * /home/deploy/backup.sh >> /home/deploy/backups/backup.log 2>&1
```

### Backup manual

```bash
/home/deploy/backup.sh
```

---

## 10. Seguridad

### Firewall (UFW)

```bash
# Instalar UFW
apt install ufw -y

# Configurar reglas
ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp    # SSH
ufw allow 80/tcp    # HTTP (para redirección a HTTPS)
ufw allow 443/tcp   # HTTPS

# Activar
ufw enable
ufw status
```

### Fail2ban (protección contra brute force)

```bash
# Instalar Fail2ban
apt install fail2ban -y

# Configurar para SSH
cat > /etc/fail2ban/jail.local << 'EOF'
[sshd]
enabled = true
port = 22
filter = sshd
logpath = /var/log/auth.log
maxretry = 3
bantime = 3600
findtime = 600
EOF

systemctl enable fail2ban
systemctl start fail2ban
```

### Cambiar passwords de producción

**IMPORTANTE:** Antes de poner en producción, cambia las credenciales semilla:

```bash
# Conectar a la BD
docker exec -it hostal-postgres psql -U hostal_user -d hostal_db

# Cambiar password del usuario centro
-- Generar nuevo hash BCrypt para la contraseña real
-- Usar la herramienta HashGen.java que ya tienes en el proyecto

UPDATE usuario
SET password_hash = '<NUEVO_HASH_BCRYPT>'
WHERE username = 'centro';

UPDATE usuario
SET password_hash = '<NUEVO_HASH_BCRYPT>'
WHERE username = 'norte';

EXIT;
```

### Deshabilitar root login por contraseña

```bash
# Verificar que ya está deshabilitado
grep PermitRootLogin /etc/ssh/sshd_config

# Si dice "yes", cambiarlo
sed -i 's/PermitRootLogin yes/PermitRootLogin prohibit-password/' /etc/ssh/sshd_config
systemctl restart sshd
```

---

## 11. Mantenimiento

### Comandos útiles diarios

```bash
# Ver logs del backend
docker logs hostal-backend --tail 50 -f

# Ver logs de Nginx
docker logs hostal-nginx --tail 50 -f

# Reiniciar todos los servicios
docker-compose down && docker-compose up -d

# Verificar estado de todos los contenedores
docker ps

# Verificar espacio en disco
df -h

# Verificar uso de RAM
free -h
```

### Actualizar el sistema

```bash
# Actualizar código desde GitHub
cd /home/deploy/hostal-system
git pull origin main

# Rebuild y reiniciar
docker-compose down
docker-compose up -d --build

# Verificar que todo funciona
curl -I https://tudominio.com
curl https://tudominio.com/actuator/health
```

### Monitoreo básico

```bash
# Script para verificar que todo está corriendo
cat > /home/deploy/check-status.sh << 'SCRIPT'
#!/bin/bash
echo "=== Estado de contenedores ==="
docker ps --format "table {{.Names}}\t{{.Status}}"

echo ""
echo "=== Uso de recursos ==="
docker stats --no-stream

echo ""
echo "=== Health check backend ==="
curl -s http://localhost:8080/actuator/health

echo ""
echo "=== Espacio en disco ==="
df -h /
SCRIPT

chmod +x /home/deploy/check-status.sh

# Ejecutar cuando quieras
/home/deploy/check-status.sh
```

---

## Resumen de Costos

### Hostinger KVM 2

| Concepto | Costo |
|----------|-------|
| VPS KVM 2 (8GB RAM, 100GB SSD) | $9/mes |
| Dominio (.com) | ~$1/año (~$0.83/mes) |
| SSL (Let's Encrypt) | $0 |
| **Total mensual** | **~$10/mes** |

### Hetzner CX22

| Concepto | Costo |
|----------|-------|
| VPS CX22 (2GB RAM, 40GB SSD) | ~$5/mes |
| Dominio (.com) | ~$1/año (~$0.83/mes) |
| SSL (Let's Encrypt) | $0 |
| **Total mensual** | **~$6/mes** |

### Hetzner CX32 (si necesitas más RAM)

| Concepto | Costo |
|----------|-------|
| VPS CX32 (4GB RAM, 80GB SSD) | ~$10/mes |
| Dominio (.com) | ~$1/año |
| SSL (Let's Encrypt) | $0 |
| **Total mensual** | **~$11/mes** |

---

## Checklist de Despliegue

- [ ] VPS creado con Ubuntu 22.04
- [ ] Docker + Docker Compose instalados
- [ ] Repositorio clonado en el VPS
- [ ] Archivo `.env` configurado con contraseñas reales
- [ ] `docker-compose.yml` actualizado con todos los servicios
- [ ] Frontend compilado para producción (`npm run build`)
- [ ] Nginx configurado con proxy inverso
- [ ] DNS configurado (A records apuntando al VPS)
- [ ] Certificado SSL instalado (Let's Encrypt)
- [ ] Auto-renovación SSL configurada
- [ ] Firewall activado (UFW: puertos 22, 80, 443)
- [ ] Fail2ban instalado y configurado
- [ ] Backups automatizados con cron
- [ ] Passwords semilla cambiados por contraseñas reales
- [ ] Login funcional con credenciales de producción
- [ ] Verificar que cada sede solo ve sus datos
- [ ] Correo corporativo configurado con el dominio
- [ ] Accesos (GitHub, VPS, dominio) a nombre del cliente
