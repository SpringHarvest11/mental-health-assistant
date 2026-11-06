package com.nie.mental.common.utils;

public class CurrentHolder {

    //
    private static final ThreadLocal<Integer> USER_ID = new ThreadLocal<>();
    //
    private static final ThreadLocal<Integer> USER_TYPE = new ThreadLocal<>();

    public static void setUser(Integer userId, Integer userType) {

        USER_ID.set(userId);

        USER_TYPE.set(userType);
    }

    public static Integer getUserId() {
        return USER_ID.get();
    }

    public static Integer getUserType() {
        return USER_TYPE.get();
    }

    public static void remove() {

        USER_ID.remove();
        USER_TYPE.remove();
    }
}
