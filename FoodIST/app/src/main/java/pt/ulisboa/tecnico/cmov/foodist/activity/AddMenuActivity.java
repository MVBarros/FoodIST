package pt.ulisboa.tecnico.cmov.foodist.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import foodist.server.grpc.contract.Contract;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.UploadMenuTask;
import pt.ulisboa.tecnico.cmov.foodist.async.UploadPhotoTask;
import pt.ulisboa.tecnico.cmov.foodist.domain.Menu;
import pt.ulisboa.tecnico.cmov.foodist.domain.Photo;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;


public class AddMenuActivity extends BaseActivity {

    private static final int PICK_FROM_GALLERY = 1;
    private static final int PICK_FROM_CAMERA = 2;
    private static final int REQUEST_PIC = 3;
    private static final int GALLERY_PIC = 4;
    private static final int CAMERA_PIC = 5;

    private static final String TAG = "TAG_AddMenuActivity";

    private String imageFilePath = null;

    private int photoView = R.id.dishView;

    private String SERVICE_NAME = "Service Name";

    private String initialMenuName = "";
    private String initialPrice = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_menu);

        setButtons();
    }

    private void setButtons() {

        Button b = findViewById(R.id.add_new_menu_done_button);
        b.setOnClickListener(v -> {
            TextView menuName = findViewById(R.id.dishName);
            TextView menuCost = findViewById(R.id.dishCost);

            Contract.FoodType type = getFoodType();
            if (type == null) {
                showToast("Give the menu a type!");
                return;
            }

            String foodService = getIntent().getStringExtra(SERVICE_NAME);

            if (menuName.getText().toString().equals(initialMenuName) || menuCost.getText().toString().equals(initialPrice)) {
                showToast(getString(R.string.add_menu_invalid_input_toast));
            } else {
                Log.d(TAG, String.format("Menu %s was added", menuName.getText().toString()));
                SharedPreferences pref = getSharedPreferences(getString(R.string.profile_file), 0);

                Menu menu = new Menu(foodService, menuName.getText().toString(), Double.parseDouble(menuCost.getText().toString()), type, pref.getString(getString(R.string.profile_language_chosen), "en"), "");
                uploadMenu(menu);
            }
        });
        ImageView photoButton = findViewById(R.id.dishView);
        photoButton.setOnClickListener(v -> askGalleryPermission());
    }

    private void uploadMenu(Menu menu) {
        if (isNetworkAvailable()) {
            new UploadMenuTask(((GlobalStatus) AddMenuActivity.this.getApplicationContext()).getStub()).execute(menu);
            if (imageFilePath != null) {
                Photo photo = new Photo(menu.getFoodServiceName(), menu.getMenuName(), imageFilePath);
                new UploadPhotoTask(((GlobalStatus) AddMenuActivity.this.getApplicationContext()).getAssyncStub(), this).execute(photo);
            }
            finish();
        } else {
            showToast(getString(R.string.add_menu_no_internet_access_toast));
        }
    }


    private void askGalleryPermission() {
        int galleryPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (galleryPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
        } else {
            askCameraPermission();
        }
    }

    private void askCameraPermission() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_CAMERA);
        } else {
            cameraOrGalleryChooser();
        }
    }

    private void cameraOrGalleryChooser() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent galleryintent = new Intent(Intent.ACTION_PICK);
            galleryintent.setType("image/*");

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                Intent chooser = new Intent(Intent.ACTION_CHOOSER);
                chooser.putExtra(Intent.EXTRA_INTENT, galleryintent);
                chooser.putExtra(Intent.EXTRA_TITLE, "Select from:");

                Intent[] intentArray = {createCameraIntent()};
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooser, REQUEST_PIC);
            } else {
                startActivityForResult(galleryintent, GALLERY_PIC);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                startActivityForResult(createCameraIntent(), CAMERA_PIC);

            }
        }
    }

    public Intent createCameraIntent() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            showToast(getString(R.string.add_menu_camera_failure_toast));
        }

        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this, "pt.ulisboa.tecnico.cmov.foodist.provider", photoFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        }

        return cameraIntent;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PICK_FROM_GALLERY:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "phone gallery permission granted");
                }
                askCameraPermission();
                break;

            case PICK_FROM_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "phone camera permission granted");
                }
                cameraOrGalleryChooser();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case GALLERY_PIC:

                    galleryReturn(data);
                    break;
                case CAMERA_PIC:
                    Log.d(TAG, "I should not entered here");

                    cameraReturn();
                    break;
                case REQUEST_PIC:
                    choiceReturn(data);
                    break;
            }
        }
    }

    private void galleryReturn(Intent data) {
        Uri selectedImage = data.getData();

        String[] filePath = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(selectedImage, filePath, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePath[0]);
        imageFilePath = cursor.getString(columnIndex);
        cursor.close();

        ImageView profile = (ImageView) findViewById(photoView);
        profile.setImageBitmap(BitmapFactory.decodeFile(imageFilePath));
    }

    private void cameraReturn() {
        ImageView profilePicture = (ImageView) findViewById(photoView);

        Bitmap photo = BitmapFactory.decodeFile(imageFilePath);
        profilePicture.setImageBitmap(photo);
    }

    private void choiceReturn(Intent data) {
        ImageView profilePicture = findViewById(photoView);

        Bitmap photo = BitmapFactory.decodeFile(imageFilePath);

        if (photo == null) {
            galleryReturn(data);
        } else {
            profilePicture.setImageBitmap(photo);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        imageFilePath = image.getAbsolutePath();
        return image;
    }

    private Contract.FoodType getFoodType() {
        RadioButton button = findViewById(R.id.add_menu_vegetarian);
        if (button.isChecked()) {
            return Contract.FoodType.Vegetarian;
        }
        button = findViewById(R.id.add_menu_meat);
        if (button.isChecked()) {
            return Contract.FoodType.Meat;
        }
        button = findViewById(R.id.add_menu_fish);
        if (button.isChecked()) {
            return Contract.FoodType.Fish;
        }
        button = findViewById(R.id.add_menu_vegan);
        if (button.isChecked()) {
            return Contract.FoodType.Vegan;
        }
        return null;
    }
}
