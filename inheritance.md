### Полиморфизм


### 1. InheritanceType.JOINED

####
```sql
   insert into doc_payment ("type", cost) values('doc_payment', generate_series(1, 10000));
   insert into doc_coupon ("type", coupon) values('doc_coupon', generate_series(1, 10000));
   insert into doc_coupon_1 ("type", coupon_1) values('doc_coupon_1', generate_series(1, 10000));
   insert into doc_coupon_2 ("type", coupon_2) values('doc_coupon_2', generate_series(1, 10000));
   insert into doc_coupon_3 ("type", coupon_3) values('doc_coupon_3', generate_series(1, 10000));
   insert into doc_coupon_4 ("type", coupon_4) values('doc_coupon_4', generate_series(1, 10000));
   insert into doc_coupon_5 ("type", coupon_5) values('doc_coupon_5', generate_series(1, 10000));
   insert into doc_coupon_6 ("type", coupon_6) values('doc_coupon_6', generate_series(1, 10000));
   insert into doc_coupon_7 ("type", coupon_7) values('doc_coupon_7', generate_series(1, 10000));
   vacuum analyze;
    
   explain analyse select
        docparent0_.id as id2_9_,
        docparent0_.create_dt as create_d3_9_,
        docparent0_.type as type1_9_,
        docparent0_1_.cost as cost1_0_,
        docparent0_1_.phone as phone2_0_,
        docparent0_2_.coupon as coupon1_1_,
        docparent0_10_.cost as cost1_10_,
        docparent0_10_.source_id as source_i2_10_,
        docparent0_10_.user_id as user_id3_10_ 
    from
        public.doc_parent docparent0_ 
    left outer join
        public.doc_bonus docparent0_1_ 
    on docparent0_.id=docparent0_1_.id 
    left outer join
        public.doc_coupon docparent0_2_ 
    on docparent0_.id=docparent0_2_.id 
    left outer join
        public.doc_coupon_1 docparent0_3_ 
    on docparent0_.id=docparent0_3_.id 
    left outer join
        public.doc_coupon_2 docparent0_4_ 
    on docparent0_.id=docparent0_4_.id 
    left outer join
        public.doc_coupon_3 docparent0_5_ 
    on docparent0_.id=docparent0_5_.id 
    left outer join
        public.doc_coupon_4 docparent0_6_ 
    on docparent0_.id=docparent0_6_.id 
    left outer join
        public.doc_coupon_5 docparent0_7_ 
    on docparent0_.id=docparent0_7_.id 
    left outer join
        public.doc_coupon_6 docparent0_8_ 
    on docparent0_.id=docparent0_8_.id 
    left outer join
        public.doc_coupon_7 docparent0_9_ 
    on docparent0_.id=docparent0_9_.id 
    left outer join
        public.doc_payment docparent0_10_ 
    on docparent0_.id=docparent0_10_.id
    
    Gather  (cost=4761.75..15893.49 rows=90004 width=153) (actual time=31.650..63.066 rows=90003 loops=1)
  Workers Planned: 2
  Workers Launched: 2
  ->  Hash Left Join  (cost=3761.75..5893.09 rows=37502 width=153) (actual time=14.405..37.023 rows=30001 loops=3)
        Hash Cond: (docparent0_.id = docparent0_10_.id)
        ->  Parallel Hash Left Join  (cost=3452.73..5485.63 rows=37502 width=116) (actual time=12.452..30.586 rows=30001 loops=3)
              Hash Cond: (docparent0_.id = docparent0_2_.id)
              ->  Parallel Hash Left Join  (cost=1726.87..3480.27 rows=37502 width=74) (actual time=6.227..17.228 rows=30001 loops=3)
                    Hash Cond: (docparent0_.id = docparent0_1_.id)
                    ->  Parallel Append  (cost=0.00..1473.92 rows=37498 width=29) (actual time=0.007..4.417 rows=30001 loops=3)
                          ->  Parallel Seq Scan on doc_payment docparent0__2  (cost=0.00..142.83 rows=5883 width=28) (actual time=0.007..1.186 rows=10001 loops=1)
                          ->  Parallel Seq Scan on doc_coupon docparent0__4  (cost=0.00..142.83 rows=5883 width=27) (actual time=0.005..0.957 rows=10001 loops=1)
                          ->  Parallel Seq Scan on doc_coupon_1 docparent0__5  (cost=0.00..142.82 rows=5882 width=29) (actual time=0.002..0.746 rows=10000 loops=1)
                          ->  Parallel Seq Scan on doc_coupon_2 docparent0__6  (cost=0.00..142.82 rows=5882 width=29) (actual time=0.003..0.945 rows=10000 loops=1)
                          ->  Parallel Seq Scan on doc_coupon_3 docparent0__7  (cost=0.00..142.82 rows=5882 width=29) (actual time=0.009..0.856 rows=10000 loops=1)
                          ->  Parallel Seq Scan on doc_coupon_4 docparent0__8  (cost=0.00..142.82 rows=5882 width=29) (actual time=0.007..0.428 rows=3333 loops=3)
                          ->  Parallel Seq Scan on doc_coupon_5 docparent0__9  (cost=0.00..142.82 rows=5882 width=29) (actual time=0.005..0.416 rows=5000 loops=2)
                          ->  Parallel Seq Scan on doc_coupon_6 docparent0__10  (cost=0.00..142.82 rows=5882 width=29) (actual time=0.003..0.929 rows=10000 loops=1)
                          ->  Parallel Seq Scan on doc_coupon_7 docparent0__11  (cost=0.00..142.82 rows=5882 width=29) (actual time=0.002..1.167 rows=10000 loops=1)
                          ->  Parallel Seq Scan on doc_bonus docparent0__3  (cost=0.00..1.01 rows=1 width=26) (actual time=0.004..0.005 rows=1 loops=1)
                          ->  Parallel Seq Scan on doc_parent docparent0__1  (cost=0.00..0.00 rows=1 width=532) (actual time=0.002..0.002 rows=0 loops=1)
                    ->  Parallel Hash  (cost=1310.25..1310.25 rows=33330 width=54) (actual time=6.080..6.083 rows=26667 loops=3)
                          Buckets: 131072  Batches: 1  Memory Usage: 4192kB
                          ->  Parallel Append  (cost=0.00..1310.25 rows=33330 width=54) (actual time=0.006..3.176 rows=26667 loops=3)
                                ->  Parallel Seq Scan on doc_coupon docparent0_1__2  (cost=0.00..142.83 rows=5883 width=25) (actual time=0.004..0.867 rows=10001 loops=1)
                                ->  Parallel Seq Scan on doc_coupon_1 docparent0_1__3  (cost=0.00..142.82 rows=5882 width=58) (actual time=0.008..0.905 rows=10000 loops=1)
                                ->  Parallel Seq Scan on doc_coupon_2 docparent0_1__4  (cost=0.00..142.82 rows=5882 width=58) (actual time=0.006..0.294 rows=3333 loops=3)
                                ->  Parallel Seq Scan on doc_coupon_3 docparent0_1__5  (cost=0.00..142.82 rows=5882 width=58) (actual time=0.005..0.359 rows=5000 loops=2)
                                ->  Parallel Seq Scan on doc_coupon_4 docparent0_1__6  (cost=0.00..142.82 rows=5882 width=58) (actual time=0.003..0.669 rows=10000 loops=1)
                                ->  Parallel Seq Scan on doc_coupon_5 docparent0_1__7  (cost=0.00..142.82 rows=5882 width=58) (actual time=0.002..0.663 rows=10000 loops=1)
                                ->  Parallel Seq Scan on doc_coupon_6 docparent0_1__8  (cost=0.00..142.82 rows=5882 width=58) (actual time=0.002..0.664 rows=10000 loops=1)
                                ->  Parallel Seq Scan on doc_coupon_7 docparent0_1__9  (cost=0.00..142.82 rows=5882 width=58) (actual time=0.002..0.710 rows=10000 loops=1)
                                ->  Parallel Seq Scan on doc_bonus docparent0_1__1  (cost=0.00..1.01 rows=1 width=27) (actual time=0.003..0.003 rows=1 loops=1)
              ->  Parallel Hash  (cost=1309.24..1309.24 rows=33329 width=50) (actual time=6.015..6.018 rows=26667 loops=3)
                    Buckets: 131072  Batches: 1  Memory Usage: 4256kB
                    ->  Parallel Append  (cost=0.00..1309.24 rows=33329 width=50) (actual time=0.008..9.057 rows=80001 loops=1)
                          ->  Parallel Seq Scan on doc_coupon docparent0_2__1  (cost=0.00..142.83 rows=5883 width=12) (actual time=0.003..0.696 rows=10001 loops=1)
                          ->  Parallel Seq Scan on doc_coupon_1 docparent0_2__2  (cost=0.00..142.82 rows=5882 width=56) (actual time=0.002..0.723 rows=10000 loops=1)
                          ->  Parallel Seq Scan on doc_coupon_2 docparent0_2__3  (cost=0.00..142.82 rows=5882 width=56) (actual time=0.002..0.666 rows=10000 loops=1)
                          ->  Parallel Seq Scan on doc_coupon_3 docparent0_2__4  (cost=0.00..142.82 rows=5882 width=56) (actual time=0.002..0.661 rows=10000 loops=1)
                          ->  Parallel Seq Scan on doc_coupon_4 docparent0_2__5  (cost=0.00..142.82 rows=5882 width=56) (actual time=0.006..0.714 rows=10000 loops=1)
                          ->  Parallel Seq Scan on doc_coupon_5 docparent0_2__6  (cost=0.00..142.82 rows=5882 width=56) (actual time=0.002..0.708 rows=10000 loops=1)
                          ->  Parallel Seq Scan on doc_coupon_6 docparent0_2__7  (cost=0.00..142.82 rows=5882 width=56) (actual time=0.002..0.712 rows=10000 loops=1)
                          ->  Parallel Seq Scan on doc_coupon_7 docparent0_2__8  (cost=0.00..142.82 rows=5882 width=56) (actual time=0.007..0.719 rows=10000 loops=1)
        ->  Hash  (cost=184.01..184.01 rows=10001 width=45) (actual time=1.899..1.899 rows=10001 loops=3)
              Buckets: 16384  Batches: 1  Memory Usage: 597kB
              ->  Seq Scan on doc_payment docparent0_10_  (cost=0.00..184.01 rows=10001 width=45) (actual time=0.041..0.876 rows=10001 loops=3)
Planning Time: 0.617 ms
Execution Time: 65.463 ms
```


