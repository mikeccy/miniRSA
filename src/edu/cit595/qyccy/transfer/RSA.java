package edu.cit595.qyccy.transfer;

import java.math.BigInteger;

public class RSA {

    public static long gcd(long a, long b) {
        long t;
        while (b != 0) {
            t = b;
            b = a % t;
            a = t;
        }
        return a;
    }

    public static long modulo(long a, long b, long c) {
        return new BigInteger(a + "").modPow(new BigInteger(b + ""),
                new BigInteger(c + "")).longValue();
    }

    public static long totient(long n) {
        if (isPrime(n))
            return n - 1;
        long count = 0;
        for (long i = 1; i < n; i++)
            if (gcd(n, i) == 1)
                count++;
        return count;
    }

    public static boolean isPrime(long n) {
        if (n % 2 == 0)
            return false;
        for (long i = 3; i * i <= n; i += 2)
            if (n % i == 0)
                return false;
        return true;
    }

    public static long encrypt(long msg_or_cipher, long key, long c) {
        return modulo(msg_or_cipher, key, c);
    }

    public static long decrypt(long y, long d, long c) {
        return modulo(y, d, c);
    }

}
