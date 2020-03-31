package pt.ulisboa.tecnico.cmov.foodist.async.base;

import pt.ulisboa.tecnico.cmov.foodist.BaseActivity;

/**
 * TODO NOT FINISHED YET DO NOT USE
 */
public abstract class UniqueAsyncTask<T, U, V, A extends BaseActivity> extends BaseAsyncTask<T, U, V, A> {

    private static boolean isRunning = false;

    private boolean willRun;

    public UniqueAsyncTask(A activity, BaseAsyncTask<T, U, V, A> task) {
        super(activity);
        willRun = false;
    }

    @Override
    protected void onPreExecute() {
        if (!isRunning) {
            willRun = true;
            isRunning = true;
        }
    }

    @Override
    protected V doInBackground(T... ts) {
        if (!willRun) {
            return null;
        }
        return asyncRun(ts);
    }

    protected abstract V asyncRun(T... ts);

    @Override
    public void onPostExecute(V result) {
        super.onPostExecute(result);
        if (willRun) {
            isRunning = false;
        }
    }

    @Override
    protected void onCancelled(V v) {
        super.onCancelled(v);
        isRunning = false;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        isRunning = false;
    }

}