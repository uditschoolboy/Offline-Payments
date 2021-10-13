package com.example.ussdtest3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView amountView, receiverView, dateView, timeView;
        public ViewHolder(View view) {
                super(view);
                amountView = view.findViewById(R.id.amountView);
                receiverView = view.findViewById(R.id.receiverView);
                dateView = view.findViewById(R.id.dateView);
                timeView = view.findViewById(R.id.timeView);
        }
    }


    private List<Transaction> transactions;
    public TransactionsAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public void onBindViewHolder(TransactionsAdapter.ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        TextView amountView, receiverView, dateView, timeView;
        amountView = holder.amountView;
        receiverView = holder.receiverView;
        dateView = holder.dateView;
        timeView = holder.timeView;
        amountView.setText(transaction.getAmount());
        receiverView.setText(transaction.getReceiver());
        dateView.setText(transaction.getDate());
        timeView.setText(transaction.getTime());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.transaction_layout, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }
}
