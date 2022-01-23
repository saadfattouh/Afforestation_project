package com.example.alahsaafforestation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.alahsaafforestation.adapters.AllServicesAdapter;
import com.example.alahsaafforestation.adapters.VolunteerMyServicesAdapter;
import com.example.alahsaafforestation.model.VoluntaryService;
import com.example.alahsaafforestation.utils.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AllServices extends AppCompatActivity {

    AllServicesAdapter mAdapter;
    ArrayList<VoluntaryService> services;
    RecyclerView mList;

    int volunteerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_services);


        volunteerId = getIntent().getIntExtra("_v_id", -1);


        mList = findViewById(R.id.all_services_list);


        getServices();
    }

    public void getServices(){

        int id = volunteerId;

        String url = "http://nawar.scit.co/trees-api/index.php?type=get_services_by_volunteer_id&id="+id+"&user_id="+id;

        //if everything is fine
        final ProgressDialog pDialog = new ProgressDialog(this);
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
                                for (int i = 0; i < servicesArray.length(); i++){
                                    JSONObject serviceJson = servicesArray.getJSONObject(i);
                                    service = new VoluntaryService(
                                            Integer.parseInt(serviceJson.getString("id")),
                                            serviceJson.getString("description"),
                                            Integer.parseInt(serviceJson.getString("volunteer_id"))
                                    );
                                    services.add(service);
                                }

                                mAdapter = new AllServicesAdapter(AllServices.this, services);
                                mList.setAdapter(mAdapter);

                            } else {
                                Toast.makeText(AllServices.this, obj.getString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        pDialog.dismiss();
                        Toast.makeText(AllServices.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });





    }
}