CREATE DATABASE `car_battery_warning_db` CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci';

-- 车辆信息表
CREATE TABLE vehicle_info (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '车辆 id',
                              vid VARCHAR(16) NOT NULL UNIQUE COMMENT '车辆唯一标识(16位)',
                              frame_number BIGINT NOT NULL UNIQUE COMMENT '车架编号',
                              battery_type TINYINT NOT NULL COMMENT '电池类型:0-三元电池,1-铁锂电池',
                              total_mileage INT NOT NULL DEFAULT 0 COMMENT '总里程(km)',
                              battery_health TINYINT NOT NULL DEFAULT 100 COMMENT '电池健康状态(%)',
                              INDEX idx_frame_number (frame_number)
) COMMENT '车辆基本信息表';

-- 预警规则表
CREATE TABLE rule (
                      id INT AUTO_INCREMENT PRIMARY KEY COMMENT '规则序号',
                      rule_id INT NOT NULL COMMENT '规则编号',
                      rule_name VARCHAR(50) NOT NULL COMMENT '规则名称',
                      battery_type TINYINT NOT NULL COMMENT '电池类型:0-三元电池,1-铁锂电池',
                      warning_rule JSON NOT NULL COMMENT '预警规则',
                      UNIQUE KEY uniq_rule (rule_id, battery_type)
) COMMENT '电池预警规则表';

-- 电池信号表
CREATE TABLE battery_signal (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '信号ID',
                                vid VARCHAR(16) NOT NULL COMMENT '车辆vid',
                                frame_number BIGINT NOT NULL COMMENT '车架编号',
                                battery_type TINYINT NOT NULL COMMENT '电池类型:0-三元电池,1-铁锂电池',
                                signal_data JSON NOT NULL COMMENT '信号数据(JSON格式)',
                                report_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
                                processed BOOLEAN DEFAULT FALSE COMMENT '是否已处理',
                                processed_time TIMESTAMP NULL COMMENT '处理时间',
                                INDEX idx_vid (vid),
                                INDEX idx_time_status (report_time, processed),
                                FOREIGN KEY (vid) REFERENCES vehicle_info(vid)
) COMMENT '电池信号上报记录表';