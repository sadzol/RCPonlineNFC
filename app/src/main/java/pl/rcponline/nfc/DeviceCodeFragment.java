package pl.rcponline.nfc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;


public class DeviceCodeFragment extends DialogFragment {

    LayoutInflater inflater;
    View v;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

//        inflater = getActivity().getLayoutInflater();

        // Use the Builder class for convenient dialog construction
        SessionManager session = new SessionManager(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getActivity().getString(R.string.device_code)+": "+session.getDeviceCode())
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}