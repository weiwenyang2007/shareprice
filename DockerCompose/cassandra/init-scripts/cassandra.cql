CREATE KEYSPACE IF NOT EXISTS EasyStoGu WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};

USE EasyStoGu;

CREATE TABLE IF NOT EXISTS ind_macd
(
  stockid text,
  date text,
  dif double,
  dea double,
  macd double,
  PRIMARY KEY (stockid, date)
)WITH gc_grace_seconds = 86400
AND compaction = {'class': 'SizeTieredCompactionStrategy'}
AND caching = {'keys': 'ALL'};

CREATE TABLE IF NOT EXISTS ind_kdj
(
  stockid text,
  date text,
  k double,
  d double,
  j double,
  rsv double,
  PRIMARY KEY (stockid, date)
)WITH gc_grace_seconds = 86400
AND compaction = {'class': 'SizeTieredCompactionStrategy'}
AND caching = {'keys': 'ALL'};

CREATE TABLE IF NOT EXISTS ind_boll
(
  stockid text,
  date text,
  mb double,
  up double,
  dn double,
  PRIMARY KEY (stockid, date)
)WITH gc_grace_seconds = 86400
AND compaction = {'class': 'SizeTieredCompactionStrategy'}
AND caching = {'keys': 'ALL'};

CREATE TABLE IF NOT EXISTS ind_shenxian
(
  stockid text,
  date text,
  h1 double,
  h2 double,
  h3 double,
  PRIMARY KEY (stockid, date)
)WITH gc_grace_seconds = 86400
AND compaction = {'class': 'SizeTieredCompactionStrategy'}
AND caching = {'keys': 'ALL'};

CREATE TABLE IF NOT EXISTS ind_qsdd
(
  stockid text,
  date text,
  lonterm double,
  shoterm double,
  midterm double,
  PRIMARY KEY (stockid, date)
)WITH gc_grace_seconds = 86400
AND compaction = {'class': 'SizeTieredCompactionStrategy'}
AND caching = {'keys': 'ALL'};

CREATE TABLE IF NOT EXISTS ind_wr
(
  stockid text,
  date text,
  lonterm double,
  shoterm double,
  midterm double,
  PRIMARY KEY (stockid, date)
)WITH gc_grace_seconds = 86400
AND compaction = {'class': 'SizeTieredCompactionStrategy'}
AND caching = {'keys': 'ALL'};

CREATE TABLE IF NOT EXISTS ind_week_macd
(
  stockid text,
  date text,
  dif double,
  dea double,
  macd double,
  PRIMARY KEY (stockid, date)
)WITH gc_grace_seconds = 86400
AND compaction = {'class': 'SizeTieredCompactionStrategy'}
AND caching = {'keys': 'ALL'};

CREATE TABLE IF NOT EXISTS ind_week_kdj
(
  stockid text,
  date text,
  k double,
  d double,
  j double,
  rsv double,
  PRIMARY KEY (stockid, date)
)WITH gc_grace_seconds = 86400
AND compaction = {'class': 'SizeTieredCompactionStrategy'}
AND caching = {'keys': 'ALL'};
