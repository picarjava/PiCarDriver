package com.example.piCarDriver;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.piCarDriver.orderPageFragment.LongTermOrderPageFragment;
import com.example.piCarDriver.orderPageFragment.SingleOrderPageFragment;

import java.util.ArrayList;
import java.util.List;

public class OrderFragment extends Fragment {
    private final static String TAG = "OrderFragment";
    private List<OrderPage> orderPages;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            if (savedInstanceState.getBoolean("Show"))
                getFragmentManager().beginTransaction().hide(this).commit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_order, container, false);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager viewPager = view.findViewById(R.id.viewPager);
        orderPages = new ArrayList<>();
        orderPages.add(new OrderPage(new SingleOrderPageFragment(), "單人訂單"));
        orderPages.add(new OrderPage(new LongTermOrderPageFragment(), "長期訂單"));
        viewPager.setAdapter(new ViewPageAdapter(getChildFragmentManager(), orderPages));
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, String.valueOf(isHidden()));
        outState.putBoolean("Show", isHidden());
    }

    private OrderPage newOrderPage(String url, String action, String title) {
        SingleOrderPageFragment orderPageFragment = new SingleOrderPageFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putString("action", action);
        orderPageFragment.setArguments(bundle);
        return new OrderPage(orderPageFragment, title);
    }
}
