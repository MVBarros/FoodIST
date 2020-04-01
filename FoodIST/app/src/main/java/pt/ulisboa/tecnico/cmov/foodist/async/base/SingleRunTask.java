package pt.ulisboa.tecnico.cmov.foodist.async.base;

import java.util.HashSet;
import java.util.Set;

import pt.ulisboa.tecnico.cmov.foodist.BaseActivity;


public class SingleRunTask<T, U, V, A extends BaseActivity> extends BaseAsyncTask<T, U, V, A> {

    private static Set<Class> set = new HashSet<>();

    private boolean exec;

    private BaseAsyncTask<T, U, V, A> task;
    private Class leafClass;

    public SingleRunTask(BaseAsyncTask<T, U, V, A> task, Class leafClass) {
        super(task.getActivity());
        this.task = task;
        this.leafClass = leafClass;
    }

    @Override
    protected void onPreExecute() {
        task.onPreExecute();
        exec = set.add(leafClass);
    }

    @Override
    protected V doInBackground(T... ts) {
        if (exec) {
            return task.doInBackground(ts);
        }
        return null;
    }


    @Override
    public void onPostExecute(V result) {
        if (exec) {
            task.onPostExecute(result);
            set.remove(leafClass);
        }
    }

    @Override
    protected void onCancelled(V v) {
        task.onCancelled(v);
        if (exec) {
            set.remove(leafClass);
        }
    }

    @Override
    protected void onCancelled() {
        task.onCancelled();
        if (exec) {
            set.remove(leafClass);
        }
    }

}