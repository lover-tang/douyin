package com.douyin.shipping.client.enums;

import lombok.Getter;

/**
 * 快递鸟快递公司编码枚举
 * 数据来源：快递鸟官方文档
 *
 * @see <a href="https://www.yuque.com/kdnjishuzhichi/dfcrg1/mza2ln">快递鸟官方文档</a>
 */
@Getter
public enum KdniaoShipperCode {

    // ==================== 主流快递 ====================
    /**
     * 顺丰速运
     */
    SF("SF", "顺丰速运"),

    /**
     * 中通快递
     */
    ZTO("ZTO", "中通快递"),

    /**
     * 圆通速递
     */
    YTO("YTO", "圆通速递"),

    /**
     * 韵达速递
     */
    YD("YD", "韵达速递"),

    /**
     * 申通快递
     */
    STO("STO", "申通快递"),

    /**
     * 百世快递（原百世汇通）
     */
    HTKY("HTKY", "百世快递"),

    /**
     * 极兔速递
     */
    JTSD("JTSD", "极兔速递"),

    /**
     * 邮政快递包裹
     */
    YZPY("YZPY", "邮政快递包裹"),

    /**
     * EMS
     */
    EMS("EMS", "EMS"),

    /**
     * 京东物流
     */
    JD("JD", "京东物流"),

    /**
     * 德邦快递
     */
    DBL("DBL", "德邦快递"),

    // ==================== 其他快递 ====================
    /**
     * 天天快递
     */
    TTKDEX("TTKDEX", "天天快递"),

    /**
     * 宅急送
     */
    ZJS("ZJS", "宅急送"),

    /**
     * 优速快递
     */
    UC("UC", "优速快递"),

    /**
     * 邮政国内标快
     */
    POST("POST", "邮政国内标快"),

    /**
     * 安能物流
     */
    ANE("ANE", "安能物流"),

    /**
     * 快捷快递
     */
    FAST("FAST", "快捷快递"),

    /**
     * 国通快递
     */
    GTO("GTO", "国通快递"),

    /**
     * 全峰快递
     */
    QFKD("QFKD", "全峰快递"),

    /**
     * 中铁快运
     */
    ZTKY("ZTKY", "中铁快运"),

    /**
     * 中铁物流
     */
    ZTWL("ZTWL", "中铁物流"),

    /**
     * 龙邦快递
     */
    LB("LB", "龙邦快递"),

    /**
     * 速尔快递
     */
    SURE("SURE", "速尔快递"),

    /**
     * 信丰物流
     */
    XFEX("XFEX", "信丰物流"),

    /**
     * 能达速递
     */
    NEDA("NEDA", "能达速递"),

    /**
     * 全一快递
     */
    UAPEX("UAPEX", "全一快递"),

    /**
     * 联昊通速递
     */
    LTS("LTS", "联昊通速递"),

    /**
     * 民航快递
     */
    CAE("CAE", "民航快递"),

    /**
     * 亚风快递
     */
    YFSD("YFSD", "亚风快递"),

    /**
     * 源安达
     */
    YADEX("YADEX", "源安达"),

    /**
     * 加运美
     */
    JYMS("JYMS", "加运美"),

    /**
     * 万象物流
     */
    WXWL("WXWL", "万象物流"),

    /**
     * 宏品物流
     */
    HPWL("HPWL", "宏品物流"),

    /**
     * 凡客如风达
     */
    RFD("RFD", "凡客如风达"),

    /**
     * 增益快递
     */
    ZYWL("ZYWL", "增益快递"),

    /**
     * 赛澳递
     */
    SAD("SAD", "赛澳递"),

    /**
     * 海盟速递
     */
    HMSD("HMSD", "海盟速递"),

    /**
     * 共速达
     */
    GSD("GSD", "共速达"),

    /**
     * 嘉里物流
     */
    KERRY("KERRY", "嘉里物流"),

    /**
     * 德邦物流
     */
    DBLWL("DBLWL", "德邦物流"),

    /**
     * 新邦物流
     */
    XBWL("XBWL", "新邦物流"),

    /**
     * 大田物流
     */
    DTWL("DTWL", "大田物流"),

    /**
     * 佳吉快运
     */
    CNEX("CNEX", "佳吉快运"),

    /**
     * 盛丰物流
     */
    SFWL("SFWL", "盛丰物流"),

    /**
     * 盛辉物流
     */
    SHWL("SHWL", "盛辉物流"),

    /**
     * 远成物流
     */
    YCWL("YCWL", "远成物流"),

    /**
     * 长通物流
     */
    CTWL("CTWL", "长通物流"),

    /**
     * 飞康达
     */
    FKD("FKD", "飞康达"),

    /**
     * 元智捷诚
     */
    YZJC("YZJC", "元智捷诚"),

    /**
     * 邮政国际
     */
    GJYZ("GJYZ", "邮政国际"),

    /**
     * 国际EMS
     */
    GJEMS("GJEMS", "国际EMS"),

    /**
     * 顺丰国际
     */
    GJSF("GJSF", "顺丰国际"),

    /**
     * 圆通国际
     */
    GJYTO("GJYTO", "圆通国际"),

    /**
     * 中通国际
     */
    GJZTO("GJZTO", "中通国际"),

    /**
     * 韵达国际
     */
    GJYD("GJYD", "韵达国际"),

    /**
     * 申通国际
     */
    GJSTO("GJSTO", "申通国际"),

    /**
     * DHL
     */
    DHL("DHL", "DHL"),

    /**
     * UPS
     */
    UPS("UPS", "UPS"),

    /**
     * FedEx
     */
    FEDEX("FEDEX", "FedEx"),

    /**
     * TNT
     */
    TNT("TNT", "TNT"),

    /**
     * 当当物流
     */
    DANGDANG("DANGDANG", "当当物流"),

    /**
     * 苏宁物流
     */
    SUNING("SUNING", "苏宁物流"),

    /**
     * 唯品会物流
     */
    VIP("VIP", "唯品会物流"),

    /**
     * 亚马逊物流
     */
    AMAZON("AMAZON", "亚马逊物流"),

    /**
     * 菜鸟物流
     */
    CAINIAO("CAINIAO", "菜鸟物流");

    /**
     * 快递公司编码
     */
    private final String code;

    /**
     * 快递公司名称
     */
    private final String name;

    KdniaoShipperCode(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 快递公司编码
     * @return 枚举对象，未找到返回null
     */
    public static KdniaoShipperCode getByCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (KdniaoShipperCode shipper : values()) {
            if (shipper.getCode().equalsIgnoreCase(code)) {
                return shipper;
            }
        }
        return null;
    }

    /**
     * 根据名称获取枚举
     *
     * @param name 快递公司名称
     * @return 枚举对象，未找到返回null
     */
    public static KdniaoShipperCode getByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (KdniaoShipperCode shipper : values()) {
            if (shipper.getName().equals(name)) {
                return shipper;
            }
        }
        return null;
    }

    /**
     * 判断编码是否有效
     *
     * @param code 快递公司编码
     * @return 是否有效
     */
    public static boolean isValidCode(String code) {
        return getByCode(code) != null;
    }
}
