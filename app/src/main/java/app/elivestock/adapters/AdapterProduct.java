package app.elivestock.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.elivestock.Config;
import app.elivestock.R;
import app.elivestock.models.Product;

public class AdapterProduct extends RecyclerView.Adapter<AdapterProduct.MyViewHolder> implements Filterable {

    private final Context context;
    private final List<Product> productList;
    private List<Product> productListFiltered;
    private final ContactsAdapterListener listener;

    public AdapterProduct(Context context, List<Product> productList, ContactsAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        this.productList = productList;
        this.productListFiltered = productList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);

        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Product product = productListFiltered.get(position);
        holder.product_name.setText(product.getProduct_name());

        if (Config.ENABLE_DECIMAL_ROUNDING) {
            String price = String.format(Locale.ENGLISH, "%1$,.0f", product.getProduct_price());
            holder.product_price.setText(price + " " + product.getCurrency_code());
        } else {
            holder.product_price.setText(product.getProduct_price() + " " + product.getCurrency_code());
        }
        holder.product_owner.setText(" " + product.getProduct_owner());
        holder.product_quantity.setText(" " + product.getProduct_quantity());

        Transformation transformation = new RoundedTransformationBuilder()
                .cornerRadiusDp(6)
                .oval(false)
                .build();

        Picasso.with(context)
                .load(Config.ADMIN_PANEL_URL + "/upload/product/" + product.getProduct_image())
                .placeholder(R.drawable.ic_loading)
                .resize(250, 250)
                .centerCrop()
                .transform(transformation)
                .into(holder.product_image);

    }

    @Override
    public int getItemCount() {
        return productListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    productListFiltered = productList;
                } else {
                    List<Product> filteredList = new ArrayList<>();
                    for (Product row : productList) {
                        if (row.getProduct_name().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    productListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = productListFiltered;
                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                productListFiltered = (ArrayList<Product>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface ContactsAdapterListener {
        void onContactSelected(Product product);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView product_name, product_price, product_quantity;
        public TextView product_owner, product_owner_contact, product_owner_address;
        public ImageView product_image;

        public MyViewHolder(View view) {
            super(view);
            product_name = view.findViewById(R.id.product_name);
            product_owner = view.findViewById(R.id.product_owner);
            product_owner_contact = view.findViewById(R.id.product_owner_contact);
            product_owner_address = view.findViewById(R.id.product_owner_address);
            product_price = view.findViewById(R.id.product_price);
            product_quantity = view.findViewById(R.id.product_quantity);
            product_image = view.findViewById(R.id.category_image);

            view.setOnClickListener(view1 -> listener.onContactSelected(productListFiltered.get(getAdapterPosition())));
        }
    }
}
