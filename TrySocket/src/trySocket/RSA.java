package trySocket;

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
    
    public static long modulo(long a,long b,long c){
        BigInteger ma = new BigInteger(a+"");
        BigInteger mb = new BigInteger(b+"");
        BigInteger mc = new BigInteger(c+"");
        return ma.modPow(mb, mc).longValue();
    }
    
    public static BigInteger modulo(BigInteger a,BigInteger b,BigInteger c){
        return a.modPow(b, c);
    }
    
    public static long totient(long n) {
        if (isPrime(n)) return n - 1;
        long count = 0;
        for(long i = 1; i < n; i++) 
            if(gcd(n,i) == 1) 
                count++;
        return count;
    }
    
    public static boolean isPrime(long n) {
        if (n % 2 == 0) return false;
        for (long i = 3; i * i <= n; i += 2)
            if (n % i == 0)
                return false;
        return true;
    }
    
    public long encrypt(long msg_or_cipher, long key, long c) {
        return modulo(msg_or_cipher, key, c);
    }
    
    public static BigInteger encrypt(BigInteger msg_or_cipher, BigInteger key, BigInteger c) {
        return modulo(msg_or_cipher, key, c);
    }
    
    public static long decrypt(long y, long d, long c){
        return modulo(y,d,c);
    }
    
    public static BigInteger decrypt(BigInteger y, BigInteger d, BigInteger c){
        return modulo(y,d,c);
    }
    public static int generateNthPrime(int input) {
		int counter = 0;
		int result = 1;
		for (int i = 1;; i++) {
			if (isPrime(i))
				counter++;
			if (counter - 1 == input) {
				result = i;
				break;
			}
		}
		return result;
	}
    public static long coprime(long input) {
		long result = 1;
		boolean isCoprime = false;
		while (!isCoprime) {
			long value = 2 + (long) (Math.random() * ((input - 2) + 1));
			// min is 2, max is input
			if (gcd(input, value) == 1) {
				result = value;
				isCoprime = true;
			}
		}
		return result;
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
    public static long bruteFind(long[] pub){
		long[] root=new long[2]; 
		long res;
		for(long i=2;i<pub[1];i++){
			long first=gcd(i,pub[1]);
			if(first==i&&isPrime(first)){
				long second=pub[1]/first;
				if(pub[1]%first==0&&isPrime(second)){
					//System.out.println("a="+first+",b="+second);
					root[0]=first;
					root[1]=second;
					break;
				}
			}
		}
		long m=(root[0]-1)*(root[1]-1);
		res=mod_inverse(pub[0],m);
		return res;
	}
}
