package com.example.alahsaafforestation.volunteerfragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.example.alahsaafforestation.R;
import com.example.alahsaafforestation.api.Constants;
import com.example.alahsaafforestation.fragments.ProductDetailsFragment;
import com.example.alahsaafforestation.utils.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;


public class VolunteerAddNewServiceFragment extends Fragment {

    public static final String TAG = "volunteerAddService";

    EditText mServiceDescriptionET;
    TextView mSaveBtn;
    TextView mCancelBtn;

    int volunteerId = -1;

    public VolunteerAddNewServiceFragment() {
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
        return inflater.inflate(R.layout.fragment_volunteer_add_new_service, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mServiceDescriptionET = view.findViewById(R.id.service_description_edit_text);
        mSaveBtn = view.findViewById(R.id.save);
        mCancelBtn = view.findViewById(R.id.cancel);


        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if(validateInput()){
                    mSaveBtn.setEnabled(false);
                    addNewService();
                }
            }
        });

        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });
    }


    private void addNewService() {

        final ProgressDialog pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        int id = volunteerId;
        String description = mServiceDescriptionET.getText().toString();

        AndroidNetworking.post(Constants.BASE_URL)
                .addBodyParameter("type", "add_service")
                .addBodyParameter("description", description)
                .addBodyParameter("volunteer_id", String.valueOf(id))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        pDialog.dismiss();
                        try {
                            Toast.makeText(requireContext(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        requireActivity().onBackPressed();
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.e("Error", error.getMessage());
                        pDialog.hide();
                    }
                });

    }


    private boolean validateInput() {
        String description = mServiceDescriptionET.getText().toString();
        if(TextUtils.isEmpty(description)){
            Toast.makeText(requireContext(), "you must provide the service description!", Toast.LENGTH_SHORT).show();
            mSaveBtn.setEnabled(true);
            return false;
        }
        if(volunteerId == -1){
            Toast.makeText(requireContext(), "some error occurred, please try again later", Toast.LENGTH_SHORT).show();
            volunteerId = SharedPrefManager.getInstance(requireContext()).getUserId();
            return false;
        }

        return true;
    }
}