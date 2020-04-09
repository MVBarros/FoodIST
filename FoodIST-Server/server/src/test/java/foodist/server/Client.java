package foodist.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;

import foodist.server.data.Storage;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import foodist.server.grpc.contract.Contract.AddMenuRequest;
import foodist.server.grpc.contract.Contract.DownloadPhotoReply;
import foodist.server.grpc.contract.Contract.DownloadPhotoRequest;
import foodist.server.grpc.contract.Contract.ListMenuReply;
import foodist.server.grpc.contract.Contract.ListMenuRequest;
import foodist.server.grpc.contract.Contract.Menu;
import foodist.server.grpc.contract.Contract.PhotoReply;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;


class Client {
	
	private static final String CLIENT_FOLDER = "photos/client/";
	private FoodISTServerServiceBlockingStub stub;
	private ManagedChannel channel;	
	
	Client(ManagedChannel channel) {
		
		this.channel = channel;
		this.stub = FoodISTServerServiceGrpc.newBlockingStub(this.channel);
	}  
  
	void addMenu(String foodService, String menuName, double price) {	
		AddMenuRequest.Builder addMenuBuilder = AddMenuRequest.newBuilder();					
	  
		addMenuBuilder.setFoodService(foodService);
		addMenuBuilder.setName(menuName);
		addMenuBuilder.setPrice(price);				
	  
		AddMenuRequest addMenuRequestExample = addMenuBuilder.build();
		
		this.stub.addMenu(addMenuRequestExample);     
	}
  
	ListMenuReply listMenu(String foodService) {	
		
		ListMenuRequest listMenuRequest = ListMenuRequest.newBuilder().setFoodService(foodService).build();
	  		
		ListMenuReply listMenuReply = this.stub.listMenu(listMenuRequest);    
		List<Menu> list = listMenuReply.getMenusList();
	  
		for(Menu m : list) {
			// This is just an example of what you might do we listMenu 
			// Download photos from menus
			for(int i = 0; i<m.getPhotoIdCount(); i++) {
				this.downloadPhoto(m.getPhotoId(i));
			}			
		}
		
		return listMenuReply;
	}
  
	void addPhoto(String foodService, String menuName, String photoPath) {
		
		final CountDownLatch finishLatch = new CountDownLatch(1);
        int sequence = 0;
        
        StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
        	@Override
        	public void onNext(Empty empty) {
        	}

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error uploading file, does that file already exist?" + throwable.getMessage());
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("File uploaded successfully");
                finishLatch.countDown();
            }
        };
        
        FoodISTServerServiceGrpc.FoodISTServerServiceStub foodISTServerServiceStub = FoodISTServerServiceGrpc.newStub(channel);			        
        
        StreamObserver<Contract.AddPhotoRequest> requestObserver = foodISTServerServiceStub.addPhoto(responseObserver);
        
        byte[] data = new byte[1024 * 1024];

        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(photoPath))) {

        	int numRead;
            
        	//Send file chunks to server
            while ((numRead = in.read(data)) >= 0) {
            	Contract.AddPhotoRequest.Builder addPhotoRequestBuilder = Contract.AddPhotoRequest.newBuilder();
                
            	addPhotoRequestBuilder.setContent(ByteString.copyFrom(Arrays.copyOfRange(data, 0, numRead)));
            	addPhotoRequestBuilder.setMenuName(menuName);
            	addPhotoRequestBuilder.setSequenceNumber(sequence);
            	addPhotoRequestBuilder.setFoodService(foodService);
            	addPhotoRequestBuilder.setPhotoName(Storage.getFileFromPath(photoPath));
            	
            	Storage.getFileFromPath(photoPath);
            	
                requestObserver.onNext(addPhotoRequestBuilder.build());
                sequence++;
            }

            requestObserver.onCompleted();

            //Wait for server to finish saving file to Database
            finishLatch.await();

        } catch (FileNotFoundException e) {
            System.out.println(String.format("File with filename: %s not found.", photoPath));
        } catch (IOException | InterruptedException e) {
            //Should Never Happen
            System.exit(1);
        }
	}
	
	void downloadPhoto(String photoId) {
		DownloadPhotoRequest.Builder downloadPhotoBuilder = DownloadPhotoRequest.newBuilder();					
		
		downloadPhotoBuilder.setPhotoId(photoId);							
		
		DownloadPhotoRequest downloadPhotoRequest = downloadPhotoBuilder.build(); 		       		
		
		Iterator<DownloadPhotoReply> iterator = this.stub.downloadPhoto(downloadPhotoRequest);   
		
			
		try {
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(assembleClientPhotoPath(photoId)));
			
	        //Write bytes to file		
	        while (iterator.hasNext()) {
	            Contract.DownloadPhotoReply chunk = iterator.next();
	            byte[] fileBytes = chunk.getContent().toByteArray();
	            out.write(fileBytes);	            
	        }
	        out.close();
		} catch(IOException ioe) {
			System.out.println("Error! Could not write file: \"" + assembleClientPhotoPath(photoId) + "\".");
		}		
	}
	
	void requestPhotoIds() {
		PhotoReply photoReply = this.stub.requestPhotoIDs(Empty.newBuilder().build());
		List<String> list = photoReply.getPhotoIDList();
		
		for(String photoId : list) {
			// This is just an example of what you might do with requestPhotoIds; 
			// Download photos with that Id from menus
			this.downloadPhoto(photoId);
		}
	}
	
	private String assembleClientPhotoPath(String photoName) {
		String photoDirectory = CLIENT_FOLDER + "/";
		Storage.createPhotoDir(photoDirectory);
		return photoDirectory + photoName;
	}
	
}