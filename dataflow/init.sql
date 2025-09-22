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
  `ROAD_NAME` VARCHAR(45) NOT NULL COMMENT '도로 명',
  `ST_NODE_NM` VARCHAR(45) NOT NULL COMMENT '시작 노드 명',
  `ED_NODE_NM` VARCHAR(3) NOT NULL COMMENT '종료 노드 명',
  `MAP_DIST` INT NOT NULL,
  `REG_CD` INT NOT NULL COMMENT '권역코드',
  PRIMARY KEY (`LINK_ID`),
  INDEX `fk_LINK_ID_REG_CD_REG_CD_idx` (`REG_CD` ASC) VISIBLE,
  CONSTRAINT `fk_LINK_ID_REG_CD_REG_CD`
    FOREIGN KEY (`REG_CD`)
    REFERENCES `toy_project`.`REG_CD` (`REG_CD`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `toy_project`.`OUTBREAK_CODE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`OUTBREAK_CODE` (
  `ACC_TYPE` VARCHAR(5) NOT NULL COMMENT '서울시 돌발 유형 코드 정보\n\n\"돌발 유형 코드\"\n',
  `ACC_TYPE_NM` VARCHAR(15) NULL COMMENT '돌발 유형 코드 명',
  PRIMARY KEY (`ACC_TYPE`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `toy_project`.`OUTBREAK_DETAIL_CODE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`OUTBREAK_DETAIL_CODE` (
  `ACC_DTYPE` VARCHAR(10) NOT NULL COMMENT '서울시 돌발 세부유형 코드 정보\n\n돌발 세부 유형 코드',
  `ACC_DTYPE_NM` VARCHAR(15) NULL COMMENT '돌발 세부 유형 코드 명',
  PRIMARY KEY (`ACC_DTYPE`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `toy_project`.`OUTBREAK`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `toy_project`.`OUTBREAK` (
  `ACC_ID` VARCHAR(5) NOT NULL COMMENT '서울시 실시간 돌발 정보\n\n돌발 아이디',
  `LINK_ID` VARCHAR(45) NOT NULL COMMENT '링크 아이디',
  `OCCR_DATE` DATETIME NOT NULL COMMENT '발생 일자',
  `EXP_CLR_DATE` DATETIME NOT NULL COMMENT '종료 예정 일자',
  `ACC_TYPE` VARCHAR(45) NOT NULL COMMENT '돌발 유형 코드',
  `ACC_DTYPE` VARCHAR(10) NOT NULL COMMENT '돌발 세부 유형 코드',
  `ACC_INFO` TINYTEXT NOT NULL COMMENT '돌발 내용',
  `GRS80TM_X` FLOAT NOT NULL COMMENT 'TM X 좌표',
  `GRS80TM_Y` FLOAT NOT NULL COMMENT 'TM Y 좌표',
  PRIMARY KEY (`ACC_ID`),
  INDEX `LINK_ID_idx` (`LINK_ID` ASC) VISIBLE,
  INDEX `ACC_TYPE_idx` (`ACC_TYPE` ASC) VISIBLE,
  INDEX `ACC_TYPE_NM_idx` (`ACC_DTYPE` ASC) VISIBLE,
  CONSTRAINT `fk_OUTBREAK_ROAD_ID_LINK_ID`
    FOREIGN KEY (`LINK_ID`)
    REFERENCES `toy_project`.`LINK_ID` (`LINK_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_OUTBREAK_OUTBREAK_CODE_ACC_TYPE`
    FOREIGN KEY (`ACC_TYPE`)
    REFERENCES `toy_project`.`OUTBREAK_CODE` (`ACC_TYPE`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_OUTBREAK_OUTBREAK_DETAIL_CODE_ACC_TYPE_NM`
    FOREIGN KEY (`ACC_DTYPE`)
    REFERENCES `toy_project`.`OUTBREAK_DETAIL_CODE` (`ACC_DTYPE`)
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
  `GU_CODE` INT NOT NULL,
  `RAINFALL10` INT NULL,
  `RECEIVE_TIME` VARCHAR(45) NULL,
  PRIMARY KEY (`GU_CODE`),
  CONSTRAINT `fk_RAIN_REGION_GU_CODE`
    FOREIGN KEY (`GU_CODE`)
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
  CONSTRAINT `fk_ROAD_TRAFFIC_ROAD_IDLINK_ID`
    FOREIGN KEY (`LINK_ID`)
    REFERENCES `toy_project`.`LINK_ID` (`LINK_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
