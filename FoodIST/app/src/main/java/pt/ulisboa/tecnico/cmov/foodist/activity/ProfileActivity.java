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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import foodist.server.grpc.contract.Contract;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;

public class ProfileActivity extends BaseActivity {

    private static final int PICK_FROM_GALLERY = 1;
    private static final int PICK_FROM_CAMERA = 2;
    private static final int REQUEST_PIC = 3;
    private static final int GALLERY_PIC = 4;
    private static final int CAMERA_PIC = 5;

    private static final String TAG = "TAG_ProfileActivity";

    private int checkedCount = 0;

    private String imageFilePath = null;

    private int photoView = R.id.profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getPreferences();

        setDiets();
        setLanguage();

        ImageView profilePicture = findViewById(R.id.profilePicture);

        profilePicture.setOnClickListener(v -> askGalleryPermission());
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
        SharedPreferences.Editor editor = pref.edit();

        TextView user = findViewById(R.id.username);

        editor.putString(getString(R.string.profile_username), user.getText().toString());

        RadioGroup status = findViewById(R.id.universityStatus);

        editor.putInt(getString(R.string.profile_position), status.getCheckedRadioButtonId());

        RadioButton statusButton = findViewById(status.getCheckedRadioButtonId());
        if (statusButton != null) {
            editor.putString(getString(R.string.profile_position_name), statusButton.getText().toString());
        }

