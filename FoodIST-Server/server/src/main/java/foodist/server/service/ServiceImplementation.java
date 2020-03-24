package foodist.server.service;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.Contract.AddPhotoRequest;
import foodist.server.grpc.contract.Contract.ListMenuReply;
import foodist.server.grpc.contract.Contract.Menu;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc.FoodISTServerServiceImplBase;
import foodist.server.util.MenuUtils;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServiceImplementation extends FoodISTServerServiceImplBase {
	
	private HashMap<String, List<Menu>> menusHashMap = new HashMap<String, List<Menu>>();
	
    @Override
    public void addMenu(Contract.AddMenuRequest request, StreamObserver<Empty> responseObserver) {
    	Menu.Builder menuBuilder = Menu.newBuilder();
    	
	    String foodService = request.getFoodService();                     
	    menuBuilder.setName(request.getName());
	    menuBuilder.setPrice(request.getPrice());
	    Menu menu = menuBuilder.build();
	        
	    System.out.println(request.getName() + ":" + request.getPrice());
	    List<Menu> menuList = this.menusHashMap.get(foodService);
	      
	    if(menuList!=null) {
	    	menuList.add(menu);
	    	this.menusHashMap.put(foodService, menuList);         
	    } 
	    else {
	    	List<Menu> new_MenuList = new ArrayList<Menu>();
	    	new_MenuList.add(menu);
	    	this.menusHashMap.put(foodService, new_MenuList);         
	    } 
	      
	    responseObserver.onNext(null);
	    responseObserver.onCompleted();      
    }  
    
    @Override
    public void listMenu(Contract.ListMenuRequest request, StreamObserver<Contract.ListMenuReply> responseObserver) {
		String foodService = request.getFoodService();                
		
		List<Menu> menuList = menusHashMap.get(foodService);
		    
		ListMenuReply.Builder listMenuReplyBuilder = ListMenuReply.newBuilder();
		    
		for(Menu m : menuList) {
			System.out.println("#%" + m.getName());
			listMenuReplyBuilder.addMenus(m);        	
		}
		    
		ListMenuReply listMenuReply = listMenuReplyBuilder.build();        
		responseObserver.onNext(listMenuReply);
		responseObserver.onCompleted();  
    } 

    @Override
	public StreamObserver<Contract.AddPhotoRequest> addPhoto(StreamObserver<Empty> responseObserver) {
    	return new StreamObserver<Contract.AddPhotoRequest>() {    		
    		private int counter = 0;
    		private ByteString photoByteString = ByteString.copyFrom(new byte[]{});
    		private String menuName;
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
    					menuName = value.getName();
    					foodService = value.getFoodService();
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
    				MenuUtils.addPhotoToMenu(foodService, menuName, photoByteString, menusHashMap);
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