#### JOINED couponRepository.findAll().toString();
```sql
    select
        doccoupon0_.id as id2_9_,
        doccoupon0_2_.create_dt as create_d3_9_,
        doccoupon0_2_.type as type1_9_,
        doccoupon0_1_.cost as cost1_0_,
        doccoupon0_1_.phone as phone2_0_,
        doccoupon0_.coupon as coupon1_1_ 
    from
        public.doc_coupon doccoupon0_ 
    inner join
        public.doc_bonus doccoupon0_1_ 
            on doccoupon0_.id=doccoupon0_1_.id 
    inner join
        public.doc_parent doccoupon0_2_ 
            on doccoupon0_.id=doccoupon0_2_.id 
    left outer join
        public.doc_coupon_1 doccoupon0_3_ 
            on doccoupon0_.id=doccoupon0_3_.id 
    left outer join
        public.doc_coupon_2 doccoupon0_4_ 
            on doccoupon0_.id=doccoupon0_4_.id 
    left outer join
        public.doc_coupon_3 doccoupon0_5_ 
            on doccoupon0_.id=doccoupon0_5_.id 
    left outer join
        public.doc_coupon_4 doccoupon0_6_ 
            on doccoupon0_.id=doccoupon0_6_.id 
    left outer join
        public.doc_coupon_5 doccoupon0_7_ 
            on doccoupon0_.id=doccoupon0_7_.id 
    left outer join
        public.doc_coupon_6 doccoupon0_8_ 
            on doccoupon0_.id=doccoupon0_8_.id 
    left outer join
        public.doc_coupon_7 doccoupon0_9_ 
            on doccoupon0_.id=doccoupon0_9_.id
```

