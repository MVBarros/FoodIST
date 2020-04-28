package pt.ulisboa.tecnico.cmov.foodist.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import foodist.server.grpc.contract.Contract;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.profile.RegisterAsyncTask;

public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setButtons();
    }

    private void setButtons() {
        Button button = findViewById(R.id.login_button);
        button.setOnClickListener(v -> {

        });
        button = findViewById(R.id.register_button);
        button.setOnClickListener(v -> {

            TextView text = findViewById(R.id.login_username);
            String username = text.getText().toString();

            text = findViewById(R.id.login_password);
            String password = text.getText().toString();

            if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
                showToast(getString(R.string.non_empty_login_params_message));
                return;
            }

            Contract.Profile profile = getGlobalStatus().loadProfile()
                    .toBuilder()
                    .setName(username)
                    .build();
            Contract.RegisterRequest request = Contract.RegisterRequest.newBuilder()
                    .setProfile(profile)
                    .setPassword(password)
                    .build();

            new RegisterAsyncTask(this).execute(request);
        });
    }
}
