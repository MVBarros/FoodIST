package pt.ulisboa.tecnico.cmov.foodist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getPreferences();
    }

    @Override
    protected void onPause(){
        super.onPause();

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
        SharedPreferences.Editor editor = pref.edit();

        TextView user = (TextView) findViewById(R.id.username);

        editor.putString(getString(R.string.username), user.getText().toString());

        RadioGroup status = (RadioGroup) findViewById(R.id.universityStatus);

        editor.putInt(getString(R.string.position), status.getCheckedRadioButtonId());

        editor.apply();
    }

    private void getPreferences(){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);

        ImageView profilePicture = (ImageView) findViewById(R.id.profilePicture);
        TextView user = (TextView) findViewById(R.id.username);

        String profilePicturePath = pref.getString(getString(R.string.user_photo), null);

        if(profilePicturePath != null){
            //TODO - Check if this is good enough to get photo or if absolute path needed
            Bitmap photo = BitmapFactory.decodeFile(profilePicturePath);
            profilePicture.setImageBitmap(photo);
        }

        String username = pref.getString(getString(R.string.username), null);

        if(username != null){
            user.setText(username);
        }

        int selectedStatus = pref.getInt(getString(R.string.position), -1);

        if(selectedStatus != -1){
            RadioButton status = (RadioButton) findViewById(selectedStatus);

            status.toggle();
        }
    }
}
