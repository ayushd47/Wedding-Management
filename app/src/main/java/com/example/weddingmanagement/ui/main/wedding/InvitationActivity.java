package com.example.weddingmanagement.ui.main.wedding;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wedding.BaseActivity;
import com.example.wedding.R;
import com.example.wedding.adapters.InvitationListAdapter;
import com.example.wedding.models.Invitation.Invitation;
import com.example.wedding.models.Response.AddInvitationResponse;
import com.example.wedding.models.Response.InvitationResponse;
import com.example.wedding.utils.Constants;
import com.example.wedding.utils.Validation;
import com.example.wedding.viewmodels.ViewModelProviderFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class InvitationActivity extends BaseActivity {




    private WeddingViewModel viewModel;

    @Inject
    ViewModelProviderFactory providerFactory;
        private ProgressDialog progressDialog;

    private RecyclerView invitationlist;
    private String token, weddingid;
    private SharedPreferences sharedPreferences;
    private EditText inviname, inviemail;
    private Button invitebtn;
    private List<Invitation> invitations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);

        Intent intent = getIntent();

        weddingid = intent.getStringExtra("weddingid");

        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);

        token = sharedPreferences.getString(Constants.TOKEN, null);

        viewModel = ViewModelProviders.of(this, providerFactory).get(WeddingViewModel.class);

        invitationlist = findViewById(R.id.inviteList);
        inviname = findViewById(R.id.invinametxt);
        inviemail = findViewById(R.id.inviemailtxt);
        invitebtn = findViewById(R.id.invitebtn);


        invitebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateInviteForm();
            }
        });







        viewModel.getInvitations(token, weddingid);

        observeInvitationlist();
        observeAddInvitations();
    }


    private void validateInviteForm(){
        String gname = inviname.getText().toString();
        String gemail = inviemail.getText().toString();

        int err = 0;
        if(!Validation.validateFields(gname)){
            err++;
            inviname.setError("Name cannot be empty!");
            inviname.requestFocus();
        }

        if(!Validation.validateFields(gemail)){
            err++;
            inviemail.setError("Name cannot be empty!");
            inviemail.requestFocus();
        }

        if(err == 0){
            viewModel.addInvitation(token, weddingid, gname, gemail);
            InputMethodManager imm = (InputMethodManager)this.getSystemService(
                    this.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(inviemail.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(inviname.getWindowToken(), 0);
            clearInputs();
            progressDialog = ProgressDialog.show(this, "",
                    "Sendiing Invitation. Please Wait...", true);
            progressDialog.show();

        }
    }

    private void clearInputs(){
        inviname.setText(null);
        inviemail.setText(null);

    }

    private void observeAddInvitations(){
        viewModel.observeAddInvitation().observe(this, new Observer<AddInvitationResponse>() {
            @Override
            public void onChanged(AddInvitationResponse addInvitationResponse) {
                if(addInvitationResponse.isSuccess()){

                    invitations.add(addInvitationResponse.getInvitationdata());
                    InvitationListAdapter adapter = new InvitationListAdapter(InvitationActivity.this, invitations);
                    invitationlist.setLayoutManager(new LinearLayoutManager(InvitationActivity.this));
                    invitationlist.setAdapter(adapter);

                    progressDialog.cancel();
                }
            }
        });
    }

    private void observeInvitationlist(){
        viewModel.observeInvitation().observe(this, new Observer<InvitationResponse>() {
            @Override
            public void onChanged(InvitationResponse invitationResponse) {
              if(invitationResponse.isSuccess()){
                  invitations= invitationResponse.getInvitationsdata();
                  InvitationListAdapter adapter = new InvitationListAdapter(InvitationActivity.this, invitations);
                  invitationlist.setLayoutManager(new LinearLayoutManager(InvitationActivity.this));
                  invitationlist.setAdapter(adapter);
              }
            }
        });
    }

}
