package pt.ulisboa.tecnico.cmov.foodist.async.campus;

import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

public abstract class BaseAsyncTask<T, U, V, A extends AppCompatActivity> extends AsyncTask<T, U, V> {
    private WeakReference<A> activity;

    public BaseAsyncTask(A activity) {
        this.activity = new WeakReference<>(activity);
    }

    @Override
    public void onPostExecute(V result) {
        A act = activity.get();
        if (act != null && !act.isFinishing() && !act.isDestroyed()) {
            safeRunOnUiThread(result, act);
        }
    }

    abstract void safeRunOnUiThread(V result, A activity);

}
