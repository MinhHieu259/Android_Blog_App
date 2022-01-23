package com.example.blogapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.blogapp.R;

public class ViewPagerAdapter extends PagerAdapter {
private Context context;
private LayoutInflater layoutInflater;

    public ViewPagerAdapter(Context context) {
        this.context = context;
    }
    private int images[] = {
        R.drawable.pig,
            R.drawable.pig,
            R.drawable.pig
    };
    private String titles[] = {
         "Learn",
         "Create",
         "Enjoy"
    };
    private String descs[] = {
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas ultricies blandit vehicula",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas ultricies blandit vehicula",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas ultricies blandit vehicula"
    };
    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (LinearLayout)object ;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.view_pager, container, false);

        // init View
        ImageView imageView = v.findViewById(R.id.imgViewPager);
        TextView txtTitle = v.findViewById(R.id.titleViewPager);
        TextView txtDesc = v.findViewById(R.id.descViewPager);

        imageView.setImageResource(images[position]);
        txtTitle.setText(titles[position]);
        txtDesc.setText(descs[position]);

        container.addView(v);
        return  v;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
       container.removeView((LinearLayout)object);
    }
}
