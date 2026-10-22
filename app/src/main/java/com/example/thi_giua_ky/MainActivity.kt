package com.example.thi_giua_ky

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val Teal = Color(0xFF28B8C3)
private val DarkBlue = Color(0xFF087FA3)
private val DeepBlue = Color(0xFF075A7A)
private val LightBackground = Color(0xFFF0F8FA)
private val AccentYellow = Color(0xFFFFC928)
private val White = Color(0xFFFFFFFF)
private val TextDark = Color(0xFF234653)

data class Product(
    val id: String = "",
    val tenSP: String = "",
    val loaiSP: String = "",
    val giaSP: Double = 0.0
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = DarkBlue,
                    secondary = Teal,
                    tertiary = AccentYellow,
                    background = LightBackground,
                    surface = White
                )
            ) {
                Surface(Modifier.fillMaxSize(), color = LightBackground) {
                    AuthApp()
                }
            }
        }
    }
}

@Composable
fun AuthApp() {
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        // Đăng xuất khi AuthApp được tạo để yêu cầu đăng nhập lại.
        auth.signOut()
        val listener = FirebaseAuth.AuthStateListener {
            currentUser = it.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    if (currentUser == null) {
        LoginScreen(auth)
    } else {
        ProductScreen(
            email = currentUser?.email ?: "",
            onLogout = { auth.signOut() }
        )
    }
}

@Composable
fun LoginScreen(auth: FirebaseAuth) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    Box(
        Modifier.fillMaxSize().background(Teal),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = White,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    "A",
                    Modifier.padding(horizontal = 30.dp, vertical = 12.dp),
                    color = DarkBlue,
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            Text("PRODUCT MANAGER", style = MaterialTheme.typography.headlineSmall, color = White)
            Text(
                if (isRegister) "TẠO TÀI KHOẢN CỦA BẠN" else "CHÀO MỪNG BẠN TRỞ LẠI",
                color = White,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (isRegister) "Đăng ký" else "Đăng nhập",
                        style = MaterialTheme.typography.headlineSmall,
                        color = DeepBlue
                    )
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mật khẩu") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(22.dp))
                    Button(
                        onClick = {
                            val userEmail = email.trim()
                            if (userEmail.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (loading) return@Button
                            loading = true
                            val task = if (isRegister) {
                                auth.createUserWithEmailAndPassword(userEmail, password)
                            } else {
                                auth.signInWithEmailAndPassword(userEmail, password)
                            }
                            task.addOnCompleteListener { result ->
                                loading = false
                                Toast.makeText(
                                    context,
                                    if (result.isSuccessful) {
                                        if (isRegister) "Đăng ký thành công" else "Đăng nhập thành công"
                                    } else {
                                        "Thất bại: ${result.exception?.localizedMessage ?: "Vui lòng thử lại"}"
                                    },
                                    if (result.isSuccessful) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !loading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentYellow,
                            contentColor = DeepBlue
                        )
                    ) {
                        if (loading) {
                            CircularProgressIndicator(Modifier.height(22.dp), color = DeepBlue)
                        } else {
                            Text(if (isRegister) "TẠO TÀI KHOẢN" else "ĐĂNG NHẬP")
                        }
                    }
                    TextButton(
                        onClick = { isRegister = !isRegister },
                        enabled = !loading
                    ) {
                        Text(
                            if (isRegister) "Đã có tài khoản? Đăng nhập"
                            else "Chưa có tài khoản? Đăng ký",
                            color = DarkBlue
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("QUẢN LÝ SẢN PHẨM DỄ DÀNG", color = White, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun ProductScreen(email: String, onLogout: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    val context = LocalContext.current
    var tenSP by remember { mutableStateOf("") }
    var loaiSP by remember { mutableStateOf("") }
    var giaSP by remember { mutableStateOf("") }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var loading by remember { mutableStateOf(true) }

    DisposableEffect(db) {
        val registration = db.collection("products").addSnapshotListener { snapshot, error ->
            loading = false
            if (error != null) {
                Toast.makeText(context, "Lỗi tải dữ liệu: ${error.message}", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            products = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            } ?: emptyList()
        }
        onDispose { registration.remove() }
    }

    fun clearForm() {
        tenSP = ""
        loaiSP = ""
        giaSP = ""
        selectedProduct = null
    }

    Column(
        Modifier.fillMaxSize().background(LightBackground).padding(16.dp)
    ) {
        Surface(
            Modifier.fillMaxWidth(),
            color = DarkBlue,
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("QUẢN LÝ SẢN PHẨM", color = White, style = MaterialTheme.typography.titleMedium)
                    Text("Tài khoản: $email", color = White, style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentYellow,
                        contentColor = DeepBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Đăng xuất") }
            }
        }

        Spacer(Modifier.height(10.dp))
        OutlinedTextField(tenSP, { tenSP = it }, label = { Text("Tên sản phẩm") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(loaiSP, { loaiSP = it }, label = { Text("Loại sản phẩm") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(giaSP, { giaSP = it }, label = { Text("Giá sản phẩm") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val price = giaSP.toDoubleOrNull()
                if (tenSP.isBlank() || loaiSP.isBlank() || price == null || price < 0) {
                    Toast.makeText(context, "Vui lòng nhập thông tin hợp lệ", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val data = hashMapOf<String, Any>(
                    "tenSP" to tenSP.trim(),
                    "loaiSP" to loaiSP.trim(),
                    "giaSP" to price
                )
                val editing = selectedProduct
                val request = if (editing != null) {
                    db.collection("products").document(editing.id).update(data)
                } else {
                    db.collection("products").add(data)
                }
                request.addOnSuccessListener {
                    Toast.makeText(
                        context,
                        if (editing == null) "Thêm sản phẩm thành công" else "Cập nhật thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    clearForm()
                }.addOnFailureListener { e ->
                    Toast.makeText(context, "Lỗi lưu: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentYellow, contentColor = DeepBlue)
        ) {
            Text(if (selectedProduct == null) "THÊM SẢN PHẨM" else "CẬP NHẬT SẢN PHẨM")
        }

        if (selectedProduct != null) {
            TextButton(onClick = { clearForm() }, modifier = Modifier.align(Alignment.End)) {
                Text("Hủy sửa", color = DarkBlue)
            }
        }

        Spacer(Modifier.height(6.dp))
        Text("Danh sách sản phẩm", style = MaterialTheme.typography.titleMedium, color = TextDark)
        Spacer(Modifier.height(6.dp))

        when {
            loading -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DarkBlue)
            }
            products.isEmpty() -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Chưa có sản phẩm nào", color = TextDark)
            }
            else -> LazyColumn(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(product.tenSP, style = MaterialTheme.typography.titleMedium, color = DeepBlue)
                            Text("Loại: ${product.loaiSP}", color = TextDark)
                            Text("Giá: ${product.giaSP}", color = TextDark)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        selectedProduct = product
                                        tenSP = product.tenSP
                                        loaiSP = product.loaiSP
                                        giaSP = product.giaSP.toString()
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) { Text("Sửa", color = DarkBlue) }

                                OutlinedButton(
                                    onClick = {
                                        db.collection("products").document(product.id).delete()
                                            .addOnSuccessListener {
                                                if (selectedProduct?.id == product.id) clearForm()
                                                Toast.makeText(context, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Lỗi xóa: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) { Text("Xóa", color = Color(0xFFD64545)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
