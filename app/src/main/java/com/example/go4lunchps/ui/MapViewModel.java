package com.example.go4lunchps.ui;

import android.app.Application;
import android.location.Location;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.go4lunchps.R;
import com.example.go4lunchps.retrofit.NearByApi;
import com.example.go4lunchps.retrofit.models.NearByApiResponse;
import com.example.go4lunchps.Restaurant;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapViewModel extends AndroidViewModel {

    private final MutableLiveData<ArrayList<Restaurant>> restaurantMutableLiveData;
    NearByApi nearByApi = null;
    private int PROXIMITY_RADIUS = 2500;
    private Location lastKnownLocation;
    private final MutableLiveData<String> queryMutableLiveData;
    // The entry point to the Places API.
    private final PlacesClient placesClient;
    private final String TAG = "MapViewmodel";
    private final ArrayList<Restaurant> restaurantsList = new ArrayList<>();


    public MapViewModel(Application application) {
        super(application);
        restaurantMutableLiveData = new MutableLiveData<>();
        queryMutableLiveData = new MutableLiveData<>();

        // Construct a PlacesClient
        Places.initialize(getApplication().getApplicationContext(),getApplication().getResources().getString(R.string.key));
        placesClient = Places.createClient(getApplication().getApplicationContext());
    }

    public MutableLiveData<ArrayList<Restaurant>> getRestaurantMutableLiveData() {
        return restaurantMutableLiveData;
    }

    public void setQueryMutableLiveData(String query) {
        this.queryMutableLiveData.setValue(query);
    }

    public MutableLiveData<String> getQueryMutableLiveData() {
        return queryMutableLiveData;
    }

    public void findPlaces(Location lastKnownLocation){
        restaurantsList.clear();
        this.lastKnownLocation = lastKnownLocation;
        Call<NearByApiResponse> call = getApiService().getNearbyPlaces("restaurant", lastKnownLocation.getLatitude() + "," + lastKnownLocation.getLongitude(), PROXIMITY_RADIUS);
        call.enqueue(new Callback<NearByApiResponse>() {
            @Override
            public void onResponse(Call<NearByApiResponse> call, Response<NearByApiResponse> response) {
                try {
                    // This loop will go through all the results and add marker on each location.
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        Log.e("new restaurant", response.body().getResults().get(i).getName() + " " +response.body().getResults().get(i).getPlaceId());
                        String id = response.body().getResults().get(i).getPlaceId();
                        Restaurant restaurant = getDetails(id);
                        if(restaurant != null){
                            restaurantsList.add(restaurant);
                        }
                    }
                    restaurantMutableLiveData.setValue(restaurantsList);
                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<NearByApiResponse> call, Throwable t) {
                Log.d("onFailure", t.toString());
                t.printStackTrace();
                PROXIMITY_RADIUS += 4000;
            }
        });
    }

    public NearByApi getApiService() {
        if (nearByApi == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().retryOnConnectionFailure(true).readTimeout(80, TimeUnit.SECONDS).connectTimeout(80, TimeUnit.SECONDS).addInterceptor(interceptor).build();

            Retrofit retrofit = new Retrofit.Builder().baseUrl("https://maps.googleapis.com/maps/").addConverterFactory(getApiConvertorFactory()).client(client).build();

            nearByApi = retrofit.create(NearByApi.class);
            return nearByApi;
        } else {
            return nearByApi;
        }
    }

    private static GsonConverterFactory getApiConvertorFactory() {
        return GsonConverterFactory.create();
    }

    public void filterPlaces(){
        restaurantsList.clear();
        String string;
        if(lastKnownLocation == null){string = "null";}
        else{string = "OK";}
        Log.e("lastKnowLocation", "filterPlaces " + string);
        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        // Create a RectangularBounds object.
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(lastKnownLocation.getLatitude()-0.01, lastKnownLocation.getLongitude()+0.01),
                new LatLng(lastKnownLocation.getLatitude()+0.01, lastKnownLocation.getLongitude()-0.01));
        // Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                .setLocationBias(bounds)
                //.setLocationRestriction(bounds)
                .setOrigin(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()))
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token)
                .setQuery(queryMutableLiveData.getValue())
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                if(prediction.getDistanceMeters() <= 2500) {
                    //restaurantsList.add(getDetails(prediction.getPlaceId()));
                    }
            }
            restaurantMutableLiveData.setValue(restaurantsList);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
            }
        });

    }

    public void setLastKnownLocation(Location lastKnownLocation) {
        if(lastKnownLocation != null) {
            this.lastKnownLocation = new Location (lastKnownLocation);}
        String string;
        if(lastKnownLocation == null){string = "null";}
        else{string = "OK";}
        Log.e("lastKnowLocation", "setLastKnowLocation " + string);
    }

    public Restaurant getDetails(String id){
        Log.e("new restaurant", "getDetails");
        final Restaurant[] restaurant = new Restaurant[1];
        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
                Place.Field.NAME,
                Place.Field.TYPES,
                Place.Field.ADDRESS,
                Place.Field.BUSINESS_STATUS,
                Place.Field.LAT_LNG,
                Place.Field.OPENING_HOURS,
                Place.Field.PHONE_NUMBER,
                Place.Field.PHOTO_METADATAS,
                Place.Field.PRICE_LEVEL,
                Place.Field.RATING,
                Place.Field.USER_RATINGS_TOTAL,
                Place.Field.WEBSITE_URI);

        FetchPlaceRequest placeRrequest = FetchPlaceRequest.newInstance(id, placeFields);
        Place place = placesClient.fetchPlace(placeRrequest).getResult().getPlace();
        if(place != null){
            if(place.getTypes().contains(Place.Type.RESTAURANT)){
                Log.e("new restaurant", place.getName());
                return new Restaurant(place.getLatLng(),place.getName(),place.getAddress());
            }
        }
//        placesClient.fetchPlace(placeRrequest).addOnSuccessListener((placeResponse) -> {
//            if(place.getTypes().contains(Place.Type.RESTAURANT)){
//                Log.e("new restaurant", place.getName());
//                restaurant[0] = new Restaurant(place.getLatLng(),place.getName(),place.getAddress());
//            }
//        }).addOnFailureListener((exception) -> {
//            if (exception instanceof ApiException) {
//                final ApiException apiException = (ApiException) exception;
//                Log.e(TAG, "Place not found: " + exception.getMessage());
//                final int statusCode = apiException.getStatusCode();
//                // TODO: Handle error with given status code.
//            }
//        });
        return null;}
}