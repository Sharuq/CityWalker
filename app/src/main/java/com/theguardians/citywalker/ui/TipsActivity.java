package com.theguardians.citywalker.ui;

import android.animation.ArgbEvaluator;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.theguardians.citywalker.R;

import java.util.ArrayList;
import java.util.List;

public class TipsActivity extends AppCompatActivity {

    ViewPager viewPager;
    TipsAdapter adapter;
    List<TipsModel> models;
    Integer[] colors = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tips_main);

        models = new ArrayList<>();
        models.add(new TipsModel(R.drawable.tip1, "Tip 1", "Walk confidently at night. Do not distract your attention with a mobile phone"));
        models.add(new TipsModel(R.drawable.tip2, "Tip 2", "Do not wear earphones while walking alone at night."));
        models.add(new TipsModel(R.drawable.tip3, "Tip 3", "Check out the location of the nearby police and places with more people around you like open shops."));
        models.add(new TipsModel(R.drawable.tip4, "Tip 4", "If your are traveling with expensive things, do not pull them out in public unless you need to."));
        models.add(new TipsModel(R.drawable.tip5, "Tip 5", "When you come across robbery, follow the robber's directions. But do not offer more than what they ask for. And make mental notes of the robber's appearance."));

        adapter = new TipsAdapter(models, this);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setPadding(100, 0, 100, 0);

        Integer[] colors_temp = {
                getResources().getColor(R.color.color1),
                getResources().getColor(R.color.color2),
                getResources().getColor(R.color.color1),
                getResources().getColor(R.color.color2),
                getResources().getColor(R.color.color1)

        };

        colors = colors_temp;


        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if (position < (adapter.getCount() -1) && position < (colors.length - 1)) {
                    viewPager.setBackgroundColor(

                            (Integer) argbEvaluator.evaluate(
                                    positionOffset,
                                    colors[position],
                                    colors[position + 1]
                            )
                    );
                }

                else {
                    viewPager.setBackgroundColor(colors[colors.length - 1]);
                }
            }


            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }
}
