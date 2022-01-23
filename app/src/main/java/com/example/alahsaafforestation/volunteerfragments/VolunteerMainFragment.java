package com.example.alahsaafforestation.volunteerfragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.alahsaafforestation.R;
import com.example.alahsaafforestation.adapters.VolunteerMyServicesAdapter;
import com.example.alahsaafforestation.adapters.VolunteerRequestsAdapter;
import com.example.alahsaafforestation.api.Constants;
import com.example.alahsaafforestation.model.CustomerServices;
import com.example.alahsaafforestation.model.VoluntaryService;
import com.example.alahsaafforestation.utils.SharedPrefManager;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class VolunteerMainFragment extends Fragment {

    public static final String TAG = "volunteerMain";

    ArrayList<CustomerServices> services;
    RecyclerView mCustomerRequestsList;
    VolunteerRequestsAdapter mCustomerRequestsAdapter;

    Button mAcceptAll;
    Button mRejectAll;

    int volunteerId;

    public VolunteerMainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        volunteerId = SharedPrefManager.getInstance(requireContext()).getUserId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_volunteer_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCustomerRequestsList = view.findViewById(R.id.customer_service_request_list);
        mAcceptAll = view.findViewById(R.id.accept_all_orders_btn);
        mRejectAll = view.findViewById(R.id.reject_all_orders_btn);

        mAcceptAll.setOnClickListener(v -> {
            if(services.isEmpty()){
                Toast.makeText(requireContext(), "you don't have any requests from customers yet", Toast.LENGTH_SHORT).show();
            }else {
                updateAllServices(Constants.SERVICE_STATUS_ACCEPTED);

            }
        });

        mRejectAll.setOnClickListener(v -> {
            if(services.isEmpty()){
                Toast.makeText(requireContext(), "you don't have any requests from customers yet", Toast.LENGTH_SHORT).show();
            }else {
                updateAllServices(Constants.SERVICE_STATUS_REJECTED);

            }
        });

        getServiceRequests();

    }

    void getServiceRequests(){
        int id = volunteerId;

        String url = "http://nawar.scit.co/trees-api/index.php?type=get_services_by_volunteer_id&id="+id+"&user_id="+id;

        //if everything is fine
        final ProgressDialog pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        services = new ArrayList<>();

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
                                JSONArray servicesArray = obj.getJSONArray("data");
                                VoluntaryService service;
                                CustomerServices request;
                                for (int i = 0; i < servicesArray.length(); i++){
                                    JSONObject serviceJson = servicesArray.getJSONObject(i);
                                    service = new VoluntaryService(
                                            Integer.parseInt(serviceJson.getString("id")),
                                            serviceJson.getString("description"),
                                            Integer.parseInt(serviceJson.getString("volunteer_id"))
                                    );
                                    if(serviceJson.getBoolean("joined_before")){
                                        continue;
                                    }else {
                                        JSONArray requestsArray = serviceJson.getJSONArray("customers");
                                        for (int j = 0; j < requestsArray.length(); j++) {
                                            JSONObject requestJson = requestsArray.getJSONObject(j);
                                            if(Integer.parseInt(requestJson.getString("status")) != Constants.ORDER_STATUS_NEW)
                                                continue;
                                            request = new CustomerServices(
                                                    Integer.parseInt(requestJson.getString("id")),
                                                    requestJson.getString("name"),
                                                    service
                                            );
                                            services.add(request);
                                        }
                                    }
                                }

                                mCustomerRequestsAdapter = new VolunteerRequestsAdapter(requireContext(), services);
                                mCustomerRequestsList.setAdapter(mCustomerRequestsAdapter);

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

    void updateAllServices(int status){
        String url = "http://nawar.scit.co/trees-api/index.php";

        final ProgressDialog pDialog = new ProgressDialog(requireContext());
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        AndroidNetworking.post(url)
                .addBodyParameter("type", "update_all_service_status")
                .addBodyParameter("volunteer_id", String.valueOf(volunteerId))
                .addBodyParameter("status", String.valueOf(status))
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

                                Toast.makeText(requireContext(), obj.getString("msg"), Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(requireContext(), obj.getString("msg"), Toast.LENGTH_SHORT).show();
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


}