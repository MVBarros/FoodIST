package pt.ulisboa.tecnico.cmov.foodist.async;

import pt.ulisboa.tecnico.cmov.foodist.BaseActivity;

public abstract class CancelableAsyncTask<T, U, V, A extends BaseActivity> extends BaseAsyncTask<T, U, V, A> {
    public CancelableAsyncTask(A activity) {
        super(activity);
        activity.add(this);
    }

    @Override
    public void onPostExecute(V result) {
       super.onPostExecute(result);
        getActivity().remove(this);
    }

}
