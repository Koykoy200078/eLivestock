package app.elivestock.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import app.elivestock.R;
import app.elivestock.models.Help;

public class AdapterHelp extends RecyclerView.Adapter<AdapterHelp.MyViewHolder> implements Filterable {

    private final List<Help> productList;
    private List<Help> productListFiltered;
    private final ContactsAdapterListener listener;

    public AdapterHelp(Context context, List<Help> productList, ContactsAdapterListener listener) {
        this.listener = listener;
        this.productList = productList;
        this.productListFiltered = productList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_help, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Help help = productListFiltered.get(position);
        holder.txt_title.setText(help.getTitle());
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
                    List<Help> filteredList = new ArrayList<>();
                    for (Help row : productList) {
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase())) {
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
                productListFiltered = (ArrayList<Help>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface ContactsAdapterListener {
        void onContactSelected(Help help);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_title;

        public MyViewHolder(View view) {
            super(view);
            txt_title = view.findViewById(R.id.title);

            view.setOnClickListener(view1 -> listener.onContactSelected(productListFiltered.get(getAdapterPosition())));
        }
    }
}
