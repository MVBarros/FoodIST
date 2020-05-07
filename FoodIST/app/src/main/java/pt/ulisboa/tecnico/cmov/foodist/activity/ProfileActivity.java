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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import foodist.server.grpc.contract.Contract;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.profile.ChangeProfileAsyncTask;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;

import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.CAMPUS;

public class ProfileActivity extends BaseActivity {

    private static final String TAG = "TAG_ProfileActivity";

    private static final int PICK_FROM_GALLERY = 1;
    private static final int PICK_FROM_CAMERA = 2;
    private static final int REQUEST_PIC = 3;
    private static final int GALLERY_PIC = 4;
    private static final int CAMERA_PIC = 5;

    private boolean editable = false;
    private int photoView = R.id.profilePicture;
    private int checkedCount = 0;
    private String imageFilePath = null;
    private String campus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setCampus();

        switchEdit();
        setEditButton();
        setLoginButton();
        setPreferenceButtons();

        ImageView profilePicture = findViewById(R.id.profilePicture);
        profilePicture.setOnClickListener(v -> askGalleryPermission());
    }

    @Override
    public void onResume() {
        super.onResume();
        drawScreen();
    }

    protected void setCampus() {
        this.campus = getIntent().getStringExtra(CAMPUS);
    }

    public void setEditButton() {
        Button editButton = findViewById(R.id.profile_edit_button);
        editButton.setOnClickListener(v -> makeEdit());
    }

    private void makeEdit() {
        Button editButton = findViewById(R.id.profile_edit_button);
        if (!editable) {
            editable = true;
            switchEdit();
            editButton.setText(R.string.commit_text_edit);
            return;
        }
        if (!isLoggedIn()) {
            saveProfile();
            returnToMain();
            return;
        }
        if (!isNetworkAvailable()) {
            showToast(getString(R.string.edit_no_conn_message));
            return;
        }
        new ChangeProfileAsyncTask(this).execute(toContract());

    }

    private void setLoginButton() {
        Button button = findViewById(R.id.profile_login_button);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(CAMPUS, this.campus);
            startActivity(intent);
        });
    }

    private void switchEdit() {

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

        button = findViewById(R.id.researcherRadioButton);
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

        RadioGroup group = findViewById(R.id.universityStatus);
        switch (group.getCheckedRadioButtonId()) {
            case R.id.studentRadioButton:
                editor.putString(getString(R.string.shared_prefs_profile_profession), Contract.Role.Student.name());
                break;
            case R.id.professorRadioButton:
                editor.putString(getString(R.string.shared_prefs_profile_profession), Contract.Role.Professor.name());
                break;
            case R.id.staffRadioButton:
                editor.putString(getString(R.string.shared_prefs_profile_profession), Contract.Role.Staff.name());
                break;
            case R.id.visitorRadioButton:
                editor.putString(getString(R.string.shared_prefs_profile_profession), Contract.Role.Public.name());
                break;
            case R.id.researcherRadioButton:
                editor.putString(getString(R.string.shared_prefs_profile_profession), Contract.Role.Researcher.name());
                break;
        }

        CheckBox dietPreferenceBox = findViewById(R.id.Vegetarian);
        editor.putBoolean(GlobalStatus.VEGETARIAN_KEY, dietPreferenceBox.isChecked());

        dietPreferenceBox = findViewById(R.id.Meat);
        editor.putBoolean(GlobalStatus.MEAT_KEY, dietPreferenceBox.isChecked());

        dietPreferenceBox = findViewById(R.id.Fish);
        editor.putBoolean(GlobalStatus.FISH_KEY, dietPreferenceBox.isChecked());

        dietPreferenceBox = findViewById(R.id.Vegan);
        editor.putBoolean(GlobalStatus.VEGAN_KEY, dietPreferenceBox.isChecked());

        group = findViewById(R.id.languageSelector);
        switch (group.getCheckedRadioButtonId()) {
            case R.id.languageEnglish:
                editor.putString(getString(R.string.shared_prefs_profile_language), "en");
                break;
            case R.id.languagePortuguese:
                editor.putString(getString(R.string.shared_prefs_profile_language), "pt");
                break;
        }
        editor.apply();
    }

    private Contract.AccountMessage toContract() {
        Contract.Profile profile = toProfile();
        return Contract.AccountMessage.newBuilder()
                .setCookie(getGlobalStatus().getCookie())
                .setProfile(profile)
                .build();
    }

    private Contract.Profile toProfile() {

        Contract.Role role = Contract.Role.Student;

        RadioGroup group = findViewById(R.id.universityStatus);
        switch (group.getCheckedRadioButtonId()) {
            case R.id.studentRadioButton:
                role = Contract.Role.Student;
                break;
            case R.id.professorRadioButton:
                role = Contract.Role.Professor;
                break;
            case R.id.staffRadioButton:
                role = Contract.Role.Staff;
                break;
            case R.id.visitorRadioButton:
                role = Contract.Role.Public;
                break;
            case R.id.researcherRadioButton:
                role = Contract.Role.Researcher;
                break;
        }

        Map<Integer, Boolean> prefs = new HashMap<>();
        CheckBox box = findViewById(R.id.Vegetarian);
        prefs.put(Contract.FoodType.Vegetarian_VALUE, box.isChecked());

        box = findViewById(R.id.Meat);
        prefs.put(Contract.FoodType.Meat_VALUE, box.isChecked());

        box = findViewById(R.id.Fish);
        prefs.put(Contract.FoodType.Fish_VALUE, box.isChecked());

        box = findViewById(R.id.Vegan);
        prefs.put(Contract.FoodType.Vegan_VALUE, box.isChecked());

        String language = "en";
        group = findViewById(R.id.languageSelector);
        switch (group.getCheckedRadioButtonId()) {
            case R.id.languageEnglish:
                language = "en";
                break;
            case R.id.languagePortuguese:
                language = "pt";
                break;
        }

        return Contract.Profile.newBuilder()
                .setLanguage(language)
                .putAllPreferences(prefs)
                .setName(getGlobalStatus().getUsername())
                .setRole(role)
                .build();
    }

    public void returnToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(CAMPUS, this.campus);
        startActivity(intent);
        this.finish();
    }

    private void drawScreen() {
        setUsername();
        setUserRole();
        setUserLanguage();
        setPreferences();
    }

    private void setUsername() {

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
        TextView user = findViewById(R.id.username);
        String username = isLoggedIn() ? pref.getString(getString(R.string.shared_prefs_profile_username), getString(R.string.not_logged_in_text)) : getString(R.string.not_logged_in_text);
        user.setText(String.format("%s: %s", getString(R.string.username_not_logged_in), username));
    }

    private void setUserRole() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
        String role = pref.getString(getString(R.string.shared_prefs_profile_profession), Contract.Role.Student.name());
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
            case Public:
                button = findViewById(R.id.visitorRadioButton);
                break;
            case Researcher:
                button = findViewById(R.id.researcherRadioButton);
                break;
            default:
                Log.e(TAG, "No user role found");
                button = findViewById(R.id.studentRadioButton);
                break;
        }
        button.toggle();
    }

    private void setUserLanguage() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
        String language = pref.getString(getString(R.string.shared_prefs_profile_language), "en");

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

    private void setPreferenceButtons() {
        final CheckBox vegBox = findViewById(R.id.Vegetarian);
        vegBox.setOnClickListener(v -> preferenceBoxClick(vegBox));

        final CheckBox meatBox = findViewById(R.id.Meat);
        meatBox.setOnClickListener(v -> preferenceBoxClick(meatBox));

        final CheckBox fishBox = findViewById(R.id.Fish);
        fishBox.setOnClickListener(v -> preferenceBoxClick(fishBox));

        final CheckBox veganBox = findViewById(R.id.Vegan);
        veganBox.setOnClickListener(v -> preferenceBoxClick(veganBox));
    }

    private void preferenceBoxClick(CheckBox box) {
        if (box.isChecked()) {
            checkedCount++;
        } else {
            checkedCount--;
        }
        if (!box.isChecked() && checkedCount == 0) {
            box.setChecked(true);
            checkedCount++;
            showToast(getString(R.string.check_preferece_warning));
        }
    }

    public void setPreferenceBox(CheckBox box, Contract.FoodType type) {
        Map<Contract.FoodType, Boolean> constraints = getGlobalStatus().getUserConstraints();

        box.setChecked(constraints.getOrDefault(type, false));
        if (box.isChecked()) {
            checkedCount++;
        }
    }

    private void setPreferences() {
        final CheckBox vegBox = findViewById(R.id.Vegetarian);
        setPreferenceBox(vegBox, Contract.FoodType.Vegetarian);

        final CheckBox meatBox = findViewById(R.id.Meat);
        setPreferenceBox(meatBox, Contract.FoodType.Meat);

        final CheckBox fishBox = findViewById(R.id.Fish);
        setPreferenceBox(fishBox, Contract.FoodType.Fish);

        final CheckBox veganBox = findViewById(R.id.Vegan);
        setPreferenceBox(veganBox, Contract.FoodType.Vegan);
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
                chooser.putExtra(Intent.EXTRA_TITLE, getString(R.string.extra_title_message));

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
            showToast(getString(R.string.invalid_image_provided_message));
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

