package pt.ulisboa.tecnico.cmov.foodist.async;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.FoodMenuActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.base.BaseAsyncTask;
import pt.ulisboa.tecnico.cmov.foodist.domain.Photo;


public class DownloadPhotoTask extends BaseAsyncTask<Photo, Integer, String, FoodMenuActivity> {

    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;

    private String photosPath;

    public DownloadPhotoTask(FoodMenuActivity activity) {
        super(activity);
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        this.photosPath = storageDir.getAbsolutePath();
        this.stub = activity.getGlobalStatus().getStub();
    }

    private static final String TAG = "DOWNLOAD-PHOTOS-TASK";

    @Override
    protected String doInBackground(Photo... photoRequest) {
        if (photoRequest.length != 1) {
            return null;
        }
        Photo photo = photoRequest[0];
        Contract.DownloadPhotoRequest.Builder downloadPhotoBuilder = Contract.DownloadPhotoRequest.newBuilder();

        downloadPhotoBuilder.setPhotoId(photo.getPhotoID());
        downloadPhotoBuilder.setFoodService(photo.getFoodServiceName());
        downloadPhotoBuilder.setMenuName(photo.getMenuName());

        Contract.DownloadPhotoRequest downloadPhotoRequest = downloadPhotoBuilder.build();

        try {
            Iterator<Contract.DownloadPhotoReply> iterator = this.stub.downloadPhoto(downloadPhotoRequest);

            try {
                String path = assembleClientPhotoPath(photo.getPhotoID(), photo.getFoodServiceName(), photo.getMenuName());
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path));

                //Write bytes to file
                while (iterator.hasNext()) {
                    Contract.DownloadPhotoReply chunk = iterator.next();
                    byte[] fileBytes = chunk.getContent().toByteArray();
                    out.write(fileBytes);
                }
                out.close();
                return path;
            } catch (IOException ioe) {
                Log.d(TAG, "Error! Could not write file: \"" + assembleClientPhotoPath(photo.getPhotoID(), photo.getFoodServiceName(), photo.getMenuName()) + "\".");
            }
        } catch (StatusRuntimeException e) {
            return null;
        }
        return null;
    }

    @Override
    public void onPostExecute(String result) {
        if (result == null) {
            photoError(getActivity());
            return;
        }

        ImageView photoView = getActivity().findViewById(R.id.menuPhotos);

        Bitmap photo = BitmapFactory.decodeFile(result);
        photoView.setImageBitmap(photo);
    }

    private void photoError(FoodMenuActivity activity) {
        activity.showToast("Unable to download photo, check connection");
        Log.d(TAG, "Unable to download photo");
    }

    private String assembleClientPhotoPath(String photoName, String foodServiceName, String menuName) {
        //TODO - Check if path has '/' in the end
        String photoDirectory = photosPath + "/Cache/" + foodServiceName + "/" + menuName + "/";
        createPhotoDir(photoDirectory);
        return photoDirectory + photoName;
    }

    public static void createPhotoDir(String photoPath) {
        File directory = new File(photoPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

}
