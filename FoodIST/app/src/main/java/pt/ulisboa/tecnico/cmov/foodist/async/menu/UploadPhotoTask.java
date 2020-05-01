package pt.ulisboa.tecnico.cmov.foodist.async.menu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.protobuf.ByteString;

import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.AddMenuActivity;
import pt.ulisboa.tecnico.cmov.foodist.activity.FoodMenuActivity;
import pt.ulisboa.tecnico.cmov.foodist.cache.PhotoCache;
import pt.ulisboa.tecnico.cmov.foodist.domain.Photo;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;


public class UploadPhotoTask extends AsyncTask<Photo, Integer, Boolean> {


    private FoodISTServerServiceGrpc.FoodISTServerServiceStub stub;
    private WeakReference<FoodMenuActivity> foodMenuActivity;
    private WeakReference<AddMenuActivity> addMenuActivity;
    private GlobalStatus mContext;
    private String cookie;
    private String photoId;
    private byte[] bitmap;

    private static final String TAG = "UPLOADPHOTO-TASK";


    public UploadPhotoTask(FoodMenuActivity activity) {
        this.stub = activity.getGlobalStatus().getAsyncStub();
        this.foodMenuActivity = new WeakReference<>(activity);
        this.addMenuActivity = new WeakReference<>(null);
        mContext = activity.getGlobalStatus();
        this.cookie = mContext.getCookie();
        this.bitmap = new byte[0];
    }

    public UploadPhotoTask(AddMenuActivity activity) {
        this.stub = activity.getGlobalStatus().getAsyncStub();
        this.foodMenuActivity = new WeakReference<>(null);
        this.addMenuActivity = new WeakReference<>(activity);
        mContext = activity.getGlobalStatus();
        this.cookie = mContext.getCookie();
        this.bitmap = new byte[0];
    }

    @Override
    protected Boolean doInBackground(Photo... photo) {
        if (photo.length != 1) {
            return false;
        }
        final CountDownLatch finishLatch = new CountDownLatch(1);
        int sequence = 0;

        StreamObserver<Contract.UploadPhotoReply> responseObserver = new StreamObserver<Contract.UploadPhotoReply>() {
            @Override
            public void onNext(Contract.UploadPhotoReply reply) {
                photoId = reply.getPhotoID();
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "Error uploading file, does that file already exist?" + throwable.getMessage());
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                Log.d(TAG, "File uploaded successfully");
                finishLatch.countDown();
            }
        };

        StreamObserver<Contract.AddPhotoRequest> requestObserver = stub.addPhoto(responseObserver);

        byte[] data = new byte[1024 * 1024];

        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(photo[0].getPhotoPath()))) {
            int numRead;

            //Send file chunks to server
            while ((numRead = in.read(data)) >= 0) {
                Contract.AddPhotoRequest.Builder addPhotoRequestBuilder = Contract.AddPhotoRequest.newBuilder();

                byte[] content = Arrays.copyOfRange(data, 0, numRead);
                addPhotoRequestBuilder.setContent(ByteString.copyFrom(content));
                addPhotoRequestBuilder.setMenuId(Long.parseLong(photo[0].getMenuId()));
                addPhotoRequestBuilder.setSequenceNumber(sequence);
                addPhotoRequestBuilder.setCookie(cookie);

                requestObserver.onNext(addPhotoRequestBuilder.build());
                sequence++;
                //ArrayUtils.a(bitmap, Arrays.copyOfRange(data, 0, numRead));
                bitmap = ArrayUtils.addAll(bitmap, content);
            }

            requestObserver.onCompleted();

            //Wait for server to finish saving file to Database
            finishLatch.await();

            //Add recently uploaded photo to cache
            Bitmap photoBitmap = BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length);
            PhotoCache.getInstance().addAndGetPhoto(photoId, photoBitmap);

            return true;
        } catch (FileNotFoundException e) {
            Log.d(TAG, String.format("File with filename: %s not found.", photo[0].getPhotoPath()));
            return false;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }


    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Log.d(TAG, "Photo uploaded successfully");
        } else {
            Log.d(TAG, "Photo unable to be uploaded");
        }
        FoodMenuActivity foodMenuAct = foodMenuActivity.get();
        if (foodMenuAct != null && !foodMenuAct.isFinishing() && !foodMenuAct.isDestroyed()) {
            foodMenuAct.updatePhoto(photoId);
            foodMenuAct.launchGetCachePhotosTask();
            foodMenuAct.showToast(foodMenuAct.getString(R.string.upload_photo_task_complete_message));
            return;
        }
        AddMenuActivity addMenuAct = addMenuActivity.get();
        if (addMenuAct != null && !addMenuAct.isFinishing() && !addMenuAct.isDestroyed()) {
            addMenuAct.showToast(addMenuAct.getString(R.string.Menu_uploaded_successfully_message));
            addMenuAct.finish();
        }
    }
}
