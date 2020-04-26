package pt.ulisboa.tecnico.cmov.foodist.activity.boot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.MainActivity;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;

public class ChooseLanguageActivity extends BaseActivity {
    public static final String CAMPUS = "pt.ulisboa.tecnico.cmov.foodlist.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_language);

        Button englishButton = findViewById(R.id.english);
        Button portugueseButton = findViewById(R.id.portuguese);

        englishButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseLanguageActivity.this, MainActivity.class);
            finish();
            setLanguage("en");
            startActivity(intent);
        });

        portugueseButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseLanguageActivity.this, MainActivity.class);
            finish();
            setLanguage("pt");
            startActivity(intent);
        });
    }

    private void setLanguage(String language) {
        SharedPreferences pref = getSharedPreferences(getString(R.string.profile_file), 0);
        SharedPreferences.Editor prefEditor = pref.edit();
        prefEditor.putString(getString(R.string.profile_language_chosen), language);
        prefEditor.putBoolean(getString(R.string.language_first_boot), false);
        prefEditor.apply();
    }
}
