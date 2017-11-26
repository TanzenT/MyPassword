/*
 * Create by 윤규도 on 2017. 11. 26.
 * Copyright (C) 2017. 윤규도. All rights reserved.
 */

package com.smileapps.mypassword.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * 암호
 *
 * @author zeus8502
 */
public class Password implements Serializable {
    private static final long serialVersionUID = -961233794781935060L;

    /** 데이터 베이스 ID */
    private int id;

    /** 만든 날짜 */
    private long createDate;

    /** 제목 */
    private String title;
    /** ID */
    private String userName;
    /** 비밀번호 */
    private String password;
    /** 메모 */
    private String note;

    /** 그룹 이름 */
    private String groupName;

    private boolean isTop = false;

    public static Password createFormJson(String json) throws JSONException {
        Password password = new Password();
        JSONObject jsonObject = new JSONObject(json);
        password.setId(jsonObject.optInt("id", 0));
        password.setCreateDate(jsonObject.getLong("createDate"));
        password.setTitle(jsonObject.getString("title"));
        password.setUserName(jsonObject.getString("userName"));
        password.setPassword(jsonObject.getString("password"));
        password.setNote(jsonObject.getString("note"));
        password.setTop(jsonObject.optBoolean("isTop", false));
        password.setGroupName(jsonObject.optString("groupName", "默认"));
        return password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("createDate", createDate);
            jsonObject.put("title", title);
            jsonObject.put("userName", userName);
            jsonObject.put("password", password);
            jsonObject.put("note", note);
            jsonObject.put("isTop", isTop);
            jsonObject.put("groupName", groupName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public boolean isTop() {
        return isTop;
    }

    public void setTop(boolean isTop) {
        this.isTop = isTop;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "Password [id=" + id + ", createDate=" + createDate + ", title=" + title + ", userName=" + userName
                + ", password=" + password + ", note=" + note + ", groupName=" + groupName + ", isTop=" + isTop + "]";
    }
}
