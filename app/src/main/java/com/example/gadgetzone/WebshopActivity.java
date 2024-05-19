package com.example.gadgetzone;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gadgetzone.Models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class WebshopActivity extends AppCompatActivity {

    private static final String LOG_TAG = RegisterActivity.class.getName();
    private FirebaseFirestore Firestore;
    private CollectionReference products;
    private RecyclerView recyclerView;
    private int cartItems = 0;
    private ProductAdapter productAdapter;
    private FrameLayout redCircle;
    private ArrayList<Product> productData;
    private TextView countTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webshop_product_list);

        productData = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        productAdapter = new ProductAdapter(this, productData);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(productAdapter);

        setSupportActionBar(findViewById(R.id.toolbar));

        Firestore = FirebaseFirestore.getInstance();
        products = Firestore.collection("products");

        queryData();
    }

    private void queryData() {
        productData.clear();
        products.orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        product.setId(document.getId());
                        productData.add(product);
                    }

                    if (productData.isEmpty()) {
                        initializeData();
                    }

                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (productData.isEmpty()) {
                        Log.d("WebshopActivity", "No products fetched, initializing data...");
                        initializeData();
                        productAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void initializeData() {
        String[] productList = getResources().getStringArray(R.array.webshop_product_names);
        String[] productPrice = getResources().getStringArray(R.array.webshop_product_price);
        String[] productQuantity = getResources().getStringArray(R.array.webshop_product_quantity);
        String[] productInfo = getResources().getStringArray(R.array.webshop_product_information);
        TypedArray productImageResources = getResources().obtainTypedArray(R.array.webshop_products);

        if (productList.length != productPrice.length || productList.length != productInfo.length ||
                productList.length != productImageResources.length() ||
                productList.length != productQuantity.length) {
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        for (int i = 0; i < productList.length; i++) {
            String price = productPrice[i].replaceAll("[^0-9.]", "");
            if (price.isEmpty()) {
                price = "0";
            }

            final int finalI = i;
            final String finalPrice = price;

            StorageReference imageRef = storageRef.child("images/" + productList[i] + ".jpg");

            int resourceId = productImageResources.getResourceId(i, 0);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);

            try {
                File tempFile = File.createTempFile("image", "jpg", getCacheDir());
                tempFile.deleteOnExit();

                try (FileOutputStream out = new FileOutputStream(tempFile)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                }

                Uri fileUri = Uri.fromFile(tempFile);

                imageRef.putFile(fileUri)
                        .continueWithTask(task -> {
                            if (!task.isSuccessful()) {
                                throw Objects.requireNonNull(task.getException());
                            }
                            return imageRef.getDownloadUrl();
                        })
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();

                                Product product = new Product(
                                        null,
                                        productList[finalI],
                                        Integer.parseInt(productQuantity[finalI]),
                                        productInfo[finalI],
                                        Double.parseDouble(finalPrice),
                                        downloadUri.toString()
                                );

                                DocumentReference newProductRef = products.document();
                                product.setId(newProductRef.getId());
                                newProductRef.set(product)
                                        .addOnSuccessListener(aVoid -> Log.d(LOG_TAG,
                                                "DocumentSnapshot added with ID: " + newProductRef.getId()))
                                        .addOnFailureListener(e -> Log.w(LOG_TAG,
                                                "Error adding document", e));
                            } else {
                                // Handle failures
                                Log.w(LOG_TAG, "Error getting download URL", task.getException());
                            }
                        });
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error creating temporary file", e);
            }
        }

        productImageResources.recycle();
        productAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_webshop, menu);
        MenuItem searchItem = menu.findItem(R.id.searchBar);
        SearchView searchView = (SearchView) searchItem.getActionView();

        MenuItem logoutItem = menu.findItem(R.id.logoutButton);
        MenuItem profileItem = menu.findItem(R.id.profileButton);
        logoutItem.setVisible(false);
        profileItem.setVisible(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                productAdapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }

    boolean isFiveMostExpensiveActive = false;
    boolean isOverHundredDollarsActive = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.logoutButton) {
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else if (itemId == R.id.profileButton) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.cart) {
            return true;
        } else if (itemId == R.id.fiveMostExpensive) {
            isFiveMostExpensiveActive = !isFiveMostExpensiveActive;
            fetchData();
            return true;
        } else if (itemId == R.id.overHundredDollars) {
            isOverHundredDollarsActive = !isOverHundredDollarsActive;
            fetchData();
            return true;
        } else if (itemId == R.id.emptyFilters) {
            isFiveMostExpensiveActive = false;
            isOverHundredDollarsActive = false;
            queryData();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void fetchData() {
        productData.clear();

        Query query;
        if (isFiveMostExpensiveActive) {
            query = products.orderBy("price", Query.Direction.DESCENDING).limit(5);
        } else if (isOverHundredDollarsActive) {
            query = products.whereGreaterThan("price", 300.00);
            query = query.orderBy("price", Query.Direction.ASCENDING);
        } else {
            return;
        }
        executeQuery(query);
    }

    private void executeQuery(Query query) {
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                runOnUiThread(() -> {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Product product = document.toObject(Product.class);
                        product.setId(document.getId());
                        productData.add(product);
                    }
                    productAdapter.notifyDataSetChanged();
                });
            } else {
                Log.w(LOG_TAG, "Error getting documents.", task.getException());
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem logoutItem = menu.findItem(R.id.logoutButton);
        MenuItem profileItem = menu.findItem(R.id.profileButton);

        boolean isLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        boolean isGuest = isLoggedIn && FirebaseAuth.getInstance().getCurrentUser().isAnonymous();

        if (isLoggedIn && !isGuest) {
            logoutItem.setVisible(true);
            profileItem.setVisible(true);
        } else {
            logoutItem.setVisible(false);
            profileItem.setVisible(false);
        }

        final MenuItem alertMenuItem = menu.findItem(R.id.cart);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();

        redCircle = rootView.findViewById(R.id.view_alert_red_circle);
        countTextView = rootView.findViewById(R.id.view_alert_count_textview);

        rootView.setOnClickListener(v -> onOptionsItemSelected(alertMenuItem));
        return true;
    }

    public void updateAlertIcon(Product item) {
        String id = item.getId();
        if (id != null) {
            cartItems++;
            countTextView.setText(cartItems > 0 ? String.valueOf(cartItems) : "");
            redCircle.setVisibility(cartItems > 0 ? VISIBLE : GONE);
        } else {
            Log.e("WebshopActivity", "Product ID is null");
        }
    }

}