package pt.ulisboa.tecnico.cmov.foodist.async;

import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;


/**
 * Task that only runs safeRunOnUiThread if the activity still exists
 */

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

    public A getActivity() {
        return activity.get();
    }

    abstract void safeRunOnUiThread(V result, A activity);

}
