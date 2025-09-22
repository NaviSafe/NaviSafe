-- MySQL dump 10.13  Distrib 8.0.33, for Win64 (x86_64)
--
-- Host: localhost    Database: weather-flow
-- ------------------------------------------------------
-- Server version	8.0.33

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `link_id`
--
CREATE DATABASE IF NOT EXISTS `weather-flow`;
USE `weather-flow`;

DROP TABLE IF EXISTS `link_id`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `link_id` (
  `map_dist` int DEFAULT NULL,
  `reg_cd` int DEFAULT NULL,
  `ed_node_nm` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `link_id` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `road_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `st_node_nm` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`link_id`),
  KEY `FK7mjyfv367c6bt412t7qwx9645` (`reg_cd`),
  CONSTRAINT `FK2qdnmuk3lnxwwb243w04wol1m` FOREIGN KEY (`link_id`) REFERENCES `road_traffic` (`link_id`),
  CONSTRAINT `FK7mjyfv367c6bt412t7qwx9645` FOREIGN KEY (`reg_cd`) REFERENCES `reg_cd` (`reg_cd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `link_id`
--

LOCK TABLES `link_id` WRITE;
/*!40000 ALTER TABLE `link_id` DISABLE KEYS */;
INSERT INTO `link_id` VALUES (273,118,'영등포로타리','1180001100','경인로','서울교남단');
/*!40000 ALTER TABLE `link_id` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `outbreak`
--

DROP TABLE IF EXISTS `outbreak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `outbreak` (
  `grs80tm_x` float DEFAULT NULL,
  `grs80tm_y` float DEFAULT NULL,
  `exp_clr_date` datetime(6) DEFAULT NULL,
  `occr_date` datetime(6) DEFAULT NULL,
  `acc_dtype` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `acc_id` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `acc_info` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `acc_type` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `link_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`acc_id`),
  KEY `FKqrteej1k1hbfc8un0scntuhjh` (`acc_type`),
  KEY `FKo2leoghxl9d4kt0c0adlgytfw` (`acc_dtype`),
  KEY `FK62ydgagcrw9ynei9ypnex2bst` (`link_id`),
  CONSTRAINT `FK62ydgagcrw9ynei9ypnex2bst` FOREIGN KEY (`link_id`) REFERENCES `link_id` (`link_id`),
  CONSTRAINT `FKo2leoghxl9d4kt0c0adlgytfw` FOREIGN KEY (`acc_dtype`) REFERENCES `outbreak_detail_code` (`acc_dtype`),
  CONSTRAINT `FKqrteej1k1hbfc8un0scntuhjh` FOREIGN KEY (`acc_type`) REFERENCES `outbreak_code` (`acc_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `outbreak`
--

LOCK TABLES `outbreak` WRITE;
/*!40000 ALTER TABLE `outbreak` DISABLE KEYS */;
INSERT INTO `outbreak` VALUES (192385,446646,'2026-06-30 00:00:00.000000','2025-06-10 00:00:00.000000','04B01','1029246','경인로 (영등포로터리 → 서울교남단) 구간 영등포고가 철거관련 공사 시설물보수','A04','1180001100');
/*!40000 ALTER TABLE `outbreak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `outbreak_code`
--

DROP TABLE IF EXISTS `outbreak_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `outbreak_code` (
  `acc_type` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `acc_type_nm` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`acc_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `outbreak_code`
--

LOCK TABLES `outbreak_code` WRITE;
/*!40000 ALTER TABLE `outbreak_code` DISABLE KEYS */;
INSERT INTO `outbreak_code` VALUES ('A04','공사');
/*!40000 ALTER TABLE `outbreak_code` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `outbreak_detail_code`
--

DROP TABLE IF EXISTS `outbreak_detail_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `outbreak_detail_code` (
  `acc_dtype` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `acc_dtype_nm` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`acc_dtype`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `outbreak_detail_code`
--

LOCK TABLES `outbreak_detail_code` WRITE;
/*!40000 ALTER TABLE `outbreak_detail_code` DISABLE KEYS */;
INSERT INTO `outbreak_detail_code` VALUES ('04B01','시설물보수');
/*!40000 ALTER TABLE `outbreak_detail_code` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rain`
--

DROP TABLE IF EXISTS `rain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rain` (
  `gu_code` int NOT NULL,
  `rainfall10` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `receive_time` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`gu_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rain`
--

LOCK TABLES `rain` WRITE;
/*!40000 ALTER TABLE `rain` DISABLE KEYS */;
/*!40000 ALTER TABLE `rain` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reg_cd`
--

DROP TABLE IF EXISTS `reg_cd`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reg_cd` (
  `reg_cd` int NOT NULL,
  `reg_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`reg_cd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reg_cd`
--

LOCK TABLES `reg_cd` WRITE;
/*!40000 ALTER TABLE `reg_cd` DISABLE KEYS */;
INSERT INTO `reg_cd` VALUES (118,'영등포구');
/*!40000 ALTER TABLE `reg_cd` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `region`
--

DROP TABLE IF EXISTS `region`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `region` (
  `gu_code` int NOT NULL,
  `gu_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`gu_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `region`
--

LOCK TABLES `region` WRITE;
/*!40000 ALTER TABLE `region` DISABLE KEYS */;
/*!40000 ALTER TABLE `region` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `road_traffic`
--

DROP TABLE IF EXISTS `road_traffic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `road_traffic` (
  `link_id` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `prcs_spd` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `prcs_trv_time` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`link_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `road_traffic`
--

LOCK TABLES `road_traffic` WRITE;
/*!40000 ALTER TABLE `road_traffic` DISABLE KEYS */;
INSERT INTO `road_traffic` VALUES ('1180001100','15','66');
/*!40000 ALTER TABLE `road_traffic` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-22 17:26:50
