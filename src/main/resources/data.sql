
-- DROP TABLE IF EXISTS retainvariables;
 
CREATE TABLE IF NOT EXISTS retainvariables (
  id INT AUTO_INCREMENT  PRIMARY KEY,
  devicehandle INT DEFAULT 0,
  propertyhandle INT DEFAULT 0,
  propertyvalue VARCHAR(255) NOT NULL,
  propertyname VARCHAR(255) NOT NULL,
  tstamp TIMESTAMP NULL
);