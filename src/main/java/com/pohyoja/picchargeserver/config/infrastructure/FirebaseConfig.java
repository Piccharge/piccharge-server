package com.pohyoja.picchargeserver.config.infrastructure;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {
    private static final String PICCHARGE_FIREBASE_ADMINSDK_JSON = "piccharge-firebase-adminsdk.json";
    private static final String FIREBASE_DATABASE_URL = "https://piccharge-afbc7-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        try {
            return FirebaseApp.getInstance();
        } catch (IllegalStateException e) {
            ClassPathResource resource =
                    new ClassPathResource(PICCHARGE_FIREBASE_ADMINSDK_JSON);
            InputStream serviceAccount = resource.getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(FIREBASE_DATABASE_URL)
                    .build();

            return FirebaseApp.initializeApp(options);
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }
}
