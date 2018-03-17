package com.example.teodordimitrov.sampleapplication.managers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

/**
 * SecurityManager class based on {@link KeyStore} for handling all encryption and decryption of data.
 * Better approach than just {@link SharedPreferences} and/or {@link android.accounts.AccountManager}/
 * The only way to save your data, even if your phone has been <STRONG>rooted</STRONG>.
 * <p>
 * <p>
 * KeyStore provides two functions:
 * Randomly generates keys and
 * Securely stores the keys
 * With these, storing secrets becomes easy. All you have to do is:
 * Generate a random key when the app runs the first time;
 * When you want to store a secret, retrieve the key from KeyStore, encrypt the data with it, and then store the encrypted data in Preferences.
 * When you want to read a secret, read the encrypted data from Preferences, get the key from KeyStore and then use the key to decrypt the data.
 *
 * @author Credit for the code goes to: https://gist.github.com/Diederikjh/36ae22d5fde9d8f671a70b5d8cada90e
 */
public class SecurityManager {

	private static final String ANDROID_KEY_STORE_NAME = "AndroidKeyStore";
	private static final String AES_MODE_M_OR_GREATER = "AES/GCM/NoPadding";
	private static final String AES_MODE_LESS_THAN_M = "AES/ECB/PKCS7Padding";
	private static final String KEY_ALIAS = "PrivateKeyAlias";
	// TODO update these bytes to be random for IV of encryption
	private static final byte[] FIXED_IV = new byte[]{55, 54, 53, 52, 51, 50,
			49, 48, 47,
			46, 45, 44};
	private static final String CHARSET_NAME = "UTF-8";
	private static final String RSA_ALGORITHM_NAME = "RSA";
	private static final String RSA_MODE = "RSA/ECB/PKCS1Padding";
	private static final String CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_RSA = "AndroidOpenSSL";
	private static final String CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_AES = "BC";
	private static final String SHARED_PREFERENCE_NAME = "com.example.teodordimitrov.sampleapplcation.credentials";
	private static final String ENCRYPTED_KEY_NAME = "encryptedKey";

	private final Context mContext;

	public SecurityManager (Context context) {
		mContext = context;
	}

	private void initKeys () throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, NoSuchProviderException, InvalidAlgorithmParameterException, UnrecoverableEntryException, NoSuchPaddingException, InvalidKeyException {
		KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME);
		keyStore.load(null);

