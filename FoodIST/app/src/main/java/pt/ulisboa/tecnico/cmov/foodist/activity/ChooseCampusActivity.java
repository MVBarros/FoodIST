package pt.ulisboa.tecnico.cmov.foodist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;

public class ChooseCampusActivity extends BaseActivity {
    public static final String CAMPUS = "pt.ulisboa.tecnico.cmov.foodlist.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_campus);

        Button tagusparkButton = findViewById(R.id.taguspark);
        Button alamedaButton = findViewById(R.id.alameda);

        tagusparkButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseCampusActivity.this, MainActivity.class);
            intent.putExtra(CAMPUS, getString(R.string.campus_taguspark));
            finish();
            startActivity(intent);
        });

        alamedaButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseCampusActivity.this, MainActivity.class);
            intent.putExtra(CAMPUS, getString(R.string.campus_alameda));
            finish();
            startActivity(intent);
        });

    }
}
