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
import android.widget.CheckBox;
import android.widget.EditText;
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
import java.util.Map;

import foodist.server.grpc.contract.Contract;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;

import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.CAMPUS;

public class ProfileActivity extends BaseActivity {

    private static final int PICK_FROM_GALLERY = 1;
    private static final int PICK_FROM_CAMERA = 2;
    private static final int REQUEST_PIC = 3;
    private static final int GALLERY_PIC = 4;
    private static final int CAMERA_PIC = 5;

    private boolean editable = false;

    private static final String TAG = "TAG_ProfileActivity";

    private int checkedCount = 0;

    private String imageFilePath = null;

    private int photoView = R.id.profilePicture;

    private String campus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        drawScreen();
        setCampus();

        setUserRoleButtons();
        setLanguageButton();
        setDietButtons();

        switchEdit();

        setEditButton();
        setLoginButton();

        ImageView profilePicture = findViewById(R.id.profilePicture);

        profilePicture.setOnClickListener(v -> askGalleryPermission());
    }

    protected void setCampus() {
        this.campus = getIntent().getStringExtra(CAMPUS);
    }

    private void setEditButton() {
        Button editButton = findViewById(R.id.profile_edit_button);
        editButton.setOnClickListener(v -> {
            editable = !editable;
            switchEdit();
            if (editButton.getText().equals(getString(R.string.commit_text_edit))) {
                saveProfile();
            }
            if (editable) {
                editButton.setText(R.string.commit_text_edit);
            }
        });
    }


    private void setLoginButton() {
        Button button = findViewById(R.id.profile_login_button);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void switchEdit() {

        /*Username*/
        EditText text = findViewById(R.id.username);
        text.setEnabled(editable);

        /*Roles*/
        RadioButton button = findViewById(R.id.studentRadioButton);
        button.setClickable(editable);
        button.setEnabled(editable);

        button = findViewById(R.id.professorRadioButton);
        button.setClickable(editable);
        button.setEnabled(editable);

        button = findViewById(R.id.staffRadioButton);
        button.setClickable(editable);
        button.setEnabled(editable);

        button = findViewById(R.id.visitorRadioButton);
        button.setClickable(editable);
        button.setEnabled(editable);

        /*Food Preferences*/
        CheckBox box = findViewById(R.id.Vegetarian);
        box.setClickable(editable);
        box.setEnabled(editable);

        box = findViewById(R.id.Meat);
        box.setClickable(editable);
        box.setEnabled(editable);

        box = findViewById(R.id.Fish);
        box.setClickable(editable);
        box.setEnabled(editable);

        box = findViewById(R.id.Vegan);
        box.setClickable(editable);
        box.setEnabled(editable);

        /*Language*/
        button = findViewById(R.id.languageEnglish);
        button.setClickable(editable);
        button.setEnabled(editable);

        button = findViewById(R.id.languagePortuguese);
        button.setClickable(editable);
        button.setEnabled(editable);
    }

    public void saveProfile() {

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
        SharedPreferences.Editor editor = pref.edit();

        /*Username*/
        EditText user = findViewById(R.id.username);
        editor.putString(getString(R.string.profile_username), user.getText().toString());


        /*Roles*/
        RadioButton button = findViewById(R.id.studentRadioButton);
        if (button.isChecked()) {
            editor.putString(getString(R.string.profile_position_name), Contract.Role.Student.name());
        }

        button = findViewById(R.id.professorRadioButton);
        if (button.isChecked()) {
            editor.putString(getString(R.string.profile_position_name), Contract.Role.Professor.name());
        }

        button = findViewById(R.id.staffRadioButton);
        if (button.isChecked()) {
            editor.putString(getString(R.string.profile_position_name), Contract.Role.Staff.name());
        }

        button = findViewById(R.id.visitorRadioButton);
        if (button.isChecked()) {
            editor.putString(getString(R.string.profile_position_name), Contract.Role.Visitor.name());
        }

        /*Food Preferences*/
        CheckBox box = findViewById(R.id.Vegetarian);
        editor.putBoolean(GlobalStatus.VEGETARIAN_KEY, box.isChecked());

        box = findViewById(R.id.Meat);
        editor.putBoolean(GlobalStatus.MEAT_KEY, box.isChecked());

        box = findViewById(R.id.Fish);
        editor.putBoolean(GlobalStatus.FISH_KEY, box.isChecked());

        box = findViewById(R.id.Vegan);
        editor.putBoolean(GlobalStatus.VEGAN_KEY, box.isChecked());

        /*Language*/
        button = findViewById(R.id.languageEnglish);
        if (button.isChecked()) {
            editor.putString(getString(R.string.profile_language_chosen), "en");
        }

        button = findViewById(R.id.languagePortuguese);
        if (button.isChecked()) {
            editor.putString(getString(R.string.profile_language_chosen), "pt");
        }

        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(CAMPUS, this.campus);
        startActivity(intent);
        this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
        SharedPreferences.Editor editor = pref.edit();

        TextView user = findViewById(R.id.username);

        editor.putString(getString(R.string.profile_username), user.getText().toString());
        editor.apply();
    }


    private void drawScreen() {
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

        setUserRole();
        setUserLanguage();
        setDiets();

    }

    private void setUserRole() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
        String role = pref.getString(getString(R.string.profile_position_name), Contract.Role.Student.name());
        Contract.Role userRole = Contract.Role.valueOf(role);
        RadioButton button;
        switch (userRole) {
            case Student:
                button = findViewById(R.id.studentRadioButton);
                break;
            case Professor:
                button = findViewById(R.id.professorRadioButton);
                break;
            case Staff:
                button = findViewById(R.id.staffRadioButton);
                break;
            case Visitor:
                button = findViewById(R.id.visitorRadioButton);
                break;
            default:
                Log.e(TAG, "No user role found");
                button = findViewById(R.id.studentRadioButton);
                break;
        }
        button.toggle();
    }

    private void setUserRoleButtons() {
    }

    private void setUserLanguage() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
        String language = pref.getString(getString(R.string.profile_language_chosen), "en");

        switch (language) {
            case "en":
                RadioButton englishLanguage = findViewById(R.id.languageEnglish);
                englishLanguage.toggle();
                break;

            case "pt":
                RadioButton portugueseLanguage = findViewById(R.id.languagePortuguese);
                portugueseLanguage.toggle();
                break;
        }
    }

    private void setLanguageButton() {
    }

    private void setDietButtons() {
        final CheckBox vegBox = findViewById(R.id.Vegetarian);
        vegBox.setOnClickListener((l) ->
        {
            if (vegBox.isChecked()) {
                checkedCount++;
            } else {
                checkedCount--;
            }
            if (!vegBox.isChecked() && checkedCount == 0) {
                vegBox.setChecked(true);
                showToast(getString(R.string.check_preferece_warning));
                checkedCount++;
            }
        });

        final CheckBox meatBox = findViewById(R.id.Meat);
        meatBox.setOnClickListener((l) ->
        {
            if (meatBox.isChecked()) {
                checkedCount++;
            } else {
                checkedCount--;
            }
            if (!meatBox.isChecked() && checkedCount == 0) {
                meatBox.setChecked(true);
                showToast(getString(R.string.check_preferece_warning));
                checkedCount++;
            }
        });

        final CheckBox fishBox = findViewById(R.id.Fish);
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
                showToast(getString(R.string.check_preferece_warning));
            }
        });

        final CheckBox veganBox = findViewById(R.id.Vegan);
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
                showToast(getString(R.string.check_preferece_warning));
            }
        });
    }

    private void setDiets() {
        Map<Contract.FoodType, Boolean> constraints = getGlobalStatus().getUserConstraints();


        final CheckBox vegBox = findViewById(R.id.Vegetarian);
        vegBox.setChecked(constraints.getOrDefault(Contract.FoodType.Vegetarian, false));
        if (vegBox.isChecked()) {
            checkedCount++;
        }

        final CheckBox meatBox = findViewById(R.id.Meat);
        meatBox.setChecked(constraints.getOrDefault(Contract.FoodType.Meat, false));
        if (meatBox.isChecked()) {
            checkedCount++;
        }

        final CheckBox fishBox = findViewById(R.id.Fish);
        fishBox.setChecked(constraints.getOrDefault(Contract.FoodType.Fish, false));
        if (fishBox.isChecked()) {
            checkedCount++;
        }

        final CheckBox veganBox = findViewById(R.id.Vegan);
        veganBox.setChecked(constraints.getOrDefault(Contract.FoodType.Vegan, false));
        if (veganBox.isChecked()) {
            checkedCount++;
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
            showToast("Invalid Image Provided");
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

}

