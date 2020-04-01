package pt.ulisboa.tecnico.cmov.foodist.async.base;

import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;


public class SafePostTask<T, U, V, A extends AppCompatActivity> extends BaseAsyncTask<T, U, V, A> {

    private BaseAsyncTask<T, U, V, A> task;
    public SafePostTask(BaseAsyncTask<T,U,V,A> task) {
        super(task.getActivity());
        this.task = task;
    }

    @Override
    protected V doInBackground(T... ts) {
        return task.doInBackground(ts);
    }

    @Override
    public void onPostExecute(V result) {
        A act = task.getActivity();
        if (act != null && !act.isFinishing() && !act.isDestroyed()) {
            task.onPostExecute(result);
        }
    }

}
