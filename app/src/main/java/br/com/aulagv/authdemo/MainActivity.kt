package br.com.aulagv.authdemo

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.hardware.biometrics.BiometricPrompt
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.CancellationSignal
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import kotlin.concurrent.thread

/**
 * Aplicativo de demonstração dos métodos de autenticação apresentados na aula:
 * conhecimento, posse, características biométricas, localização, multifator e OAuth/OIDC.
 *
 * A interface é criada por código para manter o projeto pequeno e fácil de estudar.
 */
class MainActivity : Activity() {
    private lateinit var logView: TextView
    private lateinit var mfaStatus: TextView
    private val totpSecret = "SEGREDO_DIDATICO_DO_DISPOSITIVO"
    private var passwordOk = false
    private var totpOk = false
    private var biometricOk = false
    private var locationOk = false
    private var codeVerifier = ""
    private var expectedState = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        handleOAuthRedirect(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthRedirect(intent)
    }

    /** Monta a tela principal com botões de cada método de autenticação. */
    private fun buildUi() {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 28, 28, 28)
        }
        scroll.addView(root)

        root.addView(title("Aula 06 — Métodos de Autenticação no Android"))
        root.addView(paragraph("Este projeto demonstra, em Kotlin nativo, os principais fatores de autenticação: conhecimento, posse, biometria, localização, multifator e OAuth 2.0/OpenID Connect com Google."))

        root.addView(section("1) Prova de conhecimento"))
        root.addView(button("Login com e-mail e senha", ::showPasswordLogin))
        root.addView(button("PIN numérico", ::showPinLogin))
        root.addView(button("Padrão geométrico 3x3", ::showPatternLogin))

        root.addView(section("2) Prova de posse"))
        root.addView(paragraph("Token TOTP atual do dispositivo: ${CryptoUtils.generateTotp(totpSecret)}"))
        root.addView(button("Validar token TOTP", ::showTotpLogin))

        root.addView(section("3) Prova de características"))
        root.addView(button("Autenticar com biometria do aparelho", ::showBiometricPrompt))

        root.addView(section("4) Prova de localização"))
        root.addView(button("Validar localização autorizada", ::checkLocationProof))

        root.addView(section("5) OAuth 2.0 / OpenID Connect"))
        root.addView(button("Entrar com Google usando PKCE", ::startGoogleOidcLogin))

        root.addView(section("6) Autenticação multifatorial"))
        mfaStatus = paragraph("Status MFA: senha=${ok(passwordOk)}, token=${ok(totpOk)}, biometria=${ok(biometricOk)}, localização=${ok(locationOk)}")
        root.addView(mfaStatus)
        root.addView(button("Verificar MFA: senha + token + biometria/localização", ::checkMfa))

        root.addView(section("Log didático"))
        logView = paragraph("Pronto para executar os exemplos.")
        root.addView(logView)
        setContentView(scroll)
    }

    /** Login por e-mail e senha: exemplo clássico de prova de conhecimento. */
    private fun showPasswordLogin() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 8, 24, 0) }
        val email = EditText(this).apply { hint = "demo@exemplo.com"; inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS }
        val pass = EditText(this).apply { hint = "Senha@123"; inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD }
        box.addView(email); box.addView(pass)
        AlertDialog.Builder(this)
            .setTitle("Login local")
            .setMessage("Usuário de teste: demo@exemplo.com / Senha@123")
            .setView(box)
            .setPositiveButton("Entrar") { _, _ ->
                val validHash = CryptoUtils.sha256("Senha@123")
                passwordOk = email.text.toString() == "demo@exemplo.com" && CryptoUtils.sha256(pass.text.toString()) == validHash
                updateMfa()
                log(if (passwordOk) "Senha validada. Nunca grave senhas em texto puro." else "Falha no login por senha.")
            }.setNegativeButton("Cancelar", null).show()
    }

    /** PIN: outra forma de prova de conhecimento, comum em apps bancários e telas de desbloqueio. */
    private fun showPinLogin() {
        ask("PIN", "Digite o PIN didático: 1234", true) { value ->
            log(if (value == "1234") "PIN correto: fator de conhecimento validado." else "PIN incorreto.")
        }
    }

    /** Padrão geométrico: simula sequência de pontos como conhecimento memorizado pelo usuário. */
    private fun showPatternLogin() {
        val selected = mutableListOf<Int>()
        val grid = GridLayout(this).apply { columnCount = 3; rowCount = 3; setPadding(24, 24, 24, 24) }
        for (i in 1..9) {
            val b = Button(this).apply {
                text = i.toString()
                setOnClickListener { selected.add(i); text = "✓" }
            }
            grid.addView(b, ViewGroup.LayoutParams(180, 140))
        }
        AlertDialog.Builder(this)
            .setTitle("Padrão geométrico")
            .setMessage("Toque na sequência 1-5-9.")
            .setView(grid)
            .setPositiveButton("Validar") { _, _ ->
                log(if (selected == listOf(1, 5, 9)) "Padrão correto: prova de conhecimento validada." else "Padrão incorreto: recebido $selected")
            }.show()
    }

    /** TOTP: demonstra posse de um token ou dispositivo autenticador. */
    private fun showTotpLogin() {
        ask("Token TOTP", "Digite o token exibido na tela inicial.", true) { value ->
            val current = CryptoUtils.generateTotp(totpSecret)
            totpOk = value == current
            updateMfa()
            log(if (totpOk) "Token TOTP correto: prova de posse validada." else "Token inválido. Atual: $current")
        }
    }

    /** Biometria nativa do Android: impressão digital/face conforme suporte do aparelho. */
    private fun showBiometricPrompt() {
        if (android.os.Build.VERSION.SDK_INT < 28) {
            log("BiometricPrompt exige Android 9/API 28 ou superior.")
            return
        }
        val executor = Executors.newSingleThreadExecutor()
        val prompt = BiometricPrompt.Builder(this)
            .setTitle("Autenticação biométrica")
            .setSubtitle("Prova de características pessoais")
            .setDescription("Use a biometria cadastrada no dispositivo.")
            .setNegativeButton("Cancelar", executor) { _, _ -> log("Biometria cancelada.") }
            .build()
        prompt.authenticate(CancellationSignal(), executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                biometricOk = true
                runOnUiThread { updateMfa(); log("Biometria validada com sucesso.") }
            }
            override fun onAuthenticationFailed() { runOnUiThread { log("Biometria não reconhecida.") } }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) { runOnUiThread { log("Erro biométrico: $errString") } }
        })
    }

    /** Prova de localização: valida se o aparelho está dentro de um raio autorizado. */
    private fun checkLocationProof() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 10)
            log("Permissão de localização solicitada. Execute novamente após conceder.")
            return
        }
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = manager.getProviders(true)
        val last = providers.mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }.maxByOrNull { it.time }
        if (last == null) {
            log("Nenhuma localização disponível. Ative o GPS em ${Settings.ACTION_LOCATION_SOURCE_SETTINGS}.")
            return
        }
        val target = Location("alvo").apply { latitude = AuthConfig.ALLOWED_LATITUDE; longitude = AuthConfig.ALLOWED_LONGITUDE }
        val distance = last.distanceTo(target).toDouble()
        locationOk = distance <= AuthConfig.ALLOWED_RADIUS_METERS
        updateMfa()
        log("Distância do ponto autorizado: %.0f m. Resultado: %s".format(distance, if (locationOk) "autorizado" else "fora da área"))
    }

    /** Inicia login Google pelo fluxo Authorization Code + PKCE, adequado para clientes públicos. */
    private fun startGoogleOidcLogin() {
        if (AuthConfig.GOOGLE_CLIENT_ID.startsWith("DEMO_")) {
            log("Configure AuthConfig.GOOGLE_CLIENT_ID com um Client ID real do Google antes de executar o login real.")
            return
        }
        codeVerifier = CryptoUtils.secureRandomBase64Url(64)
        expectedState = CryptoUtils.secureRandomBase64Url(16)
        val url = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=${CryptoUtils.enc(AuthConfig.GOOGLE_CLIENT_ID)}" +
                "&redirect_uri=${CryptoUtils.enc(AuthConfig.GOOGLE_REDIRECT_URI)}" +
                "&response_type=code" +
                "&scope=${CryptoUtils.enc("openid email profile")}" +
                "&state=${CryptoUtils.enc(expectedState)}" +
                "&code_challenge=${CryptoUtils.enc(CryptoUtils.pkceChallenge(codeVerifier))}" +
                "&code_challenge_method=S256"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    /** Recebe o redirect OAuth, valida state e troca o authorization code por tokens. */
    private fun handleOAuthRedirect(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != "br.com.aulagv.authdemo") return
        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        val error = uri.getQueryParameter("error")
        if (error != null) { log("OAuth retornou erro: $error"); return }
        if (state != expectedState) { log("State inválido. Possível tentativa de CSRF."); return }
        if (code != null) exchangeCodeForTokens(code)
    }

    /** Chamada HTTPS simples ao token endpoint do Google. */
    private fun exchangeCodeForTokens(code: String) {
        thread {
            try {
                val body = "client_id=${CryptoUtils.enc(AuthConfig.GOOGLE_CLIENT_ID)}" +
                        "&code=${CryptoUtils.enc(code)}" +
                        "&code_verifier=${CryptoUtils.enc(codeVerifier)}" +
                        "&grant_type=authorization_code" +
                        "&redirect_uri=${CryptoUtils.enc(AuthConfig.GOOGLE_REDIRECT_URI)}"
                val conn = (URL("https://oauth2.googleapis.com/token").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    outputStream.use { it.write(body.toByteArray()) }
                }
                val response = BufferedReader(InputStreamReader(if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream)).readText()
                runOnUiThread { log("Resposta do token endpoint:\n$response\n\nDica: copie o id_token e decodifique o payload JWT para ver sub, email e profile.") }
            } catch (e: Exception) {
                runOnUiThread { log("Falha na troca do code por tokens: ${e.message}") }
            }
        }
    }

    /** Exige dois ou mais fatores para reduzir risco de fraude de identidade. */
    private fun checkMfa() {
        val secondFactor = totpOk && (biometricOk || locationOk)
        log(if (passwordOk && secondFactor) "MFA aprovado: conhecimento + posse + biometria/localização." else "MFA negado. Exija senha + TOTP + biometria ou localização.")
    }

    private fun updateMfa() { mfaStatus.text = "Status MFA: senha=${ok(passwordOk)}, token=${ok(totpOk)}, biometria=${ok(biometricOk)}, localização=${ok(locationOk)}" }
    private fun ok(v: Boolean) = if (v) "OK" else "pendente"
    private fun log(msg: String) { runOnUiThread { logView.text = msg } }

    private fun ask(title: String, message: String, numeric: Boolean, onValue: (String) -> Unit) {
        val input = EditText(this).apply { inputType = if (numeric) InputType.TYPE_CLASS_NUMBER else InputType.TYPE_CLASS_TEXT }
        AlertDialog.Builder(this).setTitle(title).setMessage(message).setView(input)
            .setPositiveButton("Validar") { _, _ -> onValue(input.text.toString()) }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun title(text: String) = TextView(this).apply { this.text = text; textSize = 23f; typeface = Typeface.DEFAULT_BOLD; setPadding(0, 0, 0, 18) }
    private fun section(text: String) = TextView(this).apply { this.text = text; textSize = 18f; typeface = Typeface.DEFAULT_BOLD; setPadding(0, 22, 0, 8) }
    private fun paragraph(text: String) = TextView(this).apply { this.text = text; textSize = 15f; setPadding(0, 4, 0, 8) }
    private fun button(text: String, action: () -> Unit) = Button(this).apply { this.text = text; gravity = Gravity.CENTER; setOnClickListener { action() } }
}
