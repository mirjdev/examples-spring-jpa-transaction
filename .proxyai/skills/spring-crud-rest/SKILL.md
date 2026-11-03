            ---
            name: spring-crud-rest
            title: spring-crud-rest
            description: Скилл для анализа Spring Boot REST контроллеров на предмет потенциальных проблем производительности, безопасности и архитектуры.
            ---

            #При написании CRUD REST API учти:

При написании CRUD REST API учти:

### 1) Архитектура:
-  1.1 Контроллер получает данные от Сервиса, мапит сущность с помощью MapStruct
-  1.2 Используй интерфейсы для контрактов Сервиса
- пример    
```
public ResponseEntity<DriverDto> generate() {
        return ResponseEntity.ok(driverMapper.toDto(isolationLevelService.generateFirstDriver()));
    }
```
- Сервис выполняет бизнес логику и получает данные из репозитория и возвращает объект репозитория
- пример
```
public interface DriverService {
    Page<Driver> findAll(Pageable pageable);
    }
```

### 2) GET методе получения списка (GET /api/{название_сущности}):
-   2.1 Используй пагинацию Pageable и @ParameterObject и @PageableDefault. Не забудь дефолтные параметры и ограничения
-   2.2 В методе должны быть фильтры по полям сущности 

### 3) Соответствие REST-принципам. Корректный выбор HTTP методов
- GET - Получение
- POST - Создание
- PUT - Обновление (partialUpdate)
- PATCH - не делаем
- DELETE - Удаление

### 4) Для частичного обновления используй MapStruct (метод PUT)
- пример
```
    @Mapping(target = "version", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    Driver partialUpdate(DriverDto driverDto, @MappingTarget Driver driver);
```