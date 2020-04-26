package pt.ulisboa.tecnico.cmov.foodist.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import pt.ulisboa.tecnico.cmov.foodist.R;

public class FullscreenPhotoActivity extends AppCompatActivity {

    private static final String BITMAP = "BITMAP";

    private static final String PHOTOID = "PHOTOID";

    private String photoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_photo);

        Bitmap bitmap = getIntent().getParcelableExtra(BITMAP);
        ImageView image = findViewById(R.id.fullscreen_menu_image);
        image.setImageBitmap(bitmap);
        this.photoId = getIntent().getStringExtra(PHOTOID);
    }
}
