CREATE TABLE IF NOT EXISTS API_keys (
  fb       integer(10), 
  twitter  integer(10), 
  insta    integer(10), 
  linkedin integer(10));

CREATE TABLE IF NOT EXISTS Classifier (
  path        varchar(50) NOT NULL, 
  hash        varchar(32) NOT NULL, 
  last_update date NOT NULL, 
  num_recogn  integer(10) NOT NULL);

CREATE TABLE IF NOT EXISTS detectedfaces (
  id         integer(10) NOT NULL, 
  path       varchar(50) NOT NULL, 
  hash       varchar(32) NOT NULL, 
  date_taken date NOT NULL, 
  PRIMARY KEY (id), 
  FOREIGN KEY(id) REFERENCES KnownPPL(id));

CREATE TABLE IF NOT EXISTS KnownPPL (
  id      integer(10) NOT NULL, 
  name    varchar(30), 
  sname   varchar(30), 
  dob     date, 
  age     smallint(5), 
  address varchar(60), 
  PRIMARY KEY (id));

CREATE TABLE IF NOT EXISTS misc_info (
  id        integer(10) NOT NULL,
  key       varchar(30) NOT NULL,
  desc      varchar(100) NOT NULL,
  PRIMARY KEY (id), 
  FOREIGN KEY(id) REFERENCES KnownPPL(id));

CREATE TABLE IF NOT EXISTS UserLogin (
  id           INTEGER NOT NULL PRIMARY KEY, 
  username     varchar(30) NOT NULL, 
  access_token varchar(32) NOT NULL);

