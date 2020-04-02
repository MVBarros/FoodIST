package pt.ulisboa.tecnico.cmov.foodist.async.base;

import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;


/**
 * Task that maintains a reference to the activity that created it
 */

public abstract class BaseAsyncTask<T, U, V, A extends AppCompatActivity> extends AsyncTask<T, U, V> {
    private WeakReference<A> activity;

    public BaseAsyncTask(A activity) {
        this.activity = new WeakReference<>(activity);
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onCancelled() {}

    @Override
    protected void onCancelled(V result) {}


    @Override
    protected abstract V doInBackground(T... ts);

    @Override
    public void onPostExecute(V result) {}

    public A getActivity() {
        return activity.get();
    }
}
