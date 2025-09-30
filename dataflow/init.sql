-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema toy_project
-- -----------------------------------------------------
-- CREATE SCHEMA IF NOT EXISTS `mydb` DEFAULT CHARACTER SET utf8 

-- -----------------------------------------------------
-- Schema toy_project
--
-- CREATE SCHEMA IF NOT EXISTS `mydb` DEFAULT CHARACTER SET utf8 
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `toy_project` DEFAULT CHARACTER SET utf8 COLLATE utf8_bin ;
USE `toy_project` ;

-- -----------------------------------------------------
-- Table `toy_project`.`OUTBREAK_Occurrence`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`OUTBREAK_Occurrence` (
  `ACC_ID` VARCHAR(20) NOT NULL COMMENT '서울시 실시간 돌발 정보\n\n돌발 아이디',
  `occr_date_time` DATETIME NULL,
  `exp_clr_date_time` DATETIME NULL,
  PRIMARY KEY (`ACC_ID`),
  UNIQUE INDEX `ACC_ID_UNIQUE` (`ACC_ID` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `toy_project`.`REG_CD`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`REG_CD` (
  `REG_CD` INT NOT NULL COMMENT '서울시 권역 코드\n\n권역코드',
  `REG_NAME` VARCHAR(45) NULL COMMENT '권역명',
  PRIMARY KEY (`REG_CD`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `toy_project`.`LINK_ID`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`LINK_ID` (
  `LINK_ID` VARCHAR(45) NOT NULL COMMENT '서울시 소통 돌발 링크 정보\n\n링크 아이디',
  `REG_CD_REG_CD` INT NOT NULL,
  `ROAD_NAME` VARCHAR(45) NULL COMMENT '도로 명',
  `ST_NODE_NM` VARCHAR(45) NULL COMMENT '시작 노드 명',
  `ED_NODE_NM` VARCHAR(3) NULL COMMENT '종료 노드 명',
  `MAP_DIST` INT NULL,
  PRIMARY KEY (`LINK_ID`, `REG_CD_REG_CD`),
  INDEX `fk_LINK_ID_REG_CD1_idx` (`REG_CD_REG_CD` ASC) VISIBLE,
  CONSTRAINT `fk_LINK_ID_REG_CD1`
    FOREIGN KEY (`REG_CD_REG_CD`)
    REFERENCES `toy_project`.`REG_CD` (`REG_CD`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `toy_project`.`REGION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`REGION` (
  `GU_CODE` INT NOT NULL,
  `GU_NAME` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`GU_CODE`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `toy_project`.`RAIN`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`RAIN` (
  `RAINFALL10` INT NULL,
  `RECEIVE_TIME` VARCHAR(45) NULL,
  `REGION_GU_CODE` INT NOT NULL,
  PRIMARY KEY (`REGION_GU_CODE`),
  INDEX `fk_RAIN_REGION1_idx` (`REGION_GU_CODE` ASC) VISIBLE,
  CONSTRAINT `fk_RAIN_REGION1`
    FOREIGN KEY (`REGION_GU_CODE`)
    REFERENCES `toy_project`.`REGION` (`GU_CODE`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `toy_project`.`ROAD_TRAFFIC`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`ROAD_TRAFFIC` (
  `LINK_ID` VARCHAR(45) NOT NULL COMMENT '서울시 실시간 도로 소통 정보\n\n링크 아이디',
  `PRCS_SPD` INT NULL COMMENT '속도',
  `PRCS_TRV_TIME` INT NULL COMMENT '여행 시간',
  PRIMARY KEY (`LINK_ID`),
  CONSTRAINT `fk_ROAD_TRAFFIC_LINK_ID1`
    FOREIGN KEY (`LINK_ID`)
    REFERENCES `toy_project`.`LINK_ID` (`LINK_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `toy_project`.`OUTBREAK_DETAIL_CODE_NAME`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`OUTBREAK_DETAIL_CODE_NAME` (
  `ACC_DTYPE` VARCHAR(10) NOT NULL,
  `ACC_DTYPE_NM` VARCHAR(15) NULL,
  PRIMARY KEY (`ACC_DTYPE`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `toy_project`.`OUTBREAK_DETAIL_CODE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`OUTBREAK_DETAIL_CODE` (
  `OUTBREAK_ACC_ID` VARCHAR(5) NOT NULL,
  `ACC_DTYPE` VARCHAR(10) NULL,
  PRIMARY KEY (`OUTBREAK_ACC_ID`),
  CONSTRAINT `fk_OUTBREAK_DETAIL_CODE_OUTBREAK1`
    FOREIGN KEY (`OUTBREAK_ACC_ID`)
    REFERENCES `toy_project`.`OUTBREAK_Occurrence` (`ACC_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_OUTBREAK_DETAIL_CODE_OUTBREAK_DETAIL_CODE_NAME2`
    FOREIGN KEY (`ACC_DTYPE`)
    REFERENCES `toy_project`.`OUTBREAK_DETAIL_CODE_NAME` (`ACC_DTYPE`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `toy_project`.`OUTBREAK_NAME`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`OUTBREAK_NAME` (
  `ACC_TYPE` VARCHAR(5) NOT NULL,
  `ACC_TYPE_NM` VARCHAR(15) NULL,
  PRIMARY KEY (`ACC_TYPE`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `toy_project`.`OUTBREAK_CODE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`OUTBREAK_CODE` (
  `OUTBREAK_ACC_ID` VARCHAR(5) NOT NULL,
  `ACC_TYPE` VARCHAR(5) NULL COMMENT '서울시 돌발 유형 코드 정보\n\n\"돌발 유형 코드\"\n',
  PRIMARY KEY (`OUTBREAK_ACC_ID`),
  CONSTRAINT `fk_OUTBREAK`
    FOREIGN KEY (`OUTBREAK_ACC_ID`)
    REFERENCES `toy_project`.`OUTBREAK_Occurrence` (`ACC_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_OUTBREAK_CODE_OUTBREAK_NAME2`
    FOREIGN KEY (`ACC_TYPE`)
    REFERENCES `toy_project`.`OUTBREAK_NAME` (`ACC_TYPE`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `toy_project`.`STATION_GPS`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`STATION_GPS` (
  `POI_ID` INT NOT NULL COMMENT 'POI ID',
  `poiNm` VARCHAR(45) NOT NULL COMMENT 'POI 이름',
  `gpsX` DOUBLE NOT NULL COMMENT 'X좌표 (WGS84)',
  `gpsY` DOUBLE NOT NULL COMMENT 'Y좌표 (WGS84)\n',
  PRIMARY KEY (`POI_ID`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `toy_project`.`PATH`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`PATH` (
  `PATH_ID` INT NOT NULL COMMENT '탑승지 X좌표 (WGS84)',
  `fid` INT NOT NULL COMMENT 'STATION_GPS의 POI_ID 참조',
  `tid` INT NOT NULL COMMENT 'STATION_GPS의 POI_ID 참조',
  `pathType` ENUM('SUBWAY', 'BUS', 'SUBWAY_BUS') NOT NULL,
  `fx` FLOAT NOT NULL COMMENT '탑승지 X좌표 (WGS84)',
  `fy` FLOAT NOT NULL COMMENT '탑승지 Y좌표 (WGS84)',
  `tx` FLOAT NOT NULL COMMENT '하차지 X좌표 (WGS84)',
  `ty` FLOAT NOT NULL COMMENT '하차지 Y좌표 (WGS84)',
  `distance` INT NOT NULL COMMENT '거리\n',
  `time` INT NULL COMMENT '소요시간',
  `pathList` MEDIUMTEXT NOT NULL COMMENT '경로목록',
  `routeId` INT NOT NULL COMMENT '노선 ID',
  `routeNm` VARCHAR(45) NOT NULL COMMENT '노선명',
  `fname` VARCHAR(20) NOT NULL COMMENT '탑승지명',
  `tname` VARCHAR(20) CHARACTER SET 'armscii8' NOT NULL COMMENT '하차지명',
  PRIMARY KEY (`PATH_ID`),
  INDEX `fk_SUBWAY_PATH_STATION_GPS1_idx` (`fid` ASC, `tid` ASC) VISIBLE,
  CONSTRAINT `fk_SUBWAY_PATH_STATION_GPS1`
    FOREIGN KEY (`fid` , `tid`)
    REFERENCES `toy_project`.`STATION_GPS` (`POI_ID` , `POI_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `toy_project`.`SUBWAY_PATH`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`SUBWAY_PATH` (
  `PATH_ID` INT NOT NULL COMMENT '탑승지 X좌표 (WGS84)',
  `railLinkList` VARCHAR(45) NOT NULL COMMENT '경유지하철역ID목록',
  `railLinkId` INT NOT NULL COMMENT '경유지하철역ID',
  `time` INT NOT NULL,
  PRIMARY KEY (`PATH_ID`),
  CONSTRAINT `fk_PATH_PATH_ID`
    FOREIGN KEY (`PATH_ID`)
    REFERENCES `toy_project`.`PATH` (`PATH_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `toy_project`.`ACC_ALTERTS`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`ACC_ALTERTS` (
  `OUTBREAK_ACC_ID` VARCHAR(5) NOT NULL,
  `ACC_INFO` TEXT(800) NULL,
  PRIMARY KEY (`OUTBREAK_ACC_ID`),
  CONSTRAINT `fk_ACC_ALTERTS_OUTBREAK1`
    FOREIGN KEY (`OUTBREAK_ACC_ID`)
    REFERENCES `toy_project`.`OUTBREAK_Occurrence` (`ACC_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `toy_project`.`MAP_GPS`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`MAP_GPS` (
  `OUTBREAK_ACC_ID` VARCHAR(5) NOT NULL,
  `GRS80TM_X` FLOAT NULL,
  `GRS80TM_Y` FLOAT NULL,
  PRIMARY KEY (`OUTBREAK_ACC_ID`),
  CONSTRAINT `fk_MAP_GPS_OUTBREAK1`
    FOREIGN KEY (`OUTBREAK_ACC_ID`)
    REFERENCES `toy_project`.`OUTBREAK_Occurrence` (`ACC_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `toy_project`.`OUTBREAK_LINK`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`OUTBREAK_LINK` (
  `OUTBREAK_ACC_ID` VARCHAR(5) NOT NULL,
  `LINK_ID_LINK_ID` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`OUTBREAK_ACC_ID`, `LINK_ID_LINK_ID`),
  INDEX `fk_OUTBREAK_LINK_LINK_ID1_idx` (`LINK_ID_LINK_ID` ASC) VISIBLE,
  CONSTRAINT `fk_OUTBREAK_LINK_OUTBREAK1`
    FOREIGN KEY (`OUTBREAK_ACC_ID`)
    REFERENCES `toy_project`.`OUTBREAK_Occurrence` (`ACC_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_OUTBREAK_LINK_LINK_ID1`
    FOREIGN KEY (`LINK_ID_LINK_ID`)
    REFERENCES `toy_project`.`LINK_ID` (`LINK_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
