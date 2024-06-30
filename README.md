### Запуск базы
docker run --name psql-dev -d -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=root -e POSTGRES_DB=dev -p 54321:5432 --rm postgres:14.6
docker stop psql-dev

Проект носит личный характер, в нем зачастую не соблюдаются правила чистого кода, паттерны проектирования.
Нет DTO, интерфейсов для сервисов, контрактов для API. Не делайте так :) Может когда-нибудь найду время, исправлю


### 1. Уровни изоляции

- GET http://localhost:8082/api/isolation-level/test-rr

#### Ошибка сериализации, должна упасть, если данные кто то изменил в другой транзакции, нашей транзакции мы тоже пытаемся изменить наш объект
Главный метод должен быть в Isolation.REPEATABLE_READ
- вложенные методы тоже должны присоединиться к транзакции главного в Isolation.REPEATABLE_READ
- Упадем с LockAcquisitionException
  Exception in thread "pool-1-thread-1" org.springframework.dao.CannotAcquireLockException: could not execute statement; SQL [n/a]; nested exception is org.hibernate.exception.LockAcquisitionException: could not execute statement
  Caused by: org.postgresql.util.PSQLException: ERROR: could not serialize access due to concurrent update
- упадет даже если есть @Version, так как изменились другие поля, и версия. Падает потому что другая транзакция, внесла изменения в обрабатываемую энтити
- так же нужно помнить, не вызывать другие транзакционные методы этого же сервиса, в этом же сервисе. @Transactional не отработает, так как прокси

#### Оптимистическая блокировка. Должно быть поле @Version в нашей Entity 
- ошибка ObjectOptimisticLockingFailureException
  Exception in thread "pool-1-thread-1" org.springframework.orm.ObjectOptimisticLockingFailureException:
  Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1; statement executed:
  update drivers set comment=?, fio=?, version=? where driver_id=? and version=?;
  nested exception is org.hibernate.StaleStateException:
  Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1;
  statement executed: update drivers set comment=?, fio=?, version=? where driver_id=? and version=?

### 2. Вызов функций Postgres
- GET http://localhost:8082/api/procedure/generate/string/{length}
- Добавил Liquibase (01-init) 
- Добавил функцию Postgresql (2024-06-29--01-create-function-example)
- Добавил пример импорта из csv для Liquibase (02-csv-drivers) 
- Testcontainers

### 3. Общий пулл коннектов для Liquibase и нашего приложения
- JpaConfiguration