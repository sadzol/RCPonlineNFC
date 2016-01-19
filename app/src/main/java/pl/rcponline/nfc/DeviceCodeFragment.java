package pl.rcponline.nfc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;


public class DeviceCodeFragment extends DialogFragment {

    LayoutInflater inflater;
    View v;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


//        PackageInfo pInfo = null;
//        try {
//            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
////        if(pInfo != null){
//            String version = pInfo.versionName;
            String versionName = BuildConfig.VERSION_NAME;
//            Log.d(TAG,"VERSION: "+version);
//            Log.d(TAG,"VERSION: "+versionName);

//        }
        // Use the Builder class for convenient dialog construction
        SessionManager session = new SessionManager(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getString(R.string.title_activity_login) + " - " + getActivity().getString(R.string.app_name));

        String deviceCode = getActivity().getString(R.string.device_code) + ": " + session.getDeviceCode();
        String version = getActivity().getString(R.string.version) + ": " + versionName;

        builder.setMessage( "\n"+deviceCode + "\n\n" + version+ "\n")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}