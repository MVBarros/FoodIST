package pt.ulisboa.tecnico.cmov.foodist.async;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.protobuf.Empty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.cache.PhotoCache;


public class WifiPreLoadingTask extends AsyncTask<FoodISTServerServiceBlockingStub, Integer, Boolean> {

    private static final String TAG = "WIFI-PRELOADING-TASK";

    @Override
    protected Boolean doInBackground(FoodISTServerServiceBlockingStub... stub) {
        if (stub.length != 1) {
            return null;
        }

        try {
            List<String> photoIDs = stub[0].requestPhotoIDs(Empty.newBuilder().build()).getPhotoIDList();

            for (String photoID : photoIDs) {
                if (!downloadPhoto(photoID, stub[0])) {
                    Log.d(TAG, "Thread ended - Cache is full");
                    return true;
                } else {
                    Log.d(TAG, "Photo with id " + photoID + " was downloaded");
                }
            }
            return true;
        } catch (StatusRuntimeException e) {
            Log.d(TAG, "Unable to request photos from server");
            return true;
        }

    }

    private Boolean downloadPhoto(String photoID, FoodISTServerServiceBlockingStub stub) {
        if (PhotoCache.getInstance().getPhoto(photoID) == null) {
            Contract.DownloadPhotoRequest request = Contract.DownloadPhotoRequest.newBuilder().setPhotoId(photoID).build();
            Iterator<Contract.DownloadPhotoReply> iterator = stub.downloadPhoto(request);

            try (ByteArrayOutputStream bis = new ByteArrayOutputStream()) {
                //Write bytes to file
                while (iterator.hasNext()) {
                    Contract.DownloadPhotoReply chunk = iterator.next();
                    byte[] fileBytes = chunk.getContent().toByteArray();
                    bis.write(fileBytes);
                }
                Bitmap photoBitmap = BitmapFactory.decodeByteArray(bis.toByteArray(), 0, bis.size());
                return PhotoCache.getInstance().addPhotoIfNotFull(photoID, photoBitmap);
            } catch (IOException | StatusRuntimeException ioe) {
                return true;
            }
        }
        return true;
    }
}
