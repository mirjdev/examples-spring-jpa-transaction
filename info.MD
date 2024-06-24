### Запуск базы
docker run --name psql-dev -d -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=root -e POSTGRES_DB=dev -p 54321:5432 --rm postgres:14.6
docker stop psql-dev

Проект носит личный характер, в нем не соблюдаются правила чистого кода, паттерны проектирования.

Ошибка сериализации
Головной метод должен быть в Isolation.REPEATABLE_READ
- вложенные методы тоже должны присоединиться к транзакции главного в Isolation.REPEATABLE_READ
- Упадем с LockAcquisitionException
  Exception in thread "pool-1-thread-1" org.springframework.dao.CannotAcquireLockException: could not execute statement; SQL [n/a]; nested exception is org.hibernate.exception.LockAcquisitionException: could not execute statement
  Caused by: org.postgresql.util.PSQLException: ERROR: could not serialize access due to concurrent update
- упадет даже если есть @Version, так как изменились другие поля, и версия. Падает потому что другая транзакция, внесла изменения в обрабатываемую энтити
- так же нужно помнить, не вызывать другие транзакционные методы этого же сервиса, в этом же сервисе. @Transactional не отработает, так как прокси

Оптимистическая блокировка
- ошибка ObjectOptimisticLockingFailureException
  Exception in thread "pool-1-thread-1" org.springframework.orm.ObjectOptimisticLockingFailureException:
  Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1; statement executed:
  update drivers set comment=?, fio=?, version=? where driver_id=? and version=?;
  nested exception is org.hibernate.StaleStateException:
  Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1;
  statement executed: update drivers set comment=?, fio=?, version=? where driver_id=? and version=?

