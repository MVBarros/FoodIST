package pt.ulisboa.tecnico.cmov.foodist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;

import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.CAMPUS;

public class ChooseCampusActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_campus);

        Button tagusparkButton = findViewById(R.id.taguspark);
        Button alamedaButton = findViewById(R.id.alameda);
        Button ctnButton = findViewById(R.id.ctn);

        tagusparkButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseCampusActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(CAMPUS, getString(R.string.campus_taguspark));
            finish();
            startActivity(intent);
        });

        alamedaButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseCampusActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(CAMPUS, getString(R.string.campus_alameda));
            finish();
            startActivity(intent);
        });

        ctnButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseCampusActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(CAMPUS, getString(R.string.campus_ctn));
            finish();
            startActivity(intent);
        });
    }
}