        editor.apply();
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
            showToast("Invalid Image Provided");
        }

        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this, "pt.ulisboa.tecnico.cmov.foodist.provider", photoFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        }

        return cameraIntent;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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
            SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
            SharedPreferences.Editor editor = pref.edit();

            switch (requestCode) {
                case GALLERY_PIC:

                    galleryReturn(editor, data);
                    break;
                case CAMERA_PIC:
                    Log.d(TAG, "I should not entered here");

                    cameraReturn(editor, data);
                    break;
                case REQUEST_PIC:
                    choiceReturn(editor, data);
                    break;
            }
        }
    }

    private void galleryReturn(SharedPreferences.Editor editor, Intent data) {
        Uri selectedImage = data.getData();

        String[] filePath = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(selectedImage, filePath, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePath[0]);
        String absoluteFilePath = cursor.getString(columnIndex);
        cursor.close();

        ImageView profile = findViewById(photoView);
        profile.setImageBitmap(BitmapFactory.decodeFile(absoluteFilePath));

        //Save path for future reference
        editor.putString(getString(R.string.profile_user_photo), absoluteFilePath);
        editor.apply();
    }

    private void cameraReturn(SharedPreferences.Editor editor, Intent data) {
        ImageView profilePicture = findViewById(photoView);

        Bitmap photo = BitmapFactory.decodeFile(imageFilePath);
        profilePicture.setImageBitmap(photo);

        editor.putString(getString(R.string.profile_user_photo), imageFilePath);
        editor.apply();
    }

    private void choiceReturn(SharedPreferences.Editor editor, Intent data) {
        ImageView profilePicture = findViewById(photoView);

        Bitmap photo = BitmapFactory.decodeFile(imageFilePath);

        if (photo == null) {
            galleryReturn(editor, data);
        } else {
            profilePicture.setImageBitmap(photo);

            editor.putString(getString(R.string.profile_user_photo), imageFilePath);
            editor.apply();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.d(TAG, "Photo Path: " + (imageFilePath != null));


        imageFilePath = image.getAbsolutePath();
        return image;
    }

    private void getPreferences() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);

        ImageView profilePicture = findViewById(R.id.profilePicture);
        TextView user = findViewById(R.id.username);

        this.imageFilePath = pref.getString(getString(R.string.profile_user_photo), null);

        if (this.imageFilePath != null) {
            Bitmap photo = BitmapFactory.decodeFile(this.imageFilePath);
            profilePicture.setImageBitmap(photo);
        }

        String username = pref.getString(getString(R.string.profile_username), "");
        user.setText(username);


        int selectedStatus = pref.getInt(getString(R.string.profile_position), -1);

        if (selectedStatus != -1) {
            RadioButton status = findViewById(selectedStatus);
            if (status != null) {
                status.toggle();
            }
        }

        int currentLanguage = pref.getInt(getString(R.string.language), -1);

        if (currentLanguage != -1) {
            RadioButton language = findViewById(currentLanguage);
            if (language != null) {
                language.toggle();
            }
        } else {
            getLanguage();
        }
    }

    private void getLanguage() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
        SharedPreferences.Editor prefEditor = pref.edit();

        String language = pref.getString(getString(R.string.profile_language_chosen), "en");

        switch (language) {
            case "en":
                RadioButton englishLanguage = findViewById(R.id.languageEnglish);
                prefEditor.putInt(getString(R.string.language), englishLanguage.getId());
                englishLanguage.toggle();

            case "pt":
                RadioButton portugueseLanguage = findViewById(R.id.languagePortuguese);
                prefEditor.putInt(getString(R.string.language), portugueseLanguage.getId());
                portugueseLanguage.toggle();
        }
    }

    private void setLanguage() {

        final RadioButton englishLanguage = findViewById(R.id.languageEnglish);

        englishLanguage.setOnClickListener((l) ->
        {
            SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
            SharedPreferences.Editor prefEditor = pref.edit();
            prefEditor.putInt(getString(R.string.language), englishLanguage.getId());
            prefEditor.putString(getString(R.string.profile_language_chosen), "en");
            prefEditor.apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            this.finish();
        });

        final RadioButton portugueseLanguage = findViewById(R.id.languagePortuguese);

        portugueseLanguage.setOnClickListener((l) ->
        {
            SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
            SharedPreferences.Editor prefEditor = pref.edit();
            prefEditor.putInt(getString(R.string.language), portugueseLanguage.getId());
            prefEditor.putString(getString(R.string.profile_language_chosen), "pt");
            prefEditor.apply();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            this.finish();
        });
    }

    private void setDiets() {
        Map<Contract.FoodType, Boolean> constraints = getGlobalStatus().getUserConstraints();

        final CheckBox vegBox = findViewById(R.id.Vegetarian);
        vegBox.setChecked(constraints.get(Contract.FoodType.Vegetarian));
        if (vegBox.isChecked()) {
            checkedCount++;
        }
        vegBox.setOnClickListener((l) ->
        {
            if (vegBox.isChecked()) {
                checkedCount++;
            } else {
                checkedCount--;
            }
            if (!vegBox.isChecked() && checkedCount == 0) {
                vegBox.setChecked(true);
                showToast("Must have at least one preference");
                checkedCount++;
                return;
            }
            SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
            SharedPreferences.Editor prefEditor = pref.edit();
            prefEditor.putBoolean(GlobalStatus.VEGETARIAN_KEY, vegBox.isChecked());
            prefEditor.apply();

        });

        final CheckBox meatBox = findViewById(R.id.Meat);
        meatBox.setChecked(constraints.get(Contract.FoodType.Meat));
        meatBox.setOnClickListener((l) ->
        {
            if (meatBox.isChecked()) {
                checkedCount++;
            } else {
                checkedCount--;
            }
            if (!meatBox.isChecked() && checkedCount == 0) {
                meatBox.setChecked(true);
                showToast("Must have at least one preference");
                checkedCount++;
                return;
            }
            SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
            SharedPreferences.Editor prefEditor = pref.edit();
            prefEditor.putBoolean(GlobalStatus.MEAT_KEY, meatBox.isChecked());
            prefEditor.apply();
        });

        if (meatBox.isChecked()) {
            checkedCount++;
        }

        final CheckBox fishBox = findViewById(R.id.Fish);
        fishBox.setChecked(constraints.get(Contract.FoodType.Fish));
        fishBox.setOnClickListener((l) ->
        {
            if (fishBox.isChecked()) {
                checkedCount++;
            } else {
                checkedCount--;
            }
            if (!fishBox.isChecked() && checkedCount == 0) {
                fishBox.setChecked(true);
                checkedCount++;
                showToast("Must have at least one preference");
                return;
            }
            SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
            SharedPreferences.Editor prefEditor = pref.edit();
            prefEditor.putBoolean(GlobalStatus.FISH_KEY, fishBox.isChecked());
            prefEditor.apply();
        });


        if (fishBox.isChecked()) {
            checkedCount++;
        }

        final CheckBox veganBox = findViewById(R.id.Vegan);
        veganBox.setChecked(constraints.get(Contract.FoodType.Vegan));
        veganBox.setOnClickListener((l) ->
        {

            if (veganBox.isChecked()) {
                checkedCount++;
            } else {
                checkedCount--;
            }
            if (!veganBox.isChecked() && checkedCount == 0) {
                veganBox.setChecked(true);
                checkedCount++;
                showToast("Must have at least one preference");
                return;
            }
            SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
            SharedPreferences.Editor prefEditor = pref.edit();
            prefEditor.putBoolean(GlobalStatus.VEGAN_KEY, veganBox.isChecked());
            prefEditor.apply();
        });

        if (veganBox.isChecked()) {
            checkedCount++;
        }

    }
}
