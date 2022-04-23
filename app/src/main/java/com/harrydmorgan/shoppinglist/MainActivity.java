package com.harrydmorgan.shoppinglist;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.harrydmorgan.shoppinglist.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    ActivityMainBinding binding;
    DatabaseHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bottomNav = binding.bottomNavigation;

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        NavController navController = navHostFragment.getNavController();

        AppBarConfiguration barConfiguration = new AppBarConfiguration.Builder(
                R.id.listFragment,
                R.id.collectionsFragment,
                R.id.historyFragment
        ).build();

        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        NavigationUI.setupActionBarWithNavController(this, navController, barConfiguration);



    }
}