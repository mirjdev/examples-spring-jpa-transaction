### Стек и обновление зависимостей

**Текущее состояние (Current state).** Проект использует Java 25 и Gradle 9.8.2.
Обновление на последние стабильные версии:

| Компонент | До обновления | После обновления |
| --- | --- | --- |
| Spring Boot | 4.0.7 | 4.1.2 |
| Spring Cloud BOM | 2025.1.2 | 2025.1.4 |
| Spring dependency-management plugin | 1.1.6 | 1.1.7 |
| springdoc OpenAPI | 3.0.3 | 3.2.0 |
| Easy Random | `org.jeasy:easy-random-core:4.0.0` | `org.jeasy:easy-random:6.0.1` |
| JaCoCo | 0.8.14 | 0.8.16 |

Gradle 9.8.2, Testcontainers 2.0.5 и MapStruct 1.6.3 уже актуальны и сохраняются.
Версии Hibernate, Jackson, Lombok и драйвера PostgreSQL согласованы через Spring Boot BOM;
зависимости Spring Cloud и Testcontainers управляются соответствующими BOM.

**Путь обновления (Upgrade path).** Сначала обновляется платформа Spring Boot и Spring Cloud
с плагином управления зависимостями, затем совместимая версия springdoc и Easy Random,
затем JaCoCo. [Матрица Spring Cloud](https://spring.io/projects/spring-cloud/) подтверждает
совместимость линии 2025.1.x с Boot 4.1.x начиная с 2025.1.2;
[springdoc 3.2.0](https://github.com/springdoc/springdoc-openapi/releases/tag/v3.2.0)
собран на Boot 4.1.2. Источники остальных обновлений:
[Spring Boot 4.1](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.1-Release-Notes),
[dependency-management 1.1.7](https://plugins.gradle.org/plugin/io.spring.dependency-management),
[Easy Random 6.0.1](https://central.sonatype.com/artifact/org.jeasy/easy-random/6.0.1),
[JaCoCo 0.8.16](https://github.com/jacoco/jacoco/releases/tag/v0.8.16).

**Места риска (Breaking-change hotspots).** Boot 4.1 удаляет API, устаревшие в 4.0;
обновление JPA/Hibernate и Jackson требует проверки запросов, блокировок и JSON-контракта.
Easy Random 6 требует Java 17+ и меняет имя артефакта на `easy-random`.
После обновления springdoc нужно проверить генерацию OpenAPI.

**Проверки (Verification gates).** Разрешение зависимостей, компиляция, тесты, пороги покрытия
и сборка исполняемого JAR проверяются командой:

```powershell
.\gradlew.bat clean check bootJar
```

Для интеграционных тестов нужен Docker. Генерация `/v3/api-docs`, пути Client API и его схемы
проверяются автоматически. Итоговая проверка на Java 25: 123 теста без ошибок и пропусков;
покрытие кода Client — 100% строк и 97,73% ветвей. Исполняемый JAR: `build/libs/shop.jar`.

**Временные меры (Temporary mitigations).** Временных переопределений версий зависимостей
или адаптеров совместимости для обновления нет.

**Откат (Rollback note).** Вернуть изменения версий и связанные адаптации кода к предыдущему
состоянию, повторить сборку и проверки. Обновление зависимостей не меняет схему базы данных
и не требует отката Liquibase.

### Запуск базы

```powershell
docker compose -f postgres-docker/docker-compose.yml up -d postgres
```

Используется PostgreSQL 18.4 из Compose: база `dev`, пользователь и пароль `postgres`,
порт `54321`. Параметры соответствуют `application.properties`.

### Client REST API

API доступно по `/api/clients` (порт приложения по умолчанию — `8099`). Таблица `clients`
создаётся Liquibase при запуске приложения. Миграция содержит откат, первичный ключ,
уникальное ограничение на email и индекс `(created_at, id)`.

| Метод | URL | Результат |
| --- | --- | --- |
| POST | `/api/clients` | Создание, `201 Created` и заголовок `Location` |
| GET | `/api/clients/{id}` | Получение клиента |
| GET | `/api/clients` | Поиск и страница клиентов |
| PUT | `/api/clients/{id}` | Частичное обновление с проверкой `version` |
| DELETE | `/api/clients/{id}` | Удаление, `204 No Content` |

Пример тела запроса на создание:

```json
{"email":"ivan@example.com","fio":"Иван Иванов","phone":"+79991234567"}
```

Все три поля обязательны. Максимальная длина email — 254, ФИО — 255 символов.
Телефон содержит от 7 до 15 цифр и необязательный `+` в начале. Email сохраняется
в нижнем регистре; по краям ФИО удаляются пробелы. `id`, `createdAt` (UTC) и `version`
формируются сервером. Дата создания не изменяется при обновлении.

Для обновления передайте текущую версию из последнего ответа:

```json
{"fio":"Иван Петров","version":0}
```

Отсутствующие поля и `null` сохраняют прежние значения. Успешное изменение увеличивает
`version`. Устаревшая версия и повторяющийся email возвращают `409`, отсутствующий клиент —
`404`, некорректный запрос — `400`. Ошибки возвращаются в формате `application/problem+json`.

Пример поиска:

```text
GET /api/clients?fio=Иван&email=example.com&page=0&size=20&sort=createdAt,desc
```

Строковые фильтры `email`, `fio`, `phone` ищут подстроку без учёта регистра.
Символы `%`, `_` и `\` воспринимаются буквально. Доступны точные фильтры `id`, `version`
и включительный диапазон `createdFrom`, `createdTo` в формате ISO 8601, например
`2026-11-04T00:00:00Z`. Все заданные фильтры объединяются условием AND.

Нумерация страниц начинается с нуля, размер по умолчанию — 20, допустимый размер — 1–100.
Сортировка доступна по `id`, `email`, `fio`, `phone`, `createdAt`, `version`; при совпадении
значений используется дополнительная сортировка по `id`. Ответ содержит `content`, `page`,
`size`, `totalElements`, `totalPages`. Контракт также доступен в Swagger UI.

Проверка тестов и покрытия:

```powershell
.\gradlew.bat clean check
```

Для всего нового кода Client, включая DTO, обработчик ошибок и сгенерированный MapStruct
маппер, JaCoCo проверяет минимум 80% строк и 80% ветвей. Проверка входит в `check`;
HTML-отчёт: `build/reports/jacoco/test/html/index.html`, XML:
`build/reports/jacoco/test/jacocoTestReport.xml`. Покрытие старых учебных примеров
не входит в этот порог.

Для интеграционных тестов необходим работающий Docker. Используется PostgreSQL 18.4,
как в Compose. Testcontainers создаёт отдельные временные контейнеры для HTTP API
и тестов репозитория; база из Compose остаётся базой для локального запуска приложения.
Если Docker недоступен, интеграционные проверки завершаются ошибкой.

API-тесты поднимают настоящее приложение и применяют основной Liquibase changelog;
Hibernate не создаёт и не пересоздаёт таблицы. Тесты репозитория дополнительно проверяют
структуру миграции через `information_schema`/`pg_indexes`, SQL-ограничения, фильтры,
пагинацию и конкурентные обновления. H2 не используется. Сервис, маппер и веб-контракт
также покрыты модульными тестами.

Исторические учебные примеры ниже носят экспериментальный характер и местами не соблюдают
правила чистого кода и паттерны проектирования. API Client использует отдельные DTO,
интерфейс сервиса, валидацию и контракт обработки ошибок.

### 1. Уровни изоляции

- GET http://localhost:8082/api/isolation-level/test-rr

#### Ошибка сериализации, должна упасть, если данные кто то изменил в другой транзакции, нашей транзакции мы тоже пытаемся изменить наш объект

Главный метод должен быть в Isolation.REPEATABLE_READ

- вложенные методы тоже должны присоединиться к транзакции главного в Isolation.REPEATABLE_READ
- Упадем с LockAcquisitionException
  Exception in thread "pool-1-thread-1" org.springframework.dao.CannotAcquireLockException: could not execute statement;
  SQL [n/a]; nested exception is org.hibernate.exception.LockAcquisitionException: could not execute statement
  Caused by: org.postgresql.util.PSQLException: ERROR: could not serialize access due to concurrent update
- упадет даже если есть @Version, так как изменились другие поля, и версия. Падает потому что другая транзакция, внесла
  изменения в обрабатываемую энтити
- так же нужно помнить, не вызывать другие транзакционные методы этого же сервиса, в этом же сервисе. @Transactional не
  отработает, так как прокси

#### Оптимистическая блокировка. Должно быть поле @Version в нашей Entity

- ошибка ObjectOptimisticLockingFailureException
  Exception in thread "pool-1-thread-1" org.springframework.orm.ObjectOptimisticLockingFailureException:
  Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1; statement executed:
  update drivers set comment=?, fio=?, version=? where driver_id=? and version=?;
  nested exception is org.hibernate.StaleStateException:
  Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1;
  statement executed: update drivers set comment=?, fio=?, version=? where driver_id=? and version=?

### 2. Liquibase,Testcontainers, вызов функций Postgres

- GET http://localhost:8082/api/procedure/generate/string/{length}
- Добавил Liquibase (01-init)
- Добавил функцию Postgresql (2024-06-29--01-create-function-example)
- Добавил пример импорта из csv для Liquibase (02-csv-drivers)
- Testcontainers
- Пример проливки Liquibase без деплоя сервиса - 00-example-migration-without-run-application 

### 3. Общий пулл коннектов для Liquibase и нашего приложения

- JpaConfiguration

### 4. Обработка задач

#### 4.1 LockModeType блокировки строк

- SELECT FOR UPDATE SKIP LOCKED
- LockModeType.PESSIMISTIC_WRITE - добавляет в запрос FOR UPDATE, блокирует строку от изменений на запись, но не
  блокирует на обычный select.
- https://stackoverflow.com/questions/41434169/select-for-update-skip-locked-from-jpa-level
- Если строка не должна читаться, если ее заблокировала другая транзакция, добавляем SELECT FOR UPDATE SKIP LOCKED
- Возможно без натива указать хинт

    where driver0_.id=? for update of driver0_ skip locked

    String SKIP_LOCKED = "-2";
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = AvailableSettings.JPA_LOCK_TIMEOUT, value = SKIP_LOCKED))
    @Query(value = "select d from Driver d where d.id=:id")
    Optional<Driver> tryFindById(Long id);
    
    https://postgrespro.ru/docs/postgresql/9.6/explicit-locking
    FOR UPDATE
    В режиме FOR UPDATE строки, выданные оператором SELECT, блокируются как для изменения. 
    При этом они защищаются от блокировки, изменения и удаления другими транзакциями до завершения текущей. 
    То есть другие транзакции, пытающиеся выполнить UPDATE, DELETE, SELECT FOR UPDATE, SELECT FOR NO KEY UPDATE, SELECT FOR SHARE или SELECT FOR KEY SHARE 
    с этими строками, будут заблокированы до завершения текущей транзакции; 
    и наоборот, команда SELECT FOR UPDATE будет ожидать окончания параллельной транзакции, в которой выполнилась одна из этих команд с той же строкой, 
    а затем установит блокировку и вернёт изменённую строку (или не вернёт, если она была удалена). 
    Однако в транзакции REPEATABLE READ или SERIALIZABLE возникнет ошибка, если блокируемая строка изменилась с момента начала транзакции. Подробнее это обсуждается в Разделе 13.4.

- LockModeType.PESSIMISTIC_READ - добавляем FOR SHARE

    where driver0_.id=? for share of driver0_ skip locked

    @Lock(LockModeType.PESSIMISTIC_READ)
    @QueryHints(@QueryHint(name = AvailableSettings.JPA_LOCK_TIMEOUT, value = SKIP_LOCKED))
    @Query(value = "select d from Driver d where d.id=:id")
    Optional<Driver> tryFindById(Long id);

    https://postgrespro.ru/docs/postgresql/9.6/explicit-locking
    FOR SHARE
    Действует подобно FOR NO KEY UPDATE, за исключением того, что для каждой из полученных строк запрашивается разделяемая, а не исключительная блокировка. 
    Разделяемая блокировка не позволяет другим транзакциям выполнять с этими строками UPDATE, DELETE, SELECT FOR UPDATE или SELECT FOR NO KEY UPDATE, но допускает SELECT FOR SHARE и SELECT FOR KEY SHARE.

- @Lock(LockModeType.OPTIMISTIC)
  выполняет дополнительный select записи в конце транзакции. Если данные в базе были изменены, упадет

    org.hibernate.OptimisticLockException: Newer version [9] of entity [[com.mirjdev.examplesspring.entity.Task#1]] found in database

даже если в этой транзакции было только чтение, и энтити не изменялась

    @Lock(LockModeType.OPTIMISTIC)
    Optional<Task> findById(Long id);

- LockModeType.OPTIMISTIC_FORCE_INCREMENT - как только транзакция изменила энтити, установится ExclusiveLock на строку.
  А только потом, в конце транзакции будет последний инкремент.
- LockModeType.PESSIMISTIC_FORCE_INCREMENT - лочит строку сразу, инкримент делается сразу, даже если изменений не было,
  если были, инкремент версии будет и в них

#### 4.2 Исключения в транзакциях
- исключение вызванное на стороне базы не даст сохранить энтити. Но логи в другой транзакции можно сохранить.
- проверяемые, не откатывают транзакцию
- не проверяемые исключения - откатывают, если пересекли границу методов и нет noRollbackFor
- подробнее [link](https://github.com/mirjdev/examples-spring-jpa-transaction/blob/main/src/main/java/com/mirjdev/examplesspring/service/impl/IsolationLevelFacadeImpl.java#L67)

#### 4.3 Производительность, упираемся в пул коннектов
- handlers.batch-size - размер батча для выборки задач и выполнения
- на время обработки одной задачи, открывается транзакция. - Не используем транзакции без острой необходимости (
  Propagation.NEVER)
- как правило транзакция нужна для атомарности при сохранении записей в базу, и откате в случае ошибки
- каждая незаконченная транзакция держит коннект к базе

- private final Map<StepEnum, Step> mapSteps пример мапы из бинов  

### 5. Полиморфизм на уровне базы данных и JPA [link](https://github.com/mirjdev/examples-spring-jpa-transaction/blob/main/inheritance.md)
- наследование InheritanceType.JOINED 
```text
на каждую сущность - отдельная таблица, соединение через join 
+ отдельная таблица на каждую сущность, в Postgresql есть еще и наследование
- множественные join, скорость на пару порядков может быть ниже
-/+ отдельные индексы, валидация на каждую таблицу
```
- наследование InheritanceType.SINGLE_TABLE
```text
одна таблица - для всех сущностей
+ быстрая выборка данных
- сложно сделать валидацию  данных на уровне базы
```
- наследование в PostgreSQL (inherits)
- генерация тестовых данных (generate_series)
