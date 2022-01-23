package com.example.alahsaafforestation.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.alahsaafforestation.R;
import com.example.alahsaafforestation.adapters.PharmaciesAdapter;
import com.example.alahsaafforestation.api.Constants;
import com.example.alahsaafforestation.listeners.OnSearchActivatedListener;
import com.example.alahsaafforestation.model.Pharmacy;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainFragment extends Fragment {


    public static final String TAG = "customerMain";

    TextInputEditText mSearchEditText;
    //to handle search queries
    String query = "";

    List<Pharmacy> pharmacies;
    PharmaciesAdapter listAdapter;
    RecyclerView mPharmaciesList;

    OnSearchActivatedListener onSearchListener;


    public MainFragment() {
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
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mSearchEditText = view.findViewById(R.id.search_edit_text);
        mPharmaciesList = view.findViewById(R.id.pharmacies_list);

        //working with search edit text.
        //changing label of "enter" button into search instead
        mSearchEditText.setImeActionLabel("Search", KeyEvent.KEYCODE_ENTER);

        mSearchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (event == null) {
                query = mSearchEditText.getText().toString();
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    // Capture soft enters in a singleLine EditText that is the last EditText.
                    getAllPharmacies(query);
                    onSearchListener.onSearch();
                }
                else if (actionId==EditorInfo.IME_ACTION_NEXT)
                {
                    // Capture soft enters in other singleLine EditTexts
                    getAllPharmacies(query);
                    onSearchListener.onSearch();
                }
                else return false;  // Let system handle all other null KeyEvents
            } else
                return false;

            return false;
        });


        mPharmaciesList.setHasFixedSize(true);
        mPharmaciesList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        getAllPharmacies("");

    }

    public void getAllPharmacies(String query){

        final ProgressDialog pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        String url = Constants.PHARMACIES_ALL_URL+"&name="+query;

        pharmacies = new ArrayList<>();

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
                                JSONArray pharmacyJsonArray = obj.getJSONArray("data");
                                Pharmacy pharmacy;
                                for (int i = 0; i < pharmacyJsonArray.length(); i++){
                                    JSONObject pharmacyJson = pharmacyJsonArray.getJSONObject(i);
                                    Log.e(TAG, "onResponse: "+pharmacyJson.getString("id"));
                                    pharmacy = new Pharmacy(
                                            pharmacyJson.getString("id"),
                                            pharmacyJson.getString("name"),
                                            pharmacyJson.getString("pharmacist_name"),
                                            pharmacyJson.getString("image"),
                                            pharmacyJson.getString("location"),
                                            pharmacyJson.getString("phone"),
                                            pharmacyJson.getString("opensat"),
                                            pharmacyJson.getString("closesat"),
                                            "02/22"
                                    );
                                    pharmacies.add(pharmacy);
                                }

                                listAdapter = new PharmaciesAdapter(getContext(), pharmacies);
                                mPharmaciesList.setAdapter(listAdapter);

                            } else {
                                Toast.makeText(requireContext(), obj.getString("msg"), Toast.LENGTH_SHORT).show();
                                listAdapter = new PharmaciesAdapter(getContext(), pharmacies);
                                mPharmaciesList.setAdapter(listAdapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        pDialog.dismiss();
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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