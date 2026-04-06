package com.stunext.vault

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stunext.vault.ui.theme.VaultTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable?
)

class MainActivity : FragmentActivity() {

    private lateinit var appPreferences: AppPreferences
    private var isAuthenticated by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        appPreferences = AppPreferences(this)
        enableEdgeToEdge()

        setContent {
            VaultTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isAuthenticated) {
                        AppNavigation(appPreferences)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Bóveda Protegida",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isAuthenticated) {
            showBiometricPrompt { success ->
                if (success) {
                    isAuthenticated = true
                } else {
                    finish()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isAuthenticated = false
    }

    private fun showBiometricPrompt(onResult: (Boolean) -> Unit) {
        val biometricManager = BiometricManager.from(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        
        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val executor = ContextCompat.getMainExecutor(this)
                val biometricPrompt = BiometricPrompt(this, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            onResult(true)
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                                Toast.makeText(this@MainActivity, errString, Toast.LENGTH_SHORT).show()
                            }
                            onResult(false)
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                        }
                    })

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Authentication required")
                    .setSubtitle("Use your biometrics to access your vault")
                    .setAllowedAuthenticators(authenticators)
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
            else -> {
                onResult(true)
            }
        }
    }
}

@Composable
fun AppNavigation(appPreferences: AppPreferences) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                appPreferences = appPreferences,
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                appPreferences = appPreferences,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(appPreferences: AppPreferences, onSettingsClick: () -> Unit) {
    val context = LocalContext.current
    val selectedPackageNames by appPreferences.selectedApps.collectAsState(initial = emptySet())
    var allApps by remember { mutableStateOf<List<AppInfo>?>(null) }

    LaunchedEffect(Unit) {
        delay(800)
        allApps = getInstalledApps(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vault") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        val currentAllApps = allApps
        if (currentAllApps == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val selectedApps = currentAllApps.filter { it.packageName in selectedPackageNames }
            if (selectedApps.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("No apps selected.\nGo to settings.", textAlign = TextAlign.Center)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.padding(padding).fillMaxSize()
                ) {
                    items(selectedApps) { app ->
                        AppGridItem(app) {
                            launchApp(context, app.packageName)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(appPreferences: AppPreferences, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var allApps by remember { mutableStateOf<List<AppInfo>?>(null) }
    val selectedPackageNames by appPreferences.selectedApps.collectAsState(initial = emptySet())

    LaunchedEffect(Unit) {
        delay(600)
        allApps = getInstalledApps(context).sortedBy { it.name.lowercase() }
    }

    Scaffold(
        topBar = {
            if (allApps != null) {
                TopAppBar(
                    title = { Text("Select Apps") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (allApps != null) {
                FloatingActionButton(onClick = onBack) {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                }
            }
        }
    ) { padding ->
        val currentAllApps = allApps
        if (currentAllApps == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(currentAllApps) { app ->
                    val isSelected = app.packageName in selectedPackageNames
                    ListItem(
                        headlineContent = { Text(app.name) },
                        supportingContent = { Text(app.packageName) },
                        leadingContent = {
                            app.icon?.let {
                                Image(
                                    bitmap = it.toBitmap().asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        },
                        trailingContent = {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null
                            )
                        },
                        modifier = Modifier.clickable {
                            scope.launch {
                                val newSelection = if (!isSelected) {
                                    selectedPackageNames + app.packageName
                                } else {
                                    selectedPackageNames - app.packageName
                                }
                                appPreferences.saveSelectedApps(newSelection)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppGridItem(app: AppInfo, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        app.icon?.let {
            Image(
                bitmap = it.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(56.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = app.name,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun getInstalledApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN, null)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    val resolveInfos = pm.queryIntentActivities(intent, 0)
    return resolveInfos.map {
        AppInfo(
            name = it.loadLabel(pm).toString(),
            packageName = it.activityInfo.packageName,
            icon = it.loadIcon(pm)
        )
    }
}

private fun launchApp(context: Context, packageName: String) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (launchIntent != null) {
        context.startActivity(launchIntent)
    } else {
        Toast.makeText(context, "Cannot open app", Toast.LENGTH_SHORT).show()
    }
}
