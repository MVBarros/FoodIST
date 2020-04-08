package pt.ulisboa.tecnico.cmov.foodist.threads;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.cache.PhotoCache;

public class WifiPreloadingThread implements Runnable {
    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;
    private List<String> photoIDs;
    private final static String TAG = "WIFI-PRELOADING-THREAD";

    public WifiPreloadingThread(FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub, List<String> photoIDs){
        this.stub = stub;
        this.photoIDs = photoIDs;
    }

    @Override
    public void run() {
        for(String photoID : this.photoIDs){
            if(Thread.interrupted()){
                Log.d(TAG, "Thread interrupted");
                return;
            }

            if(!downloadPhoto(photoID, this.stub)){
                Log.d(TAG, "Thread ended - Cache is full");
                return;
            }
        }
    }

    private Boolean downloadPhoto(String photoID, FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub){
        if(PhotoCache.getInstance().getPhoto(photoID) == null){
            Contract.DownloadPhotoRequest request = Contract.DownloadPhotoRequest.newBuilder().setPhotoId(photoID).build();
            Iterator<Contract.DownloadPhotoReply> iterator = stub.downloadPhoto(request);

            try(ByteArrayOutputStream bis = new ByteArrayOutputStream()) {
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
