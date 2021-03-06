package com.carteresto.igr230.carteresto.MenuDetail;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;

import android.util.Log;

import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.carteresto.igr230.carteresto.MenuPrincipale.ProductShowFragment;
import com.carteresto.igr230.carteresto.Model.MenuDuCarte;
import com.carteresto.igr230.carteresto.Model.SimpleProduct;
import com.carteresto.igr230.carteresto.R;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Affichier la presentation du menu du jour et les option associé à ce menu.
 **/
public class MenuDetailActivity extends AppCompatActivity implements NoteListener, ShowProductListener {

    static final String TAG = MenuDetailActivity.class.getSimpleName();
    @BindView(R.id.appbar)
    AppBarLayout appBarLayout;
    @BindView(R.id.menu_detail_menu_title)
    TextView menuNameView;
    @BindView(R.id.menu_detail_price)
    TextView priceView;
    @BindView(R.id.menu_detail_number_less)
    FloatingActionButton menuNumberLessBtn;
    @BindView(R.id.menu_detail_number)
    TextView menuNumberView;
    @BindView(R.id.menu_detail_number_more)
    FloatingActionButton menuNumberMoreBtn;
    @BindView(R.id.menu_detail_add_note)
    FloatingActionButton noteBtn;
    @BindView(R.id.menu_detail_validate_btn)
    FloatingActionButton validateBtn;
    @BindView(R.id.menu_detail_dishes_list)
    ExpandableListView dishesListView;
    // Validation button
    AlertDialog.Builder confirmDialogBuilder;
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.img_menu)
    ImageView imgMenu;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    // Left panel widgets
    private int lastQuantity = 0;
    private String curNote;
    // Dishes ListView
    private DishesListAdapter listAdapter;
    private List<String> listDataHeader;
    private Map<String, List<SimpleProduct>> listDataChild;
    private MenuViewModel viewModel;
    private FirebaseStorage storage;
    private String menuID;
    private boolean appBarExpanded;
    private Menu collapsedMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_detail);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        menuID = intent.getStringExtra("id");
        viewModel = ViewModelProviders.of(this).get(MenuViewModel.class);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_black_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //TODO
//        Intent intent = getIntent();
//        String id = intent.getStringExtra("type");
//        Log.d(TAG, "onCreate: MenuID:" + id);

    appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            //  Vertical offset == 0 indicates appBar is fully  expanded.
            if (Math.abs(verticalOffset) > 200) {
                appBarExpanded = false;
                invalidateOptionsMenu();
                validateBtn.hide();
            } else {
                appBarExpanded = true;
                validateBtn.show();
                invalidateOptionsMenu();
            }
        }
    });

        storage = FirebaseStorage.getInstance();

        viewModel.getMenu(menuID).observe(this, new Observer<MenuDuCarte>() {
            @Override
            public void onChanged(@Nullable MenuDuCarte menu) {
                if (menu == null) {
                    Log.e(TAG, "onChanged: Can not get menu info");
                    return;
                }
                Log.e(TAG, "onChanged: get menu info:" + menu);
                prepareMenuData(menu);

            }
        });

        viewModel.getMenuDishes(menuID).observe(this, new Observer<List<SimpleProduct>>() {
            @Override
            public void onChanged(@Nullable List<SimpleProduct> simpleProductList) {
                if (simpleProductList == null) {
                    Log.e(TAG, "onChanged: Can not get simpleProductList");
                    return;
                }
                listAdapter.updateList(simpleProductList);
            }
        });

