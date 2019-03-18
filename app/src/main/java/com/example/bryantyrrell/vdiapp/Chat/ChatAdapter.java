package com.example.bryantyrrell.vdiapp.Chat;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.bryantyrrell.vdiapp.R;

import java.util.ArrayList;

public class ChatAdapter extends ArrayAdapter<ChatMessage> {

    public ChatAdapter(Context context, ArrayList<ChatMessage> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ChatMessage message = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.message, parent, false);
        }

              TextView messageText = (TextView) convertView.findViewById(R.id.message_text);
              TextView messageUser = (TextView) convertView.findViewById(R.id.message_user);
              TextView messageTime = (TextView) convertView.findViewById(R.id.message_time);

              // Set their text
              messageText.setText(message.getMessageText());
              messageUser.setText(message.getMessageUser());

              // Format the date before showing it
              messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                      message.getMessageTime()));

        // Return the completed view to render on screen
        return convertView;
    }
}
