# AuthMethodsDemo — Aula 06: Autenticação, Autorização, OAuth 2.0 e OpenID Connect

Projeto Kotlin nativo para Android Studio, criado como demonstração didática dos conceitos apresentados no PDF da Aula 06.

## O que o app demonstra

1. **Prova de conhecimento**
   - Login com e-mail e senha.
   - PIN numérico.
   - Padrão geométrico.

2. **Prova de posse**
   - Token TOTP de 6 dígitos, simulando autenticador ou token físico/lógico.

3. **Prova de características**
   - Biometria nativa do Android com `BiometricPrompt`.

4. **Prova de localização**
   - Validação por distância geográfica aproximada em relação a Diadema/SP.

5. **Autenticação multifatorial**
   - Combinação de senha + TOTP + biometria ou localização.

6. **OAuth 2.0 / OpenID Connect com Google**
   - Fluxo Authorization Code + PKCE.
   - Abertura do navegador no endpoint de autorização.
   - Recebimento do redirect no app.
   - Troca do authorization code por tokens no token endpoint.

## Como abrir no Android Studio

1. Extraia o ZIP.
2. Abra a pasta `AuthMethodsDemo` no Android Studio.
3. Aguarde o Gradle Sync.
4. Execute em emulador ou aparelho com Android 8.0+.

## Usuários e códigos de teste

- Login local: `demo@exemplo.com`
- Senha: `Senha@123`
- PIN: `1234`
- Padrão geométrico: `1 → 5 → 9`
- TOTP: o token atual aparece na tela inicial do app.

## Configuração do Google OAuth/OIDC

O app vem com OAuth em modo didático. Para login real:

1. Crie um projeto no Google Cloud Console ou Firebase.
2. Cadastre um OAuth Client ID para aplicativo instalado/Android conforme sua configuração.
3. Ajuste `AuthConfig.GOOGLE_CLIENT_ID`.
4. Ajuste o `GOOGLE_REDIRECT_URI` e o `intent-filter` do `AndroidManifest.xml` para corresponder ao Client ID/redirect registrado.
5. Rode o app e clique em **Entrar com Google usando PKCE**.

## Observação de segurança

Este projeto é didático. Em produção:

- Não grave senhas em texto puro.
- Use hashing de senha com Argon2id, bcrypt ou PBKDF2 com salt e custo adequado.
- Prefira bibliotecas oficiais de autenticação quando disponíveis.
- Valide tokens no back-end.
- Aplique princípio do privilégio mínimo para autorização.
- Use HTTPS e armazenamento seguro para credenciais e tokens.
