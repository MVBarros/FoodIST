package pt.ulisboa.tecnico.cmov.foodist.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Rating;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;
import pt.ulisboa.tecnico.cmov.foodist.activity.fullscreen.FullscreenPhotoActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.base.CancelableTask;
import pt.ulisboa.tecnico.cmov.foodist.async.base.SafePostTask;
import pt.ulisboa.tecnico.cmov.foodist.async.menu.DownloadPhotosTask;
import pt.ulisboa.tecnico.cmov.foodist.async.menu.FlagMenuTask;
import pt.ulisboa.tecnico.cmov.foodist.async.menu.UpdateMenuInfoTask;
import pt.ulisboa.tecnico.cmov.foodist.async.menu.UploadPhotoTask;
import pt.ulisboa.tecnico.cmov.foodist.async.menu.UploadRatingTask;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.MenuNetworkReceiver;
import pt.ulisboa.tecnico.cmov.foodist.cache.PhotoCache;
import pt.ulisboa.tecnico.cmov.foodist.dialog.LoginDialog;
import pt.ulisboa.tecnico.cmov.foodist.domain.Photo;

import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.DISPLAY_NAME;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.MENU_ID;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.MENU_PHOTO_IDS;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.MENU_PRICE;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.MENU_RATING;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.PHOTO_ID;

public class FoodMenuActivity extends BaseActivity {

    //Camera/Gallery tags
    private static final int PICK_FROM_GALLERY = 1;
    private static final int PICK_FROM_CAMERA = 2;
    private static final int REQUEST_PIC = 3;
    private static final int GALLERY_PIC = 4;
    private static final int CAMERA_PIC = 5;

    private static final int MARGIN_RIGHT = 30;
    private static final int MARGIN_LEFT = 0;
    private static final int MARGIN_TOP = 0;
    private static final int MARGIN_BOTTOM = 0;
    private static final int PHOTO_WIDTH = 800;
    private static final int PHOTO_DOWNLOAD_LIMIT = 3;
    private static final String TAG = "TAG_FoodMenuActivity";

    private String imageFilePath = null;
    private String menuId;

    /**
     * These two sets form a set data structure whose removed elements cannot be added back
     */
    private Set<String> photosNotDownloaded = Collections.synchronizedSet(new LinkedHashSet<>()); //Keep insertion order
    private Set<String> photosDownloaded = Collections.synchronizedSet(new HashSet<>());

    private boolean isOnCreate;

