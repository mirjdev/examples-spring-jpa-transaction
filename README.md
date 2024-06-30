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

### 4. Обработка задач

#### 4.1 LockModeType блокировки строк
- SELECT FOR UPDATE SKIP LOCKED 
- LockModeType.PESSIMISTIC_WRITE - добавляет в запрос FOR UPDATE, блокирует строку от изменений на запись, но не блокирует на обычный select.
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

#### 4.2 Исключения (TODO добавить описание)
- на стороне базы
- проверяемые и не проверяемые

#### 4.3 Производительность, упираемся в пулл коннектов
- На время обработки одной задачи, открывается транзакция и блокирует строку.
- Каждая незаконченная транзакция держит коннект к базе