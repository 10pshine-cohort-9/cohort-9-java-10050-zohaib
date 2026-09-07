# ============================================================
# Frontend Dockerfile — Multi-stage build
# Uses Debian-based images (glibc) for WSL2/Docker Desktop compat
# Stage 1: Build the React app with Node
# Stage 2: Serve with Nginx
# ============================================================

# ---------- Stage 1: Build ----------
FROM --platform=linux/amd64 node:18-bullseye-slim AS build

WORKDIR /app

# Copy package files first so the dependency layer is cached separately
COPY package.json package-lock.json* ./

# Install dependencies
ENV SKIP_PREFLIGHT_CHECK=true
RUN npm install --legacy-peer-deps

# Copy source and build the production bundle
COPY . .

RUN npm run build

# ---------- Stage 2: Serve ----------
FROM --platform=linux/amd64 nginx:1.25-bookworm

# Copy React build output (overwrites default Nginx html)
COPY --from=build /app/build /usr/share/nginx/html

# Copy custom Nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
