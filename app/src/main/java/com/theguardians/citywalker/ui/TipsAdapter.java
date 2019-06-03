package com.theguardians.citywalker.ui;
/**
 * This class is utilised for safety tip page
 * @Author Richard
 * @Version 1.1
 */
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.theguardians.citywalker.R;

import java.util.List;

public class TipsAdapter extends PagerAdapter {

    private List<TipsModel> models;
    private LayoutInflater layoutInflater;
    private Context context;

    public TipsAdapter(List<TipsModel> models, Context context) {
        this.models = models;
        this.context = context;
    }

    @Override
    public int getCount() {
        return models.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.tips_item, container, false);

        ImageView imageView;
        TextView title, desc;

        imageView = view.findViewById(R.id.image);
        //title = view.findViewById(R.id.title);
        //desc = view.findViewById(R.id.desc);

        imageView.setImageResource(models.get(position).getImage());
        //title.setText(models.get(position).getTitle());
        //desc.setText(models.get(position).getDesc());

        /**
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TipsDetailActivity.class);
                intent.putExtra("param", models.get(position).getTitle());
                context.startActivity(intent);
                //finish();
            }
        });
         **/
        container.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
