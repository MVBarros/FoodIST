package pt.ulisboa.tecnico.cmov.foodist.async.base;

import pt.ulisboa.tecnico.cmov.foodist.BaseActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.base.BaseAsyncTask;

/**
 * Task that is cancelled if it's activity is destroyed by the android runtime
 */

public abstract class CancelableAsyncTask<T, U, V, A extends BaseActivity> extends BaseAsyncTask<T, U, V, A> {
    public CancelableAsyncTask(A activity) {
        super(activity);
        activity.addTask(this);
    }

    @Override
    public void onPostExecute(V result) {
       super.onPostExecute(result);
        getActivity().removeTask(this);
    }

}
