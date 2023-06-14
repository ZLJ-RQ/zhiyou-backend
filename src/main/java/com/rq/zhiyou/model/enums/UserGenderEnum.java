package com.rq.zhiyou.model.enums;

/**
 * @author 若倾
 * @description 用户性别枚举
 */
public enum UserGenderEnum {
    FEMALE(0,"女"),
    MALE(1,"男");

    private int value;
    private String gender;

    public static UserGenderEnum getEnumByValue(Integer value){
        if (value==null){
            return null;
        }
        UserGenderEnum[] values = UserGenderEnum.values();
        for (UserGenderEnum teamStatusEnum : values) {
            if (teamStatusEnum.getValue()==value){
                return teamStatusEnum;
            }
        }
        return null;
    }

    UserGenderEnum(int value, String gender) {
        this.value = value;
        this.gender = gender;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
