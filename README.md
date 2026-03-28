# Kasay Bank
Демонстрационный банковский проект с backend на Spring Boot и веб-клиентом `Kasay Bank` на `React + Vite`.


### Быстрый запуск через Docker

первый запуск:
```bash
docker compose up --build -d
```

остальные запуски:
```bash
docker compose up -d
```

отключить:
```bash
docker compose down
```

отключить с удалением БД:
```bash
docker compose down -v
```

После запуска:

```text
Frontend: http://localhost:5173
Backend API: http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui/index.html
PostgreSQL: localhost:5432
```

### Продакшен-вариант для сервера

1. Скопируйте пример env:
```bash
cp .env.prod.example .env
```

2. Заполните `.env` своими значениями:
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `POSTGRES_DB`
- `APP_SECURITY_JWT_SECRET`

3. Поднимите production-compose:
```bash
docker compose -f docker-compose.prod.yaml up --build -d
```

В production-конфиге:
- наружу открыт только frontend
- backend не публикуется наружу
- postgres не публикуется наружу
- backend и база доступны только внутри docker-сети

Если нужен свой порт для сайта, измените `FRONTEND_PORT` в `.env`.


Проект реализует базовый сценарий интернет-банка:
- регистрация и вход
- JWT авторизация
- refresh token сессии
- выпуск виртуальных карт
- пополнение счета
- перевод по номеру карты
- перевод по номеру телефона

## Архитектура

```text
Kasay Bank Web
      |
      | REST API
      v
Spring Boot Backend
      |
      v
PostgreSQL
```

## Backend

Backend реализован на:
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT access token
- Refresh token

Основные сущности:
- `User`
- `Account`
- `Card`
- `Transaction`
- `RefreshToken`

## Frontend

Frontend находится в [frontend/](/Users/davvk/Code/Java/SpringBoot/otp-bank/frontend) и представляет собой лаконичный веб-кабинет в стиле интернет-банка.

Что есть в интерфейсе:
- отдельный вход и регистрация
- сохранение сессии в браузере
- защищённый кабинет после авторизации
- раздел карт
- раздел переводов
- список получателей с их картами при переводе по номеру карты
- зелёное подтверждение после успешного перевода


### ЕСЛИ ЗАПУСКАТЬ ПО ОТДЕЛЬНОСТИ КАЖДЫЙ СЕРВИС:

### 1. Backend

```bash
./mvnw spring-boot:run
```

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

Откройте:

```text
http://localhost:5173
```

По умолчанию frontend проксирует `/api/*` на:

```text
http://localhost:8080
```

Если backend работает на другом адресе, создайте файл `frontend/.env`:

```bash
VITE_API_BASE_URL=http://localhost:8080
```

## Авторизация

Сценарий работы:

1. Пользователь регистрируется или входит.
2. Сервер возвращает `accessToken` и `refreshToken`.
3. Frontend сохраняет сессию в браузере.
4. Защищённые запросы идут с `Bearer` токеном.
5. При необходимости используется обновление через `refreshToken`.

## Реализованные endpoint-ы, которые использует frontend

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/banking/checkAuth`
- `GET /api/banking/my_cards`
- `GET /api/banking/recipients`
- `POST /api/banking/create_card`
- `POST /api/banking/make_deposit`
- `POST /api/banking/make_transaction`

## Структура базы данных

Основные таблицы:

```
users
accounts
cards
transactions
refresh_tokens
```

Связи:

```text
User 1 -- 1 Account
Account 1 -- N Cards
Account 1 -- N Transactions
User 1 -- N RefreshTokens
```
