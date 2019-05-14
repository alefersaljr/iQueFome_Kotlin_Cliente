package br.com.alexandre_salgueirinho.iquefome_kotlin.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import br.com.alexandre_salgueirinho.iquefome_kotlin.R
import br.com.alexandre_salgueirinho.iquefome_kotlin.model.Usuário
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_cliente_cadastro.*
import java.util.*

class ClienteCadastro : AppCompatActivity() {

    private lateinit var mToolbar: androidx.appcompat.widget.Toolbar
    var uriImagemSelecionada: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cliente_cadastro)

        mToolbar = findViewById(R.id.cadastro_Toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userRegister()

        cadastro_TextView_Login.setOnClickListener {
            val intent = Intent(this, ClienteLogin::class.java)
            startActivity(intent)
            finish()
        }

        cadastro_CircleImage_ProfileImage.setOnClickListener {
            Log.d("ClienteCadastroActivity", "Try to show photo")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("ClienteCadastroActivity", "Photo was selected")

            uriImagemSelecionada = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uriImagemSelecionada)

            cadastro_CircleImage_ProfileImage.setImageBitmap(bitmap)
        }
    }

    private fun userRegister() {
        cadastro_Button_Cadastrar.setOnClickListener {
            val email = cadastro_EditText_Email.text.toString()
            val password = cadastro_EditText_Password.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                if (email.isEmpty()) {
                    cadastro_EditText_Email.requestFocus()
                    Toast.makeText(
                        this,
                        "É necessário informar um email",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (password.isEmpty()) {
                    cadastro_EditText_Password.requestFocus()
                    Toast.makeText(
                        this,
                        "É necessário informar uma senha",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@setOnClickListener
            }

            Log.d("ClienteCadastroActivity", "Email: $email")
            Log.d("ClienteCadastroActivity", "Password: $password")

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener

                    Log.d("ClienteCadastroActivity", "Usuário criado, uid: ${it.result!!.user.uid}")

                    uploadImage()

                }.addOnFailureListener {
                    Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun uploadImage() {
        if (uriImagemSelecionada == null) return

        val nomeArquivo = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("images/$nomeArquivo")

        ref.putFile(uriImagemSelecionada!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("ClienteCadastroActivity", "Link do arquivo: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }.addOnFailureListener {
                Log.d("ClienteCadastroActivity", "Erro no upload")
            }
    }

    private fun saveUserToFirebaseDatabase(urlImagemPerfil: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = Usuário(
            uid,
            cadastro_EditText_Nome.text.toString(),
            cadastro_EditText_Sobrenome.text.toString(),
            cadastro_EditText_Email.text.toString(),
            cadastro_EditText_Password.text.toString(),
            cadastro_EditText_Celular.text.toString(),
            cadastro_EditText_Indicado.text.toString(),
            urlImagemPerfil
        )

        ref.setValue(user).addOnSuccessListener {
            Log.d("ClienteCadastroActivity", "Finalmente deu boa")
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}