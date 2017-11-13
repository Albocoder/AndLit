CREATE TABLE `user` (
  uid      bigint(19) NOT NULL, 
  username varchar(30) NOT NULL UNIQUE, 
  password char(64) NOT NULL, 
  PRIMARY KEY (uid)) engine=InnoDB CHARACTER SET UTF8;
CREATE TABLE API_keys (
  uid   bigint(19) NOT NULL, 
  `key` char(32) NOT NULL, 
  PRIMARY KEY (uid)) engine=InnoDB CHARACTER SET UTF8;
CREATE TABLE profile (
  uid     bigint(19) NOT NULL, 
  name    varchar(30), 
  sname   varchar(30), 
  dob     date, 
  age     smallint(5), 
  address varchar(60), 
  PRIMARY KEY (uid)) engine=InnoDB CHARACTER SET UTF8;
CREATE TABLE user_photos (
  uid         bigint(19) NOT NULL, 
  pid         bigint(19) NOT NULL, 
  path        varchar(50) NOT NULL, 
  md5_hash    char(32) NOT NULL, 
  description varchar(100), 
  PRIMARY KEY (uid, 
  pid)) engine=InnoDB CHARACTER SET UTF8;
CREATE TABLE social_media_profile (
  uid         bigint(19) NOT NULL, 
  smedia_name varchar(30) NOT NULL, 
  profile_id  varchar(50) NOT NULL, 
  PRIMARY KEY (uid)) engine=InnoDB CHARACTER SET UTF8;
CREATE TABLE classifier (
  uid      bigint(19) NOT NULL, 
  c_path   varchar(50) NOT NULL UNIQUE, 
  md5_hash char(32) NOT NULL, 
  PRIMARY KEY (uid)) engine=InnoDB CHARACTER SET UTF8;
CREATE TABLE `group` (
  gid           bigint(19) NOT NULL, 
  name          varchar(50) NOT NULL UNIQUE, 
  bio           varchar(100), 
  minpermission smallint(5) NOT NULL, 
  member_count  smallint(5) NOT NULL, 
  PRIMARY KEY (gid)) engine=InnoDB CHARACTER SET UTF8;
CREATE TABLE user_joins (
  uid          bigint(19) NOT NULL, 
  gid          bigint(19) NOT NULL, 
  join_date    date NOT NULL, 
  role_lvl     smallint(5) NOT NULL, 
  rel_perm_lvl smallint(5) NOT NULL, 
  PRIMARY KEY (uid, 
  gid)) engine=InnoDB CHARACTER SET UTF8;
CREATE TABLE user_photo_tags (
  uid   bigint(19) NOT NULL, 
  pid   bigint(19) NOT NULL, 
  `tag` varchar(20) NOT NULL, 
  PRIMARY KEY (uid, 
  pid)) engine=InnoDB CHARACTER SET UTF8;
CREATE TABLE group_photos (
  gid         bigint(19) NOT NULL, 
  pid         bigint(19) NOT NULL, 
  path        varchar(50) NOT NULL, 
  md5_hash    char(32) NOT NULL, 
  description varchar(100), 
  PRIMARY KEY (gid, 
  pid)) engine=InnoDB CHARACTER SET UTF8;
CREATE TABLE Debt (
  did    bigint(19) NOT NULL AUTO_INCREMENT, 
  amount int(10) NOT NULL, 
  state  varchar(10) NOT NULL, 
  PRIMARY KEY (did)) engine=InnoDB CHARACTER SET UTF8;
CREATE TABLE training_log (
  tid         bigint(19) NOT NULL AUTO_INCREMENT, 
  cpu_time    int(10), 
  cpu_cores   int(10), 
  gpu_time    int(10), 
  gpu_cores   int(10), 
  memory      int(10), 
  instance_no int(10) NOT NULL, 
  PRIMARY KEY (tid)) engine=InnoDB CHARACTER SET UTF8;
CREATE TABLE group_photo_tags (
  gid   bigint(19) NOT NULL, 
  pid   bigint(19) NOT NULL, 
  `tag` varchar(20) NOT NULL, 
  PRIMARY KEY (gid, 
  pid)) engine=InnoDB CHARACTER SET UTF8;
CREATE TABLE user_has_debt (
  uid bigint(19) NOT NULL, 
  did bigint(19) NOT NULL, 
  tid bigint(19) NOT NULL, 
  PRIMARY KEY (uid, 
  did, 
  tid)) engine=InnoDB CHARACTER SET UTF8;
ALTER TABLE API_keys ADD INDEX FKAPI_keys156284 (uid), ADD CONSTRAINT FKAPI_keys156284 FOREIGN KEY (uid) REFERENCES `user` (uid);
ALTER TABLE profile ADD INDEX FKprofile595165 (uid), ADD CONSTRAINT FKprofile595165 FOREIGN KEY (uid) REFERENCES `user` (uid);
ALTER TABLE user_photos ADD INDEX FKuser_photo252499 (uid), ADD CONSTRAINT FKuser_photo252499 FOREIGN KEY (uid) REFERENCES `user` (uid);
ALTER TABLE social_media_profile ADD INDEX FKsocial_med585111 (uid), ADD CONSTRAINT FKsocial_med585111 FOREIGN KEY (uid) REFERENCES `user` (uid);
ALTER TABLE classifier ADD INDEX FKclassifier639817 (uid), ADD CONSTRAINT FKclassifier639817 FOREIGN KEY (uid) REFERENCES `user` (uid);
ALTER TABLE user_joins ADD INDEX FKuser_joins186506 (uid), ADD CONSTRAINT FKuser_joins186506 FOREIGN KEY (uid) REFERENCES `user` (uid);
ALTER TABLE user_joins ADD INDEX FKuser_joins189633 (gid), ADD CONSTRAINT FKuser_joins189633 FOREIGN KEY (gid) REFERENCES `group` (gid);
ALTER TABLE user_photo_tags ADD INDEX FKuser_photo921449 (uid, pid), ADD CONSTRAINT FKuser_photo921449 FOREIGN KEY (uid, pid) REFERENCES user_photos (uid, pid);
ALTER TABLE group_photos ADD INDEX FKgroup_phot811455 (gid), ADD CONSTRAINT FKgroup_phot811455 FOREIGN KEY (gid) REFERENCES `group` (gid);
ALTER TABLE group_photo_tags ADD INDEX FKgroup_phot819985 (gid, pid), ADD CONSTRAINT FKgroup_phot819985 FOREIGN KEY (gid, pid) REFERENCES group_photos (gid, pid);
ALTER TABLE user_has_debt ADD INDEX FKuser_has_d740793 (uid), ADD CONSTRAINT FKuser_has_d740793 FOREIGN KEY (uid) REFERENCES `user` (uid);
ALTER TABLE user_has_debt ADD INDEX FKuser_has_d234814 (did), ADD CONSTRAINT FKuser_has_d234814 FOREIGN KEY (did) REFERENCES Debt (did);
ALTER TABLE user_has_debt ADD INDEX FKuser_has_d192330 (tid), ADD CONSTRAINT FKuser_has_d192330 FOREIGN KEY (tid) REFERENCES training_log (tid);
