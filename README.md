## Обзор

Проект `java-plus-graduation` — это монолит, разделенный на микросервисы.
Логика и API сохранены от старого проекта `java-explore-with-me-plus`, а публичные маршруты обслуживаются через `gateway-server`.

## Архитектура и взаимодействие сервисов

Сервисы:
- `infra/config-server` — централизованные конфигурации (Spring Cloud Config).
- `infra/discovery-server` — сервис обнаружения (Eureka).
- `infra/gateway-server` — единая точка входа, маршрутизация запросов.
- `core/event-service` — события, категории, подборки + вызовы в `user-service`, `request-service`, `stats-service`.
- `core/request-service` — заявки на участие.
- `core/user-service` — пользователи.
- `core/comments-service` — комментарии.
- `stats-service/stats-server` — сервис статистики.
- `stats-service/stats-client` — клиент статистики (используется в `event-service`).
- `core/interaction-api` и `core/common` — общие DTO/ошибки/утилиты.

Взаимодействия:
- `event-service` запрашивает пользователей и счетчики заявок (`user-service`, `request-service`).
- `request-service` и `comments-service` проверяют событие через `event-service`.
- `event-service` пишет просмотры в `stats-service` через `stats-client`.
- `gateway-server` маршрутизирует публичные и административные пути на сервисы.

## Где лежат конфигурации

- Основные конфиги сервисов: `infra/config-server/src/main/resources/config.core/*/application.yml`
- Конфиги инфраструктуры: `infra/config-server/src/main/resources/config.infra/*/application.yml`
- Конфиги статистики: `infra/config-server/src/main/resources/config.stats/*/application.yml`
- Локальные `application.yml` в сервисах — только bootstrap для подключения к Config Server.

## Внутренний API (межсервисный)

Внутренние эндпоинты:
- `GET /internal/users/{userId}` и `GET /internal/users?ids=...` (`user-service`)
- `GET /internal/events/{eventId}` (`event-service`)
- `GET /internal/requests/confirmed?eventIds=...` и `GET /internal/requests/counts?eventIds=...` (`request-service`)

## Внешний API

Спецификация внешнего API:
- Основной сервис: `./ewm-main-service-spec.json`
- Статистика: `./ewm-stats-service-spec.json`