# Stage 1: Build React SPA
FROM node:20-alpine AS builder
WORKDIR /app

# Copy package manifests and install dependencies cleanly
COPY package.json package-lock.json ./
RUN npm ci

# Copy project source
COPY . .

# Build with empty VITE_API_BASE_URL so apiClient uses relative paths (same origin)
ARG VITE_API_BASE_URL=""
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# Stage 2: Serve with Nginx Alpine
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
