-- 物流公司映射初始数据（淘宝物流名称 -> 抖音物流名称/编码/快递鸟编码）
-- 使用 MERGE 避免重复插入
MERGE INTO t_logistics_mapping (taobao_name, taobao_code, douyin_name, douyin_code, kdniao_code, enabled, create_time, update_time)
KEY (taobao_name)
VALUES ('中通快递', 'ZTO', '中通快递', 'ZTO', 'ZTO', true, NOW(), NOW());

MERGE INTO t_logistics_mapping (taobao_name, taobao_code, douyin_name, douyin_code, kdniao_code, enabled, create_time, update_time)
KEY (taobao_name)
VALUES ('圆通速递', 'YTO', '圆通速递', 'YTO', 'YTO', true, NOW(), NOW());

MERGE INTO t_logistics_mapping (taobao_name, taobao_code, douyin_name, douyin_code, kdniao_code, enabled, create_time, update_time)
KEY (taobao_name)
VALUES ('韵达快递', 'YD', '韵达快递', 'YUNDA', 'YD', true, NOW(), NOW());

MERGE INTO t_logistics_mapping (taobao_name, taobao_code, douyin_name, douyin_code, kdniao_code, enabled, create_time, update_time)
KEY (taobao_name)
VALUES ('申通快递', 'STO', '申通快递', 'STO', 'STO', true, NOW(), NOW());

MERGE INTO t_logistics_mapping (taobao_name, taobao_code, douyin_name, douyin_code, kdniao_code, enabled, create_time, update_time)
KEY (taobao_name)
VALUES ('顺丰速运', 'SF', '顺丰速运', 'SF', 'SF', true, NOW(), NOW());

MERGE INTO t_logistics_mapping (taobao_name, taobao_code, douyin_name, douyin_code, kdniao_code, enabled, create_time, update_time)
KEY (taobao_name)
VALUES ('极兔速递', 'JTSD', '极兔速递', 'JTSD', 'JTSD', true, NOW(), NOW());

MERGE INTO t_logistics_mapping (taobao_name, taobao_code, douyin_name, douyin_code, kdniao_code, enabled, create_time, update_time)
KEY (taobao_name)
VALUES ('邮政快递包裹', 'YZPY', '邮政快递包裹', 'YZPY', 'YZPY', true, NOW(), NOW());

MERGE INTO t_logistics_mapping (taobao_name, taobao_code, douyin_name, douyin_code, kdniao_code, enabled, create_time, update_time)
KEY (taobao_name)
VALUES ('EMS', 'EMS', 'EMS', 'EMS', 'EMS', true, NOW(), NOW());

MERGE INTO t_logistics_mapping (taobao_name, taobao_code, douyin_name, douyin_code, kdniao_code, enabled, create_time, update_time)
KEY (taobao_name)
VALUES ('京东快递', 'JD', '京东快递', 'JD', 'JD', true, NOW(), NOW());

MERGE INTO t_logistics_mapping (taobao_name, taobao_code, douyin_name, douyin_code, kdniao_code, enabled, create_time, update_time)
KEY (taobao_name)
VALUES ('百世快递', 'HTKY', '百世快递', 'HTKY', 'HTKY', true, NOW(), NOW());

MERGE INTO t_logistics_mapping (taobao_name, taobao_code, douyin_name, douyin_code, kdniao_code, enabled, create_time, update_time)
KEY (taobao_name)
VALUES ('德邦快递', 'DBL', '德邦快递', 'DBL', 'DBL', true, NOW(), NOW());
