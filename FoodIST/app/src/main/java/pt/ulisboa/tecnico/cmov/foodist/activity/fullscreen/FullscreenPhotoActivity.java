package pt.ulisboa.tecnico.cmov.foodist.activity.fullscreen;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import pt.ulisboa.tecnico.cmov.foodist.R;

public class FullscreenPhotoActivity extends AppCompatActivity {

    private static final String BITMAP = "BITMAP";

    private static final String PHOTOID = "PHOTOID";

    public static Bitmap photo; //Workaround for not being able to pass bitmaps to activities (> 1 Mb)

    private String photoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_photo);

        ImageView image = findViewById(R.id.fullscreen_menu_image);
        image.setImageBitmap(photo);
        this.photoId = getIntent().getStringExtra(PHOTOID);
    }
}
