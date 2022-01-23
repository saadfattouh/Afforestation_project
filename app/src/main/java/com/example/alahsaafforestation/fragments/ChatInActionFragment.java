package com.example.alahsaafforestation.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.alahsaafforestation.R;
import com.example.alahsaafforestation.adapters.ChatMessagesAdapter;
import com.example.alahsaafforestation.api.Constants;
import com.example.alahsaafforestation.model.BaseMessage;
import com.example.alahsaafforestation.utils.SharedPrefManager;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChatInActionFragment extends Fragment {

    public static final String TAG = "chatInAction";


    RecyclerView mMessagesList;
    ArrayList<BaseMessage> messages;
    ChatMessagesAdapter messagesAdapter;

    TextInputEditText messageET;
    ImageView sendBtn;

    private String userId;

    public ChatInActionFragment() {

    }
    public static ChatInActionFragment newInstance(String otherId) {
        ChatInActionFragment fragment = new ChatInActionFragment();
        Bundle args = new Bundle();
        args.putString("otherId", otherId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("otherId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_in_action, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessagesList = view.findViewById(R.id.messages_list);
        messageET = view.findViewById(R.id.message_edit_text);
        sendBtn = view.findViewById(R.id.send_btn);


        sendBtn.setOnClickListener(v -> {

            String message = messageET.getText().toString();
            messageET.setText("");
            if(TextUtils.isEmpty(message)){

            }else{
                sendMessage(message);
            }

        });

        getAllMessages();



    }

    private void sendMessage(String message) {

        String url = Constants.BASE_URL;

        AndroidNetworking.post(url)
                .setPriority(Priority.MEDIUM)
                .addBodyParameter("type", "send_message")
                .addBodyParameter("content", message)
                .addBodyParameter("from_id", String.valueOf(SharedPrefManager.getInstance(getContext()).getUserId()))
                .addBodyParameter("to_id", String.valueOf(userId))
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        JSONObject obj = response;
                        try {

                            if (!obj.getBoolean("error")) {
                                boolean fromMe;
                                JSONObject messageJsonObject = obj.getJSONObject("data");
                                if (messageJsonObject.getString("from_id").equals(String.valueOf(SharedPrefManager.getInstance(getContext()).getUserId())))
                                    fromMe = true;
                                else
                                    fromMe = false;
                                BaseMessage baseMessage = new BaseMessage(
                                        messageJsonObject.getString("id"),
                                        fromMe,
                                        messageJsonObject.getString("content")
                                );
                                if(messages!=null){
                                    messages.add(baseMessage);
                                }else{
                                    messages = new ArrayList<BaseMessage>();
                                    messages.add(baseMessage);
                                }

                                messagesAdapter = new ChatMessagesAdapter(messages, getContext());
                                mMessagesList.setAdapter(messagesAdapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.e("Error", error.getMessage());

                    }
                });

    }

    private void getAllMessages() {

        final ProgressDialog pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        String url = Constants.GET_MESSAGES_WITH + "&user_id="+SharedPrefManager.getInstance(getContext()).getUserId() + "&with_id="+userId;

        messages = new ArrayList<>();

        AndroidNetworking.get(url)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        pDialog.dismiss();

                        try {
                            //converting response to json object
                            JSONObject obj = response;

                            //if no error in response
                            if (!obj.getBoolean("error")) {

                                //getting the user from the response
                                JSONArray messagesJsonArray = obj.getJSONArray("data");
                                BaseMessage baseMessage;
                                for (int i = 0; i < messagesJsonArray.length(); i++){
                                    JSONObject messageJsonObject = messagesJsonArray.getJSONObject(i);
                                    boolean fromMe;
                                    if(messageJsonObject.getString("from_id").equals(String.valueOf(SharedPrefManager.getInstance(getContext()).getUserId())))
                                        fromMe = true;
                                    else
                                        fromMe = false;
                                    baseMessage = new BaseMessage(
                                            messageJsonObject.getString("id"),
                                            fromMe,
                                            messageJsonObject.getString("content")
                                    );
                                    messages.add(baseMessage);
                                }

                                messagesAdapter = new ChatMessagesAdapter(messages, getContext());
                                mMessagesList.setAdapter(messagesAdapter);

                            } else {
                                Toast.makeText(getContext(), obj.getString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        pDialog.dismiss();
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}