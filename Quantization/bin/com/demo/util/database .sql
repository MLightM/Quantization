CREATE DATABASE /*!32312 IF NOT EXISTS*/ `quantization` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `quantization`;

-- ----------------------------
-- Table structure for eur_usd_15min
-- ----------------------------
DROP TABLE IF EXISTS `eur/usd_min15`;
CREATE TABLE `eur/usd_min15` (
  `snapshotdate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `timezone` varchar(10) NOT NULL DEFAULT 'EST',
  `opentime` timestamp NOT NULL DEFAULT '2000-01-01 00:00:00',
  `closetime` timestamp NOT NULL DEFAULT '2000-01-01 00:00:00',
  `openbid` double DEFAULT 0.0,
  `highbid` double DEFAULT 0.0,
  `lowbid` double DEFAULT 0.0,
  `closebid` double DEFAULT 0.0,
  `openask` double DEFAULT 0.0,
  `highask` double DEFAULT 0.0,
  `lowask` double DEFAULT 0.0,
  `closeask` double DEFAULT 0.0,
  `volume` double DEFAULT 0.0,
  PRIMARY KEY (`snapshotdate`,`timezone`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;

