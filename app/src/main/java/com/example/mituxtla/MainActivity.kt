package com.example.mituxtla

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.mituxtla.ui.theme.MiTuxtlaTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.os.Handler
import android.os.Looper
import androidx.compose.material3.LinearProgressIndicator

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.Timestamp
import androidx.core.app.ActivityCompat

import android.content.SharedPreferences

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

        crearCanalNotificaciones(this)
        revisarEventosYNotificar(this)

        setContent {
            MiTuxtlaTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    val startDestination = "splash"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable("splash") {
            SplashScreen(navController)
        }

        composable("login") {
            LoginScreen(navController)
        }

        composable("register") {
            RegisterScreen(navController)
        }

        composable(
            route = "home/{isGuest}",
            arguments = listOf(
                navArgument("isGuest") {
                    type = NavType.BoolType
                }
            )
        ) { backStackEntry ->

            val isGuest =
                backStackEntry.arguments?.getBoolean("isGuest") ?: true

            HomeScreen(navController, isGuest)
        }

        composable(
            route = "detail/{lugarId}",
            arguments = listOf(
                navArgument("lugarId") {
                    type = NavType.StringType
                }

            )
        ) { backStackEntry ->

            val lugarId = backStackEntry.arguments?.getString("lugarId") ?: ""

            DetailScreen(
                navController = navController,
                lugarId = lugarId
            )
        }
        composable("favorites") {
            FavoritesScreen(navController)
        }

        composable("profile") {
            ProfileScreen(navController)
        }


        composable(
            route = "eventDetail/{eventoId}",
            arguments = listOf(
                navArgument("eventoId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val eventoId =
                backStackEntry.arguments?.getString("eventoId") ?: ""

            EventDetailScreen(
                navController = navController,
                eventoId = eventoId
            )
        }

        composable("map") {
            MapScreen(navController)
        }

    }
}

@Composable
fun SplashScreen(
    navController: NavHostController
) {
    val auth = FirebaseAuth.getInstance()

    LaunchedEffect(Unit) {
        delay(2500)

        if (auth.currentUser != null) {
            navController.navigate("home/false") {
                popUpTo("splash") {
                    inclusive = true
                }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") {
                    inclusive = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.logo_mituxtla),
                contentDescription = "Logo Mi Tuxtla",
                modifier = Modifier.size(220.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Mi Tuxtla",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Descubre • Conoce • Vive",
                fontSize = 16.sp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(30.dp))

            CircularProgressIndicator(
                color = Color.White
            )
        }
    }
}



@Composable
fun BottomNavigationBar(
    navController: NavHostController
) {
    NavigationBar(
        containerColor = Color.White
    ) {
        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate("home/false")
            },
            icon = {
                Icon(Icons.Filled.Home, contentDescription = "Inicio")
            },
            label = {
                Text("Inicio")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate("favorites")
            },
            icon = {
                Icon(Icons.Filled.Favorite, contentDescription = "Favoritos")
            },
            label = {
                Text("Favoritos")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate("map")
            },
            icon = {
                Icon(Icons.Filled.Map, contentDescription = "Mapa")
            },
            label = {
                Text("Mapa")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate("profile")
            },
            icon = {
                Icon(Icons.Filled.Person, contentDescription = "Perfil")
            },
            label = {
                Text("Perfil")
            }
        )
    }
}