### 2. InheritanceType.SINGLE_TABLE

#### SINGLE docParentRepository.findAll()
```sql
insert into st_doc_parent ("type", cost) values('doc_payment', generate_series(1, 10000));
insert into st_doc_parent ("type", coupon) values('doc_coupon', generate_series(1, 10000));
insert into st_doc_parent ("type", coupon_1) values('doc_coupon_1', generate_series(1, 10000));
insert into st_doc_parent ("type", coupon_2) values('doc_coupon_2', generate_series(1, 10000));
insert into st_doc_parent ("type", coupon_3) values('doc_coupon_3', generate_series(1, 10000));
insert into st_doc_parent ("type", coupon_4) values('doc_coupon_4', generate_series(1, 10000));
insert into st_doc_parent ("type", coupon_5) values('doc_coupon_5', generate_series(1, 10000));
insert into st_doc_parent ("type", coupon_6) values('doc_coupon_6', generate_series(1, 10000));
insert into st_doc_parent ("type", coupon_7) values('doc_coupon_7', generate_series(1, 10000));

select
        stdocparen0_.id as id2_12_,
        stdocparen0_.create_dt as create_d3_12_,
        stdocparen0_.type as type1_12_,
        stdocparen0_.cost as cost4_12_,
        stdocparen0_.phone as phone5_12_,
        stdocparen0_.coupon as coupon6_12_,
        stdocparen0_.source_id as source_i7_12_,
        stdocparen0_.user_id as user_id8_12_ 
    from
        public.st_doc_parent stdocparen0_

    Seq Scan on st_doc_parent stdocparen0_  (cost=0.00..1742.03 rows=90003 width=101) (actual time=0.010..7.385 rows=90003 loops=1)
    Planning Time: 0.049 ms
    Execution Time: 9.268 ms
```


