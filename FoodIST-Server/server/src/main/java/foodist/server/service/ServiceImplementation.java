package foodist.server.service;

import java.util.HashMap;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.Contract.AddPhotoRequest;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc.FoodISTServerServiceImplBase;
import foodist.server.util.Menu;
import foodist.server.util.PhotoBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class ServiceImplementation extends FoodISTServerServiceImplBase {
	
	private HashMap<String, Menu> menusHashMap = new HashMap<String, Menu>();
	
    @Override
    public void listMenu(Contract.ListMenuRequest request, StreamObserver<Contract.ListMenuReply> responseObserver) {
        //TODO
        super.listMenu(request, responseObserver);
    }

    @Override
    public void addMenu(Contract.AddMenuRequest request, StreamObserver<Empty> responseObserver) {
        String foodService = request.getFoodService();
        
        String menuName = request.getName();
        double menuPrice = request.getPrice();
        
        Menu addedMenu = new Menu(menuName, menuPrice);    
        
        this.menusHashMap.put(foodService, addedMenu);       
    }    

    @Override
    public StreamObserver<Contract.AddPhotoRequest> addPhoto(StreamObserver<Empty> responseObserver) {
    	return new StreamObserver<Contract.AddPhotoRequest>() {    		
            private int counter = 0;
            private ByteString file = ByteString.copyFrom(new byte[]{});
            private String name;
            private String foodService;
            private final Object lock = new Object();

    		
			@Override
			public void onNext(AddPhotoRequest value) {
                //Synchronize onNext calls by sequence
                synchronized (lock) {
                    while (counter != value.getSequenceNumber()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            //Should never happen
                        }
                    }
                    //Renew Lease
                    if (counter == 0) {
                        name = value.getName();
                        foodService = value.getFoodService();
                    }
                    file = file.concat(value.getContent());
                    counter++;
                    lock.notify();
                }				
			}

			@Override
			public void onError(Throwable t) {
				responseObserver.onError(t);				
			}

			@Override
			public void onCompleted() {
                try {
                    responseObserver.onNext(Empty.newBuilder().build());
                    PhotoBuilder.store(foodService, name);
                    responseObserver.onCompleted();
                } catch (StatusRuntimeException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
			}  
    	};
    }

    @Override
    public void downloadPhoto(Contract.DownloadPhotoRequest request, StreamObserver<Contract.DownloadPhotoReply> responseObserver) {
        //TODO
        super.downloadPhoto(request, responseObserver);
    }
    
}
