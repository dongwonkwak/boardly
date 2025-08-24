package com.boardly.shared;

/**
 * 도메인 ID prefix 정의
 */
public class DomainPrefixes {
    public static final String USER = "u_";
    public static final String WORKSPACE = "ws_";
    public static final String BOARD = "b_";
    public static final String WORKSPACE_MEMBERSHIP = "wm_";
    public static final String COLUMN = "col_";
    public static final String CARD = "c_";
    public static final String COMMENT = "cm_";
    public static final String LABEL = "l_";
    public static final String CHECKLIST_ITEM = "ci_";
    public static final String ACTIVITY_LOG = "al_";

    private DomainPrefixes() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }
}