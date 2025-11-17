package com.example.chatapp.feature.chat

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.chatapp.R
import com.example.chatapp.feature.home.ChannelItem
import com.example.chatapp.model.Message
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(navController: NavController, channelId: String, channelName: String) {
    val context = LocalContext.current

    val chatViewModel: ChatViewModel = hiltViewModel()
    val messages = chatViewModel.messages.collectAsState()

    LaunchedEffect(key1 = true) {
        chatViewModel.listenForMessage(channelId)
    }

    var showDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            cameraImageUri?.let {
                chatViewModel.sendImageMessage(it, channelId)
            }
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            chatViewModel.sendImageMessage(it, channelId)
        }
    }

    fun createImageUri(): Uri {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
        cameraImageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        return cameraImageUri!!
    }

    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
        if(isGranted) {
            cameraImageLauncher.launch(createImageUri())
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.padding(top = 36.dp, bottom = 12.dp)
            ) { ChannelItem(channelName = channelName, onClickItem = {}) }
        },
        containerColor = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            ChatMessages(
                messages = messages.value,
                onSendMessage = { message ->
                    chatViewModel.sendMessage(channelId, message)
                },
                onImageLicked = {
                    showDialog = true
                }
            )
        }

        if (showDialog) {
            ContentSelectionDialog(
                onCameraSelected = {
                    showDialog = false
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                        cameraImageLauncher.launch(createImageUri())
                    } else {

                        // request permission
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                onGallerySelected = {
                    showDialog = false
                    imageLauncher.launch("image/*")
                }
            )
        }
    }
}

@Composable
fun ContentSelectionDialog(
    onCameraSelected: () -> Unit,
    onGallerySelected: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                onClick = { onCameraSelected() }
            ) {
                Text("Camera", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onGallerySelected() }
            ) {
                Text("Gallery", color = Color.White)
            }
        },
        title = {
            Text("Select you source?")
        },
        text = {
            Text("Would you like to pick an image from the gallery or use the camera?")
        }
    )
}

@Composable
fun ChatMessages(
    messages: List<Message>,
    onSendMessage: (String) -> Unit,
    onImageLicked: () -> Unit
) {
    var msg by remember { mutableStateOf("") }
    val hideKeyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.915f),
            verticalArrangement = Arrangement.Bottom
        ) {
            items(messages) { message ->
                ChatBubble(message = message)
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .padding(bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = null,
                    tint = Color(0xFF00C4FF),
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                        .clickable {
                            onImageLicked()
                        }
                )

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = msg,
                    onValueChange = { msg = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(20.dp),
                    placeholder = {
                        Text(
                            "Type a message",
                            textAlign = TextAlign.Center,
                            lineHeight = 8.sp
                        )
                    },
                    textStyle = TextStyle(lineHeight = 8.sp, fontSize = 16.sp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { hideKeyboardController?.hide() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF830E7F),
                        unfocusedBorderColor = Color(0xFF830E7F),
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    ),
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .width(1.dp)
                                    .background(Color.Gray)
                            )

                            Spacer(modifier = Modifier.width(2.dp))

                            IconButton(
                                onClick = {
                                    onSendMessage(msg)
                                    msg = ""
                                },
                                enabled = msg.isNotEmpty(),
                            ) {
                                val isButtonEnable = msg.isNotEmpty()
                                Image(
                                    painter = painterResource(id = R.drawable.ic_send),
                                    contentDescription = null,
                                    colorFilter = if (isButtonEnable) ColorFilter.tint(Color(0xFF00C4FF)) else ColorFilter.tint(
                                        Color(0xFF00C4FF).copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val isCurrentUser = message.senderId == Firebase.auth.currentUser?.uid
    val bubbleColor = if (isCurrentUser) Color(0xFF830E7F) else Color.DarkGray

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
        val shape = if (isCurrentUser) RoundedCornerShape(
            topStart = 20.dp,
            bottomStart = 20.dp,
            topEnd = 6.dp,
            bottomEnd = 6.dp
        ) else RoundedCornerShape(
            topEnd = 20.dp,
            bottomEnd = 20.dp,
            topStart = 6.dp,
            bottomStart = 6.dp
        )

        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .align(alignment),
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isCurrentUser) {
                Image(
                    painter = painterResource(id = R.drawable.user_avatar),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(32.dp)
                )
            }

            val screenWidth = LocalConfiguration.current.screenWidthDp.dp

            if(message.imageUrl != null) {
                AsyncImage(
                    model = message.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .sizeIn(maxWidth = screenWidth * 0.55f)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Text(
                    text = message.message ?: "",
                    color = Color.White,
                    modifier = Modifier
                        .widthIn(max = screenWidth * 0.6f)
                        .background(color = bubbleColor, shape = shape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)

                )
            }
        }
    }
}