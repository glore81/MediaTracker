# Media Tracker REST API

Backend REST API для учёта просмотренных и запланированных фильмов, сериалов и аниме с поддержкой внешнего поиска через публичные каталоги.

## Стек технологий
* **Java 25** & **Spring Boot 4.2** (Spring Web, Spring Data JPA, `RestClient`, Bean Validation)
* **PostgreSQL 16** (запуск через Docker Compose или локально)
* **Maven**
* **Docker / Docker Compose**

## Архитектура
Проект построен по классической трёхслойной архитектуре:
* `controller` — обработка HTTP-запросов, валидация параметров и возврат статусов ответа.
* `service` — бизнес-логика (трекинг серий, автоматическая смена статусов, доменные правила для фильмов без эпизодов, интеграция с внешними API).
* `repository` — доступ к данным PostgreSQL через Spring Data JPA.
* `model` & `dto` — JPA-сущности, перечисления (`MediaType`, `WatchStatus`) и DTO для маппинга ответов внешних сервисов.

## Основные возможности
* **CRUD-операции** для управления личным списком медиа (`MOVIE`, `SERIES`, `ANIME`).
* **Фильтрация и поиск** по типу контента, статусу просмотра (`PLANNED`, `WATCHING`, `COMPLETED`, `DROPPED`) и названию.
* **Валидация входных данных** (название, тип, статус обязательны; количество серий не может быть отрицательным).
* **Трекинг прогресса (`PATCH /api/media/{id}/watch`):**
  * для сериалов и аниме увеличивает счётчик просмотренных серий и автоматически переводит статус из `PLANNED` в `WATCHING`, а при достижении последней серии — в `COMPLETED`;
  * для фильмов (`MOVIE`) эпизоды не учитываются (`null`), а отметка о просмотре сразу переводит тайтл в статус `COMPLETED`.
* **Внешний поиск и импорт в один запрос:** поиск аниме через **Kitsu API** и сериалов через **TVMaze API** с последующим сохранением выбранной карточки в PostgreSQL.

## Запуск проекта

```bash
docker-compose up --build
```
Это поднимет PostgreSQL и само приложение. API будет доступен на `http://localhost:8080`.

### Переменные окружения
| Переменная    | По умолчанию                                     | Описание             |
|---------------|---------------------------------------------------|----------------------|
| `DB_URL`      | `jdbc:postgresql://localhost:5432/media_tracker`  | строка подключения к БД |
| `DB_USER`     | `postgres`                                        | пользователь БД      |
| `DB_PASSWORD` | `1234`                                             | пароль БД            |

## Примеры запросов

**Создать тайтл вручную:**
```bash
curl -X POST http://localhost:8080/api/media \
  -H "Content-Type: application/json" \
  -d '{"title":"Attack on Titan","type":"ANIME","status":"PLANNED","totalEpisodes":25}'
```

**Поиск во внешнем каталоге:**
```bash
curl "http://localhost:8080/api/media/search-external?query=naruto&type=ANIME"
```

**Импорт найденного тайтла в свой список:**
```bash
curl -X POST "http://localhost:8080/api/media/import?status=PLANNED" \
  -H "Content-Type: application/json" \
  -d '{"title":"Naruto","type":"ANIME","totalEpisodes":220,"synopsis":"..."}'
```

**Отметить серию просмотренной:**
```bash
curl -X PATCH http://localhost:8080/api/media/1/watch
```

## Список эндпоинтов

| Метод  | Путь                        | Описание                                   |
|--------|-----------------------------|---------------------------------------------|
| POST   | `/api/media`                | создать тайтл вручную                       |
| GET    | `/api/media`                | список с фильтрами (`type`, `status`, `search`) |
| GET    | `/api/media/{id}`           | получить тайтл по id                        |
| PUT    | `/api/media/{id}`           | обновить тайтл                              |
| PATCH  | `/api/media/{id}/watch`     | отметить следующую серию/фильм просмотренным |
| DELETE | `/api/media/{id}`           | удалить тайтл                               |
| GET    | `/api/media/search-external`| поиск во внешнем каталоге (Kitsu/TVMaze)    |
| POST   | `/api/media/import`         | импортировать найденный тайтл в свой список |
