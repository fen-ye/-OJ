package com.siyue.siojbackendmodel.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目提交枚举
 *
 */
public enum QuestionSubmitStatusEnum {

    // 0 - 待判题、1 - 判题中、2 - 成功、3 - 失败
    WAITING("等待中", 0),
    RUNNING("判题中", 1),
    SUCCEED("成功", 2),
    FAILED("失败", 3);

    private final String text;

    private final Integer value;

    QuestionSubmitStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取枚举类中所有枚举实例的value值，并将其转换为一个整数列表。
     * 该方法通过流式操作将枚举实例的value属性提取出来，并收集到一个List中。
     *
     * @return 包含所有枚举实例value值的整数列表
     */
    public static List<Integer> getValues() {
        // 使用流式操作将枚举实例的value属性提取并收集到List中 每个枚举类都会自动生成一个静态方法values()
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }


    /**
     * 根据给定的整数值获取对应的枚举实例。
     * 该方法会遍历 QuestionSubmitStatusEnum 的所有枚举值，查找与给定值匹配的枚举实例。
     * 如果给定值为空或未找到匹配的枚举实例，则返回 null。
     *
     * @param value 要查找的整数值，通常与枚举实例的某个属性值对应
     * @return 与给定值匹配的 QuestionSubmitStatusEnum 枚举实例，如果未找到则返回 null
     */
    public static QuestionSubmitStatusEnum getEnumByValue(Integer value) {
        // 检查输入值是否为空，若为空则直接返回 null
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }

        // 遍历所有枚举值，查找与给定值匹配的枚举实例
        for (QuestionSubmitStatusEnum anEnum : QuestionSubmitStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }

        // 未找到匹配的枚举实例，返回 null
        return null;
    }


    public Integer getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
