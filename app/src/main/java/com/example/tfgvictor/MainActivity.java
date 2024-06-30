package com.example.tfgvictor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.tfgvictor.Fragments.GastosHogarFragment;
import com.example.tfgvictor.Fragments.ListaCompraFragment;
import com.example.tfgvictor.Fragments.PrincipalFragment;
import com.example.tfgvictor.Fragments.TareasHogarFragment;
import com.example.tfgvictor.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;
    private PrincipalFragment principalFragment;
    private ListaCompraFragment listaCompraFragment;
    private TareasHogarFragment tareasHogarFragment;
    private GastosHogarFragment gastosHogarFragment;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        principalFragment = new PrincipalFragment();
        listaCompraFragment = new ListaCompraFragment();
        tareasHogarFragment = new TareasHogarFragment();
        gastosHogarFragment = new GastosHogarFragment();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        frameLayout = findViewById(R.id.frameLayout);

        PrincipalFragment principalFragment1 = new PrincipalFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, principalFragment1).commit();


        auth = FirebaseAuth.getInstance();

        //Metodo para el boton seleccionado
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.principal:
                        setFragment(principalFragment);
                        return true;
                    case R.id.listaCompra:
                        setFragment(listaCompraFragment);
                        return true;
                    case R.id.tareasHogar:
                        setFragment(tareasHogarFragment);
                        return true;
                    case R.id.gastosHogar:
                        setFragment(gastosHogarFragment);
                        return true;
                }

                return false;
            }
        });


    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commitAllowingStateLoss();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Logout:
                auth.signOut();
                startActivity(new Intent(MainActivity.this, LoginPrincipalActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putInt("selectedFragment", bottomNavigationView.getSelectedItemId());
    }


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int selectedFragmentId = savedInstanceState.getInt("selectedFragment");
        bottomNavigationView.setSelectedItemId(selectedFragmentId);
    }
}