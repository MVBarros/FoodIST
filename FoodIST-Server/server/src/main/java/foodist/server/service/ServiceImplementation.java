package foodist.server.service;

import com.google.protobuf.Empty;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc.FoodISTServerServiceImplBase;
import io.grpc.stub.StreamObserver;

public class ServiceImplementation extends FoodISTServerServiceImplBase {

    @Override
    public void listMenu(Contract.ListMenuRequest request, StreamObserver<Contract.ListMenuReply> responseObserver) {
        //TODO
        super.listMenu(request, responseObserver);
    }

    @Override
    public void addMenu(Contract.AddMenuRequest request, StreamObserver<Empty> responseObserver) {
        //TODO
        super.addMenu(request, responseObserver);
    }

    @Override
    public StreamObserver<Contract.AddPhotoRequest> addPhoto(StreamObserver<Empty> responseObserver) {
        //TODO
        return super.addPhoto(responseObserver);
    }

    @Override
    public void downloadPhoto(Contract.DownloadPhotoRequest request, StreamObserver<Contract.DownloadPhotoReply> responseObserver) {
        //TODO
        super.downloadPhoto(request, responseObserver);
    }
}
