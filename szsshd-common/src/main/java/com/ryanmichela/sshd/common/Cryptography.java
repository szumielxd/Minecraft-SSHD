package com.ryanmichela.sshd.common;

import java.security.spec.InvalidKeySpecException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import java.math.BigInteger;

// You should run `openssl speed` to see which parts of these algorithms may need
// tweaking in the future as CPUs and GPUs get faster to crack these hashing algos.


class Cryptography 
{
	///////////////////////////////////////////////////////////////////////////////
	// BCrypt-based password hashing algorithm
	///////////////////////////////////////////////////////////////////////////////

	public static String BCrypt_HashPassword(String password) throws NoSuchAlgorithmException
	{
		// This algo handles the salt itself.
		return BCrypt.hashpw(password, BCrypt.gensalt());
	}

	public static Boolean BCrypt_ValidatePassword(String password, String ConfigPassword) throws NoSuchAlgorithmException
	{
		// Unfortunately, the BCrypt library uses String.compareTo which is not
		// hardened against timing attacks so we have to compare the password
		// ourselves otherwise it doesn't work well.
		String test = BCrypt.hashpw(password, ConfigPassword);
		return TimingSafeCmp(test.getBytes(), ConfigPassword.getBytes());
	}

	///////////////////////////////////////////////////////////////////////////////
	// SHA256-based password hashing algorithm
	///////////////////////////////////////////////////////////////////////////////

	public static String SHA256_HashPassword(String password) throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] salt		 = GetSalt();
		int iterations   = 500000; // sha256 is a fast algo to make lots of hashes for, 
		                           // try and make it kinda computationally expensive.
		md.update(salt);
		byte[] bytes = md.digest(password.getBytes());

		// Hash it a few thousand times.
		for (int i = 0; i < iterations; i++)
			bytes = md.digest(bytes);
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++)
			sb.append(Integer.toString((bytes[i] & 0xFF) + 0x100, 16).substring(1));

		return iterations + "$" + ToHex(salt) + "$" + sb.toString();
	}

	public static Boolean SHA256_ValidatePassword(String password, String ConfigPassword) throws NoSuchAlgorithmException
	{
		String[] hparts = ConfigPassword.split("\\$");
		int iterations = Integer.parseInt(hparts[0]);
		byte[] salt = FromHex(hparts[1]);
		String hash = hparts[2];

		MessageDigest md = MessageDigest.getInstance("SHA-256");

		md.update(salt);
		byte[] bytes = md.digest(password.getBytes());

		// Hash it a few thousand times.
		for (int i = 0; i < iterations; i++)
			bytes = md.digest(bytes);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++)
			sb.append(Integer.toString((bytes[i] & 0xFF) + 0x100, 16).substring(1));

		return TimingSafeCmp(hash.getBytes(), sb.toString().getBytes());
	}

	///////////////////////////////////////////////////////////////////////////////
	// PBKDF2-based password hashing algoritm
	///////////////////////////////////////////////////////////////////////////////
	public static String PBKDF2_HashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		char[] passwdchars = password.toCharArray();
		int iterations	 = 20000; // NOTE: Change this as CPUs get faster
		// First: Start getting 16 bytes of guaranteed random data to use for our salt
		byte[] salt = GetSalt();

		PBEKeySpec spec = new PBEKeySpec(passwdchars, salt, iterations, 64*8);
		SecretKeyFactory skf  = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] hash = skf.generateSecret(spec).getEncoded();
		return iterations + "$" + ToHex(salt) + "$" + ToHex(hash);
	}

	public static Boolean PBKDF2_ValidateHash(String password, String ConfigPassword) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		String[] hparts = ConfigPassword.split("\\$");
		int iterations = Integer.parseInt(hparts[0]);
		byte[] salt = FromHex(hparts[1]);
		byte[] hash = FromHex(hparts[2]);

		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, hash.length * 8);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] cmphash = skf.generateSecret(spec).getEncoded();

		return TimingSafeCmp(cmphash, hash);
	}

	///////////////////////////////////////////////////////////////////////////////
	// Utility Functions
	///////////////////////////////////////////////////////////////////////////////

	public static byte[] GetSalt() throws NoSuchAlgorithmException
	{
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt		= new byte[16];
		sr.nextBytes(salt);
		return salt;
	}

	// This is a string comparitor function safe against timing attacks.
	public static boolean TimingSafeCmp(byte[] str1, byte[] str2)
	{
		int diff = str1.length ^ str2.length;
		for (int i = 0; i < str1.length && i < str2.length; i++)
			diff |= str1[i] ^ str2[i];

		return diff == 0;
	}

  	private static byte[] FromHex(String hex) throws NoSuchAlgorithmException
	{
		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; i++)
		{
			bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}

	private static String ToHex(byte[] array) throws NoSuchAlgorithmException
	{
		BigInteger bi			 = new BigInteger(1, array);
		String	 hex			 = bi.toString(16);
		int		   paddingLength = (array.length * 2) - hex.length();
		if (paddingLength > 0)
			return String.format("%0" + paddingLength + "d", 0) + hex;
		else
			return hex;
	}
}