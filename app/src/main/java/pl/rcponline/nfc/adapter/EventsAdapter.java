package pl.rcponline.nfc.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import pl.rcponline.nfc.R;
import pl.rcponline.nfc.model.Event;

public class EventsAdapter  extends ArrayAdapter<Event>{

    public static final String TAG = "Event_Adapter";
    private List<Event> events;
    private final Context context;

    public EventsAdapter(Context context, List<Event> events) {
        super(context, 0, events);

        this.context = context;
        this.events = events;
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Event getItem(int position) {
        return events.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_events,null);
        }

        Event e  = events.get(position);
        if(e != null){
            TextView EventTime  = (TextView) view.findViewById(R.id.tv_list_events_time);
            TextView EventName  = (TextView) view.findViewById(R.id.tv_list_events_event);
            ImageView EventIcon = (ImageView) view.findViewById(R.id.iv_list_events_icon);
            TextView EventDesc  = (TextView) view.findViewById(R.id.tv_list_events_desc);

            if(e.getStatus() == 0){
                EventTime.setTextColor(Color.GRAY);
                EventName.setTextColor(Color.GRAY);
                EventDesc.setTextColor(Color.GRAY);
            }
            if(EventTime != null){
                EventTime.setText(getFormat(e.getDatetime()));
            }
            if(EventName != null){
                EventName.setText(getContext().getString(getContext().getResources().getIdentifier(String.valueOf(e.getTypeName()), "string", getContext().getPackageName())));
            }
            if(EventDesc != null){
                EventDesc.setText(e.getComment());
            }
            if(EventIcon != null){

                switch (e.getType()){
                    case 1:
                        EventIcon.setImageResource(R.drawable.info_play);
                        break;
                    case 2:
                        EventIcon.setImageResource(R.drawable.info_pause);
                        break;
                    case 3:
                        EventIcon.setImageResource(R.drawable.info_pause_green);
                        break;
                    case 4:
                        EventIcon.setImageResource(R.drawable.info_record);
                        break;
                    case 5:
                        EventIcon.setImageResource(R.drawable.info_record_green);
                        break;
                    case 6:
                        EventIcon.setImageResource(R.drawable.info_stop);
                        break;
                    default:
                        break;
                }

            }
        }

        return view;
    }

    private String getFormat(String dateEvent){

        String dataEventFormat = "";

        try{
            //Zamiana stringa na date
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date data = format.parse(dateEvent);

            //Formatowanie daty i zamiana na stringa
            SimpleDateFormat formatView = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            dataEventFormat = formatView.format(data);

        }catch (ParseException e){
            e.printStackTrace();
        }

        return dataEventFormat;
    }
}
