package com.example.gadgetzone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gadgetzone.Models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> implements Filterable {
    private List<Product> productData;
    private List<Product> productDataAll;
    private final Context productContext;
    private int lastPosition = -1;
    private boolean isButtonClicked = false;

    public ProductAdapter(Context context, List<Product> productData) {
        this.productData = productData;
        this.productContext = context;
        this.productDataAll = productData;
    }

    public void setButtonClicked(boolean isButtonClicked) {
        this.isButtonClicked = isButtonClicked;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(productContext).inflate(R.layout.activity_product_view,
                parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product currentProduct = productData.get(position);
        holder.bindTo(currentProduct, this);

        if (holder.getAdapterPosition() > lastPosition && !isButtonClicked) {
            holder.itemView.startAnimation(AnimationUtils.loadAnimation(productContext,
                    R.anim.logo_entrance_pop_in));
            lastPosition = holder.getAdapterPosition();
        }

        int price = (int) currentProduct.getPrice();
        String formattedPrice = String.format("%d $", price);
        holder.productPriceText.setText(formattedPrice);
        holder.quantityTextView.setText(String.format("On storage: %d piece", currentProduct.getQuantity()));

        isButtonClicked = false;
    }

    @Override
    public void onViewRecycled(@NonNull ProductViewHolder holder) {
        super.onViewRecycled(holder);
        holder.clearAnimation();
        lastPosition = -1;
    }

    @Override
    public int getItemCount() {
        return productData.size();
    }

    @Override
    public Filter getFilter() {
        return webshopFilter;
    }

    private final Filter webshopFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Product> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if (charSequence == null || charSequence.length() == 0) {
                results.count = productDataAll.size();
                results.values = productDataAll;
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (Product item : productDataAll) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }

                results.count = filteredList.size();
                results.values = filteredList;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            productData = (List<Product>) filterResults.values;

            Log.d("Filtering", "Filter results: " + filterResults.values);
            Log.d("Filtering", "Updated product data: " + productData);

            notifyDataSetChanged();
        }
    };

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productTitleText;
        TextView productInfoText;
        TextView productPriceText;
        ImageView productImage;
        TextView quantityTextView;

        ProductViewHolder(View itemView) {
            super(itemView);
            productTitleText = itemView.findViewById(R.id.itemTitle);
            productInfoText = itemView.findViewById(R.id.subTitle);
            productImage = itemView.findViewById(R.id.itemImage);
            productPriceText = itemView.findViewById(R.id.price);
            quantityTextView = itemView.findViewById(R.id.quantity);
        }


        void bindTo(Product currentItem, ProductAdapter adapter) {
            productTitleText.setText(currentItem.getName());

            String productInfo = "<b>Product Description:</b> " + "\n" + currentItem.getInformation();
            productInfoText.setText(android.text.Html.fromHtml(productInfo));

            productPriceText.setText(String.valueOf(currentItem.getPrice()));

            Glide.with(itemView.getContext()).load(currentItem.getProductImage()).into(productImage);

            itemView.findViewById(R.id.add_to_cart).setOnClickListener(view -> {
                if (currentItem.getQuantity() > 0) {
                    ((WebshopActivity) itemView.getContext()).updateAlertIcon(currentItem);
                    currentItem.setQuantity(currentItem.getQuantity() - 1);
                    quantityTextView.setText(String.format("On storage: %d piece", currentItem.getQuantity()));
                    adapter.setButtonClicked(true);
                } else {
                    Toast.makeText(itemView.getContext(), "This product is out of stock",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        void clearAnimation() {
            itemView.clearAnimation();
        }
    }
}