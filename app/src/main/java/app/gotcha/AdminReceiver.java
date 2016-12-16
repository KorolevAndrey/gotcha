package app.gotcha;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AdminReceiver extends DeviceAdminReceiver {

    Context context;


    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        DevicePolicyManager mgr = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        int no = mgr.getCurrentFailedPasswordAttempts();
        this.context = context;
        if (no >= 3) {
            Toast.makeText(context, "You have tried more than the allowed number.", Toast.LENGTH_LONG).show();
            Intent translucent = new Intent(context, TakePicture.class);
            translucent.putExtra("FLASH", "off");
            translucent.putExtra("Front_Request", true);
            translucent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(translucent);
        }
    }

    @Override
    public void onPasswordSucceeded(Context ctxt, Intent intent) {
        String tag = "tag";
        Log.v(tag, "this massage from success");
    }


}
