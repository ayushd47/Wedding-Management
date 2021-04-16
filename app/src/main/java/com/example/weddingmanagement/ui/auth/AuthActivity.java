package com.example.weddingmanagement.ui.auth;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.example.wedding.R;
import com.example.wedding.adapters.TabsAdapter;
import com.example.wedding.ui.auth.login.LoginFragment;
import com.example.wedding.ui.auth.register.RegisterFragment;
import com.google.android.material.tabs.TabLayout;

import dagger.android.support.DaggerAppCompatActivity;

public class AuthActivity extends DaggerAppCompatActivity {

    TabLayout tabLayout;
    ViewPager viewPager;
    Spinner locationSpinner;

    String[] locations = {"Select Your Location", "Kathmandu", "Bhaktapur", "Patan"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tabLayout  = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.viewpager);
        locationSpinner = findViewById(R.id.locationSpinner);


        ArrayAdapter<String> locationAdapter = new ArrayAdapter<String>(this,
                R.layout.location_values, locations);


        FragmentManager fragmentManager = getSupportFragmentManager();
        TabsAdapter tabsAdapter = new TabsAdapter(fragmentManager, 1);



        try{

            tabsAdapter.addFragment(new LoginFragment(), "Login");
            tabsAdapter.addFragment(new RegisterFragment(), "Register");
        }catch (Exception e){
            Log.d("Error", e.toString());
        }
        viewPager.setAdapter(tabsAdapter);
        tabLayout.setupWithViewPager(viewPager);



    }
}
