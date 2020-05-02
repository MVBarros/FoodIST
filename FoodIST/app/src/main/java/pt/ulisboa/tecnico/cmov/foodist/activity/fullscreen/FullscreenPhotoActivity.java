package pt.ulisboa.tecnico.cmov.foodist.activity.fullscreen;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.menu.FlagPhotoTask;
import pt.ulisboa.tecnico.cmov.foodist.dialog.LoginDialog;

import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.PHOTO_ID;

public class FullscreenPhotoActivity extends BaseActivity {


    public static Bitmap photo; //Workaround for not being able to pass bitmaps to activities (> 1 Mb)

    private String photoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_photo);

        ImageView image = findViewById(R.id.fullscreen_menu_image);
        image.setImageBitmap(photo);
        this.photoId = getIntent().getStringExtra(PHOTO_ID);
    }

    @Override
    protected void onResume() {
        setFlagButton();
        setButtonClickable();
        super.onResume();
    }

    public void setFlagButton() {
        Button button = findViewById(R.id.flag_photo_button);
        button.setOnClickListener(v -> {
            if (!isLoggedIn()) {
                new LoginDialog(this, getGlobalStatus().getCampus()).show(getSupportFragmentManager(), "login");
                return;
            }
            if (!isNetworkAvailable()) {
                showToast(getString(R.string.no_internet_conn_flag_photo_error));
                return;
            }
            new FlagPhotoTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, photoId);
        });
    }

    public void setButtonClickable() {
        if (isLoggedIn()) {
            Button button = findViewById(R.id.flag_photo_button);
            button.setEnabled(!getGlobalStatus().isFlagged(photoId));
        }
    }
}
