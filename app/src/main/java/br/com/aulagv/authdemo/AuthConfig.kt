package br.com.aulagv.authdemo

/**
 * Configurações didáticas do aplicativo.
 *
 * O objetivo deste arquivo é concentrar valores que normalmente seriam definidos
 * no console do provedor de identidade, como Google Cloud/Firebase.
 */
object AuthConfig {
    /**
     * ID de cliente OAuth 2.0 do Google.
     *
     * Substitua pelo Client ID real criado no Google Cloud Console/Firebase.
     * Enquanto estiver com DEMO, a tela apenas explicará o fluxo e não abrirá login real.
     */
    const val GOOGLE_CLIENT_ID = "DEMO_SUBSTITUA_PELO_CLIENT_ID.apps.googleusercontent.com"

    /**
     * URI de retorno registrada no AndroidManifest.xml.
     * Em produção, use o padrão indicado pelo Google para apps instalados.
     */
    const val GOOGLE_REDIRECT_URI = "br.com.aulagv.authdemo://oauth2redirect"

    /** Coordenadas aproximadas de Diadema/SP para demonstrar prova de localização. */
    const val ALLOWED_LATITUDE = -23.6866
    const val ALLOWED_LONGITUDE = -46.6234
    const val ALLOWED_RADIUS_METERS = 30000.0
}
