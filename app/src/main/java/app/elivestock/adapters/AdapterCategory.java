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

import app.elivestock.Config;
import app.elivestock.R;
import app.elivestock.models.Category;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.MyViewHolder> implements Filterable {

    private final Context context;
    private final List<Category> categoryList;
    private List<Category> categoryListFiltered;
    private final ContactsAdapterListener listener;

    public AdapterCategory(Context context, List<Category> categoryList, ContactsAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        this.categoryList = categoryList;
        this.categoryListFiltered = categoryList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);

        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Category category = categoryListFiltered.get(position);
        holder.category_name.setText(category.getCategory_name());

        Transformation transformation = new RoundedTransformationBuilder()
                .cornerRadiusDp(6)
                .oval(false)
                .build();

        Picasso.with(context)
                .load(Config.ADMIN_PANEL_URL + "/upload/category/" + category.getCategory_image())
                .placeholder(R.drawable.ic_loading)
                .resize(250, 250)
                .centerCrop()
                .transform(transformation)
                .into(holder.category_image);

    }

    @Override
    public int getItemCount() {
        return categoryListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    categoryListFiltered = categoryList;
                } else {
                    List<Category> filteredList = new ArrayList<>();
                    for (Category row : categoryList) {
                        if (row.getCategory_name().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    categoryListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = categoryListFiltered;
                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                categoryListFiltered = (ArrayList<Category>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface ContactsAdapterListener {
        void onContactSelected(Category category);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView category_name, product_count;
        public ImageView category_image;

        public MyViewHolder(View view) {
            super(view);
            category_name = view.findViewById(R.id.category_name);
            //product_count = view.findViewById(R.id.product_count);
            category_image = view.findViewById(R.id.category_image);

            view.setOnClickListener(view1 -> listener.onContactSelected(categoryListFiltered.get(getAdapterPosition())));
        }
    }
}
