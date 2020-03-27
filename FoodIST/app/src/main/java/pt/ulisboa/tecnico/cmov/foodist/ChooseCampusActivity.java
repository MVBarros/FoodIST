package pt.ulisboa.tecnico.cmov.foodist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
            startActivity(intent);
        });

        alamedaButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseCampusActivity.this, MainActivity.class);
            intent.putExtra(CAMPUS, getString(R.string.campus_alameda));
            startActivity(intent);
        });

    }
}
