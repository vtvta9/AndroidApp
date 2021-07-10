package dev.edmt.android_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import dev.edmt.android_app.Database.Database;
import dev.edmt.android_app.Interface.ItemClickListener;
import dev.edmt.android_app.ViewHolder.FoodViewHolder;
import dev.edmt.android_app.model.Food;

public class FoodList extends AppCompatActivity {

        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;

        FirebaseDatabase database;
        DatabaseReference foodList;

        String categoryId="";

        FirebaseRecyclerAdapter<Food,FoodViewHolder>adapter;

        //Search Functionalily
        FirebaseRecyclerAdapter<Food,FoodViewHolder>searchAdapter;
        List<String> suggestList = new ArrayList<>();
        MaterialSearchBar materialSearchBar;

        //Favorites
        Database localDB;

        //fb share
        CallbackManager callbackManager;
        ShareDialog shareDialog;
        SwipeRefreshLayout swipeRefreshLayout;

        //create target
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                //create photo from bitmap
                SharePhoto photo = new SharePhoto.Builder().setBitmap(bitmap).build();
                if(ShareDialog.canShow(SharePhotoContent.class))
                {
                    SharePhotoContent content = new SharePhotoContent.Builder().addPhoto(photo).build();
                    shareDialog.show(content);
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        //init fb
        callbackManager = CallbackManager.Factory.create();
        shareDialog= new ShareDialog(this);

        //firebase
        database =FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");
        // Local Database
        localDB = new Database(this);
        //swipe
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swip_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(getIntent() != null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if(!categoryId.isEmpty() && categoryId != null){

                    loadListFood(categoryId);

                }
            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if(getIntent() != null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if(!categoryId.isEmpty() && categoryId != null){

                    loadListFood(categoryId);

                }
            }
        });

        recyclerView=(RecyclerView)findViewById(R.id.recycler_food);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //get intent here
        if(getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryId");
        if(!categoryId.isEmpty() && categoryId != null){

            loadListFood(categoryId);

        }
        //search
        materialSearchBar =(MaterialSearchBar)findViewById(R.id.searchBar);
        materialSearchBar.setHint("NHẬP TÊN MÓN");

        loadSuggest();
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //when  user  type their text, we will change suggest list

                List<String> suggest = new ArrayList<String>();
                for (String search : suggestList) {

                    if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }

                    materialSearchBar.setLastSuggestions(suggest);
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                // when search bar is close
                //restore original suggest adapter
                if(!enabled){
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });
    }

    private void startSearch(CharSequence text) {
        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("name").equalTo(text.toString())
        ) {
            @Override
            protected void populateViewHolder(final FoodViewHolder viewHolder, final Food model, final int position) {
                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);


                // add favorites
                if (localDB.isFavorites(adapter.getRef(position).getKey()))
                    viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                // click to change state of favorites
                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!localDB.isFavorites(adapter.getRef(position).getKey()))
                        {
                            localDB.addToFavorites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(FoodList.this,"" + model.getName() + "đã thêm vào yêu thích",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            localDB.removeFromFavorites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(FoodList.this,"" + model.getName() + "đã hủy yêu thích món ăn",Toast.LENGTH_SHORT).show();
                        }
                    }
                });




                final Food local =model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId",searchAdapter.getRef(position).getKey()); //send food id to new activity
                        startActivity(foodDetail);
                    }
                });

            }
        };
            recyclerView.setAdapter(searchAdapter); //set adapter for recycle view  is search result
    }

    private void loadSuggest() {
        foodList.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                            Food item = postSnapshot.getValue(Food.class);
                            suggestList.add(item.getName()); /// add name of food  to suggest list

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadListFood(String categoryId) {

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,R.layout.food_item,FoodViewHolder.class,
                foodList.orderByChild("menuId").equalTo(categoryId)) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, final Food model, int position) {

                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);

                //click to share
                viewHolder.share_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso.with(getApplicationContext())
                                .load(model.getImage())
                                .into(target);
                    }
                });

                final Food local =model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId",adapter.getRef(position).getKey()); //send food id to new activity
                        startActivity(foodDetail);
                    }
                });

            }
        };

        //set adapter

        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

}
