package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ServerEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JellyAmber
import com.example.ui.theme.JellyCyan
import com.example.ui.theme.JellyDarkBg
import com.example.ui.theme.JellyEmerald
import com.example.ui.theme.JellyPink
import com.example.ui.theme.JellyPurplePrimary
import com.example.ui.theme.JellyPurpleSecondary
import com.example.ui.theme.JellySurface
import com.example.ui.theme.JellySurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ServerSelectScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeServer by viewModel.activeServer.collectAsState()
    val allServers by viewModel.allServers.collectAsState()
    val pingResult by viewModel.pingResult.collectAsState()
    val discoveredServerInfo by viewModel.discoveredServerInfo.collectAsState()
    val publicUsers by viewModel.publicUsers.collectAsState()
    val quickConnectState by viewModel.quickConnectState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddForm by remember { mutableStateOf(false) }
    var selectedAuthTab by remember { mutableIntStateOf(0) } // 0 = Password, 1 = QuickConnect

    var serverUrl by remember { mutableStateOf("http://192.168.1.100:8096") }
    var serverName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successNotice by remember { mutableStateOf<String?>(null) }
    var sessionCheckStatus by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JellyDarkBg)
            .statusBarsPadding()
            .testTag("server_select_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("server_screen_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Jellyfin Servers & Auth",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
            }

            // Active Connection Card
            if (activeServer != null) {
                item {
                    Text(
                        text = "ACTIVE SERVER CONNECTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = JellyCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, JellyPurplePrimary.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .testTag("active_server_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = JellySurfaceElevated)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(JellyPurplePrimary, JellyCyan)
                                            )
                                        ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Dns,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = activeServer!!.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        )
                                        Text(
                                            text = activeServer!!.url,
                                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(JellyEmerald),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Badges & Logout row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "User: ${activeServer!!.userName ?: "Guest"}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = JellyCyan,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier
                                            .background(JellyCyan.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    )

                                    Text(
                                        text = activeServer!!.serverVersion ?: "10.x",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSecondary
                                        ),
                                        modifier = Modifier
                                            .background(Color(0xFF261E3E), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }

                                if (!activeServer!!.isDemo) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                sessionCheckStatus = "Testing session..."
                                                viewModel.checkServerSession(activeServer!!) { isValid ->
                                                    sessionCheckStatus = if (isValid) "Session Active & Valid ✓" else "Session Expired! Re-login required"
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(32.dp).testTag("verify_session_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NetworkCheck,
                                                contentDescription = null,
                                                tint = JellyCyan,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Verify",
                                                style = MaterialTheme.typography.labelSmall.copy(color = JellyCyan)
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.logoutServer(activeServer!!) },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(32.dp).testTag("logout_server_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                                contentDescription = null,
                                                tint = JellyPink,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Logout",
                                                style = MaterialTheme.typography.labelSmall.copy(color = JellyPink)
                                            )
                                        }
                                    }
                                }
                            }

                            if (sessionCheckStatus != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val isOk = sessionCheckStatus!!.contains("Valid")
                                Text(
                                    text = sessionCheckStatus!!,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isOk) JellyEmerald else JellyPink,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Quick Demo Server toggle button
            item {
                OutlinedButton(
                    onClick = {
                        val demo = allServers.firstOrNull { it.isDemo }
                        if (demo != null) {
                            viewModel.selectServer(demo.id)
                        } else {
                            viewModel.refreshMedia()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("connect_demo_server_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = JellyCyan
                    )
                ) {
                    Icon(imageVector = Icons.Default.Cloud, contentDescription = null, tint = JellyCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Switch to Jellyfin Demo Cloud", fontWeight = FontWeight.SemiBold)
                }
            }

            // Add/Connect Server Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONNECT JELLYFIN INSTANCE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    OutlinedButton(
                        onClick = { showAddForm = !showAddForm },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp).testTag("toggle_add_server_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = JellyPurplePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showAddForm) "Close" else "Add Server",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary)
                        )
                    }
                }
            }

            // Authentication Form
            if (showAddForm) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                            .testTag("auth_form_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = JellySurface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Authenticate to Jellyfin",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )

                            // Server URL Field
                            OutlinedTextField(
                                value = serverUrl,
                                onValueChange = {
                                    serverUrl = it
                                    errorMessage = null
                                },
                                label = { Text("Server Address / URL") },
                                placeholder = { Text("http://192.168.1.100:8096 or https://...") },
                                supportingText = {
                                    Text(
                                        text = "LAN IP (e.g. http://192.168.1.50:8096) or public domain. Web URLs (/web) are automatically cleaned.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                    )
                                },
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            viewModel.testServerPing(serverUrl) { info ->
                                                if (serverName.isBlank() && !info.serverName.isNullOrBlank()) {
                                                    serverName = info.serverName
                                                }
                                            }
                                        },
                                        modifier = Modifier.testTag("ping_icon_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NetworkCheck,
                                            contentDescription = "Ping",
                                            tint = JellyCyan
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("server_url_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JellyCyan,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            // Ping Status / Server Discovery Pill
                            if (pingResult != null) {
                                val isSuccess = discoveredServerInfo != null
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSuccess) Color(0x2210B981) else Color(0x22F43F5E))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Dns,
                                        contentDescription = null,
                                        tint = if (isSuccess) JellyEmerald else JellyPink,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = pingResult!!,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isSuccess) JellyEmerald else JellyPink,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            // Server Nickname (Optional)
                            OutlinedTextField(
                                value = serverName,
                                onValueChange = { serverName = it },
                                label = { Text("Server Display Name (optional)") },
                                placeholder = { Text(discoveredServerInfo?.serverName ?: "e.g. My Media Server") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("server_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JellyCyan,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            // Tabs: Password vs QuickConnect
                            TabRow(
                                selectedTabIndex = selectedAuthTab,
                                containerColor = JellySurfaceElevated,
                                contentColor = TextPrimary,
                                indicator = { tabPositions ->
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedAuthTab]),
                                        color = JellyCyan
                                    )
                                },
                                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                            ) {
                                Tab(
                                    selected = selectedAuthTab == 0,
                                    onClick = { selectedAuthTab = 0 },
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Password")
                                        }
                                    }
                                )
                                Tab(
                                    selected = selectedAuthTab == 1,
                                    onClick = { selectedAuthTab = 1 },
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Quick Connect")
                                        }
                                    }
                                )
                            }

                            // Tab 0: Password Flow
                            if (selectedAuthTab == 0) {
                                // Discovered Public Users Chip row (if any)
                                if (publicUsers.isNotEmpty()) {
                                    Column {
                                        Text(
                                            text = "Select user or enter credentials below:",
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(publicUsers) { pUser ->
                                                val isSelectedUser = username == pUser.name
                                                Row(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(20.dp))
                                                        .background(if (isSelectedUser) JellyPurplePrimary else JellySurfaceElevated)
                                                        .clickable {
                                                            username = pUser.name ?: ""
                                                            if (pUser.hasPassword == false) {
                                                                password = ""
                                                            }
                                                        }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(pUser.name ?: "User", style = MaterialTheme.typography.labelSmall.copy(color = Color.White))
                                                    if (pUser.hasPassword == false) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "No PW",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = JellyEmerald,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold
                                                            ),
                                                            modifier = Modifier
                                                                .background(JellyEmerald.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = username,
                                    onValueChange = {
                                        username = it
                                        errorMessage = null
                                    },
                                    label = { Text("Username") },
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = JellyCyan)
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("server_username_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = JellyCyan,
                                        unfocusedBorderColor = GlassBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it
                                        errorMessage = null
                                    },
                                    label = { Text("Password") },
                                    singleLine = true,
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = JellyCyan)
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle password visibility",
                                                tint = TextSecondary
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("server_password_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = JellyCyan,
                                        unfocusedBorderColor = GlassBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                // Connect via Password Button
                                Button(
                                    onClick = {
                                        errorMessage = null
                                        successNotice = null
                                        viewModel.authenticateWithCredentials(
                                            name = serverName.ifBlank { discoveredServerInfo?.serverName ?: "Jellyfin" },
                                            url = serverUrl,
                                            user = username,
                                            pass = password
                                        ) { success, err ->
                                            if (success) {
                                                successNotice = "Connected to Jellyfin successfully!"
                                                showAddForm = false
                                                viewModel.refreshMedia()
                                            } else {
                                                errorMessage = err ?: "Authentication failed"
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("save_server_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = JellyPurplePrimary)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Authenticate & Connect",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // Tab 1: Quick Connect Flow
                            if (selectedAuthTab == 1) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(JellySurfaceElevated)
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (!quickConnectState.code.isNullOrBlank()) {
                                        Text(
                                            text = "ENTER THIS CODE IN JELLYFIN",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = JellyCyan,
                                                letterSpacing = 1.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        // 6-Character Big Code Display
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF0F0B1E))
                                                .border(2.dp, JellyPurplePrimary, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 24.dp, vertical = 12.dp)
                                                .testTag("quick_connect_code_box")
                                        ) {
                                            Text(
                                                text = quickConnectState.code!!.chunked(3).joinToString(" "),
                                                style = MaterialTheme.typography.headlineMedium.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    letterSpacing = 4.sp
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = JellyCyan,
                                                strokeWidth = 2.dp
                                            )
                                            Text(
                                                text = "Waiting for approval in your browser / TV...",
                                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        OutlinedButton(
                                            onClick = { viewModel.cancelQuickConnect() },
                                            modifier = Modifier.testTag("cancel_quick_connect_button")
                                        ) {
                                            Text("Cancel", color = TextSecondary)
                                        }
                                    } else {
                                        Text(
                                            text = "Jellyfin Quick Connect allows instant login without typing your password on mobile. Simply start Quick Connect, then approve the code in your Jellyfin web dashboard or client.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondary,
                                                textAlign = TextAlign.Center
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Button(
                                            onClick = {
                                                viewModel.startQuickConnect(
                                                    url = serverUrl,
                                                    serverName = serverName.ifBlank { discoveredServerInfo?.serverName ?: "Jellyfin" }
                                                ) { success, err ->
                                                    if (success) {
                                                        successNotice = "Quick Connect approved! Connected to Jellyfin."
                                                        showAddForm = false
                                                        viewModel.refreshMedia()
                                                    } else if (err != null) {
                                                        errorMessage = err
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("start_quick_connect_button"),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = JellyPurplePrimary)
                                        ) {
                                            Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = Color.White)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Generate Quick Connect Code", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Error or Success Alert
                            if (errorMessage != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x33F43F5E))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = errorMessage!!,
                                        color = JellyPink,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                    )
                                }
                            }

                            if (successNotice != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x3310B981))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = successNotice!!,
                                        color = JellyEmerald,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Saved Servers Section
            item {
                Text(
                    text = "SAVED SERVERS (${allServers.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            // Saved Servers List
            items(allServers) { server ->
                val isSelected = server.id == activeServer?.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.selectServer(server.id) }
                        .border(
                            1.dp,
                            if (isSelected) JellyPurplePrimary else GlassBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .testTag("server_item_${server.id}"),
                    colors = CardDefaults.cardColors(containerColor = JellySurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) JellyPurplePrimary else Color(0xFF261D42)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dns,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = server.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    if (server.isDemo) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "DEMO",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = JellyCyan,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier
                                                .background(JellyCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${server.url} • ${server.userName ?: "Guest"}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(JellyEmerald),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            if (!server.isDemo) {
                                IconButton(
                                    onClick = { viewModel.deleteServer(server.id) },
                                    modifier = Modifier.size(32.dp).testTag("delete_server_${server.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
