package com.ifit.ronnie.configuration;

public class ChatContext {

    private static final InheritableThreadLocal<String> USER_ID = new InheritableThreadLocal<>();
    private static final InheritableThreadLocal<String> COACH_NAME = new InheritableThreadLocal<>();

    private ChatContext() {}

    public static void set(String userId, String coachName) {
        USER_ID.set(userId);
        COACH_NAME.set(coachName);
        System.out.println("[ChatContext] set — hilo=" + Thread.currentThread().getName() + " userId=" + userId + " coachName=" + coachName);
    }

    public static String getUserId() {
        return USER_ID.get();
    }

    public static String getCoachName() {
        return COACH_NAME.get();
    }

    public static void clear() {
        USER_ID.remove();
        COACH_NAME.remove();
    }
}
