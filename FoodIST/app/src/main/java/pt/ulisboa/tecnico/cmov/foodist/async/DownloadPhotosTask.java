package pt.ulisboa.tecnico.cmov.foodist.async;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.FoodMenuActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.base.BaseAsyncTask;
import pt.ulisboa.tecnico.cmov.foodist.cache.PhotoCache;
import pt.ulisboa.tecnico.cmov.foodist.data.BitmapAndId;
import pt.ulisboa.tecnico.cmov.foodist.domain.Photo;


public class DownloadPhotosTask extends BaseAsyncTask<Photo, BitmapAndId, Boolean, FoodMenuActivity> {

    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;


    public DownloadPhotosTask(FoodMenuActivity activity) {
        super(activity);

        this.stub = activity.getGlobalStatus().getStub();
    }

    private static final String TAG = "DOWNLOAD-PHOTOS-TASK";

    @Override
    protected Boolean doInBackground(Photo... photoRequest) {
        for (Photo photo : photoRequest) {
            Bitmap cachedPhoto = PhotoCache.getInstance().getPhoto(photo.getPhotoID());
            if (cachedPhoto != null) {
                publishProgress(new BitmapAndId(cachedPhoto, photo.getPhotoID()));
                continue;
            }

            Contract.DownloadPhotoRequest.Builder downloadPhotoBuilder = Contract.DownloadPhotoRequest.newBuilder();

            downloadPhotoBuilder.setPhotoId(photo.getPhotoID());

            Contract.DownloadPhotoRequest downloadPhotoRequest = downloadPhotoBuilder.build();

            Iterator<Contract.DownloadPhotoReply> iterator = this.stub.downloadPhoto(downloadPhotoRequest);

            try (ByteArrayOutputStream bis = new ByteArrayOutputStream()) {
                //Write bytes to file
                while (iterator.hasNext()) {
                    Contract.DownloadPhotoReply chunk = iterator.next();
                    byte[] fileBytes = chunk.getContent().toByteArray();
                    bis.write(fileBytes);
                }
                Bitmap photoBitmap = BitmapFactory.decodeByteArray(bis.toByteArray(), 0, bis.size());
                PhotoCache.getInstance().addAndGetPhoto(photo.getPhotoID(), photoBitmap);
                publishProgress(new BitmapAndId(photoBitmap, photo.getPhotoID()));
            } catch (IOException | StatusRuntimeException ioe) {
                return null;
            }
        }
        return true;
    }

    @Override
    public void onPostExecute(Boolean result) {
        if (result == null) {
            photoError(getActivity());
        }
    }

    @Override
    protected void onProgressUpdate(BitmapAndId... values) {
        getActivity().addPhoto(values[0].bitmap, values[0].photoId);
    }

    private void photoError(FoodMenuActivity activity) {
        activity.showToast(activity.getString(R.string.download_photo_task_error_message));
        Log.d(TAG, "Unable to download photo");
    }


}

