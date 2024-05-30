package com.example.blueroom;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.blueroom.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;
    private ArrayList<products> cartProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        NavController navController = ((NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.mobile_navigation)).getNavController();

        NavigationUI.setupWithNavController(binding.bottomNavView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller,
                                             @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if (destination.getId() == R.id.signInFragment || destination.getId() == R.id.registerFragment
                        || destination.getId() == R.id.showProduct || destination.getId() == R.id.buyFragment
                        || destination.getId() == R.id.favouritesFragment || destination.getId() == R.id.historyFragment
                        || destination.getId() == R.id.finalLogin) {
                    binding.bottomNavView.setVisibility(View.GONE);
                } else {
                    binding.bottomNavView.setVisibility(View.VISIBLE);
                }
            }
        });

        if (isLoggedIn()) {
            navController.navigate(R.id.homeFragment);
        }
    }
    @Override
    public void onBackPressed() {

        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.mobile_navigation)
                .getChildFragmentManager()
                .getFragments()
                .get(0);
        if (currentFragment instanceof HomeFragment ||
                currentFragment instanceof CartFragment ||
                currentFragment instanceof profileFragment) {//the fragment on which you want to handle your back press
            //Log.i("BACK PRESSED", "BACK PRESSED");
        }else{
            super.onBackPressed();
        }
    }


    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }
}
