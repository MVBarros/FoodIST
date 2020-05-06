package foodist.server.service;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import foodist.server.data.Account;
import foodist.server.data.Menu;
import foodist.server.data.Photo;
import foodist.server.data.Service;
import foodist.server.data.exception.ServiceException;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.Contract.AddPhotoRequest;
import foodist.server.grpc.contract.Contract.DownloadPhotoReply;
import foodist.server.grpc.contract.Contract.ListMenuReply;
import foodist.server.grpc.contract.Contract.PhotoReply;
import foodist.server.grpc.contract.Contract.UpdateMenuReply;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc.FoodISTServerServiceImplBase;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.RandomStringUtils;

import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ServiceImplementation extends FoodISTServerServiceImplBase {

    public static final int COOKIE_SIZE = 256;
    public static final int CHUNK_SIZE = 1024 * 1024;
    public static final int NUM_PHOTOS = 3;

    private final Map<String, Account> users = new ConcurrentHashMap<>();
    private final Map<String, Account> sessions = new ConcurrentHashMap<>();

    private final Map<String, Service> services = new ConcurrentHashMap<>();
    private final Map<Long, Menu> menus = new ConcurrentHashMap<>();
    private final Map<Long, Photo> photos = new ConcurrentHashMap<>();


    @Override
    public void addMenu(Contract.AddMenuRequest request, StreamObserver<Contract.AddMenuReply> responseObserver) {
        try {
            if (!validateCookie(request.getCookie())) {
                responseObserver.onError(Status.UNAUTHENTICATED.asRuntimeException());
                return;
            }
            Account account = sessions.get(request.getCookie());
            Service service = getService(request.getFoodService());
            Menu menu = Menu.fromContract(request, account);
            service.addMenu(menu);
            menus.put(menu.getMenuId(), menu);

            Contract.AddMenuReply reply = Contract.AddMenuReply.newBuilder()
                    .setMenuId(menu.getMenuId())
                    .build();

            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (ServiceException e) {
            responseObserver.onError(Status.ALREADY_EXISTS.asRuntimeException());
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
        }
    }


    @Override
    public void listMenu(Contract.ListMenuRequest request, StreamObserver<Contract.ListMenuReply> responseObserver) {
        Service service = getService(request.getFoodService());
        List<Contract.Menu> menus = service.getContractMenus(request.getLanguage());

        ListMenuReply reply = ListMenuReply.newBuilder().addAllMenus(menus).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();

    }

    @Override
    public void updateMenu(Contract.UpdateMenuRequest request, StreamObserver<Contract.UpdateMenuReply> responseObserver) {
        Long id = request.getMenuId();
        Menu menu = menus.get(id);
        if (menu == null) {
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
            return;
        }
        
        UpdateMenuReply reply = UpdateMenuReply.newBuilder().setRating(menu.averageRating())
                .addAllPhotoID(menu.getPhotos())
                .build();
	
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Contract.AddPhotoRequest> addPhoto(StreamObserver<Contract.UploadPhotoReply> responseObserver) {
        return new StreamObserver<>() {
            private int counter = 0;
            private String cookie;
            private ByteString photoByteString = ByteString.copyFrom(new byte[0]);
            private long menuId;
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
                        menuId = value.getMenuId();
                        cookie = value.getCookie();
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
                    if (!validateCookie(cookie)) {
                        responseObserver.onError(Status.UNAUTHENTICATED.asRuntimeException());
                        return;
                    }
                    Account account = sessions.get(cookie);
                    Photo photo = new Photo(photoByteString.toByteArray(), account);
                    Menu menu = menus.get(menuId);
                    if (menu == null) {
                        responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
                        return;
                    }
                    photos.put(photo.getPhotoId(), photo);
                    menu.addPhoto(photo);

                    responseObserver.onNext(Contract.UploadPhotoReply.newBuilder()
                            .setPhotoID(String.valueOf(photo.getPhotoId()))
                            .build());
                    responseObserver.onCompleted();

                } catch (IllegalArgumentException e) {
                    responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
                }
            }
        };
    }

    @Override
    public void downloadPhoto(Contract.DownloadPhotoRequest request, StreamObserver<Contract.DownloadPhotoReply> responseObserver) {
        try {
            Photo photo = photos.get(Long.parseLong(request.getPhotoId()));
            if (photo == null) {
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
                return;
            }
            //Send file 1MB chunk at a time
            byte[] photoBytes = photo.getContent();
            for (int i = 0, seq = 0; i < photoBytes.length; i += CHUNK_SIZE, seq++) {
                byte[] chunk = Arrays.copyOfRange(photoBytes, i, i + CHUNK_SIZE);

                DownloadPhotoReply reply = DownloadPhotoReply.newBuilder()
                        .setContent(ByteString.copyFrom(chunk))
                        .setSequenceNumber(seq)
                        .build();

                responseObserver.onNext(reply);
            }

            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
        }
    }

    @Override
    public void flagPhoto(Contract.FlagRequest request, StreamObserver<Empty> responseObserver) {
        if (!validateCookie(request.getCookie())) {
            responseObserver.onError(Status.UNAUTHENTICATED.asRuntimeException());
            return;
        }
        Account account = sessions.get(request.getCookie());
        long photoId = request.getPhotoId();
        Photo photo = photos.get(photoId);
        if (photo == null) {
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
            return;
        }
        photo.flag(account.getUsername());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void flagMenu(Contract.FlagMenuRequest request, StreamObserver<Empty> responseObserver) {
        if (!validateCookie(request.getCookie())) {
            responseObserver.onError(Status.UNAUTHENTICATED.asRuntimeException());
            return;
        }
        Account account = sessions.get(request.getCookie());
        long menuId = request.getMenuId();
        Menu menu = menus.get(menuId);
        if (menu == null) {
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
            return;
        }
        menu.flag(account.getUsername());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    /**
     * Fetches the best 3 photoIds of each Menu
     */
    @Override
    public void requestPhotoIDs(Empty request, StreamObserver<foodist.server.grpc.contract.Contract.PhotoReply> responseObserver) {
        List<String> photoIds = menus.values()
                .stream()
                .map(menu -> menu.getPhotos(NUM_PHOTOS))
                .flatMap(List::stream)
                .collect(Collectors.toList());

        PhotoReply reply = PhotoReply.newBuilder().addAllPhotoID(photoIds).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void register(Contract.RegisterRequest request, StreamObserver<Contract.AccountMessage> responseObserver) {
        try {
            Account account = Account.fromContract(request.getProfile(), request.getPassword());

            var curr = users.putIfAbsent(account.getUsername(), account);
            if (curr != null) {
                responseObserver.onError(Status.ALREADY_EXISTS.asRuntimeException());
                return;
            }

            String cookie = generateRandomCookie();
            sessions.put(cookie, account);

            responseObserver.onNext(account.toReply(cookie));
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
        }
    }

    @Override
    public void login(Contract.LoginRequest request, StreamObserver<Contract.AccountMessage> responseObserver) {
        try {
            String username = request.getUsername();
            String password = request.getPassword();
            Account account = users.get(username);
            if (account == null) {
                responseObserver.onError(Status.UNAUTHENTICATED.asRuntimeException());
                return;
            }
            if (!account.checkPassword(password)) {
                responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
                return;
            }
            String cookie = generateRandomCookie();
            sessions.put(cookie, account);

            responseObserver.onNext(account.toReply(cookie));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
        }
    }

    @Override
    public void changeProfile(Contract.AccountMessage request, StreamObserver<Empty> responseObserver) {
        try {
            if (!validateCookie(request.getCookie())) {
                responseObserver.onError(Status.UNAUTHENTICATED.asRuntimeException());
                return;
            }
            Account account = sessions.get(request.getCookie());
            Account newAccount = Account.fromContract(request.getProfile(), account.getPassword(), account.getSalt());

            sessions.put(request.getCookie(), newAccount);
            users.put(account.getUsername(), newAccount);

            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
        }
    }

    @Override
    public void logout(Contract.LogoutRequest request, StreamObserver<Empty> responseObserver) {
        if (!validateCookie(request.getCookie())) {
            responseObserver.onError(Status.UNAUTHENTICATED.asRuntimeException());
            return;
        }
        sessions.remove(request.getCookie());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }
    
    @Override
    public void uploadRating(Contract.RatingRequest request, StreamObserver<com.google.protobuf.Empty> responseObserver) {
    	if (!validateCookie(request.getCookie())) {
            responseObserver.onError(Status.UNAUTHENTICATED.asRuntimeException());
            return;
        }
    	else {
    		long id = request.getMenuId();
            Menu menu = menus.get(id);
            if (menu == null) {
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
                return;
            }        
            
            menu.addRating(request.getUsername(), request.getRating());
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
    	}    	
    }

    @Override
    public void addToQueue(Contract.QueueRequest request, StreamObserver<Empty> responseObserver) {
        Service service = services.computeIfAbsent(request.getFoodService(), Service::new);
        service.addToQueue(request.getUuid());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }
    
    @Override
    public void cancelQueueJoin(Contract.QueueRequest request, StreamObserver<Empty> responseObserver) {
        Service service = services.computeIfAbsent(request.getFoodService(), Service::new);
        service.cancelQueueJoin(request.getUuid());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void removeFromQueue(Contract.QueueRequest request, StreamObserver<Empty> responseObserver) {
        Service service = services.computeIfAbsent(request.getFoodService(), Service::new);
        service.removeFromQueue(request.getUuid());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void getQueueTime(Contract.QueueTimeRequest request, StreamObserver<Contract.QueueTimeResponse> responseObserver) {
        Contract.QueueTimeResponse.Builder builder = Contract.QueueTimeResponse.newBuilder();
        request.getFoodServiceList().stream()
                .map(this::getService)
                .forEach(service -> {
                    String waitTime = service.currentQueueWaitTime();
                    if (waitTime != null) {
                        builder.putQueueTime(service.getName(), waitTime);
                    }
                });
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    private String generateRandomCookie() {
        return RandomStringUtils.random(COOKIE_SIZE);
    }

    private boolean validateCookie(String cookie) {
        return sessions.containsKey(cookie);
    }

    public Map<String, Account> getUsers() {
        return users;
    }

    public Map<String, Account> getSessions() {
        return sessions;
    }

    public Map<Long, Menu> getMenus() {
        return menus;
    }

    public Service getService(String name) {
        return services.computeIfAbsent(name, Service::new);
    }

    public Map<Long, Photo> getPhotos() {
        return photos;
    }

    public void cleanup() {
        services.values().forEach(Service::resetMenus);
        menus.clear();
        photos.clear();
    }
}
