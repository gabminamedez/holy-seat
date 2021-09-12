package com.mobdeve.s15.holyseat;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
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
import com.squareup.picasso.Picasso;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NearestToiletsActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    private final String TAG = "NearestToiletsActivity";
    private final String SOURCE_ID = "SOURCE_ID";
    private final String ICON_ID = "ICON_ID";
    private final String LAYER_ID = "LAYER_ID";

    private PermissionsManager permissionsManager;
    private MapboxMap globalMap;
    private Style globalStyle;
    private SupportMapFragment mapFragment;

    private ImageButton backButton;

    private String roomFilter = "All Rooms";
    private String toiletFilter = "All Toilets";
    private int ratingFilter = 0;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storage = FirebaseStorage.getInstance().getReference();
    private String profileRefString;

    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_nearest_toilets);
        loadMapFragment(savedInstanceState);

        this.sp = PreferenceManager.getDefaultSharedPreferences(this);
        profileRefString = sp.getString(ProfileActivity.PROFILE_KEY, "");


        backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @SuppressWarnings( {"MissingPermission"})
    public void loadMapFragment(Bundle savedInstanceState) {
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

                                        Location lastKnownLocation = mapboxMap.getLocationComponent().getLastKnownLocation();
                                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                                .target(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))
                                                .zoom(15)
                                                .build();
                                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                        mapboxMap.getUiSettings().setZoomGesturesEnabled(false);
                                        mapboxMap.getUiSettings().setScrollGesturesEnabled(false);
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
                                                                Dialog dialog = new Dialog(com.mobdeve.s15.holyseat.NearestToiletsActivity.this);
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
                                                                RatingBar ratingBar = dialog.findViewById(R.id.toiletMapRating);
                                                                ratingBar.setRating(document.getDouble("avgRating").floatValue());
                                                                Button toiletMapCheckin = dialog.findViewById(R.id.toiletMapCheckin);
                                                                Button toiletMapAddReview = dialog.findViewById(R.id.toiletMapAddReview);
                                                                Button toiletMapToilet = dialog.findViewById(R.id.toiletMapToilet);
                                                                ImageButton backMapButton = dialog.findViewById(R.id.backMapButton);
                                                                ImageView toiletMapImg = dialog.findViewById(R.id.toiletMapImg);

                                                                if (!document.getString("imageUri").isEmpty()){
                                                                    String path = "toilet_images/" + Uri.parse(document.getString("imageUri")).getLastPathSegment();
                                                                    storage.child(path).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Uri> task) {
                                                                            if (task.isSuccessful())
                                                                                Picasso.get()
                                                                                        .load(task.getResult())
                                                                                        .error(R.drawable.ic_error_foreground)
                                                                                        .into(toiletMapImg);
                                                                        }

                                                                    });
                                                                }

                                                                String checkInToiletId = document.getId();
                                                                String checkInToiletName = document.getString("location");

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
                                                                toiletMapCheckin.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        dialog.dismiss();

                                                                        DocumentReference profileRef = db.collection("Users").document(profileRefString);
                                                                        profileRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    DocumentSnapshot document = task.getResult();
                                                                                    if (document.exists()) {
                                                                                        User user = document.toObject(User.class);
                                                                                        Map<String, Object> checkin = new HashMap<>();
                                                                                        checkin.put("userID", db.document("Users/" + user.getId()));
                                                                                        checkin.put("userName", user.getDisplayName());
                                                                                        checkin.put("toiletID", db.document("Toilets/" + checkInToiletId));
                                                                                        checkin.put("toiletLocation", checkInToiletName);
                                                                                        checkin.put("checked", FieldValue.serverTimestamp());
                                                                                        db.collection("Check Ins")
                                                                                                .add(checkin)
                                                                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(DocumentReference documentReference) {
                                                                                                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());

                                                                                                        Dialog dialog2 = new Dialog(NearestToiletsActivity.this);
                                                                                                        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                                                                                        dialog2.setCancelable(true);
                                                                                                        dialog2.setContentView(R.layout.checkin_dialog);
                                                                                                        dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                                                                        Button btnCancel = dialog2.findViewById(R.id.btnCancel);
                                                                                                        Button btnCheckInAddReview = dialog2.findViewById(R.id.btnCheckinAddReview);
                                                                                                        dialog2.show();

                                                                                                        btnCancel.setOnClickListener(new View.OnClickListener() {
                                                                                                            @Override
                                                                                                            public void onClick(View v) {
                                                                                                                dialog2.dismiss();
                                                                                                            }
                                                                                                        });
                                                                                                        btnCheckInAddReview.setOnClickListener(new View.OnClickListener() {
                                                                                                            @Override
                                                                                                            public void onClick(View v) {
                                                                                                                Intent intent = new Intent(NearestToiletsActivity.this, ReviewAddActivity.class);
                                                                                                                intent.putExtra(ToiletActivity.TOILET_KEY, checkInToiletId);
                                                                                                                startActivity(intent);
                                                                                                                dialog2.dismiss();
                                                                                                            }
                                                                                                        });
                                                                                                    }
                                                                                                })
                                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                                    @Override
                                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                                        Log.w(TAG, "Error adding checkin", e);
                                                                                                    }
                                                                                                });
                                                                                        Log.d(TAG, "onComplete: done loading");
                                                                                    } else {
                                                                                        Log.d(TAG, "No such document");
                                                                                    }
                                                                                } else {
                                                                                    Log.d(TAG, "get failed with ", task.getException());
                                                                                }

                                                                                db.collection("Toilets").document(checkInToiletId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                                    @Override
                                                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                        DocumentReference toilet = documentSnapshot.getReference();
                                                                                        toilet.update("numCheckins", FieldValue.increment(1))
                                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(Void aVoid) {
                                                                                                        Log.d(TAG, "numReviews incremented.");
                                                                                                    }
                                                                                                })
                                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                                    @Override
                                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                                        Log.w(TAG, "Error updating document", e);
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                                toiletMapAddReview.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        dialog.dismiss();
                                                                        Intent intent = new Intent(NearestToiletsActivity.this, ReviewAddActivity.class);
                                                                        intent.putExtra(ToiletActivity.TOILET_KEY, document.getId());
                                                                        startActivity(intent);
                                                                    }
                                                                });
                                                                toiletMapToilet.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        dialog.dismiss();
                                                                        Intent intent = new Intent(NearestToiletsActivity.this, ToiletActivity.class);
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