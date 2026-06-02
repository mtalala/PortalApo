FROM node:20-alpine

WORKDIR /app

# Instala as dependências necessárias para o Expo rodar no Linux Alpine
RUN apk add --no-cache bash

# Instala o CLI do Expo globalmente (evita erros de comando não encontrado)
RUN npm install -g expo-cli

# Copia os arquivos de configuração primeiro para aproveitar o cache do Docker
COPY package*.json ./

# Instala as dependências do seu projeto
RUN npm install

# Copia o restante dos arquivos do seu código
COPY . .

# O Expo Web usa por padrão a porta 19006 (ou 8081 nas versões mais novas)
EXPOSE 8081

# O comando força o Expo a aceitar conexões externas e rodar no modo web
CMD ["npx", "expo", "start", "--web", "--host", "lan"]