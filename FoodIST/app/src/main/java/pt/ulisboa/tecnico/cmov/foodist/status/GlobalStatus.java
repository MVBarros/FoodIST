package pt.ulisboa.tecnico.cmov.foodist.status;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import org.conscrypt.Conscrypt;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.x500.X500Principal;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.SimWifiP2pBroadcastReceiver;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;

public class GlobalStatus extends Application {

    private Executor executor;

    public static final String MEAT_KEY = Contract.FoodType.Meat.name();
    public static final String VEGAN_KEY = Contract.FoodType.Vegan.name();
    public static final String FISH_KEY = Contract.FoodType.Fish.name();
    public static final String VEGETARIAN_KEY = Contract.FoodType.Vegetarian.name();
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub = null;
    private FoodISTServerServiceGrpc.FoodISTServerServiceStub asyncStub = null;
    private Map<String, FoodService> services = new ConcurrentHashMap<>();
    private String campus;

    /**Termite*/
    private SimWifiP2pBroadcastReceiver mReceiver;
    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private static Messenger mService = null;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(GlobalStatus.this, getMainLooper(), null);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mManager = null;
            mChannel = null;
        }
    };

    public void setBroadcastReceiver(List<FoodService> foodServices) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiver(this, foodServices);
        registerReceiver(mReceiver, filter);
        Intent intent = new Intent(this, SimWifiP2pService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

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
                asyncStub = FoodISTServerServiceGrpc.newStub(channel);
            }
            return stub;
        } catch (Exception e) {
            Log.e("SSL-SOCKET-FACTORY", e.getMessage());
            return null;
        }
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getCampus() {
        return this.campus;
    }

    public FoodISTServerServiceGrpc.FoodISTServerServiceStub getAsyncStub() {
        try {
            if (asyncStub == null) {
                Provider provider = Conscrypt.newProvider();
                Security.insertProviderAt(provider, 1);
                SSLSocketFactory factory = newSslSocketFactoryForCa(provider);
                String address = getString(R.string.foodist_server_address);
                int port = Integer.parseInt(getString(R.string.foodist_server_port));
                ManagedChannel channel = OkHttpChannelBuilder.forAddress(address, port)
                        .sslSocketFactory(factory)
                        .build();
                asyncStub = FoodISTServerServiceGrpc.newStub(channel);
                stub = FoodISTServerServiceGrpc.newBlockingStub(channel);
            }
            return asyncStub;
        } catch (Exception e) {
            Log.e("SSL-SOCKET-FACTORY", e.getMessage());
            return null;
        }
    }

    public List<FoodService> getServices() {
        return new ArrayList<>(this.services.values());
    }

    public void setServices(List<FoodService> services) {
        Map<String, FoodService> serviceMap = new ConcurrentHashMap<>();
        services.forEach(service -> serviceMap.put(service.getName(), service));
        this.services = serviceMap;
    }

    public void setQueueTimes(Map<String, String> times) {
        times.forEach((name, time) -> {
            FoodService service = this.services.get(name);
            if (service != null) {
                service.setTime(time);
            }
        });
    }

    public String getApiKey() {
        return getString(R.string.map_api_key);
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

    public Map<Contract.FoodType, Boolean> getUserConstraints() {
        Map<Contract.FoodType, Boolean> userConstraints = new HashMap<>();

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);

        boolean vegetarian = pref.getBoolean(VEGETARIAN_KEY, true);
        userConstraints.put(Contract.FoodType.Vegetarian, vegetarian);

        boolean meat = pref.getBoolean(MEAT_KEY, true);
        userConstraints.put(Contract.FoodType.Meat, meat);

        boolean vegan = pref.getBoolean(VEGAN_KEY, true);
        userConstraints.put(Contract.FoodType.Vegan, vegan);

        boolean fish = pref.getBoolean(FISH_KEY, true);
        userConstraints.put(Contract.FoodType.Fish, fish);

        return userConstraints;
    }

    public String getLanguage() {
        SharedPreferences pref = getSharedPreferences(getString(R.string.profile_file), 0);
        return pref.getString(getString(R.string.shared_prefs_profile_language), "en");
    }

    public String getUsername() {
        SharedPreferences pref = getSharedPreferences(getString(R.string.profile_file), 0);
        return pref.getString(getString(R.string.shared_prefs_profile_username), "");
    }

    public String getUserRole() {
        SharedPreferences pref = getSharedPreferences(getString(R.string.profile_file), 0);
        return pref.getString(getString(R.string.shared_prefs_profile_profession), Contract.Role.Student.name());
    }

    public void saveProfile(Contract.Profile profile) {
        SharedPreferences pref = getSharedPreferences(getString(R.string.profile_file), 0);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(getString(R.string.shared_prefs_profile_username), profile.getName());
        editor.putString(getString(R.string.shared_prefs_profile_profession), profile.getRole().name());
        editor.putString(getString(R.string.shared_prefs_profile_language), profile.getLanguage());
        editor.putBoolean(GlobalStatus.VEGETARIAN_KEY, profile.getPreferencesOrDefault(Contract.FoodType.Vegetarian_VALUE, true));
        editor.putBoolean(GlobalStatus.MEAT_KEY, profile.getPreferencesOrDefault(Contract.FoodType.Meat_VALUE, true));
        editor.putBoolean(GlobalStatus.FISH_KEY, profile.getPreferencesOrDefault(Contract.FoodType.Fish_VALUE, true));
        editor.putBoolean(GlobalStatus.VEGAN_KEY, profile.getPreferencesOrDefault(Contract.FoodType.Vegan_VALUE, true));

        editor.apply();
    }

    public Contract.Profile loadProfile() {
        Contract.Profile.Builder builder = Contract.Profile.newBuilder();
        SharedPreferences pref = getSharedPreferences(getString(R.string.profile_file), 0);

        String username = pref.getString(getString(R.string.shared_prefs_profile_username), "");
        String position = pref.getString(getString(R.string.shared_prefs_profile_profession), Contract.Role.Student.name());
        String language = pref.getString(getString(R.string.shared_prefs_profile_language), "en");

        builder.setName(username);
        builder.setRole(Contract.Role.valueOf(position));
        builder.setLanguage(language);

        builder.putPreferences(Contract.FoodType.Vegetarian_VALUE, pref.getBoolean(GlobalStatus.VEGETARIAN_KEY, true));
        builder.putPreferences(Contract.FoodType.Meat_VALUE, pref.getBoolean(GlobalStatus.MEAT_KEY, true));
        builder.putPreferences(Contract.FoodType.Fish_VALUE, pref.getBoolean(GlobalStatus.FISH_KEY, true));
        builder.putPreferences(Contract.FoodType.Vegan_VALUE, pref.getBoolean(GlobalStatus.VEGAN_KEY, true));

        return builder.build();
    }


    public String getUUID() {
        SharedPreferences pref = getSharedPreferences(getString(R.string.profile_file), 0);
        String uuid = pref.getString(getString(R.string.shared_prefs_user_uuid), null);
        if (uuid == null) {
            SharedPreferences.Editor editor = pref.edit();
            uuid = UUID.randomUUID().toString();
            editor.putString(getString(R.string.shared_prefs_user_uuid), uuid);
            editor.apply();
        }
        return uuid;
    }

    public void saveCookie(String cookie) {
        SharedPreferences pref = getSharedPreferences(getString(R.string.profile_file), 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(getString(R.string.cookie_pref_key), cookie);
        editor.apply();
    }

    public void removeCookie() {
        SharedPreferences pref = getSharedPreferences(getString(R.string.profile_file), 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(getString(R.string.cookie_pref_key));
        editor.apply();
    }

    public String getCookie() {
        SharedPreferences pref = getSharedPreferences(getString(R.string.profile_file), 0);
        return pref.getString(getString(R.string.cookie_pref_key), "");
    }

    public boolean isLoggedIn() {
        SharedPreferences pref = getSharedPreferences(getString(R.string.profile_file), 0);
        return pref.getString(getString(R.string.cookie_pref_key), null) != null;
    }

    public void setFlagged(String photoId) {
        SharedPreferences pref = getSharedPreferences(getString(R.string.flag_file), 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(getString(R.string.shared_prefs_flagged_photo_key, photoId, getUsername()), true);
        editor.apply();
    }

    public boolean isFlagged(String photoId) {
        if (!isLoggedIn()) {
            return false;
        }
        SharedPreferences pref = getSharedPreferences(getString(R.string.flag_file), 0);
        return pref.getBoolean(getString(R.string.shared_prefs_flagged_photo_key, photoId, getUsername()), false);
    }

    public void setRated(String menuId, float value) {
        SharedPreferences pref = getSharedPreferences(getString(R.string.shared_prefs_ratings_file), 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat(getString(R.string.shared_prefs_rated_menu_key, menuId, getUsername()), value);
        editor.apply();
    }

    public float getRating(String menuId) {
        if (!isLoggedIn()) {
            return  0f;
        }
        SharedPreferences pref = getSharedPreferences(getString(R.string.shared_prefs_ratings_file), 0);
        return pref.getFloat(getString(R.string.shared_prefs_rated_menu_key, menuId, getUsername()), 0f);
    }

    public void setMenuFlagged(String menuId) {
        SharedPreferences pref = getSharedPreferences(getString(R.string.flag_file), 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(getString(R.string.shared_prefs_flagged_menu_key, menuId, getUsername()), true);
        editor.apply();
    }

    public boolean isMenuFlagged(String menuId) {
        SharedPreferences pref = getSharedPreferences(getString(R.string.flag_file), 0);
        return pref.getBoolean(getString(R.string.shared_prefs_flagged_menu_key, menuId, getUsername()), false);
    }

    public String formatRating(double rating) {
        return DECIMAL_FORMAT.format(rating);
    }

    public Executor getExecutor() {
        if (executor == null) {
            executor = Executors.newCachedThreadPool();
        }
        return executor;
    }
}
