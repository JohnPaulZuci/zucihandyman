-- MySQL dump 10.17  Distrib 10.3.22-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: spw_config
-- ------------------------------------------------------
-- Server version	10.3.22-MariaDB-1:10.3.22+maria~bionic-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `QRTZ_BLOB_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_BLOB_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_BLOB_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(190) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  `BLOB_DATA` blob DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `SCHED_NAME` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_BLOB_TRIGGERS`
--

LOCK TABLES `QRTZ_BLOB_TRIGGERS` WRITE;
/*!40000 ALTER TABLE `QRTZ_BLOB_TRIGGERS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_BLOB_TRIGGERS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_CALENDARS`
--

DROP TABLE IF EXISTS `QRTZ_CALENDARS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_CALENDARS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `CALENDAR_NAME` varchar(190) NOT NULL,
  `CALENDAR` blob NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`CALENDAR_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_CALENDARS`
--

LOCK TABLES `QRTZ_CALENDARS` WRITE;
/*!40000 ALTER TABLE `QRTZ_CALENDARS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_CALENDARS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_CRON_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_CRON_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_CRON_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(190) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  `CRON_EXPRESSION` varchar(120) NOT NULL,
  `TIME_ZONE_ID` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_CRON_TRIGGERS`
--

LOCK TABLES `QRTZ_CRON_TRIGGERS` WRITE;
/*!40000 ALTER TABLE `QRTZ_CRON_TRIGGERS` DISABLE KEYS */;
INSERT INTO `QRTZ_CRON_TRIGGERS` VALUES ('quartzScheduler','assign.transform#1','assign.transform#1','0 0/30 6-8 ? * SAT,SUN','Asia/Kolkata'),('quartzScheduler','com.jc.dropbox.to.s3#2','com.jc.dropbox.to.s3#2','0 0/10 6-23 * * ?','Asia/Kolkata');
/*!40000 ALTER TABLE `QRTZ_CRON_TRIGGERS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_FIRED_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_FIRED_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_FIRED_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `ENTRY_ID` varchar(95) NOT NULL,
  `TRIGGER_NAME` varchar(190) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  `INSTANCE_NAME` varchar(190) NOT NULL,
  `FIRED_TIME` bigint(13) NOT NULL,
  `SCHED_TIME` bigint(13) NOT NULL,
  `PRIORITY` int(11) NOT NULL,
  `STATE` varchar(16) NOT NULL,
  `JOB_NAME` varchar(190) DEFAULT NULL,
  `JOB_GROUP` varchar(190) DEFAULT NULL,
  `IS_NONCONCURRENT` varchar(1) DEFAULT NULL,
  `REQUESTS_RECOVERY` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`ENTRY_ID`),
  KEY `IDX_QRTZ_FT_TRIG_INST_NAME` (`SCHED_NAME`,`INSTANCE_NAME`),
  KEY `IDX_QRTZ_FT_INST_JOB_REQ_RCVRY` (`SCHED_NAME`,`INSTANCE_NAME`,`REQUESTS_RECOVERY`),
  KEY `IDX_QRTZ_FT_J_G` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_FT_JG` (`SCHED_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_FT_T_G` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `IDX_QRTZ_FT_TG` (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_FIRED_TRIGGERS`
--

LOCK TABLES `QRTZ_FIRED_TRIGGERS` WRITE;
/*!40000 ALTER TABLE `QRTZ_FIRED_TRIGGERS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_FIRED_TRIGGERS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_JOB_DETAILS`
--

DROP TABLE IF EXISTS `QRTZ_JOB_DETAILS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_JOB_DETAILS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `JOB_NAME` varchar(190) NOT NULL,
  `JOB_GROUP` varchar(190) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `JOB_CLASS_NAME` varchar(250) NOT NULL,
  `IS_DURABLE` varchar(1) NOT NULL,
  `IS_NONCONCURRENT` varchar(1) NOT NULL,
  `IS_UPDATE_DATA` varchar(1) NOT NULL,
  `REQUESTS_RECOVERY` varchar(1) NOT NULL,
  `JOB_DATA` blob DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_J_REQ_RECOVERY` (`SCHED_NAME`,`REQUESTS_RECOVERY`),
  KEY `IDX_QRTZ_J_GRP` (`SCHED_NAME`,`JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_JOB_DETAILS`
--

LOCK TABLES `QRTZ_JOB_DETAILS` WRITE;
/*!40000 ALTER TABLE `QRTZ_JOB_DETAILS` DISABLE KEYS */;
INSERT INTO `QRTZ_JOB_DETAILS` VALUES ('quartzScheduler','6c88f5a3-bd8f-4c1f-9964-bbee214b1736','assign.transform#1','Pipeline Execution','com.zuci.zio.job.Job','1','0','0','0','¬í\0sr\0org.quartz.JobDataMapŸ°ƒè¿©°Ë\0\0xr\0&org.quartz.utils.StringKeyDirtyFlagMap‚èÃûÅ](\0Z\0allowsTransientDataxr\0org.quartz.utils.DirtyFlagMapæ.­(v\nÎ\0Z\0dirtyL\0mapt\0Ljava/util/Map;xpsr\0java.util.HashMapÚÁÃ`Ñ\0F\0\nloadFactorI\0	thresholdxp?@\0\0\0\0\0w\0\0\0\0\0\0t\0pipelineNamet\0assign.transformt\0channelNamet\0assign.transform#1x\0'),('quartzScheduler','8e57dc04-e9b1-4f52-8147-82180c40b6a0','com.jc.dropbox.to.s3#2','Pipeline Execution','com.zuci.zio.job.Job','1','0','0','0','¬í\0sr\0org.quartz.JobDataMapŸ°ƒè¿©°Ë\0\0xr\0&org.quartz.utils.StringKeyDirtyFlagMap‚èÃûÅ](\0Z\0allowsTransientDataxr\0org.quartz.utils.DirtyFlagMapæ.­(v\nÎ\0Z\0dirtyL\0mapt\0Ljava/util/Map;xpsr\0java.util.HashMapÚÁÃ`Ñ\0F\0\nloadFactorI\0	thresholdxp?@\0\0\0\0\0w\0\0\0\0\0\0t\0pipelineNamet\0com.jc.dropbox.to.s3t\0channelNamet\0com.jc.dropbox.to.s3#2x\0');
/*!40000 ALTER TABLE `QRTZ_JOB_DETAILS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_LOCKS`
--

DROP TABLE IF EXISTS `QRTZ_LOCKS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_LOCKS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `LOCK_NAME` varchar(40) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`LOCK_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_LOCKS`
--

LOCK TABLES `QRTZ_LOCKS` WRITE;
/*!40000 ALTER TABLE `QRTZ_LOCKS` DISABLE KEYS */;
INSERT INTO `QRTZ_LOCKS` VALUES ('quartzScheduler','TRIGGER_ACCESS');
/*!40000 ALTER TABLE `QRTZ_LOCKS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_PAUSED_TRIGGER_GRPS`
--

DROP TABLE IF EXISTS `QRTZ_PAUSED_TRIGGER_GRPS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_PAUSED_TRIGGER_GRPS`
--

LOCK TABLES `QRTZ_PAUSED_TRIGGER_GRPS` WRITE;
/*!40000 ALTER TABLE `QRTZ_PAUSED_TRIGGER_GRPS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_PAUSED_TRIGGER_GRPS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_SCHEDULER_STATE`
--

DROP TABLE IF EXISTS `QRTZ_SCHEDULER_STATE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_SCHEDULER_STATE` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `INSTANCE_NAME` varchar(190) NOT NULL,
  `LAST_CHECKIN_TIME` bigint(13) NOT NULL,
  `CHECKIN_INTERVAL` bigint(13) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`INSTANCE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_SCHEDULER_STATE`
--

LOCK TABLES `QRTZ_SCHEDULER_STATE` WRITE;
/*!40000 ALTER TABLE `QRTZ_SCHEDULER_STATE` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_SCHEDULER_STATE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_SIMPLE_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_SIMPLE_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_SIMPLE_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(190) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  `REPEAT_COUNT` bigint(7) NOT NULL,
  `REPEAT_INTERVAL` bigint(12) NOT NULL,
  `TIMES_TRIGGERED` bigint(10) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_SIMPLE_TRIGGERS`
--

LOCK TABLES `QRTZ_SIMPLE_TRIGGERS` WRITE;
/*!40000 ALTER TABLE `QRTZ_SIMPLE_TRIGGERS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_SIMPLE_TRIGGERS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_SIMPROP_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_SIMPROP_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_SIMPROP_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(190) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  `STR_PROP_1` varchar(512) DEFAULT NULL,
  `STR_PROP_2` varchar(512) DEFAULT NULL,
  `STR_PROP_3` varchar(512) DEFAULT NULL,
  `INT_PROP_1` int(11) DEFAULT NULL,
  `INT_PROP_2` int(11) DEFAULT NULL,
  `LONG_PROP_1` bigint(20) DEFAULT NULL,
  `LONG_PROP_2` bigint(20) DEFAULT NULL,
  `DEC_PROP_1` decimal(13,4) DEFAULT NULL,
  `DEC_PROP_2` decimal(13,4) DEFAULT NULL,
  `BOOL_PROP_1` varchar(1) DEFAULT NULL,
  `BOOL_PROP_2` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_SIMPROP_TRIGGERS`
--

LOCK TABLES `QRTZ_SIMPROP_TRIGGERS` WRITE;
/*!40000 ALTER TABLE `QRTZ_SIMPROP_TRIGGERS` DISABLE KEYS */;
/*!40000 ALTER TABLE `QRTZ_SIMPROP_TRIGGERS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `QRTZ_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QRTZ_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(190) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  `JOB_NAME` varchar(190) NOT NULL,
  `JOB_GROUP` varchar(190) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `NEXT_FIRE_TIME` bigint(13) DEFAULT NULL,
  `PREV_FIRE_TIME` bigint(13) DEFAULT NULL,
  `PRIORITY` int(11) DEFAULT NULL,
  `TRIGGER_STATE` varchar(16) NOT NULL,
  `TRIGGER_TYPE` varchar(8) NOT NULL,
  `START_TIME` bigint(13) NOT NULL,
  `END_TIME` bigint(13) DEFAULT NULL,
  `CALENDAR_NAME` varchar(190) DEFAULT NULL,
  `MISFIRE_INSTR` smallint(2) DEFAULT NULL,
  `JOB_DATA` blob DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `IDX_QRTZ_T_J` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_T_JG` (`SCHED_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_T_C` (`SCHED_NAME`,`CALENDAR_NAME`),
  KEY `IDX_QRTZ_T_G` (`SCHED_NAME`,`TRIGGER_GROUP`),
  KEY `IDX_QRTZ_T_STATE` (`SCHED_NAME`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_N_STATE` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_N_G_STATE` (`SCHED_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_NEXT_FIRE_TIME` (`SCHED_NAME`,`NEXT_FIRE_TIME`),
  KEY `IDX_QRTZ_T_NFT_ST` (`SCHED_NAME`,`TRIGGER_STATE`,`NEXT_FIRE_TIME`),
  KEY `IDX_QRTZ_T_NFT_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`),
  KEY `IDX_QRTZ_T_NFT_ST_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_NFT_ST_MISFIRE_GRP` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
  CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `QRTZ_JOB_DETAILS` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `QRTZ_TRIGGERS`
--

LOCK TABLES `QRTZ_TRIGGERS` WRITE;
/*!40000 ALTER TABLE `QRTZ_TRIGGERS` DISABLE KEYS */;
INSERT INTO `QRTZ_TRIGGERS` VALUES ('quartzScheduler','assign.transform#1','assign.transform#1','6c88f5a3-bd8f-4c1f-9964-bbee214b1736','assign.transform#1',NULL,1593822600000,1593438992122,5,'WAITING','CRON',1592844913000,0,NULL,0,''),('quartzScheduler','com.jc.dropbox.to.s3#2','com.jc.dropbox.to.s3#2','8e57dc04-e9b1-4f52-8147-82180c40b6a0','com.jc.dropbox.to.s3#2',NULL,1593496200000,1593495600000,5,'WAITING','CRON',1592320017000,0,NULL,0,'');
/*!40000 ALTER TABLE `QRTZ_TRIGGERS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `spw_common_config`
--

DROP TABLE IF EXISTS `spw_common_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `spw_common_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `variable` varchar(150) DEFAULT NULL,
  `value` varchar(500) DEFAULT NULL,
  `active` enum('Y','N') DEFAULT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=72 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `spw_common_config`
--

LOCK TABLES `spw_common_config` WRITE;
/*!40000 ALTER TABLE `spw_common_config` DISABLE KEYS */;
INSERT INTO `spw_common_config` VALUES (26,'basepath','root','Y',0),(27,'single','single','Y',0),(28,'json.storagepath','/home/jpvel/docker-data/mariadb/test','Y',0),(29,'csv','csv','Y',0),(30,'runsed','true','Y',0),(31,'uri','http://localhost:8443/rawdata/fetch/esh9','Y',0),(32,'get-board','com.zuci.trello.HandymanTrelloapi','Y',0),(33,'audit','audit','Y',0),(34,'lpath','/home/sid/ftp/','Y',0),(35,'rpath','/home/vstpd/','Y',0),(36,'authurl','s','Y',0),(37,'cusurl','s','Y',0),(38,'test','test','Y',0),(39,'mi9','mi9','Y',0),(40,'from','ffsczuci@gmail.com','Y',0),(41,'pass','Ffs3Zuc12019','Y',0),(42,'to','canvasgold98@gmail.com,gopinath.b@zucisystems.com','Y',0),(43,'cc','msenthilsiddharth@gmail.com,sidyercaud1@gmail.com,anil@zucisystems.com','Y',0),(44,'bcc','ffsczuci@gmail.com,sidyercaud@gmail.com','Y',0),(45,'subject1','Message All Clear','Y',0),(46,'subject0','Issue: InterfaceAP','Y',0),(47,'body0','No Transactions loaded for yesterday','Y',0),(48,'body1','Transactions were loaded for yesterday','Y',0),(49,'S3_Upload_limit','1','Y',0),(50,'11','11','Y',0),(54,'new ','@#%df hasdfisj d$^','Y',0),(55,'#$%$#DSFG','%*&N <><?','Y',0),(56,'filenametest1','filenametest1','Y',0),(58,'single1','single1','Y',0),(59,'test new','single','Y',0),(62,'try','try','Y',0),(63,'bb','update','Y',0),(64,'aa','aa','Y',0),(65,'ffsc-sqldb','oltp/ffsc','Y',0),(66,'builder',' /home/handy/workspace/halo-model/halo-modelTraining/mahout','Y',0),(67,'seq','/home/handy/workspace/halo-model/halo-modelTraining/mahout/tweets-seq','Y',0),(68,'trainer','/home/handy/workspace/halo-model/halo-modelTraining/mahout/target/twitter-naive-bayes-example-1.0-jar-with-dependencies.jar com.chimpler.example.bayes.ModelTrainingService','Y',0),(69,'csv.storagepath','/home/handy/workspace/zio/halo-zio/extract_files/home/anuradha/workspace/storage/sigma/model_input.csv','Y',0),(70,'model','/home/handy/workspace/halo-model/halo-modelTraining/$(date +%Y-%m-%d)_model','Y',0),(71,'inputmodel','/home/handy/workspace/halo-model/halo-modelTraining/','Y',0);
/*!40000 ALTER TABLE `spw_common_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `spw_common_config_audit`
--

DROP TABLE IF EXISTS `spw_common_config_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `spw_common_config_audit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `variable` varchar(150) DEFAULT NULL,
  `value` varchar(500) DEFAULT NULL,
  `active` enum('Y','N') DEFAULT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `spw_common_config_audit`
--

LOCK TABLES `spw_common_config_audit` WRITE;
/*!40000 ALTER TABLE `spw_common_config_audit` DISABLE KEYS */;
INSERT INTO `spw_common_config_audit` VALUES (26,'basepath1','root1','Y',1),(31,'basepath1','root1','Y',1),(32,'basepath1','root1','Y',1),(33,'basepath1','root1','Y',1),(34,'basepath1','root1','Y',1),(35,'1','1','Y',0),(36,'','','Y',0),(37,'','','Y',0),(38,'new 2','new 2','Y',0),(39,'new var','new var','Y',0),(40,'11','11','Y',0),(41,'new1','new1','Y',0),(42,'filepath','assign.transform.spw','Y',0),(43,'sample','sample','Y',0),(44,'filepath','assign.transform.spw','Y',0);
/*!40000 ALTER TABLE `spw_common_config_audit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `spw_instance`
--

DROP TABLE IF EXISTS `spw_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `spw_instance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `process` varchar(150) DEFAULT NULL,
  `instance` varchar(150) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `shortName` varchar(255) DEFAULT NULL,
  `alias` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=190 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `spw_instance`
--

LOCK TABLES `spw_instance` WRITE;
/*!40000 ALTER TABLE `spw_instance` DISABLE KEYS */;
INSERT INTO `spw_instance` VALUES (20,'com.mail','com.mail#1','mail','mail',NULL),(21,'com.mail','com.mail#2','mail 2','mail 2',NULL),(39,'com.jc.dropbox.to.s3','com.jc.dropbox.to.s3#2','#2','','#2'),(41,'com.jc.dropbox.to.s3','com.jc.dropbox.to.s3#3','#3','','#3'),(43,'com.jc.dropbox.to.s3','com.jc.dropbox.to.s3#4','#4','','#4'),(52,'com.jc.dropboxhourlyfolder.to.s3','com.jc.dropboxhourlyfolder.to.s3#4','anu ubuntu','','#4'),(54,'com.jc.dropboxhourlyfolder.to.s3','com.jc.dropboxhourlyfolder.to.s3#5','#5','','#5'),(84,'com.jc.dropbox.to.s3','com.jc.dropbox.to.s3#6','#6','','#6'),(98,'assign.transform','assign.transform#2','Sample Process','','#2'),(113,'com.jc.dropbox.to.s3','com.jc.dropbox.to.s3#7','#7','','#7'),(163,'com.jc.dropbox.to.s3','com.jc.dropbox.to.s3#5','five','','#5'),(164,'com.sf.dropboxhourlyfolder.to.s3','com.sf.dropboxhourlyfolder.to.s3#4','anu local ubuntu','','#4'),(173,'com.jc.dropbox.to.s3','com.jc.dropbox.to.s3#1','#1','','#1'),(174,'assign.transform','assign.transform#3','Sample Process','','#3'),(175,'assign.transform','assign.transform#4','Sample Process','','#4'),(180,'com.jc.dropbox.to.s3','com.jc.dropbox.to.s3#9','#9','','#9'),(184,'com.jc.dropbox.to.s3','com.jc.dropbox.to.s3#8','#8','','#8'),(185,'sample.macro','sample.macro#1','sample.macro','','#1'),(186,'assign.transform','assign.transform#1','Sample Process','','#1'),(188,'ffsc.funnelstatus.process','ffsc.funnelstatus.process#1','data cleansing','','#1'),(189,'ffsc.model.training','ffsc.model.training#1','ModelTraining','','#1');
/*!40000 ALTER TABLE `spw_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `spw_instance_config`
--

DROP TABLE IF EXISTS `spw_instance_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `spw_instance_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `instance` varchar(150) DEFAULT NULL,
  `process` varchar(150) DEFAULT NULL,
  `variable` varchar(150) DEFAULT NULL,
  `value` varchar(500) DEFAULT NULL,
  `active` enum('Y','N') DEFAULT NULL,
  `version` int(11) NOT NULL,
  `seedConfig` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=465 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `spw_instance_config`
--

LOCK TABLES `spw_instance_config` WRITE;
/*!40000 ALTER TABLE `spw_instance_config` DISABLE KEYS */;
INSERT INTO `spw_instance_config` VALUES (22,'com.mail#2','com.mail','mi9','mi9','Y',0,0),(79,'com.jc.dropbox.to.s3#2','com.jc.dropbox.to.s3','bucket','myjetcapital/QA/MCASuite/Extracts','Y',0,0),(80,'com.jc.dropbox.to.s3#2','com.jc.dropbox.to.s3','11','11','Y',0,0),(81,'com.jc.dropbox.to.s3#2','com.jc.dropbox.to.s3','file','CrossTest.zip','Y',0,0),(82,'com.jc.dropbox.to.s3#2','com.jc.dropbox.to.s3','filepath','com.jc.dropbox.to.s3.spw','Y',0,0),(83,'com.jc.dropbox.to.s3#2','com.jc.dropbox.to.s3','Dropbox_Download_limit','1','Y',0,0),(85,'com.jc.dropbox.to.s3#3','com.jc.dropbox.to.s3','11','13','Y',0,0),(87,'com.jc.dropbox.to.s3#4','com.jc.dropbox.to.s3','key','AKIAV62GZFGMZ43VGT6X','Y',0,0),(102,'com.jc.dropboxhourlyfolder.to.s3#4','com.jc.dropboxhourlyfolder.to.s3','Extract_Target','E:\\\\workspace\\\\sf\\\\Extract Files','Y',0,0),(103,'com.jc.dropboxhourlyfolder.to.s3#4','com.jc.dropboxhourlyfolder.to.s3','target','/home/anu/sa','Y',0,0),(106,'com.jc.dropboxhourlyfolder.to.s3#5','com.jc.dropboxhourlyfolder.to.s3','Extract_Target','E:\\\\workspace\\\\sf\\\\Extract Files','Y',0,0),(107,'com.jc.dropboxhourlyfolder.to.s3#5','com.jc.dropboxhourlyfolder.to.s3','cleanup_duration','200','Y',0,0),(108,'com.jc.dropboxhourlyfolder.to.s3#5','com.jc.dropboxhourlyfolder.to.s3','target','/home/anu/sa','Y',0,0),(163,'com.jc.dropbox.to.s3#6','com.jc.dropbox.to.s3','filepath','com.jc.dropbox.to.s3.spw','Y',0,0),(164,'com.jc.dropbox.to.s3#6','com.jc.dropbox.to.s3','auth','SJzKyyobHqAAAAAAAAAAmR1w5Ts__RuW1wNcsa9BXuDbzX1JSm-wlCvc7P3He8eW','Y',0,0),(165,'com.jc.dropbox.to.s3#6','com.jc.dropbox.to.s3','key','AKIAV62GZFGMZ43VGT6X','Y',0,0),(166,'com.jc.dropbox.to.s3#6','com.jc.dropbox.to.s3','target','E:\\workspace\\sf','Y',0,0),(204,'assign.transform#2','assign.transform','filepath','assign.transform.spw','Y',0,0),(205,'assign.transform#2','assign.transform','sample','sample1','Y',0,0),(249,'com.jc.dropbox.to.s3#7','com.jc.dropbox.to.s3','filepath','com.jc.dropbox.to.s3.spw','Y',0,0),(250,'com.jc.dropbox.to.s3#7','com.jc.dropbox.to.s3','key','AKIAV62GZFGMZ43VGT6X','Y',0,0),(251,'com.jc.dropbox.to.s3#7','com.jc.dropbox.to.s3','target','E:\\workspace\\sf','Y',0,0),(399,'com.jc.dropbox.to.s3#5','com.jc.dropbox.to.s3','filepath','com.jc.dropbox.to.s3.spw','Y',0,0),(400,'com.jc.dropbox.to.s3#5','com.jc.dropbox.to.s3','key','AKIAV62GZFGMZ43VGT6X','Y',0,0),(401,'com.jc.dropbox.to.s3#5','com.jc.dropbox.to.s3','target','E:\\workspace\\sf','Y',0,0),(403,'com.mail#2','com.mail','filepath','test','Y',0,0),(410,'com.mail#1','com.mail','13','13','Y',0,0),(412,'com.mail#1','com.mail','2','3','Y',0,0),(436,'com.jc.dropbox.to.s3#1','com.jc.dropbox.to.s3','auth','SJzKyyobHqAAAAAAAAAAmR1w5Ts__RuW1wNcsa9BXuDbzX1JSm-wlCvc7P3He8eW','Y',0,0),(437,'com.jc.dropbox.to.s3#1','com.jc.dropbox.to.s3','filepath','com.jc.dropbox.to.s3.spw','Y',0,0),(438,'com.mail#1','com.mail','mi9','mi9','Y',0,0),(439,'assign.transform#3','assign.transform','filepath','assign.transform.spw','Y',0,0),(440,'assign.transform#4','assign.transform','filepath','assign.transform.spw','Y',0,0),(441,'com.jc.dropbox.to.s3#3','com.jc.dropbox.to.s3','path','samp1','Y',0,0),(449,'com.jc.dropbox.to.s3#9','com.jc.dropbox.to.s3','jc','jc','Y',0,0),(450,'com.jc.dropbox.to.s3#9','com.jc.dropbox.to.s3','target','E:\\workspace\\sf','Y',0,0),(457,'com.jc.dropbox.to.s3#8','com.jc.dropbox.to.s3','jc','jc','Y',0,0),(458,'com.jc.dropbox.to.s3#8','com.jc.dropbox.to.s3','target','E:\\workspace\\sf','Y',0,0),(459,'com.jc.dropbox.to.s3#1','com.jc.dropbox.to.s3','bucket','bucket','Y',0,0),(460,'sample.macro#1','sample.macro','filepath','sample.macro.spw','Y',0,0),(461,'assign.transform#1','assign.transform','filepath','assign.transform.spw','Y',0,0),(462,'assign.transform#1','assign.transform','sample','sample','Y',0,0),(464,'ffsc.funnelstatus.process#1','ffsc.funnelstatus.process','filepath','ffsc.funnelstatus.process.spw','Y',0,0);
/*!40000 ALTER TABLE `spw_instance_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `spw_instance_config_audit`
--

DROP TABLE IF EXISTS `spw_instance_config_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `spw_instance_config_audit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `instance` varchar(150) DEFAULT NULL,
  `process` varchar(150) DEFAULT NULL,
  `variable` varchar(150) DEFAULT NULL,
  `value` varchar(500) DEFAULT NULL,
  `active` enum('Y','N') DEFAULT NULL,
  `version` int(11) NOT NULL,
  `seedConfig` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `spw_instance_config_audit`
--

LOCK TABLES `spw_instance_config_audit` WRITE;
/*!40000 ALTER TABLE `spw_instance_config_audit` DISABLE KEYS */;
INSERT INTO `spw_instance_config_audit` VALUES (6,'asd','fasdf','asdfa','fasdf','Y',0,0),(7,'n3','n3','n3','n3','Y',0,0),(8,'','','','','Y',0,0),(9,'com.mail#2','com.mail','new','new','Y',0,0),(10,'com.mail#2','com.mail','new','new','Y',0,0),(11,'com.mail#1','com.mail','1','1','Y',0,0),(12,'com.jc.dropbox.to.s3#7','com.jc.dropbox.to.s3','jc','jc','Y',0,0),(13,'com.jc.dropbox.to.s3#6','com.jc.dropbox.to.s3','target','E:\\workspace\\sf','Y',0,0),(14,'com.jc.dropbox.to.s3#1','com.jc.dropbox.to.s3','new 1','new 1','Y',0,0),(15,'com.jc.dropbox.to.s3#1','com.jc.dropbox.to.s3','new 1','new 1','Y',0,0),(16,'com.jc.dropbox.to.s3#1','com.jc.dropbox.to.s3','new 2','new 2','Y',0,0),(17,'com.jc.dropbox.to.s3#1','com.jc.dropbox.to.s3','new 3','new 3','Y',0,0),(18,'com.jc.dropbox.to.s3#1','com.jc.dropbox.to.s3','new 1','new 1','Y',0,0),(19,'com.jc.dropbox.to.s3#5','com.jc.dropbox.to.s3','target','1','Y',0,0),(20,'com.jc.dropbox.to.s3#1','com.jc.dropbox.to.s3','target','2','Y',0,0),(21,'com.jc.dropbox.to.s3#1','com.jc.dropbox.to.s3','auth','2','Y',0,0),(22,'com.mail#2','com.mail','mail var 3','mail var 3','Y',0,0),(23,'com.jc.dropbox.to.s3#6','com.jc.dropbox.to.s3','Dropbox_Download_limit','3','Y',0,0),(24,'com.jc.dropbox.to.s3#6','com.jc.dropbox.to.s3','jc','jc','Y',0,0),(25,'com.mail#2','com.mail','filepath','test','Y',0,0),(26,'com.mail#1','com.mail','filepath','1','Y',0,0),(27,'com.mail#1','com.mail','var 3','var 1','Y',0,0),(28,'com.mail#1','com.mail','mi9','1','Y',0,0),(29,'com.mail#1','com.mail','mi9','1','Y',0,0),(30,'com.mail#1','com.mail','13','13','Y',0,0),(31,'com.mail#1','com.mail','mi9','TEST','Y',0,0),(32,'com.mail#1','com.mail','13','13','Y',0,0),(33,'com.mail#1','com.mail','13','13','Y',0,0),(34,'com.mail#1','com.mail','mi9','test','Y',0,0),(35,'com.mail#1','com.mail','1','1','Y',0,0),(36,'com.mail#1','com.mail','filepath','edit','Y',0,0),(37,'com.mail#1','com.mail','filepath','update','Y',0,0),(38,'com.mail#1','com.mail','mi9','test','Y',0,0),(39,'com.mail#1','com.mail','mi9','check','Y',0,0),(40,'com.mail#1','com.mail','filepath','check file','Y',0,0);
/*!40000 ALTER TABLE `spw_instance_config_audit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `spw_process`
--

DROP TABLE IF EXISTS `spw_process`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `spw_process` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `process` varchar(150) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `shortName` varchar(255) DEFAULT NULL,
  `uploadFileName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `spw_process`
--

LOCK TABLES `spw_process` WRITE;
/*!40000 ALTER TABLE `spw_process` DISABLE KEYS */;
INSERT INTO `spw_process` VALUES (20,'com.cubic.ftp','cubic','cubic',NULL),(21,'com.mail','mail','mail',NULL),(22,'com.jc.dropbox.to.s3','drop box','drop box','SPW'),(23,'com.jc.dropboxhourlyfolder.to.s3','','','process'),(24,'assign.transform','','','AssignTransform'),(25,'com.sf.dropboxhourlyfolder.to.s3','','',''),(26,'sample.macro','','','SampleMacro'),(27,'ffsc.funnelstatus.process','','','funnelstatus'),(28,'ffsc.model.training','','','ffsc');
/*!40000 ALTER TABLE `spw_process` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `spw_process_config`
--

DROP TABLE IF EXISTS `spw_process_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `spw_process_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `process` varchar(150) DEFAULT NULL,
  `variable` varchar(150) DEFAULT NULL,
  `value` varchar(500) DEFAULT NULL,
  `active` enum('Y','N') DEFAULT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=148 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `spw_process_config`
--

LOCK TABLES `spw_process_config` WRITE;
/*!40000 ALTER TABLE `spw_process_config` DISABLE KEYS */;
INSERT INTO `spw_process_config` VALUES (28,'com.cubic.ftp','filepath','com.cubic.ftp.spw','Y',0),(29,'com.cubic.ftp','cubic','cubic','Y',0),(30,'com.mail','filepath','com.mail.spw','Y',0),(31,'com.mail','mi9','mi9','Y',0),(32,'com.jc.dropbox.to.s3','target','E:\\workspace\\sf','Y',0),(33,'com.jc.dropbox.to.s3','filepath','com.jc.dropbox.to.s3.spw','Y',0),(34,'com.jc.dropbox.to.s3','jc','jc','Y',0),(36,'com.jc.dropbox.to.s3','auth','SJzKyyobHqAAAAAAAAAAmR1w5Ts__RuW1wNcsa9BXuDbzX1JSm-wlCvc7P3He8eW','Y',0),(37,'com.jc.dropbox.to.s3','source','','Y',0),(38,'com.jc.dropbox.to.s3','bucket','myjetcapital/QA/MCASuite/Extracts','Y',0),(39,'com.jc.dropbox.to.s3','key','AKIAV62GZFGMZ43VGT6X','Y',0),(40,'com.jc.dropbox.to.s3','token','USy+owjt9nvMQZPsZZU1w8r+kkO4uridCc5lNTMu','Y',0),(42,'com.jc.dropbox.to.s3','path','','Y',0),(43,'com.jc.dropbox.to.s3','extract target','E:\\workspace\\sf\\Extract Files','Y',0),(44,'com.jc.dropbox.to.s3','Dropbox_Download_limit','1','Y',0),(45,'com.jc.dropbox.to.s3','S3_Upload_limit','1','Y',0),(46,'com.mail','13','15','Y',0),(48,'com.cubic.ftp','11','12','Y',0),(59,'com.jc.dropboxhourlyfolder.to.s3','target','/home/anu/sa','Y',0),(60,'com.jc.dropboxhourlyfolder.to.s3','filepath','com.jc.dropboxhourlyfolder.to.s3.spw','Y',0),(61,'com.jc.dropboxhourlyfolder.to.s3','sf','sf','Y',0),(62,'com.jc.dropboxhourlyfolder.to.s3','auth','SJzKyyobHqAAAAAAAAAAmR1w5Ts__RuW1wNcsa9BXuDbzX1JSm-wlCvc7P3He8eW','Y',0),(63,'com.jc.dropboxhourlyfolder.to.s3','bucket','myjetcapital/QA/MCASuite/Extracts','Y',0),(64,'com.jc.dropboxhourlyfolder.to.s3','key','AKIAV62GZFGMZ43VGT6X','Y',0),(65,'com.jc.dropboxhourlyfolder.to.s3','token','USy+owjt9nvMQZPsZZU1w8r+kkO4uridCc5lNTMu','Y',0),(66,'com.jc.dropboxhourlyfolder.to.s3','Extract_Target','E:\\\\workspace\\\\sf\\\\Extract Files','Y',0),(67,'com.jc.dropboxhourlyfolder.to.s3','Dropbox_Download_limit','1','Y',0),(68,'com.jc.dropboxhourlyfolder.to.s3','S3_Upload_limit','1','Y',0),(69,'com.jc.dropboxhourlyfolder.to.s3','mca_suite','mca_suite','Y',0),(70,'com.jc.dropboxhourlyfolder.to.s3','cleanup_duration','168','Y',0),(71,'com.jc.dropboxhourlyfolder.to.s3','source','','Y',0),(72,'com.jc.dropbox.to.s3','1','3','Y',0),(107,'assign.transform','sample','sample','Y',0),(108,'assign.transform','filepath','assign.transform.spw','Y',0),(109,'com.mail','test1','test1','Y',0),(110,'com.sf.dropboxhourlyfolder.to.s3','sf','sf','Y',0),(111,'com.sf.dropboxhourlyfolder.to.s3','bucket','myjetcapital/QA/MCASuite/Extracts','Y',0),(112,'com.sf.dropboxhourlyfolder.to.s3','key','USy+owjt9nvMQZPsZZU1w8r+kkO4uridCc5lNTMu','Y',0),(113,'com.sf.dropboxhourlyfolder.to.s3','token','USy+owjt9nvMQZPsZZU1w8r+kkO4uridCc5lNTMu','Y',0),(114,'com.sf.dropboxhourlyfolder.to.s3','filepath','com.sf.dropboxhourlyfolder.to.s3.spw','Y',0),(115,'com.sf.dropboxhourlyfolder.to.s3','auth','SJzKyyobHqAAAAAAAAAAmR1w5Ts__RuW1wNcsa9BXuDbzX1JSm-wlCvc7P3He8eW','Y',0),(116,'com.sf.dropboxhourlyfolder.to.s3','source','','Y',0),(117,'com.sf.dropboxhourlyfolder.to.s3','target','/home/anu/Downloads','Y',0),(118,'com.sf.dropboxhourlyfolder.to.s3','Dropbox_Download_limit','1','Y',0),(119,'com.sf.dropboxhourlyfolder.to.s3','S3_Upload_limit','1','Y',0),(120,'com.mail','1','1','Y',0),(123,'com.mail','2','3','Y',0),(130,'com.mail','new','new','Y',0),(131,'sample.macro','filepath','sample.macro.spw','Y',0),(132,'sample.macro','ffsc-model','ffsc-sqldb','Y',0),(133,'sample.macro','table_name','Funnel_788','Y',0),(134,'sample.macro','instance-audit-val','instance-audit-val','Y',0),(135,'ffsc.funnelstatus.process','ffsc-sqldb','ffsc-sqldb','Y',0),(136,'ffsc.funnelstatus.process','filepath','ffsc.funnelstatus.process.spw','Y',0),(137,'ffsc.funnelstatus.process','config/ffsc','config/ffsc','Y',0),(138,'sample.macro','process_id','788','Y',0),(139,'com.model.generation','process_id','1204','Y',0),(140,'com.model.generation','filepath','com.model.generation.spw','Y',0),(141,'com.model.generation','ffsc-model','oltp/ffsc','Y',0),(142,'com.model.generation','model-details','model_file_details','Y',0),(143,'com.model.generation','instance-audit-val','instance-audit-val','Y',0),(144,'ffsc.model.training','filepath','ffsc.model.training.spw','Y',0),(145,'ffsc.model.training','ffsc-model','oltp/ffsc','Y',0),(146,'ffsc.model.training','config/ffsc','config/ffsc','Y',0),(147,'ffsc.model.training','instance-audit-val','instance-audit-val','Y',0);
/*!40000 ALTER TABLE `spw_process_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `spw_process_config_audit`
--

DROP TABLE IF EXISTS `spw_process_config_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `spw_process_config_audit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `process` varchar(150) DEFAULT NULL,
  `variable` varchar(150) DEFAULT NULL,
  `value` varchar(500) DEFAULT NULL,
  `active` enum('Y','N') DEFAULT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=94 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `spw_process_config_audit`
--

LOCK TABLES `spw_process_config_audit` WRITE;
/*!40000 ALTER TABLE `spw_process_config_audit` DISABLE KEYS */;
INSERT INTO `spw_process_config_audit` VALUES (20,'com.jc.dropbox.to.s3','token','USy+owjt9nvMQZPsZZU1w8r+kkO4uridCc5lNTMu','Y',0),(21,'com.jc.dropbox.to.s3','auth','K9W7IeXxTzAAAAAAAAAAokuBuCk2qkXpjgqOobMrp8JT61obuNV9LZkWCwy9CvJa','Y',0),(22,'new3','new3','new3','Y',0),(23,'new6','new5','new5','Y',0),(24,'com.cubic.ftp','new','new','Y',0),(25,'com.cubic.ftp','','','Y',0),(26,'com.cubic.ftp','new','new','Y',0),(27,'com.cubic.ftp','new','new','Y',0),(28,'com.cubic.ftp','new','new','Y',0),(29,'com.cubic.ftp','new','new','Y',0),(30,'com.cubic.ftp','new','new','Y',0),(31,'com.cubic.ftp','','','Y',0),(32,'com.cubic.ftp','','','Y',0),(33,'com.cubic.ftp','new','new','Y',0),(34,'com.cubic.ftp','new','new','Y',0),(35,'com.cubic.ftp','a','a','Y',0),(36,'com.cubic.ftp','new','new','Y',0),(37,'com.mail','1','12','Y',0),(38,'com.mail','1','12','Y',0),(39,'com.jc.dropbox.to.s3','file','CrossTest.zip','Y',0),(40,'com.jc.dropbox.to.s3','file','JC11.zip','Y',0),(41,'com.jc.dropbox.to.s3','11','11','Y',0),(42,'com.jc.dropbox.to.s3','12','12','Y',0),(43,'com.jc.dropbox.to.s3','3','3','Y',0),(44,'com.mail','new 3','new 3','Y',0),(45,'com.mail','new 5','new 5','Y',0),(46,'com.mail','new 4','new 4','Y',0),(47,'com.mail','new 3','new 3','Y',0),(48,'com.mail','new 6','new 6','Y',0),(49,'com.mail','new 5','new 5','Y',0),(50,'com.mail','new 4','new 4','Y',0),(51,'com.mail','new 6','new 6','Y',0),(52,'com.mail','new 5','new 5','Y',0),(53,'com.mail','new 4','new 4','Y',0),(54,'com.mail','new 8 ','new 8','Y',0),(55,'com.mail','new 5','new 5','Y',0),(56,'com.mail','new 3','new 3','Y',0),(57,'com.mail','new 6','new 6','Y',0),(58,'com.mail','new 5','new 5','Y',0),(59,'com.mail','new 4','new 4','Y',0),(60,'com.mail','new 3','new 3','Y',0),(61,'com.mail','new 3','new 3','Y',0),(62,'com.mail','new 3','new 3','Y',0),(63,'com.mail','new 3','new 3','Y',0),(64,'com.mail','new3','new3','Y',0),(65,'com.mail','new 2','new 2','Y',0),(66,'com.mail','new1','new1','Y',0),(67,'com.mail','new1','new1','Y',0),(68,'com.mail','new2','new2','Y',0),(69,'com.mail','new','new','Y',0),(70,'com.mail','new 6','new 6','Y',0),(71,'com.mail','4','4','Y',0),(72,'com.mail','3','3','Y',0),(73,'com.mail','7','7','Y',0),(74,'com.mail','6','6','Y',0),(75,'com.mail','5','5','Y',0),(76,'com.mail','5','5','Y',0),(77,'com.mail','4','4','Y',0),(78,'com.mail','6','6','Y',0),(79,'com.mail','5','5','Y',0),(80,'com.mail','test 3','test 3','Y',0),(81,'com.mail','test 2','test 2','Y',0),(82,'com.mail','test 3','test 3','Y',0),(83,'com.mail','test 2','test 2','Y',0),(84,'com.mail','test 2','test 2','Y',0),(85,'com.mail','4','4','Y',0),(86,'com.mail','2','3','Y',0),(87,'com.mail','3','3','Y',0),(88,'com.mail','5','5','Y',0),(89,'com.mail','6','6','Y',0),(90,'com.mail','4','4','Y',0),(91,'com.mail','3','3','Y',0),(92,'com.jc.dropbox.to.s3','3','3','Y',0),(93,'com.jc.dropbox.to.s3','2','2','Y',0);
/*!40000 ALTER TABLE `spw_process_config_audit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `spw_resource_config`
--

DROP TABLE IF EXISTS `spw_resource_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `spw_resource_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `config_type` varchar(50) DEFAULT NULL,
  `config_name` varchar(50) DEFAULT NULL,
  `auth_info` varchar(1000) DEFAULT NULL,
  `resource_url` varchar(1000) DEFAULT NULL,
  `active` enum('Y','N') DEFAULT NULL,
  `created_date` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `user_name` varchar(50) DEFAULT NULL,
  `password` varchar(50) DEFAULT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `spw_resource_config`
--

LOCK TABLES `spw_resource_config` WRITE;
/*!40000 ALTER TABLE `spw_resource_config` DISABLE KEYS */;
INSERT INTO `spw_resource_config` VALUES (1,'1','oltp/bonsai','{ \"user\": \"root\", \"password\": \"password\"}','jdbc:mysql://localhost:3306/bonsai_crm?useUnicode=true&characterEncoding=utf8&dumpQueriesOnException=true','Y','2019-01-25 13:41:37','root','password',0),(2,'1','audit/bonsai-audit','{ \"user\": \"root\", \"password\": \"password\"}','jdbc:mysql://localhost:3306/spw_audit?useUnicode=true&characterEncoding=utf8&dumpQueriesOnException=true','Y','2019-01-25 13:41:37','root','password',0),(3,'1','oltp/parkingdb','{ \"user\": \"root\", \"password\": \"password\"}','jdbc:mysql://localhost:3306/parkingbay?charset=utf8mb4&dumpQueriesOnException=true','Y','2019-01-25 13:41:37','root','password',0),(4,'1','oltp/candy-intg','{ \"user\": \"root\", \"password\": \"password\"}','jdbc:mysql://localhost:3306/candy_lead?useUnicode=true&characterEncoding=utf8&dumpQueriesOnException=true','Y','2019-01-25 13:41:37','root','password',0),(5,'1','audit/candy_lead-audit','{ \"user\": \"root\", \"password\": \"password\"}','jdbc:mysql://localhost:3306/spw_audit?useUnicode=true&characterEncoding=utf8&dumpQueriesOnException=true','Y','2019-01-25 13:41:37','root','password',0),(6,'1','oltp/candy_lead-intg','{ \"user\": \"root\", \"password\": \"password\"}','jdbc:mysql://localhost:3306/lead?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&characterSetResults=utf8&connectionCollation=utf8_general_ci&dumpQueriesOnException=true','Y','2019-01-25 13:41:37','root','password',0),(7,'1','audit/wordpress-audit','{ \"user\": \"root\", \"password\": \"password\"}','jdbc:mysql://localhost:3306/spw_audit?useUnicode=true&characterEncoding=utf8&dumpQueriesOnException=true','Y','2019-01-25 13:41:37','root','password',0),(8,'1','oltp/wci-intg','{ \"user\": \"root\", \"password\": \"password\"}','jdbc:mysql://localhost:3306/bonsai_crm?useUnicode=true&characterEncoding=utf8&dumpQueriesOnException=true','Y','2019-01-25 13:41:37','root','password',0),(9,'1','oltp/cus-intg','{ \"user\": \"root\", \"password\": \"password\"}','jdbc:mysql://localhost:3306/customer?useUnicode=true&characterEncoding=utf8&dumpQueriesOnException=true','Y','2019-01-25 13:41:37','root','password',0),(10,'1','audit','{ \"user\": \"root\", \"password\": \"password\"}','jdbc:mysql://localhost:3306/spw_audit?useUnicode=true&characterEncoding=utf8&dumpQueriesOnException=true','Y','2017-09-24 18:42:29','root','password',0),(11,'1','sf','{ \"user\": \"root\", \"password\": \"password\"}','jdbc:mysql://localhost:3306/jc?useUnicode=true&characterEncoding=utf8&dumpQueriesOnException=true','Y','2020-05-08 14:03:43','root','password',0),(12,'1','mca_suite','{ \"user\": \"dbadmin\", \"password\": \"loJwH2YA8iJgDV7p\"}','jdbc:redshift://jc-dwh-cluster.cwcctuxdzmnd.us-west-2.redshift.amazonaws.com:5439/jcdwhqa?ssl=true','Y','2020-05-15 17:39:57','dbadmin','loJwH2YA8iJgDV7p',0),(13,'1','sample','{\"user\":\"root\",\"password\":\"password\"}','jdbc:mysql://localhost:3306/sample?','Y','2020-05-29 10:58:46','root','password',0),(14,'1','sample1','{\"user\":\"root\",\"password\":\"password\"}','jdbc:mysql://localhost:3306/sample?','Y','2020-05-29 10:59:56','root','password',0),(15,'1','sample2','{\"user\":\"root\",\"password\":\"password\"}','jdbc:mysql://localhost:3306/sample?','Y','2020-06-05 06:21:27','root','password',0),(16,'1','sample4','{\"user\":\"root\",\"password\":\"password\"}','jdbc:mysql://localhost:3306/sample?','Y','2020-06-05 11:51:13','root','password',0),(17,'1','instance-audit-val','{ \"user\": \"root\", \"password\": \"password\"}','jdbc:mysql://172.18.0.2:3306/spw_audit?useUnicode=true&characterEncoding=utf8&dumpQueriesOnException=true','Y','2020-07-15 08:07:09','root','password',1),(18,'1','hm','{ \"user\": \"root\", \"password\": \"password\"}','jdbc:mysql://172.18.0.2:3306/handyman_macro?useUnicode=true&characterEncoding=utf8&dumpQueriesOnException=true','Y','2020-07-15 08:07:33','root','password',1),(19,'1','oltp/ffsc','{ \"user\": \"root\", \"password\": \"password\"}','jdbc:mysql://172.18.0.2:3306/ffsc_model?useUnicode=true&characterEncoding=utf8&dumpQueriesOnException=true','Y','2020-08-05 05:55:15','root','password',0),(20,'1','config/ffsc','{ \"user\": \"root\", \"password\": \"password\"}','jdbc:mysql://localhost:3306/spw_config?useUnicode=true&characterEncoding=utf8&dumpQueriesOnException=true','Y','2020-08-04 17:38:07','root','password',0),(21,'1','ffsc-sqldb','{ \"user\": \"root\", \"password\": \"password\"}','jdbc:mysql://localhost:3306/ffsc_data?useUnicode=true&characterEncoding=utf8&dumpQueriesOnException=true','Y','2020-08-04 17:39:20','root','password',0);
/*!40000 ALTER TABLE `spw_resource_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `spw_resource_config_audit`
--

DROP TABLE IF EXISTS `spw_resource_config_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `spw_resource_config_audit` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `config_type` varchar(50) DEFAULT NULL,
  `config_name` varchar(50) DEFAULT NULL,
  `auth_info` varchar(1000) DEFAULT NULL,
  `resource_url` varchar(1000) DEFAULT NULL,
  `active` enum('Y','N') DEFAULT NULL,
  `created_date` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `user_name` varchar(50) DEFAULT NULL,
  `password` varchar(50) DEFAULT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `spw_resource_config_audit`
--

LOCK TABLES `spw_resource_config_audit` WRITE;
/*!40000 ALTER TABLE `spw_resource_config_audit` DISABLE KEYS */;
INSERT INTO `spw_resource_config_audit` VALUES (13,'new','new','new','new','Y','2020-05-15 17:40:15','new','new',0),(14,'aaa','aaa','aaa','aaa','Y','2020-05-20 16:19:29','aaaa','aaa',0),(15,'aaa','aaa','aaa','aaa','Y','2020-05-20 16:19:23','aaa','aaa',0),(16,'aaaa','aaa','aaa','aaa','Y','2020-05-20 16:21:37','aaaa','aaa',0);
/*!40000 ALTER TABLE `spw_resource_config_audit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_file_location_details`
--

DROP TABLE IF EXISTS `user_file_location_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_file_location_details` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `file_location` varchar(255) NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_file_location_details`
--

LOCK TABLES `user_file_location_details` WRITE;
/*!40000 ALTER TABLE `user_file_location_details` DISABLE KEYS */;
INSERT INTO `user_file_location_details` VALUES (1,6865171714315187,'/home/handy/workspace/zio/halo-zio/tar_files/model06-08-2020_15:43:47.tar.gz','model06-08-2020_15:43:47.tar.gz','sent');
/*!40000 ALTER TABLE `user_file_location_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` varchar(100) DEFAULT NULL,
  `roleId` int(11) DEFAULT NULL,
  `organizationId` varchar(100) DEFAULT NULL,
  `paymentId` varchar(100) DEFAULT NULL,
  `firstName` varchar(100) DEFAULT NULL,
  `lastName` varchar(100) DEFAULT NULL,
  `subscribedPhoneNumber` varchar(100) DEFAULT NULL,
  `registeredPhoneNumber` varchar(100) DEFAULT NULL,
  `registeredEmailId` varchar(100) DEFAULT NULL,
  `address` varchar(100) DEFAULT NULL,
  `alternateNumber` varchar(100) DEFAULT NULL,
  `selectedRole` varchar(100) DEFAULT NULL,
  `selectedTemplate` varchar(100) DEFAULT NULL,
  `onboardingDate` date DEFAULT NULL,
  `lastModifiedDate` date DEFAULT NULL,
  `accessMode` varchar(100) DEFAULT NULL,
  `subscriptionStatus` varchar(100) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('12346789098810',2,'5998250143654595','123336644','Anuradha','Oomaidurai','8940425401','12345654323','anu@gmail.com','NA','NA','Data Engineer','Personal Loan','2020-06-16','2020-06-16',NULL,'LANDED','$2a$10$x3oM2x9boP/.3Xn.XYmwPuc7DXWXbtHF/AwMBV2yUhGTvzUq5hXwK'),('6050908087171559',2,'6050908086686058',NULL,'Anuradha','Oomaidurai',NULL,'8940425401','anuradha.o@zucisystems.com','Chennai','','Data Engineer','Lead Selection','2020-07-23','2020-07-23',NULL,'Activated','$2a$10$6m7VFTTVm22oCzYAZcRcVey8nZ7rDIytX5c/dcp7qkzXT4zsr7soC'),('6744599151973227',3,'6744599151947947',NULL,'Sanjeeya','Velayutham',NULL,'8940425401','sanjeeya17596@gmail.com','Chennai','','Learning Modeller','Lead Selection','2020-07-31','2020-07-31',NULL,'Activated','$2a$10$HyoSweG5Juzfv5VP6Ds3U.uM7b5t8Vyr5lViEgGmFa3tB32QzfPI2'),('6865171714315187',3,'6865171713768025',NULL,'Sanjeeya','Velayutham',NULL,'8940425401','sanjeeya.v@zucisystems.com','Chennai','','Learning Modeller','Lead Selection','2020-08-01','2020-08-01',NULL,'Activated','$2a$10$2v0PE05Mk/as7dLQrCaUfejCLPjRE1pS/tZ.KK5g0vs4uoCXcg5Ka'),(NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-08-07  8:04:19
