package pt.ulisboa.tecnico.cmov.foodist.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.LoginActivity;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;

import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.CAMPUS;

public class LoginDialog extends DialogFragment {

    public String campus;
    public BaseActivity activity;

    public LoginDialog(BaseActivity activity, String campus) {
        super();
        this.activity = activity;
        this.campus = campus;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.must_login_message)
                .setPositiveButton(getString(R.string.login_dialog_positive_message), (dialog, id) -> {
                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.putExtra(CAMPUS, this.campus);
                    activity.startActivity(intent);
                })
                .setNegativeButton(R.string.login_dialog_negative_message, (dialog, id) -> {
                    // User cancelled the dialog
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
