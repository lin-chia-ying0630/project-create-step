CREATE TABLE main.code_definition (
  code_group VARCHAR(64) NOT NULL,
  code_field VARCHAR(64) NOT NULL,
  code_before VARCHAR(64) NOT NULL,
  code_after VARCHAR(64),
  code_description VARCHAR(255) NOT NULL,
  PRIMARY KEY (code_group, code_field, code_before)
);

INSERT INTO main.code_definition
(code_group,code_field,code_before,code_after,code_description)
VALUES
('postal-code','zip_code3','100','臺北市|中正區','Zhongzheng Dist., Taipei City'),
('postal-code','zip_code3','114','臺北市|內湖區','Neihu Dist., Taipei City'),
('postal-code','zip_code3','334','桃園市|八德區','Bade Dist., Taoyuan City');
