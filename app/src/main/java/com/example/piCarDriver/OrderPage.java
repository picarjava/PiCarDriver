package com.example.piCarDriver;

import android.support.v4.app.Fragment;

public class OrderPage {
    private Fragment fragment;
    private String title;

    public OrderPage(Fragment fragment, String title) {
        setFragment(fragment);
        setTitle(title);
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
