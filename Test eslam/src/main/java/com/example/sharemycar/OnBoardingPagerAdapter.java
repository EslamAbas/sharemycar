package com.example.sharemycar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

public class OnBoardingPagerAdapter extends PagerAdapter {

    private ArrayList<Integer> layouts;
    private LayoutInflater layoutInflater;
    private Context context;

    OnBoardingPagerAdapter(Context context, ArrayList<Integer> layouts) {
        this.layouts = new ArrayList<>();
        this.context = context;
        this.layouts = layouts;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(layouts.get(position), container, false);
        container.addView(view);

        return view;
    }

    @Override
    public int getCount() {
        return layouts.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
        return view == obj;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        View view = (View) object;
        container.removeView(view);
    }
}

