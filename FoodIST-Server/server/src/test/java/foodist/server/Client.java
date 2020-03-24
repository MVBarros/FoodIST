package foodist.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import foodist.server.grpc.contract.Contract.AddMenuRequest;
import foodist.server.grpc.contract.Contract.ListMenuReply;
import foodist.server.grpc.contract.Contract.ListMenuRequest;
import foodist.server.grpc.contract.Contract.Menu;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;


class Client {
	
	private FoodISTServerServiceBlockingStub stub;
	private ManagedChannel channel;
	
	Client(ManagedChannel channel) {
		
		this.channel = channel;
		this.stub = FoodISTServerServiceGrpc.newBlockingStub(this.channel);
	}  
  
	void addMenu(String foodService, String name, double price) {	
		
		System.out.println(foodService);
		AddMenuRequest.Builder addMenuBuilder = AddMenuRequest.newBuilder();					
	  
		addMenuBuilder.setFoodService(foodService);
		addMenuBuilder.setName(name);
		addMenuBuilder.setPrice(price);					
	  
		AddMenuRequest addMenuRequestExample = addMenuBuilder.build();
		
		this.stub.addMenu(addMenuRequestExample);     
	}
  
	void listMenu(String foodService) {	
		
		ListMenuRequest listMenuRequest = ListMenuRequest.newBuilder().setFoodService(foodService).build();
	  		
		ListMenuReply listMenuReply = this.stub.listMenu(listMenuRequest);    
		List<Menu> list = listMenuReply.getMenusList();
	  
		for(Menu m : list) {
			System.out.println(m.getName());
		}
	}
  
	void addPhoto(String photoName, String photoFoodService, String photoPath) {
		
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
            	addPhotoRequestBuilder.setName(photoName);
            	addPhotoRequestBuilder.setSequenceNumber(sequence);
            	addPhotoRequestBuilder.setFoodService(photoFoodService);
            	
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
	
}