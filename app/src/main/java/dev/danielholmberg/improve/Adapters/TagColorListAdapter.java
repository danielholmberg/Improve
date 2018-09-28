package dev.danielholmberg.improve.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import dev.danielholmberg.improve.R;

public class TagColorListAdapter extends ArrayAdapter<String> {

    private Context context;
    public int[] tagDrawables = new int[]{
            R.drawable.ic_menu_tag_red,
            R.drawable.ic_menu_tag_purple,
            R.drawable.ic_menu_tag_blue,
            R.drawable.ic_menu_tag_orange,
            R.drawable.ic_menu_tag_blue_grey,
            R.drawable.ic_menu_tag_baby_blue,
            R.drawable.ic_menu_tag_dark_grey,
            R.drawable.ic_menu_tag_green,
            R.drawable.ic_menu_tag_untagged
    };
    public int[] tagColors = new int[] {
            R.color.tagRed,
            R.color.tagPurple,
            R.color.tagBlue,
            R.color.tagDarkOrange,
            R.color.tagBlueGrey,
            R.color.tagBabyBlue,
            R.color.tagDarkGrey,
            R.color.tagGreen,
            R.color.tagUntagged
    };

    public TagColorListAdapter(Context context) {
        super(context, R.layout.item_tag);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        if(convertView == null) {
            View tagItem = inflater.inflate(R.layout.item_tag, parent, false);
            Drawable drawable = context.getResources().getDrawable(tagDrawables[position]);

            ImageView icon = (ImageView) tagItem.findViewById(R.id.tagIcon_iv);
            icon.setImageDrawable(drawable);

            convertView = tagItem;
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    @Override
    public int getCount() {
        return tagDrawables.length;
    }
}
