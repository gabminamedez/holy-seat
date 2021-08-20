package com.mobdeve.s15.holyseat;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    private final String TAG = "MainActivity";
    private final String SOURCE_ID = "SOURCE_ID";
    private final String ICON_ID = "ICON_ID";
    private final String LAYER_ID = "LAYER_ID";

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private Toolbar menuToolbar;
    private Switch fragmentSwitch;

    private PermissionsManager permissionsManager;
    private MapboxMap globalMap;
    private Style globalStyle;

    private RecyclerView recyclerView;
    private ToiletListAdapter toiletListAdapter;
    private ArrayList<Toilet> toilets = new ArrayList<>();;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Boolean isMapView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navView = (NavigationView) findViewById(R.id.navView);
        menuToolbar = (Toolbar) findViewById(R.id.menuToolbar);

        setSupportActionBar(menuToolbar);
        getSupportActionBar().setTitle(null);
        navView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, menuToolbar, R.string.navDrawerOpen, R.string.navDrawerClose);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_profile:
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_logout:
                        Intent intent2 = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent2);
                        finish();
                        break;
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        switchFragment(savedInstanceState);

        fragmentSwitch = (Switch) findViewById(R.id.fragmentSwitch);
        fragmentSwitch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                isMapView = fragmentSwitch.isChecked();
                switchFragment(savedInstanceState);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //update UI
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    public void switchFragment(Bundle savedInstanceState) {
        retrieveToilets();

        if(isMapView) {
            ((Switch) findViewById(R.id.fragmentSwitch)).setText("Map View");
            SupportMapFragment mapFragment;
            if(savedInstanceState == null) {
                final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                MapboxMapOptions options = MapboxMapOptions.createFromAttributes(this, null);
                options.camera(new CameraPosition.Builder()
                        .target(new LatLng(14.594, 121.006))
                        .zoom(12)
                        .build());

                mapFragment = SupportMapFragment.newInstance(options);

                transaction.add(R.id.fragmentHolder, mapFragment, "com.mapbox.map");
                transaction.commit();
            }
            else {
                mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag("com.mapbox.map");
            }

            if(mapFragment != null) {
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull MapboxMap mapboxMap) {
                        globalMap = mapboxMap;

                        List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
                        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                                Point.fromLngLat(121.0096107326682, 14.604617512957176)));
                        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                                Point.fromLngLat(121.012356644117, 14.57854558434147)));

                        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_location_on_24, null);
                        Bitmap mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);

                        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/streets-v10")
                                .withImage(ICON_ID, mBitmap)
                                .withSource(new GeoJsonSource(SOURCE_ID,
                                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)))
                                .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                                        .withProperties(
                                                iconImage(ICON_ID),
                                                iconSize(2.0f),
                                                iconAllowOverlap(true),
                                                iconIgnorePlacement(true)
                                        )
                                ), new Style.OnStyleLoaded() {
                                @Override
                                public void onStyleLoaded(@NonNull Style style) {
                                    globalStyle = style;
                                    enableLocation(mapboxMap, style);
                                }
                        });

                        mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                            @Override
                            public boolean onMapClick(@NonNull LatLng point) {
                                double temp_lat = point.getLatitude();
                                double temp_lng = point.getLongitude();

                                // check db if may combination neto, retrieve accompanying data, and then flash modal

                                return true;
                            }
                        });
                    }
                });
            }
        }
        else {
            ((Switch) findViewById(R.id.fragmentSwitch)).setText("List View");
            ListFragment listFragment = new ListFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.fragmentHolder, listFragment, listFragment.getTag())
                    .commit();
        }
        for (int i = 0; i < toilets.size(); i++)
            Log.d(TAG, "onCreate: " + toilets.get(i).toString());
    }

    public void retrieveToilets() {
        toilets.clear();
        db.collection("Toilets").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        toilets.add(document.toObject(Toilet.class));
                    }
                    recyclerView = findViewById(R.id.toiletListRecycler);
                    toiletListAdapter = new ToiletListAdapter(toilets);
                    recyclerView.setAdapter(toiletListAdapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    @SuppressWarnings( {"MissingPermission"})
    public void enableLocation(MapboxMap mapboxMap, Style style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, style).build());
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
        }
        else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onExplanationNeeded(List<String> list) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocation(globalMap, globalStyle);
        }
        else {
            Toast.makeText(this, R.string.no_location, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {

    }

}