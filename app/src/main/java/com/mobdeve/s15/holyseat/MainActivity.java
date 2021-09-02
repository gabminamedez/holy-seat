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

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
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


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
    private ImageButton filterButton;
    private FloatingActionButton nearestToiletsButton;

    private PermissionsManager permissionsManager;
    private MapboxMap globalMap;
    private Style globalStyle;

    private String roomFilter = "All Rooms";
    private String toiletFilter = "All Toilets";
    private int ratingFilter = 0;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String profileRefString;

    private Boolean isMapView = false;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        this.sp = PreferenceManager.getDefaultSharedPreferences(this);
        this.editor = sp.edit();

        profileRefString = sp.getString(ProfileActivity.PROFILE_KEY, "");
        if (mAuth.getCurrentUser() == null){
            Log.d(TAG, "onCreate: NO USER");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else if (profileRefString.isEmpty()){// user opening the app while logged in
            db.collection("Users")
                    .whereEqualTo("email", mAuth.getCurrentUser().getEmail())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    profileRefString = document.getId();
                                    editor.putString(ProfileActivity.PROFILE_KEY, profileRefString);
                                    editor.commit();
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
            Log.d(TAG, "onCreate: MAY USER " + mAuth.getCurrentUser().toString());
        }
        
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);

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
                        intent.putExtra(ProfileActivity.PROFILE_KEY, profileRefString);
                        startActivity(intent);
                        break;
                    case R.id.nav_logout:
                        mAuth.signOut();
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

        filterButton = (ImageButton) findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.filter_toilet_dialog);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                Spinner spinnerRoom = dialog.findViewById(R.id.spinnerRoom);
                spinnerRoom.setSelection(getRoomFilterId(roomFilter));
                Spinner spinnerToilet = dialog.findViewById(R.id.spinnerToilet);
                spinnerToilet.setSelection(getToiletFilterId(toiletFilter));
                SeekBar seekRating = dialog.findViewById(R.id.seekRating);
                seekRating.setProgress(ratingFilter);
                TextView curRating = dialog.findViewById(R.id.curRating);
                curRating.setText(Integer.toString((int) ratingFilter));
                Button btnCancel = dialog.findViewById(R.id.btnCancelFilter);
                Button btnApply = dialog.findViewById(R.id.btnApplyFilter);

                dialog.show();

                seekRating.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        curRating.setText(Integer.toString(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                btnApply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String roomType = spinnerRoom.getSelectedItem().toString();
                        String toiletType = spinnerToilet.getSelectedItem().toString();
                        int minRating = seekRating.getProgress();
                        roomFilter = roomType;
                        toiletFilter = toiletType;
                        ratingFilter = minRating;
                        switchFragment(savedInstanceState);
                        dialog.dismiss();
                    }
                });
            }
        });

        nearestToiletsButton = (FloatingActionButton) findViewById(R.id.nearestToiletsButton);
        nearestToiletsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NearestToiletsActivity.class);
                startActivity(intent);
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

    @SuppressWarnings( {"MissingPermission"})
    public void switchFragment(Bundle savedInstanceState) {
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
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("Toilets").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()){
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        double lng = (Double) document.get("longitude");
                                        double lat = (Double) document.get("latitude");
                                        if(document.getDouble("avgRating") >= ratingFilter) {
                                            if(roomFilter.equals("All Rooms") && toiletFilter.equals("All Toilets")) {
                                                symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(lng, lat)));
                                            }
                                            else if(!roomFilter.equals("All Rooms") && toiletFilter.equals("All Toilets")) {
                                                if(roomFilter.equals(document.get("roomType"))) {
                                                    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(lng, lat)));
                                                }
                                            }
                                            else if(roomFilter.equals("All Rooms") && !toiletFilter.equals("All Toilets")) {
                                                if(toiletFilter.equals(document.get("toiletType"))) {
                                                    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(lng, lat)));
                                                }
                                            }
                                            else if(!roomFilter.equals("All Rooms") && !toiletFilter.equals("All Toilets")) {
                                                if(roomFilter.equals(document.get("roomType")) && toiletFilter.equals(document.get("toiletType"))) {
                                                    symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(lng, lat)));
                                                }
                                            }
                                        }
                                    }

                                    Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_location_on_24, null);
                                    Bitmap mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
                                    GeoJsonSource source = new GeoJsonSource(SOURCE_ID, FeatureCollection.fromFeatures(symbolLayerIconFeatureList));
                                    SymbolLayer symbolLayer = new SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(
                                            iconImage(ICON_ID),
                                            iconSize(2.0f),
                                            iconAllowOverlap(true),
                                            iconIgnorePlacement(true)
                                    );

                                    mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/streets-v10")
                                            .withImage(ICON_ID, mBitmap)
                                            .withSource(source)
                                            .withLayer(symbolLayer), new Style.OnStyleLoaded() {
                                            @Override
                                            public void onStyleLoaded(@NonNull Style style) {
                                                globalStyle = style;
                                                enableLocation(mapboxMap, style);
                                            }
                                    });

                                    mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                                        @Override
                                        public boolean onMapClick(@NonNull LatLng point) {
                                            PointF screenPoint = mapboxMap.getProjection().toScreenLocation(point);
                                            List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, LAYER_ID);
                                            if (!features.isEmpty()) {
                                                Feature feature = features.get(0);

                                                String geoJsonString = feature.geometry().toJson();
                                                try {
                                                    JSONObject obj = new JSONObject(geoJsonString);
                                                    JSONArray cJson = obj.getJSONArray("coordinates");
                                                    List<Double> coordinates = new ArrayList<Double>();
                                                    for(int i = 0; i < cJson.length(); i++){
                                                        coordinates.add(cJson.getDouble(i));
                                                    }

                                                    Double marginOfError = 0.00001;
                                                    Double lngLower = coordinates.get(0) - marginOfError;
                                                    Double lngUpper = coordinates.get(0) + marginOfError;
                                                    Double latLower = coordinates.get(1) - marginOfError;
                                                    Double latUpper = coordinates.get(1) + marginOfError;

                                                    CollectionReference toiletsRef = db.collection("Toilets");
                                                    toiletsRef.whereLessThanOrEqualTo("longitude", lngUpper).whereGreaterThanOrEqualTo("longitude", lngLower);
                                                    toiletsRef.whereLessThanOrEqualTo("latitude", latUpper).whereGreaterThanOrEqualTo("latitude", latLower);

                                                    toiletsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                                    Dialog dialog = new Dialog(MainActivity.this);
                                                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                                                    dialog.setCancelable(true);
                                                                    dialog.setContentView(R.layout.toilet_map_detail_dialog);
                                                                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                                    TextView toiletMapName = dialog.findViewById(R.id.toiletMapName);
                                                                    toiletMapName.setText(document.getString("location"));
                                                                    TextView toiletMapRoom = dialog.findViewById(R.id.toiletMapRoom);
                                                                    toiletMapRoom.setText(document.getString("roomType"));
                                                                    TextView toiletMapType = dialog.findViewById(R.id.toiletMapType);
                                                                    toiletMapType.setText(document.getString("toiletType"));
                                                                    RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
                                                                    ratingBar.setRating(document.getDouble("avgRating").floatValue());
                                                                    Button toiletMapCheckin = dialog.findViewById(R.id.toiletMapCheckin);
                                                                    Button toiletMapAddReview = dialog.findViewById(R.id.toiletMapAddReview);
                                                                    Button toiletMapToilet = dialog.findViewById(R.id.toiletMapToilet);
                                                                    ImageButton backMapButton = dialog.findViewById(R.id.backMapButton);

                                                                    dialog.show();

                                                                    backMapButton.setOnClickListener(new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {
                                                                            dialog.dismiss();
                                                                        }
                                                                    });
                                                                    toiletMapCheckin.setOnClickListener(new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {
                                                                            dialog.dismiss();
                                                                        }
                                                                    });
                                                                    toiletMapAddReview.setOnClickListener(new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {
                                                                            dialog.dismiss();
                                                                            Intent intent = new Intent(MainActivity.this, ReviewAddActivity.class);
                                                                            intent.putExtra(ToiletActivity.TOILET_KEY, document.getId());
                                                                            startActivity(intent);
                                                                        }
                                                                    });
                                                                    toiletMapToilet.setOnClickListener(new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {
                                                                            dialog.dismiss();
                                                                            Intent intent = new Intent(MainActivity.this, ToiletActivity.class);
                                                                            intent.putExtra(ToiletActivity.TOILET_KEY, document.getId());
                                                                            startActivity(intent);
                                                                        }
                                                                    });

                                                                    break;
                                                                }
                                                            }
                                                            else {
                                                                Log.d(TAG, "Error getting documents: ", task.getException());
                                                            }
                                                        }
                                                    });
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            return true;
                                        }
                                    });
                                }
                                else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
                    }
                });
            }
        }
        else {
            ((Switch) findViewById(R.id.fragmentSwitch)).setText("List View");
            ListFragment listFragment = new ListFragment(roomFilter, toiletFilter, ratingFilter);
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.fragmentHolder, listFragment, listFragment.getTag())
                    .commit();
        }
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

    public int getRoomFilterId(String roomString) {
        int id = 0;
        switch(roomString) {
            case "All Rooms": id = 0; break;
            case "Female": id = 1; break;
            case "Male": id = 2; break;
            case "Ungendered": id = 3; break;
        }
        return id;
    }

    public int getToiletFilterId(String toiletString) {
        int id = 0;
        switch(toiletString) {
            case "All Toilets": id = 0; break;
            case "Flushing Toilet": id = 1; break;
            case "One-Piece Toilet": id = 2; break;
            case "Two-Piece Toilet": id = 3; break;
            case "Upflush Toilet": id = 4; break;
            case "Small Compact Toilet": id = 5; break;
            case "Corner Toilet": id = 6; break;
            case "Wall Mounted Toilet": id = 7; break;
            case "Square Toilet": id = 8; break;
            case "Elongated Toilet": id = 9; break;
            case "Round Bowl Toilet": id = 10; break;
            case "Tankless Toilet": id = 11; break;
            case "Composting Toilet": id = 12; break;
            case "Portable Toilet": id = 13; break;
            case "Pressure-Assisted Toilet": id = 14; break;
            case "Gravity Toilet": id = 15; break;
            case "Touchless Toilet": id = 16; break;
            case "Pull Chain Toilet": id = 17; break;
            case "Water-Saving Toilet": id = 18; break;
            case "Dual-Flush Toilet": id = 19; break;
            case "Comfort Height Toilet": id = 20; break;
            case "Expensive Toilet": id = 21; break;
        }
        return id;
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