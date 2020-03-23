package foodist.server.service;

import java.util.HashMap;

import com.google.protobuf.Empty;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc.FoodISTServerServiceImplBase;
import foodist.server.util.Menu;
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
        return super.addPhoto(responseObserver);
    }

    @Override
    public void downloadPhoto(Contract.DownloadPhotoRequest request, StreamObserver<Contract.DownloadPhotoReply> responseObserver) {
        //TODO
        super.downloadPhoto(request, responseObserver);
    }
    
}