//
//        listAdapter = new DishesListAdapter(this, listDataHeader, listDataChild);

        initeListData();
        listAdapter = new DishesListAdapter(this, listDataHeader, listDataChild);
        listAdapter.setClickListener(this);
        dishesListView.setAdapter(listAdapter);
        dishesListView.expandGroup(0);
        confirmDialogBuilder = new AlertDialog.Builder(this);
        confirmDialogBuilder.setPositiveButton(R.string.menu_detail_validate_accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MenuDetailActivity.this, R.string.menu_detail_validate_ok, Toast.LENGTH_LONG).show();
                viewModel.updateMenu(listAdapter.getListDataChild(), lastQuantity, curNote);
                finish();
            }
        });
        confirmDialogBuilder.setNegativeButton(R.string.menu_detail_validate_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // nothing special to do there
            }
        });
        confirmDialogBuilder.setTitle(R.string.menu_detail_validate_confirmation_title)
                .setMessage(R.string.menu_detail_validate_confirmation_message);
    }

    private void prepareMenuData(MenuDuCarte menu) {
        lastQuantity = menu.getQuantity();
        menuNameView.setText(menu.getName());
        menuNumberView.setText(String.valueOf(menu.getQuantity()));
        priceView.setText(menu.getPrice() + " €");
        description.setText(menu.getDescription());

        Glide.with(MenuDetailActivity.this)
                .using(new FirebaseImageLoader())
                .load(storage.getReferenceFromUrl("gs://carterestoandroid.appspot.com/product/" + menu.getId() +
                        "/" + menu.getId() + ".jpg"))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .fitCenter()
                .into(imgMenu);

    }

    private void initeListData() {
        listDataHeader = new ArrayList<String>();
        listDataHeader.add(getString(R.string.starters));
        listDataHeader.add(getString(R.string.main_dishes));
        listDataHeader.add(getString(R.string.deserts));
        List<SimpleProduct> starters = new ArrayList<SimpleProduct>();
        List<SimpleProduct> mainDishes = new ArrayList<SimpleProduct>();
        List<SimpleProduct> deserts = new ArrayList<SimpleProduct>();
        listDataChild = new HashMap<String, List<SimpleProduct>>();
        listDataChild.put(listDataHeader.get(0), starters);
        listDataChild.put(listDataHeader.get(1), mainDishes);
        listDataChild.put(listDataHeader.get(2), deserts);
        listDataChild.put(listDataHeader.get(0), starters);
        listDataChild.put(listDataHeader.get(1), mainDishes);
        listDataChild.put(listDataHeader.get(2), deserts);

        //Adding lunch steps :

//
//        // Adding child data : these are example to show how to add dishes to each category.

//        String[] startersTitlesList = getResources().getStringArray(R.array.menu_detail_starters_list);
//        Bitmap start1Bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.starter_saint_jacques);
//        SimpleProduct starter1 = new SimpleProduct(start1Bitmap, startersTitlesList[0], 0);
//        Bitmap start2Bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.starter_religieuse_tomates);
//        SimpleProduct starter2 = new SimpleProduct(start2Bitmap, startersTitlesList[1], 0);
//        starters.add(starter1);
//        starters.add(starter2);
//

//        String[] mainDishesTitlesList = getResources().getStringArray(R.array.menu_detail_main_dishes_list);
//        Bitmap dish1Bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.main_dish_taboule_quinoa);
//        SimpleProduct dish1 = new SimpleProduct(dish1Bitmap, mainDishesTitlesList[0], 0);
//        mainDishes.add(dish1);
//

//        String[] desertsTitlesList = getResources().getStringArray(R.array.menu_detail_deserts_list);
//        Bitmap desert1Bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.desert_pavlova);
//        SimpleProduct desert1 = new SimpleProduct(desert1Bitmap, desertsTitlesList[0], 0);
//        Bitmap desert2Bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.desert_tartelette);
//        SimpleProduct desert2 = new SimpleProduct(desert2Bitmap, desertsTitlesList[1], 0);
//        deserts.add(desert1);
//        deserts.add(desert2);


    }

    public int getMaxQuantity() {
        return lastQuantity;
    }

    public void showNoteDialog() {
        NoteDialog noteDialog = new NoteDialog(this);
        noteDialog.show(getFragmentManager(), getString(R.string.menu_detail_note_dialog_title));

    }

    public void setNote(String note) {
        this.curNote = note;
    }

    public String getCurNote() {
        return this.curNote;
    }


    @OnClick(R.id.menu_detail_number_less)
    public void onLess() {
        if (lastQuantity == 0) return;
        boolean success = listAdapter.decrementMenusNumber();
        if (success) {
            lastQuantity--;
            menuNumberView.setText(String.valueOf(lastQuantity));
        }
        listAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.menu_detail_number_more)
    public void onMore() {
        lastQuantity++;
        menuNumberView.setText(String.valueOf(lastQuantity));
        listAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.menu_detail_add_note)
    public void onNote() {
        showNoteDialog();
    }

    @OnClick(R.id.menu_detail_validate_btn)
    public void onValide() {
        if (lastQuantity == 0) {
            Snackbar.make(validateBtn, R.string.menu_detail_validate_no_menu_added,Snackbar.LENGTH_LONG).show();
            viewModel.updateMenu(listAdapter.getListDataChild(), lastQuantity, curNote);
            return;
        }

        if (listAdapter.isChoiceComplete()) {
            AlertDialog confirmDialog = confirmDialogBuilder.create();
            confirmDialog.show();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        collapsedMenu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (collapsedMenu != null
                && (!appBarExpanded || collapsedMenu.size() != 1)) {
            //collapsed
            collapsedMenu.add("Valide")
                    .setIcon(R.drawable.ic_check_black_24dp)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else {
            //expanded
        }
        return super.onPrepareOptionsMenu(collapsedMenu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_settings:
                return true;
        }
        if (item.getTitle() == "Valide") {
            onValide();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void showDialog(String id) {
        ProductShowFragment fragment = ProductShowFragment.newInstance(id, false);
        fragment.show(getSupportFragmentManager(), "prodct-" + id);
    }

    public void setStartersRemainingNumber(int n) {

    }
}
