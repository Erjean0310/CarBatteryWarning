CREATE DATABASE `car_battery_warning_db` CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci';

-- 车辆信息表
CREATE TABLE vehicle_info (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '车辆 id',
                              vid VARCHAR(16) NOT NULL UNIQUE COMMENT '车辆唯一标识(16位)',
                              car_id BIGINT NOT NULL UNIQUE COMMENT '车架编号',
                              battery_type TINYINT NOT NULL COMMENT '电池类型:0-三元电池,1-铁锂电池',
                              total_mileage INT NOT NULL DEFAULT 0 COMMENT '总里程(km)',
                              battery_health TINYINT NOT NULL DEFAULT 100 COMMENT '电池健康状态(%)'
) COMMENT '车辆基本信息表';

-- 预警规则表
CREATE TABLE warn_rule (
                           id INT AUTO_INCREMENT PRIMARY KEY COMMENT '规则序号',
                           warn_id INT NOT NULL COMMENT '规则编号',
                           rule_name VARCHAR(50) NOT NULL COMMENT '规则名称',
                           battery_type TINYINT NOT NULL COMMENT '电池类型:0-三元电池,1-铁锂电池',
                           rule JSON NOT NULL COMMENT '预警规则',
                           UNIQUE KEY uniq_rule (warn_id, battery_type)
) COMMENT '电池预警规则表';

-- 电池信号表
CREATE TABLE battery_signal (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '信号ID',
                                vid VARCHAR(16) NOT NULL COMMENT '车辆vid',
                                car_id BIGINT NOT NULL COMMENT '车架编号',
                                battery_type TINYINT NOT NULL COMMENT '电池类型:0-三元电池,1-铁锂电池',
                                `signal` JSON NOT NULL COMMENT '信号数据(JSON格式)',
                                report_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
                                processed BOOLEAN DEFAULT FALSE COMMENT '是否已处理',
                                processed_time TIMESTAMP NULL COMMENT '处理时间',
                                INDEX idx_vid (vid),
                                INDEX idx_time_status (report_time, processed),
                                FOREIGN KEY (vid) REFERENCES vehicle_info(vid)
) COMMENT '电池信号上报记录表';

INSERT INTO vehicle_info (vid, car_id, battery_type, total_mileage, battery_health) VALUES
                                                                                        ('1234abcd1234abcd', 1, 0, 100, 100),
                                                                                        ('2345bcde2345bccde', 2, 1, 600, 95),
                                                                                        ('3456cdef3456cdef', 3, 0, 300, 98);

-- 规则1: 三元电池的电压差报警
INSERT INTO warn_rule (warn_id, rule_name, battery_type, rule)
VALUES (
           1,
           '电压差报警',
           0,
           JSON_ARRAY(
                   JSON_OBJECT('max', NULL, 'min', 5.0, 'level', 0),
                   JSON_OBJECT('max', 5.0, 'min', 3.0, 'level', 1),
                   JSON_OBJECT('max', 3.0, 'min', 1.0, 'level', 2),
                   JSON_OBJECT('max', 1.0, 'min', 0.6, 'level', 3),
                   JSON_OBJECT('max', 0.6, 'min', 0.2, 'level', 4)
           )
       );

-- 规则2: 铁锂电池的电压差报警
INSERT INTO warn_rule (warn_id, rule_name, battery_type, rule)
VALUES (
           1,
           '电压差报警',
           1,
           JSON_ARRAY(
                   JSON_OBJECT('max', NULL, 'min', 2.0, 'level', 0),
                   JSON_OBJECT('max', 2.0, 'min', 1.0, 'level', 1),
                   JSON_OBJECT('max', 1.0, 'min', 0.7, 'level', 2),
                   JSON_OBJECT('max', 0.7, 'min', 0.4, 'level', 3),
                   JSON_OBJECT('max', 0.4, 'min', 0.2, 'level', 4)
           )
       );

-- 规则3: 三元电池的电流差报警
INSERT INTO warn_rule (warn_id, rule_name, battery_type, rule)
VALUES (
           2,
           '电流差报警',
           0,
           JSON_ARRAY(
                   JSON_OBJECT('max', NULL, 'min', 3.0, 'level', 0),
                   JSON_OBJECT('max', 3.0, 'min', 1.0, 'level', 1),
                   JSON_OBJECT('max', 1.0, 'min', 0.2, 'level', 2)
           )
       );

-- 规则4: 铁锂电池的电流差报警
INSERT INTO warn_rule (warn_id, rule_name, battery_type, rule)
VALUES (
           2,
           '电流差报警',
           1,
           JSON_ARRAY(
                   JSON_OBJECT('max', NULL, 'min', 1.0, 'level', 0),
                   JSON_OBJECT('max', 1.0, 'min', 0.5, 'level', 1),
                   JSON_OBJECT('max', 0.5, 'min', 0.2, 'level', 2)
           )
       );