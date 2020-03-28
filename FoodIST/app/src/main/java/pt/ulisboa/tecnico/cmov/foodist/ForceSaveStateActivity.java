package pt.ulisboa.tecnico.cmov.foodist;

import android.os.Bundle;

/**
 * Activity that forces the programmer to save the instance state before destroying the activity
 * and automatically loads it if it was saved previously
 * (This may not be necessary but it is here non the less)
 */
public abstract class ForceSaveStateActivity extends BaseActivity {

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        loadState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        saveState(savedInstanceState);
    }

    abstract void loadState(Bundle savedInstanceState);

    abstract void saveState(Bundle savedInstanceState);

}
