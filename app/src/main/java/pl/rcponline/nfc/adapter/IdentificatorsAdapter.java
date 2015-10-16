package pl.rcponline.nfc.adapter;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import pl.rcponline.nfc.R;
import pl.rcponline.nfc.model.Employee;
import pl.rcponline.nfc.model.Identificator;

public class IdentificatorsAdapter extends ArrayAdapter<Identificator>{

    private final static String TAG = "Employee_Adapter";
    private List<Identificator> identyficators;
    Context context;

    public IdentificatorsAdapter(Context context, List<Identificator> identificators) {
        super(context,0,identificators);

        this.context = context;
        this.identyficators = identificators;
    }

    @Override
    public int getCount() {
        return identyficators.size();
    }

    @Override
    public Identificator getItem(int position) {
        return identyficators.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if(view == null){
            LayoutInflater lInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = lInflater.inflate(R.layout.list_identificators, null);
        }

        Identificator id = identyficators.get(position);
        if(id != null){
            TextView EmployeeName = (TextView) view.findViewById(R.id.tv_list_ids_employee_name);
            TextView IdNumber = (TextView) view.findViewById(R.id.tv_list_ids_number);
            TextView IdDesc = (TextView) view.findViewById(R.id.tv_list_ids_desc);

            String fullName = id.getEmployee().getFirstname()+ " "+id.getEmployee().getName();
            EmployeeName.setText(fullName);
            IdNumber.setText(id.getNumber());
            IdDesc.setText(id.getDesc());

            Log.d(TAG, fullName+ " :"+String.valueOf(id.getEmployee().getPermission()));
            int permission = id.getEmployee().getPermission();
            ImageView permissionStar = (ImageView) view.findViewById(R.id.iv_list_ids_icon);
            if( permission == 1){
                permissionStar.setVisibility(View.VISIBLE);
            }else {
                permissionStar.setVisibility(View.GONE);
            }
        }

        return view;
    }
}
