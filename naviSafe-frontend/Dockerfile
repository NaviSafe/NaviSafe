# FROM node:22

# WORKDIR /app

# COPY package*.json ./

# RUN npm install && npm cache clean --force

# COPY . .

# CMD ["npm", "run", "dev"]


# build stage
FROM node:18 AS build
WORKDIR /app
COPY . .
RUN npm install && npm run build

# runtime stage
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
