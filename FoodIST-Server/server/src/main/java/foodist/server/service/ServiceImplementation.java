package foodist.server.service;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import foodist.server.data.Storage;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.Contract.AddPhotoRequest;
import foodist.server.grpc.contract.Contract.DownloadPhotoReply;
import foodist.server.grpc.contract.Contract.ListMenuReply;
import foodist.server.grpc.contract.Contract.Menu;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc.FoodISTServerServiceImplBase;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class ServiceImplementation extends FoodISTServerServiceImplBase {

    @Override
    public void addMenu(Contract.AddMenuRequest request, StreamObserver<Empty> responseObserver) {
        Menu.Builder menuBuilder = Menu.newBuilder();

        String foodService = request.getFoodService();
        menuBuilder.setName(request.getName());
        menuBuilder.setPrice(request.getPrice());
        Menu menu = menuBuilder.build();

        Storage.addMenu(foodService, menu);

        responseObserver.onNext(null);
        responseObserver.onCompleted();
    }

    @Override
    public void listMenu(Contract.ListMenuRequest request, StreamObserver<Contract.ListMenuReply> responseObserver) {
        String foodService = request.getFoodService();

        HashMap<String, Menu> menuMap = Storage.getMenuMap(foodService);
        
        ListMenuReply.Builder listMenuReplyBuilder = ListMenuReply.newBuilder();
	    Iterator<Entry<String, Menu>> iterator = menuMap.entrySet().iterator();
	    while (iterator.hasNext()) {
	        Entry<String, Menu> entry = iterator.next();
	        Menu menu = Storage.fetchMenuPhotos(foodService, entry.getKey(), entry.getValue().getPrice());
	        listMenuReplyBuilder.addMenus(menu);
	    }
	
        ListMenuReply listMenuReply = listMenuReplyBuilder.build();
        responseObserver.onNext(listMenuReply);
        responseObserver.onCompleted();
    }

    @Override
    public void updateMenu(Contract.UpdateMenuRequest request, StreamObserver<Menu> responseObserver) {
        String service = request.getFoodService();
        String name = request.getMenuName();

        HashMap<String, Menu> menus = Storage.getMenuMap(service);
        
        Menu menu = menus.get(name);
        
        if(menu!=null) {
        	Menu ret = Storage.fetchMenuPhotos(service, name, menu.getPrice());
        	responseObserver.onNext(ret);
            responseObserver.onCompleted();
        }
        else {
        	responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("No such menu").asRuntimeException());
        }
    }

    @Override
    public StreamObserver<Contract.AddPhotoRequest> addPhoto(StreamObserver<Empty> responseObserver) {
        return new StreamObserver<>() {
			private int counter = 0;
			private ByteString photoByteString = ByteString.copyFrom(new byte[]{});
			private String menuName;
			private String foodService;
			private String photoName;
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
						menuName = value.getMenuName();
						foodService = value.getFoodService();
						photoName = value.getPhotoName();
					}
					photoByteString = photoByteString.concat(value.getContent());
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
					Storage.addPhotoToMenu(photoName, foodService, menuName, photoByteString);
					responseObserver.onCompleted();
				} catch (StatusRuntimeException e) {
					throw new IllegalArgumentException(e.getMessage());
				}
			}
		};
    }

    @Override
    public void downloadPhoto(Contract.DownloadPhotoRequest request, StreamObserver<Contract.DownloadPhotoReply> responseObserver) {
        String photoId = request.getPhotoId();

        int sequence = 0;

        byte[] photo = Storage.fetchPhotoBytes(photoId);
        
        //Send file 1MB chunk at a time
        for (int i = 0; i < photo.length; i += 1024 * 1024, sequence++) {
            int chunkSize = Math.min(1024 * 1024, photo.length - i);
            DownloadPhotoReply.Builder downloadPhotoReplyBuilder = Contract.DownloadPhotoReply.newBuilder();
            downloadPhotoReplyBuilder.setContent(ByteString.copyFrom(Arrays.copyOfRange(photo, i, i + chunkSize)));
            downloadPhotoReplyBuilder.setSequenceNumber(sequence);
            responseObserver.onNext(downloadPhotoReplyBuilder.build());
        }
        responseObserver.onCompleted();
    }

}
