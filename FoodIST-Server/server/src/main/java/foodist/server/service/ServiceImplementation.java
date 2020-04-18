package foodist.server.service;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import foodist.server.data.Storage;
import foodist.server.data.StorageException;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.Contract.AddPhotoRequest;
import foodist.server.grpc.contract.Contract.DownloadPhotoReply;
import foodist.server.grpc.contract.Contract.ListMenuReply;
import foodist.server.grpc.contract.Contract.Menu;
import foodist.server.grpc.contract.Contract.PhotoReply;
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
        menuBuilder.setType(request.getType());
        menuBuilder.setLanguage(request.getLanguage());
        String translation = requestGoogleTranslation(request.getName(), request.getLanguage(), getTargetLanguage(request.getLanguage()));
        menuBuilder.setTranslatedName(translation);
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
		for (Entry<String, Menu> entry : menuMap.entrySet()) {
			Menu menu = Storage.fetchMenuPhotos(foodService, entry.getValue());
			listMenuReplyBuilder.addMenus(menu.toBuilder().setType(entry.getValue().getType()));
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
        	Menu ret = Storage.fetchMenuPhotos(service, menu);
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
				} catch (StorageException e) {
					this.onError(e);
				}
			}
		};
    }

    @Override
    public void downloadPhoto(Contract.DownloadPhotoRequest request, StreamObserver<Contract.DownloadPhotoReply> responseObserver) {
        String photoId = request.getPhotoId();

        int sequence = 0;
        
        try {
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
		} catch (StorageException e) {
			responseObserver.onError(e);
		}       
    }
    
    @Override
    public void requestPhotoIDs(Empty request, StreamObserver<foodist.server.grpc.contract.Contract.PhotoReply> responseObserver) {
    	// fetches the oldest three photo ids 
        PhotoReply photoReply = Storage.fetchPhotoIds(3);
        responseObserver.onNext(photoReply);
        responseObserver.onCompleted();
    }

    /*******************/
	/** Aux Functions **/
	/*******************/

	public String requestGoogleTranslation(String content, String sourceLanguage, String targetLanguage){
		Translate translate = TranslateOptions.getDefaultInstance().getService();

		Translation translation = translate.translate(
				content,
				Translate.TranslateOption.sourceLanguage(sourceLanguage),
				Translate.TranslateOption.targetLanguage(targetLanguage));

		return translation.getTranslatedText();
	}

	//Para o Miguel Barros
	//Sim, isto e estupido e tenho nocao, mas estou mt enervado com isto e foi um pouco a balda, pensarei numa maneira melhor depois

	public String getTargetLanguage(String language){
		if(language.equals("en")){
			return "pt";
		}
		return "en";
	}

}