@Composable
fun LoginScreen(navController: NavHostController) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "Mi Tuxtla",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Guía Turística de Tuxtla Gtz",
                color = Color.LightGray,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(24.dp)
                ) {

                    Text(
                        "Iniciar Sesión",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {

                            if (email.isBlank() || password.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Completa todos los campos",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            auth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Inicio de sesión exitoso",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    navController.navigate("home/false") {
                                        popUpTo("login") {
                                            inclusive = true
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->

                                    when (exception) {
                                        is FirebaseAuthInvalidUserException -> {
                                            Toast.makeText(
                                                context,
                                                "Correo no existente",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        is FirebaseAuthInvalidCredentialsException -> {
                                            Toast.makeText(
                                                context,
                                                "Contraseña incorrecta",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        else -> {
                                            Toast.makeText(
                                                context,
                                                exception.message,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Entrar")
                    }

                    TextButton(
                        onClick = {
                            if (email.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Escribe tu correo para recuperar contraseña",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                auth.sendPasswordResetEmail(email)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Correo de recuperación enviado",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Error al enviar correo",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("¿Olvidaste tu contraseña?")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            navController.navigate("home/true")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                    ) {
                        Text("Continuar como visitante")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = {
                            navController.navigate("register")
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Crear cuenta")
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(navController: NavHostController) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "Mi Tuxtla",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(30.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {

                Column(
                    modifier = Modifier.padding(24.dp)
                ) {

                    Text(
                        "Registro",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {

                            if (
                                nombre.isBlank() ||
                                correo.isBlank() ||
                                password.isBlank() ||
                                confirmPassword.isBlank()
                            ) {
                                Toast.makeText(
                                    context,
                                    "Completa todos los campos",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            if (password != confirmPassword) {
                                Toast.makeText(
                                    context,
                                    "Las contraseñas no coinciden",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            auth.createUserWithEmailAndPassword(correo, password)
                                .addOnSuccessListener {

                                    Toast.makeText(
                                        context,
                                        "Usuario creado correctamente",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    navController.navigate("home/false") {
                                        popUpTo("register") {
                                            inclusive = true
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->

                                    when (exception) {
                                        is FirebaseAuthUserCollisionException -> {
                                            Toast.makeText(
                                                context,
                                                "Ese correo ya está registrado",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        else -> {
                                            Toast.makeText(
                                                context,
                                                exception.message,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Crear cuenta")
                    }

                    TextButton(
                        onClick = {
                            navController.popBackStack()
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("¿Ya tienes cuenta?")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    isGuest: Boolean
) {
    val db = FirebaseFirestore.getInstance()

    var lugares by remember { mutableStateOf<List<Lugar>>(emptyList()) }
    var eventos by remember {
        mutableStateOf<List<Evento>>(emptyList())
    }
    var loading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }

    val azulPrincipal = Color(0xFF1565C0)
    val azulOscuro = Color(0xFF0D47A1)
    val fondoClaro = Color(0xFFF3F7FB)

    val categorias = listOf("Todos", "Turismo", "Restaurantes", "Parques", "Cultura", "Museos")

    LaunchedEffect(Unit) {
        db.collection("lugares")
            .get()
            .addOnSuccessListener { result ->
                lugares = result.documents.mapNotNull {
                    it.toObject(Lugar::class.java)?.copy(id = it.id)
                }
                loading = false
            }
            .addOnFailureListener {
                loading = false
            }

        db.collection("eventos")
            .get()
            .addOnSuccessListener { result ->
                eventos = result.documents.mapNotNull {
                    it.toObject(Evento::class.java)?.copy(
                        id = it.id
                    )
                }
            }
    }

    val lugaresFiltrados = lugares.filter { lugar ->
        val coincideBusqueda =
            lugar.nombre.contains(searchText, ignoreCase = true) ||
                    lugar.categoria.contains(searchText, ignoreCase = true)

        val coincideCategoria =
            categoriaSeleccionada == "Todos" ||
                    lugar.categoria.equals(categoriaSeleccionada, ignoreCase = true)

        coincideBusqueda && coincideCategoria
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Acceso restringido") },
            text = { Text("Debes iniciar sesión para ver más información.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        navController.navigate("login")
                    }
                ) {
                    Text("Iniciar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            if (!isGuest) {
                BottomNavigationBar(navController)
            }
        }
    ) { paddingValues ->

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = azulPrincipal)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(fondoClaro),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(azulPrincipal, azulOscuro)
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "TUXTLA GUTIÉRREZ",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "Descubre\nTu ciudad",
                                        color = Color.White,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 38.sp
                                    )

                                    Text(
                                        text = "Guía turística y cultural",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 13.sp
                                    )
                                }

                                Image(
                                    painter = painterResource(id = R.drawable.logo_mituxtla),
                                    contentDescription = "Logo Mi Tuxtla",
                                    modifier = Modifier.size(86.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(22.dp))

                            OutlinedTextField(
                                value = searchText,
                                onValueChange = { searchText = it },
                                placeholder = { Text("Buscar lugares, rutas...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(30.dp)),
                                singleLine = true,
                                shape = RoundedCornerShape(30.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categorias.forEach { categoria ->
                            if (categoriaSeleccionada == categoria) {
                                Button(
                                    onClick = { categoriaSeleccionada = categoria },
                                    shape = RoundedCornerShape(30.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = azulPrincipal)
                                ) {
                                    Text(categoria)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { categoriaSeleccionada = categoria },
                                    shape = RoundedCornerShape(30.dp)
                                ) {
                                    Text(categoria)
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Destacados",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }



                if (lugaresFiltrados.isEmpty()) {
                    item {
                        Text(
                            text = "No se encontraron lugares",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(lugaresFiltrados) { lugar ->
                                Card(
                                    modifier = Modifier.width(250.dp),
                                    shape = RoundedCornerShape(26.dp),
                                    elevation = CardDefaults.cardElevation(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column {
                                        AsyncImage(
                                            model = lugar.imagenUrl,
                                            contentDescription = lugar.nombre,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(155.dp),
                                            contentScale = ContentScale.Crop
                                        )

                                        Column(
                                            modifier = Modifier.padding(14.dp)
                                        ) {
                                            Text(
                                                text = lugar.categoria,
                                                fontSize = 12.sp,
                                                color = azulPrincipal,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = lugar.nombre,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 2
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = lugar.direccion,
                                                color = Color.Gray,
                                                fontSize = 13.sp,
                                                maxLines = 1
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Button(
                                                onClick = {
                                                    if (isGuest) {
                                                        showDialog = true
                                                    } else {
                                                        navController.navigate("detail/${lugar.id}")
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(30.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = azulPrincipal
                                                )
                                            ) {
                                                Text("Ver")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                }

                item {
                    Text(
                        text = "🔔 Próximos eventos",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(eventos) { evento ->

                    Card(
                        onClick = {
                            navController.navigate("eventDetail/${evento.id}")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(22.dp),
                        elevation = CardDefaults.cardElevation(6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            AsyncImage(
                                model = evento.imagen,
                                contentDescription = evento.titulo,
                                modifier = Modifier
                                    .size(90.dp),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = evento.titulo,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = evento.lugar,
                                    color = azulPrincipal,
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = evento.descripcion,
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavHostController,
    lugarId: String
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var lugar by remember { mutableStateOf<Lugar?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(lugarId) {
        db.collection("lugares")
            .document(lugarId)
            .get()
            .addOnSuccessListener { document ->
                lugar = document.toObject(Lugar::class.java)?.copy(
                    id = document.id
                )
                loading = false
            }
            .addOnFailureListener {
                error = true
                loading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(lugar?.nombre ?: "Detalle del lugar")
                },
                navigationIcon = {
                    TextButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Text("Atrás")
                    }
                }
            )
        }
    ) { padding ->

        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error || lugar == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se pudo cargar la información del lugar")
                }
            }

            else -> {
                val lugarActual = lugar!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFF5F5F5))
                ) {
                    item {
                        AsyncImage(
                            model = lugarActual.imagenUrl,
                            contentDescription = lugarActual.nombre,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp),
                            contentScale = ContentScale.Crop
                        )

                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = lugarActual.nombre,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = lugarActual.categoria,
                                color = Color(0xFF2C5364),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Descripción",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = lugarActual.descripcion,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Dirección",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = lugarActual.direccion,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Horario",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = lugarActual.horario,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(30.dp))

                            Button(
                                onClick = {
                                    val user = FirebaseAuth.getInstance().currentUser

                                    if (user == null) {
                                        Toast.makeText(
                                            context,
                                            "Debes iniciar sesión para agregar favoritos",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }

                                    db.collection("usuarios")
                                        .document(user.uid)
                                        .collection("favoritos")
                                        .document(lugarActual.id)
                                        .set(lugarActual)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "Agregado a favoritos",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                context,
                                                "Error al agregar favorito",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Agregar a favoritos")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = {
                                    if (lugarActual.mapsUrl.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            "No hay ubicación disponible",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@OutlinedButton
                                    }

                                    val mapsIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(lugarActual.mapsUrl)
                                    ).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }

                                    try {
                                        context.startActivity(mapsIntent)
                                    } catch (e: Exception) {
                                        val browserIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(lugarActual.mapsUrl)
                                        )
                                        context.startActivity(browserIntent)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("📍 Cómo llegar")
                            }

                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    navController: NavHostController,
    eventoId: String
) {
    val db = FirebaseFirestore.getInstance()

    var evento by remember {
        mutableStateOf<Evento?>(null)
    }

    var loading by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(Unit) {
        db.collection("eventos")
            .document(eventoId)
            .get()
            .addOnSuccessListener { document ->
                evento = document.toObject(Evento::class.java)?.copy(
                    id = document.id
                )
                loading = false
            }
            .addOnFailureListener {
                loading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Detalle del evento")
                }
            )
        }
    ) { padding ->

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (evento == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Evento no encontrado")
            }
        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF5F5F5))
            ) {

                item {
                    AsyncImage(
                        model = evento!!.imagen,
                        contentDescription = evento!!.titulo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = evento!!.titulo,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "📍 ${evento!!.lugar}",
                            fontSize = 18.sp,
                            color = Color(0xFF1565C0)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "📅 ${evento!!.fecha}",
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Descripción",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = evento!!.descripcion,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavHostController
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val user = auth.currentUser

    var favoritos by remember { mutableStateOf<List<Lugar>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    fun cargarFavoritos() {
        if (user == null) {
            loading = false
            return
        }

        loading = true

        db.collection("usuarios")
            .document(user.uid)
            .collection("favoritos")
            .get()
            .addOnSuccessListener { result ->
                favoritos = result.documents.mapNotNull { document ->
                    document.toObject(Lugar::class.java)?.copy(
                        id = document.id
                    )
                }
                loading = false
            }
            .addOnFailureListener {
                loading = false
                Toast.makeText(
                    context,
                    "Error al cargar favoritos",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    LaunchedEffect(Unit) {
        cargarFavoritos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Mis favoritos")
                },
                navigationIcon = {
                    TextButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Text("Atrás")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->

        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            user == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Debes iniciar sesión para ver tus favoritos")
                }
            }

            favoritos.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aún no tienes lugares favoritos")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFF5F5F5)),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(favoritos) { lugar ->

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column {

                                AsyncImage(
                                    model = lugar.imagenUrl,
                                    contentDescription = lugar.nombre,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentScale = ContentScale.Crop
                                )

                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = lugar.nombre,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = lugar.descripcion,
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedButton(
                                        onClick = {
                                            navController.navigate("detail/${lugar.id}")
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Ver detalle")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            if (user != null) {
                                                db.collection("usuarios")
                                                    .document(user.uid)
                                                    .collection("favoritos")
                                                    .document(lugar.id)
                                                    .delete()
                                                    .addOnSuccessListener {
                                                        favoritos = favoritos.filter {
                                                            it.id != lugar.id
                                                        }

                                                        Toast.makeText(
                                                            context,
                                                            "Eliminado de favoritos",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    .addOnFailureListener {
                                                        Toast.makeText(
                                                            context,
                                                            "Error al quitar favorito",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFB3261E)
                                        )
                                    ) {
                                        Text("Quitar de favoritos")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController
) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val user = auth.currentUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Perfil")
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "👤",
                        fontSize = 64.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = user?.displayName ?: "Usuario",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Correo:",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = user?.email ?: "Sin correo",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Tipo de usuario:",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Usuario registrado",
                        fontSize = 16.sp,
                        color = Color(0xFF2C5364)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = {
                            val email = user?.email

                            if (email.isNullOrBlank()) {
                                Toast.makeText(
                                    context,
                                    "No se encontró correo del usuario",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                auth.sendPasswordResetEmail(email)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Correo para cambiar contraseña enviado",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Error al enviar correo",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cambiar contraseña")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            auth.signOut()

                            navController.navigate("login") {
                                popUpTo(0)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar sesión")
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavHostController) {
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val azulPrincipal = Color(0xFF1565C0)
    val fondoClaro = Color(0xFFF3F7FB)

    var selectedFilter by remember { mutableStateOf("Restaurantes") }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var places by remember { mutableStateOf<List<PlaceMarker>>(emptyList()) }
    var loadingPlaces by remember { mutableStateOf(false) }
    var centeredMap by remember { mutableStateOf(false) }

    val filters = mapOf(
        "Restaurantes" to Pair("amenity", "restaurant"),
        "Cafés" to Pair("amenity", "cafe"),
        "Hoteles" to Pair("tourism", "hotel"),
        "Casas de cambio" to Pair("amenity", "bureau_de_change")
    )

    fun loadNearbyPlaces(latitude: Double, longitude: Double, filter: String) {
        loadingPlaces = true

        val pair = filters[filter] ?: Pair("amenity", "restaurant")
        val key = pair.first
        val value = pair.second

        val query = """
            [out:json][timeout:25];
            (
              node["$key"="$value"](around:4000,$latitude,$longitude);
              way["$key"="$value"](around:4000,$latitude,$longitude);
              relation["$key"="$value"](around:4000,$latitude,$longitude);
            );
            out center;
        """.trimIndent()

        Thread {
            try {
                val url = "https://overpass-api.de/api/interpreter?data=${Uri.encode(query)}"
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""

                val json = JSONObject(body)
                val elements = json.getJSONArray("elements")
                val result = mutableListOf<PlaceMarker>()

                for (i in 0 until elements.length()) {
                    val item = elements.getJSONObject(i)
                    val tags = item.optJSONObject("tags")
                    val name = tags?.optString("name", "Sin nombre") ?: "Sin nombre"

                    val lat: Double
                    val lon: Double

                    if (item.has("lat") && item.has("lon")) {
                        lat = item.getDouble("lat")
                        lon = item.getDouble("lon")
                    } else if (item.has("center")) {
                        val center = item.getJSONObject("center")
                        lat = center.getDouble("lat")
                        lon = center.getDouble("lon")
                    } else {
                        continue
                    }

                    result.add(
                        PlaceMarker(
                            name = name,
                            lat = lat,
                            lon = lon
                        )
                    )
                }

                Handler(Looper.getMainLooper()).post {
                    places = result
                    loadingPlaces = false
                }

            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    places = emptyList()
                    loadingPlaces = false
                    Toast.makeText(
                        context,
                        "No se pudieron cargar lugares cercanos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val point = GeoPoint(location.latitude, location.longitude)
                        userLocation = point
                        loadNearbyPlaces(location.latitude, location.longitude, selectedFilter)
                    } else {
                        Toast.makeText(
                            context,
                            "Activa tu ubicación e intenta de nuevo",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else {
            Toast.makeText(
                context,
                "Permiso de ubicación denegado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName

        val permission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val point = GeoPoint(location.latitude, location.longitude)
                    userLocation = point
                    loadNearbyPlaces(location.latitude, location.longitude, selectedFilter)
                } else {
                    Toast.makeText(
                        context,
                        "No se pudo obtener tu ubicación",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(fondoClaro)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(azulPrincipal)
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Mapa cercano",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Encuentra servicios cerca de ti",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filters.keys.forEach { filter ->

                            if (selectedFilter == filter) {
                                Button(
                                    onClick = {
                                        selectedFilter = filter
                                        userLocation?.let {
                                            loadNearbyPlaces(
                                                it.latitude,
                                                it.longitude,
                                                selectedFilter
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(30.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = filter,
                                        color = azulPrincipal
                                    )
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        selectedFilter = filter
                                        userLocation?.let {
                                            loadNearbyPlaces(
                                                it.latitude,
                                                it.longitude,
                                                selectedFilter
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(30.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(filter)
                                }
                            }
                        }
                    }
                }
            }

            if (loadingPlaces) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = azulPrincipal
                )
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(16.0)

                        val startPoint = userLocation ?: GeoPoint(16.7516, -93.1029)
                        controller.setCenter(startPoint)
                    }
                },
                update = { mapView ->

                    mapView.overlays.clear()

                    userLocation?.let { location ->

                        if (!centeredMap) {
                            mapView.controller.setZoom(16.0)
                            mapView.controller.setCenter(location)
                            centeredMap = true
                        }

                        val userMarker = Marker(mapView)
                        userMarker.position = location
                        userMarker.title = "Tu ubicación"
                        mapView.overlays.add(userMarker)
                    }

                    places.forEach { place ->
                        val marker = Marker(mapView)
                        marker.position = GeoPoint(place.lat, place.lon)
                        marker.title = place.name
                        marker.subDescription = selectedFilter
                        mapView.overlays.add(marker)
                    }

                    mapView.invalidate()
                }
            )
        }
    }
}

data class PlaceMarker(
    val name: String,
    val lat: Double,
    val lon: Double
)

fun crearCanalNotificaciones(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "eventos_channel",
            "Eventos Mi Tuxtla",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notificaciones de eventos próximos y finalizados"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}

fun mostrarNotificacionEvento(
    context: Context,
    titulo: String,
    mensaje: String,
    id: Int
) {
    val notification = NotificationCompat.Builder(context, "eventos_channel")
        .setSmallIcon(R.drawable.logo_mituxtla)
        .setContentTitle(titulo)
        .setContentText(mensaje)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }

    NotificationManagerCompat.from(context).notify(id, notification)
}

fun revisarEventosYNotificar(context: Context) {
    val db = FirebaseFirestore.getInstance()
    val ahora = Timestamp.now()

    val prefs = context.getSharedPreferences(
        "notificaciones_eventos",
        Context.MODE_PRIVATE
    )

    db.collection("eventos")
        .get()
        .addOnSuccessListener { result ->

            result.documents.forEachIndexed { index, document ->

                val eventoId = document.id
                val titulo = document.getString("titulo") ?: "Evento"
                val lugar = document.getString("lugar") ?: "Lugar no disponible"
                val fechaInicio = document.getTimestamp("fechaInicio")
                val fechaFin = document.getTimestamp("fechaFin")

                if (fechaInicio != null && fechaFin != null) {

                    val claveProximo = "proximo_$eventoId"
                    val claveFinalizado = "finalizado_$eventoId"

                    when {
                        fechaInicio.seconds > ahora.seconds -> {

                            val yaNotificado = prefs.getBoolean(claveProximo, false)

                            if (!yaNotificado) {
                                mostrarNotificacionEvento(
                                    context,
                                    "🔔 Próximo evento",
                                    "$titulo será en $lugar",
                                    index + 100
                                )

                                prefs.edit()
                                    .putBoolean(claveProximo, true)
                                    .apply()
                            }
                        }

                        fechaFin.seconds < ahora.seconds -> {

                            val yaNotificado = prefs.getBoolean(claveFinalizado, false)

                            if (!yaNotificado) {
                                mostrarNotificacionEvento(
                                    context,
                                    "✅ Evento finalizado",
                                    "$titulo ya terminó",
                                    index + 200
                                )

                                prefs.edit()
                                    .putBoolean(claveFinalizado, true)
                                    .apply()
                            }
                        }
                    }
                }
            }
        }
}