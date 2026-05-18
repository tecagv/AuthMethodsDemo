# AuthMethodsDemo — Guia Completo de Autenticação, Autorização, OAuth 2.0 e OpenID Connect no Android

Projeto Kotlin nativo para Android Studio criado como demonstração didática dos conceitos práticos de métodos de autenticação, desde fatores básicos até protocolos modernos como OAuth 2.0 e OpenID Connect (OIDC). 

A interface de usuário do aplicativo é construída programaticamente no código, garantindo que o projeto seja compacto, focado em lógica e fácil de estudar.

---

## 🏗️ Estrutura do Código

O projeto está estruturado nos seguintes arquivos principais em `app/src/main/java/br/com/aulagv/authdemo/`:

1. **`MainActivity.kt`**: Contém a lógica principal e a interface construída via código. Centraliza as funções que disparam cada tipo de autenticação.
2. **`CryptoUtils.kt`**: Agrupa funções criptográficas didáticas para demonstrar os conceitos, incluindo geração de *hash* SHA-256, implementação de TOTP (RFC 6238) e cálculo do *Code Challenge* para PKCE.
3. **`AuthConfig.kt`**: Um arquivo de constantes destinado a armazenar as configurações do provedor OAuth (ex.: Google) e os parâmetros geográficos autorizados para a prova de localização.

---

## 🔍 O que o app demonstra (Funcionalidades)

### 1. Prova de Conhecimento (Aquilo que você sabe)
Demonstra como validar algo memorizado pelo usuário.
*   **Login por E-mail e Senha**: Simula um login clássico. O código utiliza um *hash* simples de SHA-256 (`CryptoUtils.sha256`) na senha ("Senha@123") para demonstração.
*   **PIN Numérico**: Valida se o usuário conhece um código específico de 4 dígitos ("1234").
*   **Padrão Geométrico (Pattern)**: Simula o uso de um desenho padrão 3x3 na tela. O código espera a sequência de cliques nas posições 1 → 5 → 9.

### 2. Prova de Posse (Aquilo que você tem)
*   **Token TOTP (Time-Based One-Time Password)**: Demonstra o funcionamento de autenticadores (como Google Authenticator). Em `CryptoUtils.kt`, há uma implementação baseada na RFC 6238, utilizando HMAC-SHA1 e janelas de 30 segundos. A chave do dispositivo está definida estaticamente no código de demonstração. O token válido é exibido na tela principal para testes.

### 3. Prova de Características Pessoais (Inerência / Aquilo que você é)
*   **Biometria Nativa (`BiometricPrompt`)**: Mostra como solicitar o reconhecimento biométrico (impressão digital, facial, etc.) utilizando as APIs nativas do Android, disponíveis a partir da API 28 (Android 9).

### 4. Prova de Localização (Onde você está)
*   **Geofencing Básico**: Solicita a permissão de localização do usuário e compara as coordenadas atuais via GPS/Rede com coordenadas pré-estabelecidas no `AuthConfig.kt` (neste exemplo, configurado para Diadema/SP). Caso o dispositivo esteja em um raio autorizado (ex: 30km), a prova de localização é bem-sucedida.

### 5. Autenticação Multifatorial (MFA)
*   Agrupa os métodos anteriores, exigindo combinações obrigatórias. O projeto simula a exigência de **Conhecimento** (Senha) + **Posse** (TOTP) + **Inerência** (Biometria) ou **Localização** simultaneamente para liberar acesso total.

### 6. OAuth 2.0 / OpenID Connect (OIDC) com Google
*   **Fluxo Authorization Code + PKCE**: Implementação manual (didática) sem uso de bibliotecas de terceiros pesadas para ilustrar como funciona "por baixo dos panos".
*   Gera um `code_verifier` e `code_challenge` (PKCE).
*   Abre o navegador no endpoint de autorização do Google.
*   Intercepta a resposta (redirecionamento) com um `intent-filter` definido no `AndroidManifest.xml` (capturando o *authorization code* e o *state*).
*   Garante segurança mitigando ataques CSRF checando o parâmetro `state`.
*   Realiza uma requisição HTTP POST assíncrona ao servidor de *tokens* do Google para trocar o *code* por *access_token* e *id_token*.

