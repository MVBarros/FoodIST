package pt.ulisboa.tecnico.cmov.foodist.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import pt.ulisboa.tecnico.cmov.foodist.FoodMenuActivity;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.domain.Menu;

public class MenuAdapter extends ArrayAdapter<Menu> {
    public static final String NUMBER_PHOTOS = "Number_photos";
    public static final String MENU_NAME = "Menu_name";
    public static final String MENU_PRICE = "Menu_price";
    public static final String MENU_SERVICE = "Menu_service";

    public MenuAdapter(Context context, ArrayList<Menu> menus){
        super(context, 0, menus);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Menu menu = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.food_menu, parent, false);
        }

        TextView menuFood = convertView.findViewById(R.id.menuFood);
        TextView menuCost = convertView.findViewById(R.id.menuCost);

        menuFood.setText(menu.getMenuName());
        menuCost.setText(String.format(Locale.US, "%.2f", menu.getPrice()));

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(getContext(), FoodMenuActivity.class);
               intent.putExtra(NUMBER_PHOTOS, menu.getPhotoIdCount());
               intent.putExtra(MENU_NAME, menu.getMenuName());
               intent.putExtra(MENU_PRICE, menu.getPrice());
               intent.putExtra(MENU_SERVICE, menu.getFoodServiceName());
               getContext().startActivity(intent);

            }
        });
        return convertView;
    }

}
