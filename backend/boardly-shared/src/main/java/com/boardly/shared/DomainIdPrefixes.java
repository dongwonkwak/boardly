package com.boardly.shared;

/**
 * 도메인 ID prefix 정의
 */
public final class DomainIdPrefixes {
    public static final String USER = "usr";
    public static final String WORKSPACE = "wsp";
    public static final String BOARD = "brd";
    public static final String LIST = "lst";
    public static final String CARD = "crd";
    public static final String CHECKLIST = "chl";
    public static final String CHECKLIST_ITEM = "chi";
    public static final String LABEL = "lbl";
    public static final String COMMENT = "cmt";
    public static final String ATTACHMENT = "att";
    public static final String ACTIVITY_LOG = "log";
    public static final String INVITATION = "inv";

    private DomainIdPrefixes() {
        throw new UnsupportedOperationException("Utility class");
    }
}