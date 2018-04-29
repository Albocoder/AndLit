package com.andlit.ui.groupView;

public class Group
{
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String name;

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean admin;

    Group(String name, boolean admin)
    {
        this.name = name;
        this.admin = admin;
    }
}
