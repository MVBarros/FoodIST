package pt.ulisboa.tecnico.cmov.foodist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import foodist.server.grpc.contract.Contract;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.profile.LoginAsyncTask;
import pt.ulisboa.tecnico.cmov.foodist.async.profile.RegisterAsyncTask;

import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.CAMPUS;

public class LoginActivity extends BaseActivity {

    private String campus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setCampus();
        setButtons();
    }

    private void setButtons() {
        Button button = findViewById(R.id.login_button);
        button.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                doLogin();
            } else {
                showToast(getString(R.string.cannot_login_network_message));

            }
        });
        button = findViewById(R.id.register_button);
        button.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                doRegister();
            } else {
                showToast(getString(R.string.register_no_connection_message));
            }
        });
    }

    public void doLogin() {

        TextView text = findViewById(R.id.login_username);
        String username = text.getText().toString();

        text = findViewById(R.id.login_password);
        String password = text.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            showToast(getString(R.string.non_empty_login_params_message));
            return;
        }
        Contract.LoginRequest request = Contract.LoginRequest.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build();

        new LoginAsyncTask(this).execute(request);
    }

    public void doRegister() {

        TextView text = findViewById(R.id.login_username);
        String username = text.getText().toString();

        text = findViewById(R.id.login_password);
        String password = text.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
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
    }

    protected void setCampus() {
        this.campus = getIntent().getStringExtra(CAMPUS);
    }


    public void returnToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(CAMPUS, this.campus);
        startActivity(intent);
        this.finish();
    }
}