		if (!keyStore.containsAlias(KEY_ALIAS)) {
			initValidKeys();
		} else {
			boolean keyValid = false;
			KeyStore.Entry keyEntry = keyStore.getEntry(KEY_ALIAS, null);

			if (keyEntry instanceof KeyStore.SecretKeyEntry &&
					android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
				keyValid = true;
			}

			if (keyEntry instanceof KeyStore.PrivateKeyEntry && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
				keyValid = true;
			}

			if (!keyValid) {
				// System upgrade or something made the key invalid
				removeKeys(keyStore);
				initValidKeys();
			}

		}
	}

	protected void removeKeys (KeyStore keyStore) throws KeyStoreException {
		keyStore.deleteEntry(KEY_ALIAS);
		removeSavedSharedPreferences();
	}

	private void initValidKeys () throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, CertificateException, UnrecoverableEntryException, NoSuchPaddingException, KeyStoreException, InvalidKeyException, IOException {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			generateKeysForAPIMOrGreater();
		} else {
			generateKeysForAPILessThanM();
		}
	}

	@SuppressLint ("ApplySharedPref")
	private void removeSavedSharedPreferences () {
		SharedPreferences pref = mContext.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		pref.edit().clear().commit();
	}

	private void generateKeysForAPILessThanM () throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, CertificateException, UnrecoverableEntryException, NoSuchPaddingException, KeyStoreException, InvalidKeyException, IOException {
		// Generate a key pair for encryption
		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		end.add(Calendar.YEAR, 30);
		KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(mContext)
				.setAlias(KEY_ALIAS)
				.setSubject(new X500Principal("CN=" + KEY_ALIAS))
				.setSerialNumber(BigInteger.TEN)
				.setStartDate(start.getTime())
				.setEndDate(end.getTime())
				.build();
		KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA_ALGORITHM_NAME, ANDROID_KEY_STORE_NAME);
		kpg.initialize(spec);
		kpg.generateKeyPair();

		saveEncryptedKey();
	}

	@SuppressLint ("ApplySharedPref")
	private void saveEncryptedKey () throws CertificateException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, UnrecoverableEntryException, IOException {
		SharedPreferences pref = mContext.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		String encryptedKeyBase64encoded = pref.getString(ENCRYPTED_KEY_NAME, null);
		if (encryptedKeyBase64encoded == null) {
			byte[] key = new byte[16];
			SecureRandom secureRandom = new SecureRandom();
			secureRandom.nextBytes(key);
			byte[] encryptedKey = rsaEncryptKey(key);
			encryptedKeyBase64encoded = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
			SharedPreferences.Editor edit = pref.edit();
			edit.putString(ENCRYPTED_KEY_NAME, encryptedKeyBase64encoded);
			edit.commit();
		}

	}

	private Key getSecretKeyAPILessThanM () throws CertificateException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, UnrecoverableEntryException, IOException {
		SharedPreferences pref = mContext.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		String encryptedKeyBase64Encoded = pref.getString(ENCRYPTED_KEY_NAME, null);
		// need to check null, omitted here
		byte[] encryptedKey = Base64.decode(encryptedKeyBase64Encoded, Base64.DEFAULT);
		byte[] key = rsaDecryptKey(encryptedKey);
		return new SecretKeySpec(key, "AES");
	}

	@RequiresApi (api = Build.VERSION_CODES.M)
	protected void generateKeysForAPIMOrGreater () throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
		KeyGenerator keyGenerator;
		keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE_NAME);
		keyGenerator.init(
				new KeyGenParameterSpec.Builder(KEY_ALIAS,
						KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
						.setBlockModes(KeyProperties.BLOCK_MODE_GCM)
						.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
						// NOTE no Random IV. According to above this is less secure but acceptably so.
						.setRandomizedEncryptionRequired(false)
						.build());
		// Note according to [docs](https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec.html)
		// this generation will also add it to the keystore.
		keyGenerator.generateKey();
	}

	public String encryptData (String stringDataToEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateException, KeyStoreException, IOException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchProviderException, BadPaddingException, IllegalBlockSizeException {
		initKeys();

		if (stringDataToEncrypt == null) {
			throw new IllegalArgumentException("Data to be decrypted must be non null");
		}

		Cipher cipher;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			cipher = Cipher.getInstance(AES_MODE_M_OR_GREATER);
			cipher.init(Cipher.ENCRYPT_MODE, getSecretKeyAPIMorGreater(),
					new GCMParameterSpec(128, FIXED_IV));
		} else {
			cipher = Cipher.getInstance(AES_MODE_LESS_THAN_M, CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_AES);
			try {
				cipher.init(Cipher.ENCRYPT_MODE, getSecretKeyAPILessThanM());
			} catch (InvalidKeyException | IOException e) {
				// Since the keys can become bad (perhaps because of lock screen change)
				// drop keys in this case.
				removeKeys();
				throw e;
			}
		}

		byte[] encodedBytes = cipher.doFinal(stringDataToEncrypt.getBytes(CHARSET_NAME));
		String encryptedBase64Encoded = Base64.encodeToString(encodedBytes, Base64.DEFAULT);
		return encryptedBase64Encoded;
	}

	public String decryptData (String encryptedData) throws NoSuchPaddingException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateException, KeyStoreException, IOException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchProviderException, BadPaddingException, IllegalBlockSizeException {
		initKeys();

		if (encryptedData == null) {
			throw new IllegalArgumentException("Data to be decrypted must be non null");
		}

		byte[] encryptedDecodedData = Base64.decode(encryptedData, Base64.DEFAULT);

		Cipher c;
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				c = Cipher.getInstance(AES_MODE_M_OR_GREATER);
				c.init(Cipher.DECRYPT_MODE, getSecretKeyAPIMorGreater(), new GCMParameterSpec(128, FIXED_IV));
			} else {
				c = Cipher.getInstance(AES_MODE_LESS_THAN_M, CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_AES);
				c.init(Cipher.DECRYPT_MODE, getSecretKeyAPILessThanM());
			}
		} catch (InvalidKeyException | IOException e) {
			// Since the keys can become bad (perhaps because of lock screen change)
			// drop keys in this case.
			removeKeys();
			throw e;
		}

		byte[] decodedBytes = c.doFinal(encryptedDecodedData);
		return new String(decodedBytes, CHARSET_NAME);
	}

	private Key getSecretKeyAPIMorGreater () throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, UnrecoverableKeyException {
		KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME);
		keyStore.load(null);
		return keyStore.getKey(KEY_ALIAS, null);
	}

	private byte[] rsaEncryptKey (byte[] secret) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, NoSuchProviderException, NoSuchPaddingException, UnrecoverableEntryException, InvalidKeyException {
		KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME);
		keyStore.load(null);

		KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
		Cipher inputCipher = Cipher.getInstance(RSA_MODE, CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_RSA);
		inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, inputCipher);
		cipherOutputStream.write(secret);
		cipherOutputStream.close();

		byte[] encryptedKeyAsByteArray = outputStream.toByteArray();
		return encryptedKeyAsByteArray;
	}

	private byte[] rsaDecryptKey (byte[] encrypted) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableEntryException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException {
		KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME);
		keyStore.load(null);

		KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
		Cipher output = Cipher.getInstance(RSA_MODE, CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_RSA);
		output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());
		CipherInputStream cipherInputStream = new CipherInputStream(
				new ByteArrayInputStream(encrypted), output);
		ArrayList<Byte> values = new ArrayList<>();
		int nextByte;
		while ((nextByte = cipherInputStream.read()) != -1) {
			values.add((byte) nextByte);
		}

		byte[] decryptedKeyAsBytes = new byte[values.size()];
		for (int i = 0; i < decryptedKeyAsBytes.length; i++) {
			decryptedKeyAsBytes[i] = values.get(i);
		}
		return decryptedKeyAsBytes;
	}

	public void removeKeys () throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
		KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME);
		keyStore.load(null);
		removeKeys(keyStore);
	}

}