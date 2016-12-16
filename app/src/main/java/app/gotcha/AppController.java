package app.gotcha;


import android.app.Application;
import android.util.Log;


import java.util.Locale;


public class AppController extends Application {

    private static AppController mInstance;
    protected static final String TAG = AppController.class.getSimpleName();
    private Locale locale = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }


    public static synchronized AppController getInstance() {
        if (mInstance == null) {
            try {
                mInstance = AppController.class.newInstance();
            } catch (InstantiationException e) {
                Log.e(TAG
                        + " "
                        + " getInstance: InstantiationException>>LineNumber: "
                        + Thread.currentThread().getStackTrace()[2]
                        .getLineNumber(), e.getMessage());
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                Log.e(TAG
                        + " "
                        + " getInstance: IllegalAccessException>>LineNumber: "
                        + Thread.currentThread().getStackTrace()[2]
                        .getLineNumber(), e.getMessage());
                e.printStackTrace();
            }
        }
        return mInstance;
    }

}
