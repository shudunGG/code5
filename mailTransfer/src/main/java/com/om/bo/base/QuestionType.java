package com.om.bo.base;

import java.util.Objects;

public class QuestionType {
    private String title;
    private String icon;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestionType)) return false;
        QuestionType that = (QuestionType) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(icon, that.icon);
    }

    @Override
    public String toString() {
        return "QuestionType{" +
                "title='" + title + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, icon);
    }

    public QuestionType(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
