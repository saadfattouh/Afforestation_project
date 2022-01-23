package com.example.alahsaafforestation.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.alahsaafforestation.LoginActivity;
import com.example.alahsaafforestation.R;
import com.example.alahsaafforestation.adapters.VolunteersAdapter;
import com.example.alahsaafforestation.api.Constants;
import com.example.alahsaafforestation.listeners.OnSearchActivatedListener;
import com.example.alahsaafforestation.model.Volunteer;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class VolunteersFragment extends Fragment {


    public static final String TAG = "customerVolunteers";

    TextInputEditText mSearchET;
    ImageView mFilterBtn;
    String query;

    RecyclerView mVolunteersListView;
    List<Volunteer> volunteers;
    VolunteersAdapter volunteersAdapter;

    OnSearchActivatedListener onSearchListener;

    int searchBy = 0;



    public VolunteersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_volunteers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSearchET = view.findViewById(R.id.search_edit_text);
        mVolunteersListView = view.findViewById(R.id.volunteers_list);
        mFilterBtn = view.findViewById(R.id.filter_btn);


        mFilterBtn.setOnClickListener(v -> {
            Context ctx = requireContext();
            LayoutInflater factory = LayoutInflater.from(ctx);
            final View view1 = factory.inflate(R.layout.volunteer_search_filter_dialog, null);
            final AlertDialog filterChooserDialog = new AlertDialog.Builder(ctx).create();
            filterChooserDialog.setView(view1);

            RadioGroup rg = view1.findViewById(R.id.rg_chooser);

            rg.setOnCheckedChangeListener((group, checkedId) -> {
                switch (checkedId){
                    case R.id.name:
                        searchBy = 0;
                        filterChooserDialog.dismiss();
                        break;
                    case R.id.desc:
                        searchBy = 1;
                        filterChooserDialog.dismiss();
                        break;
                    case R.id.address :
                        searchBy = 2;
                        filterChooserDialog.dismiss();
                        break;
                }
            });

            filterChooserDialog.show();
        });

        //working with search edit text.
        //changing label of "enter" button into search instead
        mSearchET.setImeActionLabel("Search", KeyEvent.KEYCODE_ENTER);

        mSearchET.setOnEditorActionListener((v, actionId, event) -> {
            if (event == null) {
                query = mSearchET.getText().toString();
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    // Capture soft enters in a singleLine EditText that is the last EditText.
                    if(query.isEmpty()){
                        Toast.makeText(requireContext(), "enter a search query first!", Toast.LENGTH_SHORT).show();
                    }else {
                        if(searchBy == 0)
                            getVolunteers(query);
                        else {
                            getVolunteers(query, searchBy);
                        }
                        onSearchListener.onSearch();
                    }
                }
                else if (actionId==EditorInfo.IME_ACTION_NEXT)
                {
                    // Capture soft enters in other singleLine EditTexts
                    if(query.isEmpty()){
                        Toast.makeText(requireContext(), "enter a search query first!", Toast.LENGTH_SHORT).show();
                    }else {
                        if(searchBy == 0)
                            getVolunteers(query);
                        else {
                            getVolunteers(query, searchBy);
                        }
                        onSearchListener.onSearch();
                    }
                }
                else return false;  // Let system handle all other null KeyEvents
            } else
                return false;

            return false;
        });


        getVolunteers("");
    }


    public void getVolunteers(String query) {
        final ProgressDialog pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        String url = Constants.GET_ALL_VOLUNTEERS+"&name="+query;

        volunteers = new ArrayList<>();

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
                                JSONArray volunteersArray = obj.getJSONArray("data");
                                Volunteer volunteer;
                                for (int i = 0; i < volunteersArray.length(); i++){
                                    JSONObject volunteerJson = volunteersArray.getJSONObject(i);
                                    volunteer = new Volunteer(
                                            Integer.parseInt(volunteerJson.getString("id")),
                                            volunteerJson.getString("name"),
                                            volunteerJson.getString("description"),
                                            Integer.parseInt(volunteerJson.getString("availability")),
                                            volunteerJson.getString("image"),
                                            volunteerJson.getString("phone"),
                                            volunteerJson.getString("address")
                                    );
                                    volunteers.add(volunteer);
                                }

                                volunteersAdapter = new VolunteersAdapter(getContext(), volunteers);
                                mVolunteersListView.setAdapter(volunteersAdapter);

                            } else {
                                Toast.makeText(getContext(), obj.getString("msg"), Toast.LENGTH_SHORT).show();
                                volunteersAdapter = new VolunteersAdapter(getContext(), volunteers);
                                mVolunteersListView.setAdapter(volunteersAdapter);
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

    public void getVolunteers(String query, int filterBy) {
        final ProgressDialog pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        String url = Constants.GET_ALL_VOLUNTEERS;

        volunteers = new ArrayList<>();

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
                                JSONArray volunteersArray = obj.getJSONArray("data");
                                Volunteer volunteer;
                                for (int i = 0; i < volunteersArray.length(); i++){
                                    JSONObject volunteerJson = volunteersArray.getJSONObject(i);
                                    volunteer = new Volunteer(
                                            Integer.parseInt(volunteerJson.getString("id")),
                                            volunteerJson.getString("name"),
                                            volunteerJson.getString("description"),
                                            Integer.parseInt(volunteerJson.getString("availability")),
                                            volunteerJson.getString("image"),
                                            volunteerJson.getString("phone"),
                                            volunteerJson.getString("address")
                                    );
                                    if(filterBy == 1){
                                        if(volunteer.getDescription().contains(query))
                                            volunteers.add(volunteer);
                                    }else if(filterBy == 2){
                                        if(volunteer.getAddress().contains(query))
                                            volunteers.add(volunteer);
                                    }
                                }

                                volunteersAdapter = new VolunteersAdapter(getContext(), volunteers);
                                mVolunteersListView.setAdapter(volunteersAdapter);

                            } else {
                                Toast.makeText(getContext(), obj.getString("msg"), Toast.LENGTH_SHORT).show();
                                volunteersAdapter = new VolunteersAdapter(getContext(), volunteers);
                                mVolunteersListView.setAdapter(volunteersAdapter);
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSearchActivatedListener) {
            onSearchListener = (OnSearchActivatedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onSearchListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSearchListener = null;
    }

}