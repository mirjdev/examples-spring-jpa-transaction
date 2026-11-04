Session statistics
LLM time
11m21s
Tool time
14m47s
Avg time to first token (TTFT)
2s
Tokens per second (TPS)
215 tok/s


Token usage
9,861,240 tok
Cache hit
99%
Uncached input
119,433 tok
Cached input
9,631,232 tok
Output
110,575 tok

¥0.76 CNY потратил, примерно 10 рублей
 
Готово. CRUD для Client реализован отдельным модулем com.mirjdev.examplesspring.client.

Что добавлено
Сущность Client: id, email, fio, phone, created_at, version.
Репозиторий ClientRepository на базе JpaRepository + JpaSpecificationExecutor.
Сервисный контракт ClientService и реализация ClientServiceImpl.
MapStruct-маппер ClientMapper с partial update.
DTO: ClientCreateRequest, ClientUpdateRequest, ClientResponse, ClientFilter, PageResponse.
Поиск через JPA Specifications ClientSpecifications с экранированием %, _ и \.
REST-контроллер ClientController.
Единая обработка ошибок в формате RFC 9457 Problem Details: ClientExceptionHandler.
Liquibase-миграция: 2026-11-04--01-create-table-clients.xml, подключена в changelog-v_1.0-cumulative.xml.
JaCoCo с порогом 80% в build.gradle.
Описание API в README.md.
API
Метод	Путь	Результат
POST	/api/clients	201 Created + Location
GET	/api/clients/{id}	200 или 404
GET	/api/clients	страница клиентов
PUT	/api/clients/{id}	обновление, обязателен version
DELETE	/api/clients/{id}	204 No Content
Параметры списка:

Code block


page=0&size=20&sort=createdAt,desc
query=ivan
fio=Иванов&email=example.com&phone=999
createdFrom=2026-01-01T00:00:00Z&createdTo=2026-12-31T23:59:59Z
Особенности:

size ограничен 100;
сортировка разрешена только по id, email, fio, phone, createdAt, version;
query ищет без учёта регистра сразу по fio, email, phone;
при обновлении проверяется version, при конфликте возвращается 409 Conflict;
ошибки валидации, дубли email и конкурентные изменения возвращаются в едином ProblemDetail.
Тесты и покрытие
Добавлены интеграционные и unit-тесты:

ClientApiIntegrationTest — MockMvc, H2, реальная Liquibase-миграция и ddl-auto=validate;
ClientServiceImplTest;
ClientMapperTest;
ClientExceptionHandlerTest;
ClientEntityTest;
ClientContextSmokeTest.
Результат полного прогона:

Code block


tests=58, failures=0, errors=0
LINE coverage:        96.64%
BRANCH coverage:      86.49%
INSTRUCTION coverage: 96.82%
Проверка выполняется командой:

bash


./gradlew check
Она запускает тесты, генерирует JaCoCo-отчёт и проверяет минимальный порог 80% строк по клиентскому модулю. Полный clean test и check проходят успешно.


Edited 9 files
+81
-13

README.md
+42
-1

build.gradle
+6
-0

src/main/java/com/mirjdev/examplesspring/client/ClientRepository.java
+2
-2

src/main/java/com/mirjdev/examplesspring/client/ClientServiceImpl.java
+3
-3

src/main/java/com/mirjdev/examplesspring/client/ClientSpecifications.java
+1
-1

src/main/java/com/mirjdev/examplesspring/client/exception/ClientExceptionHandler.java
+12
-1

src/test/java/com/mirjdev/examplesspring/client/ClientApiIntegrationTest.java
+0
-1

src/test/java/com/mirjdev/examplesspring/client/ClientServiceImplTest.java
+4
-4

src/test/java/com/mirjdev/examplesspring/client/exception/ClientExceptionHandlerTest.java
+11
-0

Collapse