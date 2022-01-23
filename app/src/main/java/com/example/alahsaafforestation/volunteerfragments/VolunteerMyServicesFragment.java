package com.example.alahsaafforestation.volunteerfragments;

import android.app.ProgressDialog;
import android.content.Context;
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
import com.example.alahsaafforestation.adapters.SellerMyProductsAdapter;
import com.example.alahsaafforestation.adapters.VolunteerMyServicesAdapter;
import com.example.alahsaafforestation.model.Product;
import com.example.alahsaafforestation.model.VoluntaryService;
import com.example.alahsaafforestation.sellerfragments.SellerMyProductsFragment;
import com.example.alahsaafforestation.utils.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class VolunteerMyServicesFragment extends Fragment {

    public static final String TAG = "volunteerMyServices";


    ArrayList<VoluntaryService> services;
    RecyclerView mMyServicesList;
    VolunteerMyServicesAdapter mMyServicesAdapter;

    Button mAddServiceBtn;


    OnAddServiceClicked mListener;

    int volunteerId;



    public VolunteerMyServicesFragment() {
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
        return inflater.inflate(R.layout.fragment_volunteer_my_services, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMyServicesList = view.findViewById(R.id.volunteer_my_services_list);
        mAddServiceBtn = view.findViewById(R.id.add_service_btn);

        getServices();

        mAddServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onAddServiceClicked();
            }
        });


    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof VolunteerMyServicesFragment.OnAddServiceClicked) {
            mListener = (VolunteerMyServicesFragment.OnAddServiceClicked) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnAddServiceClicked{
        void onAddServiceClicked();
    }

    public void getServices(){

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
                                for (int i = 0; i < servicesArray.length(); i++){
                                    JSONObject serviceJson = servicesArray.getJSONObject(i);
                                    service = new VoluntaryService(
                                            Integer.parseInt(serviceJson.getString("id")),
                                            serviceJson.getString("description"),
                                            Integer.parseInt(serviceJson.getString("volunteer_id"))
                                    );
                                    services.add(service);
                                }

                                mMyServicesAdapter = new VolunteerMyServicesAdapter(requireContext(), services);
                                mMyServicesList.setAdapter(mMyServicesAdapter);

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