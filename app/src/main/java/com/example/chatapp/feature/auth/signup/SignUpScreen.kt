package com.example.chatapp.feature.auth.signup

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.chatapp.R

@Composable
fun SignUpScreen(navController: NavController) {
    val signUpViewModel: SignUpViewModel = hiltViewModel()
    val uiState = signUpViewModel.state.collectAsState()

    var isShowPassword by remember { mutableStateOf(false) }
    var isShowConfirmPassword by remember { mutableStateOf(false) }

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    val context = LocalContext.current
    LaunchedEffect(key1 = uiState.value) {
        when(uiState.value) {
            is SignUpState.Success -> {
                navController.navigate("home")
            }

            is SignUpState.Error -> {
                Toast.makeText(context, "Sign up failed", Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.chat_app_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .background(color = Color.White)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("Enter your username") },
                label = { Text("Username", color = Color.Black) },
                textStyle = TextStyle(color = Color.Black),
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Black.copy(0.6f)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Enter your email") },
                label = { Text("Email", color = Color.Black) },
                textStyle = TextStyle(color = Color.Black),
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = Color.Black.copy(0.6f)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Enter your password") },
                label = { Text("Password", color = Color.Black) },
                textStyle = TextStyle(color = Color.Black),
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.Black.copy(0.6f)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { isShowPassword = !isShowPassword }) {
                        Icon(
                            if (isShowPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (!isShowPassword) PasswordVisualTransformation() else VisualTransformation.None
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                placeholder = { Text("Confirm your password") },
                label = { Text("Confirm password", color = Color.Black) },
                textStyle = TextStyle(color = Color.Black),
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.Black.copy(0.6f)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { isShowConfirmPassword = !isShowConfirmPassword }) {
                        Icon(
                            if (isShowConfirmPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                isError = password.isNotEmpty() && confirm.isNotEmpty() && password != confirm,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (!isShowConfirmPassword) PasswordVisualTransformation() else VisualTransformation.None
            )

            Spacer(modifier = Modifier.height(24.dp))

            if(uiState.value == SignUpState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        signUpViewModel.signUp(username, email, password)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2853AF),
                        disabledContainerColor = Color(0xFF2853AF).copy(alpha = 0.4f)
                    ),
                    enabled = username.isNotEmpty() &&
                            email.isNotEmpty() &&
                            password.isNotEmpty() &&
                            confirm.isNotEmpty() &&
                            password == confirm,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Sign Up",
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Already an account? ", color = Color.Black)
                    Text(
                        "Sign In",
                        color = Color(0xFF2853AF),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = LocalIndication.current
                        ) {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}