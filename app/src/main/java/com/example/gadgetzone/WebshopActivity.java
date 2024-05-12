package com.example.gadgetzone;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gadgetzone.Models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Optional;

public class WebshopActivity extends AppCompatActivity {

    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;
    private RecyclerView recyclerView;
    private int cartItems = 0;
    private ProductAdapter productAdapter;
    private FrameLayout redCircle;
    private ArrayList<Product> mProductData;
    private boolean viewRow = true;
    private NotificationHelper mNotificationHelper;
    private TextView countTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webshop_product_list);

        mProductData = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        productAdapter = new ProductAdapter(this, mProductData);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(productAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mItems = mFirestore.collection("Items");
        mNotificationHelper = new NotificationHelper(this);

        queryData();
    }

    private void queryData() {
        mProductData.clear();
        mItems.orderBy("cartedCount", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        product.set_id(document.getId());
                        mProductData.add(product);
                    }

                    if (mProductData.isEmpty()) {
                        initializeData();
                    }

                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (mProductData.size() == 0) {
                        Log.d("WebshopActivity", "No products fetched, initializing data...");
                        initializeData();
                        productAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void initializeData() {
        String[] itemsList = getResources().getStringArray(R.array.shopping_item_names);
        String[] itemsPrice = getResources().getStringArray(R.array.shopping_item_price);
        String[] itemsInfo = getResources().getStringArray(R.array.shopping_item_desc);
        TypedArray itemsImageResources = getResources().obtainTypedArray(R.array.webshop_products);

        if (itemsList.length != itemsPrice.length || itemsList.length != itemsInfo.length || itemsList.length != itemsImageResources.length()) {
            return;
        }

        for (int i = 0; i < itemsList.length; i++) {
            String price = itemsPrice[i].replaceAll("[^0-9.]", "");
            if (price.isEmpty()) {
                price = "0";
            }

            mProductData.add(new Product(
                    itemsList[i],
                    Double.parseDouble(price),
                    itemsInfo[i],
                    0,
                    itemsImageResources.getResourceId(i, 0),
                    0));
        }

        itemsImageResources.recycle();
        productAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.webshop_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.log_out_button) {
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else if (itemId == R.id.settings_button) {
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else if (itemId == R.id.cart) {
            return true;
        } else if (itemId == R.id.view_selector) {
            if (viewRow) {
                changeSpanCount(item, R.drawable.ic_view_grid, 1);
            } else {
                changeSpanCount(item, R.drawable.ic_view_row, 2);
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void changeSpanCount(MenuItem item, int drawableId, int spanCount) {
        viewRow = !viewRow;
        item.setIcon(drawableId);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            ((GridLayoutManager) layoutManager).setSpanCount(spanCount);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.cart);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();

        redCircle = rootView.findViewById(R.id.view_alert_red_circle);
        countTextView = rootView.findViewById(R.id.view_alert_count_textview);

        rootView.setOnClickListener(v -> onOptionsItemSelected(alertMenuItem));
        return super.onPrepareOptionsMenu(menu);
    }

    public void updateAlertIcon(Product item) {
        String id = item.get_id();
        if (id != null) {
            cartItems++;
            countTextView.setText(cartItems > 0 ? String.valueOf(cartItems) : "");
            redCircle.setVisibility(cartItems > 0 ? VISIBLE : GONE);

            mItems.document(id).update("cartedCount", item.getCartedCount() + 1)
                    .addOnFailureListener(fail -> displayToast("Item " + id + " cannot be changed."));

            mNotificationHelper.send(item.getName());
            updateDataAndNotify();
        } else {
            Log.e("WebshopActivity", "Product ID is null");
        }
    }

    public void deleteItem(Product item) {
        String id = item.get_id();
        if (id != null) {
            DocumentReference ref = mItems.document(id);
            ref.delete()
                    .addOnSuccessListener(success -> displayToast("Item " + id + " deleted."))
                    .addOnFailureListener(fail -> displayToast("Item " + id + " cannot be deleted."));

            updateDataAndNotify();
            mNotificationHelper.cancel();
        } else {
            Log.e("WebshopActivity", "Product ID is null");
        }
    }

    private void updateDataAndNotify() {
        queryData();
    }

    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}