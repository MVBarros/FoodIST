package pt.ulisboa.tecnico.cmov.foodist.async;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

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
import pt.ulisboa.tecnico.cmov.foodist.domain.Photo;


public class DownloadPhotoTask extends BaseAsyncTask<Photo, Integer, Bitmap, FoodMenuActivity> {

    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;


    public DownloadPhotoTask(FoodMenuActivity activity) {
        super(activity);

        this.stub = activity.getGlobalStatus().getStub();
    }

    private static final String TAG = "DOWNLOAD-PHOTOS-TASK";

    @Override
    protected Bitmap doInBackground(Photo... photoRequest) {
        if (photoRequest.length != 1) {
            return null;
        }
        Photo photo = photoRequest[0];


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
            return PhotoCache.getInstance().addAndGetPhoto(photo.getPhotoID(), photoBitmap);
        } catch (IOException | StatusRuntimeException ioe) {
            return null;
        }
    }

    @Override
    public void onPostExecute(Bitmap result) {
        if (result == null) {
            photoError(getActivity());
            return;
        }

        ImageView photoView = getActivity().findViewById(R.id.menuPhotos);
        photoView.setImageBitmap(result);
    }

    private void photoError(FoodMenuActivity activity) {
        activity.showToast(activity.getString(R.string.download_photo_task_error_message));
        Log.d(TAG, "Unable to download photo");
    }
}
