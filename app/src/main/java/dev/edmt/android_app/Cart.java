package dev.edmt.android_app;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.edmt.android_app.Common.Common;
import dev.edmt.android_app.Database.Database;
import dev.edmt.android_app.ViewHolder.CartAdapter;
import dev.edmt.android_app.model.Order;
import dev.edmt.android_app.model.Request;


public class Cart extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    TextView txtTotalPrice;
    Button btnPlace;


    List<Order> cart= new ArrayList<>();
    CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
//firebase
        database =FirebaseDatabase.getInstance();
        requests= database.getReference("Requests");
//init
        recyclerView = (RecyclerView)findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        txtTotalPrice = (TextView)findViewById(R.id.total);
        btnPlace=(Button)findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cart.size()>0)
                    showAlerDialog();
                else
                    Toast.makeText(Cart.this,"Đơn hàng trống!!",Toast.LENGTH_SHORT).show();
            }
        });

                loadListFood();
    }

    private void showAlerDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Thêm bước nữa nhé :");
        alertDialog.setMessage("Bạn hãy nhập địa chỉ !");

        LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment,null);
        final MaterialEditText edtAddress = (MaterialEditText)order_address_comment.findViewById(R.id.edtAddress);
        final MaterialEditText edtComment = (MaterialEditText)order_address_comment.findViewById(R.id.edtComment);

       alertDialog.setView(order_address_comment);
       alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

       alertDialog.setPositiveButton("ĐỒNG Ý", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialogInterface, int i) {
               Request request = new Request(
                       Common.currentUser.getPhone(),
                       Common.currentUser.getName(),
                       edtAddress.getText().toString(),
                       txtTotalPrice.getText().toString(),
                       "0",
                       edtComment.getText().toString(),
                       cart

               );

               //submit to fire base

               requests.child(String.valueOf(System.currentTimeMillis()))
                       .setValue(request);
               // delete cart
               new Database(getBaseContext()).cleanCart();
               Toast.makeText(Cart.this,"CẢM ƠN, ĐẶT HÀNG THÀNH CÔNG",Toast.LENGTH_SHORT).show();
               finish();

           }
       });

       alertDialog.setNegativeButton("KHÔNG", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialogInterface, int i) {

               dialogInterface.dismiss();

           }
       });

       alertDialog.show();
    }

    private void loadListFood() {

        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);



        //ccl total price
        int total=0;
        for(Order order:cart)
            total+= (Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en","US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {
        cart.remove(position);
        new  Database(this).cleanCart();
        for(Order item:cart)
            new Database(this).addToCart(item);
        loadListFood();
    }
}
