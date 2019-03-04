package com.example.piCarDriver;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class OrderFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_order, container, false);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager viewPager = view.findViewById(R.id.viewPager);
        List<OrderPage> orderPages = new ArrayList<>();
        viewPager.setAdapter(new ViewPageAdapter(getChildFragmentManager(), orderPages));
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

    private class ViewPageAdapter extends FragmentPagerAdapter {
        private List<OrderPage> pages;

        ViewPageAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        ViewPageAdapter(FragmentManager fragmentManager, List<OrderPage> pages) {
            this(fragmentManager);
            setPages(pages);
        }

        @Override
        public Fragment getItem(int i) {
            return pages.get(i).getFragment();
        }

        @Override
        public int getCount() {
            return pages.size();
        }

        public List<OrderPage> getPages() {
            return pages;
        }

        void setPages(List<OrderPage> pages) {
            this.pages = pages;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pages.get(position).getTitle();
        }
    }
}
