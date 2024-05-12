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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gadgetzone.Models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> implements Filterable {
    private List<Product> mProductData;
    private final List<Product> mProductDataAll;
    private final Context mContext;
    private int lastPosition = -1;

    public ProductAdapter(Context context, List<Product> productData) {
        this.mProductData = productData;
        this.mContext = context;
        this.mProductDataAll = new ArrayList<>(productData);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.product_view, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product currentItem = mProductData.get(position);
        holder.bindTo(currentItem);

        if(holder.getAdapterPosition() > lastPosition) {
            holder.itemView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_in_bottom));
            lastPosition = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mProductData.size();
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

            if(charSequence == null || charSequence.length() == 0) {
                results.count = mProductDataAll.size();
                results.values = mProductDataAll;
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for(Product item : mProductDataAll) {
                    if(item.getName().toLowerCase().contains(filterPattern)){
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
            mProductData = (List<Product>)filterResults.values;
            notifyDataSetChanged();
        }
    };

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView mTitleText;
        TextView mInfoText;
        TextView mPriceText;
        ImageView mItemImage;

        ProductViewHolder(View itemView) {
            super(itemView);
            mTitleText = itemView.findViewById(R.id.itemTitle);
            mInfoText = itemView.findViewById(R.id.subTitle);
            mItemImage = itemView.findViewById(R.id.itemImage);
            mPriceText = itemView.findViewById(R.id.price);
        }

        void bindTo(Product currentItem){
            mTitleText.setText(currentItem.getName());
            mInfoText.setText(currentItem.getInformation());
            mPriceText.setText(String.valueOf(currentItem.getPrice()));

            Glide.with(itemView.getContext()).load(currentItem.getImageResource()).into(mItemImage);

            itemView.findViewById(R.id.add_to_cart).setOnClickListener(view -> ((WebshopActivity)itemView.getContext()).updateAlertIcon(currentItem));
            itemView.findViewById(R.id.delete).setOnClickListener(view -> ((WebshopActivity)itemView.getContext()).deleteItem(currentItem));
        }
    }
}