---

## 🚀 Como abrir no Android Studio

1. Faça o download ou clone o repositório.
2. Abra a pasta `AuthMethodsDemo` através do Android Studio (*File* > *Open*).
3. Aguarde o *Gradle Sync* terminar de baixar as dependências.
4. Conecte um aparelho real ou inicie um Emulador (recomendado Android 8.0/API 26 ou superior).
5. Clique em **Run** (Shift + F10) para testar.

---

## 🧪 Dados para Testes Práticos (Mock)

A versão didática possui usuários em *hardcode* para que o desenvolvedor possa testar os métodos sem banco de dados.

*   **Login local (Email)**: `demo@exemplo.com`
*   **Senha local**: `Senha@123`
*   **PIN Numérico**: `1234`
*   **Padrão geométrico**: `1 → 5 → 9`
*   **TOTP**: O código atual de 6 dígitos é exibido dinamicamente direto na tela principal do app para facilitar a validação.

---

## ⚙️ Configurando Login Google OAuth 2.0 (Real)

O projeto vem com constantes de demonstração para o OAuth, que exibirão o fluxo, mas falharão na comunicação real. Para fazer a integração OIDC funcionar:

1. Acesse o [Google Cloud Console](https://console.cloud.google.com/).
2. Crie um projeto novo.
3. Configure a **Tela de consentimento OAuth** (External ou Internal).
4. Vá em **Credenciais** > **Criar Credenciais** > **ID do cliente OAuth**.
5. Selecione o tipo de aplicativo como **Android**.
6. Informe o Nome do pacote (`br.com.aulagv.authdemo`) e o *hash* SHA-1 do seu certificado de depuração (debug keystore).
7. Copie o **Client ID** gerado.
8. No Android Studio, abra o arquivo `app/src/main/java/br/com/aulagv/authdemo/AuthConfig.kt`.
9. Substitua a constante `GOOGLE_CLIENT_ID` pelo Client ID que você copiou.
10. O `GOOGLE_REDIRECT_URI` padrão (`br.com.aulagv.authdemo:/oauth2redirect`) já está configurado no Manifest para interceptar a resposta. 
11. Execute o app novamente e toque em "Entrar com Google usando PKCE". O app conseguirá resgatar e imprimir no log da tela os *tokens* JSON de resposta!

---

## 🔒 Observações de Segurança (Do Didático ao Produção)

**Este projeto possui finalidade estritamente DIDÁTICA. NUNCA utilize este código diretamente em ambiente de produção sem os devidos ajustes de segurança!**

Para construir um aplicativo real de produção seguro, obedeça às seguintes regras:

1.  **Não faça validação de senha local (App-Side)**: Validações de *hash* SHA-256 e senhas nunca devem ser validadas no app. Envie a senha por túnel seguro (HTTPS) para o servidor e faça a verificação de hash (utilizando **Argon2id** ou **Bcrypt**) lá.
2.  **Tokens TOTP e Chaves Secretas**: A geração/validação de tokens TOTP e a `secretKey` correspondente não devem ficar no cliente (App). O cliente deve ser a fonte da verdade de posse (exibir o token), não quem o valida.
3.  **AppAuth / Bibliotecas Oficiais**: Para OAuth 2.0 e OIDC, não gerencie WebViews ou requisições HTTP manualmente. Utilize a biblioteca padrão e recomendada [AppAuth-Android](https://github.com/openid/AppAuth-Android) ou o SDK do *Firebase Authentication*, que cuidam do estado de segurança, renovação e validação de forma nativa e correta.
4.  **Chaves, Senhas ou Secrets**: Não armazene senhas, chaves de API cruas ou *Client Secrets* *hardcoded* dentro de classes Kotlin/Java. Processos de engenharia reversa podem revelar essas strings facilmente.
5.  **Armazenamento Seguro de Tokens**: Se precisar guardar *Access Tokens* e *Refresh Tokens* após um login, utilize tecnologias criptografadas fornecidas pelo Android, como a **EncryptedSharedPreferences** da biblioteca *AndroidX Security* ou o *Keystore*.
