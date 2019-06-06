package br.edu.ifsp.scl.firebaseui_sdm

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.kotlinpermissions.KotlinPermissions
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // Choose authentication providers
    val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.PhoneBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build(),
        //AuthUI.IdpConfig.GitHubBuilder().build(),
        AuthUI.IdpConfig.FacebookBuilder().build(),
        AuthUI.IdpConfig.TwitterBuilder().build()
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        permissions()
        navView.setNavigationItemSelectedListener(this)

        verificaStatus()

    }


    fun verificaStatus(){
        if(FirebaseAuth.getInstance().currentUser == null){
            paginaLogin()
        }
        else{
            atualizaUI()
        }
    }

    override fun onStart() {
        super.onStart()

    }

    //Cria-se uma Intent do Firebase UI com as opçãos de Login
    fun paginaLogin(){
        // Create and launch sign-in intent
        startActivityForResult(AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.if_logo)//Inserido Logo Custom
            .build()
            , RC_SIGN_IN
        )

    }

    //Recebe as informações de status do Login para redirecionamento
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == RC_SIGN_IN){
            val response = IdpResponse.fromResultIntent(data);
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                atualizaUI()
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(this, R.string.sign_in_cancelled, Toast.LENGTH_SHORT).show()
                    return;
                }

                if (response.getError()?.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, response.error?.message , Toast.LENGTH_SHORT).show()
                    return;
                }
                Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show()
                Log.e("ERROR-SIGNIN", "Sign-in error: ", response.getError());
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    //Realiza o logoff do usuário
    private fun signOut() {

        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener{
                verificaStatus()
            }

    }

    companion object {

        private const val RC_SIGN_IN = 123
    }

    private fun atualizaUI(){

        if (FirebaseAuth.getInstance().currentUser != null){

            val user = FirebaseAuth.getInstance().currentUser

            txt_id.text = user?.uid
            txt_email.text = user?.email
            txt_nome.text = user?.displayName
            txt_numeroCel.text = user?.phoneNumber

        }

    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.itemMenuSair -> {
                signOut()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_exit -> {
                signOut()
            }

        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    private fun permissions(){

        KotlinPermissions.with(this)
            .permissions(Manifest.permission.INTERNET)
            .ask()
    }


}
