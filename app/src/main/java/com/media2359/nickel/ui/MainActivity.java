package com.media2359.nickel.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.media2359.nickel.R;
import com.media2359.nickel.ui.fragments.BaseFragment;
import com.media2359.nickel.ui.fragments.ConfirmationFragment;
import com.media2359.nickel.ui.fragments.HomeFragment;
import com.media2359.nickel.ui.customview.PaymentConfirmationDialog;
import com.media2359.nickel.ui.fragments.ProfileFragment;
import com.media2359.nickel.ui.fragments.SpinnerFragment;

/**
 * This handles the transaction logic
 */
public class MainActivity extends AppCompatActivity implements BaseFragment.FragmentVisibleListener, PaymentConfirmationDialog.ConfirmationDialogListener {

    private FragmentManager manager;
    private MenuItem menuCancel;
    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout coordinatorLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private TextView tvTitle;
    private Fragment mSpinnerFragment;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        manager = getSupportFragmentManager();

        switchFragment(new HomeFragment(), false);
    }


    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        mDrawerToggle.syncState();
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_main);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_closed  /* "close drawer" description */
        ) {
            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //mDrawerToggle.syncState();

        tvTitle = (TextView) findViewById(R.id.tvToolbarTitle);

    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setCheckedItem(R.id.nav_home); // default checked item is the first one
        navigationView.setNavigationItemSelectedListener(itemListener);
    }

    private NavigationView.OnNavigationItemSelectedListener itemListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            Fragment newFragment = null;
            switch (item.getItemId()) {
                case R.id.nav_home:
                    if (getCurrentFragment() instanceof HomeFragment) {
                        newFragment = null;
                    } else {
                        clearFragmentStack();
                        switchFragment(new HomeFragment(), false);
                    }
                    break;
                case R.id.nav_profile:
                    newFragment = new ProfileFragment();
                    break;
//                case R.id.nav_recipients:
//                    newFragment = new RecipientFragment();
//                    break;
                case R.id.nav_settings:
                    //newFragment = new SettingsFragment();
                    return false;
                case R.id.nav_logout:
                    return false;
                default:
                    newFragment = null;
                    break;
            }

            if (newFragment != null) {
                if (manager.getBackStackEntryCount() >= 1) //only maintain one entry on backStack
                    manager.popBackStack();

                switchFragment(newFragment, true);
                item.setChecked(true);
            }

            mDrawerLayout.closeDrawers();
            return true;
        }
    };

    void clearFragmentStack() {
        for (int i = 0; i < manager.getBackStackEntryCount(); ++i)
            manager.popBackStack();
    }

    Fragment getCurrentFragment() {
        return manager.findFragmentById(R.id.fl_container);
    }


    public void switchFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fl_container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        updateDrawerItem(fragment);

        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.show(fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getCurrentFragment() !=null){
            updateDrawerItem(getCurrentFragment());
        }
    }

    private void updateDrawerItem(Fragment fragment){
        if (fragment instanceof ProfileFragment){
            navigationView.setCheckedItem(R.id.nav_profile);
        }else{
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    public void switchRecipientFragment(int position){

    }
    public void showLoadingSpinner() {
        mSpinnerFragment = new SpinnerFragment();
        manager.beginTransaction().add(R.id.fl_container, mSpinnerFragment).commit();
    }

    public void dismissLoadingSpinner() {
        if (mSpinnerFragment == null)
            return;

        manager.beginTransaction().remove(mSpinnerFragment).commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null)
            mDrawerToggle.syncState();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;

        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setPageTitle(String title) {
        tvTitle.setText(title);
    }

    PaymentConfirmationDialog paymentConfirmationDialog;

    public void showPaymentConfirmationDialog(String sendTo, float sendAmount, float exchangeRate){
        if (paymentConfirmationDialog != null){
            paymentConfirmationDialog.dismiss();
        }
        paymentConfirmationDialog = PaymentConfirmationDialog.newInstance("",0,0);
        paymentConfirmationDialog.show(getSupportFragmentManager(),"PaymentConfirmation");
    }

    /**
     * Responds to the payment confirmation dialog
     * Do necessary transactions
     * @param dialog
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        switchFragment(ConfirmationFragment.newInstance("www.google.com.sg"),true);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        //TODO
    }
}
