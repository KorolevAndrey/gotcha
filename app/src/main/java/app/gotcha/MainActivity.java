package app.gotcha;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String LOG_TAG = "DevicePolicyAdmin";
    DevicePolicyManager gotchaDevicePolicyManager;
    ComponentName gotchaDevicePolicyAdmin;
    private static final String TAG = "MainActivity";
    protected static final int REQUEST_ENABLE = 1;
    private String possibleEmail;

    LinearLayout lin_location, lin_delete, lin_mail;
    Switch enable_mail;
    CheckBox enable_sound;
    TextView tv_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_email = (TextView) findViewById(R.id.tv_email);
        lin_location = (LinearLayout) findViewById(R.id.lin_location);
        lin_mail = (LinearLayout) findViewById(R.id.lin_mail);
        assert lin_location != null;
        lin_location.setOnClickListener(this);
        lin_mail.setOnClickListener(this);

        lin_delete = (LinearLayout) findViewById(R.id.lin_delete);
        assert lin_delete != null;
        lin_delete.setOnClickListener(this);
        enable_mail = (Switch) findViewById(R.id.enable_mail);
        enable_sound = (CheckBox) findViewById(R.id.enable_sound);

        enable_mail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, gotchaDevicePolicyAdmin);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            getString(R.string.description));
                    startActivityForResult(intent, REQUEST_ENABLE);


                } else {
                    if (isMyDevicePolicyReceiverActive()) {
                        removeMyDevicePolicyReceiverActive();
                        Snackbar.make(lin_delete, "Gotcha has been Removed Successfully from your Device", Snackbar.LENGTH_LONG).show();
                        stopService(new Intent(MainActivity.this, GoogleApiService.class));
                        stopService(new Intent(MainActivity.this, GotchaService.class));
                    }
                }
            }
        });
        enable_sound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PreferenceManager.getDefaultSharedPreferences(AppController.getInstance()).edit().putBoolean("sound", true).apply();
                } else {
                    PreferenceManager.getDefaultSharedPreferences(AppController.getInstance()).edit().putBoolean("sound", false).apply();
                }
            }
        });


        if (PreferenceManager.getDefaultSharedPreferences(AppController.getInstance()).getString("email", "").length() == 0) {
            Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
            Account[] accounts = AccountManager.get(this).getAccounts();
            for (Account account : accounts) {
                if (emailPattern.matcher(account.name).matches()) {
                    possibleEmail = account.name;
                    tv_email.setText(possibleEmail);
                    PreferenceManager.getDefaultSharedPreferences(AppController.getInstance()).edit().putString("email", tv_email.getText().toString()).apply();

                }
            }
        } else {
            tv_email.setText(PreferenceManager.getDefaultSharedPreferences(AppController.getInstance()).getString("email", ""));
        }

        gotchaDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        gotchaDevicePolicyAdmin = new ComponentName(this, AdminReceiver.class);
        enable_sound.setChecked(PreferenceManager.getDefaultSharedPreferences(AppController.getInstance()).getBoolean("sound", false));
        enable_mail.setChecked(isMyDevicePolicyReceiverActive());
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!isMyDevicePolicyReceiverActive()) {
            enable_mail.setChecked(false);

        }
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ENABLE:
                    Log.v(LOG_TAG, "Enabling Policies Now");
                    startService(new Intent(MainActivity.this, GoogleApiService.class));
                    startService(new Intent(MainActivity.this, GotchaService.class));
                    Snackbar.make(enable_mail, "Gotcha is active now", Snackbar.LENGTH_LONG).show();
                    break;
            }
        }
    }

    private boolean isMyDevicePolicyReceiverActive() {
        return gotchaDevicePolicyManager
                .isAdminActive(gotchaDevicePolicyAdmin);
    }

    private void removeMyDevicePolicyReceiverActive() {
        gotchaDevicePolicyManager
                .removeActiveAdmin(gotchaDevicePolicyAdmin);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_location:
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(callGPSSettingIntent);
                break;

            case R.id.lin_delete:
                if (isMyDevicePolicyReceiverActive()) {

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Remove Gotcha")
                            .setMessage("Are you sure you want to disable Gotcha before uninstalling ?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                    removeMyDevicePolicyReceiverActive();
                                    enable_mail.setChecked(false);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Snackbar.make(lin_delete, "Gotcha has been Removed Successfully from your Device", Snackbar.LENGTH_LONG).show();
                                            Uri packageURI = Uri.parse("package:" + getPackageName());
                                            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                                            startActivity(uninstallIntent);
                                        }
                                    }, 500);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                } else {
                    Uri packageURI = Uri.parse("package:" + getPackageName());
                    Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                    startActivity(uninstallIntent);
                }
                break;
            case R.id.lin_mail:
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                dialog.setContentView(R.layout.edit_mail_dialog);
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

                final EditText ed_email = (EditText) dialog.findViewById(R.id.ed_mail);
                TextView ok = (TextView) dialog.findViewById(R.id.ok);
                TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tv_email.setText(ed_email.getText().toString());
                        PreferenceManager.getDefaultSharedPreferences(AppController.getInstance()).edit().putString("email", tv_email.getText().toString()).apply();
                        dialog.dismiss();
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                //Grab the window of the dialog, and change the width
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                Window window = dialog.getWindow();
                lp.copyFrom(window.getAttributes());
                //This makes the dialog take up the full width
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(lp);
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                String shareBody = "Check out this awesome Android app. It takes a photo of any one who tries to unlock your phone with the wrong code.\nhttp://bit.ly/1UQbfZY";
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Gotcha App");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                return false;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}

