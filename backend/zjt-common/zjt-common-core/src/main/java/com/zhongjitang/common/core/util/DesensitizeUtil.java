package com.zhongjitang.common.core.util;

/**
 * 脱敏工具类
 * <p>
 * 提供手机号、身份证号、姓名等敏感信息的脱敏处理。
 * </p>
 */
public class DesensitizeUtil {

    private DesensitizeUtil() {
    }

    /**
     * 手机号脱敏
     * <p>
     * 保留前3位和后4位，中间用*号替代。
     * 例如: 13800138000 → 138****8000
     * </p>
     *
     * @param phone 原始手机号
     * @return 脱敏后的手机号
     */
    public static String desensitizePhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 身份证号脱敏
     * <p>
     * 保留前6位和后4位，中间用*号替代。
     * 例如: 110101199001011234 → 110101****1234
     * </p>
     *
     * @param idCard 原始身份证号
     * @return 脱敏后的身份证号
     */
    public static String desensitizeIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) {
            return idCard;
        }
        return idCard.substring(0, 6) + "****" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 姓名脱敏
     * <p>
     * 保留第一个字，其余用*号替代。
     * 例如: 张三丰 → 张**
     * </p>
     *
     * @param name 原始姓名
     * @return 脱敏后的姓名
     */
    public static String desensitizeName(String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        return name.charAt(0) + "**";
    }
}
