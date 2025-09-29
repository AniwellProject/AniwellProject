# -------- deps ----------
FROM node:20-alpine AS deps
WORKDIR /app
COPY package*.json ./
# CI 환경과 동일하게 설치(잠금파일 사용)
RUN npm ci

# -------- build ---------
FROM deps AS build
WORKDIR /app
COPY . .
# 브라우저에서 백엔드 호출용 공개 환경변수 (필요시 값 교체)
ARG NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
ENV NEXT_PUBLIC_API_BASE_URL=$NEXT_PUBLIC_API_BASE_URL
RUN npm run build

# -------- run -----------
FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
ENV PORT=3000
ENV NEXT_TELEMETRY_DISABLED=1

# standalone 산출물 + 정적리소스만 복사
COPY --from=build /app/.next/standalone ./
COPY --from=build /app/public ./public
COPY --from=build /app/.next/static ./.next/static

USER node
EXPOSE 3000
CMD ["node","server.js"]
