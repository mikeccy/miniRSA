package edu.cit595.qyccy.transfer;

import java.math.BigInteger;

public class RSA {

    public static long coprime(long input) {
        long result = input - 1;
        boolean isCoprime = false;
        while (!isCoprime) {
            long value = (long) (Math.random() * (input - 1));
            if (value < 2)
                continue;
            if (gcd(input, value) == 1) {
                return value;
            }
        }
        return result;
    }

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

    public static long nthPrime(int n) {
        int counter = 0;
        for (long i = 1;; i++) {
            if (isPrime(i)) {
                counter++;
                if (counter == n)
                    return i;
            }
        }
    }

    public final static long[] generateKeyFromNthPrime(int p, int q) {
        long a = nthPrime(p);
        long b = nthPrime(q);
        long c = a * b;
        long m = (a - 1) * (b - 1);
        long e = RSA.coprime(m);
        long d = RSA.mod_inverse(e, m);
        long[] result = new long[3];
        result[0] = e;
        result[1] = d;
        result[2] = c;
        return result;
    }

    public static long encrypt(long msg_or_cipher, long key, long c) {
        return modulo(msg_or_cipher, key, c);
    }

    public static long decrypt(long y, long d, long c) {
        return modulo(y, d, c);
    }

    public static long bruteFind(long[] pub) {
        long[] root = new long[2];
        long res;
        for (long i = 2; i < pub[1]; i++) {
            long first = gcd(i, pub[1]);
            if (first == i && isPrime(first)) {
                long second = pub[1] / first;
                if (pub[1] % first == 0 && isPrime(second)) {
                    root[0] = first;
                    root[1] = second;
                    break;
                }
            }
        }
        long m = (root[0] - 1) * (root[1] - 1);
        res = mod_inverse(pub[0], m);
        return res;
    }

    public static long mod_inverse(long base, long m) {
        long storeM = m;
        long gcd = gcd(base, m);
        long x = 0;
        long lastX = 1;
        long divideRes, tempM, tempX;
        while (m != 0) {
            divideRes = base / m;
            tempM = m;
            m = base % m;
            base = tempM;
            tempX = x;
            x = lastX - (divideRes * x);
            lastX = tempX;
        }
        if (gcd > 1) {
            return 0;
        } else {

            if (lastX > 0) {
                return lastX % storeM;
            } else {
                return storeM + (lastX % storeM);
            }
        }
    }

}
