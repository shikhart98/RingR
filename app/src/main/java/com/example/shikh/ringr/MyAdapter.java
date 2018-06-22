package com.example.shikh.ringr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by shikh on 06-06-2018.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.Contact_ViewHolder> {

    private ArrayList<model> items = new ArrayList<>();
    private Context context ;

    MyAdapter(ArrayList<model> items, Context context) {
        this.items = items;
        this.context = context;
    }

    public void setitems(ArrayList<model> items) {
        this.items = items;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public Contact_ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(parent.getContext());
        return new Contact_ViewHolder(li.inflate(R.layout.item_contacts, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Contact_ViewHolder holder, int position) {
        holder.BindView(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    class Contact_ViewHolder extends RecyclerView.ViewHolder {
        TextView contact_name,contact_number;

        public Contact_ViewHolder(View itemView) {
            super(itemView);
            contact_name = itemView.findViewById(R.id.contact_name);
            contact_number = itemView.findViewById(R.id.contact_number);
        }

        public void BindView(model item) {
            String Name = item.name;
            String Number = item.number;
            contact_name.setText(Name);
            contact_number.setText("Number: "+Number);
        }
    }

}
