package com.example.lm;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;


public class MessagesAdapter extends RecyclerView.Adapter {

    String monthName;
    Context context;
    ArrayList<Messages> messagesArrayList;

    int ITEM_SEND = 1;
    int ITEM_RECEIVE = 2;

    public MessagesAdapter(Context context, ArrayList<Messages> messagesArrayList) {
        this.context = context;
        this.messagesArrayList = messagesArrayList;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SEND)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.senderchatlayout,parent,false);
            return new SenderViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.recieverchatlayout,parent,false);
            return new ReceiverViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Messages message = messagesArrayList.get(position);

        long previousTs = 0;
        if(position >= 1){
            Messages pm = messagesArrayList.get(position-1);
            previousTs = pm.getTimestamp();
        }

        if(holder.getClass() == SenderViewHolder.class)
        {
            SenderViewHolder viewHolder = (SenderViewHolder)holder;

            viewHolder.senderTextOfMessage.setText(message.getMessage());
            viewHolder.senderTimeOfMessage.setText(message.getCurrentTime());

            if (message.isSeen()){
                viewHolder.seen.setVisibility(View.VISIBLE);
                viewHolder.delivered.setVisibility(View.GONE);
            }
            else{
                viewHolder.seen.setVisibility(View.GONE);
                viewHolder.delivered.setVisibility(View.VISIBLE);
            }

            setTimeTextVisibility(message.getTimestamp(), previousTs, viewHolder.senderGroupDate);

            if (position == messagesArrayList.size()-1)
                viewHolder.senderView.setVisibility(View.VISIBLE);
            else
                viewHolder.senderView.setVisibility(View.GONE);

            if (position < messagesArrayList.size()-1)
            {
                Messages nextMessage = messagesArrayList.get(position+1);
                if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(nextMessage.getSenderId()))
                    viewHolder.senderView.setVisibility(View.VISIBLE);
                else
                    viewHolder.senderView.setVisibility(View.GONE);
            }

        }
        else
        {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder)holder;

            viewHolder.receiverTextOfMessage.setText(message.getMessage());
            viewHolder.receiverTimeOfMessage.setText(message.getCurrentTime());

            setTimeTextVisibility(message.getTimestamp(), previousTs, viewHolder.receiverGroupDate);

            if (position == messagesArrayList.size()-1)
                viewHolder.receiverView.setVisibility(View.VISIBLE);
            else
                viewHolder.receiverView.setVisibility(View.GONE);

            if (position < messagesArrayList.size()-1)
            {
                Messages nextMessage = messagesArrayList.get(position+1);
                if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(nextMessage.getSenderId()))
                    viewHolder.receiverView.setVisibility(View.GONE);
                else
                    viewHolder.receiverView.setVisibility(View.VISIBLE);
            }

        }


    }


    private void setTimeTextVisibility(long ts1, long ts2, TextView timeText){
        Calendar current_date = Calendar.getInstance();
        current_date.getTimeInMillis();
        String text;

        if(ts2==0) {
            Calendar group_date = Calendar.getInstance();
            group_date.setTimeInMillis(ts1);

            boolean sameYear = current_date.get(Calendar.YEAR) == group_date.get(Calendar.YEAR);

            timeText.setVisibility(View.VISIBLE);

            if (sameYear){
                text = DateFormat.format("d", ts1).toString() + " " + getMonthName(group_date.get(Calendar.MONTH));
            }
            else{
                text = DateFormat.format("d", ts1).toString() + " " + getMonthName(group_date.get(Calendar.MONTH)) + " " + group_date.get(Calendar.YEAR);
            }
            timeText.setText(text);
        }
        else {
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();

            cal1.setTimeInMillis(ts1);
            cal2.setTimeInMillis(ts2);

            boolean Day = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
            boolean Year = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

            if (Year && Day){
                timeText.setVisibility(View.GONE);
                timeText.setText("");
            }
            else{
                if (cal1.get(Calendar.YEAR) == current_date.get(Calendar.YEAR))
                    text = DateFormat.format("d", ts1).toString() + " " + getMonthName(cal1.get(Calendar.MONTH));
                else
                    text = DateFormat.format("d", ts1).toString() + " " + getMonthName(cal1.get(Calendar.MONTH)) + " " + cal1.get(Calendar.YEAR);

                timeText.setVisibility(View.VISIBLE);
                timeText.setText(text);
            }

        }
    }


    @Override
    public int getItemViewType(int position) {
        Messages messages = messagesArrayList.get(position);
        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messages.getSenderId()))
        {
            return  ITEM_SEND;
        }
        else
        {
            return ITEM_RECEIVE;
        }
    }


    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }


    static class SenderViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener
    {
        TextView senderTextOfMessage, senderTimeOfMessage, senderGroupDate;
        ImageView delivered, seen;
        View senderView;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderView = itemView.findViewById(R.id.view);
            senderTextOfMessage = itemView.findViewById(R.id.sendermessage);
            senderTimeOfMessage = itemView.findViewById(R.id.timeofmessage);
            delivered = itemView.findViewById(R.id.status_of_seen);
            seen = itemView.findViewById(R.id.status_of_seen_2);
            senderGroupDate = itemView.findViewById(R.id.date_group);
            senderTextOfMessage.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(@NonNull ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.add(this.getBindingAdapterPosition(), 120, 0, "Ответить");
            contextMenu.add(this.getBindingAdapterPosition(), 121, 1, "Копировать");
            contextMenu.add(this.getBindingAdapterPosition(), 122, 2, "Переслать");
            contextMenu.add(this.getBindingAdapterPosition(), 123, 3, "Закрепить");
            contextMenu.add(this.getBindingAdapterPosition(), 124, 4, "Изменить");
            contextMenu.add(this.getBindingAdapterPosition(), 125, 5, "Удалить");
        }
    }


    static class ReceiverViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener
    {
        TextView receiverTextOfMessage, receiverTimeOfMessage, receiverGroupDate;
        View receiverView;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverView = itemView.findViewById(R.id.view);
            receiverTextOfMessage = itemView.findViewById(R.id.sendermessage);
            receiverTimeOfMessage = itemView.findViewById(R.id.timeofmessage);
            receiverGroupDate = itemView.findViewById(R.id.date_group);
            receiverTextOfMessage.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(@NonNull ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.add(this.getBindingAdapterPosition(), 120, 0, "Ответить");
            contextMenu.add(this.getBindingAdapterPosition(), 121, 1, "Копировать");
            contextMenu.add(this.getBindingAdapterPosition(), 122, 2, "Переслать");
            contextMenu.add(this.getBindingAdapterPosition(), 123, 3, "Закрепить");
            contextMenu.add(this.getBindingAdapterPosition(), 125, 5, "Удалить");
        }
    }


    public void copyMessage(int position){
        Messages messages = messagesArrayList.get(position);
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", messages.getMessage());
        clipboard.setPrimaryClip(clip);
    }


    private String getMonthName(int num){

        switch (num) {
            case Calendar.JANUARY:
                monthName = "января";
                break;
            case Calendar.FEBRUARY:
                monthName = "февраля";
                break;
            case Calendar.MARCH:
                monthName = "марта";
                break;
            case Calendar.APRIL:
                monthName = "апреля";
                break;
            case Calendar.MAY:
                monthName = "мая";
                break;
            case Calendar.JUNE:
                monthName = "июня";
                break;
            case Calendar.JULY:
                monthName = "июля";
                break;
            case Calendar.AUGUST:
                monthName = "августа";
                break;
            case Calendar.SEPTEMBER:
                monthName = "сентября";
                break;
            case Calendar.OCTOBER:
                monthName = "октября";
                break;
            case Calendar.NOVEMBER:
                monthName = "ноября";
                break;
            case Calendar.DECEMBER:
                monthName = "декабря";
                break;
        }
        return monthName;
    }

}