#### SINGLE couponRepository.findAll().toString();
```sql
    select
        stdoccoupo0_.id as id2_12_,
        stdoccoupo0_.create_dt as create_d3_12_,
        stdoccoupo0_.type as type1_12_,
        stdoccoupo0_.cost as cost4_12_,
        stdoccoupo0_.phone as phone5_12_,
        stdoccoupo0_.coupon as coupon6_12_
    from
        public.st_doc_parent stdoccoupo0_
    where
        stdoccoupo0_.type in (
                              'doc_coupon', 'doc_coupon_1', 'doc_coupon_2', 'doc_coupon_3', 'doc_coupon_4', 'doc_coupon_5', 'doc_coupon_6', 'doc_coupon_7'
            )
        
        Seq Scan on st_doc_parent stdoccoupo0_  (cost=0.00..2642.06 rows=79995 width=49) (actual time=0.012..15.066 rows=80001 loops=1)
        Filter: ((type)::text = ANY ('{doc_coupon,doc_coupon_1,doc_coupon_2,doc_coupon_3,doc_coupon_4,doc_coupon_5,doc_coupon_6,doc_coupon_7}'::text[]))
        Rows Removed by Filter: 10002
        Planning Time: 0.087 ms
        Execution Time: 16.821 ms
```
#### Удаление только из наследников
```sql
-- удаление из детей, но оставляем в родительской (реализация архива и оперативной таблицы)
begin;
lock table doc_parent;
lock table doc_coupon;
insert into public.doc_parent(id,type,create_dt) (select id,type,create_dt from only public.doc_coupon);
delete from only public.doc_coupon;
commit;
```