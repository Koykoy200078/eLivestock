package app.elivestock.activities;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import app.elivestock.Config;
import app.elivestock.R;
import app.elivestock.adapters.AdapterHistory;
import app.elivestock.models.History;
import app.elivestock.utilities.DBHelper;
import app.elivestock.utilities.MyDividerItemDecoration;
import app.elivestock.utilities.Utils;

public class ActivityHistory extends AppCompatActivity {

    public static ArrayList<Integer> id = new ArrayList<>();
    public static ArrayList<String> code = new ArrayList<>();
    public static ArrayList<String> order_list = new ArrayList<>();
    public static ArrayList<String> order_total = new ArrayList<>();
    public static ArrayList<String> date_time = new ArrayList<>();
    final int CLEAR_ALL_ORDER = 0;
    final int CLEAR_ONE_ORDER = 1;
    RecyclerView recyclerView;
    View lyt_empty_history;
    RelativeLayout lyt_history;
    DBHelper dbhelper;
    AdapterHistory adapterHistory;
    int FLAG;
    int ID;
    ArrayList<ArrayList<Object>> data;
    List<History> arrayItemHistory;
    View view, bottom_sheet;
    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        view = findViewById(android.R.id.content);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_history);
        }

        bottom_sheet = findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehavior.from(bottom_sheet);

        recyclerView = findViewById(R.id.recycler_view);
        lyt_empty_history = findViewById(R.id.lyt_empty_history);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new MyDividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL, 0));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        lyt_history = findViewById(R.id.lyt_history);

        adapterHistory = new AdapterHistory(this, arrayItemHistory);
        dbhelper = new DBHelper(this);

        dbhelper.openDataBase();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                new Handler().postDelayed(() -> showBottomSheetDialog(position), 400);
            }

            @Override
            public void onLongClick(View view, final int position) {
                new Handler().postDelayed(() -> showClearDialog(CLEAR_ONE_ORDER, id.get(position)), 400);
            }
        }));

        new getDataTask().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            case R.id.clear:
                if (id.size() > 0) {
                    showClearDialog(CLEAR_ALL_ORDER, 1111);
                } else {
                    Snackbar.make(view, R.string.msg_no_history, Snackbar.LENGTH_SHORT).show();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showClearDialog(int flag, int id) {
        FLAG = flag;
        ID = id;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm);
        switch (FLAG) {
            case 0:
                builder.setMessage(getString(R.string.clear_all_order));
                break;
            case 1:
                builder.setMessage(getString(R.string.clear_one_order));
                break;
        }
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.dialog_option_yes), (dialog, which) -> {
            switch (FLAG) {
                case 0:
                    dbhelper.deleteAllDataHistory();
                    clearData();
                    new getDataTask().execute();
                    break;
                case 1:
                    dbhelper.deleteDataHistory(ID);
                    clearData();
                    new getDataTask().execute();
                    break;
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.dialog_option_no), (dialog, which) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();

    }

    public void clearData() {
        id.clear();
        code.clear();
        order_list.clear();
        order_total.clear();
        date_time.clear();
    }

    public void getDataFromDatabase() {

        clearData();
        data = dbhelper.getAllDataHistory();

        for (int i = 0; i < data.size(); i++) {
            ArrayList<Object> row = data.get(i);
            id.add(Integer.parseInt(row.get(0).toString()));
            code.add(row.get(1).toString());
            order_list.add(row.get(2).toString());
            order_total.add(row.get(3).toString());
            date_time.add(row.get(4).toString());
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void showBottomSheetDialog(final int position) {
        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        final View view = getLayoutInflater().inflate(R.layout.item_bottom_sheet, null);
        ((TextView) view.findViewById(R.id.sheet_code)).setText(code.get(position));
        ((TextView) view.findViewById(R.id.sheet_date)).setText(Utils.getFormatedDate(date_time.get(position)));
        ((TextView) view.findViewById(R.id.sheet_order_list)).setText(order_list.get(position));
        ((TextView) view.findViewById(R.id.sheet_order_total)).setText(order_total.get(position));

        view.findViewById(R.id.img_copy).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Order Id", code.get(position));
            clipboard.setPrimaryClip(clip);
            Toast.makeText(ActivityHistory.this, R.string.msg_copy, Toast.LENGTH_SHORT).show();
        });

        mBottomSheetDialog = new BottomSheetDialog(this);
        mBottomSheetDialog.setContentView(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    @SuppressWarnings("deprecation")
    public class getDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            getDataFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (id.size() > 0) {
                lyt_history.setVisibility(View.VISIBLE);
                recyclerView.setAdapter(adapterHistory);
            } else {
                lyt_empty_history.setVisibility(View.VISIBLE);
            }
        }
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private final GestureDetector gestureDetector;
        private final ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {

            this.clickListener = clickListener;

            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, MotionEvent e) {
            View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }

            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

}