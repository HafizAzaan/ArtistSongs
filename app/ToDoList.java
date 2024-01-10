package com.fizazaan.itt632_group_project;
public class ToDoList {

    String titleId;
    String title;


    public ToDoList(){}

    public ToDoList(String titleId, String title) {
        this.titleId = titleId;
        this.title = title;
    }

    public String getTitleId() {
        return titleId;
    }

    public String getTitle() {
        return title;
    }
}
