package com.example.shikh.ringr;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    AudioManager audioManager;
    Button select_contact;
    ArrayList<model> items;
    final int RQS_PICKCONTACT = 1;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    PermissionManager perMan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.mRecyclerView);
//        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        items = new ArrayList<>();
        mAdapter = new MyAdapter(items,this);
        mRecyclerView.setAdapter(mAdapter);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        select_contact = findViewById(R.id.select_contact);
        perMan = new PermissionManager(this);

        perMan.doWithPermission(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                new PermissionManager.OnPermissionResult() {
                    @Override
                    public void onGranted(String permission) {
//                      nothing to do
                    }

                    @Override
                    public void onDenied(String permission) {
                        Toast.makeText(MainActivity.this, "Permission not granted :(", Toast.LENGTH_LONG).show();
                    }
                });

        getFavoriteContacts();


        ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                ContentValues v = new ContentValues();
                v.put(ContactsContract.Contacts.STARRED, 0);
                getContentResolver().update(ContactsContract.Contacts.CONTENT_URI,
                        v,
                        ContactsContract.Contacts.DISPLAY_NAME + "=?",
                        new String[]{String.valueOf(items.get(viewHolder.getAdapterPosition()).getName())});
                items.remove(viewHolder.getAdapterPosition());
                mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        });

        swipeToDismissTouchHelper.attachToRecyclerView(mRecyclerView);


        final Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);


        select_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                perMan.doWithPermission(new String[]{Manifest.permission.READ_CONTACTS},
                        new PermissionManager.OnPermissionResult() {
                            @Override
                            public void onGranted(String permission) {
                                startActivityForResult(intent, RQS_PICKCONTACT);

                            }

                            @Override
                            public void onDenied(String permission) {
                                Toast.makeText(MainActivity.this, "Permission not granted :(", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

    public boolean check(String name) {
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < items.size(); ++i) {
            names.add(items.get(i).getName());
        }
        return names.contains(name);
    }

    public void getFavoriteContacts() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, ContactsContract.Contacts.STARRED + "=?", new String[]{"1"}, null);

        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

            if (hasPhoneNumber > 0 ) {
                Cursor c2 = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?",
                        new String[]{id},
                        null);
                while (c2.moveToNext()) {
                    String phoneNumber = c2.getString(c2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    model m = new model(name, phoneNumber);
                    if (!check(name)) {
                        items.add(m);
                        items.trimToSize();
                        mAdapter.setitems(items);
                    }

                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RQS_PICKCONTACT) {
                Uri returnUri = data.getData();
                Cursor cursor = getContentResolver().query(returnUri, null, null, null, null);

                if (cursor.moveToNext()) {
                    int columnIndex_ID = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                    String contactID = cursor.getString(columnIndex_ID);

                    int columnIndex_HASPHONENUMBER = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
                    int columnIndex_HASNAME = cursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID);
                    String stringHasPhoneNumber = cursor.getString(columnIndex_HASPHONENUMBER);
                    String stringHasName = cursor.getString(columnIndex_HASNAME);

                    if (stringHasPhoneNumber.equalsIgnoreCase("1")) {

                        Cursor cursorNum = getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactID,
                                null,
                                null);

                        if (cursorNum.moveToNext()) {
                            int columnIndex_number = cursorNum.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                            int columnIndex_name = cursorNum.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                            String stringNumber = cursorNum.getString(columnIndex_number);
                            String stringName = cursorNum.getString(columnIndex_name);
                            model m = new model(stringName, stringNumber);
                            ContentValues v = new ContentValues();
                            v.put(ContactsContract.Contacts.STARRED, 1);
                            getContentResolver().update(ContactsContract.Contacts.CONTENT_URI,
                                    v,
                                    ContactsContract.Contacts._ID + "=?",
                                    new String[]{String.valueOf(contactID)});
                            if (!check(stringName)) {
                                items.add(m);
                                items.trimToSize();
                                mAdapter.setitems(items);
                            }

                        }
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        perMan.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

