package pt.ulisboa.tecnico.cmov.foodist.async.base;

import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;

/**
 * Task that is cancelled if it's activity is destroyed by the android runtime
 * Must be the root of the decoration
 */

public class CancelableTask<T, U, V, A extends BaseActivity> extends BaseAsyncTask<T, U, V, A> {

    private BaseAsyncTask<T, U, V, A> task;

    public CancelableTask(BaseAsyncTask<T, U, V, A> task) {
        super(task.getActivity());
        this.task = task;
    }

    @Override
    protected void onPreExecute() {
        task.onPreExecute();
        task.getActivity().addTask(this);
    }


    @Override
    protected V doInBackground(T... ts) {
        return task.doInBackground(ts);
    }

    @Override
    public void onPostExecute(V result) {
        task.onPostExecute(result);
        task.getActivity().removeTask(this);
    }

    @Override
    public void onCancelled(V result) {
        task.onCancelled(result);
    }

    @Override
    public void onCancelled() {
        task.onCancelled();
    }

}
