package dev.edmt.android_app;

import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.EGLExt;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import dev.edmt.android_app.Common.Common;
import dev.edmt.android_app.Database.Database;
import dev.edmt.android_app.Interface.ItemClickListener;
import dev.edmt.android_app.Service.ListenOrder;
import dev.edmt.android_app.ViewHolder.MenuViewHolder;
import dev.edmt.android_app.model.Category;
import dev.edmt.android_app.model.Order;
import dmax.dialog.SpotsDialog;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    FirebaseDatabase database;
    DatabaseReference category;

    TextView txtFullName;

    RecyclerView recyler_menu;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Category,MenuViewHolder>adapter;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("DANH MỤC");
        setSupportActionBar(toolbar);


        //view swipe
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swip_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Common.isConnectedToInterner(getBaseContext()))
                    loadMenu();
                else
                    Toast.makeText(getBaseContext(),"Kiểm tra lại kết nối!!",Toast.LENGTH_SHORT).show();
                return;

            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if(Common.isConnectedToInterner(getBaseContext()))
                    loadMenu();
                else
                    Toast.makeText(getBaseContext(),"Kiểm tra lại kết nối!!",Toast.LENGTH_SHORT).show();
                return;
            }
        });

        //init FB

        database=FirebaseDatabase.getInstance();
        category=database.getReference("Category");

        FloatingActionButton fab =(FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent =new Intent( Home.this, Cart.class);
                startActivity(cartIntent); }

        });


                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
       ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
              this, drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //set Name
        View headerView = navigationView.getHeaderView(0);
        txtFullName = (TextView)headerView.findViewById(R.id.txtFullName);
        txtFullName.setText(Common.currentUser.getName());

        //Load menu
        recyler_menu = (RecyclerView)findViewById(R.id.recycler_menu);
        recyler_menu.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyler_menu.setLayoutManager(layoutManager);

        loadMenu();


        ///Register service
        Intent service = new Intent(Home.this, ListenOrder.class);
        startService(service);
    }

    private void loadMenu() {
            adapter = new FirebaseRecyclerAdapter<Category,MenuViewHolder>(Category.class,R.layout.menu_item,MenuViewHolder.class,category) {
            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, dev.edmt.android_app.model.Category model, int position) {

                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.imageView);
                final Category clickitem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //get category id
                        Intent foodList = new Intent(Home.this,FoodList.class);
                        //Because Category id is key
                        foodList.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(foodList);
                    }
                });
            }
        };
        recyler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {



        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {

        } else if (id == R.id.nav_Carts) {
            Intent cartIntent = new Intent(Home.this, Cart.class);
            startActivity(cartIntent);

        } else if (id == R.id.nav_orders) {
            Intent orderIntent = new Intent (Home.this, OrderStatus.class);
            startActivity(orderIntent);

        } else if (id == R.id.nav_Log_out) {

            Intent SignIn = new Intent(Home.this, Signin.class);
            SignIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(SignIn);

        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
