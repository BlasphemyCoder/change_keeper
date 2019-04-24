package com.example.changekeeper;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class RegLoanScreen extends AppCompatActivity implements AdapterView.OnItemSelectedListener, ConfirmDialogue.ConfirmDialogListener{

    private static final String TAG = "RegLoan";
    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private int typeFlag;
    private ArrayAdapter<String> destinationAdapter;


    //To save:
    //Amount
    private double amount;

    //Destination (card or wallet);
    private String destination;

    //From
    private String person;

    //Date
    private String date;

    //Description
    private String description;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        ActionBar toolbar = getSupportActionBar();

        setContentView(R.layout.activity_reg_loan_screen);


        switch(message){
            case "BORROW":
                this.typeFlag = 0;
                ((TextView)findViewById(R.id.typeText)).setText("Borrow");

                break;

            case "LEND":
                this.typeFlag = 1;
                ((TextView)findViewById(R.id.typeText)).setText("Lend");

                break;

            default:
                Log.v(TAG,"wtf erro :D");
        }

        Log.v(TAG,"OLAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

        //Date
        mDisplayDate = (TextView) findViewById(R.id.datePicker);
        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        RegLoanScreen.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month+1; //We do this cus by default January = 0
                String date = day+"/"+month+"/"+year;
                mDisplayDate.setText(date);
            }
        };


        //Destination Dropwdown
        buildDestinationSpinner();

    }

    private void buildDestinationSpinner(){
        String[] items = {"WALLET","CARD"};
        Spinner spinner = findViewById(R.id.destination);
        spinner.setOnItemSelectedListener(this);

        this.destinationAdapter= new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(this.destinationAdapter);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
         this.destination = parent.getSelectedItem().toString();



    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    public void errorCheck(View view){
        boolean valid = true;
        //Check amount

        if (((TextView)findViewById(R.id.editAmount)).getText().length() == 0){
            valid = false;
        }

        //Check person
        if (((TextView)findViewById(R.id.fromInput)).getText().length() == 0){
            valid = false;
        }

        //Check date
        if (((TextView)findViewById(R.id.datePicker)).getText().length() == 0){
            valid = false;
        }

        if (((TextView)findViewById(R.id.descriptionInput)).getText().length() == 0){
            this.description = "";
        }


        if (valid == false){
            /*TO DO: ADD MESSAGE TELLING THE USER TO FILL ALL FIELDS*/
        }else{
            callConfirm();
        }

    }

    private void callConfirm(){
        Bundle args = new Bundle();
        args.putString("amount",((TextView)findViewById(R.id.editAmount)).getText().toString());
        switch(this.typeFlag){
            case 0:
                args.putString("type","INCOME");
                break;

            case 1:
                args.putString("type","EXPENSE");
                break;

            default:
                Log.v(TAG,"wtf erro :D");
        }
        switch(this.destination){
            case "WALLET":
                args.putString("dest","WALLET");
                break;

            case "CARD":
                args.putString("dest","CARD");
                break;

            default:
                Log.v(TAG,"wtf erro :D");
        }

        ConfirmDialogue confirmDialogue = new ConfirmDialogue();
        confirmDialogue.setArguments(args);
        confirmDialogue.show(getSupportFragmentManager(), "Confirm Dialogue");
    }

    @Override
    public void confirm() {
        registerLoan();
    }

    private void registerLoan(){
        Intent intent = new Intent(this, LoanScreen.class);
        EditText editText = (EditText) findViewById(R.id.editAmount);

        this.amount = Double.parseDouble(editText.getText().toString());
        this.date = ((TextView)findViewById(R.id.datePicker)).getText().toString();
        this.person = ((TextView)findViewById(R.id.fromInput)).getText().toString();
        this.description = ((TextView)findViewById(R.id.descriptionInput)).getText().toString();

        //Register
        writeFile();
        Calendar cal = Calendar.getInstance();

        if(cal.get(Calendar.DAY_OF_MONTH) == Integer.parseInt(this.date.split("/")[0]) && (cal.get(Calendar.MONTH)+1) == Integer.parseInt(this.date.split("/")[1]) && cal.get(Calendar.YEAR) == Integer.parseInt(this.date.split("/")[2]))
            updateWallet();

        Toast toast = Toast.makeText(this,"Loan Registered Successfully", Toast.LENGTH_SHORT);

        toast.show();
        startActivity(intent);
    }

    private void writeFile(){
        boolean found = false;

        String fileName = "";
        switch(this.typeFlag){
            case 0:
               fileName = "UserBorrows";
                break;

            case 1:
                fileName = "UserLends";
                break;

            default:
                Log.v(TAG,"wtf erro :D");
        }

        for(String i : fileList()){
            Log.v(TAG,i+" ------------------------");
            if(i.equals(fileName+".txt")){
                found = true;
                break;
            }
        }

        try{
            FileOutputStream fileOutputStream;
            if(!found) {
                fileOutputStream = openFileOutput(fileName+".txt", MODE_PRIVATE);
            }else{
                fileOutputStream = openFileOutput(fileName+".txt", MODE_APPEND);
            }

            //Register Template: WALLET/CARD - Amount - Person - Date - Description

            StringBuilder register = new StringBuilder();
            register.append(this.destination);
            register.append("-");
            register.append(this.amount);
            register.append("-");
            register.append(this.person);
            register.append("-");
            register.append(this.date);
            register.append("-");
            register.append(this.description+"\n");

            fileOutputStream.write(register.toString().getBytes());
            fileOutputStream.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void updateWallet() {
        try {
            FileInputStream fileInputStream = openFileInput("UserMoney.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            Double walletAmount = Double.parseDouble(bufferedReader.readLine());
            Double cardAmount = Double.parseDouble(bufferedReader.readLine());

            if(this.typeFlag == 0){
                if(this.destination == "WALLET")
                    walletAmount = walletAmount + this.amount;
                else
                    cardAmount = cardAmount + this.amount;
            }else{
                if(this.destination == "WALLET")
                    walletAmount = walletAmount - this.amount;
                else
                    cardAmount = cardAmount - this.amount;
            }


            FileOutputStream fileOutputStream = openFileOutput("UserMoney.txt", MODE_PRIVATE);
            fileOutputStream.write((walletAmount+"\n").getBytes());
            fileOutputStream.write((cardAmount+"\n").getBytes());

            fileOutputStream.close();
            inputStreamReader.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
