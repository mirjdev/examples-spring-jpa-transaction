Промт:
Я хочу локально запустить отказоустойчивый кластер Postgresql + patroni
Составь docker-compose.yml
пароли везде: postgres
1 мастер, и 2 слейва в асинхронном режиме 
данные и конфигурации не должны теряться, сделай монтируемые разделы 


mkdir -p -m 0755 config
docker compose up -d --build
docker compose exec pg1 patronictl -c /config/patroni.yml list

docker compose -f docker-compose.yml up -d init-dev-db
docker compose exec pg1 bash
psql -h haproxy -p 5432 -U postgres -d postgres -W
CREATE DATABASE dev;

# статистика
patronictl -c /config/patroni.yml list postgres-ha
# Переключение pg2 -> pg1
patronictl -c /config/patroni.yml switchover postgres-ha --leader pg2 --candidate pg1 --scheduled now


# Режим обслуживание
patronictl -c /config/patroni.yml pause postgres-ha --wait
patronictl -c /config/patroni.yml resume postgres-ha --wait