    private boolean downloading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_menu);

        setScroll();
        intentInitialization(getIntent());
        setButtons();

        isOnCreate = true;
    }

    public void setScroll() {
        HorizontalScrollView scrollView = findViewById(R.id.food_menu_photos_scroll);
        scrollView.setOnScrollChangeListener((a, b, c, d, e) -> {
            checkScrollLimit(scrollView);
        });
    }

    public void checkScrollLimit(HorizontalScrollView scrollView) {
        if (!scrollView.canScrollHorizontally(1)) {
            //Reached limit
            if (!downloading) {
                launchDownloadPhotosTask(PHOTO_DOWNLOAD_LIMIT);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Update menu ids when screen returns
        setFlagClickable();
        launchUpdateMenuTask();
    }

    @Override
    public void addReceivers() {
        addReceiver(new MenuNetworkReceiver(this), ConnectivityManager.CONNECTIVITY_ACTION, WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

    public boolean launchGetCachePhotosTask() {
        Photo[] photos = photosNotDownloaded.stream()
                .filter(photo -> PhotoCache.getInstance().containsPhoto(photo))
                .map(photoId -> new Photo(this.menuId, null, photoId))
                .toArray(Photo[]::new);

        if (photos.length == 0) {
            return false;
        }

        //Prevent downloading the same photo twice
        Arrays.stream(photos)
                .map(Photo::getPhotoID)
                .forEach(this::acceptDownload);

        new CancelableTask<>(new SafePostTask<>(new DownloadPhotosTask(this))).executeOnExecutor(getGlobalStatus().getExecutor(), photos);
        return true;
    }


    public void launchDownloadPhotosTask(int limit) {
        //Prioritize cached photos
        if (launchGetCachePhotosTask()) {
            return;
        }


        Photo[] photos = photosNotDownloaded.stream()
                .limit(limit)
                .map(photoId -> new Photo(this.menuId, null, photoId))
                .toArray(Photo[]::new);

        if (photos.length == 0) {
            return;
        }

        if (!isNetworkAvailable()) {
            showToast(getString(R.string.food_menu_update_menu_failure_toast));
            return;
        }

        //Prevent downloading the same photo twice
        Arrays.stream(photos)
                .map(Photo::getPhotoID)
                .forEach(this::acceptDownload);

        new CancelableTask<>(new SafePostTask<>(new DownloadPhotosTask(this))).executeOnExecutor(getGlobalStatus().getExecutor(), photos);
    }

    public void addPhoto(Bitmap bitmap, String photoId) {
        //Is a new photo (Just to be safe)
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(PHOTO_WIDTH,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(MARGIN_LEFT, MARGIN_TOP, MARGIN_RIGHT, MARGIN_BOTTOM);
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        imageView.setLayoutParams(params);
        imageView.setOnClickListener(v -> {
                    Intent intent = new Intent(this, FullscreenPhotoActivity.class);
                    intent.putExtra(PHOTO_ID, photoId);
                    FullscreenPhotoActivity.photo = bitmap;
                    startActivity(intent);
                }
        );

        LinearLayout layout = findViewById(R.id.food_menu_photo_layout);
        layout.addView(imageView);

        TextView text = findViewById(R.id.photoNumber);
        text.setText("");
    }

    protected void setButtons() {
        Button addPhoto = findViewById(R.id.add_photo_button);
        addPhoto.setOnClickListener(v -> {
            loginConfirmation();
            // precisavas disto a seguir senao da para fazer upload na mesma
            if (isLoggedIn()) {
                askGalleryPermission();
            }
        });

        Button flagMenuButton = findViewById(R.id.flag_menu_button);
        flagMenuButton.setOnClickListener(v -> {
            if (!isLoggedIn()) {
                new LoginDialog(this, getGlobalStatus().getCampus()).show(getSupportFragmentManager(), "login");
                return;
            }
            if (!isNetworkAvailable()) {
                showToast(getString(R.string.flag_menu_no_conn_erro_message));
                return;
            }
            new FlagMenuTask(this).executeOnExecutor(getGlobalStatus().getExecutor(), menuId);
        });

        RatingBar ratingBar = findViewById(R.id.userStarRating);
        ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
            if(fromUser) {
                loginConfirmation();
                if(isLoggedIn()) {
                    launchAddRatingTask(rating);
                }

            }
        });
    }

    public void setFlagClickable() {
        if (isLoggedIn()) {
            Button button = findViewById(R.id.flag_menu_button);
            button.setEnabled(!getGlobalStatus().isMenuFlagged(menuId));
        }
    }

    public void updatePhotos(Collection<String> photos) {
        boolean shouldDownload = photosNotDownloaded.size() == 0;
        photos.removeAll(photosDownloaded);
        photosNotDownloaded.addAll(photos);
        shouldDownload = shouldDownload && photosNotDownloaded.size() != 0;
        if (shouldDownload) {
            //If there were no previous photos displayed and there are some now download some
            launchDownloadPhotosTask(PHOTO_DOWNLOAD_LIMIT);
        }
    }

    public void updatePhoto(String photoId) {
        if (photosDownloaded.contains(photoId)) {
            return;
        }
        photosNotDownloaded.add(photoId);
    }



    private void intentInitialization(Intent intent) {
        initializeMenuId(intent.getStringExtra(MENU_ID));
        initializeMenuCost(intent.getDoubleExtra(MENU_PRICE, -1.0));
        initializeDisplayName(intent.getStringExtra(DISPLAY_NAME));
        initializePhotoIds(intent.getStringArrayListExtra(MENU_PHOTO_IDS));
        initializeRatings(intent.getStringExtra(MENU_ID));
    }

    private void initializeMenuId(String menuId) {
        if (menuId == null) {
            showToast(getString(R.string.food_menu_name_failure_toast));
        } else {
            this.menuId = menuId;
        }
    }

    private void initializeDisplayName(String menuName) {
        if (menuName == null) {
            showToast(getString(R.string.food_menu_name_failure_toast));
        } else {
            TextView menuNameText = findViewById(R.id.menuName);
            menuNameText.setText(menuName);
        }
    }

    private void initializeMenuCost(Double menuCost) {
        if (menuCost == -1.0) {
            showToast(getString(R.string.food_menu_cost_failure_toast));
        } else {
            TextView menuCostText = findViewById(R.id.menuCost);
            menuCostText.setText(String.format(Locale.US, "%.2f", menuCost));
        }
    }

    private void initializePhotoIds(List<String> photoIds) {
        if (photoIds == null) {
            Log.d(TAG, "No menus from food service activity");
            launchUpdateMenuTask();
            return;
        }
        updatePhotos(photoIds);
    }

    private void initializeRatings(String menuId) {
        RatingBar ratingBar = findViewById(R.id.userStarRating);
        ratingBar.setRating(getGlobalStatus().getRating(menuId));
    }

    public void launchUpdateMenuTask() {
        if (!isNetworkAvailable()) {
            showToast(getString(R.string.food_menu_update_menu_failure_toast));
            return;
        }
        new CancelableTask<>(new SafePostTask<>(new UpdateMenuInfoTask(this))).executeOnExecutor(getGlobalStatus().getExecutor(), menuId);
    }

    private void launchUploadPhotoTask(Photo photo) {
        if (!isNetworkAvailable()) {
            showToast(getString(R.string.food_menu_photo_upload_no_internet_failure_toast));
            return;
        }
        new UploadPhotoTask(this).executeOnExecutor(getGlobalStatus().getExecutor(), photo);
    }


    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
    }

    private void acceptDownload(String photo) {
        photosDownloaded.add(photo);
        photosNotDownloaded.remove(photo);
    }

    private void launchAddRatingTask(double rating) {
        if (!isNetworkAvailable()) {
            showToast(getString(R.string.food_menu_rating_upload_no_internet_failure_toast));
            return;
        }
       new UploadRatingTask(this).executeOnExecutor(getGlobalStatus().getExecutor(), rating);
    }

    private void choiceReturn(SharedPreferences.Editor editor, Intent data) {
        Bitmap tryingPhoto = BitmapFactory.decodeFile(imageFilePath);

        if (tryingPhoto == null) {
            galleryReturn(editor, data);
        } else {
            Photo photo = new Photo(this.menuId, this.imageFilePath);
            launchUploadPhotoTask(photo);
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
    private void loginConfirmation() {
        if (!isLoggedIn()) {
            new LoginDialog(this, getGlobalStatus().getCampus()).show(getSupportFragmentManager(), "login");
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
            Intent galleryIntent = new Intent(Intent.ACTION_PICK);
            galleryIntent.setType("image/*");

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                Intent chooser = new Intent(Intent.ACTION_CHOOSER);
                chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent);
                chooser.putExtra(Intent.EXTRA_TITLE, "Select from:");

                Intent[] intentArray = {createCameraIntent()};
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooser, REQUEST_PIC);
            } else {
                startActivityForResult(galleryIntent, GALLERY_PIC);
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
            showToast(getString(R.string.food_menu_camera_failure_toast));
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

        Photo photo = new Photo(this.menuId, absoluteFilePath);
        launchUploadPhotoTask(photo);
    }

    private void cameraReturn(SharedPreferences.Editor editor, Intent data) {
        Photo photo = new Photo(this.menuId, this.imageFilePath);
        launchUploadPhotoTask(photo);
    }

    public String getMenuId() {
        return this.menuId;
    }
}
