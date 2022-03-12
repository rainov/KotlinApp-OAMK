package com.example.myapplication

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


@Composable
fun ShoppingListMain() {

    val db = Firebase.firestore

    val fAuth = Firebase.auth

    val userVM = viewModel<UserViewModel>()

    val navControl = rememberNavController()

    if ( userVM.isLoggedIn.value ) {
        MainScreen(navControl, userVM, db, fAuth )
    } else {
        LoginScreen( navControl, fAuth, userVM )
    }

}

@Composable
fun LoginScreen(navControl: NavHostController, fAuth: FirebaseAuth, userVM: UserViewModel) {

    Scaffold(
        content = { ContentLoggedOut(navControl, fAuth, userVM) },
        bottomBar = { BottomBarLoggedOut(navControl) }
    )

}

@Composable
fun ContentLoggedOut(navControl: NavHostController, fAuth: FirebaseAuth, userVM: UserViewModel) {

    NavHost(navController = navControl, startDestination = "loginView" ) {
        composable( route = "loginView") {
            LoginView(fAuth, userVM)
        }
        composable( route = "registerView") {
            RegisterView( fAuth, userVM )
        }
    }

}

@Composable
fun BottomBarLoggedOut(navControl: NavHostController) {

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .clickable { navControl.navigate("loginView") },
            painter = painterResource(R.drawable.login_24),
            contentDescription = "",
            tint = Color.Yellow
        )
        Icon(
            modifier = Modifier
                .clickable { navControl.navigate("registerView") },
            painter = painterResource(R.drawable.ic_baseline_add_reaction_24),
            contentDescription = "",
            tint = Color.Yellow
        )
    }

}

@Composable
fun LoginView(fAuth: FirebaseAuth, userVM: UserViewModel) {

    var userEmail by remember{ mutableStateOf("") }
    var userPassword by remember { mutableStateOf("")}

    fun signIn() {
        fAuth
            .signInWithEmailAndPassword(userEmail, userPassword)
            .addOnSuccessListener {
                userVM.logInUser()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            OutlinedTextField(
                value = userEmail,
                onValueChange = { userEmail = it },
                label = { Text("email") })
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = userPassword,
                onValueChange = { userPassword = it },
                label = { Text("password") })
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { signIn() }) { Text(text = "Login") }
        }
    }
}

@Composable
fun RegisterView(  fAuth: FirebaseAuth, userVM: UserViewModel ) {

    var userEmail by remember{ mutableStateOf("") }
    var userPassword by remember { mutableStateOf("")}

    fun register() {
        fAuth
            .createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnSuccessListener {
                userVM.logInUser()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            OutlinedTextField(
                value = userEmail,
                onValueChange = { userEmail = it },
                label = { Text("email") })
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = userPassword,
                onValueChange = { userPassword = it },
                label = { Text("password") })
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { register() }) { Text(text = "Register") }
        }
    }
}

@Composable
fun MainScreen( navControl: NavHostController, userVM: UserViewModel, db: FirebaseFirestore, fAuth: FirebaseAuth ) {

    Scaffold(
        content = { Content(navControl, userVM, db, fAuth) },
        bottomBar = { BottomBar(navControl) }
    )

}

@Composable
fun Content(navControl: NavHostController, userVM: UserViewModel, db: FirebaseFirestore, fAuth: FirebaseAuth){

    NavHost( navController = navControl, startDestination = "view 1") {
        composable(route = "view 1") {
            WelcomeScreen( userVM, fAuth )
        }
        composable(route = "view 2") {
            ShoppingListPage( db, fAuth )
        }
    }

}

@Composable
fun WelcomeScreen( userVM: UserViewModel, fAuth: FirebaseAuth ) {

    val userEmail = fAuth.currentUser!!.email.toString()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text(text = "Hello $userEmail", fontSize = 30.sp)
        Button(onClick = { userVM.logOutUser() }) { Text(text = "Logout") }
    }

}

@Composable
fun ShoppingListPage( db: FirebaseFirestore, fAuth: FirebaseAuth ) {

    var itemName by remember{ mutableStateOf("") }
    val user = fAuth.currentUser!!.email.toString()

    val item = ListItem(itemName, user)

    fun addToFiresStore() {
        db.collection("ShoppingList").add(item)
        itemName = ""
    }

    var itemsToBuy by remember { mutableStateOf(mutableListOf<DocumentSnapshot>())}

    fun getItems() {
        db.collection("ShoppingList")
            .whereEqualTo("user", user) //Searching only for items logged by the current user
            .get()
            .addOnSuccessListener { shoppingList ->
                val items = mutableListOf<DocumentSnapshot>()
                for( product in shoppingList ) {
                    items.add(product)
                }
                itemsToBuy = items
        }
    }
    getItems()

    fun deleteItem(itemPath: String) {
        db
            .collection("ShoppingList")
            .document(itemPath)
            .delete()
        getItems()
    }

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "My shopping list", fontSize = 35.sp)
                Spacer(modifier = Modifier.height(30.dp))
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item name") })
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = { addToFiresStore() }) { Text(text = "Add item") }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .verticalScroll(enabled = true, state = ScrollState(1))
            ) {
                itemsToBuy.forEach {
                    Row(
                        modifier = Modifier
                            .width(350.dp)
                    ){
                        Card(
                            modifier = Modifier
                                .width(240.dp)
                                .height(36.dp),
                            backgroundColor = Color.DarkGray
                        ) {
                            Text(text = it.get("name").toString(), fontSize = 23.sp)
                        }
                        Button(onClick = { deleteItem(it.id) }) {
                            Text(text = "Check out")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
}

@Composable
fun BottomBar(navControl: NavHostController){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .clickable { navControl.navigate("view 1") },
            painter = painterResource(R.drawable.ic_person),
            contentDescription = "",
            tint = Color.Yellow
        )
        Icon(
            modifier = Modifier
                .clickable { navControl.navigate("view 2") },
            painter = painterResource(R.drawable.ic_menu),
            contentDescription = "",
            tint = Color.Yellow
        )
    }
}