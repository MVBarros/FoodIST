package pt.ulisboa.tecnico.cmov.foodist.status;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import org.conscrypt.Conscrypt;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.x500.X500Principal;

import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;

public class GlobalStatus extends Application {

    public enum DietConstraint {Meat, Vegetarian, Vegan, Fish}

    public static final String MEAT_KEY = "MEAT";
    public static final String VEGAN_KEY = "VEGAN";
    public static final String FISH_KEY = "FISH";
    public static final String VEGETARIAN_KEY = "VEGETARIAN";

    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub = null;
    private FoodISTServerServiceGrpc.FoodISTServerServiceStub assyncStub = null;

    private List<FoodService> services = Collections.synchronizedList(new ArrayList<>());

    private boolean freshBootFlag = true;

    public FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub getStub() {
        try {
            if (stub == null) {

                Provider provider = Conscrypt.newProvider();
                Security.insertProviderAt(provider, 1);
                SSLSocketFactory factory = newSslSocketFactoryForCa(provider);
                String address = getString(R.string.foodist_server_address);
                int port = Integer.parseInt(getString(R.string.foodist_server_port));
                ManagedChannel channel = OkHttpChannelBuilder.forAddress(address, port)
                        .sslSocketFactory(factory)
                        .build();
                stub = FoodISTServerServiceGrpc.newBlockingStub(channel);
                assyncStub = FoodISTServerServiceGrpc.newStub(channel);
            }
            return stub;
        } catch (Exception e) {
            Log.e("SSL-SOCKET-FACTORY", e.getMessage());
            return null;
        }
    }

    public FoodISTServerServiceGrpc.FoodISTServerServiceStub getAssyncStub() {
        try {
            if (assyncStub == null) {
                Provider provider = Conscrypt.newProvider();
                Security.insertProviderAt(provider, 1);
                SSLSocketFactory factory = newSslSocketFactoryForCa(provider);
                String address = getString(R.string.foodist_server_address);
                int port = Integer.parseInt(getString(R.string.foodist_server_port));
                ManagedChannel channel = OkHttpChannelBuilder.forAddress(address, port)
                        .sslSocketFactory(factory)
                        .build();
                assyncStub = FoodISTServerServiceGrpc.newStub(channel);
                stub = FoodISTServerServiceGrpc.newBlockingStub(channel);
            }
            return assyncStub;
        } catch (Exception e) {
            Log.e("SSL-SOCKET-FACTORY", e.getMessage());
            return null;
        }
    }

    public List<FoodService> getServices() {
        return services;
    }

    public void setServices(List<FoodService> services) {
        this.services = Collections.synchronizedList(services);
    }

    public String getApiKey() {
        return getString(R.string.map_api_key);
    }

    public boolean isFreshBootFlag() {
        return freshBootFlag;
    }

    public void setFreshBootFlag(boolean freshBootFlag) {
        this.freshBootFlag = freshBootFlag;
    }

    public InputStream getServerCert() {

        return getResources().openRawResource(R.raw.ca);
    }

    public SSLSocketFactory newSslSocketFactoryForCa(Provider provider) throws Exception {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(
                new BufferedInputStream(getServerCert()));
        X500Principal principal = cert.getSubjectX500Principal();
        ks.setCertificateEntry(principal.getName("RFC2253"), cert);

        // Set up trust manager factory to use our key store.
        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(ks);
        SSLContext context = SSLContext.getInstance("TLS", provider);
        context.init(null, trustManagerFactory.getTrustManagers(), null);
        return context.getSocketFactory();
    }

    public Map<DietConstraint, Boolean> getUserConstraints() {
        Map<DietConstraint, Boolean> userConstraints = new HashMap<>();

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);

        boolean vegetarian = pref.getBoolean(VEGETARIAN_KEY, true);
        userConstraints.put(DietConstraint.Vegetarian, vegetarian);

        boolean meat = pref.getBoolean(MEAT_KEY, true);
        userConstraints.put(DietConstraint.Meat, meat);

        boolean vegan = pref.getBoolean(VEGAN_KEY, true);
        userConstraints.put(DietConstraint.Vegan, vegan);

        boolean fish = pref.getBoolean(FISH_KEY, true);
        userConstraints.put(DietConstraint.Fish, fish);

        return userConstraints;
    }


    public void switchConstraint(DietConstraint constraint) {
        getUserConstraints().put(constraint, getUserConstraints().get(constraint));
    }

}
