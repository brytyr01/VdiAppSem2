package com.example.bryantyrrell.vdiapp.GPSMap.MapUI;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.bryantyrrell.vdiapp.Database.DatabaseService;
import com.example.bryantyrrell.vdiapp.GPSMap.MapUI.MapsActivity;
import com.example.bryantyrrell.vdiapp.R;


public class FabButtons  {
    private int State=3;
    private String m_Text;
    private ImageButton fab;
    private boolean setUp=true;
    private boolean expanded = false;
    private View fabAction1,fabAction2,fabAction3;
    private float offset1,offset2,offset3;
    Context context;
    private DatabaseService databaseUser;
    private ViewGroup fabContainer;
    MapsActivity activity;

    //fab constructor
    public FabButtons(DatabaseService databaseUser, ImageButton fabButton, ViewGroup fabContainer, View fabAction1, View fabAction2, View fabAction3, Context context,MapsActivity activity){
        this.databaseUser=databaseUser;
        fab=fabButton;
        this.fabContainer=fabContainer;
        this.fabAction1=fabAction1;
        this.fabAction2=fabAction2;
        this.fabAction3=fabAction3;
        this.context=context;
        fabSetUp();
        this.activity =activity;

    }

    // start pause stop states
    public void fabAction1(View v) {
        if(setUp==true){
            //ask user to set route name
            popUpRouteBox();
        }
        if(setUp==false) {
            State = 1;
            Toast.makeText(context, "Tracking started",Toast.LENGTH_LONG).show();
        }
    }

    public void fabAction2(View v) {
        if(setUp==false){
            Toast.makeText(context, "Tracking paused",Toast.LENGTH_LONG).show();
            State=2;
        }
    }
    public void fabAction3(View v) {
        Toast.makeText(context, "Tracking stopped",Toast.LENGTH_LONG).show();
        State=3;

        setUp=true;
    }
    private void collapseFab() {
        fab.setImageResource(R.drawable.animated_minus);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createCollapseAnimator(fabAction1, offset1),
                createCollapseAnimator(fabAction2, offset2),
                createCollapseAnimator(fabAction3, offset3));
        animatorSet.start();
        animateFab();
    }

    private void expandFab() {
        fab.setImageResource(R.drawable.animated_plus);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createExpandAnimator(fabAction1, offset1),
                createExpandAnimator(fabAction2, offset2),
                createExpandAnimator(fabAction3, offset3));
        animatorSet.start();
        animateFab();
    }

    private static final String TRANSLATION_Y = "translationY";

    private Animator createCollapseAnimator(View view, float offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_Y, 0, offset)
                .setDuration(context.getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator createExpandAnimator(View view, float offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_Y, offset, 0)
                .setDuration(context.getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private void animateFab() {
        Drawable drawable = fab.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }

    // asks user to chose a name for the route
    private void popUpRouteBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Please enter Route Name");

// Set up the input
        final EditText input = new EditText(context);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                //set up route in database
                databaseUser.createRouteGpsStorage(m_Text);
                setUp=false;
                activity.StatTracking();
                State = 1;
                Toast.makeText(context.getApplicationContext() , "Tracking started", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void fabSetUp(){
        // 3button system set up
        final ViewGroup fabContainer = this.fabContainer;
//        fab = (ImageButton) findViewById(R.id.fab);
//        fabAction1 = findViewById(R.id.fab_action_1);
//        fabAction2 = findViewById(R.id.fab_action_2);
//        fabAction3 = findViewById(R.id.fab_action_3);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expanded = !expanded;
                if (expanded) {
                    expandFab();
                } else {
                    collapseFab();
                }
            }
        });
        fabContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                fabContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                offset1 = fab.getY() - fabAction1.getY();
                fabAction1.setTranslationY(offset1);
                offset2 = fab.getY() - fabAction2.getY();
                fabAction2.setTranslationY(offset2);
                offset3 = fab.getY() - fabAction3.getY();
                fabAction3.setTranslationY(offset3);
                return true;
            }
        });
    }
    public int getState(){
        return State;
    }
    public Boolean getSetUpValue(){
        return setUp;
    }
}
