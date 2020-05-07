package pt.ulisboa.tecnico.cmov.foodist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import foodist.server.grpc.contract.Contract;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.profile.LoginAsyncTask;
import pt.ulisboa.tecnico.cmov.foodist.async.profile.LogoutAsyncTask;
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

    public void drawScreen() {
        EditText text = findViewById(R.id.login_username);
        text.setEnabled(!isLoggedIn());
        if (isLoggedIn()) {
            text.setText(getGlobalStatus().getUsername());
        }

        text = findViewById(R.id.login_password);
        text.setEnabled(!isLoggedIn());

        Button button = findViewById(R.id.login_button);

        if (isLoggedIn()) {
            button.setText(R.string.logout_button_message);
        } else {
            button.setText(R.string.login);
        }

        button = findViewById(R.id.register_button);
        button.setEnabled(!isLoggedIn());

    }

    public void setButtons() {
        drawScreen();
        Button button = findViewById(R.id.login_button);
        button.setOnClickListener(v -> loginButton());

        button = findViewById(R.id.register_button);
        button.setOnClickListener(v -> doRegister());
    }

    public void loginButton() {
        if (!isLoggedIn()) {
            doLogin();
        } else {
            doLogout();
        }
    }

    public void doLogin() {
        if (!isNetworkAvailable()) {
            showToast(getString(R.string.cannot_login_network_message));
            return;
        }
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

        new LoginAsyncTask(this).executeOnExecutor(getGlobalStatus().getExecutor(), request);
    }

    public void doLogout() {
        if (!isNetworkAvailable()) {
            showToast(getString(R.string.logout_no_connection_message));
            return;
        }

        new LogoutAsyncTask(this).executeOnExecutor(getGlobalStatus().getExecutor(), getGlobalStatus().getCookie());
    }

    public void doRegister() {
        if (!isNetworkAvailable()) {
            showToast(getString(R.string.register_no_connection_message));
            return;
        }
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

        new RegisterAsyncTask(this).executeOnExecutor(getGlobalStatus().getExecutor(), request);
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
