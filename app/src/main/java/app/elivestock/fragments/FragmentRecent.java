package app.elivestock.fragments;

import static app.elivestock.utilities.Constant.GET_RECENT_PRODUCT;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.elivestock.Config;
import app.elivestock.R;
import app.elivestock.activities.ActivityProductDetail;
import app.elivestock.activities.MyApplication;
import app.elivestock.adapters.AdapterProduct;
import app.elivestock.models.Product;
import app.elivestock.utilities.ItemOffsetDecoration;
import app.elivestock.utilities.Utils;

public class FragmentRecent extends Fragment implements AdapterProduct.ContactsAdapterListener {

    SwipeRefreshLayout swipeRefreshLayout = null;
    LinearLayout lyt_root;
    private List<Product> productList;
    private AdapterProduct mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent, container, false);
        setHasOptionsMenu(true);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setRefreshing(true);

        lyt_root = view.findViewById(R.id.lyt_root);
        if (Config.ENABLE_RTL_MODE) {
            lyt_root.setRotationY(180);
        }

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        productList = new ArrayList<>();
        mAdapter = new AdapterProduct(getActivity(), productList, this);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(requireActivity(), R.dimen.item_offset);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        fetchData();
        onRefresh();

        return view;
    }

    private void onRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            productList.clear();
            new Handler().postDelayed(() -> {
                if (Utils.isNetworkAvailable(requireActivity())) {
                    swipeRefreshLayout.setRefreshing(false);
                    fetchData();
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getActivity(), getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                }

            }, 1500);
        });
    }

    private void fetchData() {
        @SuppressLint("NotifyDataSetChanged") JsonArrayRequest request = new JsonArrayRequest(GET_RECENT_PRODUCT, response -> {
            if (response == null) {
                Toast.makeText(getActivity(), getResources().getString(R.string.failed_fetch_data), Toast.LENGTH_LONG).show();
                return;
            }

            List<Product> items = new Gson().fromJson(response.toString(), new TypeToken<List<Product>>() {
            }.getType());

            // adding contacts to contacts list
            productList.clear();
            productList.addAll(items);

            // refreshing recycler view
            mAdapter.notifyDataSetChanged();

            swipeRefreshLayout.setRefreshing(false);
        }, error -> {
            // error in getting json
            Log.e("INFO", "Error: " + error.getMessage());
            Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        });

        MyApplication.getInstance().addToRequestQueue(request);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search, menu);

        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onContactSelected(Product product) {
        Intent intent = new Intent(getActivity(), ActivityProductDetail.class);
        intent.putExtra("product_id", product.getProduct_id());
        intent.putExtra("title", product.getProduct_name());
        intent.putExtra("image", product.getProduct_image());
        intent.putExtra("product_price", product.getProduct_price());
        intent.putExtra("product_description", product.getProduct_description());
        intent.putExtra("product_quantity", product.getProduct_quantity());
        intent.putExtra("product_status", product.getProduct_status());
        intent.putExtra("currency_code", product.getCurrency_code());
        intent.putExtra("category_name", product.getCategory_name());
        intent.putExtra("product_owner", product.getProduct_owner());
        intent.putExtra("product_owner_contact", product.getProduct_owner_contact());
        intent.putExtra("product_owner_address", product.getProduct_owner_address());
        startActivity(intent);
    }

}